package com.github.drxaos.mpsync.sync;

import com.github.drxaos.mpsync.bus.Bus;
import com.github.drxaos.mpsync.bus.ServerInfo;
import com.github.drxaos.mpsync.sim.Simulation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class ClientSimSync<STATE, INPUT, INFO> extends Thread {

    Simulation<STATE, INPUT, INFO> simulation;

    Bus<STATE, INPUT, INFO> bus;

    HashSet<SimState<STATE>> states = new HashSet<SimState<STATE>>();
    HashSet<SimInput<INPUT>> inputs = new HashSet<SimInput<INPUT>>();
    HashSet<SimState<STATE>> futureStates = new HashSet<SimState<STATE>>();

    final int ADJUST_INTERVALS = 3;
    final int KEEP_ADJUST_INTERVALS = 20;
    int currentFrame = 0;
    int lastSyncFrame = 0;
    long lastSyncTimestamp = 0;
    long lastFrameTimestamp = 0;
    int fullStateFramesInterval = 1000;
    LinkedList<Long> syncIntervals = new LinkedList<Long>();
    long oneFrameInterval = Long.MAX_VALUE;

    int clientId = 0;

    public ClientSimSync(Simulation<STATE, INPUT, INFO> simulation, Bus<STATE, INPUT, INFO> bus) {
        super("ClientSimSync");
        this.simulation = simulation;
        this.bus = bus;
    }

    public void gotFullState(SimState<STATE> simState, boolean adjust) {
        System.out.println("FULLSTATE: " + currentFrame + " -> " + simState.frame);

        int lag = simState.frame - currentFrame;
        if (adjust && lag < fullStateFramesInterval / 4) {
            futureStates.add(simState);
        } else {
            lag = 0;

            // merge
            simulation.setFullState(simState.state);
            currentFrame = simState.frame;

            for (Iterator<SimState<STATE>> iterator = states.iterator(); iterator.hasNext(); ) {
                SimState<STATE> next = iterator.next();
                if (next.frame >= currentFrame) {
                    iterator.remove();
                }
            }
            states.add(simState);
        }

        // adjust speed
        if (adjust) {
            if (lastSyncTimestamp > 0) {
                syncIntervals.add(simState.timestamp - lastSyncTimestamp);
                while (syncIntervals.size() > KEEP_ADJUST_INTERVALS) {
                    syncIntervals.remove(0);
                }
                long averageTimeInterval = 0;
                long addCount = 0;
                long k = 0;
                for (Long syncInterval : syncIntervals) {
                    k++;
                    averageTimeInterval += syncInterval * k;
                    addCount += k;
                }
                averageTimeInterval = averageTimeInterval / addCount;
                fullStateFramesInterval = simState.frame - lastSyncFrame + lag;
                oneFrameInterval = averageTimeInterval / fullStateFramesInterval;
            }
            lastSyncTimestamp = simState.timestamp;
            lastSyncFrame = simState.frame;

            System.out.println("ADJUST: " + oneFrameInterval + "ms * " + fullStateFramesInterval + " frames");
        }
    }

    @Override
    public void run() {
        mainLoop:
        while (true) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }

            ServerInfo<INFO> serverInfo = bus.getServerInfo();
            if (serverInfo != null) {
                clientId = serverInfo.clientId;
                //fullStateFramesInterval = serverInfo.fullStateFramesInterval;
                //oneFrameInterval = serverInfo.oneFrameInterval;
            }

            if (syncIntervals.size() < ADJUST_INTERVALS) {
                // Initializing
                SimState<STATE> fullState = bus.getFullState();
                if (fullState != null) {
                    gotFullState(fullState, true);
                }
                continue;
            }

            // handle sync
            SimState<STATE> fullState = bus.getFullState();
            if (fullState != null) {
                gotFullState(fullState, true);
                continue;
            }

            // clean old inputs
            for (Iterator<SimInput<INPUT>> iterator = inputs.iterator(); iterator.hasNext(); ) {
                SimInput<INPUT> next = iterator.next();
                if (next.frame < currentFrame - fullStateFramesInterval * 4) {
                    iterator.remove();
                }
            }

            INPUT input = simulation.getInput();
            if (input != null) {
                SimInput<INPUT> simInput = new SimInput<INPUT>(currentFrame, input);
                simInput.client = clientId;
                bus.sendInput(simInput);
                simulation.input(simInput);
                inputs.add(simInput);
            }

            handleIncomingInputs();

            if (currentFrame - lastSyncFrame > fullStateFramesInterval * 4) {
                // no server activity - hang
                continue;
            }

            // continue prediction
            if (lastFrameTimestamp + oneFrameInterval < System.currentTimeMillis()) {

                if (futureStates.contains(new SimState<STATE>(currentFrame + 1, null))) {
                    // haz state
                    for (Iterator<SimState<STATE>> iterator = futureStates.iterator(); iterator.hasNext(); ) {
                        SimState<STATE> futureState = iterator.next();
                        if (futureState.frame == currentFrame + 1) {
                            gotFullState(futureState, false);
                            for (SimInput<INPUT> simInput : inputs) {
                                if (simInput.frame == currentFrame) {
                                    simulation.input(simInput);
                                }
                            }
                            iterator.remove();
                            continue mainLoop;
                        }
                    }
                }
                simulation.step();
                currentFrame++;
                lastFrameTimestamp = System.currentTimeMillis();
                states.add(new SimState<STATE>(currentFrame, simulation.getFullState()));
                for (Iterator<SimState<STATE>> iterator = states.iterator(); iterator.hasNext(); ) {
                    SimState<STATE> next = iterator.next();
                    if (next.frame < currentFrame - fullStateFramesInterval * 2) {
                        iterator.remove();
                    }
                }

                for (SimInput<INPUT> simInput : inputs) {
                    if (simInput.frame == currentFrame) {
                        simulation.input(simInput);
                    }
                }
            }
        }
    }

    void handleIncomingInputs() {
        // handle inputs
        SimInput<INPUT> input = bus.getInput();
        while (input != null && input.client == clientId) {
            input = bus.getInput();
        }
        if (input != null) {
            int firstFrame = input.frame;
            while (input != null) {
                if (input.frame < firstFrame) {
                    firstFrame = input.frame;
                }
                inputs.add(input);
                input = bus.getInput();
                while (input != null && input.client == clientId) {
                    input = bus.getInput();
                }
            }
            // merge
            int actualFrame = currentFrame;
            SimState<STATE> simState = findNearestState(firstFrame);

            if (simState == null) {
                // wait for next full state
                return;
            }
            gotFullState(simState, false);
            for (SimInput<INPUT> simInput : inputs) {
                if (simInput.frame == currentFrame) {
                    simulation.input(simInput);
                }
            }

            // seek
            while (currentFrame < actualFrame) {
                simulation.step();
                currentFrame++;
                states.add(new SimState<STATE>(currentFrame, simulation.getFullState()));
                for (Iterator<SimState<STATE>> iterator = states.iterator(); iterator.hasNext(); ) {
                    SimState<STATE> next = iterator.next();
                    if (next.frame < currentFrame - fullStateFramesInterval * 2) {
                        iterator.remove();
                    }
                }

                for (SimInput<INPUT> simInput : inputs) {
                    if (simInput.frame == currentFrame) {
                        simulation.input(simInput);
                    }
                }
            }
        }
    }

    private SimState<STATE> findNearestState(int frame) {
        SimState<STATE> result = null;

        for (SimState<STATE> state : states) {
            if (state.frame > frame) {
                continue;
            }
            if (result == null || result.frame < state.frame) {
                result = state;
            }
        }

        return result;
    }
}

package com.github.drxaos.mpsync.sync;

import com.github.drxaos.mpsync.bus.Bus;
import com.github.drxaos.mpsync.sim.Simulation;

import java.util.Iterator;
import java.util.LinkedList;

public class ClientSimSync<STATE, INPUT> extends Thread {

    Simulation<STATE, INPUT> simulation;

    Bus<STATE, INPUT> bus;

    LinkedList<SimState<STATE>> states = new LinkedList<SimState<STATE>>();
    LinkedList<SimInput<INPUT>> inputs = new LinkedList<SimInput<INPUT>>();

    final int ADJUST_INTERVALS = 1;
    final int KEEP_ADJUST_INTERVALS = 20;
    int currentFrame = 0;
    int lastSyncFrame = 0;
    long lastSyncTimestamp = 0;
    long lastFrameTimestamp = 0;
    int fullStateFramesInterval = 1000;
    LinkedList<Long> syncIntervals = new LinkedList<Long>();
    long oneFrameInterval = Long.MAX_VALUE;

    public ClientSimSync(Simulation<STATE, INPUT> simulation, Bus<STATE, INPUT> bus) {
        super("ClientSimSync");
        this.simulation = simulation;
        this.bus = bus;
    }

    public void gotFullState(SimState<STATE> simState, boolean adjust) {

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


        // adjust speed
        if (adjust) {
            if (lastSyncTimestamp > 0) {
                syncIntervals.add(simState.timestamp - lastSyncTimestamp);
                while (syncIntervals.size() > KEEP_ADJUST_INTERVALS) {
                    syncIntervals.remove(0);
                }
                long averageTimeInterval = 0;
                for (Long syncInterval : syncIntervals) {
                    averageTimeInterval += syncInterval;
                }
                averageTimeInterval = averageTimeInterval / syncIntervals.size();
                fullStateFramesInterval = simState.frame - lastSyncFrame;
                oneFrameInterval = averageTimeInterval / fullStateFramesInterval;
            }
            lastSyncTimestamp = simState.timestamp;
            lastSyncFrame = simState.frame;

            //System.out.println("ADJUST: " + oneFrameInterval + "ms * " + fullStateFramesInterval + " frames");
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
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

            // handle inputs
            SimInput<INPUT> input = bus.getInput();
            if (input != null) {
                int firstFrame = input.frame;
                while (input != null) {
                    if (input.frame < firstFrame) {
                        firstFrame = input.frame;
                    }
                    inputs.add(input);
                    input = bus.getInput();
                }
                // merge
                int actualFrame = currentFrame;
                SimState<STATE> simState = findNearestState(firstFrame);

                if (simState == null) {
                    // wait for next full state
                    continue;
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
                    while (states.size() > fullStateFramesInterval * 4) {
                        states.remove(0);
                    }

                    for (SimInput<INPUT> simInput : inputs) {
                        if (simInput.frame == currentFrame) {
                            simulation.input(simInput);
                        }
                    }
                }
            }

            if (currentFrame - lastSyncFrame > fullStateFramesInterval * 4) {
                // no server activity - hang
                continue;
            }

            // continue prediction
            if (lastFrameTimestamp + oneFrameInterval < System.currentTimeMillis()) {
                simulation.step();
                currentFrame++;
                lastFrameTimestamp = System.currentTimeMillis();
                states.add(new SimState<STATE>(currentFrame, simulation.getFullState()));
                while (states.size() > fullStateFramesInterval * 4) {
                    states.remove(0);
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

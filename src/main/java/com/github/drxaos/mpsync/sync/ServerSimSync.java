package com.github.drxaos.mpsync.sync;

import com.github.drxaos.mpsync.bus.Bus;
import com.github.drxaos.mpsync.bus.ServerInfo;
import com.github.drxaos.mpsync.sim.Simulation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class ServerSimSync<STATE, INPUT, INFO> extends Thread {

    Simulation<STATE, INPUT, INFO> simulation;

    Bus<STATE, INPUT, INFO> bus;

    HashSet<SimState<STATE>> states = new HashSet<SimState<STATE>>();
    HashSet<SimInput<INPUT>> inputs = new HashSet<SimInput<INPUT>>();

    int currentFrame = 0;
    long lastFrameTimestamp = 0;
    int fullStateFramesInterval = 50;
    long oneFrameInterval = 25;

    public ServerSimSync(Simulation<STATE, INPUT, INFO> simulation, Bus<STATE, INPUT, INFO> bus) {
        super("ServerSimSync");
        this.simulation = simulation;
        this.bus = bus;
        this.bus.setServerInfo(new ServerInfo(fullStateFramesInterval, oneFrameInterval, simulation.getInfo()));
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }

            if (lastFrameTimestamp + oneFrameInterval <= System.currentTimeMillis()) {
                simulation.step();
                currentFrame++;
                lastFrameTimestamp = System.currentTimeMillis();
                for (SimInput<INPUT> simInput : inputs) {
                    if (simInput.frame == currentFrame) {
                        simulation.input(simInput);
                    }
                }

                SimState<STATE> simState = new SimState<STATE>(currentFrame, simulation.getFullState());

                states.add(simState);
                for (Iterator<SimState<STATE>> iterator = states.iterator(); iterator.hasNext(); ) {
                    SimState<STATE> next = iterator.next();
                    if (next.frame < currentFrame - fullStateFramesInterval) {
                        iterator.remove();
                    }
                }

                // clean old inputs
                for (Iterator<SimInput<INPUT>> iterator = inputs.iterator(); iterator.hasNext(); ) {
                    SimInput<INPUT> next = iterator.next();
                    if (next.frame < currentFrame - fullStateFramesInterval * 2) {
                        iterator.remove();
                    }
                }

                if (currentFrame % fullStateFramesInterval == 0) {
                    bus.sendFullState(simState);
                    continue;
                }

                while (bus.getFullState() != null) {
                    // ignore
                }

                INPUT input = simulation.getInput();
                if (input != null) {
                    SimInput<INPUT> simInput = new SimInput<INPUT>(currentFrame, input);
                    bus.sendInput(simInput);
                    simulation.input(simInput);
                    inputs.add(simInput);
                }

                handleIncomingInputs();
            }
        }
    }

    void handleIncomingInputs() {
        // handle inputs
        SimInput<INPUT> input = bus.getInput();
        if (input != null) {
            ArrayList<SimInput<INPUT>> newInputs = new ArrayList<SimInput<INPUT>>();

            int firstFrame = input.frame;
            while (input != null) {
                if (input.frame < currentFrame + fullStateFramesInterval) { // ignore far future inputs
                    if (input.frame < firstFrame) {
                        firstFrame = input.frame;
                    }
                    inputs.add(input);
                    newInputs.add(input);
                }
                input = bus.getInput();
            }
            if (firstFrame >= currentFrame) {
                // apply current, send all
                for (SimInput<INPUT> simInput : newInputs) {
                    if (simInput.frame == currentFrame) {
                        simulation.input(simInput);
                    }
                    bus.sendInput(simInput);
                }
                return;
            }
            // merge
            int actualFrame = currentFrame;
            SimState<STATE> simState = findNearestState(firstFrame);

            System.out.println("Found state: " + simState);

            if (simState == null) {
                // no saved states found, ignore all inputs
                return;
            }

            simulation.setFullState(simState.state);
            currentFrame = simState.frame;
            for (Iterator<SimState<STATE>> iterator = states.iterator(); iterator.hasNext(); ) {
                SimState<STATE> next = iterator.next();
                if (next.frame > currentFrame) {
                    iterator.remove();
                }
            }

            for (SimInput<INPUT> simInput : inputs) {
                if (simInput.frame == currentFrame) {
                    simulation.input(simInput);
                    if (newInputs.contains(simInput)) {
                        System.out.println("Send input");
                        bus.sendInput(simInput);
                    }
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
                        if (newInputs.contains(simInput)) {
                            System.out.println("Send input");
                            bus.sendInput(simInput);
                        }
                    }
                }
            }

            // send future
            for (SimInput<INPUT> simInput : newInputs) {
                if (simInput.frame > currentFrame) {
                    bus.sendInput(simInput);
                }
            }
        }
    }

    private SimState<STATE> findNearestState(int frame) {
        SimState<STATE> result = null;
        SimState<STATE> oldestFrame = null;

        for (SimState<STATE> state : states) {
            if (oldestFrame == null || state.frame < oldestFrame.frame) {
                oldestFrame = state;
            }
            if (state.frame > frame) {
                continue;
            }
            if (result == null || result.frame < state.frame) {
                result = state;
            }
        }

        return result != null ? result : oldestFrame;
    }
}

package com.github.drxaos.mpsync.sync;

import com.github.drxaos.mpsync.bus.Bus;
import com.github.drxaos.mpsync.bus.ServerInfo;
import com.github.drxaos.mpsync.sim.Simulation;

import java.util.HashMap;
import java.util.Iterator;

public class ServerSimSync<STATE, INPUT, INFO> extends Thread {

    Simulation<STATE, INPUT, INFO> simulation;
    Bus<STATE, INPUT, INFO> bus;

    HashMap<SimState<STATE>, SimState<STATE>> states = new HashMap<SimState<STATE>, SimState<STATE>>();
    HashMap<SimInput<INPUT>, SimInput<INPUT>> inputs = new HashMap<SimInput<INPUT>, SimInput<INPUT>>();

    int keyFrameInterval = 50;
    int frameTime = 1000 / 25;

    boolean shouldMergeInputs = false;
    long mergeFrom = 0;

    long currentFrame = 0;
    long currentFrameStart = 0;

    public boolean debug = false;

    private void debug(String message) {
        if (debug) {
            System.out.println(message);
        }
    }

    public ServerSimSync(Simulation<STATE, INPUT, INFO> simulation, Bus<STATE, INPUT, INFO> bus) {
        super("ServerSimSync");
        this.simulation = simulation;
        this.bus = bus;
        this.bus.setServerInfo(new ServerInfo<INFO>(keyFrameInterval, keyFrameInterval * frameTime, simulation.getInfo()));
    }

    @Override
    public void run() {
        while (true) {
            sleep();

            if (currentFrameStart + frameTime <= System.currentTimeMillis()) {

                readIncomingInputs();
                readUserInput();

                try {
                    simulation.lockView();
                    handleIncomingInputs();

                    // apply inputs
                    applyInputs(inputs, currentFrame);

                    // simulation
                    simulation.step();
                    currentFrame++;
                    currentFrameStart = System.currentTimeMillis();

                } finally {
                    simulation.unlockView();
                }

                // save state
                SimState<STATE> simState = new SimState<STATE>(currentFrame, simulation.getFullState());
                states.put(simState, simState);

                cleanOldData();
                sendFullState(simState);
            }
        }
    }

    private void readUserInput() {
        // read user input
        INPUT input = simulation.getInput();
        if (input != null) {
            SimInput<INPUT> simInput = new SimInput<INPUT>(currentFrame, input);
            inputs.put(simInput, simInput);
            bus.sendInput(simInput);
        }
    }

    private void sendFullState(SimState<STATE> simState) {
        // send fullstate
        if (currentFrame % keyFrameInterval == 0) {
            debug("Send fullstate: " + currentFrame);
            SimState<STATE> simStatePair = new SimState<STATE>(simState);
            // send old state for clients with unaccepted inputs
            simStatePair.prevState = findNearestState(currentFrame - keyFrameInterval);
            bus.sendFullState(simStatePair);
        }

        // ignore incoming fullstates
        while (bus.getFullState() != null) {
            // ignore
        }
    }

    private void cleanOldData() {
        // remove old states
        for (Iterator<SimState<STATE>> iterator = states.values().iterator(); iterator.hasNext(); ) {
            SimState<STATE> next = iterator.next();
            if (next.frame < currentFrame - keyFrameInterval * 2) {
                iterator.remove();
            }
        }

        // clean old inputs
        for (Iterator<SimInput<INPUT>> iterator = inputs.values().iterator(); iterator.hasNext(); ) {
            SimInput<INPUT> next = iterator.next();
            if (next.frame < currentFrame - keyFrameInterval * 2) {
                iterator.remove();
            }
        }
    }

    private void sleep() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
        }
    }

    private void applyInputs(HashMap<SimInput<INPUT>, SimInput<INPUT>> inputs, long frame) {
        for (SimInput<INPUT> simInput : inputs.values()) {
            if (simInput.frame == currentFrame) {
                debug("Apply input " + simInput.client + ":" + simInput.frame + "");
                simulation.input(simInput);
            }
        }
    }

    void readIncomingInputs() {
        // handle inputs
        SimInput<INPUT> input = bus.getInput();
        if (input != null) {
            // loading all inputs
            SimState<STATE> earliestState = getEarliestState(states);
            while (input != null) {
                if (input.frame >= earliestState.frame) {
                    if (input.frame < currentFrame &&
                            (!shouldMergeInputs || mergeFrom > input.frame)) {
                        mergeFrom = input.frame;
                        shouldMergeInputs = true;
                    }
                    debug("Save input " + input.client + ":" + input.frame + "");
                    inputs.put(input, input);
                    bus.sendInput(input);
                }
                input = bus.getInput();
            }

            if (shouldMergeInputs) {
                removeStatesAfter(states, mergeFrom);
            }
        }
    }

    private void removeStatesAfter(HashMap<SimState<STATE>, SimState<STATE>> states, long fromFrame) {
        for (Iterator<SimState<STATE>> iterator = states.values().iterator(); iterator.hasNext(); ) {
            SimState<STATE> next = iterator.next();
            if (next.frame > fromFrame) {
                iterator.remove();
            }
        }
    }

    private SimState<STATE> getEarliestState(HashMap<SimState<STATE>, SimState<STATE>> states) {
        SimState<STATE> result = null;
        for (SimState<STATE> simState : states.values()) {
            if (result == null || result.frame > simState.frame) {
                result = simState;
            }
        }
        return result;
    }

    private void applyState(SimState<STATE> simState) {
        simulation.setFullState(simState.state);
        currentFrame = simState.frame;
        debug("currentFrame=" + currentFrame + " at applyState");
        currentFrameStart = System.currentTimeMillis();
    }

    void handleIncomingInputs() {
        if (shouldMergeInputs) {
            shouldMergeInputs = false;

            // merge
            long actualFrame = currentFrame;
            SimState<STATE> simState = findNearestState(mergeFrom);

            debug("Found state: " + simState);

            if (simState == null) {
                // no saved states found, ignore all inputs
                return;
            }

            applyState(simState);
            seek(actualFrame);
        }
    }

    private void seek(long toFrame) {
        // seek
        while (currentFrame < toFrame) {
            applyInputs(inputs, currentFrame);
            simulation.step();
            currentFrame++;
            debug("currentFrame=" + currentFrame + " at handleIncomingInputs");
            SimState<STATE> newSimState = new SimState<STATE>(currentFrame, simulation.getFullState());
            states.put(newSimState, newSimState);
        }
    }

    private SimState<STATE> findNearestState(long frame) {
        SimState<STATE> result = null;
        SimState<STATE> oldestFrame = null;

        for (SimState<STATE> state : states.values()) {
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

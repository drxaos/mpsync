package com.github.drxaos.mpsync.sync;

import com.github.drxaos.mpsync.bus.Bus;
import com.github.drxaos.mpsync.bus.ServerInfo;
import com.github.drxaos.mpsync.sim.Simulation;

import java.util.HashMap;
import java.util.Iterator;

public class ClientSimSync<STATE, INPUT, INFO> extends Thread {

    Simulation<STATE, INPUT, INFO> simulation;
    Bus<STATE, INPUT, INFO> bus;

    HashMap<SimState<STATE>, SimState<STATE>> states = new HashMap<SimState<STATE>, SimState<STATE>>();
    HashMap<SimInput<INPUT>, SimInput<INPUT>> inputs = new HashMap<SimInput<INPUT>, SimInput<INPUT>>();
    HashMap<SimState<STATE>, SimState<STATE>> serverStates = new HashMap<SimState<STATE>, SimState<STATE>>();
    HashMap<SimInput<INPUT>, SimInput<INPUT>> serverInputs = new HashMap<SimInput<INPUT>, SimInput<INPUT>>();
    ServerInfo<INFO> serverInfo = null;

    int keyFrameInterval = 1000;
    int keyFrameIntervalTime = 60000;
    int clientId = 0;

    boolean shouldMergeInputs = false;
    long mergeFrom = 0;

    long lastSyncFrame = 0;
    int frameTime = 60000;
    long currentFrame = 0;
    long currentFrameStart = 0;
    int lag = 0;

    public boolean debug = false;

    private void debug(String message) {
        if (debug) {
            System.out.println(message);
        }
    }

    public ClientSimSync(Simulation<STATE, INPUT, INFO> simulation, Bus<STATE, INPUT, INFO> bus) {
        super("ClientSimSync");
        this.simulation = simulation;
        this.bus = bus;
    }

    @Override
    public void run() {

        waitForServerInfo();

        while (true) {
            sleep();
            cleanOldData();
            handleIncomingState();
            readIncomingInputs();
            clientPrediction();
        }
    }

    private SimState<STATE> findState(HashMap<SimState<STATE>, SimState<STATE>> states, long frame) {
        for (SimState<STATE> simState : states.values()) {
            if (simState.frame == frame) {
                return simState;
            }
        }
        return null;
    }

    private void applyInputs(HashMap<SimInput<INPUT>, SimInput<INPUT>> inputs, long frame) {
        for (SimInput<INPUT> simInput : inputs.values()) {
            if (simInput.frame == currentFrame) {
                debug("Apply input " + simInput.client + ":" + simInput.frame + "");
                simulation.input(simInput);
            }
        }
    }

    private boolean hasUnacceptedInputs(HashMap<SimInput<INPUT>, SimInput<INPUT>> inputs) {
        for (SimInput<INPUT> simInput : inputs.values()) {
            if (!simInput.accepted) {
                return true;
            }
        }
        return false;
    }

    public void handleIncomingState() {
        // handle incoming fullstate
        SimState<STATE> simState = bus.getFullState();
        if (simState != null) {
            debug("FULLSTATE: " + currentFrame + " -> " + simState.frame);

            if (hasUnacceptedInputs(inputs) && simState.prevState != null) { // merge previous server state
                debug("Has unaccepted inputs, merge " + simState.prevState.frame);
                long actualFrame = currentFrame;
                applyState(simState.prevState);
                seek(actualFrame);
            } else { // save new state
                serverStates.put(simState, simState);
            }
            lastSyncFrame = simState.frame;

            lag = (int) (simState.frame - currentFrame);
            if (lag < 0) {
                // make simulation slower
                lag--;
            }

            if (Math.abs(lag) > keyFrameInterval) {
                // reset to current
                lag = 0;
                frameTime = keyFrameIntervalTime / keyFrameInterval;

                applyState(simState);

            } else {
                // compensate lag
                frameTime = keyFrameIntervalTime / (keyFrameInterval + lag);
            }

            debug("ADJUST: " + frameTime + "ms * " + keyFrameInterval + " frames + " + lag + " lag");
        }
    }

    private void applyState(SimState<STATE> simState) {
        simulation.setFullState(simState.state);
        currentFrame = simState.frame;
        debug("currentFrame=" + currentFrame + " at applyState");
        currentFrameStart = System.currentTimeMillis();
    }

    private void sleep() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
        }
    }

    private void readUserInput() {
        // handle user input
        INPUT input = simulation.getInput();
        if (input != null) {
            SimInput<INPUT> simInput = new SimInput<INPUT>(currentFrame, input);
            simInput.client = clientId;
            inputs.put(simInput, simInput); // save
            bus.sendInput(simInput); // send

            removeStatesAfter(serverStates, currentFrame);
            removeStatesAfter(states, currentFrame);
        }
    }

    private void waitForServerInfo() {
        // handle incoming server info
        ServerInfo<INFO> serverInfo = bus.getServerInfo();
        while (serverInfo == null) {
            serverInfo = bus.getServerInfo();
        }
        this.serverInfo = serverInfo;
        clientId = serverInfo.clientId;
        keyFrameInterval = serverInfo.keyFrameInterval;
        keyFrameIntervalTime = serverInfo.keyFrameIntervalTime;
        frameTime = keyFrameIntervalTime / keyFrameInterval;
        simulation.setServerInfo(serverInfo.info);
    }

    private void cleanOldData() {
        // clean old server states
        long earliestServerFrame = -1;
        for (Iterator<SimState<STATE>> iterator = serverStates.values().iterator(); iterator.hasNext(); ) {
            SimState<STATE> next = iterator.next();
            if (next.frame < currentFrame - keyFrameInterval * 3) {
                iterator.remove();
            } else if (earliestServerFrame == -1 || earliestServerFrame > next.frame) {
                earliestServerFrame = next.frame;
            }
        }

        // clean old server inputs
        for (Iterator<SimInput<INPUT>> iterator = serverInputs.values().iterator(); iterator.hasNext(); ) {
            SimInput<INPUT> next = iterator.next();
            if (next.frame < earliestServerFrame) {
                iterator.remove();
            }
        }

        // clean old states
        for (Iterator<SimState<STATE>> iterator = states.values().iterator(); iterator.hasNext(); ) {
            SimState<STATE> next = iterator.next();
            if (next.frame < earliestServerFrame) {
                iterator.remove();
            }
        }

        // clean old inputs
        for (Iterator<SimInput<INPUT>> iterator = inputs.values().iterator(); iterator.hasNext(); ) {
            SimInput<INPUT> next = iterator.next();
            if (next.frame < earliestServerFrame) {
                iterator.remove();
            }
        }
    }

    void readIncomingInputs() {
        // handle inputs
        SimInput<INPUT> input = bus.getInput();
        if (input != null) {
            // loading all inputs
            boolean onlyMyInputs = true;
            SimState<STATE> earliestState = getEarliestState(states);
            while (input != null) {
                if (input.frame >= earliestState.frame &&
                        input.frame < currentFrame &&
                        (!shouldMergeInputs || mergeFrom > input.frame)) {
                    mergeFrom = input.frame;
                    shouldMergeInputs = true;
                    if (input.client != clientId) {
                        onlyMyInputs = false;
                    }
                }
                if (input.client == clientId) {
                    debug("Save my input " + input.client + ":" + input.frame + "");
                    SimInput<INPUT> myInput = inputs.get(input);
                    if (myInput != null) {
                        myInput.accepted = true;
                    } else {
                        inputs.put(input, input);
                    }
                } else {
                    debug("Save srv input " + input.client + ":" + input.frame + "");
                    serverInputs.put(input, input);
                }
                input = bus.getInput();
            }

            if (shouldMergeInputs) {
                if (onlyMyInputs) {
                    shouldMergeInputs = false;
                } else {
                    removeStatesAfter(serverStates, mergeFrom);
                    removeStatesAfter(states, mergeFrom);
                }
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

    private void clientPrediction() {
        // client prediction
        if (currentFrameStart + frameTime < System.currentTimeMillis()) {

            // stop if no sync from server
            if (currentFrame - lastSyncFrame > keyFrameInterval * 5) {
                return;
            }

            handleIncomingInputs();
            readUserInput();

            // find and apply saved future state
            SimState<STATE> serverState = findState(serverStates, currentFrame + 1);
            if (serverState != null) {
                applyState(serverState);
                return;
            }

            // simulation
            applyInputs(serverInputs, currentFrame);
            applyInputs(inputs, currentFrame);
            simulation.step();
            currentFrame++;
            currentFrameStart = System.currentTimeMillis();
            SimState<STATE> simState = new SimState<STATE>(currentFrame, simulation.getFullState());
            states.put(simState, simState);
        }
    }

    void handleIncomingInputs() {
        if (shouldMergeInputs) {
            shouldMergeInputs = false;

            // merge
            long actualFrame = currentFrame;
            SimState<STATE> simState = findNearestState(mergeFrom);

            if (simState == null) {
                // wait for next full state
                return;
            }

            applyState(simState);
            seek(actualFrame);
        }
    }

    private void seek(long toFrame) {
        // seek
        debug("Seek to " + toFrame);
        while (currentFrame < toFrame) {
            applyInputs(serverInputs, currentFrame);
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

        for (SimState<STATE> state : states.values()) {
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

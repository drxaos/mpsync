package com.github.drxaos.mpsync.sync;

import com.github.drxaos.mpsync.bus.Bus;
import com.github.drxaos.mpsync.sim.Simulation;

import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;

public class ClientSimSync<STATE, INPUT> extends Thread {

    Simulation<STATE, INPUT> simulation;

    Bus<STATE, INPUT> bus;

    ArrayBlockingQueue<SimInput<INPUT>> inputs = new ArrayBlockingQueue<SimInput<INPUT>>(10);

    final int KEEP_FRAMES = 10;
    LinkedList<SimState<STATE>> states = new LinkedList<SimState<STATE>>();

    final int ADJUST_INTERVALS = 5;
    int currentFrame = 0;
    int lastSyncFrame = 0;
    long lastSyncTimestamp = 0;
    long lastFrameTimestamp = 0;
    int fullStateFramesInterval = 10;
    LinkedList<Long> syncIntervals = new LinkedList<Long>();
    long oneFrameInterval = Long.MAX_VALUE;

    public ClientSimSync(Simulation<STATE, INPUT> simulation, Bus<STATE, INPUT> bus) {
        this.simulation = simulation;
        this.bus = bus;
    }

    public void gotFullState(STATE state, int frame) {
        // merge
        // TODO merge with local inputs
        simulation.setFullState(state);
        currentFrame = frame;

        // adjust speed
        if (lastSyncTimestamp > 0) {
            syncIntervals.add(System.currentTimeMillis() - lastSyncTimestamp);
            while (syncIntervals.size() > ADJUST_INTERVALS) {
                syncIntervals.remove(0);
            }
            long averageTimeInterval = 0;
            for (Long syncInterval : syncIntervals) {
                averageTimeInterval += syncInterval;
            }
            averageTimeInterval = averageTimeInterval / syncIntervals.size();
            fullStateFramesInterval = frame - lastSyncFrame;
            oneFrameInterval = averageTimeInterval / fullStateFramesInterval;
        }
        lastSyncTimestamp = System.currentTimeMillis();
        lastSyncFrame = frame;

        System.out.println("ADJUST: " + oneFrameInterval + "ms * " + fullStateFramesInterval + " frames");
    }

    public void gotInput(INPUT input, int frame) {

    }

    @Override
    public void run() {
        while (true) {
            if (syncIntervals.size() < ADJUST_INTERVALS) {
                // Initializing
                SimState<STATE> fullState = bus.getFullState();
                if (fullState != null) {
                    gotFullState(fullState.state, fullState.frame);
                }
                continue;
            }

            SimState<STATE> fullState = bus.getFullState();
            if (fullState != null) {
                gotFullState(fullState.state, fullState.frame);
                continue;
            }

            if (currentFrame - lastSyncFrame > fullStateFramesInterval * 4) {
                // no server activity - hang
                continue;
            }

            if (lastFrameTimestamp + oneFrameInterval < System.currentTimeMillis()) {
                simulation.step();
                currentFrame++;
                lastFrameTimestamp = System.currentTimeMillis();
                states.add(new SimState<STATE>(currentFrame, simulation.getFullState()));
                while (states.size() > KEEP_FRAMES) {
                    states.remove(0);
                }
            }
        }
    }
}

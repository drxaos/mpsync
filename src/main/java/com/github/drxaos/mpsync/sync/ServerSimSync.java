package com.github.drxaos.mpsync.sync;

import com.github.drxaos.mpsync.bus.Bus;
import com.github.drxaos.mpsync.sim.Simulation;

import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;

public class ServerSimSync<STATE, INPUT> extends Thread {

    Simulation<STATE, INPUT> simulation;

    Bus<STATE, INPUT> bus;

    ArrayBlockingQueue<SimInput<INPUT>> inputs = new ArrayBlockingQueue<SimInput<INPUT>>(10);

    final int KEEP_FRAMES = 10;
    LinkedList<SimState<STATE>> states = new LinkedList<SimState<STATE>>();

    int currentFrame = 0;
    long lastFrameTimestamp = 0;
    int fullStateFramesInterval = 10;
    long oneFrameInterval = 100;

    public ServerSimSync(Simulation<STATE, INPUT> simulation, Bus<STATE, INPUT> bus) {
        this.simulation = simulation;
        this.bus = bus;
    }

    public void gotInput(INPUT input, int frame) {

    }

    @Override
    public void run() {
        while (true) {
            if (lastFrameTimestamp + oneFrameInterval < System.currentTimeMillis()) {
                simulation.step();
                currentFrame++;
                lastFrameTimestamp = System.currentTimeMillis();

                SimState<STATE> simState = new SimState<STATE>(currentFrame, simulation.getFullState());

                states.add(simState);
                while (states.size() > KEEP_FRAMES) {
                    states.remove(0);
                }

                if (currentFrame % fullStateFramesInterval == 0) {
                    bus.broadcastFullState(simState);
                }
            }
        }
    }
}

package com.github.drxaos.mpsync.sync;

import com.github.drxaos.mpsync.bus.Bus;
import com.github.drxaos.mpsync.sim.Simulation;

import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;

public class ServerSimSync<STATE, INPUT> extends Thread {

    Simulation<STATE, INPUT> simulation;

    Bus<STATE, INPUT> bus;

    final int KEEP_FRAMES = 10;
    LinkedList<SimState<STATE>> states = new LinkedList<SimState<STATE>>();

    int currentFrame = 0;
    long lastFrameTimestamp = 0;
    int fullStateFramesInterval = 100;
    long oneFrameInterval = 25;

    public ServerSimSync(Simulation<STATE, INPUT> simulation, Bus<STATE, INPUT> bus) {
        super("ServerSimSync");
        this.simulation = simulation;
        this.bus = bus;
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
                }
            }
        }
    }
}

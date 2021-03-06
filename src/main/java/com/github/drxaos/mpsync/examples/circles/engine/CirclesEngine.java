package com.github.drxaos.mpsync.examples.circles.engine;

import com.github.drxaos.mpsync.bus.ServerInfo;
import com.github.drxaos.mpsync.examples.circles.sim.MoveEngine;
import com.github.drxaos.mpsync.examples.circles.ui.MainWindow;
import com.github.drxaos.mpsync.sync.Simulation;
import com.github.drxaos.mpsync.sync.SimInput;

import java.awt.event.MouseEvent;
import java.util.concurrent.locks.ReentrantLock;

public class CirclesEngine implements Simulation<State, Click, CirclesInfo> {

    final MoveEngine moveEngine = new MoveEngine();
    private MainWindow controller;
    private ReentrantLock lockView = new ReentrantLock();

    Click input;

    public CirclesEngine() {
        moveEngine.init();

        // sample world
        moveEngine.giveBirth(30, 30, 350, 250, 15);
        moveEngine.giveBirth(30, 130, 500, -200, 15);
        moveEngine.giveBirth(30, 230, 200, 550, 15);
    }

    public State getFullState() {
        try {
            lockView.lock();
            State state = new State();
            state.living = moveEngine.getLiving();
            return state;
        } finally {
            lockView.unlock();
        }
    }

    public void setFullState(State state) {
        try {
            lockView.lock();
            moveEngine.setLiving(state.living);
        } finally {
            lockView.unlock();
        }
    }

    public boolean forceFullState() {
        return false;
    }

    public void lockView() {
        lockView.lock();
    }

    public void unlockView() {
        lockView.unlock();
    }

    public void step() {
        if (controller != null) {
            MouseEvent click = controller.getClick();
            if (click != null) {
                input = new Click(click.getX(), click.getY(),
                        (Math.random() - 0.5) * 300, (Math.random() - 0.5) * 300,
                        (int) Math.round(Math.random()) * 15 + 10);
            }
        }
        moveEngine.step();
    }

    public Click getInput() {
        try {
            return input;
        } finally {
            input = null;
        }
    }

    public CirclesInfo getInfo() {
        return new CirclesInfo(moveEngine.getTimeFraction());
    }

    public void setServerInfo(ServerInfo<CirclesInfo> serverInfo) {
        moveEngine.setTimeFraction(serverInfo.info.timeFraction);
    }

    public void input(SimInput<Click> simInput) {
        moveEngine.giveBirth(simInput.input.x, simInput.input.y,
                simInput.input.vx, simInput.input.vy, simInput.input.r);
    }

    public void setController(MainWindow controller) {
        this.controller = controller;
    }
}

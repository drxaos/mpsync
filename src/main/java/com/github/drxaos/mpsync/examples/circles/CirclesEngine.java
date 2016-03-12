package com.github.drxaos.mpsync.examples.circles;

import com.github.drxaos.mpsync.examples.circles.sim.MoveEngine;
import com.github.drxaos.mpsync.examples.circles.ui.MainWindow;
import com.github.drxaos.mpsync.sim.Simulation;
import com.github.drxaos.mpsync.sync.SimInput;

import java.awt.event.MouseEvent;

public class CirclesEngine implements Simulation<State, Click, CirclesInfo> {

    final MoveEngine moveEngine = new MoveEngine();
    private MainWindow controller;

    Click input;

    public CirclesEngine() {
        moveEngine.init();

        // sample world
        moveEngine.giveBirth(30, 30, 350, 250, 15);
        moveEngine.giveBirth(30, 130, 500, -200, 15);
        moveEngine.giveBirth(30, 230, 200, 550, 15);
    }

    public State getFullState() {
        synchronized (moveEngine) {
            State state = new State();
            state.living = moveEngine.getLiving();
            return state;
        }
    }

    public void setFullState(State state) {
        synchronized (moveEngine) {
            //System.out.println("FULLSTATE: " + state.living.size());
            moveEngine.setLiving(state.living);
        }
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

    public void setServerInfo(CirclesInfo circlesInfo) {
        moveEngine.setTimeFraction(circlesInfo.timeFraction);
    }

    public void input(SimInput<Click> simInput) {
        moveEngine.giveBirth(simInput.input.x, simInput.input.y,
                simInput.input.vx, simInput.input.vy, simInput.input.r);
    }

    public void setController(MainWindow controller) {
        this.controller = controller;
    }
}

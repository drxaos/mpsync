package com.github.drxaos.mpsync.examples.circles;

import com.github.drxaos.mpsync.examples.circles.sim.MoveEngine;
import com.github.drxaos.mpsync.sim.Simulation;
import com.github.drxaos.mpsync.sync.SimInput;
import com.github.drxaos.mpsync.sync.SimState;

import java.util.Collection;

public class CirclesEngine implements Simulation<State, Click> {

    MoveEngine moveEngine = new MoveEngine();

    public CirclesEngine() {
        moveEngine.init();

        // sample world
        moveEngine.giveBirth(30, 30, 100, 50, 15);
        moveEngine.giveBirth(30, 130, 100, -50, 15);
        moveEngine.giveBirth(30, 230, 100, 50, 15);
    }

    public State getFullState() {
        State state = new State();
        state.living = moveEngine.getLiving();
        return state;
    }

    public void setFullState(State state) {
        moveEngine.getLiving().clear();
        moveEngine.getLiving().addAll(state.living);
    }

    public void mergeAndSeek(SimState<State> simState, Collection<SimInput<Click>> simInputs, int toFrame) {
        setFullState(simState.state);
        for (int frame = simState.frame; frame < toFrame; frame++) {
            for (SimInput<Click> simInput : simInputs) {
                if (simInput.frame == frame) {
                    input(simInput);
                }
            }
            moveEngine.step();
        }
    }

    public void step() {
        moveEngine.step();
    }

    public void input(SimInput<Click> simInput) {

    }


}

/*
 * The MIT License (MIT)
 *
 * Copyright © 2015, Heiko Brumme
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package Visualization.state;

import DataTypes.CactusData;
import Visualization.graphic.Renderer;
import Visualization.graphic.Window;

import java.util.HashMap;
import java.util.Map;

/**
 * The stateMachine machine handles different states and transitions between states.
 *
 * @author Heiko Brumme, Jelle Schukken
 */
public class StateMachine extends State {

    /**
     * Contains all states of this stateMachine machine.
     */
    private final Map<String, State> states;
    /**
     * Current active stateMachine.
     */
    private State currentState;

    /**
     * Creates a stateMachine machine.
     */
    public StateMachine() {
        super(null, null);
        states = new HashMap<>();
        currentState = new EmptyState(null, null);
        states.put(null, currentState);
    }

    /**
     * Adds a stateMachine with specified name.
     *
     * @param name  Name of the stateMachine
     * @param state The stateMachine to add
     */
    public void add(String name, State state) {
        states.put(name, state);
    }

    /**
     * returns the statemachine to a specific instance of a state
     * @param state the state to which to return
     */
    public void back(State state){
        currentState.exit();

        currentState = state;
        currentState.setZoom();

        currentState.enter();
    }
    /**
     * Changes the current state of the stateMachine.
     *
     * @param name Name of the desired stateMachine
     */
    public void change(String name, Window window, Renderer renderer, CactusData data) {
        currentState.exit();
        if(name == null){
            currentState = states.get(null);
        }else {
            currentState = generateState(name, window, renderer, data);
        }
        currentState.enter();
    }

    /**
     * creates and returns the desired state
     * @param name the name of the desired state
     * @param window the window to which the state should render
     * @param renderer the renderer the state should use
     * @param data the data which the state should process
     * @return the desired state
     */
    private State generateState(String name, Window window, Renderer renderer, CactusData data){

        if(data != null){
            switch (name) {
                case "InputPrompt":
                    return new InputPrompt(window, renderer, data);
                case "VerificationState":
                    return new VerificationState(window, renderer, data);
                case "NibbledRingState":
                    return new NibbledRingState(window, renderer, data, currentState, ((LayerSeparatorState)currentState).getNibbled());
                case "OuterNibbledRingState":
                    return new NibbledRingState(window, renderer, data, currentState, ((LayerSeparatorState)currentState).getOuterNibbled());
                case "LayerSeparatorState":
                    return new LayerSeparatorState(window, renderer, data);
                case "Test":
                    return new EmptyState(window, renderer);


                default:
                    return null;
            }
        }else{
            switch (name) {
                case "InputPrompt":
                    return new InputPrompt(window, renderer);
                case "Test":
                    return new EmptyState(window, renderer);
                case "Restart":
                    return new InputPrompt(window, renderer, new CactusData());

                default:
                    return null;
            }
        }

    }

    @Override
    public void input() {
        currentState.input();
    }

    @Override
    public void update() {
        currentState.update();
    }

    @Override
    public void update(float delta) {
        currentState.update(delta);
    }

    @Override
    public void render() {
        currentState.render();
    }

    @Override
    public void render(float alpha) {
        currentState.render(alpha);
    }

    @Override
    public void enter() {
        currentState.enter();
    }

    @Override
    public void exit() {
        currentState.exit();
    }

}

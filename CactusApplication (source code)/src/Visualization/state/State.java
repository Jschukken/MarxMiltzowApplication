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

import DataTypes.Point;
import Visualization.Components.Button;
import Visualization.core.Game;
import Visualization.graphic.Renderer;
import Visualization.graphic.Window;
import javafx.util.Pair;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

/**
 * States are used for the current application stateMachine.
 *
 * @author Heiko Brumme, Jelle Schukken
 */
public abstract class State {

    protected final Renderer renderer;
    protected final Window window;

    protected ArrayList<Button> buttonList = new ArrayList<>();
    protected int gameWidth;
    protected int gameHeight;
    protected int prevGameWidth;
    protected int prevGameHeight;

    protected float displayScale = 100f;
    protected Point center = new Point(0f, 0f);
    protected Point lastCenter = new Point(0f, 0f);


    /**
     * constructor
     * sets zoom behavior for scroll wheel
     * @param window the window to which this state renders
     * @param renderer the renderer this state uses to render
     */
    public State(Window window, Renderer renderer) {
        this.window = window;
        this.renderer = renderer;
        setZoom();

    }

    public void setZoom(){
        if(window != null) {
            GLFW.glfwSetScrollCallback(window.getId(), new GLFWScrollCallback() {
                @Override
                public void invoke(long win, double dx, double dy) {

                    if (displayScale - 5 * dy >= 10 && glfwGetMouseButton(window.getId(), GLFW_MOUSE_BUTTON_1) != GLFW_PRESS) {
                        int mouseX;
                        int mouseY;
                        DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
                        DoubleBuffer y = BufferUtils.createDoubleBuffer(1);

                        glfwGetCursorPos(window.getId(), x, y);
                        x.rewind();
                        y.rewind();

                        mouseX = (int) Math.floor(x.get()) - gameWidth / 3;
                        mouseY = gameHeight - (int) Math.floor(y.get());

                        displayScale -= 5 * dy;

                        center.a -= (5 * dy * mouseX * 100f) / ((displayScale - 5 * dy) * displayScale);
                        center.b -= (5 * dy * mouseY * 100f) / ((displayScale - 5 * dy) * displayScale);
                        lastCenter.a = center.a;
                        lastCenter.b = center.b;

                    }

                }
            });
        }
    }

    /**
     * handles user input to the application.
     */
    public void input() {

        if (glfwGetMouseButton(window.getId(), GLFW_MOUSE_BUTTON_1) == GLFW_PRESS) {
            int mouseX;
            int mouseY;
            DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
            DoubleBuffer y = BufferUtils.createDoubleBuffer(1);

            glfwGetCursorPos(window.getId(), x, y);
            x.rewind();
            y.rewind();

            mouseX = (int) Math.floor(x.get());
            mouseY = gameHeight - (int) Math.floor(y.get());

            for (int i = 0; i < buttonList.size(); i++) {
                Button b = buttonList.get(i);
                if (b.canPress() && b.inButton(mouseX, mouseY)) {
                    b.press();

                }
            }

        } else {
            int mouseX;
            int mouseY;
            DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
            DoubleBuffer y = BufferUtils.createDoubleBuffer(1);

            glfwGetCursorPos(window.getId(), x, y);
            x.rewind();
            y.rewind();

            mouseX = (int) Math.floor(x.get());
            mouseY = gameHeight - (int) Math.floor(y.get());

            for (int i = 0; i < buttonList.size(); i++) {
                Button b = buttonList.get(i);
                b.updateTooltip(mouseX, mouseY);
                b.update();
            }
        }

    }

    /**
     * Updates the stateMachine (fixed timestep).
     */
    public void update() {
        update(1f / Game.TARGET_UPS);
    }

    /**
     * Updates the stateMachine (variable timestep)
     *
     * @param delta Time difference in seconds
     */
    public abstract void update(float delta);

    /**
     * Renders the stateMachine (no interpolation).
     */
    public void render() {
        render(1f);
    }

    /**
     * Renders the stateMachine (with interpolation).
     *
     * @param alpha Alpha value, needed for interpolation
     */
    public abstract void render(float alpha);

    /**
     * Gets executed when entering the stateMachine, useful for initialization.
     */
    public void enter() {
        /* Get width and height of framebuffer */
        int width, height;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long window = org.lwjgl.glfw.GLFW.glfwGetCurrentContext();
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            GLFW.glfwGetFramebufferSize(window, widthBuffer, heightBuffer);
            width = widthBuffer.get();
            height = heightBuffer.get();
        }

        /* Initialize variables */
        gameWidth = width;
        prevGameWidth = width;
        gameHeight = height;
        prevGameHeight = height;
    }


    /**
     * resizes button when the window is resized
     */
    protected void onWindowResize(){
        for(Button b: buttonList){
            b.onResize(gameWidth, gameHeight);
        }
    }

    /**
     * converts point location to pixel location
     * @param p the point to convert
     * @return the pixel location of p
     */
    protected Pair<Float,Float> convertPointToScale(Pair<Float,Float> p){
        return new Pair((float)(center.a + gameWidth / 3f + (100 / displayScale) * p.getKey()), (float)(center.b + (100 / displayScale) * p.getValue()));
    }

    /**
     * Gets executed when leaving the stateMachine, useful for disposing.
     */
    public void exit() {
        for (Button b : buttonList) {
            b.delete();
        }
        buttonList.clear();
    }


}

/*
 * The MIT License (MIT)
 *
 * Copyright © 2014-2016, Heiko Brumme
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
package Visualization.core;

import Visualization.game.Box;
import Visualization.graphic.Color;
import Visualization.graphic.Texture;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glViewport;

/**
 * This class contains the implementation for a fixed timestep game loop.
 *
 * @author Heiko Brumme, Jelle Schukken
 */
public class FixedTimestepGame extends Game {

    public static final boolean DEBUG_MODE_ENABLED = false;
    private int gameHeight,prevGameWidth;
    private int gameWidth,prevGameHeight;

    @Override
    public void init(){
        super.init();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long window = GLFW.glfwGetCurrentContext();
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            GLFW.glfwGetFramebufferSize(window, widthBuffer, heightBuffer);
            gameWidth = widthBuffer.get();
            gameHeight = heightBuffer.get();
        }
    }

    @Override
    public void gameLoop() {
        float delta;
        float accumulator = 0f;
        float interval = 1f / TARGET_UPS;
        float alpha;

        while (running) {
            /* Check if game should close */
            if (window.isClosing()) {
                running = false;
            }

            try (MemoryStack stack = MemoryStack.stackPush()) {
                long window = GLFW.glfwGetCurrentContext();
                IntBuffer widthBuffer = stack.mallocInt(1);
                IntBuffer heightBuffer = stack.mallocInt(1);
                GLFW.glfwGetFramebufferSize(window, widthBuffer, heightBuffer);
                gameWidth = widthBuffer.get();
                gameHeight = heightBuffer.get();
            }
            if(gameWidth != prevGameWidth || gameHeight != prevGameHeight) {
                onWindowResize();
                prevGameHeight = gameHeight;
                prevGameWidth = gameWidth;
            }


            /* Get delta time and update the accumulator */
            delta = timer.getDelta();
            accumulator += delta;

            /* Handle input */
            input();

            /* Update game and timer UPS if enough time has passed */
            while (accumulator >= interval) {
                update();
                timer.updateUPS();
                accumulator -= interval;
            }

            /* Calculate alpha value for interpolation */
            alpha = accumulator / interval;

            /* Render game and update timer FPS */
            render(alpha);
            timer.updateFPS();

            /* Update timer */
            timer.update();

            /* Draw FPS, UPS and Context version */
            if(DEBUG_MODE_ENABLED) {
                int height = renderer.getDebugTextHeight("Context");
                renderer.drawDebugText("FPS: " + timer.getFPS() + " | UPS: " + timer.getUPS(), 5, 5 + height);
                renderer.drawDebugText("Context: " + (Game.isDefaultContext() ? "3.2 core" : "2.1"), 5, 5);
            }

            /* Update window to show the new screen */
            window.update();

            /* Synchronize if v-sync is disabled */
            if (!window.isVSyncEnabled()) {
                sync(TARGET_FPS);
            }
        }
    }

    /**
     * clears old data when window is resized
     */
    private void onWindowResize(){

        glViewport(0, 0, gameWidth, gameHeight);
        glClear(GL_COLOR_BUFFER_BIT);
        renderer.dispose();
        renderer.init();
    }


}

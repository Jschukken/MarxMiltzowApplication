/*
 * The MIT License (MIT)
 *
 * Copyright © 2014-2018, Heiko Brumme
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

package Visualization.graphic;

import java.awt.FontFormatException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import Visualization.core.Game;
import DataTypes.Matrix4f;
import Visualization.text.Font;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;


import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

/**
 * This class is performing the rendering process.
 *
 * @author Heiko Brumme, Jelle Schukken
 */
public class Renderer {

    private VertexArrayObject vao;
    private VertexBufferObject vbo;
    private ShaderProgram program;

    private FloatBuffer vertices;
    private int numVertices;
    private boolean drawing;

    private Font font;
    private Font debugFont;

    /** Initializes the renderer. */
    public void init() {
        /* Setup shader programs */
        setupShaderProgram();

        /* Enable blending */
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);


        /* Create fonts */
        try {
            font = new Font(new FileInputStream("resources/Inconsolata.ttf"), 16);
        } catch (FontFormatException | IOException ex) {
            Logger.getLogger(Renderer.class.getName()).log(Level.CONFIG, null, ex);
            font = new Font();
        }
        debugFont = new Font(12, false);
    }

    /**
     * Clears the drawing area.
     */
    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    /**
     * Begin rendering.
     */
    public void begin() {
        if (drawing) {
            throw new IllegalStateException("Renderer is already drawing!");
        }
        drawing = true;
        numVertices = 0;
    }

    /**
     * End rendering.
     */
    public void end() {
        if (!drawing) {
            throw new IllegalStateException("Renderer isn't drawing!");
        }
        drawing = false;
        flush();
    }

    /**
     * Flushes the data to the GPU to let it get rendered.
     */
    public void flush() {
        if (numVertices > 0) {
            vertices.flip();

            if (vao != null) {
                vao.bind();
            } else {
                vbo.bind(GL_ARRAY_BUFFER);
                specifyVertexAttributes();
            }
            program.use();

            /* Upload the new vertex data */
            vbo.bind(GL_ARRAY_BUFFER);
            vbo.uploadSubData(GL_ARRAY_BUFFER, 0, vertices);

            /* Draw batch */
            glDrawArrays(GL_TRIANGLES, 0, numVertices);

            /* Clear vertex data for next batch */
            vertices.clear();
            numVertices = 0;
        }
    }

    /**
     * Calculates total width of a text.
     *
     * @param text The text
     *
     * @return Total width of the text
     */
    public int getTextWidth(CharSequence text) {
        return font.getWidth(text);
    }

    /**
     * Calculates total height of a text.
     *
     * @param text The text
     *
     * @return Total width of the text
     */
    public int getTextHeight(CharSequence text) {
        return font.getHeight(text);
    }

    /**
     * Calculates total height of a debug text.
     *
     * @param text The text
     *
     * @return Total width of the text
     */
    public int getDebugTextHeight(CharSequence text) {
        return debugFont.getHeight(text);
    }

    /**
     * Draw debug text at the specified position.
     *
     * @param text Text to draw
     * @param x    X coordinate of the text position
     * @param y    Y coordinate of the text position
     */
    public void drawDebugText(CharSequence text, float x, float y) {
        debugFont.drawText(this, text, x, y);
    }

    /**
     * Draw text at the specified position and color.
     *
     * @param text Text to draw
     * @param x    X coordinate of the text position
     * @param y    Y coordinate of the text position
     * @param c    Color to use
     */
    public void drawText(CharSequence text, float x, float y, Color c) {
        font.drawText(this, text, x, y, c);
    }

    /**
     * Draws a texture region with the currently bound texture stretched to cover entire region
     *
     * @param texture   Used for getting width and height of the texture
     * @param x         X position of the texture
     * @param y         Y position of the texture
     * @param regWidth  Width of the texture region
     * @param regHeight Height of the texture region
     * @param c         The color to use
     */
    public void drawFullTextureRegion(Texture texture, float x, float y, float regWidth, float regHeight, Color c) {
        /* Vertex positions */
        float x1 = x;
        float y1 = y;
        float x2 = x + regWidth;
        float y2 = y + regHeight;

        /* Texture coordinates */
        float s1 = 0;//regX / texture.getWidth();
        float t1 = 0;//regY / texture.getHeight();
        float s2 = 1f;//(regX + regWidth) / texture.getWidth();
        float t2 = 1f;//(regY + regHeight) / texture.getHeight();

        drawTextureRegion(x1, y1, x2, y2, s1, t1, s2, t2, c);
    }
    /**
     * Draws a texture region with the currently bound texture on specified
     * coordinates.
     *
     * @param texture   Used for getting width and height of the texture
     * @param x         X position of the texture
     * @param y         Y position of the texture
     * @param regX      X position of the texture region
     * @param regY      Y position of the texture region
     * @param regWidth  Width of the texture region
     * @param regHeight Height of the texture region
     * @param c         The color to use
     */
    public void drawTextureRegion(Texture texture, float x, float y, float regX, float regY, float regWidth, float regHeight, Color c) {
        /* Vertex positions */
        float x1 = x;
        float y1 = y;
        float x2 = x + regWidth;
        float y2 = y + regHeight;

        /* Texture coordinates */
        float s1 = regX / texture.getWidth();
        float t1 = regY / texture.getHeight();
        float s2 = (regX + regWidth) / texture.getWidth();
        float t2 = (regY + regHeight) / texture.getHeight();

        drawTextureRegion(x1, y1, x2, y2, s1, t1, s2, t2, c);
    }

    /**
     * draws a line between two points
     * @param x1,y1 first point
     * @param x2,y2 second point
     * @param width width of the line
     * @param c color of the line
     */
    public void drawLine(float x1, float y1, float x2, float y2, float width, Color c){
        float dx = 0;
        float dy = 0;

        if(x1 == x2 && y1 == y2) {
            return;
        }else if (x1 == x2){
            dx = 0;
            dy = width;
        }else if (y1 == y2){
            dx = width;
            dy = 0;
        }else{
            dx = x2-x1;
            dy = y2-y1;

            float length = (float)Math.sqrt(dx*dx + dy*dy);
            dx = (dx/length) * width;
            dy = (dy/length) * width;
        }


        if (vertices.remaining() < 7 * 6) {
            /* We need more space in the buffer, so flush it */
            flush();
        }

        float r = c.getRed();
        float g = c.getGreen();
        float b = c.getBlue();
        float a = c.getAlpha();

        vertices.put(x1 - dy).put(y1 + dx).put(r).put(g).put(b).put(a).put(0).put(0);
        vertices.put(x2 - dy).put(y2 + dx).put(r).put(g).put(b).put(a).put(0).put(1);
        vertices.put(x1 + dy).put(y1 - dx).put(r).put(g).put(b).put(a).put(1).put(1);

        vertices.put(x1 + dy).put(y1 - dx).put(r).put(g).put(b).put(a).put(0).put(0);
        vertices.put(x2 - dy).put(y2 + dx).put(r).put(g).put(b).put(a).put(1).put(1);
        vertices.put(x2 + dy).put(y2 - dx).put(r).put(g).put(b).put(a).put(1).put(0);

        numVertices += 6;
    }

    /**
     * Draws a texture region with the currently bound texture on specified
     * coordinates.
     *
     * @param x1 Bottom left x position
     * @param y1 Bottom left y position
     * @param x2 Top right x position
     * @param y2 Top right y position
     * @param s1 Bottom left s coordinate
     * @param t1 Bottom left t coordinate
     * @param s2 Top right s coordinate
     * @param t2 Top right t coordinate
     * @param c  The color to use
     */
    public void drawTextureRegion(float x1, float y1, float x2, float y2, float s1, float t1, float s2, float t2, Color c) {
        if (vertices.remaining() < 7 * 6) {
            /* We need more space in the buffer, so flush it */
            flush();
        }

        float r = c.getRed();
        float g = c.getGreen();
        float b = c.getBlue();
        float a = c.getAlpha();

        vertices.put(x1).put(y1).put(r).put(g).put(b).put(a).put(s1).put(t1);
        vertices.put(x1).put(y2).put(r).put(g).put(b).put(a).put(s1).put(t2);
        vertices.put(x2).put(y2).put(r).put(g).put(b).put(a).put(s2).put(t2);

        vertices.put(x1).put(y1).put(r).put(g).put(b).put(a).put(s1).put(t1);
        vertices.put(x2).put(y2).put(r).put(g).put(b).put(a).put(s2).put(t2);
        vertices.put(x2).put(y1).put(r).put(g).put(b).put(a).put(s2).put(t1);

        numVertices += 6;
    }

    /**
     * Dispose renderer and clean up its used data.
     */
    public void dispose() {
        MemoryUtil.memFree(vertices);

        if (vao != null) {
            vao.delete();
        }
        vbo.delete();
        program.delete();

        font.dispose();
        debugFont.dispose();
    }

    /** Setups the default shader program. */
    private void setupShaderProgram() {
        if (Game.isDefaultContext()) {
            /* Generate Vertex Array Object */
            vao = new VertexArrayObject();
            vao.bind();
        } else {
            vao = null;
        }

        /* Generate Vertex Buffer Object */
        vbo = new VertexBufferObject();
        vbo.bind(GL_ARRAY_BUFFER);

        /* Create FloatBuffer */
        vertices = MemoryUtil.memAllocFloat(4096);

        /* Upload null data to allocate storage for the VBO */
        long size = vertices.capacity() * Float.BYTES;
        vbo.uploadData(GL_ARRAY_BUFFER, size, GL_DYNAMIC_DRAW);

        /* Initialize variables */
        numVertices = 0;
        drawing = false;

        /* Load shaders */
        Shader vertexShader, fragmentShader;
        vertexShader = Shader.loadShader(GL_VERTEX_SHADER, "resources/default.vert");
        fragmentShader = Shader.loadShader(GL_FRAGMENT_SHADER, "resources/default.frag");

        /* Create shader program */
        program = new ShaderProgram();
        program.attachShader(vertexShader);
        program.attachShader(fragmentShader);
        if (Game.isDefaultContext()) {
            program.bindFragmentDataLocation(0, "fragColor");
        }
        program.link();
        program.use();

        /* Delete linked shaders */
        vertexShader.delete();
        fragmentShader.delete();

        /* Get width and height of framebuffer */
        long window = GLFW.glfwGetCurrentContext();
        int width, height;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            GLFW.glfwGetFramebufferSize(window, widthBuffer, heightBuffer);
            width = widthBuffer.get();
            height = heightBuffer.get();
        }

        /* Specify Vertex Pointers */
        specifyVertexAttributes();

        /* Set texture uniform */
        int uniTex = program.getUniformLocation("texImage");
        program.setUniform(uniTex, 0);

        /* Set model matrix to identity matrix */
        Matrix4f model = new Matrix4f();
        int uniModel = program.getUniformLocation("model");
        program.setUniform(uniModel, model);

        /* Set view matrix to identity matrix */
        Matrix4f view = new Matrix4f();
        int uniView = program.getUniformLocation("view");
        program.setUniform(uniView, view);

        /* Set projection matrix to an orthographic projection */
        Matrix4f projection = Matrix4f.orthographic(0f, width, 0f, height, -1f, 1f);
        int uniProjection = program.getUniformLocation("projection");
        program.setUniform(uniProjection, projection);
    }

    /**
     * Specifies the vertex pointers.
     */
    private void specifyVertexAttributes() {
        /* Specify Vertex Pointer */
        int posAttrib = program.getAttributeLocation("position");
        program.enableVertexAttribute(posAttrib);
        program.pointVertexAttribute(posAttrib, 2, 8 * Float.BYTES, 0);

        /* Specify Color Pointer */
        int colAttrib = program.getAttributeLocation("color");
        program.enableVertexAttribute(colAttrib);
        program.pointVertexAttribute(colAttrib, 4, 8 * Float.BYTES, 2 * Float.BYTES);

        /* Specify Texture Pointer */
        int texAttrib = program.getAttributeLocation("texcoord");
        program.enableVertexAttribute(texAttrib);
        program.pointVertexAttribute(texAttrib, 2, 8 * Float.BYTES, 6 * Float.BYTES);
    }

}

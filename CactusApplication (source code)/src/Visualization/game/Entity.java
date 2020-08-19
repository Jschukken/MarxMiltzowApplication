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

package Visualization.game;

import DataTypes.*;
import Visualization.graphic.Color;
import Visualization.graphic.Renderer;
import Visualization.graphic.Texture;

/**
 * This class represents an game entity.
 *
 * @author Heiko Brumme, Jelle Schukken
 */
public abstract class Entity {

    protected Vector2f previousPosition;
    protected Vector2f position;

    protected final AABB aabb;

    protected Vector2f direction;

    protected Color color;
    protected final Texture texture;

    protected int width;
    protected int height;

    protected final int tx;
    protected final int ty;

    public Entity(Color color, Texture texture, float x, float y, int width, int height, int tx, int ty) {
        previousPosition = new Vector2f(x, y);
        position = new Vector2f(x, y);

        aabb = new AABB(this);

        direction = new Vector2f();

        this.color = color;
        this.texture = texture;

        this.width = width;
        this.height = height;

        this.tx = tx;
        this.ty = ty;
    }

    /**
     * Handles input of the entity.
     */
    public void input() {
        input(null);
    }

    /**
     * Handles input of the entity.
     *
     * @param entity Can be used for the AI
     */
    public abstract void input(Entity entity);

    /**
     * Updates the entity.
     *
     * @param delta Time difference in seconds
     */
    public void update(float delta) {
        previousPosition = new Vector2f(position.x, position.y);
        if (direction.length() != 0) {
            direction = direction.normalize();
        }

        aabb.min.x = position.x;
        aabb.min.y = position.y;
        aabb.max.x = position.x + width;
        aabb.max.y = position.y + height;
    }

    /**
     * Renders the entity.
     *
     * @param renderer Renderer for batching
     * @param alpha    Alpha value, needed for interpolation
     */
    public void render(Renderer renderer, float alpha) {
        Vector2f interpolatedPosition = previousPosition.lerp(position, alpha);
        float x = interpolatedPosition.x;
        float y = interpolatedPosition.y;
        if(texture != null)
            renderer.drawFullTextureRegion(texture, x, y, width, height, color);
        else{
            renderer.drawTextureRegion(x,y,x+width,y+height,0,0,x+width, y+height, color);
        }
    }

    public void setPosition(float x, float y){
        position.x = x;
        position.y = y;
        previousPosition.x = x;
        previousPosition.y = y;
    }

    public void setSize(int x, int y){
        width = x;
        height = y;
    }

    public void setColor(Color color){
        this.color = color;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

}

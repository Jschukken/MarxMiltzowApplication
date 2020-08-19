package Visualization.game;

import Visualization.graphic.Color;
import Visualization.graphic.Texture;

/**
 * a simple box
 *
 * @author Jelle Schukken
 */
public class Box extends Entity {

    public Box(Color color, Texture texture, float x, float y, int height, int width){
        super(color, texture, x, y, width, height, 0, 0);

    }
    @Override
    public void input(Entity entity) {

    }
}

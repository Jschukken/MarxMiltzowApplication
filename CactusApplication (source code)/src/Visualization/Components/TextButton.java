package Visualization.Components;

import Visualization.game.Box;
import Visualization.game.Entity;
import Visualization.graphic.Color;
import Visualization.graphic.Renderer;
import Visualization.graphic.Texture;

/**
 * a button that uses text instead of a texture
 *
 * @author Jelle Schukken
 */
public class TextButton extends Button{

    private String text;
    private Entity buttonTextBox;

    public TextButton(LocationPacket location, int screenWidth, int screenHeight, int width, int height, String text, String tooltip){
        super(location,screenWidth,screenHeight,width+10,height+10,null,tooltip);
        this.text = text;
        buttonObject.setColor(Color.BLACK);
        buttonTextBox = new Box(Color.WHITE, buttonTexture, buttonObject.getX() + 2, buttonObject.getY()+2, height+6, width+6);
    }

    @Override
    public void onResize(int screenWidth, int screenHeight){
        float width = buttonObject.getWidth();
        float height = buttonObject.getHeight();
        float x = location.screenPosX*screenWidth;
        float y = location.screenPosY*screenHeight;
        if(x+width > screenWidth)
            x = screenWidth-width;
        if(y+height > screenHeight){
            y = screenHeight-height;
        }
        buttonObject.setPosition(x + location.offsetX, y + location.offsetY);
        buttonTextBox.setPosition(x + location.offsetX + 2, y + location.offsetY + 2);
    }

    @Override
    public void renderButton(Renderer renderer, float alpha) {

        buttonTexture.bind();
        buttonObject.setColor(Color.BLACK);
        buttonObject.render(renderer, alpha);

        if(pressed){
            buttonTextBox.setColor(new Color(.8f, .8f, .8f,1f));
        }else{
            buttonTextBox.setColor(Color.WHITE);
        }
        buttonTextBox.render(renderer, alpha);
        renderer.end();
        renderer.drawText(text, buttonObject.getX()+4, buttonObject.getY()+4, Color.BLACK);
        renderer.begin();
    }
}

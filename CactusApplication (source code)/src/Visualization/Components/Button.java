package Visualization.Components;

import Visualization.game.Box;
import Visualization.game.Entity;
import Visualization.graphic.Color;
import Visualization.graphic.Renderer;
import Visualization.graphic.Texture;

/**
 * a button object
 * behavior of the button is defined when the instance is created
 *
 * @author Jelle Schukken
 */
public class Button {


    private Texture tooltipTexture = Texture.loadTexture("resources/BlankTexture.png");

    protected Texture buttonTexture;
    protected Entity buttonObject;
    protected boolean pressed;
    private String tooltip;

    protected LocationPacket location;

    private boolean tooltipActive = false;
    private long startTime = 0;
    private long pressTime = 0;

    /**
     * constructor
     * @param location relative location of the button
     * @param screenWidth,screenHeight used to compute pixel location of button
     * @param width the width of the button
     * @param height the height of the button
     * @param texture the texture of the button
     * @param tooltip the buttons tooltip
     */
    public Button(LocationPacket location, int screenWidth, int screenHeight, int width, int height, Texture texture, String tooltip) {

        float x = location.screenPosX*screenWidth;
        float y = location.screenPosY*screenHeight;
        if(x+width > screenWidth)
            x= screenWidth-width;
        if(y+height > screenHeight){
            y = screenHeight-height;
        }
        if(texture == null){
            texture = Texture.loadTexture("resources/BlankTexture.png");
        }

        buttonObject = new Box(new Color((float)Math.random(),(float)Math.random(), .4f), texture, x + location.offsetX, y + location.offsetY, height, width);
        this.tooltip = tooltip;


        buttonTexture = texture;
        this.location = location;
        pressed = false;
    }

    /**
     * returns wether the given coordinate lies within this button
     * @param x x-coordiante
     * @param y y-coordinate
     * @return true if it lies within false otherwise
     */
    public boolean inButton(int x, int y) {

        if (buttonObject.getX() <= x && buttonObject.getY() <= y) {
            if (buttonObject.getX() + buttonObject.getWidth() >= x && buttonObject.getY() + buttonObject.getHeight() >= y) {
                return true;
            }
        }
        return false;
    }

    /**
     * triggers visual change in button when pressed
     */
    public void press() {

        pressed = true;
        pressTime = System.currentTimeMillis();
        buttonObject.setColor(new Color(.9f,.9f,.9f));
    }

    /**
     * a short time after pressing trigger on press effect
     */
    public void update(){

        if(pressed && System.currentTimeMillis()-pressTime > 150){
            pressed = false;
            buttonObject.setColor(Color.WHITE);
            onPress();
        }
    }

    /**
     * on press effect of button. must be overridden when creating button
     */
    protected void onPress(){
        throw new IllegalStateException("\"onPress()\" must be overridden when button is created");
    }

    /**
     * determine if user has started hovering above button
     * @param x,y coordinates of mouse
     */
    public void updateTooltip(int x, int y) {
        if (inButton(x, y) && !tooltipActive) {
            startTime = System.currentTimeMillis();
            tooltipActive = true;
        } else if (!inButton(x, y) && tooltipActive) {
            tooltipActive = false;
        }
    }

    /**
     * determines if the tooltip should be displayed
     * @return true if it should be displayed, false otherwise
     */
    private boolean displayTooltip() {
        if (tooltipActive && System.currentTimeMillis() - startTime > 1000) {
            return true;
        }
        return false;
    }

    /**
     * checks if the button can be pressed (prevents pressing button multiple times on accident)
     * @return true if it can, false otherwise
     */
    public boolean canPress() {
        return !pressed;
    }

    /**
     * renders the button
     * @param renderer the renderer to use
     * @param alpha input used by renderer
     */
    public void renderButton(Renderer renderer, float alpha) {
        if(pressed){
            buttonObject.setColor(new Color(.9f, .9f, .9f,1f));
        }
        buttonObject.render(renderer, alpha);
    }

    /**
     * renders the tooltip at mouse location. Enforces entire tooltip to be within window
     * @param renderer the renderer to use
     * @param alpha input used by renderer
     * @param mouseX,mouseY location of the mouse
     * @param screenWidth,screenHeight dimensions of the window
     */
    public void renderToolTip(Renderer renderer, float alpha, int mouseX, int mouseY, int screenWidth, int screenHeight){
        if (displayTooltip()) {
            renderer.begin();
            int textWidth = renderer.getTextWidth(tooltip);
            int textHeight = renderer.getTextHeight(tooltip);
            if(mouseX+textWidth + 8 > screenWidth)
                mouseX= screenWidth-(textWidth+8);
            if(mouseY+textHeight + 8 > screenHeight){
                mouseY = screenHeight-(textHeight + 8);
            }

            Color black = new Color(0, 0, 0);
            Entity boundingBox = new Box(black, tooltipTexture, mouseX, mouseY, textHeight + 8, textWidth + 8);
            Color grey = new Color(.9f, .9f, .9f,1f);
            Entity textBox = new Box(grey, tooltipTexture, mouseX + 1, mouseY + 1, textHeight + 6, textWidth + 6);
            tooltipTexture.bind();

            boundingBox.render(renderer, alpha);
            textBox.render(renderer, alpha);
            renderer.end();
            renderer.drawText(tooltip, mouseX+4, mouseY+4, Color.BLACK);
        }
    }

    /**
     * repositions the button when screen is resized
     * @param screenWidth,screenHeight new screen dimensions
     */
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
    }

    public int getWidth(){
        return (int)buttonObject.getWidth() + location.offsetX;
    }

    public int getHeight(){
        return (int)buttonObject.getHeight() + location.offsetY;
    }

    public void delete() {

        buttonTexture.delete();
    }

}

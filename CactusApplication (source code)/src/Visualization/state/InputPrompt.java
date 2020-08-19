package Visualization.state;

import DataTypes.CactusData;
import DataTypes.*;
import FileReader.InputReader;
import Visualization.Components.Button;
import Visualization.Components.LocationPacket;
import Visualization.Components.TextButton;
import Visualization.core.Game;
import Visualization.game.Box;
import Visualization.game.Entity;
import Visualization.graphic.Color;
import Visualization.graphic.Renderer;
import Visualization.graphic.Texture;
import Visualization.graphic.Window;
import javafx.util.Pair;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glClearColor;

/**
 * the state in which point set is input
 *
 * allows the user to manually input a point set or load one from a file
 *
 * @author Jelle Schukken
 */
public class InputPrompt extends State {


    private Texture texture;
    private Entity boundingBox;
    private Entity textBox;
    private Entity point;
    private CactusData dataPacket;


    private long pressTime = 0;
    private Point pressPoint = null;

    private boolean pressed = false;


    public InputPrompt(Window window, Renderer renderer) {
        super(window, renderer);
        dataPacket = new CactusData();

    }

    public InputPrompt(Window window, Renderer renderer, CactusData dataPacket) {
        super(window, renderer);
        this.dataPacket = dataPacket;
        dataPacket.NibbledRingDataBase.clear();
        dataPacket.LayerRingDataBase.clear();
        NibbledRingData.pointIDs.clear();
        LayerRingData.layerPointIDs.clear();


    }

    @Override
    public void input() {
        super.input();
        int mouseX;
        int mouseY;
        DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer y = BufferUtils.createDoubleBuffer(1);

        glfwGetCursorPos(window.getId(), x, y);
        x.rewind();
        y.rewind();

        mouseX = (int) Math.floor(x.get());
        mouseY = gameHeight - (int) Math.floor(y.get());
        if (!pressed && glfwGetMouseButton(window.getId(), GLFW_MOUSE_BUTTON_1) == GLFW_PRESS) {
            if (mouseX > gameWidth / 3) {
                pressed = true;
                pressTime = System.currentTimeMillis();
                pressPoint = new Point(mouseX - gameWidth / 3.0f, (float) mouseY);
            }
        } else if (pressed && glfwGetMouseButton(window.getId(), GLFW_MOUSE_BUTTON_1) == GLFW_PRESS) {
            center.a = lastCenter.a + (mouseX - gameWidth / 3.0f - pressPoint.a);
            center.b = lastCenter.b + (mouseY - pressPoint.b);
        } else if (pressed && glfwGetMouseButton(window.getId(), GLFW_MOUSE_BUTTON_1) == GLFW_RELEASE) {
            pressed = false;

            lastCenter = new Point(center.a, center.b);
            if (pressPoint != null) {
                if (Math.abs(mouseX - gameWidth / 3.0f - pressPoint.a) < 15 && Math.abs(mouseY - pressPoint.b) < 15) {
                    boolean inButton = false;
                    for(Button b : buttonList){
                        if(b.inButton(mouseX, mouseY)){
                            inButton = true;
                        }
                    }
                    if(!inButton) {
                        dataPacket.points.add(new Point(displayScale / 100f * (pressPoint.a - center.a), displayScale / 100f * (pressPoint.b - center.b)));
                    }
                }
            }

        }
    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void render(float alpha) {

        /* Clear drawing area */
        renderer.clear();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            long window = org.lwjgl.glfw.GLFW.glfwGetCurrentContext();
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            GLFW.glfwGetFramebufferSize(window, widthBuffer, heightBuffer);
            gameWidth = widthBuffer.get();
            gameHeight = heightBuffer.get();
        }
        if (gameWidth != prevGameWidth || gameHeight != prevGameHeight) {
            onWindowResize();
            prevGameHeight = gameHeight;
            prevGameWidth = gameWidth;
        }

        /* Draw game objects */
        texture.bind();
        renderer.begin();

        point.setSize((int) (7 * 100 / displayScale), (int) (7 * 100 / displayScale));
        for (Point p : dataPacket.points) {
            point.setPosition(center.a + gameWidth / 3f + (100 / displayScale) * p.a - point.getHeight() / 2, center.b + (100 / displayScale) * p.b - point.getHeight() / 2);
            point.render(renderer, alpha);
        }

        boundingBox.render(renderer, alpha);
        textBox.render(renderer, alpha);

        for (Button b : buttonList) {
            b.renderButton(renderer, alpha);
        }


        renderer.end();

        /* Draw text */
        drawText(gameWidth / 3);

        int mouseX;
        int mouseY;
        DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer y = BufferUtils.createDoubleBuffer(1);

        glfwGetCursorPos(window.getId(), x, y);
        x.rewind();
        y.rewind();

        mouseX = (int) Math.floor(x.get());
        mouseY = gameHeight - (int) Math.floor(y.get());

        for (Button b : buttonList) {
            b.renderToolTip(renderer, alpha, mouseX, mouseY, gameWidth, gameHeight);
        }


    }

    private void drawText(int windowWidth) {
        windowWidth = windowWidth - 15;
        String inputText = "Please define your point set. " +
                "Click in the window to the right to place points or press \"Load\" to load points from a file. " +
                "Once you are happy with your point set press \"Done\".";

        int textWidth = renderer.getTextWidth(inputText);


        if (textWidth > windowWidth) {
            int n = 0;
            int lastSpace = 0;
            for (int i = 0; i < inputText.length(); i++, n++) {
                if (inputText.charAt(i) == ' ') {
                    lastSpace = i;
                }
                if (n == (int) (inputText.length() * ((float) windowWidth / (float) textWidth))) {
                    n = i - lastSpace;
                    inputText = inputText.substring(0, lastSpace + 1) + "\n" + inputText.substring(lastSpace + 1);
                }
            }
        }

        int textHeight = renderer.getTextHeight(inputText);
        float textX = 10;
        float textY = gameHeight - textHeight - 10;
        renderer.drawText(inputText, textX, textY, Color.BLACK);

    }

    @Override
    public void enter() {
        super.enter();

        texture = Texture.loadTexture("resources/BlankTexture.png");
        Color black = new Color(0, 0, 0);
        boundingBox = new Box(black, texture, 0, 0, gameHeight, gameWidth / 3);
        point = new Box(black, texture, 0, 0, 5, 5);
        Color white = new Color(.9f, .9f, .9f);
        textBox = new Box(white, texture, 2, 2, gameHeight - 4, gameWidth / 3 - 4);

        createButtons();

        /* Set clear color to gray */
        glClearColor(1f, 1f, 1f, 1f);
    }

    private void createButtons() {


        String testButtonTooltip = "Load a pointset from pointset.txt";
        String buttonText = "Load";
        LocationPacket locationLoad = new LocationPacket(0f, 0f, 0, 0);
        int textWidth = renderer.getTextWidth(buttonText);
        int textHeight = renderer.getTextHeight(buttonText);
        Button loadButton = new TextButton(locationLoad, gameWidth, gameHeight, textWidth, textHeight, buttonText, testButtonTooltip) {
            @Override
            protected void onPress() {
                dataPacket.points = InputReader.getPoints("pointset");
            }
        };
        buttonList.add(loadButton);

        testButtonTooltip = "Clear all current points";
        buttonText = "Clear";
        LocationPacket locationClear = new LocationPacket(0f, 0f, loadButton.getWidth() + 2, 0);
        textWidth = renderer.getTextWidth(buttonText);
        textHeight = renderer.getTextHeight(buttonText);
        Button clearButton = new TextButton(locationClear, gameWidth, gameHeight, textWidth, textHeight, buttonText, testButtonTooltip) {
            @Override
            protected void onPress() {
                dataPacket.points.clear();
            }
        };
        buttonList.add(clearButton);

        testButtonTooltip = "Undo last added point";
        buttonText = "Undo";
        LocationPacket locationUndo = new LocationPacket(0f, 0f, clearButton.getWidth() + 2, 0);
        textWidth = renderer.getTextWidth(buttonText);
        textHeight = renderer.getTextHeight(buttonText);
        Button undoButton = new TextButton(locationUndo, gameWidth, gameHeight, textWidth, textHeight, buttonText, testButtonTooltip) {
            @Override
            protected void onPress() {

                if (dataPacket.points.size() >= 1) {
                    dataPacket.points.remove(dataPacket.points.size() - 1);
                }
            }
        };
        buttonList.add(undoButton);

        testButtonTooltip = "Centers display window";
        buttonText = "Center";
        LocationPacket locationCenter = new LocationPacket(1f/3f, 1f, 0, 0);
        textWidth = renderer.getTextWidth(buttonText);
        textHeight = renderer.getTextHeight(buttonText);
        Button centerButton = new TextButton(locationCenter, gameWidth, gameHeight, textWidth, textHeight, buttonText, testButtonTooltip) {
            @Override
            protected void onPress() {
                center.a = 0;
                center.b = 0;
                lastCenter.a = 0;
                lastCenter.b = 0;
                displayScale = 100f;
            }

        };
        buttonList.add(centerButton);

        testButtonTooltip = "Continue with current point set";
        buttonText = "Done";
        LocationPacket locationDone = new LocationPacket(1f/3f, 1f, centerButton.getWidth()+2, 0);
        textWidth = renderer.getTextWidth(buttonText);
        textHeight = renderer.getTextHeight(buttonText);
        Button DoneButton = new TextButton(locationDone, gameWidth, gameHeight, textWidth, textHeight, buttonText, testButtonTooltip) {
            @Override
            protected void onPress() {
                if(dataPacket.points.size()>0)
                    Game.stateMachine.change("VerificationState", window, renderer, dataPacket);
            }

        };
        buttonList.add(DoneButton);
    }

    @Override
    public void exit() {
        super.exit();
        texture.delete();
    }

    protected void onWindowResize() {

        super.onWindowResize();
        texture = Texture.loadTexture("resources/BlankTexture.png");
        Color black = new Color(0, 0, 0);
        boundingBox = new Box(black, texture, 0, 0, gameHeight, gameWidth / 3);
        Color white = new Color(.9f, .9f, .9f);
        textBox = new Box(white, texture, 2, 2, gameHeight - 4, gameWidth / 3 - 4);
    }


}

package Visualization.state;

import DataTypes.CactusData;
import DataTypes.Point;
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
 * this class manages and rendered the verification state
 * in this state, the point set is checked to see if it is in general position
 * and the user is given the opportunity to check if the point set is as desired
 *
 * @author Jelle Schukken
 */
public class VerificationState extends State {


    private Texture texture;
    private Entity boundingBox;
    private Entity textBox;
    private Entity point;
    private CactusData dataPacket;

    private String errorMessage = "";
    private boolean valid = true;


    private long pressTime = 0;
    private Point pressPoint = null;

    private boolean pressed = false;

    /**
     * constructor
     * checks if point set is in general position. if it is not it generates an error message
     * @param window the window on which to display
     * @param renderer the renderer with which to render
     * @param dataPacket the data packet containing the point set
     */
    public VerificationState(Window window, Renderer renderer, CactusData dataPacket) {
        super(window, renderer);
        this.dataPacket = dataPacket;

        for (Point p1 : dataPacket.points) {
            for (Point p2 : dataPacket.points) {
                if (!p1.equals(p2) && p1.a == p2.a && p1.b == p2.b) {
                    valid = false;
                    errorMessage = "point (" + p1.a +
                            "," + p1.b +
                            ") is in the point set multiple times.";
                }
            }
        }
        for (int i = 0; i < dataPacket.points.size() - 2 && valid; i++) {
            Point p1 = dataPacket.points.get(i);
            for (int j = i + 1; j < dataPacket.points.size() - 1; j++) {
                Point p2 = dataPacket.points.get(j);
                for (int k = j + 1; k < dataPacket.points.size(); k++) {
                    Point p3 = dataPacket.points.get(k);
                    float ABSlope = (p2.b - p1.b) / (p2.a - p1.a);
                    float ACSlope = (p3.b - p1.b) / (p3.a - p1.a);
                    if (ABSlope == ACSlope) {//if the three points are collinear
                        valid = false;
                        System.out.println(ABSlope + ": " + ACSlope);
                        errorMessage = "points (" + p1.a +
                                "," + p1.b +
                                "), (" + p2.a +
                                "," + p2.b +
                                "), (" + p3.a + ","
                                + p3.b + ") are collinear";
                        break;
                    }

                    final double offset = Math.pow(p2.a, 2) + Math.pow(p2.b, 2);
                    final double bc = (Math.pow(p1.a, 2) + Math.pow(p1.b, 2) - offset) / 2.0;
                    final double cd = (offset - Math.pow(p3.a, 2) - Math.pow(p3.b, 2)) / 2.0;
                    final double det = (p1.a - p2.a) * (p2.b - p3.b) - (p2.a - p3.a) * (p1.b - p2.b);

                    if (Math.abs(det) < 0.000001) {
                        //stateText = "The application failed to check for co-circular points. As a result it may return an incorrect result for this point set.";
                        break;
                    }

                    final double idet = 1 / det;

                    final double centerx = (bc * (p2.b - p3.b) - cd * (p1.b - p2.b)) * idet;
                    final double centery = (cd * (p1.a - p2.a) - bc * (p2.a - p3.a)) * idet;
                    final double radius =
                            Math.sqrt(Math.pow(p2.a - centerx, 2) + Math.pow(p2.b - centery, 2));
                    Point center = new Point((float) centerx, (float) centery);

                    for (int l = k + 1; l < dataPacket.points.size(); l++) {
                        Point p4 = dataPacket.points.get(l);
                        if (Math.abs(radius - center.distance(p4)) < 0.0001) {
                            valid = false;
                            errorMessage = "points (" + p1.a +
                                    "," + p1.b +
                                    "), (" + p2.a +
                                    "," + p2.b +
                                    "), (" + p3.a + ","
                                    + p3.b + ")" + ", " + "(" + p4.a + "," +
                                    p4.b + ") are co-circular";
                            break;
                        }

                    }
                }

            }
        }

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

    /**
     * renders text to text box
     * @param windowWidth the width of the text box
     */
    private void drawText(int windowWidth) {
        windowWidth = windowWidth - 15;


        String printedString;
        if (valid) {
            if (dataPacket.points.size() == 3) {
                printedString = "This pointset simply defines a triangle." +
                        " It has 1 triangulation.";
            } else if(dataPacket.points.size() < 3){
                printedString = "This point set has less then 3 points. It has 0 triangulations. Please press \"Back\" and input a larger point set.";
            } else {
                printedString = "Please verify that the point set displayed on the right is correct. Press \"Solve\" to run the algorithm on the displayed point set or \"Back\" to input a different point set.";
            }
        } else {
            printedString = "Points must be in general position. These points are not in general position because " + errorMessage;
            printedString += " Please press \"Back\" and try a different point set.";
        }

        int textWidth = renderer.getTextWidth(printedString);


        if (textWidth > windowWidth) {
            int n = 0;
            int lastSpace = 0;
            for (int i = 0; i < printedString.length(); i++, n++) {
                if (printedString.charAt(i) == ' ') {
                    lastSpace = i;
                }
                if (n == (int) (printedString.length() * ((float) windowWidth / (float) textWidth))) {
                    n = i - lastSpace;
                    printedString = printedString.substring(0, lastSpace + 1) + "\n" + printedString.substring(lastSpace + 1);
                }
            }
        }

        int textHeight = renderer.getTextHeight(printedString);
        float textX = 10;
        float textY = gameHeight - textHeight - 10;
        renderer.drawText(printedString, textX, textY, Color.BLACK);

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
        float maxX = dataPacket.points.get(0).a;
        float minX = dataPacket.points.get(0).a;
        float maxY = dataPacket.points.get(0).b;
        float minY = dataPacket.points.get(0).b;
        for (Point p : dataPacket.points) {
            if (p.a > maxX) {
                maxX = p.a;
            }
            if (p.b > maxY) {
                maxY = p.b;
            }
            if (p.a < minX) {
                minX = p.a;
            }
            if (p.b < minY) {
                minY = p.b;
            }
        }

        float multiplier;
        if (maxX > maxY) {
            multiplier = ((gameWidth - 50) * 2f / 3f) / (maxX - Math.min(minX, minY));
        } else {
            multiplier = (gameHeight - 50) / (maxY - Math.min(minX, minY));
        }

        dataPacket.pointHeight = (int)((maxY - Math.min(minX, minY)) * multiplier) + 25;
        dataPacket.pointWidth = (int)((maxX - Math.min(minX, minY)) * multiplier) + 25;

        for (Point p : dataPacket.points) {
            p.a -= Math.min(minX, minY);
            p.b -= Math.min(minX, minY);
            p.a *= multiplier;
            p.b *= multiplier;
            p.a += 25;
            p.b += 25;
        }

        /* Set clear color to gray */
        glClearColor(1f, 1f, 1f, 1f);
    }

    /**
     * creates and positions buttons for this state
     */
    private void createButtons() {
        String testButtonTooltip;

        testButtonTooltip = "Return to the previous state";
        String buttonText = "Back";
        LocationPacket locationBack = new LocationPacket(0f, 0f, 0, 0);
        int textWidth = renderer.getTextWidth(buttonText);
        int textHeight = renderer.getTextHeight(buttonText);
        Button BackButton = new TextButton(locationBack, gameWidth, gameHeight, textWidth, textHeight, buttonText, testButtonTooltip) {
            @Override
            protected void onPress() {
                Game.stateMachine.change("InputPrompt", window, renderer, dataPacket);
            }
        };
        buttonList.add(BackButton);

        testButtonTooltip = "Centers the display window";
        buttonText = "Center";
        LocationPacket locationCenter = new LocationPacket(1f / 3f, 1f, 0, 0);
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
                //dataPacket.points.remove(dataPacket.points.size() - 1);
            }

        };
        buttonList.add(centerButton);

        testButtonTooltip = "Run Marx and Miltzow's algorithm on this point set";
        buttonText = "Solve";
        LocationPacket locationSolve = new LocationPacket(0f, 0f, BackButton.getWidth() + 2, 0);
        textWidth = renderer.getTextWidth(buttonText);
        textHeight = renderer.getTextHeight(buttonText);
        Button SolveButton = new TextButton(locationSolve, gameWidth, gameHeight, textWidth, textHeight, buttonText, testButtonTooltip) {
            @Override
            protected void onPress() {
                if (dataPacket.points.size() > 3) {
                    Game.stateMachine.change("LayerSeparatorState", window, renderer, dataPacket);
                }
            }

        };
        if (valid)
            buttonList.add(SolveButton);


    }

    @Override
    public void exit() {
        super.exit();
        texture.delete();
    }

    /**
     * resizes interior components when the window is resized
     */
    protected void onWindowResize() {

        super.onWindowResize();
        texture = Texture.loadTexture("resources/BlankTexture.png");
        Color black = new Color(0, 0, 0);
        boundingBox = new Box(black, texture, 0, 0, gameHeight, gameWidth / 3);
        Color white = new Color(.9f, .9f, .9f);
        textBox = new Box(white, texture, 2, 2, gameHeight - 4, gameWidth / 3 - 4);
    }


}

package Visualization.state;

import Algorithm.LayerRingSolver;
import Algorithm.NibbledRingSolver;
import Algorithm.NibbledRingSumProduct;
import DataTypes.*;
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
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glClearColor;


/**
 * the state for the Nibbled Ring Sub-Problem
 *
 * allows to user to explore the nibbled ring sub-problems of the datapacket
 * visualizes the currently selected nibbled ring sub-problem
 *
 * @author Jelle Schukken
 */
public class NibbledRingState extends State {

    private Texture texture;
    private Texture BlankTexture;
    private Entity boundingBox;
    private Entity textBox;
    private Entity point;
    private CactusData dataPacket;
    private ArrayList<NibbledRingData> initialProblems;
    private NibbledRingData nibbledRingData;
    private ArrayList<NibbledRingData> parentProblems = new ArrayList<>();
    private ArrayList<Integer> parentProblemIndex = new ArrayList<>();
    private int subproblemIndex = 0;
    private State prevState;


    private long pressTime = 0;
    private Point pressPoint = null;

    private boolean pressed = false;

    public NibbledRingState(Window window, Renderer renderer, CactusData dataPacket, State state, ArrayList<NibbledRingData> problems) {
        super(window, renderer);
        this.dataPacket = dataPacket;
        prevState = state;
        initialProblems = problems;
        nibbledRingData = problems.get(0);
        displayScale = 150;
        center = new Point(50,30);
        lastCenter = new Point(center.a, center.b);
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
            org.lwjgl.glfw.GLFW.glfwGetFramebufferSize(window, widthBuffer, heightBuffer);
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
        Pair<Float, Float> p1;
        Pair<Float, Float> p2;
        Pair<Float, Float> l1;
        Pair<Float, Float> l2;
        Pair<Float, Float> l3;
        Pair<Float, Float> l4;

        Pair<Float, Float> scaledLocation;

        point.setSize((int) (7f * Math.max(100 / displayScale,1)), (int)(7f * Math.max(100 / displayScale,1)));

        point.setColor(Color.BLACK);

        if(parentProblems.size() != 0) {
            NibbledRingData nrd;
            NibbledRingData parent = parentProblems.get(parentProblems.size()-1);
            if (subproblemIndex % 2 == 1) {
                nrd = parentProblems.get(parentProblems.size() - 1).getCount().getSubproblems(dataPacket).getKey().get(subproblemIndex - 1);
            } else {
                nrd = parentProblems.get(parentProblems.size() - 1).getCount().getSubproblems(dataPacket).getKey().get(subproblemIndex + 1);
            }
            for (int i = 0; i < parent.outerLayer.size() - 1; i++) {
                p1 = convertPointToScale(new Pair(parent.outerLayer.get(i).a+dataPacket.pointWidth/2, parent.outerLayer.get(i).b + dataPacket.pointHeight + 50));
                p2 = convertPointToScale(new Pair(parent.outerLayer.get(i + 1).a+dataPacket.pointWidth/2, parent.outerLayer.get(i + 1).b + dataPacket.pointHeight+ 50));
                renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale,1), Color.BLACK);
            }

            for (int i = 0; i < parent.b1.size() - 1; i++) {
                p1 = convertPointToScale(new Pair(parent.b1.get(i).a+dataPacket.pointWidth/2, parent.b1.get(i).b + dataPacket.pointHeight+ 50));
                p2 = convertPointToScale(new Pair(parent.b1.get(i + 1).a+dataPacket.pointWidth/2, parent.b1.get(i + 1).b + dataPacket.pointHeight+ 50));
                renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale,1), Color.RED);
            }

            for (int i = 0; i < parent.b2.size() - 1; i++) {
                p1 = convertPointToScale(new Pair(parent.b2.get(i).a+dataPacket.pointWidth/2, parent.b2.get(i).b + dataPacket.pointHeight+ 50));
                p2 = convertPointToScale(new Pair(parent.b2.get(i + 1).a+dataPacket.pointWidth/2, parent.b2.get(i + 1).b + dataPacket.pointHeight+ 50));
                renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale,1), Color.BLUE);
            }


            for (int i = 0; i < parent.innerCactus.getEdges().size(); i++) {
                p1 = convertPointToScale(new Pair(parent.innerCactus.getEdges().get(i).a.a+dataPacket.pointWidth/2, parent.innerCactus.getEdges().get(i).a.b + dataPacket.pointHeight+ 50));
                p2 = convertPointToScale(new Pair(parent.innerCactus.getEdges().get(i).b.a+dataPacket.pointWidth/2, parent.innerCactus.getEdges().get(i).b.b + dataPacket.pointHeight+ 50));
                renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale,1), Color.GREEN);
            }

            p1 = convertPointToScale(new Pair(parent.baseEdge.a.a+dataPacket.pointWidth/2, parent.baseEdge.a.b + dataPacket.pointHeight+ 50));
            p2 = convertPointToScale(new Pair(parent.baseEdge.b.a+dataPacket.pointWidth/2, parent.baseEdge.b.b + dataPacket.pointHeight+ 50));
            renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale,1), new Color(1,0,1));



            Color orange = new Color(1f,.7f,0f);
            p1 = convertPointToScale(new Pair(nrd.baseEdge.a.a+dataPacket.pointWidth/2, nrd.baseEdge.a.b + dataPacket.pointHeight+ 50));
            p2 = convertPointToScale(new Pair(nrd.baseEdge.b.a+dataPacket.pointWidth/2, nrd.baseEdge.b.b + dataPacket.pointHeight+ 50));
            renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale,1), orange);

            p1 = convertPointToScale(new Pair(nibbledRingData.baseEdge.a.a+dataPacket.pointWidth/2, nibbledRingData.baseEdge.a.b + dataPacket.pointHeight+ 50));
            p2 = convertPointToScale(new Pair(nibbledRingData.baseEdge.b.a+dataPacket.pointWidth/2, nibbledRingData.baseEdge.b.b + dataPacket.pointHeight+ 50));
            renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale,1), orange);

            for (int i = 0; i < nibbledRingData.b2.size() - 1; i++) {
                p1 = convertPointToScale(new Pair(nibbledRingData.b2.get(i).a+dataPacket.pointWidth/2, nibbledRingData.b2.get(i).b + dataPacket.pointHeight+ 50));
                p2 = convertPointToScale(new Pair(nibbledRingData.b2.get(i + 1).a+dataPacket.pointWidth/2, nibbledRingData.b2.get(i + 1).b + dataPacket.pointHeight+ 50));
                renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale,1), orange);

            }

            for (int i = 0; i < nrd.b1.size() - 1; i++) {
                p1 = convertPointToScale(new Pair(nrd.b1.get(i).a+dataPacket.pointWidth/2, nrd.b1.get(i).b + dataPacket.pointHeight+ 50));
                p2 = convertPointToScale(new Pair(nrd.b1.get(i + 1).a+dataPacket.pointWidth/2, nrd.b1.get(i + 1).b + dataPacket.pointHeight+ 50));
                renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale,1), orange);
            }

            point.setColor(Color.BLACK);
            for (Point p : parent.getPoints()) {
                if(!parent.innerCactus.getPoints().contains(p)) {
                    scaledLocation = convertPointToScale(new Pair(p.a + dataPacket.pointWidth / 2, p.b + dataPacket.pointHeight + 50));
                    point.setPosition(scaledLocation.getKey() - point.getWidth() / 2, scaledLocation.getValue() - point.getHeight() / 2);
                    point.render(renderer, alpha);
                }
            }

            point.setColor(Color.GREEN);
            for (Point p : parent.innerCactus.getPoints()) {
                scaledLocation = convertPointToScale(new Pair(p.a +dataPacket.pointWidth/2 , p.b+dataPacket.pointHeight+ 50));
                point.setPosition(scaledLocation.getKey() - point.getWidth() / 2, scaledLocation.getValue() - point.getHeight() / 2);
                point.render(renderer, alpha);
            }
            point.setColor(Color.BLACK);



            l1 = convertPointToScale(new Pair(dataPacket.pointWidth/2f , (float)dataPacket.pointHeight+ 50));
            l2 = convertPointToScale(new Pair(3*dataPacket.pointWidth/2f + 25f , (float)dataPacket.pointHeight+ 50));
            l3 = convertPointToScale(new Pair(3*dataPacket.pointWidth/2f + 25f , 2f*dataPacket.pointHeight + 25f + 50));
            l4 = convertPointToScale(new Pair(dataPacket.pointWidth/2f , 2f*dataPacket.pointHeight + 25f+ 50));
            renderer.drawLine(l1.getKey(), l1.getValue(), l2.getKey(), l2.getValue(), Math.max(1f * 100 / displayScale,1), Color.BLACK);
            renderer.drawLine(l1.getKey(), l1.getValue(), l4.getKey(), l4.getValue(), Math.max(1f * 100 / displayScale,1), Color.BLACK);
            renderer.drawLine(l2.getKey(), l2.getValue(), l3.getKey(), l3.getValue(), Math.max(1f * 100 / displayScale,1), Color.BLACK);
            renderer.drawLine(l4.getKey(), l4.getValue(), l3.getKey(), l3.getValue(), Math.max(1f * 100 / displayScale,1), Color.BLACK);

            l4 = convertPointToScale(new Pair((float)dataPacket.pointWidth , 2f*dataPacket.pointHeight + 25f+ 50));
            l3 = convertPointToScale(new Pair((float)dataPacket.pointWidth/2f , 0f));
            l2 = convertPointToScale(new Pair((float)dataPacket.pointWidth/2f * 3f , 0f));
            renderer.end();
            renderer.drawText("Parent", l4.getKey()-15, l4.getValue(), Color.BLACK);
            renderer.drawText("Left Child", l3.getKey()-25, l3.getValue()-20, Color.BLACK);
            renderer.drawText("Right Child", l2.getKey()-25, l2.getValue()-20, Color.BLACK);
            renderer.begin();
            texture = BlankTexture;
            texture.bind();


            for (int i = 0; i < nrd.outerLayer.size() - 1; i++) {
                p1 = convertPointToScale(new Pair(nrd.outerLayer.get(i).a+dataPacket.pointWidth + 25, nrd.outerLayer.get(i).b));
                p2 = convertPointToScale(new Pair(nrd.outerLayer.get(i + 1).a+dataPacket.pointWidth + 25, nrd.outerLayer.get(i + 1).b));
                renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale,1), Color.BLACK);
            }

            for (int i = 0; i < nrd.b1.size() - 1; i++) {
                p1 = convertPointToScale(new Pair(nrd.b1.get(i).a+dataPacket.pointWidth + 25, nrd.b1.get(i).b));
                p2 = convertPointToScale(new Pair(nrd.b1.get(i + 1).a+dataPacket.pointWidth + 25, nrd.b1.get(i + 1).b));
                renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale,1), Color.RED);
            }

            if(false && nrd.outerBoundary != null) {//debug show boundary
                for (int i = 0; i < nrd.outerBoundary.getPoints().size() - 1; i++) {
                    Point a = nrd.outerBoundary.getPoints().get(i);
                    Point b = nrd.outerBoundary.getPoints().get(i + 1);
                    p1 = convertPointToScale(new Pair(a.a + dataPacket.pointWidth + 25, a.b));
                    p2 = convertPointToScale(new Pair(b.a + dataPacket.pointWidth + 25, b.b));
                    renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(8f * 100 / displayScale, 1), Color.RED);
                }
            }

            for (int i = 0; i < nrd.b2.size() - 1; i++) {
                p1 = convertPointToScale(new Pair(nrd.b2.get(i).a+dataPacket.pointWidth + 25, nrd.b2.get(i).b));
                p2 = convertPointToScale(new Pair(nrd.b2.get(i + 1).a+dataPacket.pointWidth + 25, nrd.b2.get(i + 1).b));
                renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale,1), Color.BLUE);
            }

            for (int i = 0; i < nrd.innerCactus.getEdges().size(); i++) {
                p1 = convertPointToScale(new Pair(nrd.innerCactus.getEdges().get(i).a.a+dataPacket.pointWidth + 25, nrd.innerCactus.getEdges().get(i).a.b));
                p2 = convertPointToScale(new Pair(nrd.innerCactus.getEdges().get(i).b.a+dataPacket.pointWidth + 25, nrd.innerCactus.getEdges().get(i).b.b));
                renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale,1), Color.GREEN);
            }

            p1 = convertPointToScale(new Pair(nrd.baseEdge.a.a+dataPacket.pointWidth+25, nrd.baseEdge.a.b));
            p2 = convertPointToScale(new Pair(nrd.baseEdge.b.a+dataPacket.pointWidth+25, nrd.baseEdge.b.b));
            renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale,1), new Color(1,0,1));

            point.setColor(Color.BLACK);
            for (Point p : nrd.getPoints()) {
                if(!nrd.innerCactus.getPoints().contains(p)) {
                    scaledLocation = convertPointToScale(new Pair(p.a + dataPacket.pointWidth + 25, p.b));
                    point.setPosition(scaledLocation.getKey() - point.getWidth() / 2, scaledLocation.getValue() - point.getHeight() / 2);
                    point.render(renderer, alpha);
                }
            }

            point.setColor(Color.GREEN);
            for (Point p : nrd.innerCactus.getPoints()) {
                scaledLocation = convertPointToScale(new Pair(p.a +dataPacket.pointWidth + 25 , p.b));
                point.setPosition(scaledLocation.getKey() - point.getWidth() / 2, scaledLocation.getValue() - point.getHeight() / 2);
                point.render(renderer, alpha);
            }
            point.setColor(Color.BLACK);

            l1 = convertPointToScale(new Pair((float)dataPacket.pointWidth + 25f , 0f));
            l2 = convertPointToScale(new Pair(2f*dataPacket.pointWidth + 50f , 0f));
            l3 = convertPointToScale(new Pair(2f*dataPacket.pointWidth + 50f , (float)dataPacket.pointHeight + 25f));
            l4 = convertPointToScale(new Pair((float)dataPacket.pointWidth + 25f , (float)dataPacket.pointHeight + 25f));
            renderer.drawLine(l1.getKey(), l1.getValue(), l2.getKey(), l2.getValue(), Math.max(1f * 100 / displayScale,1), Color.BLACK);
            renderer.drawLine(l1.getKey(), l1.getValue(), l4.getKey(), l4.getValue(), Math.max(1f * 100 / displayScale,1), Color.BLACK);
            renderer.drawLine(l2.getKey(), l2.getValue(), l3.getKey(), l3.getValue(), Math.max(1f * 100 / displayScale,1), Color.BLACK);
            renderer.drawLine(l4.getKey(), l4.getValue(), l3.getKey(), l3.getValue(), Math.max(1f * 100 / displayScale,1), Color.BLACK);


        }


        //________________EDGES__________________________
        for (int i = 0; i < nibbledRingData.outerLayer.size() - 1; i++) {
            p1 = convertPointToScale(new Pair(nibbledRingData.outerLayer.get(i).a, nibbledRingData.outerLayer.get(i).b));
            p2 = convertPointToScale(new Pair(nibbledRingData.outerLayer.get(i + 1).a, nibbledRingData.outerLayer.get(i + 1).b));
            renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale,1), Color.BLACK);
        }

        for (int i = 0; i < nibbledRingData.b1.size() - 1; i++) {
            p1 = convertPointToScale(new Pair(nibbledRingData.b1.get(i).a, nibbledRingData.b1.get(i).b));
            p2 = convertPointToScale(new Pair(nibbledRingData.b1.get(i + 1).a, nibbledRingData.b1.get(i + 1).b));
            renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale,1), Color.RED);
        }

        for (int i = 0; i < nibbledRingData.b2.size() - 1; i++) {
            p1 = convertPointToScale(new Pair(nibbledRingData.b2.get(i).a, nibbledRingData.b2.get(i).b));
            p2 = convertPointToScale(new Pair(nibbledRingData.b2.get(i + 1).a, nibbledRingData.b2.get(i + 1).b));
            renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale,1), Color.BLUE);
        }

        for (int i = 0; i < nibbledRingData.innerCactus.getEdges().size(); i++) {
            p1 = convertPointToScale(new Pair(nibbledRingData.innerCactus.getEdges().get(i).a.a, nibbledRingData.innerCactus.getEdges().get(i).a.b));
            p2 = convertPointToScale(new Pair(nibbledRingData.innerCactus.getEdges().get(i).b.a, nibbledRingData.innerCactus.getEdges().get(i).b.b));
            renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale,1), Color.GREEN);
        }

        p1 = convertPointToScale(new Pair(nibbledRingData.baseEdge.a.a, nibbledRingData.baseEdge.a.b));
        p2 = convertPointToScale(new Pair(nibbledRingData.baseEdge.b.a, nibbledRingData.baseEdge.b.b));
        renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale,1), new Color(1,0,1));


        l1 = convertPointToScale(new Pair(0f, 0f));
        l2 = convertPointToScale(new Pair((float)dataPacket.pointWidth + 25f , 0f));
        l3 = convertPointToScale(new Pair((float)dataPacket.pointWidth + 25f , (float)dataPacket.pointHeight + 25f));
        l4 = convertPointToScale(new Pair(0f , (float)dataPacket.pointHeight + 25f));
        renderer.drawLine(l1.getKey(), l1.getValue(), l2.getKey(), l2.getValue(), Math.max(1f * 100 / displayScale,1), Color.BLACK);
        renderer.drawLine(l1.getKey(), l1.getValue(), l4.getKey(), l4.getValue(), Math.max(1f * 100 / displayScale,1), Color.BLACK);
        renderer.drawLine(l2.getKey(), l2.getValue(), l3.getKey(), l3.getValue(), Math.max(1f * 100 / displayScale,1), Color.BLACK);
        renderer.drawLine(l4.getKey(), l4.getValue(), l3.getKey(), l3.getValue(), Math.max(1f * 100 / displayScale,1), Color.BLACK);

        //____________END EDGES__________________
        //____________POINTS___________________

        for (Point p : nibbledRingData.freePoints) {
            scaledLocation = convertPointToScale(new Pair(p.a, p.b));
            point.setPosition(scaledLocation.getKey() - point.getWidth() / 2, scaledLocation.getValue() - point.getHeight() / 2);
            point.render(renderer, alpha);
        }

        for (Point p : nibbledRingData.b1) {
            scaledLocation = convertPointToScale(new Pair(p.a, p.b));
            point.setPosition(scaledLocation.getKey() - point.getWidth() / 2, scaledLocation.getValue() - point.getHeight() / 2);
            point.render(renderer, alpha);
        }

        for (Point p : nibbledRingData.b2) {
            scaledLocation = convertPointToScale(new Pair(p.a, p.b));
            point.setPosition(scaledLocation.getKey() - point.getWidth() / 2, scaledLocation.getValue() - point.getHeight() / 2);
            point.render(renderer, alpha);
        }

        for (Point p : nibbledRingData.outerLayer) {
            scaledLocation = convertPointToScale(new Pair(p.a, p.b));
            point.setPosition(scaledLocation.getKey() - point.getWidth() / 2, scaledLocation.getValue() - point.getHeight() / 2);
            point.render(renderer, alpha);
        }

        point.setColor(Color.GREEN);
        for (Point p : nibbledRingData.innerCactus.getPoints()) {
            scaledLocation = convertPointToScale(new Pair(p.a,p.b));
            point.setPosition(scaledLocation.getKey() - point.getWidth() / 2, scaledLocation.getValue() - point.getHeight() / 2);
            point.render(renderer, alpha);
        }

        point.setColor(Color.BLACK);
        //________________END POINTS_______________________

        drawKey(alpha);
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

    private void drawKey(float alpha){

        int width = 150;
        int height = 125;
        //texture = Texture.loadTexture("resources/BlankTexture.png");
        Color grey = new Color(.95f, .95f, .95f);
        new Box(grey, texture, gameWidth-width, gameHeight-height, height, width).render(renderer, alpha);
        new Box(Color.GREEN, texture, gameWidth-width+5, gameHeight-height+5, 15, 15).render(renderer, alpha);
        new Box(Color.BLACK, texture, gameWidth-width+5, gameHeight-height+25, 15, 15).render(renderer, alpha);
        new Box(Color.RED, texture, gameWidth-width+5, gameHeight-height+45, 15, 15).render(renderer, alpha);
        new Box(Color.BLUE, texture, gameWidth-width+5, gameHeight-height+65, 15, 15).render(renderer, alpha);
        new Box(new Color(1,0,1), texture, gameWidth-width+5, gameHeight-height+85, 15, 15).render(renderer, alpha);
        new Box(new Color(1f,.7f,0f), texture, gameWidth-width+5, gameHeight-height+105, 15, 15).render(renderer, alpha);
        renderer.end();
        renderer.drawText("Inner Layer", gameWidth-width+25, gameHeight-height+5, Color.BLACK);
        renderer.drawText("Outer Layer", gameWidth-width+25, gameHeight-height+25, Color.BLACK);
        renderer.drawText("b1", gameWidth-width+25, gameHeight-height+45, Color.BLACK);
        renderer.drawText("b2", gameWidth-width+25, gameHeight-height+65, Color.BLACK);
        renderer.drawText("Base Edge", gameWidth-width+25, gameHeight-height+85, Color.BLACK);
        renderer.drawText("Separator Path", gameWidth-width+25, gameHeight-height+105, Color.BLACK);
        renderer.begin();
    }

    private void drawText(int windowWidth) {
        windowWidth = windowWidth - 15;

        String inputText;
        if(!dataPacket.outOfTime) {
            if (parentProblems.size() > 0) {
                inputText = "The visualization on the right shows three nibbled ring sub-problems. " +
                        "The parent at the top and two sub-problems on the bottom. " +
                        "The left and the right sub-problem are formed by the parent problem with the orange separator path. ";
                inputText += "The left sub-problem is constrained with the constraint vector: " + nibbledRingData.constraints.display() + " and has ";
                if (nibbledRingData.getCount().get() == 1) {
                    inputText += "1 triangulation. ";
                } else {
                    inputText += nibbledRingData.getCount().get() + " triangulations. ";
                }

                NibbledRingData nrd = parentProblems.get(parentProblems.size() - 1).getCount().getSubproblems(dataPacket).getKey().get(subproblemIndex + 1);
                inputText += "The right sub-problem is constrained with the constraint vector: " + nrd.constraints.display() + " and has ";
                if (nrd.getCount().get() == 1) {
                    inputText += "1 triangulation. ";
                } else {
                    inputText += nrd.getCount().get() + " triangulations. ";
                }

            } else {
                inputText = "The visualization on the right shows a nibbled ring sub-problem. ";

                inputText += " This sub-problem is constrained with the constraint vector: " + nibbledRingData.constraints.display() + " and has ";
                if (nibbledRingData.getCount().get() == 1) {
                    inputText += "1 triangulation. ";
                } else {
                    inputText += nibbledRingData.getCount().get() + " triangulations. ";
                }
            }
        }else{
            inputText = "The algorithm ran out of memory or was stopped before it could count the triangulations for the point set. " +
                    "You can still explore what it did compute, however there will be missing sub-problems and no triangulation counts.";
            if (parentProblems.size() > 0) {
                inputText += "The visualization on the right shows three nibbled ring sub-problems. " +
                        "The parent at the top and two sub-problems on the bottom. " +
                        "The left and the right sub-problem are formed by the parent problem with the orange separator path. ";
                inputText += "The left sub-problem is constrained with the constraint vector: " + nibbledRingData.constraints.display() + ". ";

                NibbledRingData nrd = parentProblems.get(parentProblems.size() - 1).getCount().getSubproblems(dataPacket).getKey().get(subproblemIndex + 1);
                inputText += "The right sub-problem is constrained with the constraint vector: " + nrd.constraints.display() + ". ";

            } else {
                inputText += "The visualization on the right shows a nibbled ring sub-problem. ";

                inputText += "This sub-problem is constrained with the constraint vector: " + nibbledRingData.constraints.display() + ".";
            }
        }

        inputText = resizeText(windowWidth, inputText);

        String descriptionText;
        if(parentProblems.size() == 0){
            descriptionText = "Press \"Next\" to view the next nibbled ring sub-problem with the same parent. " +
                    "Press either \"Explore Left\" or \"Explore Right\" to view the sub-problems of this problem. " +
                    "Press \"Parent\" to return to the parent of this problem. " +
                    "Press \"Restart\" to input a different point set. ";
        }else{
            descriptionText = "Press \"Next\" to view the next pair of nibbled ring sub-problems with the same parent. " +
                    "Press \"Explore Left\" to view the left nibbled ring sub-problem. " +
                    "Press \"Explore Right\" to view right nibbled ring sub-problem. " +
                    "Press \"Parent\" to return to the parent of this problem. " +
                    "Press \"Restart\" to input a different point set. ";
        }
        if(dataPacket.outOfTime) {
            descriptionText += "Press \"Solve\" to reattempt to compute the triangulations with no memory limit.";
        }
        descriptionText = resizeText(windowWidth, descriptionText);

        int textHeight = renderer.getTextHeight(inputText);
        float textX = 10;
        float textY = gameHeight - textHeight - 10;
        renderer.drawText(inputText, textX, textY, Color.BLACK);
        int textHeight2 = renderer.getTextHeight(descriptionText);
        float textX2 = 10;
        float textY2 = gameHeight - textHeight - textHeight2 - 20;
        renderer.drawText(descriptionText, textX2, textY2, Color.BLACK);

    }

    private String resizeText(int windowWidth, String inputText){
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

        return inputText;
    }

    @Override
    public void enter() {
        super.enter();

        texture = Texture.loadTexture("resources/BlankTexture.png");
        BlankTexture = Texture.loadTexture("resources/BlankTexture.png");
        Color black = new Color(0, 0, 0);
        boundingBox = new Box(black, texture, 0, 0, gameHeight, gameWidth / 3);
        point = new Box(black, texture, 0, 0, 7, 7);
        Color white = new Color(.9f, .9f, .9f);
        textBox = new Box(white, texture, 2, 2, gameHeight - 4, gameWidth / 3 - 4);

        createButtons();

        /* Set clear color to gray */
        glClearColor(1f, 1f, 1f, 1f);
    }

    private void createButtons() {

        String testButtonTooltip = "Switches to next pair of children";
        String buttonText = "Next";
        LocationPacket locationNibbled = new LocationPacket(1f / 3f, 1f, 0, 0);
        int textWidth = renderer.getTextWidth(buttonText);
        int textHeight = renderer.getTextHeight(buttonText);
        Button NibbledButton = new TextButton(locationNibbled, gameWidth, gameHeight, textWidth, textHeight, buttonText, testButtonTooltip) {
            @Override
            protected void onPress() {
                if(parentProblems.size() != 0){
                    subproblemIndex += 2;
                    subproblemIndex = (subproblemIndex/2)*2;
                    subproblemIndex = subproblemIndex % parentProblems.get(parentProblems.size()-1).getCount().getSubproblems(dataPacket).getKey().size();
                    nibbledRingData = parentProblems.get(parentProblems.size()-1).getCount().getSubproblems(dataPacket).getKey().get(subproblemIndex);
                }else{
                    subproblemIndex = (subproblemIndex+1)%initialProblems.size();
                    nibbledRingData = initialProblems.get(subproblemIndex);
                }
            }

        };
        buttonList.add(NibbledButton);

        testButtonTooltip = "Switches to left sub-problem";
        buttonText = "Explore Left";
        LocationPacket locationLeft = new LocationPacket(1f / 3f, 1f, NibbledButton.getWidth() + 2, 0);
        textWidth = renderer.getTextWidth(buttonText);
        textHeight = renderer.getTextHeight(buttonText);
        Button LeftNibbledButton = new TextButton(locationLeft, gameWidth, gameHeight, textWidth, textHeight, buttonText, testButtonTooltip) {
            @Override
            protected void onPress() {
                if(nibbledRingData.getCount() != null && nibbledRingData.getCount().getSubproblems(dataPacket).getKey().size() > 0) {
                    parentProblems.add(nibbledRingData);
                    parentProblemIndex.add(new Integer(subproblemIndex));
                    subproblemIndex = 0;
                    nibbledRingData = parentProblems.get(parentProblems.size() - 1).getCount().getSubproblems(dataPacket).getKey().get(subproblemIndex);
                    displayScale = 250;
                    center = new Point(50,30);
                    lastCenter = new Point(center.a, center.b);
                }

            }

        };
        buttonList.add(LeftNibbledButton);

        testButtonTooltip = "Switches to right sub-problem";
        buttonText = "Explore Right";
        LocationPacket locationRight = new LocationPacket(1f / 3f, 1f, LeftNibbledButton.getWidth() + 2, 0);
        textWidth = renderer.getTextWidth(buttonText);
        textHeight = renderer.getTextHeight(buttonText);
        Button RightNibbledButton = new TextButton(locationRight, gameWidth, gameHeight, textWidth, textHeight, buttonText, testButtonTooltip) {
            @Override
            protected void onPress() {
                NibbledRingData set = nibbledRingData;
                if(parentProblems.size() != 0){
                    set = parentProblems.get(parentProblems.size()-1).getCount().getSubproblems(dataPacket).getKey().get(subproblemIndex+1);
                    parentProblemIndex.add(new Integer(subproblemIndex+1));
                }else{
                    parentProblemIndex.add(new Integer(subproblemIndex));
                }

                if(set.getCount() != null && set.getCount().getSubproblems(dataPacket).getKey().size() > 0) {
                    parentProblems.add(set);

                    subproblemIndex = 0;
                    nibbledRingData = parentProblems.get(parentProblems.size() - 1).getCount().getSubproblems(dataPacket).getKey().get(subproblemIndex);
                    displayScale = 250;
                    center = new Point(50,30);
                    lastCenter = new Point(center.a, center.b);
                }
            }

        };
        buttonList.add(RightNibbledButton);

        testButtonTooltip = "Returns to the parent problem";
        buttonText = "Parent";
        LocationPacket locationParent = new LocationPacket(1f / 3f, 1f, RightNibbledButton.getWidth() + 2, 0);
        textWidth = renderer.getTextWidth(buttonText);
        textHeight = renderer.getTextHeight(buttonText);
        Button ParentNibbledButton = new TextButton(locationParent, gameWidth, gameHeight, textWidth, textHeight, buttonText, testButtonTooltip) {
            @Override
            protected void onPress() {

                if(parentProblems.size() > 1) {
                    subproblemIndex = parentProblemIndex.remove(parentProblemIndex.size() - 1);
                    if(subproblemIndex%2 == 1){
                        parentProblems.remove(parentProblems.size() - 1);
                        subproblemIndex--;
                        nibbledRingData = parentProblems.get(parentProblems.size() - 1).getCount().getSubproblems(dataPacket).getKey().get(subproblemIndex);
                    }else {
                        nibbledRingData = parentProblems.remove(parentProblems.size() - 1);
                    }
                }else if(parentProblems.size() > 0){
                    subproblemIndex = parentProblemIndex.remove(parentProblemIndex.size() - 1);
                    nibbledRingData = parentProblems.remove(parentProblems.size() - 1);
                    center = new Point(50,30);
                    lastCenter = new Point(center.a, center.b);
                }else if(prevState != null){
                    Game.stateMachine.back(prevState);
                }
            }

        };
        buttonList.add(ParentNibbledButton);

        testButtonTooltip = "Return to the start and input a new point set";
        buttonText = "Restart";
        LocationPacket locationRestart= new LocationPacket(1f, 0f, 0, 0);
        textWidth = renderer.getTextWidth(buttonText);
        textHeight = renderer.getTextHeight(buttonText);
        Button restartButton = new TextButton(locationRestart, gameWidth, gameHeight, textWidth, textHeight, buttonText, testButtonTooltip) {
            @Override
            protected void onPress() {
                Game.stateMachine.change("Restart", window, renderer, null);
            }

        };
        buttonList.add(restartButton);
    }

    @Override
    public void exit() {
        super.exit();
        texture.delete();
        BlankTexture.delete();
    }

    protected void onWindowResize() {

        super.onWindowResize();
        texture = BlankTexture;
        Color black = new Color(0, 0, 0);
        boundingBox = new Box(black, texture, 0, 0, gameHeight, gameWidth / 3);
        Color white = new Color(.9f, .9f, .9f);
        textBox = new Box(white, texture, 2, 2, gameHeight - 4, gameWidth / 3 - 4);
    }



}


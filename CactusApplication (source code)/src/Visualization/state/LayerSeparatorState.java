package Visualization.state;

import Algorithm.LayerRingSolver;
import Algorithm.NibbledRingSolver;
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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glClearColor;

/**
 * the state for the general unconstrained-layer ring sub-problem
 *
 * allows the user to explore the general unconstrained-layer ring sub-problems
 * visualises the currently selected sub-problem
 */
public class LayerSeparatorState extends State {

    private Texture texture;
    private Entity boundingBox;
    private Entity textBox;
    private Entity point;
    private CactusData dataPacket;
    private LayerRingData layerRingData;
    private NibbledRingData nrd;
    private ArrayList<LayerRingData> parentProblems = new ArrayList<>();
    private ArrayList<Integer> parentProblemIndex = new ArrayList<>();
    private Texture BlankTexture;
    private int subproblemIndex = 0;
    private boolean skip = false;


    LoadingDisplay algorithmSolver;
    final Semaphore loading = new Semaphore(1, true);

    private ArrayList<Integer> memoryUsage = new ArrayList<>();
    int dots = 0;
    boolean memlimit = true;


    private long pressTime = 0;
    private Point pressPoint = null;

    private boolean pressed = false;


    public LayerSeparatorState(Window window, Renderer renderer, CactusData dataPacket) {
        super(window, renderer);
        this.dataPacket = dataPacket;

        center = new Point(50, 30);
        lastCenter = new Point(center.a, center.b);
        displayScale = 125;

        ArrayList<Point> outerLayer = dataPacket.getConvexHull();
        ArrayList<Point> freePoints = new ArrayList<>();
        for (Point p : dataPacket.points) {
            if (!outerLayer.contains(p)) {
                freePoints.add(p);
            }
        }

        LayerRingData layerRingData = new LayerRingData(outerLayer, freePoints, 0);

        dataPacket.LayerRingDataBase.put(layerRingData.getStringID(), layerRingData);

        this.layerRingData = layerRingData;

        solve();

    }

    /**
     * Uses Marx and Miltzow's algorithm to count the triangulations of the point set in data
     */
    private void solve() {

        algorithmSolver = new LoadingDisplay(loading, dataPacket, memlimit);
        algorithmSolver.start();
    }

    public void renderLoading() {
        String loadingText;
        if(loading.availablePermits() == 0){
            loadingText = "Stopping";
        }else {
            loadingText = "Loading";
        }
        for (int i = 0; i < dots; i += 30) {
            loadingText += " .";
        }
        dots = (dots + 1) % 150;
        float textX = gameWidth / 2 - 25;
        float textY = gameHeight / 2;
        renderer.drawText(loadingText, textX, textY, Color.BLACK);

        textX = gameWidth - 110;
        textY = gameHeight - 130;
        renderer.drawText("Heap Usage(%)", textX, textY, Color.BLACK);


        float used = 0;
        float max = 0;
        for (MemoryPoolMXBean mpBean : ManagementFactory.getMemoryPoolMXBeans()) {
            if (mpBean.getType() == MemoryType.HEAP) {
                max += mpBean.getUsage().getMax();
                used += mpBean.getUsage().getUsed();
            }
        }

        int round = (int) ((max * 100) / 1073741824l);
        String space = "Max: " + round / 100f + "GB";
        renderer.drawText(space, gameWidth - 110 - renderer.getTextWidth(space), gameHeight - 60, Color.BLACK);

        memoryUsage.add((int) ((used * 100) / max));
        if (memoryUsage.size() > 300)
            memoryUsage.remove(0);

        texture.bind();
        renderer.begin();

        for (int i = 0; i < memoryUsage.size() - 2; i += 3) {
            int average = (memoryUsage.get(i) + memoryUsage.get(i + 1) + memoryUsage.get(i + 2)) / 3;
            renderer.drawLine(gameWidth - 5 - 100 + i / 3, gameHeight - 100 + average, gameWidth - 5 - 100 + i / 3, gameHeight - 100, 1f, Color.BLACK);
        }
        if (memlimit) {
            renderer.drawLine(gameWidth - 5 - 100, gameHeight - 12, gameWidth - 5, gameHeight - 12, 1f, Color.RED);
        }
        renderer.drawLine(gameWidth - 5 - 100, gameHeight - 100, gameWidth - 5, gameHeight - 100, 1f, Color.BLACK);
        renderer.drawLine(gameWidth - 5 - 100, gameHeight, gameWidth - 5, gameHeight, 1f, Color.BLACK);
        renderer.drawLine(gameWidth - 5 - 100, gameHeight - 100, gameWidth - 5 - (100), gameHeight, 1f, Color.BLACK);
        renderer.drawLine(gameWidth - 5, gameHeight - 100, gameWidth - 5, gameHeight, 1f, Color.BLACK);

        for (Button b : buttonList) {
            b.renderButton(renderer, 1f);
        }


        renderer.end();

        /* Draw text */

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
            b.renderToolTip(renderer, 1f, mouseX, mouseY, gameWidth, gameHeight);
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
            //System.out.println(displayScale);

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

        if (algorithmSolver.isAlive()) {
            renderLoading();
        } else {
            if (buttonList.size() == 1) {
                createButtons();
                if (!dataPacket.outOfTime && layerRingData != null && layerRingData.getCount().getSubproblems(dataPacket).getValue().size() == 0) {
                    skip = true;
                }
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
            if (parentProblems.size() == 0) {
                l1 = new Pair(0f, 0f);
                l2 = convertPointToScale(new Pair(dataPacket.pointWidth + 25f, 0f));
                l3 = convertPointToScale(new Pair(dataPacket.pointWidth + 25f, dataPacket.pointHeight + 25f));
                l4 = convertPointToScale(new Pair(0f, dataPacket.pointHeight + 25f));
                point.setSize((int) (7 * 100 / displayScale), (int) (7 * 100 / displayScale));
            } else {
                point.setSize((int) (7f * Math.max(100 / displayScale,1)), (int)(7f * Math.max(100 / displayScale,1)));
                l1 = new Pair(dataPacket.pointWidth / 2f, (float) dataPacket.pointHeight + 50);
                l2 = convertPointToScale(new Pair(3f * dataPacket.pointWidth / 2f + 25f, (float) dataPacket.pointHeight + 50));
                l3 = convertPointToScale(new Pair(3f * dataPacket.pointWidth / 2f + 25f, 2f * dataPacket.pointHeight + 25f + 50));
                l4 = convertPointToScale(new Pair(dataPacket.pointWidth / 2f, 2f * dataPacket.pointHeight + 25f + 50));
            }


            //________________CURRENT SUB-PROBLEM_____________________
            renderer.drawLine(convertPointToScale(l1).getKey(), convertPointToScale(l1).getValue(), l2.getKey(), l2.getValue(), Math.max(1f * 100 / displayScale, 1), Color.BLACK);
            renderer.drawLine(convertPointToScale(l1).getKey(), convertPointToScale(l1).getValue(), l4.getKey(), l4.getValue(), Math.max(1f * 100 / displayScale, 1), Color.BLACK);
            renderer.drawLine(l2.getKey(), l2.getValue(), l3.getKey(), l3.getValue(), Math.max(1f * 100 / displayScale, 1), Color.BLACK);
            renderer.drawLine(l4.getKey(), l4.getValue(), l3.getKey(), l3.getValue(), Math.max(1f * 100 / displayScale, 1), Color.BLACK);

            if (nrd != null) {




                for (Point p : nrd.freePoints) {
                    scaledLocation = convertPointToScale(new Pair(p.a + l1.getKey(), p.b + l1.getValue()));
                    point.setPosition(scaledLocation.getKey() - point.getWidth() / 2, scaledLocation.getValue() - point.getHeight() / 2);
                    point.render(renderer, alpha);
                }

                for (Point p : nrd.outerLayer) {
                    scaledLocation = convertPointToScale(new Pair(p.a + l1.getKey(), p.b + l1.getValue()));
                    point.setPosition(scaledLocation.getKey() - point.getWidth() / 2, scaledLocation.getValue() - point.getHeight() / 2);
                    point.render(renderer, alpha);
                }
            }

            ArrayList<Point> convexHull;
            if (parentProblems.size() > 0) {
                convexHull = parentProblems.get(parentProblems.size() - 1).outerLayer;
            } else {
                convexHull = dataPacket.getConvexHull();
            }
            //________________EDGES__________________________
            for (int i = 0; i < convexHull.size() - 1; i++) {
                p1 = convertPointToScale(new Pair(convexHull.get(i).a + l1.getKey(), convexHull.get(i).b + l1.getValue()));
                p2 = convertPointToScale(new Pair(convexHull.get(i + 1).a + l1.getKey(), convexHull.get(i + 1).b + l1.getValue()));
                renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(),  Math.max(1f * 100 / displayScale,1), Color.BLACK);
            }
            p1 = convertPointToScale(new Pair(convexHull.get(0).a + l1.getKey(), convexHull.get(0).b + l1.getValue()));
            p2 = convertPointToScale(new Pair(convexHull.get(convexHull.size() - 1).a + l1.getKey(), convexHull.get(convexHull.size() - 1).b + l1.getValue()));
            renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(),  Math.max(1f * 100 / displayScale,1), Color.BLACK);

            //____________END EDGES__________________
            //____________POINTS___________________


            ArrayList<Point> outerPoints = new ArrayList<>();
            if (parentProblems.size() > 0) {
                outerPoints.addAll(parentProblems.get(parentProblems.size() - 1).freePoints);
                outerPoints.addAll(parentProblems.get(parentProblems.size() - 1).outerLayer);
            } else {
                outerPoints.addAll(dataPacket.points);
            }
            for (Point p : outerPoints) {
                scaledLocation = convertPointToScale(new Pair(p.a + l1.getKey(), p.b + l1.getValue()));
                point.setPosition(scaledLocation.getKey() - point.getWidth() / 2, scaledLocation.getValue() - point.getHeight() / 2);
                point.render(renderer, alpha);
            }

            if (layerRingData != null) {
                for (Point p : layerRingData.freePoints) {
                    scaledLocation = convertPointToScale(new Pair(p.a + l1.getKey(), p.b + l1.getValue()));
                    point.setPosition(scaledLocation.getKey() - point.getWidth() / 2, scaledLocation.getValue() - point.getHeight() / 2);
                    point.render(renderer, alpha);
                }

                for (Point p : layerRingData.outerLayer) {
                    scaledLocation = convertPointToScale(new Pair(p.a + l1.getKey(), p.b + l1.getValue()));
                    point.setPosition(scaledLocation.getKey() - point.getWidth() / 2, scaledLocation.getValue() - point.getHeight() / 2);
                    point.render(renderer, alpha);
                }
                point.setColor(Color.BLACK);
            }
            //________________END CURRENT SUB-PROBLEM___________________
            //________________OUTER SUB-PROBLEM_________________________
            if (parentProblems.size() != 0) {
                l1 = new Pair(0f, 0f);
                l2 = convertPointToScale(new Pair(dataPacket.pointWidth + 25f, 0f));
                l3 = convertPointToScale(new Pair(dataPacket.pointWidth + 25f, dataPacket.pointHeight + 25f));
                l4 = convertPointToScale(new Pair(0f, dataPacket.pointHeight + 25f));

                renderer.drawLine(convertPointToScale(l1).getKey(), convertPointToScale(l1).getValue(), l2.getKey(), l2.getValue(), Math.max(1f * 100 / displayScale, 1), Color.BLACK);
                renderer.drawLine(convertPointToScale(l1).getKey(), convertPointToScale(l1).getValue(), l4.getKey(), l4.getValue(), Math.max(1f * 100 / displayScale, 1), Color.BLACK);
                renderer.drawLine(l2.getKey(), l2.getValue(), l3.getKey(), l3.getValue(), Math.max(1f * 100 / displayScale, 1), Color.BLACK);
                renderer.drawLine(l4.getKey(), l4.getValue(), l3.getKey(), l3.getValue(), Math.max(1f * 100 / displayScale, 1), Color.BLACK);

                if(nrd != null) {
                    for (Edge e : nrd.innerCactus.getEdges()) {
                        p1 = convertPointToScale(new Pair(e.a.a + l1.getKey(), e.a.b + l1.getValue()));
                        p2 = convertPointToScale(new Pair(e.b.a + l1.getKey(), e.b.b + l1.getValue()));
                        renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale, 1), Color.GREEN);
                    }

                    for (int i = 0; i < nrd.outerLayer.size() - 1; i++) {
                        p1 = convertPointToScale(new Pair(convexHull.get(i).a + l1.getKey(), convexHull.get(i).b + l1.getValue()));
                        p2 = convertPointToScale(new Pair(convexHull.get(i + 1).a + l1.getKey(), convexHull.get(i + 1).b + l1.getValue()));
                        renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale, 1), Color.BLACK);
                    }
                    p1 = convertPointToScale(new Pair(convexHull.get(0).a + l1.getKey(), convexHull.get(0).b + l1.getValue()));
                    p2 = convertPointToScale(new Pair(convexHull.get(convexHull.size() - 1).a + l1.getKey(), convexHull.get(convexHull.size() - 1).b + l1.getValue()));
                    renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale, 1), Color.BLACK);

                    for (Point p : nrd.freePoints) {
                        scaledLocation = convertPointToScale(new Pair(p.a + l1.getKey(), p.b + l1.getValue()));
                        point.setPosition(scaledLocation.getKey() - point.getWidth() / 2, scaledLocation.getValue() - point.getHeight() / 2);
                        point.render(renderer, alpha);
                    }

                    for (Point p : nrd.outerLayer) {
                        scaledLocation = convertPointToScale(new Pair(p.a + l1.getKey(), p.b + l1.getValue()));
                        point.setPosition(scaledLocation.getKey() - point.getWidth() / 2, scaledLocation.getValue() - point.getHeight() / 2);
                        point.render(renderer, alpha);
                    }

                    point.setColor(Color.GREEN);
                    for (Point p : nrd.innerCactus.getPoints()) {
                        scaledLocation = convertPointToScale(new Pair(p.a + l1.getKey(), p.b + l1.getValue()));
                        point.setPosition(scaledLocation.getKey() - point.getWidth() / 2, scaledLocation.getValue() - point.getHeight() / 2);
                        point.render(renderer, alpha);
                    }
                    point.setColor(Color.BLACK);
                }
            }

            //________________END OUTER SUB-PROBLEM_____________________
            //________________INNER SUB-PROBLEM_________________________
            if (parentProblems.size() != 0) {
                l1 = new Pair((float) dataPacket.pointWidth + 25f, 0f);
                l2 = convertPointToScale(new Pair(2f * dataPacket.pointWidth + 50f, 0f));
                l3 = convertPointToScale(new Pair(2f * dataPacket.pointWidth + 50f, (float) dataPacket.pointHeight + 25f));
                l4 = convertPointToScale(new Pair((float) dataPacket.pointWidth + 25f, (float) dataPacket.pointHeight + 25f));
                renderer.drawLine(convertPointToScale(l1).getKey(), convertPointToScale(l1).getValue(), l2.getKey(), l2.getValue(), Math.max(1f * 100 / displayScale, 1), Color.BLACK);
                renderer.drawLine(convertPointToScale(l1).getKey(), convertPointToScale(l1).getValue(), l4.getKey(), l4.getValue(), Math.max(1f * 100 / displayScale, 1), Color.BLACK);
                renderer.drawLine(l2.getKey(), l2.getValue(), l3.getKey(), l3.getValue(), Math.max(1f * 100 / displayScale, 1), Color.BLACK);
                renderer.drawLine(l4.getKey(), l4.getValue(), l3.getKey(), l3.getValue(), Math.max(1f * 100 / displayScale, 1), Color.BLACK);

                for (Edge e : nrd.innerCactus.getEdges()) {
                    p1 = convertPointToScale(new Pair(e.a.a + l1.getKey(), e.a.b + l1.getValue()));
                    p2 = convertPointToScale(new Pair(e.b.a + l1.getKey(), e.b.b + l1.getValue()));
                    renderer.drawLine(p1.getKey(), p1.getValue(), p2.getKey(), p2.getValue(), Math.max(1f * 100 / displayScale, 1), Color.BLACK);
                }

                for (Point p : nrd.innerCactus.getPoints()) {
                    scaledLocation = convertPointToScale(new Pair(p.a + l1.getKey(), p.b + l1.getValue()));
                    point.setPosition(scaledLocation.getKey() - point.getWidth() / 2, scaledLocation.getValue() - point.getHeight() / 2);
                    point.render(renderer, alpha);
                }

                if(layerRingData != null) {
                    for (Point p : layerRingData.freePoints) {
                        scaledLocation = convertPointToScale(new Pair(p.a + l1.getKey(), p.b + l1.getValue()));
                        point.setPosition(scaledLocation.getKey() - point.getWidth() / 2, scaledLocation.getValue() - point.getHeight() / 2);
                        point.render(renderer, alpha);
                    }
                }
            }

            //________________END INNER SUB-PROBLEM_____________________

            drawKey(alpha);
            boundingBox.render(renderer, alpha);
            textBox.render(renderer, alpha);

            for (Button b : buttonList) {
                b.renderButton(renderer, alpha);
            }


            renderer.end();

            if(parentProblems.size()>0) {
                l4 = convertPointToScale(new Pair((float) dataPacket.pointWidth, 2f * dataPacket.pointHeight + 25f + 50));
                l3 = convertPointToScale(new Pair((float) dataPacket.pointWidth / 2f, 0f));
                l2 = convertPointToScale(new Pair((float) dataPacket.pointWidth / 2f * 3f, 0f));
                renderer.drawText("Selected Problem", l4.getKey() - 60, l4.getValue()+5, Color.BLACK);
                renderer.drawText("Outer Sub-Problems", l3.getKey() - 60, l3.getValue() - 20, Color.BLACK);
                if(layerRingData == null){
                    renderer.drawText("Layer Separator", l2.getKey() - 50, l2.getValue() - 20, Color.BLACK);
                }else {
                    renderer.drawText("Inner Sub-Problem", l2.getKey() - 50, l2.getValue() - 20, Color.BLACK);
                }
                texture = BlankTexture;
                texture.bind();
            }

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


    }

    private void drawKey(float alpha) {

        int width = 125;
        int height = 45;
        Color grey = new Color(.95f, .95f, .95f);
        new Box(grey, texture, gameWidth - width, gameHeight - height, height, width).render(renderer, alpha);
        new Box(Color.GREEN, texture, gameWidth - width + 5, gameHeight - height + 5, 15, 15).render(renderer, alpha);
        new Box(Color.BLACK, texture, gameWidth - width + 5, gameHeight - height + 25, 15, 15).render(renderer, alpha);
        renderer.end();
        renderer.drawText("Inner Layer", gameWidth - width + 25, gameHeight - height + 5, Color.BLACK);
        renderer.drawText("Outer Layer", gameWidth - width + 25, gameHeight - height + 25, Color.BLACK);
        renderer.begin();
    }

    private void drawText(int windowWidth) {
        windowWidth = windowWidth - 15;
        String inputText;
        if (!dataPacket.outOfTime) {
            if (parentProblems.size() > 0) {
                int sum = 0;
                for (NibbledRingData nrd : getOuterNibbled()) {
                    sum += nrd.getCount().get();
                }

                if (layerRingData != null) {


                    inputText = "The visualization on the right is of a general layer-unconstrained ring sub-problem and two sub-problems generated by the current selected layer separator. " +
                            "The Outer Sub-Problems show the set of nibbled ring sub-problems generated by the layer separator. " +
                            "The Inner Sub-Problem shows the general layer-unconstrained ring sub-problem generated by the layer separator." +
                            "The layer separator has layer-index: " + layerRingData.outerIndex + ". " +
                            "The number of triangulations that contain this layer separator is: " + sum * layerRingData.getCount().get() + ". " +
                            "This is the product of the triangulations of the outer layer(" + sum + ") and the triangulations of the inner layer (" + layerRingData.getCount().get() + "). ";
                } else {
                    inputText = "The visualization on the right is of a general layer-unconstrained ring sub-problem and two sub-problems generated by a layer separator. " +
                            "The Outer Sub-Problems show the set of nibbled ring sub-problems generated by the layer separator. " +
                            "The Layer Separator box shows the layer separator itself as no inner sub-problem exists." +
                            "The layer separator has layer-index: " + nrd.innerIndex + ". " +
                            "The number of triangulations that contain this layer separator is: " + sum + ". " +
                            "This is number of triangulations of the outer layer.";
                }
            } else {
                if (layerRingData.getCount().get() == 1) {
                    inputText = "This point set has 1 triangulation. ";

                } else {
                    inputText = "This point set has " + layerRingData.getCount().get() + " triangulations. ";
                }
            }
        } else {
            if (parentProblems.size() > 0) {
                inputText = "";
                if (layerRingData != null) {

                    inputText += "The visualization on the right is of a general layer-unconstrained ring sub-problem and two sub-problems generated by a layer separator. " +
                            "The Outer Sub-Problems show the set of nibbled ring sub-problems generated by the layer separator. " +
                            "The Inner Sub-Problem shows the general layer-unconstrained ring sub-problem generated by the layer separator. " +
                            "The layer separator has layer-index: " + layerRingData.outerIndex + ". ";
                } else {
                    inputText += "The visualization on the right is of a general layer-unconstrained ring sub-problem and two sub-problems generated by a layer separator. " +
                            "The Outer Sub-Problems show the set of nibbled ring sub-problems generated by the layer separator. " +
                            "The Layer Separator box shows the layer separator itself as no inner sub-problem exists." +
                            "The layer separator has layer-index: " + nrd.innerIndex + ". ";
                }
            } else {
                inputText = "The algorithm ran out of memory before it could count the triangulations for the point set. " +
                        "You can still explore what it did compute, however there will be missing sub-problems and no triangulation counts. ";
            }
        }

        inputText = resizeText(windowWidth, inputText);

        String descriptionText;
        if (parentProblems.size() == 0) {
            descriptionText = "Marx and Miltzow use two dynamic programming algorithms to count the triangulations of a point set. " +
                    "The visualization on the right is of a general layer-unconstrained ring sub-problem with triangulations equivalent to the original point set. ";


            if (!skip) {
                descriptionText += "Press \"Explore\" to view the sub-problems of this problem. ";
            } else if (getNibbled().size() > 0) {
                descriptionText += "This problem has no general layer-unconstrained ring sub-problems since there are less then 12 free points. " +
                        "Press \"Explore\" to view the nibbled ring sub-problems of this problem. ";
            }
            descriptionText += "Press \"Restart\" to input a different point set. ";
        } else {
            descriptionText = "Press \"Next\" to select the next layer separator. " +
                    "Press \"Explore\" to view the Inner Sub-Problem of this layer separator. " +
                    "Press \"Outer\" to view the Outer Sub-Problems generated by this layer separator. " +
                    "Press \"Parent\" to return to the parent of this problem. " +
                    "Press \"Restart\" to input a different point set. ";
        }
        if (dataPacket.outOfTime) {
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

    private String resizeText(int windowWidth, String inputText) {
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

        if (buttonList.size() == 0)
            createLoadingButtons();

        /* Set clear color to gray */
        glClearColor(1f, 1f, 1f, 1f);
    }

    private void createLoadingButtons() {
        buttonList.clear();
        String testButtonTooltip = "Stop the algorithm early and present what was computed. Can take a long time.";
        String buttonText = "Stop";
        LocationPacket locationNibbled = new LocationPacket(0, 0f, 0, 0);
        int textWidth = renderer.getTextWidth(buttonText);
        int textHeight = renderer.getTextHeight(buttonText);
        Button NibbledButton = new TextButton(locationNibbled, gameWidth, gameHeight, textWidth, textHeight, buttonText, testButtonTooltip) {
            @Override
            protected void onPress() {
                if (loading.availablePermits() == 1) {
                    try {
                        loading.acquire();
                    } catch (InterruptedException e) {
                        System.out.println(e);
                    }
                }
            }

        };

        buttonList.add(NibbledButton);
    }

    private void createButtons() {

        buttonList.clear();
        String testButtonTooltip = "Switches to next general unconstrained-layer sub-problem";
        String buttonText = "Next";
        LocationPacket locationNibbled = new LocationPacket(1f / 3f, 1f, 0, 0);
        int textWidth = renderer.getTextWidth(buttonText);
        int textHeight = renderer.getTextHeight(buttonText);
        Button NibbledButton = new TextButton(locationNibbled, gameWidth, gameHeight, textWidth, textHeight, buttonText, testButtonTooltip) {
            @Override
            protected void onPress() {
                try {
                    if (parentProblems.size() != 0) {
                        subproblemIndex++;
                        subproblemIndex = subproblemIndex % parentProblems.get(parentProblems.size() - 1).getCount().getSubproblemIDs(dataPacket).getValue().size();
                        while (parentProblems.get(parentProblems.size() - 1).getCount().getSubproblemIDs(dataPacket).getValue().get(subproblemIndex).size() < 1 && parentProblems.get(parentProblems.size() - 1).getCount().getSubproblemIDs(dataPacket).getKey().get(subproblemIndex).size() < 1) {
                            subproblemIndex++;
                            subproblemIndex = subproblemIndex % parentProblems.get(parentProblems.size() - 1).getCount().getSubproblemIDs(dataPacket).getValue().size();
                        }
                        if (parentProblems.get(parentProblems.size() - 1).getCount().getSubproblemIDs(dataPacket).getValue().get(subproblemIndex).size() < 1) {
                            layerRingData = null;
                            String s1 = parentProblems.get(parentProblems.size() - 1).getCount().getSubproblemIDs(dataPacket).getKey().get(subproblemIndex).get(0);
                            String s2 = parentProblems.get(parentProblems.size() - 1).getCount().getSubproblemIDs(dataPacket).getKey().get(subproblemIndex).get(1);
                            nrd = dataPacket.NibbledRingDataBase.get(s1).get(s2);
                        } else {
                            layerRingData = dataPacket.LayerRingDataBase.get(parentProblems.get(parentProblems.size() - 1).getCount().getSubproblemIDs(dataPacket).getValue().get(subproblemIndex).get(0));
                            String s1 = parentProblems.get(parentProblems.size() - 1).getCount().getSubproblemIDs(dataPacket).getKey().get(subproblemIndex).get(0);
                            String s2 = parentProblems.get(parentProblems.size() - 1).getCount().getSubproblemIDs(dataPacket).getKey().get(subproblemIndex).get(1);
                            nrd = dataPacket.NibbledRingDataBase.get(s1).get(s2);
                        }
                    }
                } catch (NullPointerException e) {
                }
            }

        };

        buttonList.add(NibbledButton);

        testButtonTooltip = "Explore the outer ring sub-problems";
        buttonText = "Outer";
        LocationPacket locationRight = new LocationPacket(1f / 3f, 1f, NibbledButton.getWidth() + 2, 0);
        textWidth = renderer.getTextWidth(buttonText);
        textHeight = renderer.getTextHeight(buttonText);
        Button RightNibbledButton = new TextButton(locationRight, gameWidth, gameHeight, textWidth, textHeight, buttonText, testButtonTooltip) {
            @Override
            protected void onPress() {
                if (parentProblems.size() > 0 && getOuterNibbled().size() > 0)
                    Game.stateMachine.change("OuterNibbledRingState", window, renderer, dataPacket);
            }

        };
        buttonList.add(RightNibbledButton);

        testButtonTooltip = "Explore the inner ring sub-problem";
        buttonText = "Explore";
        LocationPacket locationExplore = new LocationPacket(1f / 3f, 1f, RightNibbledButton.getWidth() + 2, 0);
        textWidth = renderer.getTextWidth(buttonText);
        textHeight = renderer.getTextHeight(buttonText);
        Button ExploreNibbledButton = new TextButton(locationExplore, gameWidth, gameHeight, textWidth, textHeight, buttonText, testButtonTooltip) {
            @Override
            protected void onPress() {

                try {
                    if (skip) {
                        if (getNibbled().size() > 0) {
                            Game.stateMachine.change("NibbledRingState", window, renderer, dataPacket);
                        }
                    } else if (layerRingData == null) {
                        //do nothing
                    } else if (layerRingData.getCount().getSubproblemIDs(dataPacket).getValue().size() > 0) {
                        parentProblems.add(layerRingData);
                        parentProblemIndex.add(new Integer(subproblemIndex));
                        subproblemIndex = 0;
                        while (parentProblems.get(parentProblems.size() - 1).getCount().getSubproblemIDs(dataPacket).getValue().get(subproblemIndex).size() < 1) {
                            subproblemIndex++;
                            if (subproblemIndex >= parentProblems.get(parentProblems.size() - 1).getCount().getSubproblemIDs(dataPacket).getValue().size()) {
                                subproblemIndex = 0;
                                Game.stateMachine.change("NibbledRingState", window, renderer, dataPacket);
                                return;
                            }
                        }
                        layerRingData = dataPacket.LayerRingDataBase.get(parentProblems.get(parentProblems.size() - 1).getCount().getSubproblemIDs(dataPacket).getValue().get(subproblemIndex).get(0));
                        String s1 = parentProblems.get(parentProblems.size() - 1).getCount().getSubproblemIDs(dataPacket).getKey().get(subproblemIndex).get(0);
                        String s2 = parentProblems.get(parentProblems.size() - 1).getCount().getSubproblemIDs(dataPacket).getKey().get(subproblemIndex).get(1);
                        nrd = dataPacket.NibbledRingDataBase.get(s1).get(s2);
                        displayScale = 250;
                        center = new Point(50,30);
                        lastCenter = new Point(center.a, center.b);
                    } else if (layerRingData.getCount().getSubproblemIDs(dataPacket).getKey().size() > 0) {
                        Game.stateMachine.change("NibbledRingState", window, renderer, dataPacket);
                    }
                } catch (NullPointerException e) {
                }
            }

        };
        buttonList.add(ExploreNibbledButton);

        testButtonTooltip = "Returns to the parent problem";
        buttonText = "Parent";
        LocationPacket locationParent = new LocationPacket(1f / 3f, 1f, ExploreNibbledButton.getWidth() + 2, 0);
        textWidth = renderer.getTextWidth(buttonText);
        textHeight = renderer.getTextHeight(buttonText);
        Button ParentNibbledButton = new TextButton(locationParent, gameWidth, gameHeight, textWidth, textHeight, buttonText, testButtonTooltip) {
            @Override
            protected void onPress() {
                try {
                    if (parentProblems.size() > 0) {
                        subproblemIndex = parentProblemIndex.remove(parentProblemIndex.size() - 1);
                        layerRingData = parentProblems.remove(parentProblems.size() - 1);
                        if (parentProblems.size() > 0) {
                            String s1 = parentProblems.get(parentProblems.size() - 1).getCount().getSubproblemIDs(dataPacket).getKey().get(subproblemIndex).get(0);
                            String s2 = parentProblems.get(parentProblems.size() - 1).getCount().getSubproblemIDs(dataPacket).getKey().get(subproblemIndex).get(1);
                            nrd = dataPacket.NibbledRingDataBase.get(s1).get(s2);
                        } else {
                            nrd = null;
                        }
                    }
                } catch (NullPointerException e) {
                }
            }

        };
        buttonList.add(ParentNibbledButton);

        testButtonTooltip = "Return to the start and input a new point set";
        buttonText = "Restart";
        LocationPacket locationRestart = new LocationPacket(1f, 0f, 0, 0);
        textWidth = renderer.getTextWidth(buttonText);
        textHeight = renderer.getTextHeight(buttonText);
        Button restartButton = new TextButton(locationRestart, gameWidth, gameHeight, textWidth, textHeight, buttonText, testButtonTooltip) {
            @Override
            protected void onPress() {
                Game.stateMachine.change("Restart", window, renderer, null);
            }

        };
        buttonList.add(restartButton);

        if (dataPacket.outOfTime) {
            testButtonTooltip = "Reattempt to count triangulations of the point set with no memory limit (may cause errors or crash).";
            buttonText = "Solve";
            LocationPacket locationSolve = new LocationPacket(0f, 0f, 0, 0);
            textWidth = renderer.getTextWidth(buttonText);
            textHeight = renderer.getTextHeight(buttonText);
            Button SolveButton = new TextButton(locationSolve, gameWidth, gameHeight, textWidth, textHeight, buttonText, testButtonTooltip) {
                @Override
                protected void onPress() {
                    memlimit = false;
                    if (loading.availablePermits() == 0) {
                        loading.release();
                    }
                    createLoadingButtons();
                    solve();
                }

            };
            buttonList.add(SolveButton);
        }
    }

    @Override
    public void exit() {
        super.exit();
        texture.delete();
        if (algorithmSolver.isAlive() && loading.availablePermits() == 1) {
            try {
                loading.acquire();
                algorithmSolver.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void onWindowResize() {

        super.onWindowResize();
        texture = Texture.loadTexture("resources/BlankTexture.png");
        Color black = new Color(0, 0, 0);
        boundingBox = new Box(black, texture, 0, 0, gameHeight, gameWidth / 3);
        Color white = new Color(.9f, .9f, .9f);
        textBox = new Box(white, texture, 2, 2, gameHeight - 4, gameWidth / 3 - 4);
    }

    public ArrayList<NibbledRingData> getNibbled() {
        return layerRingData.getCount().getSubproblems(dataPacket).getKey();
    }

    public ArrayList<NibbledRingData> getOuterNibbled() {
        ArrayList<NibbledRingData> nrd = new ArrayList<>();
        ArrayList<String> strings = parentProblems.get(parentProblems.size() - 1).getCount().getSubproblemIDs(dataPacket).getKey().get(subproblemIndex);
        for (int i = 0; i < strings.size() - 1; i += 2) {
            nrd.add(dataPacket.NibbledRingDataBase.get(strings.get(i)).get(strings.get(i + 1)));
        }

        return nrd;
    }
}

class LoadingDisplay extends Thread {
    Semaphore shouldStop;
    CactusData dataPacket;
    boolean memlimit;

    LoadingDisplay(Semaphore shouldStop, CactusData dataPacket, boolean memlimit) {
        this.shouldStop = shouldStop;
        this.dataPacket = dataPacket;
        this.memlimit = memlimit;
    }

    public void run() {
        dataPacket.outOfTime = false;
        boolean done = false;
        ArrayList<String> incompNibbled = new ArrayList<>();
        ArrayList<String> incompLayer = new ArrayList<>();
        while (!done && shouldStop.availablePermits() == 1) {
            incompNibbled.clear();
            incompLayer.clear();
            for (String key1 : dataPacket.NibbledRingDataBase.keySet()) {
                for (String key2 : dataPacket.NibbledRingDataBase.get(key1).keySet()) {

                    if (dataPacket.NibbledRingDataBase.get(key1).get(key2).getCount() == null) {
                        incompNibbled.add(key1);
                        incompNibbled.add(key2);
                    }
                }
            }

            for (String key : dataPacket.LayerRingDataBase.keySet()) {
                if (dataPacket.LayerRingDataBase.get(key).getCount() == null) {
                    incompLayer.add(key);
                }
            }

            if (incompNibbled.size() == 0 && incompLayer.size() == 0) {
                done = true;

            } else {
                for (int i = 0; i < incompNibbled.size(); i += 2) {
                    NibbledRingSolver.nibbledRingSubproblem(dataPacket, dataPacket.NibbledRingDataBase.get(incompNibbled.get(i)).get(incompNibbled.get(i + 1)));
                    if ((memlimit && outOfMem()) || shouldStop.availablePermits() == 0) {
                        done = true;
                        dataPacket.outOfTime = true;
                        break;
                    }
                }
                for (String key : incompLayer) {
                    LayerRingSolver.peeling(dataPacket, dataPacket.LayerRingDataBase.get(key));
                    if ((memlimit && outOfMem()) || shouldStop.availablePermits() == 0) {
                        done = true;
                        dataPacket.outOfTime = true;
                        break;
                    }
                }

            }
        }

        done = false;
        while ((!memlimit || !dataPacket.outOfTime) && !done && shouldStop.availablePermits() == 1) {
            done = true;

            for (String key1 : dataPacket.NibbledRingDataBase.keySet()) {
                for (String key2 : dataPacket.NibbledRingDataBase.get(key1).keySet()) {

                    if (!dataPacket.NibbledRingDataBase.get(key1).get(key2).getCount().isPresent()) {
                        dataPacket.NibbledRingDataBase.get(key1).get(key2).getCount().getValue(dataPacket);
                        done = false;
                        if ((memlimit && outOfMem()) || shouldStop.availablePermits() == 0) {
                            done = true;
                            dataPacket.outOfTime = true;
                            break;
                        }
                    }

                }
            }

            for (String key : dataPacket.LayerRingDataBase.keySet()) {
                if (!dataPacket.LayerRingDataBase.get(key).getCount().isPresent()) {
                    dataPacket.LayerRingDataBase.get(key).getCount().getValue(dataPacket);
                    done = false;
                    if ((memlimit && outOfMem()) || shouldStop.availablePermits() == 0) {
                        done = true;
                        dataPacket.outOfTime = true;
                        break;
                    }
                }
            }

        }
        try {
            if (shouldStop.availablePermits() == 1) {
                shouldStop.acquire();
            } else {
                dataPacket.outOfTime = true;
            }
        } catch (InterruptedException e) {
            System.out.println("interrupted: " + e);
            dataPacket.outOfTime = true;
        }
    }

    private boolean outOfMem() {
        long used = 0;
        long max = 0;
        for (MemoryPoolMXBean mpBean : ManagementFactory.getMemoryPoolMXBeans()) {
            if (mpBean.getType() == MemoryType.HEAP) {
                used += mpBean.getUsage().getUsed();
                max += mpBean.getUsage().getMax();
            }
        }
        if ((100 * used) / max > 88) {
            return true;
        }
        return false;
    }
}




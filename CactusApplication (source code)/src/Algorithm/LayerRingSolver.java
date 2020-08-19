package Algorithm;

import DataTypes.*;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * a toolbox for solving general layer-unconstrained ring sub-problems.
 * this class is never instantiated.
 *
 * @author Jelle Schukken
 */
public class LayerRingSolver {

    /**
     * solves a general layer-unconstrained ring sub-problem by computing and assigning it the appropriate sumproduct
     * @param cactusData the main datapacket containing the dynamic programming databases
     * @param data the data of the specific general layer-unconstrained ring sub-problem to solve
     */
    public static void peeling(CactusData cactusData, LayerRingData data) {
        SumProduct sumProduct;
        if (cactusData.LayerRingDataBase.get(data.getStringID()).getCount() != null) {//case where problem is already solved
            return;
        } else if (data.outerLayer.size() == 3 && data.freePoints.size() == 0) {//base case
            sumProduct = new LayerRingSumProduct(new ArrayList<>(), new ArrayList<>());
            sumProduct.setValue(1);
            data.setSumProdct(sumProduct);
            return;
        } else if (data.getTotalPoints() - data.outerLayer.size() <= Math.sqrt(data.getTotalPoints())) {//case where max width is less then sqrt(n)

            //define components of nibbled ring problem
            Edge baseEdge = new Edge(data.outerLayer.get(0), data.outerLayer.get(data.outerLayer.size() - 1));
            ArrayList<Point> b1 = new ArrayList<>();
            ArrayList<Point> b2 = new ArrayList<>();
            b1.add(data.outerLayer.get(0));
            b2.add(data.outerLayer.get(data.outerLayer.size() - 1));

            //compute the set of possible constraints and generate a nibbled ring sub-problem for each constraint vector
            ArrayList<NibbledRingData> constraintSet = new ArrayList<>();
            ArrayList<ConstraintVector<Integer>> C = getNibbledConstraints(data);
            if(C.size()>0) {
                for (ConstraintVector<Integer> c : C) {
                    NibbledRingData thinRing =
                            new NibbledRingData(
                                    data.outerLayer,
                                    baseEdge,
                                    b1,
                                    b2,
                                    new CactusLayer(),
                                    data.freePoints,
                                    c,
                                    new CactusPolygon(data.outerLayer),
                                    0,
                                    data.getTotalPoints() - 1,
                                    data.getTotalPoints() - 0 + 1);
                    constraintSet.add(thinRing);
                }

                //add sub-problems to database
                HashMap<String, NibbledRingData> constraintMap;
                constraintMap = cactusData.NibbledRingDataBase.get(constraintSet.get(0).getPointSetID());

                if (constraintMap != null) {
                    for (NibbledRingData nibbledData : constraintSet) {
                        if (!constraintMap.containsKey(nibbledData.getConstraintID())) {
                            constraintMap.put(nibbledData.getConstraintID(), nibbledData);
                        }
                    }

                } else {
                    constraintMap = new HashMap<>();
                    for (NibbledRingData nibbledData : constraintSet) {
                        constraintMap.put(nibbledData.getConstraintID(), nibbledData);
                    }
                    cactusData.NibbledRingDataBase.put(constraintSet.get(0).getPointSetID(), constraintMap);
                }

                ArrayList<String> constraintSetString = new ArrayList<>();
                for (NibbledRingData nibbledData : constraintSet) {
                    constraintSetString.add(nibbledData.getPointSetID());
                    constraintSetString.add(nibbledData.getConstraintID());
                }

                sumProduct = new NibbledSetSumProduct(constraintSetString);
                data.setSumProdct(sumProduct);
                return;
            }
            sumProduct = new NibbledSetSumProduct(new ArrayList<>());
            data.setSumProdct(sumProduct);
            return;

        } else {//case where width could be greater then sqrt(n)
            ArrayList<ArrayList<String>> s1 = new ArrayList<>();
            ArrayList<ArrayList<String>> s2 = new ArrayList<>();

            //compute all possible layer separators
            ArrayList<Pair<Integer, CactusLayer>> L = computeLayers(data);

            //compute, for each layer separator, the resulting sub-problems
            for (Pair<Integer, CactusLayer> l : L) {
                ArrayList<String> s1Components = new ArrayList<>();

                Edge baseEdge = new Edge(data.outerLayer.get(0), data.outerLayer.get(data.outerLayer.size() - 1));
                ArrayList<Point> b1 = new ArrayList<>();
                ArrayList<Point> b2 = new ArrayList<>();
                b1.add(data.outerLayer.get(0));
                b2.add(data.outerLayer.get(data.outerLayer.size() - 1));
                ArrayList<Point> newFreePoints = new ArrayList<>();
                ArrayList<CactusPolygon> innerComponents = new ArrayList<>();
                CactusPolygon poly;

                //compute general layer-unconstrained ring sub-problems of inner components, if any
                for (ArrayList<Point> component : l.getValue().getInnerComponents()) {



                    double sum  = 0;
                    for(int k = 0; k < component.size(); k++){
                        double angle = Edge.getAngle(new Edge(component.get(k), component.get((k+1)%component.size())),
                                new Edge(component.get((k+1)%component.size()), component.get((k+2)%component.size())));
                        sum += angle;

                    }
                    if(Math.abs(sum -(component.size()-2)*Math.PI) > 0.001){
                        Collections.reverse(component);//ensure that the component is ordered correctly for the outer layer
                    }

                    poly = new CactusPolygon(component);
                    innerComponents.add(poly);

                    ArrayList<Point> newFreePointsInner = new ArrayList<>();
                    for (Point p : data.freePoints) {
                        if (poly.contains(p) && !component.contains(p)) {
                            newFreePointsInner.add(p);
                        }
                    }

                    LayerRingData thickRing = new LayerRingData(component, newFreePointsInner,l.getKey());
                    if (!cactusData.LayerRingDataBase.containsKey(thickRing.getStringID())) {
                        cactusData.LayerRingDataBase.put(thickRing.getStringID(), thickRing);
                    }
                    s1Components.add(thickRing.getStringID());
                }



                boolean inside;
                for (Point p : data.freePoints) {
                    if (l.getValue().getPoints().contains(p)) {
                    } else {
                        inside = true;
                        for (CactusPolygon cp : innerComponents) {
                            if (cp.contains(p)) {
                                inside = false;
                                break;
                            }
                        }
                        if (inside) {
                            newFreePoints.add(p);
                        }
                    }

                }

                //sanity check is there are enough points for the required nibbled ring sun-problem
                if((newFreePoints.size() >= (l.getKey() - data.outerIndex -1) * (1+(int)(data.freePoints.size() / Math.sqrt(data.getTotalPoints())))
                        && !((l.getKey()-1) == 0 && newFreePoints.size() > 0))) {

                    s1.add(s1Components);

                    HashSet<Point> numPoints = new HashSet<>();
                    numPoints.addAll(data.outerLayer);
                    numPoints.addAll(newFreePoints);
                    numPoints.addAll(l.getValue().getPoints());
                    ArrayList<NibbledRingData> constraintSet = new ArrayList<>();
                    //compute constraints for outer nibbled ring problem
                    ArrayList<ConstraintVector<Integer>> C = getPossibleVectors(data, l, numPoints.size());
                    numPoints.clear();
                    //for each constraint define a nibbled ring problem and add it to the database
                    if (C.size() > 0) {
                        for (ConstraintVector<Integer> c : C) {
                            NibbledRingData thinRing = new NibbledRingData(data.outerLayer, baseEdge, b1, b2, l.getValue(), newFreePoints, c, new CactusPolygon(data.outerLayer), 0, l.getKey(), l.getKey() + 1);
                            constraintSet.add(thinRing);
                        }
                        HashMap<String, NibbledRingData> constraintMap;
                        constraintMap = cactusData.NibbledRingDataBase.get(constraintSet.get(0).getPointSetID());

                        if (constraintMap != null) {
                            for (NibbledRingData nibbledData : constraintSet) {
                                if (!constraintMap.containsKey(nibbledData.getConstraintID())) {
                                    constraintMap.put(nibbledData.getConstraintID(), nibbledData);
                                }
                            }

                        } else {
                            constraintMap = new HashMap<>();
                            for (NibbledRingData nibbledData : constraintSet) {
                                constraintMap.put(nibbledData.getConstraintID(), nibbledData);
                            }
                            cactusData.NibbledRingDataBase.put(constraintSet.get(0).getPointSetID(), constraintMap);
                        }

                        ArrayList<String> constraintSetString = new ArrayList<>();
                        for (NibbledRingData nibbledData : constraintSet) {
                            constraintSetString.add(nibbledData.getPointSetID());
                            constraintSetString.add(nibbledData.getConstraintID());
                        }
                        s2.add(constraintSetString);


                    } else
                        s2.add(new ArrayList<>());
                }

            }
            sumProduct = new LayerRingSumProduct(s1, s2);
            data.setSumProdct(sumProduct);
            return;
        }
    }

    /**
     * computes all peripheral layers for a general layer-unconstrained ring sub-problem
     *
     * @param data the general layer-unconstrained ring sub-problem
     * @return a arraylist of all peripheral layers
     */
    public static ArrayList<Pair<Integer, CactusLayer>> computeLayers(LayerRingData data) {

        ArrayList<Pair<Integer, CactusLayer>> layers = new ArrayList<>();
        ArrayList<CactusLayer> L = new ArrayList<>();
        L.add(new CactusLayer(new ArrayList<>(), new ArrayList<>())); //add empty layer

        computeCactusLayers(data, L, new ArrayList<Edge>(), 0, getAllPossibleEdges(data));
        for (CactusLayer c : L) {

            for (int i = 1; i <= Math.sqrt(data.freePoints.size())+2 && i < data.getTotalPoints(); i++) {
                Pair<Integer, CactusLayer> p = new Pair(i, c);
                layers.add(p);
            }
        }

        return layers;
    }

    /**
     * recursively computes every possible peripheral layer for a given layer ring problem.
     * layers consist of only free points
     *
     * @param data   The general layer-unconstrained ring sub-problem
     * @param L      The set of layers that have been found. New layers are added to this.
     * @param cactus The current layer
     * @param i      The index of the next edge to add to the current later
     * @param edges  The set of all possible edges between free points of the problem
     */
    private static void computeCactusLayers(LayerRingData data, ArrayList<CactusLayer> L, ArrayList<Edge> cactus, int i, ArrayList<Edge> edges) {
        if(i>=edges.size()){
            computeCactusLayersPoints(data,L,cactus,0,new ArrayList<Point>());
        }else {
            while (crosses(edges.get(i), cactus)) {
                i++;
                if (i >= edges.size()) {
                    computeCactusLayersPoints(data,L,cactus,0,new ArrayList<Point>());
                    return;
                }
            }
            ArrayList<Edge> newCactus = new ArrayList<>();
            for (Edge e : cactus) {
                newCactus.add(e);
            }
            newCactus.add(edges.get(i));
            CactusLayer newLayer = new CactusLayer(newCactus, new ArrayList<Point>());
            if (newLayer.isValid() && getPoints(newCactus).size() <= (int)(data.freePoints.size() / Math.sqrt(data.getTotalPoints()))) {
                    L.add(newLayer);
                computeCactusLayers(data, L, newCactus, i + 1, edges);
            }
            computeCactusLayers(data, L, cactus, i + 1, edges);
        }
    }


    /**
     * recursively computes every possible peripheral layer for a given layer ring problem.
     * layers consist of only free points
     *
     * @param data   The layer ring problem
     * @param L      The set of layers that have been found. New layers are added to this.
     * @param cactus The current layer
     * @param i      The index of the next edge to add to the current later
     * @param points  The set of all possible edges between free points of the problem
     */
    private static void computeCactusLayersPoints(LayerRingData data, ArrayList<CactusLayer> L, ArrayList<Edge> cactus, int i, ArrayList<Point> points) {
        if (getPoints(cactus).size() + points.size()  == (int)(data.freePoints.size() / Math.sqrt(data.getTotalPoints()))) {
            return;
        }
        if(i>= data.freePoints.size()){
            return;
        }else {
            while (getPoints(cactus).contains(data.freePoints.get(i))) {
                i++;
                if (i >= data.freePoints.size()) {
                    return;
                }
            }
            ArrayList<Point> newCactusPoints = new ArrayList<>();
            for(Point p: points){
                newCactusPoints.add(p);
            }

            newCactusPoints.add(data.freePoints.get(i));
            CactusLayer newLayer = new CactusLayer(cactus, newCactusPoints);
            if (newLayer.isValid()) {
                L.add(newLayer);
                computeCactusLayersPoints(data, L, cactus, i + 1, newCactusPoints);
            }
            computeCactusLayersPoints(data, L, cactus, i + 1, points);
        }
    }

    /**
     * checks if an edge crosses any edge in a list of edges
     * @param e1 the edge
     * @param edges the list of edges
     * @return true of e1 intersects and edge in edges
     */
    private static boolean crosses(Edge e1, ArrayList<Edge> edges) {
        for (Edge e2 : edges) {
            if (e1.intersects(e2)) {
                return true;
            }
        }

        return false;
    }

    /**
     * gets all possible edges between points in a general layer-unconstrained ring sub-problem
     * @param data the general layer-unconstrained ring sub-problem
     * @return a list of all edges between points in data
     */
    private static ArrayList<Edge> getAllPossibleEdges(LayerRingData data) {
        ArrayList<Edge> edges = new ArrayList<>();
        ArrayList<Point> points = new ArrayList<>();
        points.addAll(data.freePoints);
        for (int i = 0; i < points.size(); i++) {

            for (int j = i + 1; j < points.size(); j++) {
                Edge newEdge = new Edge(points.get(i), points.get(j));
                edges.add(newEdge);
            }
        }

        return edges;
    }


    /**
     * returns the set of all possible constraint vectors of the nibbled ring problem for a given layer-index pair
     *
     * @param data      the problem for which to generate constraints
     * @param layerPair the pair for which to generate constraints
     * @return an arraylist of every possible constraint vector
     */
    private static ArrayList<ConstraintVector<Integer>> getPossibleVectors(LayerRingData data, Pair<Integer, CactusLayer> layerPair, int numPoints) {
        ArrayList<ConstraintVector<Integer>> c1List = new ArrayList<>();
        ConstraintVector<Integer> initialVector = new ConstraintVector<>();
        ConstraintVector<Integer> workVector = new ConstraintVector<>();
        int minSize = (int)(data.freePoints.size() / Math.sqrt(data.getTotalPoints()))+1;
        int maxSize = data.getTotalPoints();

        int sum = 0;
        for (int i = 0; i < data.getTotalPoints(); i++) {
            if (i > 0 && i < layerPair.getKey()) {
                sum += minSize;
                initialVector.add(minSize);
                workVector.add(minSize);
            } else {
                initialVector.add(0);
                workVector.add(0);
            }
        }
        initialVector.set(0, data.outerLayer.size());
        sum += data.outerLayer.size();
        workVector.set(0, data.outerLayer.size());
        workVector.set(layerPair.getKey(), layerPair.getValue().getPoints().size());
        initialVector.set(layerPair.getKey(), layerPair.getValue().getPoints().size());
        sum += layerPair.getValue().getPoints().size();

        if (sum == numPoints) {
            c1List.add(initialVector);
        } else {

            for (int i = layerPair.getKey() - 1; i < layerPair.getKey(); ) {
                if (i == 0) {
                    return c1List;
                } else {
                    workVector.set(i, (workVector.get(i) + 1) % (maxSize + 1));
                    if (workVector.get(i) == 0) {
                        workVector.set(i, minSize);
                        i--;
                    } else {
                        if (i + 1 != layerPair.getKey()) {
                            i++;
                        }
                        ConstraintVector<Integer> c1 = new ConstraintVector<>();
                        sum = 0;
                        boolean foundNum = false;
                        boolean foundlast = false;
                        boolean possible = true;
                        for (Integer n : workVector) {
                            if (!foundNum && n > 0) { //first non empty layer
                                foundNum = true;
                            } else if (foundNum && !foundlast && n < 0) { // first empty layer after a non empty layer
                                foundlast = true;
                            } else if (foundlast && n > 0) { // must be a zero size layer in the middle of non zero layers
                                possible = false;
                            }
                            c1.add(new Integer(n));
                            if(c1.size() > 1){
                                if(foundNum && c1.get(c1.size()-2) < 3 && n > 0){
                                    possible = false;
                                }
                            }
                            sum += n;
                        }
                        if (possible && sum == numPoints)
                            c1List.add(c1);
                    }

                }
            }
        }


        return c1List;
    }


    /**
     * get the constraints for the nibbled ring problem corresponding to a general layer-unconstrained ring sub-problem
     * @param data the general layer-unconstrained ring sub-problem
     * @return a list of constraint for a nibbled ring problem corresponding to data
     */
    private static ArrayList<ConstraintVector<Integer>> getNibbledConstraints(LayerRingData data) {
        ArrayList<ConstraintVector<Integer>> c1List = new ArrayList<>();
        ConstraintVector<Integer> initialVector = new ConstraintVector<>();
        ConstraintVector<Integer> workVector = new ConstraintVector<>();
        int maxSize = data.getTotalPoints();


        int sum = 0;
        for (int i = 0; i < data.getTotalPoints(); i++) {
            if (i == 0) {
                sum+=data.outerLayer.size();
                initialVector.add(data.outerLayer.size());
                workVector.add(data.outerLayer.size());
            } else {
                initialVector.add(0);
                workVector.add(0);
            }
        }
        if(sum == data.getTotalPoints())
            c1List.add(initialVector);

        for (int i = data.getTotalPoints() - 1; i < data.getTotalPoints(); ) {
            if (i <= 0) {
                return c1List;
            } else {
                workVector.set(i, (workVector.get(i) + 1) % (maxSize + 1));
                if (workVector.get(i) == 0) {
                    i--;
                } else {
                    if (i + 1 != data.getTotalPoints()) {
                        i++;
                    }
                    ConstraintVector<Integer> c1 = new ConstraintVector<>();
                    sum = 0;
                    boolean foundNum = false;
                    boolean foundZero = false;
                    boolean possible = true;
                    for (Integer n : workVector) {
                        if (!foundNum && n > 0) { //first non empty layer
                            foundNum = true;
                        } else if (foundNum && !foundZero && n == 0) { // first empty layer after a non empty layer
                            foundZero = true;
                        } else if (foundZero && n > 0) { // must be a zero size layer in the middle of non zero layers
                            possible = false;
                        }

                        c1.add(new Integer(n));
                        if(c1.size() > 1){
                            if(foundNum && c1.get(c1.size()-2) < 3 && n > 0){
                                possible = false;
                            }
                        }
                        sum += n;
                    }
                    if (possible && sum == data.getTotalPoints())
                        c1List.add(c1);
                }

            }
        }

        return c1List;
    }

    /**
     * get the set of points used by edges in a list
     * @param cactus the list of edges
     * @return the points of the edges in cactus
     */
    private static ArrayList<Point> getPoints(ArrayList<Edge> cactus){
        ArrayList<Point> points = new ArrayList<>();
        for(Edge e: cactus){
            if(!points.contains(e.a))
                points.add(e.a);
            if(!points.contains(e.b))
                points.add(e.b);
        }
        return points;
    }

}

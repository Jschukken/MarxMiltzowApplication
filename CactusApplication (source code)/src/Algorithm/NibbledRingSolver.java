package Algorithm;

import DataTypes.*;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/**
 * a toolbox for solving nibbled ring sub-problems.
 * this class is never instantiated.
 *
 * @author Jelle Schukken
 */
public class NibbledRingSolver {

    /**
     * solves a nibbled ring sub-problem by computing and assigning it the appropriate sumproduct
     * @param cactusData the main datapacket containing the dynamic programming databases
     * @param data the data of the specific nibbled ring sub-problem to solve
     */
    public static void nibbledRingSubproblem(CactusData cactusData, NibbledRingData data) {

        if (cactusData.NibbledRingDataBase.containsKey(data.getPointSetID())
                && cactusData.NibbledRingDataBase.get(data.getPointSetID()).containsKey(data.constraints)
                && cactusData.NibbledRingDataBase.get(data.getPointSetID()).get(data.constraints).getCount() != null) {
            return;
        }
        if (baseCaseTriangle(data)) {
            data.setSumProduct(new NibbledRingSumProduct(1));
            return;
        } else if (data.getPoints().size() <= 2) {
            data.setSumProduct(new NibbledRingSumProduct(0));
            return;
        } else {
            NibbledRingData rightSide;
            NibbledRingData leftSide;
            NibbledRingData rightSidec;
            NibbledRingData leftSidec;
            ArrayList<String> sumProduct = new ArrayList<>();
            for (ArrayList<Point> path : computeSeperators(data)) {


                //generate sub-problems independent of constraints
                rightSide = splitProblemOnPathRight(path, data);
                leftSide = splitProblemOnPathLeft(path, data);

                //set proper indexes of each point
                ArrayList<Point> innerLayer = rightSide.innerCactus.getPoints();
                Point.setIndexes(rightSide.outerLayer, rightSide.outerIndex);
                Point.setIndexes(innerLayer, rightSide.innerIndex);
                Point.clearIndexes(rightSide.freePoints);
                rightSide.b1.get(rightSide.b1.size() - 1).index = rightSide.outerIndex;
                for (int i = rightSide.b1.size() - 2; i >= 0; i--) {
                    int index = rightSide.b1.get(i + 1).index + 1;
                    if (true) {
                        rightSide.b1.get(i).index = index;
                    }
                }

                //compute shared path in order to compute cp
                ArrayList<Point> sharedPath = new ArrayList<>();
                for (int i = 0; i < Math.min(rightSide.b1.size(), leftSide.b2.size()); i++) {
                    if (rightSide.b1.get(i).equals(leftSide.b2.get(i))) {
                        sharedPath.add(rightSide.b1.get(i));
                    }
                }

                ConstraintVector<Integer> newConstraint = new ConstraintVector<>();

                //check for rare case where a point is removed from the problem in creating sub-problems
                // and adjust constraints respectively
                HashSet<Point> allPoints = new HashSet<>();
                allPoints.addAll(rightSide.innerCactus.getPoints());
                allPoints.addAll(leftSide.innerCactus.getPoints());
                for (int i = 0; i < data.constraints.size(); i++) {
                    if (i == data.innerIndex &&  allPoints.size() != data.innerCactus.getPoints().size()) {
                        newConstraint.add(Math.max(0,(data.constraints.get(i) - 1)));
                    } else {
                        newConstraint.add(data.constraints.get(i));
                    }
                }


                //compute all possible constraints of sub-problems and compute sum-produce based on these
                ConstraintVector<Integer> cp = ConstraintVector.convertToIndicatorVector(cactusData, sharedPath);
                for (ConstraintVector<Integer> c1 : ConstraintVector.getAllC1(newConstraint, cp)) {

                    ConstraintVector<Integer> c2 = ConstraintVector.getC2(c1, newConstraint, cp);
                    if(!(c1.get(rightSide.innerIndex) != rightSide.innerCactus.getPoints().size() ||
                            c1.get(rightSide.outerIndex) != rightSide.outerLayer.size() ||
                            c2.get(leftSide.innerIndex) != leftSide.innerCactus.getPoints().size() ||
                            c2.get(leftSide.outerIndex) != leftSide.outerLayer.size() ||
                            sum(c1) != rightSide.getPoints().size() ||
                            sum(c2) != leftSide.getPoints().size())) {//sanity checks

                        String pointIDR = rightSide.getPointSetID();
                        String pointIDL = leftSide.getPointSetID();
                        rightSidec = new NibbledRingData(rightSide, c1);
                        leftSidec = new NibbledRingData(leftSide, c2);
                        String s1 = rightSidec.getConstraintID();
                        String s2 = leftSidec.getConstraintID();


                        //add sub-problems to database
                        if (cactusData.NibbledRingDataBase.containsKey(pointIDR)) {
                            if (!cactusData.NibbledRingDataBase.get(pointIDR).containsKey(s1)) {
                                cactusData.NibbledRingDataBase.get(pointIDR).put(s1, rightSidec);
                            }
                        } else {
                            HashMap<String, NibbledRingData> newConstraintSet = new HashMap<>();
                            newConstraintSet.put(s1, rightSidec);
                            cactusData.NibbledRingDataBase.put(pointIDR, newConstraintSet);
                        }

                        if (cactusData.NibbledRingDataBase.containsKey(pointIDL)) {
                            if (!cactusData.NibbledRingDataBase.get(pointIDL).containsKey(s2)) {
                                cactusData.NibbledRingDataBase.get(pointIDL).put(s2, leftSidec);
                            }
                        } else {
                            HashMap<String, NibbledRingData> newConstraintSet = new HashMap<>();
                            newConstraintSet.put(s2, leftSidec);
                            cactusData.NibbledRingDataBase.put(pointIDL, newConstraintSet);
                        }


                        sumProduct.add(pointIDL);
                        sumProduct.add(s2);
                        sumProduct.add(pointIDR);
                        sumProduct.add(s1);
                    }
                }
            }
            //set sumproduct
            data.setSumProduct(new NibbledRingSumProduct(sumProduct));
            return;
        }
    }

    /**
     * sums the constraints in a constraint vector
     * @param c the constraint vector
     * @return the sum of constraints in c
     */
    private static int sum(ConstraintVector<Integer> c){
        int sum = 0;
        for(Integer i: c){
            sum+=i;
        }
        return sum;
    }

    /**
     * checks if a nibbled ring sum-problem is in one of the base cases
     * @param data the nibbled ring sub-problem
     * @return true if data is in a base case
     */
    private static boolean baseCaseTriangle(NibbledRingData data) {

        ArrayList<Point> points = data.getPoints();
        if (points.size() != 3 && points.size() != 2) {
            return false;
        }
        ArrayList<Point> innerLayer = data.innerCactus.getPoints();

        for (Point p : points) {
            p.index = -1;
        }

        Point.setIndexes(data.outerLayer, data.outerIndex);
        Point.setIndexes(innerLayer, data.innerIndex);
        int index = data.outerIndex;
        for (int i = data.b1.size() - 1; i >= 0; i--) {
            data.b1.get(i).index = index;
            index++;
        }

        index = data.outerIndex;
        for (int i = data.b2.size() - 1; i >= 0; i--) {
            data.b2.get(i).index = index;
            index++;
        }

        for (int i = 0; i < data.constraints.size(); i++) {
            int size = data.constraints.get(i);
            for (Point p : points) {
                if (p.index == i) {
                    size--;
                }
            }
            if (size != 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * compute all separator paths for a nibbled ring sub-problem
     * @param data the nibbled ring sub-problem
     * @return a list of all separator paths
     */
    private static ArrayList<ArrayList<Point>> computeSeperators(NibbledRingData data) {
        ArrayList<ArrayList<Point>> seperatorPaths = new ArrayList<>();


        ArrayList<Point> innerLayer = data.innerCactus.getPoints();
        Point.setIndexes(data.outerLayer, data.outerIndex);
        Point.setIndexes(innerLayer, data.innerIndex);
        Point.clearIndexes(data.freePoints);
        for (int i = data.b1.size() - 2; i >= 0; i--) {
            int index = Math.min(data.b1.get(i + 1).index + 1, data.innerIndex);
            if (data.b1.get(i).index == -1) {
                data.b1.get(i).index = index;
            }
        }
        for (int i = data.b2.size() - 2; i >= 0; i--) {
            int index = Math.min(data.b2.get(i + 1).index + 1, data.innerIndex);
            if (data.b2.get(i).index == -1) {
                data.b2.get(i).index = index;
            }
        }


        HashSet<Point> potentialPointSet = new HashSet<>();
        potentialPointSet.addAll(data.outerLayer);
        potentialPointSet.addAll(innerLayer);
        potentialPointSet.addAll(data.b1);
        potentialPointSet.addAll(data.b2);
        potentialPointSet.addAll(data.freePoints);
        potentialPointSet.remove(data.baseEdge.a);
        potentialPointSet.remove(data.baseEdge.b);

        ArrayList<Point> potentialPoints = new ArrayList<>(potentialPointSet);
        potentialPointSet.clear();
        for (int i = 0; i < potentialPoints.size(); i++) {//find all points in outerlayer, innerlayer, b1, b2, or free points that form an empty triangle with baseEdge
            boolean empty = true;
            for (int j = 0; j < potentialPoints.size(); j++) {
                if (i != j && Triangle.contains(data.baseEdge.a, data.baseEdge.b, potentialPoints.get(i), potentialPoints.get(j))) {
                    empty = false;
                    break;
                }
            }
            Point w = potentialPoints.get(i);

            ArrayList<Point> a = new ArrayList<>();
            a.add(data.baseEdge.a);

            ArrayList<Point> b = new ArrayList<>();
            b.add(data.baseEdge.b);

            if (empty && intersects(a, w, data)) {//first new edge intersects anything
                empty = false;

            } else if (empty && intersects(b, w, data)) {//second new edge inetersects anything
                empty = false;

            } else if (empty && data.innerCactus.insideForbidden(data.baseEdge.a, data.baseEdge.b, w)) {//new triangle is inside the inner cactus
                empty = false;

            } else if (!data.outerBoundary.incident(new Edge(data.baseEdge.a, w)) &&
                    !data.outerBoundary.contains(new Point((data.baseEdge.a.a + w.a)/2f, (data.baseEdge.a.b + w.b)/2f))) {//both new edges must be inside the boundary polygon
                empty = false;
            } else if (!data.outerBoundary.incident(new Edge(data.baseEdge.b, w)) &&
                    !data.outerBoundary.contains(new Point((data.baseEdge.b.a + w.a)/2f, (data.baseEdge.b.b + w.b)/2f))) {//both new edges must be inside the boundary polygon
                empty = false;
            }

            if (empty) {
                if (data.outerLayer.contains(w)) { // only one possible path namely just the point w
                    ArrayList<Point> path = new ArrayList<>();
                    path.add(w);
                    seperatorPaths.add(path);
                } else if (data.b1.contains(w)) { //special case where the path coincides with boundary path 1
                    ArrayList<Point> path = new ArrayList<>();
                    for (int pathIndex = data.b1.indexOf(w); pathIndex < data.b1.size(); pathIndex++) {
                        path.add(data.b1.get(pathIndex));
                    }
                    seperatorPaths.add(path);
                } else if (data.b2.contains(w)) { //special case where the path coincides with boundary path 2
                    ArrayList<Point> path = new ArrayList<>();
                    for (int pathIndex = data.b2.indexOf(w); pathIndex < data.b2.size(); pathIndex++) {
                        path.add(data.b2.get(pathIndex));
                    }
                    seperatorPaths.add(path);
                } else {//general case where a free point is chosen
                    ArrayList<Point> initialPath = new ArrayList<>();
                    initialPath.add(w);
                    computeSeperatorsFromPoint(seperatorPaths, initialPath, data);

                }
            }
        }

        for (int i = 0; i < seperatorPaths.size(); i++) {
            ArrayList<Point> path = seperatorPaths.get(i);
            if (!meetsConstraints(data, path, data.constraints)) {
                seperatorPaths.remove(i);
                i--;
            }
        }

        return seperatorPaths;
    }

    /**
     * recursivly compute all separator paths for a nibbled ring sub-problem that start with a sequence of points
     * @param paths the set of all separator paths computed so far
     * @param path the sequence of points which which the seperator paths start
     * @param data the nibbled ring sub-problem
     */
    private static void computeSeperatorsFromPoint(ArrayList<ArrayList<Point>> paths, ArrayList<Point> path, NibbledRingData data) {
        if (data.outerLayer.contains(path.get(path.size() - 1))) {// if path is a valid seperator path

            ArrayList<Point> newPath = new ArrayList<>();
            newPath.addAll(path);
            paths.add(newPath);
            return;

        } else if (path.size() >= data.width) { // if path cannot be valid
            return;
        } else {
            ArrayList<Point> innerLayer = data.innerCactus.getPoints();
            HashSet<Point> potentialPointSet = new HashSet<>();
            potentialPointSet.addAll(data.outerLayer);
            potentialPointSet.addAll(data.b1);
            potentialPointSet.addAll(data.b2);
            potentialPointSet.addAll(data.freePoints);
            potentialPointSet.add(data.baseEdge.a);
            potentialPointSet.add(data.baseEdge.b);
            potentialPointSet.removeAll(path);
            potentialPointSet.removeAll(innerLayer);
            ArrayList<Point> potentialPoints = new ArrayList<>(potentialPointSet);
            potentialPointSet.clear();
            for (Point p : potentialPoints) {
                if (!intersects(path, p, data)) {//edge is valid, edge does not cross any existing edges
                    if (data.b1.contains(p)) {
                        ArrayList<Point> newPath = new ArrayList<>();
                        newPath.addAll(path);
                        for (int pathIndex = data.b1.indexOf(p); pathIndex < data.b1.size(); pathIndex++) {
                            newPath.add(data.b1.get(pathIndex));
                        }
                        if (newPath.size() <= data.width) {
                            paths.add(newPath);
                        }

                    } else if (data.b2.contains(p)) {
                        ArrayList<Point> newPath = new ArrayList<>();
                        newPath.addAll(path);
                        for (int pathIndex = data.b2.indexOf(p); pathIndex < data.b2.size(); pathIndex++) {
                            newPath.add(data.b2.get(pathIndex));
                        }
                        if (newPath.size() <= data.width) {
                            paths.add(newPath);
                        }
                    } else {
                        path.add(p);
                        computeSeperatorsFromPoint(paths, path, data);
                        path.remove(p);
                    }
                }
            }
            return;
        }
    }

    /**
     * given a path and a nibbled ring sub-problem compute the right sub-problem.
     * @param path the separator path
     * @param data the nibbled ring sub-problem
     * @return the right sub-problem of data
     */
    private static NibbledRingData splitProblemOnPathRight(ArrayList<Point> path, NibbledRingData data) {

        ArrayList<Point> newOuterLayer = new ArrayList<>();
        ArrayList<Point> newb2 = new ArrayList<>();
        ArrayList<Point> newb1 = new ArrayList<>();
        ArrayList<Edge> newInnerCactusEdges = new ArrayList<>();
        ArrayList<Point> newInnerCactusPoints = new ArrayList<Point>();
        ArrayList<Point> newFreePoints = new ArrayList<>();
        Edge newBase = new Edge(path.get(0), data.baseEdge.b);
        int newOuterIndex = data.outerIndex;

        ArrayList<Point> polygonPoints = new ArrayList<>();


        if (data.innerCactus.getEdges().contains(newBase) || data.innerCactus.getEdges().contains(new Edge(newBase.b, newBase.a))) {
            boolean fixed = false;
            for (ArrayList<Point> list : data.innerCactus.getInnerComponents()) {//deal with new base edge being on an inner component
                for (int i = 0; i < list.size(); i++) {
                    Point p = list.get((i + 1) % list.size());
                    if (list.get(i).equals(newBase.a) && p.equals(newBase.b)) {
                        newBase = new Edge(newBase.a, list.get((list.size() + i - 1) % list.size()));
                        fixed = true;
                        break;
                    } else if (list.get(i).equals(newBase.b) && p.equals(newBase.a)) {
                        newBase = new Edge(newBase.a, list.get((i + 2) % list.size()));
                        fixed = true;
                        break;
                    }
                }
            }
            if (!fixed && data.outerBoundary.incident(newBase)) {
                for (Edge e : data.innerCactus.getEdges()) {
                    if (e.a.equals(newBase.a)) {
                        newBase = new Edge(e.b, e.a);
                        break;
                    } else if (e.b.equals(newBase.a)) {
                        newBase = new Edge(e.a, e.b);
                        break;
                    }
                }
            }
        }

        polygonPoints.addAll(data.innerCactus.pComponent(newBase, newBase.b, data.b2.get(0), true));

        if (data.innerCactus.getEdges().contains(newBase)) {
            newInnerCactusEdges.add(newBase);
        }
        for (int i = 1; i <= polygonPoints.size() - 2; i++) {
            newInnerCactusEdges.add(new Edge(polygonPoints.get(i), polygonPoints.get(i + 1)));
        }

        if (data.b2.get(data.b2.size() - 1).equals(path.get(path.size() - 1))) {//deal with path intersecting b2
            int cap = 1;
            for (; cap <= Math.min(path.size(), data.b2.size()); cap++) {
                if (!path.get(path.size() - cap).equals(data.b2.get(data.b2.size() - cap))) {

                    break;
                }

            }
            cap--;
            newOuterLayer.add(path.get(path.size() - cap));
            newOuterIndex += (cap - 1);


            for (int k = 1; k < data.b2.size() - cap; k++) {//boundary path b2 and outer layer point
                polygonPoints.add(data.b2.get(k));
                newb2.add(data.b2.get(k));
            }


            newb2.add(0, data.b2.get(0));
            if (path.get(path.size() - cap) != data.b2.get(0))
                newb2.add(path.get(path.size() - cap));

            if (!polygonPoints.contains(newOuterLayer.get(0)))
                polygonPoints.add(newOuterLayer.get(0));

            for (int k = path.size() - cap - 1; k > 0; k--) { //new boundary path b1
                polygonPoints.add(path.get(k));
                newb1.add(0, path.get(k));
            }
            newb1.add(0, path.get(0));
            if (path.get(path.size() - cap) != path.get(0))
                newb1.add(path.get(path.size() - cap));

        } else {
            newb2.addAll(data.b2);
            newb1.addAll(path);
            for (int k = 1; k < data.b2.size(); k++) {
                polygonPoints.add(data.b2.get(k));
            }

            newOuterLayer.add(data.outerLayer.get(data.outerLayer.size() - 1));

            for (int k = data.outerLayer.size() - 2; !data.outerLayer.get(k).equals(path.get(path.size() - 1)); k--) {

                polygonPoints.add(data.outerLayer.get(k));
                newOuterLayer.add(0, data.outerLayer.get(k));
            }
            newOuterLayer.add(0, path.get(path.size() - 1));

            for (int k = path.size() - 1; k >= 1; k--) {
                polygonPoints.add(path.get(k));
            }

        }

        for (int i = 1; i < polygonPoints.size(); i++) {
            if (polygonPoints.get(i).equals(polygonPoints.get(i - 1))) {
                polygonPoints.remove(i);
                i--;
            }
        }

        CactusPolygon polygon = null;
        if (polygonPoints.size() > 2) {
            polygon = new CactusPolygon(polygonPoints);

            for (Point p : data.freePoints) {
                if (!path.contains(p) && polygon.contains(p)) {
                    newFreePoints.add(p);
                }
            }
            ArrayList<Edge> cactusEdges = data.innerCactus.getEdges();
            for (Edge e : cactusEdges) {
                if (!newInnerCactusEdges.contains(e) && polygon.contains(new Point((e.a.a + e.b.a) / 2f, (e.a.b + e.b.b) / 2f))) {
                    newInnerCactusEdges.add(e);
                }
            }

            for (Point p : data.innerCactus.getLoosePoints()) {
                if (polygon.contains(p) || polygonPoints.contains(p)) {
                    newInnerCactusPoints.add(p);
                }
            }
        }

        HashSet<Point> allPoints = new HashSet<>();
        allPoints.addAll(newb1);
        allPoints.addAll(newb2);
        allPoints.addAll(newOuterLayer);
        allPoints.addAll(newFreePoints);
        for (Point p : allPoints) {
            if (data.innerCactus.getPoints().contains(p)) {
                boolean inLayer = false;
                if (newInnerCactusPoints.contains(p)) {
                    inLayer = true;
                } else {
                    for (Edge e : newInnerCactusEdges) {
                        if (e.a.equals(p) || e.b.equals(p)) {
                            inLayer = true;
                        }
                    }
                }
                if (!inLayer) {
                    newInnerCactusPoints.add(p);
                }
            }
        }

        return new NibbledRingData(newOuterLayer, newBase, newb1, newb2, new CactusLayer(newInnerCactusEdges, newInnerCactusPoints), newFreePoints, null, polygon, newOuterIndex, data.innerIndex, data.innerIndex - newOuterIndex + 1);

    }

    /**
     * given a path and a nibbled ring sub-problem compute the left sub-problem.
     * @param path the separator path
     * @param data the nibbled ring sub-problem
     * @return the left sub-problem of data
     */
    private static NibbledRingData splitProblemOnPathLeft(ArrayList<Point> path, NibbledRingData data) {

        //Sub problem 1:
        //b2 = b2
        //b2 = path
        //base edge = (base edge.a path(0))
        //outer layer = outerlayer(0)-path.last
        //inner cactus = all edges inside new poly
        //free points = all edges inside new poly
        ArrayList<Point> newOuterLayer = new ArrayList<>();
        ArrayList<Point> newb2 = new ArrayList<>();
        ArrayList<Point> newb1 = new ArrayList<>();
        ArrayList<Edge> newInnerCactusEdges = new ArrayList<>();
        ArrayList<Point> newInnerCactusPoints = new ArrayList<>();
        ArrayList<Point> newFreePoints = new ArrayList<>();
        Edge newBase = new Edge(data.baseEdge.a, path.get(0));
        int newOuterIndex = data.outerIndex;


        ArrayList<Point> polygonPoints = new ArrayList<>();


        if (data.innerCactus.getEdges().contains(newBase) || data.innerCactus.getEdges().contains(new Edge(newBase.b, newBase.a))) {
            boolean fixed = false;
            for (ArrayList<Point> list : data.innerCactus.getInnerComponents()) {//deal with new base edge being on an inner component
                for (int i = 0; i < list.size(); i++) {
                    Point p = list.get((i + 1) % list.size());
                    if (list.get(i).equals(newBase.a) && p.equals(newBase.b)) {
                        newBase = new Edge(list.get((i + 2) % list.size()), path.get(0));
                        break;
                    } else if (list.get(i).equals(newBase.b) && p.equals(newBase.a)) {
                        newBase = new Edge(list.get((list.size() + i - 1) % list.size()), path.get(0));
                        break;
                    }
                }
            }
            if (!fixed && data.outerBoundary.incident(newBase)) {
                for (Edge e : data.innerCactus.getEdges()) {
                    if (e.a.equals(newBase.b)) {
                        newBase = new Edge(e.b, e.a);
                        break;
                    } else if (e.b.equals(newBase.b)) {
                        newBase = new Edge(e.a, e.b);
                        break;
                    }
                }
            }

        }


        polygonPoints.addAll(data.innerCactus.pComponent(newBase, newBase.a, data.b1.get(0), false));

        if (data.innerCactus.getEdges().contains(newBase)) {
            newInnerCactusEdges.add(newBase);
        }
        for (int i = 1; i <= polygonPoints.size() - 2; i++) {
            newInnerCactusEdges.add(new Edge(polygonPoints.get(i), polygonPoints.get(i + 1)));
        }

        if (data.b1.get(data.b1.size() - 1).equals(path.get(path.size() - 1))) {//deal with path intersecting b1
            int cap = 1;
            for (; cap <= Math.min(path.size(), data.b1.size()); cap++) {
                if (!path.get(path.size() - cap).equals(data.b1.get(data.b1.size() - cap))) {
                    break;
                }

            }
            cap--;
            newOuterLayer.add(path.get(path.size() - cap));
            newOuterIndex += (cap - 1);
            for (int k = 1; k < data.b1.size() - cap; k++) {//boundary path b2 and outer layer point
                polygonPoints.add(data.b1.get(k));
                newb1.add(data.b1.get(k));
            }
            newb1.add(0, data.b1.get(0));
            if (path.get(path.size() - cap) != data.b1.get(0))
                newb1.add(path.get(path.size() - cap));

            if (!polygonPoints.contains(newOuterLayer.get(0)))
                polygonPoints.add(newOuterLayer.get(0));


            for (int k = path.size() - cap - 1; k > 0; k--) { //new boundary path b1
                polygonPoints.add(path.get(k));
                newb2.add(0, path.get(k));
            }
            newb2.add(0, path.get(0));
            if (path.get(path.size() - cap) != path.get(0))
                newb2.add(path.get(path.size() - cap));

        } else {
            newb1.addAll(data.b1);
            newb2.addAll(path);
            for (int k = 1; k < data.b1.size(); k++) {
                polygonPoints.add(data.b1.get(k));
            }
            newOuterLayer.add(data.outerLayer.get(0));
            for (int k = 1; !data.outerLayer.get(k).equals(path.get(path.size() - 1)); k++) {
                polygonPoints.add(data.outerLayer.get(k));
                newOuterLayer.add(data.outerLayer.get(k));
            }
            newOuterLayer.add(path.get(path.size() - 1));

            for (int k = path.size() - 1; k >= 1; k--) {
                polygonPoints.add(path.get(k));
            }
        }

        for (int i = 1; i < polygonPoints.size(); i++) {
            if (polygonPoints.get(i).equals(polygonPoints.get(i - 1))) {
                polygonPoints.remove(i);
                i--;
            }
        }

        CactusPolygon polygon = null;
        if (polygonPoints.size() > 2) {
            polygon = new CactusPolygon(polygonPoints);

            for (Point p : data.freePoints) {
                if (!path.contains(p) && polygon.contains(p)) {
                    newFreePoints.add(p);
                }
            }
            ArrayList<Edge> cactusEdges = data.innerCactus.getEdges();
            for (Edge e : cactusEdges) {
                if (!newInnerCactusEdges.contains(e) && polygon.contains(new Point((e.a.a + e.b.a) / 2f, (e.a.b + e.b.b) / 2f))) {
                    newInnerCactusEdges.add(e);
                }
            }

            for (Point p : data.innerCactus.getLoosePoints()) {
                if (polygon.contains(p) || polygonPoints.contains(p)) {
                    newInnerCactusPoints.add(p);
                }
            }
        }

        HashSet<Point> allPoints = new HashSet<>();
        allPoints.addAll(newb1);
        allPoints.addAll(newb2);
        allPoints.addAll(newOuterLayer);
        allPoints.addAll(newFreePoints);
        for (Point p : allPoints) {
            if (data.innerCactus.getPoints().contains(p)) {
                boolean inLayer = false;
                if (newInnerCactusPoints.contains(p)) {
                    inLayer = true;
                } else {
                    for (Edge e : newInnerCactusEdges) {
                        if (e.a.equals(p) || e.b.equals(p)) {
                            inLayer = true;
                        }
                    }
                }
                if (!inLayer) {
                    newInnerCactusPoints.add(p);
                }
            }
        }

        return new NibbledRingData(newOuterLayer, newBase, newb1, newb2, new CactusLayer(newInnerCactusEdges, newInnerCactusPoints), newFreePoints, null, polygon, newOuterIndex, data.innerIndex, data.innerIndex - newOuterIndex + 1);

    }


    /**
     * check if a separator path meets the constraints in a sub-problem
     * @param data the nibbled ring sub-problem
     * @param path the separator path
     * @param constraints the set of layer constraints
     * @return true if path meets all the constraints, false otherwise
     */
    private static boolean meetsConstraints(NibbledRingData data, ArrayList<Point> path, ConstraintVector<Integer> constraints) {
        ArrayList<Point> points = data.getPoints();
        for (Point p : points) {
            p.index = -1;
        }

        Point.setIndexes(data.outerLayer, data.outerIndex);
        Point.setIndexes(data.innerCactus.getPoints(), data.innerIndex);


        for (int i = data.b1.size() - 2; i >= 0; i--) {
            int index = data.b1.get(i + 1).index + 1;
            if (data.b1.get(i).index != -1 && data.b1.get(i).index != index) {//ensure layer indexes make sense
                return false;
            }
            data.b1.get(i).index = index;
        }

        for (int i = data.b2.size() - 2; i >= 0; i--) {
            int index = data.b2.get(i + 1).index + 1;
            if (data.b2.get(i).index != -1 && data.b2.get(i).index != index) {//ensure layer indexes make sense
                return false;
            }
            data.b2.get(i).index = index;
        }

        for (int i = path.size() - 2; i >= 0; i--) {
            int index = path.get(i + 1).index + 1;
            if (path.get(i).index != -1 && path.get(i).index != index) {//ensure layer indexes make sense
                return false;
            }
            path.get(i).index = index;
        }

        //one edge (the base edge here) cannot span more then one layer
        if (Math.abs(data.baseEdge.a.index - data.baseEdge.b.index) > 1) {
            return false;//redundent check
        }
        //one edge (a new base edge here) cannot span more then one layer
        if (Math.abs(path.get(0).index - data.baseEdge.b.index) > 1) {
            return false;
        }
        //one edge (a new base edge here) cannot span more then one layer
        if (Math.abs(path.get(0).index - data.baseEdge.a.index) > 1) {
            return false;
        }

        //confirm id numbers make sense
        if (data.b1.size() > 1 && data.b1.get(1).index == path.get(0).index && data.b1.get(1).ID_NUMBER > path.get(0).ID_NUMBER) {
            return false;
        }
        if (data.b2.size() > 1 && data.b2.get(1).index == path.get(0).index && data.b2.get(1).ID_NUMBER > path.get(0).ID_NUMBER) {
            return false;
        }
        if (path.size() > 1 && path.get(1).index == data.baseEdge.a.index && path.get(1).ID_NUMBER > data.baseEdge.a.ID_NUMBER) {
            return false;
        }
        if (path.size() > 1 && path.get(1).index == data.baseEdge.b.index && path.get(1).ID_NUMBER > data.baseEdge.b.ID_NUMBER) {
            return false;
        }

        //Any edge with both endpoints in the inner layer must also be in the inner layer
        if (path.get(0).index == data.innerIndex && data.baseEdge.a.index == data.innerIndex &&
                !(data.innerCactus.getEdges().contains(new Edge(path.get(0), data.baseEdge.a)) || data.innerCactus.getEdges().contains(new Edge(data.baseEdge.a, path.get(0))))) {
            return false;
        }

        //Any edge with both endpoints in the inner layer must also be in the inner layer
        if (path.get(0).index == data.innerIndex && data.baseEdge.b.index == data.innerIndex &&
                !(data.innerCactus.getEdges().contains(new Edge(path.get(0), data.baseEdge.b)) || data.innerCactus.getEdges().contains(new Edge(data.baseEdge.b, path.get(0))))) {
            return false;
        }
        //ensure the path only has one point in the outer layer
        int num = 0;
        for (Point p : path) {
            if (data.outerLayer.contains(p))
                num++;
        }
        if (num != 1)
            return false;

        //ensure no constraint are violated
        for (int i = 0; i < constraints.size(); i++) {
            int sum = 0;
            for (Point p : points) {
                if (p.index == i) {
                    sum++;
                }
            }
            if (sum > constraints.get(i)) {
                return false;
            }
        }

        return true;
    }

    /**
     * check if adding a point to a separator path would result in it crossing itself or
     * another other component of a nibbled ring sub-problem
     * @param path the separator path
     * @param newPoint the new point to add to path
     * @param data the nibbled ring sub-problem
     * @return true if it intersects anything, false otherwise
     */
    private static boolean intersects(ArrayList<Point> path, Point newPoint, NibbledRingData data) {

        //Check if new edge intersects existing path
        Edge e = new Edge(path.get(path.size() - 1), newPoint);
        for (int i = 1; i < path.size(); i++) {
            if (e.intersects(new Edge(path.get(i - 1), path.get(i)))) {
                return true;
            }
        }

        //check if inner cactus intersects the new edge
        if (data.innerCactus.intersects(e)) {

            return true;
        }


        //check outerlayer
        for (int i = 1; i < data.outerLayer.size(); i++) {
            if (e.intersects(new Edge(data.outerLayer.get(i - 1), data.outerLayer.get(i)))) {
                return true;
            }
        }

        //check boundary path 1
        for (int i = 1; i < data.b1.size(); i++) {
            if (e.intersects(new Edge(data.b1.get(i - 1), data.b1.get(i)))) {
                return true;
            }
        }

        //check boundary path 2
        for (int i = 1; i < data.b2.size(); i++) {
            if (e.intersects(new Edge(data.b2.get(i - 1), data.b2.get(i)))) {
                return true;
            }
        }

        //check base Edge
        if (e.intersects(data.baseEdge)) {
            return true;
        }

        return false;
    }
}

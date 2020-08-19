package DataTypes;

import Algorithm.SumProduct;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

/**
 * an instance of a Nibbled Ring Sub-Problem
 */
public class NibbledRingData {

    public ArrayList<Point> outerLayer;
    public Edge baseEdge;
    public ArrayList<Point> b1, b2;
    public CactusLayer innerCactus;
    public ArrayList<Point> freePoints;
    public ConstraintVector<Integer> constraints;
    public int outerIndex, innerIndex, width;
    public CactusPolygon outerBoundary;

    public static ArrayList<String> pointIDs = new ArrayList<>();

    String pointID = null,constraintID = null;

    public SumProduct triangulationCount = null;

    public NibbledRingData(ArrayList<Point> outerLayer, Edge baseEdge, ArrayList<Point> b1, ArrayList<Point> b2, CactusLayer innerCactus, ArrayList<Point> freePoints, ConstraintVector<Integer> constraints, CactusPolygon outerBoundary, int outerIndex, int innerIndex, int width) {
        this.outerIndex = outerIndex;
        this.b1 = b1;
        this.b2 = b2;
        this.outerLayer = outerLayer;
        this.baseEdge = baseEdge;
        this.innerCactus = innerCactus;
        this.freePoints = freePoints;
        this.constraints = constraints;
        this.innerIndex = innerIndex;
        this.width = width;
        this.outerBoundary = outerBoundary;
    }

    /**
     * creates a copy of a nibbled ring sub-problem but with the given constraints
     *
     * @param data        the original problem
     * @param constraints the new constraints
     */
    public NibbledRingData(NibbledRingData data, ConstraintVector<Integer> constraints) {
        this.outerIndex = data.outerIndex;
        this.b1 = data.b1;
        this.b2 = data.b2;
        this.outerLayer = data.outerLayer;
        this.baseEdge = data.baseEdge;
        this.innerCactus = data.innerCactus;
        this.freePoints = data.freePoints;
        this.constraints = constraints;
        this.innerIndex = data.innerIndex;
        this.pointID = data.pointID;
        this.width = data.width;
        this.outerBoundary = data.outerBoundary;
    }

    public void setSumProduct(SumProduct sumProduct) {
        triangulationCount = sumProduct;
    }

    /**
     * generate a string that identifies this nibbled ring sub-problem
     * @returns a unique string identifying the nibbled ring sub-problem aside from its constraints
     */
    public String getPointSetID() {
        if(pointID == null) {
            ArrayList<Point> pointsDone = new ArrayList<>();
            StringBuilder id = new StringBuilder(outerIndex);
            id.append('/');
            id.append(innerIndex);
            id.append('/');
            id.append(baseEdge);

            freePoints.sort(new Comparator<Point>() {
                @Override
                public int compare(Point p1, Point p2) {
                    if (p1.ID_NUMBER > p2.ID_NUMBER) {
                        return 1;
                    } else if (p1.ID_NUMBER < p2.ID_NUMBER) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });

            ArrayList<Point> innerPoints = innerCactus.getLoosePoints();
            innerPoints.sort(new Comparator<Point>() {
                @Override
                public int compare(Point p1, Point p2) {
                    if (p1.ID_NUMBER > p2.ID_NUMBER) {
                        return 1;
                    } else if (p1.ID_NUMBER < p2.ID_NUMBER) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });

            ArrayList<Edge> innerEdges = innerCactus.getEdges();
            innerEdges.sort(new Comparator<Edge>() {
                @Override
                public int compare(Edge p1, Edge p2) {
                    if (p1.a.ID_NUMBER > p2.a.ID_NUMBER) {
                        return 1;
                    } else if (p1.a.ID_NUMBER < p2.a.ID_NUMBER) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });

            id.append('/');
            for (Point p : outerLayer) {
                if (!pointsDone.contains(p)) {
                    id.append(p.ID_NUMBER);
                    id.append(',');
                    pointsDone.add(p);
                }
            }

            id.append('/');
            for (Point p : innerPoints) {
                id.append(p.ID_NUMBER);
            }

            id.append(',');
            for (Edge e : innerEdges) {
                if (e.a.ID_NUMBER > e.b.ID_NUMBER) {
                    id.append(e.a.ID_NUMBER);
                    id.append('-');
                    id.append(e.b.ID_NUMBER);
                    id.append(',');
                } else {
                    id.append(e.b.ID_NUMBER);
                    id.append('-');
                    id.append(e.a.ID_NUMBER);
                    id.append(',');
                }
            }

            id.append('/');
            for (Point p : b1) {
                if (!pointsDone.contains(p)) {
                    id.append(p.ID_NUMBER);
                    id.append(',');
                    pointsDone.add(p);
                }
            }

            id.append('/');
            for (Point p : b2) {
                if (!pointsDone.contains(p)) {
                    id.append(p.ID_NUMBER);
                    id.append(',');
                    pointsDone.add(p);
                }
            }
            String ids = id.toString();
            for(String s: pointIDs){
                if(s.equals(ids)){
                    pointID = s;
                    return pointID;
                }
            }
            pointIDs.add(ids);
            pointID = ids;
        }
        return pointID;
    }

    /**
     * get the points of this nibbled ring sub-problem
     * @return an array list of points of this sub-problem
     */
    public ArrayList<Point> getPoints() {
        ArrayList<Point> points = new ArrayList<>();
        for (Point p : outerLayer) {
            if (!points.contains(p)) {
                points.add(p);
            }
        }
        for (Point p : innerCactus.getPoints()) {
            if (!points.contains(p)) {
                points.add(p);
            }
        }
        for (Point p : b1) {
            if (!points.contains(p)) {
                points.add(p);
            }
        }
        for (Point p : b2) {
            if (!points.contains(p)) {
                points.add(p);
            }
        }
        for (Point p : freePoints) {
            if (!points.contains(p)) {
                points.add(p);
            }
        }

        return points;
    }

    /**
     * @return a unique string identifying the constraints
     */
    public String getConstraintID() {
        if(constraintID == null)
            constraintID = constraints.toString();
        return constraintID;
    }

    public SumProduct getCount() {
        return triangulationCount;
    }

}

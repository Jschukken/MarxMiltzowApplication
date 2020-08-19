package DataTypes;

import java.util.ArrayList;
import java.util.Collections;

/**
 * a cactus layer
 *
 * @author Jelle Schukken
 */
public class CactusLayer {
    ArrayList<Edge> edges;
    ArrayList<Point> points;
    ArrayList<ArrayList<Point>> innerComponents;
    boolean valid = true;

    /**
     * creates a new empty cactus layer
     */
    public CactusLayer(){
        innerComponents = new ArrayList<>();
        edges = new ArrayList<>();
        points = new ArrayList<>();
    }

    /**
     * creates a cactus layer with the input edges and points
     * determines if the cactus layer is valid (all edges are incident to the outer boundary
     * @param edges the edges of the cactus layer
     * @param points the points of the cactus layer
     */
    public CactusLayer(ArrayList<Edge> edges, ArrayList<Point> points){
        this.edges = edges;
        if(edges.size()>2) {
            innerComponents = computeInnerComponents(false);
            for(int i = 0; i < innerComponents.size() && valid; i++){
                ArrayList<Point> comp1 = innerComponents.get(i);
                for(int k = i+1; k < innerComponents.size() && valid; k++){
                    ArrayList<Point> comp2 = innerComponents.get(k);
                    for(int p = 0; p < innerComponents.get(i).size() && valid; p++){
                        Point p1 = comp1.get(p);
                        Point p2 = comp1.get((p+1)%comp1.size());
                        for(int q = 0; q < innerComponents.get(k).size() && valid; q++){

                            Point p3 = comp2.get(k);
                            Point p4 = comp2.get((k+1)%comp2.size());

                            if(p1.equals(p3) && p2.equals(p4)){
                                valid = false;
                            }
                            if(p1.equals(p4) && p2.equals(p3)){
                                valid = false;
                            }
                        }
                    }
                }
            }

            ArrayList<ArrayList<Point>> altComp = computeInnerComponents(true);
            for(ArrayList<Point> comp1: innerComponents){
                boolean contains = true;
                for(ArrayList<Point> comp2: altComp){
                    contains = true;
                    for(Point p: comp1){
                        if(!comp2.contains(p)){
                            contains = false;
                            break;
                        }
                    }
                    if(contains){
                        break;
                    }
                }
                if(!contains){
                    valid = false;
                    break;
                }
            }
            if(valid) {
                for (Edge e : edges) {
                    if (isForbidden(e)) {
                        valid = false;
                        break;
                    }
                    for(int i = 0; i < innerComponents.size() && valid; i++){
                        boolean hasEdge = false;
                        if(innerComponents.get(i).contains(e.a) && innerComponents.get(i).contains(e.b)) {
                            for (int p = 0; p < innerComponents.get(i).size() && valid; p++) {
                                if (e.a.equals(innerComponents.get(i).get(p)) &&
                                        e.b.equals(innerComponents.get(i).get((p + 1) % innerComponents.get(i).size()))) {
                                    hasEdge = true;
                                } else if (e.b.equals(innerComponents.get(i).get(p)) &&
                                        e.a.equals(innerComponents.get(i).get((p + 1) % innerComponents.get(i).size()))) {
                                    hasEdge = true;
                                }
                            }
                            if(!hasEdge){
                                valid = false;
                                System.out.println(e + " : " + innerComponents.get(i));
                                break;
                            }
                        }

                    }
                }
            }

        }else{
            innerComponents = new ArrayList<>();
        }

        for(Edge e: edges){
            if(points.contains(e.a) || points.contains(e.b)){
                valid = false;
                break;
            }
        }

        this.points = points;

        for(ArrayList<Point> component: innerComponents){
            CactusPolygon p = new CactusPolygon(component);
            for(Point point: points){
                if(p.contains(point)){
                    valid = false;
                    break;
                }
            }
            if(!valid){
                break;
            }
        }

    }

    public ArrayList<ArrayList<Point>> getInnerComponents() {
        return innerComponents;
    }

    /**
     * find all edges bounding an interior face, for each interior face.
     * @param clockwise the direction in which to iterate through edges
     * @return a list of lists of points where each list of points is a polygon bounding an interior face
     */
    private ArrayList<ArrayList<Point>> computeInnerComponents(boolean clockwise) {
        ArrayList<ArrayList<Point>> innerComponents = new ArrayList<>();
        ArrayList<Edge> usedEdges = new ArrayList<>();
        ArrayList<Point> component = new ArrayList<>();

        Edge currentEdge = edges.get(0);

        if(clockwise) {
            component.add(currentEdge.a);
            component.add(currentEdge.b);
        }else{
            component.add(currentEdge.b);
            component.add(currentEdge.a);
        }
        usedEdges.add(currentEdge);

        while (usedEdges.size() != edges.size()) {
            Edge bestEdge = null;
            double bestAngle = 0.0;
            if(clockwise){
                bestAngle = 100;
            }

            for (Edge e : edges) {
                if (!usedEdges.contains(e)) {
                    //System.out.println(" Edge: " + e.b.ID_NUMBER + " (" + e.a.a + " : " + e.a.b + ") - (" + e.b.a + " : " + e.b.b + ")");
                    if (e.a.equals(component.get(component.size() - 1)) || e.b.equals(component.get(component.size() - 1))) {
                        double angle = Edge.getAngle(currentEdge, e);
                        if (bestEdge == null || ((clockwise && angle < bestAngle) || (!clockwise && angle > bestAngle))) {
                            bestEdge = e;
                            bestAngle = angle;
                        }
                    }
                }
            }


            if (bestEdge == null) {
                if (component.size() <= 2) {


                    for (Edge e : edges) {
                        if (!usedEdges.contains(e)) {
                            currentEdge = e;

                            component.clear();
                            if(clockwise) {
                                component.add(currentEdge.a);
                                component.add(currentEdge.b);
                            }else{
                                component.add(currentEdge.b);
                                component.add(currentEdge.a);
                            }
                            usedEdges.add(currentEdge);
                            break;
                        }
                    }
                } else {

                    component.remove(component.size() - 1);
                    for (Edge e : usedEdges) {
                        if ((e.a.equals(component.get(component.size() - 1)) && e.b.equals(component.get(component.size() - 2)))
                                || (e.b.equals(component.get(component.size() - 1)) && e.a.equals(component.get(component.size() - 2)))) {

                            currentEdge = e;
                            break;
                        }
                    }
                }
            } else {
                Point newPoint;

                if (component.get(component.size() - 1).equals(bestEdge.a)) {
                    newPoint = bestEdge.b;
                } else {
                    newPoint = bestEdge.a;
                }

                if (component.contains(newPoint)) {
                    ArrayList<Point> newInnerComponent = new ArrayList<>();
                    newInnerComponent.add(newPoint);
                    while (component.size() > component.indexOf(newPoint) + 1) {
                        newInnerComponent.add(component.remove(component.indexOf(newPoint) + 1));
                    }

                    innerComponents.add(newInnerComponent);
                } else {
                    component.add(newPoint);
                }
                currentEdge = bestEdge;
                usedEdges.add(bestEdge);
            }

        }
        return innerComponents;
    }

    public ArrayList<Point> getPoints() {
        ArrayList<Point> points = new ArrayList<>();
        for (Edge e : edges) {
            if (!points.contains(e.a)) {
                points.add(e.a);
            }
            if (!points.contains(e.b)) {
                points.add(e.b);
            }
        }
        if(this.points != null){
            points.addAll(this.points);
        }
        return points;
    }

    public ArrayList<Point> getLoosePoints(){
        return this.points;
    }

    public boolean intersects(Edge e) {
        for (Edge edge : edges) {
            if (e.intersects(edge)) {
                return true;
            }
        }

        return false;
    }

    /**
     * finds set of edges connecting point b to endpoint
     * @param newBase the base edge of the new nibbled ring sub-problem
     * @param b the point of the base edge on which to start
     * @param endPoint the point on which to end
     * @param clockwise the direction which edges are searched
     * @return a path along the cactus layer that starts at b and ends at endpoint
     */
    public ArrayList<Point> pComponent(Edge newBase, Point b, Point endPoint, boolean clockwise) {

        ArrayList<Point> component = new ArrayList<>();
        if(newBase.b.equals(b)) {
            component.add(newBase.a);
            component.add(newBase.b);
        }
        else{
            component.add(newBase.b);
            component.add(newBase.a);
        }

        ArrayList<Edge> usedEdges = new ArrayList<>();
        usedEdges.add(newBase);

        Edge currentEdge = newBase;

        if(component.get(1).equals(endPoint) || component.get(0).equals(endPoint)){
            return component;
        }

        while (!component.get(component.size() - 1).equals(endPoint)) {
            Edge bestEdge = null;
            double bestAngle;
            if(clockwise){
                bestAngle = -10000.0;
            }else{
                bestAngle = 10000.0;
            }
            for (Edge e : edges) {
                if (!usedEdges.contains(e)) {
                    if (e.a.equals(component.get(component.size() - 1)) || e.b.equals(component.get(component.size() - 1))) {
                        double angle = Edge.getAngle(currentEdge, e);
                        if (bestEdge == null || (clockwise && angle > bestAngle) || (!clockwise && angle < bestAngle)) {
                            bestEdge = e;
                            //System.out.println(" Edge: " + e.b.ID_NUMBER + " (" + e.a.a + " : " + e.a.b + ") - (" + e.b.a + " : " + e.b.b + ")");
                            bestAngle = angle;
                        }
                    }
                }
            }

            if (bestEdge == null) {
                if(component.size() == 2){
                    component.add(component.remove(0));
                }else {
                    component.remove(component.size() - 1);
                    for (Edge e : usedEdges) {
                        if ((e.a.equals(component.get(component.size() - 1)) && e.b.equals(component.get(component.size() - 2)))
                                || (e.b.equals(component.get(component.size() - 1)) && e.a.equals(component.get(component.size() - 2)))) {
                            currentEdge = e;
                            break;
                        }
                    }
                }

            } else {
                Point newPoint;

                if (component.get(component.size() - 1).equals(bestEdge.a)) {
                    newPoint = bestEdge.b;
                } else {
                    newPoint = bestEdge.a;
                }

                if (component.contains(newPoint)) {

                    while (component.size() > component.indexOf(newPoint) + 1) {
                        component.remove(component.indexOf(newPoint) + 1);
                    }


                } else {
                    component.add(newPoint);
                }
                currentEdge = bestEdge;
                usedEdges.add(bestEdge);
            }
        }
        return component;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    /**
     * checks if an edge lies within a forbidden region
     * @param e the edge to check
     * @return true if it does, false otherwise
     */
    public boolean isForbidden(Edge e) {
        for(ArrayList<Point> component: innerComponents){
            CactusPolygon p = new CactusPolygon(component);
            if(p.contains(e.a) && !p.hasVertex(e.a)){
                return true;
            }else if(p.contains(e.b) && !p.hasVertex(e.b)){
                return true;
            }
        }
        return false;
    }
    /**
     * checks if a triangle lies within a forbidden region
     * @param a,b,w the vertexes of the triangle
     * @return true if it does, false otherwise
     */
    public boolean insideForbidden(Point a, Point b, Point w) {
        if(edges.contains(new Edge(a,w)) && edges.contains(new Edge(b,w))){
            return true;
        }
        for(ArrayList<Point> component: innerComponents){
            CactusPolygon p = new CactusPolygon(component);
            if(p.contains(new Point((a.a+w.a)/2, (a.b+w.b)/2)) && p.contains(new Point((b.a+w.a)/2, (b.b+w.b)/2))){
                return true;
            }
        }
        return false;
    }

    /**
     * @return return true of this is a valid cactus layer
     */
    public boolean isValid() {
        return valid;

    }

    public String toString(){
        String output = "";
        for(Edge e: edges){
            output += e.toString() + ", ";
        }
        output += ": ";
        for(Point p: points){
            output += p.toString() + ", ";
        }
        return output;
    }
}

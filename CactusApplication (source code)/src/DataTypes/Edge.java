package DataTypes;

/**
 * an instance of an undirected edge between two points
 */
public class Edge {

    public Point a;
    public Point b;
    public double length;

    public Edge(Point a, Point b){
        this.a = a;
        this.b = b;
        length = Math.sqrt((a.a-b.a)*(a.a-b.a) + (a.b-b.b)*(a.b-b.b));
    }

    /**
     * determines if a point is on a segment
     * @param p the point
     * @param q the start point of the segment
     * @param r the end point of the segment
     * @return true if p lies on the segment
     */
    private boolean onSegment(Point p, Point q, Point r)
    {
        if (q.a <= Math.max(p.a, r.a) && q.a >= Math.min(p.a, r.a) &&
                q.b <= Math.max(p.b, r.b) && q.b >= Math.min(p.b, r.b))
            return true;

        return false;
    }

    private int orientation(Point p, Point q, Point r)
    {

        float val = (q.b - p.b) * (r.a - q.a) -
                (q.a - p.a) * (r.b - q.b);

        if (val == 0) return 0;

        return (val > 0)? 1: 2;
    }


    /**
     * checks if this edge intersects another edge
     * @param e the edge to check for intersections
     * @return true if this edge intersects e, false otherwise
     */
    public boolean intersects(Edge e)
    {

        if(a.equals(e.a) || a.equals(e.b) || b.equals(e.a) || b.equals(e.b)){
            return false;
        }

        int o1 = orientation(a, b, e.a);
        int o2 = orientation(a, b, e.b);
        int o3 = orientation(e.a, e.b, a);
        int o4 = orientation(e.a, e.b, b);

        if (o1 != o2 && o3 != o4)
            return true;


        if (o1 == 0 && onSegment(a, e.a, b)) return true;


        if (o2 == 0 && onSegment(a, e.b, b)) return true;


        if (o3 == 0 && onSegment(e.a, a, e.b)) return true;


        if (o4 == 0 && onSegment(e.a, b, e.b)) return true;

        return false;
    }

    /**
     * returns the counter clockwise angle between two edges in radians.
     * @param e1
     * @param e2
     * @return the angle
     */
    public static double getAngle(Edge e1, Edge e2) {
        Float x1, x2, y1, y2;
        if (e1.a.equals(e2.a)) {
            x1 = e1.b.a - e1.a.a;
            x2 = e2.b.a - e1.a.a;
            y1 = e1.b.b - e1.a.b;
            y2 = e2.b.b - e1.a.b;
        } else if (e1.b.equals(e2.b)) {
            x1 = e1.a.a - e1.b.a;
            x2 = e2.a.a - e1.b.a;
            y1 = e1.a.b - e1.b.b;
            y2 = e2.a.b - e1.b.b;
        } else if (e1.a.equals(e2.b)) {
            x1 = e1.b.a - e1.a.a;
            x2 = e2.a.a - e1.a.a;
            y1 = e1.b.b - e1.a.b;
            y2 = e2.a.b - e1.a.b;
        } else if (e1.b.equals(e2.a)) {
            x1 = e1.a.a - e1.b.a;
            x2 = e2.b.a - e1.b.a;
            y1 = e1.a.b - e1.b.b;
            y2 = e2.b.b - e1.b.b;
        } else {
            return -1;
        }

        double dot = x1 * x2 + y1 * y2;
        double squaredLength = e1.length * e2.length;
        dot = dot / (squaredLength);
        double det = x1 * y2 - y1 * x2;
        det = det / (squaredLength);

        double angle = Math.atan2(det, dot);
        if(angle < 0){
            return Math.PI - angle;
        }
        return angle;
    }

    @Override
    public int hashCode(){
        return this.a.ID_NUMBER*751+this.b.ID_NUMBER;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Edge){
            Edge e = (Edge)obj;
            if(e.a.ID_NUMBER == this.a.ID_NUMBER && e.b.ID_NUMBER == this.b.ID_NUMBER){
                return true;
            }
            if(e.a.ID_NUMBER == this.b.ID_NUMBER && e.b.ID_NUMBER == this.a.ID_NUMBER){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString(){
        return a.toString() + "-" + b.toString();
    }
}

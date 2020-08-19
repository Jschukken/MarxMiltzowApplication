package DataTypes;

/**
 * a triangle
 *
 * @author Jelle Schukken
 */
public class Triangle {

    /**
     * returns true if point p is contained in the triangle formed by t1-t3. False otherwise
     */
    public static boolean contains(Point t1, Point t2, Point t3, Point p){
        if (p.ID_NUMBER == t1.ID_NUMBER || p.ID_NUMBER == t2.ID_NUMBER || p.ID_NUMBER == t3.ID_NUMBER) {
            return false;
        }
        float d1, d2, d3;
        boolean has_neg, has_pos;

        d1 = sign(p, t1, t2);
        d2 = sign(p, t2, t3);
        d3 = sign(p, t3, t1);

        has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(has_neg && has_pos);
    }

    private static float sign(Point p1, Point p2, Point p3) {
        return (p1.a - p3.a) * (p2.b - p3.b) - (p2.a - p3.a) * (p1.b - p3.b);
    }
}

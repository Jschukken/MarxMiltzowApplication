package DataTypes;

import java.util.ArrayList;

/**
 * a point object with unique order label
 *
 * @author Jelle Schukken
 */
public class Point {

    private static int ID = 0;

    public float a,b;
    public final int ID_NUMBER;
    public int index = -1;

    public Point(float a, float b){
        ID_NUMBER = ID;
        ID++;
        this.a = a;
        this.b = b;
    }

    @Override
    public int hashCode() {
        return ID_NUMBER;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Point){
            Point p = (Point) obj;
            if(p.ID_NUMBER == this.ID_NUMBER){
                return true;
            }
        }
        return false;
    }

    /**
     * returns distance between this point and point p
     * @param p the point
     * @return the distance between this point and point p
     */
    public double distance(Point p){
        return Math.sqrt((a-p.a)*(a-p.a) + (b-p.b)*(b-p.b));
    }

    /**
     * clears the indexes of points in points
     */
    public static void clearIndexes(ArrayList<Point> points){
        for(Point p: points){
            p.index = -1;
        }
    }

    /**
     * sets the index of all points in points to index
     */
    public static void setIndexes(ArrayList<Point> points, int index){
        for(Point p: points){
            p.index = index;
        }
    }

    @Override
    public String toString(){
        return "("+a+", " + b +")";
    }
}

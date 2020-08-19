package DataTypes;

import java.awt.*;
import java.util.ArrayList;

/**
 * a wrapper around Polygon2D for Point data type
 *
 * @author Jelle Schukken
 */
public class CactusPolygon extends Polygon2D {

    ArrayList<Point> points;

    public CactusPolygon(ArrayList<Point> points){
        super();
        for(int i = 0; i < points.size(); i++){
            this.addPoint(points.get(i).a, points.get(i).b);
        }
        this.points = points;
    }

    public boolean contains(Point point){
        return this.contains(point.a,point.b);
    }

    public boolean hasVertex(Point p) {
        return points.contains(p);
    }

    public boolean incident(Edge e){
        for(int i = 0; i < points.size(); i++){
            if(points.get(i).equals(e.a) && points.get((i+1)%points.size()).equals(e.b)){
                return true;
            }
            if(points.get(i).equals(e.b) && points.get((i+1)%points.size()).equals(e.a)){
                return true;
            }
        }
        return false;
    }

    public ArrayList<Point> getPoints(){
        return points;
    }
}

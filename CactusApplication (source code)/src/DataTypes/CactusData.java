package DataTypes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * maintains data for cactus algorithm. Notably, contains the dynamic programming databases and the original point set.
 *
 * @author Jelle Schukken
 */
public class CactusData {

    public ArrayList<Point> points = new ArrayList<>();
    public int pointWidth = 0;
    public int pointHeight = 0;
    public boolean outOfTime = false;
    public static final HashMap<String,HashMap<String, NibbledRingData>> NibbledRingDataBase = new HashMap<>();
    public static final HashMap<String, LayerRingData> LayerRingDataBase = new HashMap<>();


    /**
     * computes and returns the convex hull of points in this class
     * @return the convex hull
     */
    public ArrayList<Point> getConvexHull(){

        ArrayList<Point> points = new ArrayList<>();

        for(Point p: this.points)
        {
            points.add(p);
        }

        ArrayList<Point> convexHull = new ArrayList<>();
        points.sort(new Comparator<Point>(){
            @Override
            public int compare(Point p1, Point p2){
                if(p1.a > p2.a){
                    return 1;
                }else if(p1.a < p2.a){
                    return -1;
                }
                return 0;
            }
        });
        convexHull.add(points.get(0));

        for(int i = 1; i < points.size();){
            if(convexHull.size() == 1){
                convexHull.add(points.get(i));
                i++;
            }else {
                Edge edge1 = new Edge(convexHull.get(convexHull.size() - 2), convexHull.get(convexHull.size() - 1));
                Edge edge2 = new Edge(convexHull.get(convexHull.size() - 1), points.get(i));
                if (Edge.getAngle(edge1, edge2) > Math.PI) {
                    convexHull.remove(convexHull.size() - 1);
                } else {
                    convexHull.add(points.get(i));
                    i++;
                }
            }
        }

        int upperSize = convexHull.size()-1;


        for(int i = points.size()-2; i >= 0;){
            if(i != 0 && convexHull.contains(points.get(i))){
                i--;
            }else if(convexHull.size() - upperSize == 1){
                convexHull.add(points.get(i));
                i--;
            }else {
                Edge edge1 = new Edge(convexHull.get(convexHull.size() - 2), convexHull.get(convexHull.size() - 1));
                Edge edge2 = new Edge(convexHull.get(convexHull.size() - 1), points.get(i));
                if (Edge.getAngle(edge1, edge2) > Math.PI) {
                    convexHull.remove(convexHull.size() - 1);
                } else {
                    convexHull.add(points.get(i));
                    i--;
                }
            }
        }

        convexHull.remove(convexHull.size()-1);
        return convexHull;

    }

}

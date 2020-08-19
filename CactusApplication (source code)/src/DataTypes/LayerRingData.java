package DataTypes;

import Algorithm.SumProduct;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

/**
 * an instance of a general unconstrained-layer ring sub-problem
 *
 * @author Jelle Schukken
 */
public class LayerRingData {

    public ArrayList<Point> outerLayer;
    public ArrayList<Point> freePoints;
    public int outerIndex;

    private SumProduct triangulationCount = null;

    public static ArrayList<String> layerPointIDs = new ArrayList<>();

    private String ID = null;

    /**
     * constructor
     * @param outerLayer the outer layer
     * @param freePoints the set of free points
     * @param outerIndex the layer index of the outer layer
     */
    public LayerRingData(ArrayList<Point> outerLayer, ArrayList<Point> freePoints, int outerIndex){
        this.outerIndex = outerIndex;

        this.outerLayer = outerLayer;
        this.freePoints = freePoints;
    }

    public void setSumProdct(SumProduct sumProdct){
        triangulationCount = sumProdct;
    }

    public int getTotalPoints(){
        HashSet<Point> points = new HashSet<>();
        for(Point p: outerLayer){
            points.add(p);
        }
        for(Point p: freePoints){
            points.add(p);
        }
        return points.size();
    }

    /**
     * generate a unique identifier string for a general unconstrained-layer ring sub-problem
     * @return a unique identifier String of this sub-problem
     */
    public String getStringID(){
        if(ID == null) {
            String id = "" + outerIndex;

            for (Point p : outerLayer) {
                id += p.ID_NUMBER;
            }

            for(String s: layerPointIDs){
                if(s.equals(id)){
                    ID = s;
                    return s;
                }
            }
            layerPointIDs.add(id);
            ID = id;
        }

        return ID;

    }

    public SumProduct getCount(){
        return triangulationCount;
    }
}

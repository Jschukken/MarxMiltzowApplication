package Algorithm;

import DataTypes.CactusData;
import DataTypes.LayerRingData;
import DataTypes.NibbledRingData;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Optional;


/**
 * an equation representing the number of triangulations of a general layer-unconstrained ring sub-problem
 * in terms of its sub-problems
 *
 * @author Jelle Schukken
 */
public class LayerRingSumProduct extends SumProduct {

    private ArrayList<ArrayList<String>> s1 = new ArrayList<>();//layer ring problems
    private ArrayList<ArrayList<String>> s2 = new ArrayList<>();//nibbled ring problems

    public LayerRingSumProduct(ArrayList<ArrayList<String>> s1, ArrayList<ArrayList<String>> s2) {
        this.s1 = s1;
        this.s2 = s2;
    }

    @Override
    public void setValue(int value) {
        this.value = Optional.of(value);
    }

    @Override
    public Optional<Integer> getValue(CactusData data) {
        if (!value.isPresent()) {
            //check if dependents are present
            if (canSolve(data)) {
                solve(data);
            }
        }
        return value;
    }

    private boolean canSolve(CactusData data) {
        for (ArrayList<String> list : s1) {
            for (String s : list) {
                if (!data.LayerRingDataBase.get(s).getCount().isPresent())
                    return false;
            }
        }
        for (ArrayList<String> list : s2) {
            for (int i = 0; i < list.size(); i += 2) {
                if (!data.NibbledRingDataBase.get(list.get(i)).get(list.get(i + 1)).getCount().isPresent()) {
                    return false;
                }
            }
        }

        return true;
    }

    private void solve(CactusData data) {
        int s1SumProduct = 0;
        int s2SumProduct = 0;
        ArrayList<String> list;
        for (int j = 0; j < s1.size(); j++) {
            list = s2.get(j);
            s2SumProduct = 0;
            for (int i = 0; i < list.size(); i += 2) {
                s2SumProduct += data.NibbledRingDataBase.get(list.get(i)).get(list.get(i + 1)).getCount().get();
            }
            int s1Product = 1;
            for (String s : s1.get(j)) {
                s1Product *= data.LayerRingDataBase.get(s).getCount().get();
            }
            s1SumProduct += s1Product * s2SumProduct;
        }


        value = Optional.of(s1SumProduct);
    }
    @Override
    public Pair<ArrayList<NibbledRingData>, ArrayList<LayerRingData>> getSubproblems(CactusData data) {
        ArrayList<NibbledRingData> nrd= new ArrayList<>();
        ArrayList<LayerRingData> lrd = new ArrayList<>();
        for(ArrayList<String> list : s2) {
            for (int i = 0; i < list.size(); i += 2) {
                nrd.add(data.NibbledRingDataBase.get(list.get(i)).get(list.get(i + 1)));
            }
        }

        for(ArrayList<String> list2 : s1) {
            for (int i = 0; i < list2.size(); i ++) {
                lrd.add(data.LayerRingDataBase.get(list2.get(i)));
            }
        }

        return new Pair<ArrayList<NibbledRingData>, ArrayList<LayerRingData>>(nrd, lrd);
    }

    @Override
    public Pair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> getSubproblemIDs(CactusData data){
        return new Pair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>(s2, s1);
    }

}

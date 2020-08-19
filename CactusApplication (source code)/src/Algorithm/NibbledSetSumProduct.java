package Algorithm;

import DataTypes.CactusData;
import DataTypes.LayerRingData;
import DataTypes.NibbledRingData;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Optional;

/**
 * an equation representing the number of triangulations of a set of nibbled ring sub-problems
 * in terms of those sub-problems
 *
 * @author Jelle Schukken
 */
public class NibbledSetSumProduct extends SumProduct{

    private ArrayList<String> problems = new ArrayList<>();

    public NibbledSetSumProduct(ArrayList<String> problems){
        this.problems = problems;
        if(problems.size()== 0){
            value = Optional.of(0);
        }
    }

    @Override
    public void setValue(int value){
        this.value = Optional.of(value);
    }

    @Override
    public Optional<Integer> getValue(CactusData data){
        if(!value.isPresent()){
            //check if dependents are present
            if(canSolve(data)){
                solve(data);
            }
        }
        return value;
    }

    private boolean canSolve(CactusData data){
        for(int i = 0; i < problems.size(); i+=2){
            if(!data.NibbledRingDataBase.get(problems.get(i)).get(problems.get(i+1)).getCount().isPresent())
                return false;
        }
        return true;
    }

    private void solve(CactusData data){
        int sumProduct = 0;
        for(int i = 0; i < problems.size(); i+=2){
            int problem1 = data.NibbledRingDataBase.get(problems.get(i)).get(problems.get(i+1)).getCount().get();
            sumProduct += problem1;
        }

        value = Optional.of(sumProduct);
    }

    @Override
    public Pair<ArrayList<NibbledRingData>, ArrayList<LayerRingData>> getSubproblems(CactusData data) {
        ArrayList<NibbledRingData> nrd= new ArrayList<>();
        ArrayList<LayerRingData> lrd = new ArrayList<>();
        for(int i = 0; i < problems.size(); i+=2){
            nrd.add(data.NibbledRingDataBase.get(problems.get(i)).get(problems.get(i+1)));
        }
        return new Pair<ArrayList<NibbledRingData>, ArrayList<LayerRingData>>(nrd, lrd);
    }

    @Override
    public Pair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> getSubproblemIDs(CactusData data){
        ArrayList<ArrayList<String>> list = new ArrayList<>();
        list.add(problems);
        return new Pair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>(list, new ArrayList<ArrayList<String>>());
    }

}

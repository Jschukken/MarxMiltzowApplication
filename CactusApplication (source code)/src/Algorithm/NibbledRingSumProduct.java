package Algorithm;

import DataTypes.CactusData;
import DataTypes.LayerRingData;
import DataTypes.NibbledRingData;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Optional;

/**
 * This class represents a blueprint for computing the solution to a nibbled ring problem
 * using the solutions its sub-problems.
 *
 * @author Jelle Schukken
 */
public class NibbledRingSumProduct extends SumProduct{

    private ArrayList<String> problems = new ArrayList<>();

    public NibbledRingSumProduct(ArrayList<String> problems){
        this.problems = problems;
        if(problems.size()== 0){
            value = Optional.of(0);
        }
    }

    public NibbledRingSumProduct(int value){
        this.value = Optional.of(value);
    }

    @Override
    public void setValue(int value){
        this.value = Optional.of(value);
    }

    @Override
    public Pair<ArrayList<NibbledRingData>, ArrayList<LayerRingData>> getSubproblems(CactusData data) {
        ArrayList<NibbledRingData> nrd= new ArrayList<>();
        ArrayList<LayerRingData> lrd = new ArrayList<>();
        for(int i = 0; i < problems.size()-1; i+=2){
            nrd.add(data.NibbledRingDataBase.get(problems.get(i)).get(problems.get(i+1)));
        }
        return new Pair<>(nrd, lrd);
    }

    @Override
    public Pair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> getSubproblemIDs(CactusData data){
        ArrayList<ArrayList<String>> list = new ArrayList<>();
        list.add(problems);
        return new Pair<>(list, new ArrayList<>());
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
        for(int i = 0; i < problems.size(); i+=4){
            if(!data.NibbledRingDataBase.get(problems.get(i)).get(problems.get(i+1)).getCount().isPresent())
                return false;
            if(!data.NibbledRingDataBase.get(problems.get(i+2)).get(problems.get(i+3)).getCount().isPresent())
                return false;
        }
        return true;
    }

    private void solve(CactusData data){
        int sumProduct = 0;
        for(int i = 0; i < problems.size(); i+=4){
            int problem1 = data.NibbledRingDataBase.get(problems.get(i)).get(problems.get(i+1)).getCount().get();
            int problem2 = data.NibbledRingDataBase.get(problems.get(i+2)).get(problems.get(i+3)).getCount().get();
            sumProduct += problem1*problem2;
        }

        value = Optional.of(sumProduct);
    }
}

package Algorithm;

import DataTypes.CactusData;
import DataTypes.LayerRingData;
import DataTypes.NibbledRingData;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Optional;

/**
 * a equation representing the number of triangulations of problem in terms of its sub-problems
 *
 * @author Jelle Schukken
 */
public abstract class SumProduct {

    protected Optional<Integer> value = Optional.empty();

    /**
     * solves equation if possible and return the result
     * @param data the datapacket containing the databases of solutions
     * @return the value of the equation if computed otherwise return missing optional
     */
    public abstract Optional<Integer> getValue(CactusData data);

    /**
     * gets result of equation
     * @return value of optional
     */
    public Integer get(){
        return value.get();
    }

    /**
     * checks if equation has been solved
     * @return true if it has, false otherwise
     */
    public boolean isPresent(){
        return value.isPresent();
    }

    /**
     * sets result of equation. useful for basecases
     * @param value the value to set the result to
     */
    public void setValue(int value){
        this.value = Optional.of(value);
    }

    /**
     * @returns a part of lists. One list of all the nibbled ring subproblems
     * and one list of all the general unconstrained-layer ring sub-problems
     * @param data the datapacket containing the databases of sub-problems
     */
    public abstract Pair<ArrayList<NibbledRingData>, ArrayList<LayerRingData>> getSubproblems(CactusData data);

    /**
     * @returns a part of lists. One list of all the string identifiers of the nibbled ring sub-problems
     * and one list of all the string identifiers of the general unconstrained-layer ring sub-problems
     * @param data the datapacket containing the databases of sub-problems
     */
    public abstract Pair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> getSubproblemIDs(CactusData data);

}

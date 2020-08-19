package DataTypes;

import java.util.ArrayList;

/**
 * a vector of layer constraints
 *
 * @author Jelle Schukken
 */
public class ConstraintVector<T> extends ArrayList<T> {

    private static ArrayList<String> constraintStrings = new ArrayList<>();

    /**
     * Computes the constraint vector for the right nibbled ring sub-problem
     * @param c1 the constraint vector of the left nibbled ring sub-problem
     * @param c the constraint vector of the parent problem
     * @param cp the indicator vector for shared points
     * @return the constraint vector of the right nibbled ring sub-problem
     */
    public static ConstraintVector<Integer> getC2(ConstraintVector<Integer> c1, ConstraintVector<Integer> c, ConstraintVector<Integer> cp) {
        ConstraintVector<Integer> c2 = new ConstraintVector<Integer>();
        for (int i = 0; i < c.size(); i++) {
            c2.add(c.get(i) + cp.get(i) - c1.get(i));
        }
        return c2;
    }

    /**
     * Generates all constraint vectors for the left nibbled ring sub-problem
     * @param c the constraints of the current problem
     * @param cp the indicator vector of shared points
     * @return a list of constraint vectors
     */
    public static ArrayList<ConstraintVector<Integer>> getAllC1(ConstraintVector<Integer> c, ConstraintVector<Integer> cp) {

        ArrayList<ConstraintVector<Integer>> c1List = new ArrayList<>();
        ConstraintVector<Integer> workVector = new ConstraintVector<>();

        for (int i = 0; i < c.size(); i++) {
            workVector.add(0);
        }

        for (int i = c.size() - 1; i < c.size(); ) {
            if (i == -1) {
                return c1List;
            } else if ((c.get(i) + cp.get(i)) == 0) {
                i--;
            } else {
                workVector.set(i, (workVector.get(i) + 1) % (c.get(i) + cp.get(i) + 1));
                if (workVector.get(i) == 0) {
                    i--;
                } else {
                    if (i + 1 != c.size()) {
                        i++;
                    }
                    ConstraintVector<Integer> c1 = new ConstraintVector<>();
                    boolean foundNum = false;
                    boolean foundZero = false;
                    boolean possible = true;
                    for (int k = 0; k < workVector.size(); k++) {
                        Integer n = workVector.get(k);
                        if(n > c.get(k)){
                            possible = false;
                            break;
                        }
                        if(n < cp.get(k)){
                            possible = false;
                            break;
                        }

                        if(!foundNum && n > 0) { //first non empty layer
                            foundNum = true;
                        }else if(foundNum && !foundZero && n == 0) { // first empty layer after a non empty layer
                            foundZero = true;
                        }else if(foundZero && n > 0){ // must be a zero size layer in the middle of non zero layers
                            possible = false;
                            break;
                        }

                        c1.add(new Integer(n));

                    }
                    if(possible)
                        c1List.add(c1);
                }

            }
        }

        return c1List;
    }

    /**
     * creates an indicator vector from a data packet and a shared path
     * @param data the data packet
     * @param path the shared path
     * @return the relevant indicator vector
     */
    public static ConstraintVector<Integer> convertToIndicatorVector(CactusData data, ArrayList<Point> path) {
        ConstraintVector<Integer> cp = new ConstraintVector<>();
        for(int i = 0; i < data.points.size();i++){
            if(i >= path.get(path.size()-1).index && i <= path.get(0).index){
                cp.add(1);
            }else{
                cp.add(0);
            }
        }
        return cp;
    }

    /**
     * turns this constraint vector into a string.
     * If this string has been generated before return a reference to that string instead.
     * @return a string representation of this constraint vector
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for(T t: this){
            s.append(t);
            s.append(',');
        }
        String ss = s.toString();

        for(String cs: constraintStrings){
            if(cs.equals(ss)){
                return cs;
            }
        }

        constraintStrings.add(ss);
        return ss;
    }

    /**
     * turn a constraint vector into a string for display
     * @return a string representing this constraint vector
     */
    public String display(){
        StringBuilder s = new StringBuilder();
        s.append('[');
        for(T t: this){
            s.append(t);
            s.append(", ");
        }
        s.deleteCharAt(s.length()-1);
        s.deleteCharAt(s.length()-1);
        s.append(']');
        return s.toString();
    }
}

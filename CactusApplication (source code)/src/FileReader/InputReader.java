package FileReader;

import DataTypes.Point;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Static class
 * Reads input from file and returns the point set it represents, reports any errors to the consul.
 *
 * @author Jelle Schukken
 */
public class InputReader {

    public static ArrayList<Point> getPoints(String fileName) {

        File file = new File(fileName+".txt");
        ArrayList<Point> points= new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String st;
            float float1;
            float float2;
            while ((st = br.readLine()) != null) {
                float1 = Float.parseFloat(st.substring(0, st.indexOf(' ')));
                float2 = Float.parseFloat(st.substring(st.indexOf(' ')+1));
                points.add(new Point(float1,float2));
            }
            br.close();
        } catch (IOException e){
            file = new File("resources\\" + fileName+".txt");
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));

                String st;
                float float1;
                float float2;
                while ((st = br.readLine()) != null) {
                    float1 = Float.parseFloat(st.substring(0, st.indexOf(' ')));
                    float2 = Float.parseFloat(st.substring(st.indexOf(' ')+1));
                    points.add(new Point(float1,float2));
                }
                br.close();
            } catch (IOException e2) {
                System.out.println("cannot find file");
            }
        }
        return points;
    }
}

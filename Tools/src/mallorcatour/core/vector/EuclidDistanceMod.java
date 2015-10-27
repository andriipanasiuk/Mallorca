/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.vector;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class EuclidDistanceMod extends EuclidDistance {

    private int[] columns;

    public EuclidDistanceMod(int... columns) {
        if (columns == null) {
            throw new NullPointerException();
        }
        this.columns = columns;
    }

    @Override
    public double getDistance(IVector vector1, IVector vector2) {
        if (vector1.getValues().size() != vector2.getValues().size()) {
            throw new IllegalArgumentException("Dimensions of vectors must be equals!");
        }
        List<Number> newVector1 = new ArrayList<Number>();
        List<Number> newVector2 = new ArrayList<Number>();
        for (int column : columns) {
            newVector1.add(vector1.getValues().get(column));
            newVector2.add(vector2.getValues().get(column));
        }
        return super.getDistance(new BaseVector(newVector1), new BaseVector(newVector2));
    }
}

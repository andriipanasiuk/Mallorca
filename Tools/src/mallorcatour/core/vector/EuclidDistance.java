/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.vector;


/**
 *
 * @author Andrew
 */
public class EuclidDistance implements IDistanceCalculator<IVector> {

   
    public EuclidDistance() {
    }

   
    public double getDistance(IVector vector1, IVector vector2) {
        double result = 0;
        if (vector1.getValues().size() != vector2.getValues().size()) {
            throw new IllegalArgumentException("Dimensions of vectors must be equals!");
        }
        for (int i = 0; i < vector1.getValues().size(); i++) {
            result += Math.pow(vector1.getValues().get(i).doubleValue()
                    - vector2.getValues().get(i).doubleValue(), 2);
        }
        result = Math.sqrt(result);
        return result;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.neural.core;

import mallorcatour.core.vector.IInputInterpreter;
import mallorcatour.core.vector.IVector;

/**
 *
 * @author Andrew
 */
public class VectorInterpreter implements IInputInterpreter<IVector> {

    private boolean changeToZeros = false;

    public VectorInterpreter(boolean changeToZeros) {
        this.changeToZeros = changeToZeros;
    }

    public double[] createInput(IVector object) {
        int i = 0;
        double[] result = new double[object.getValues().size()];
        for (Number number : object.getValues()) {
            double value = number.doubleValue();
            if (value < 0 && changeToZeros) {
                value = 0;
            }
            result[i++] = value;
        }
        return result;
    }
}

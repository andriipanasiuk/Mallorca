/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.neuronetworkwrapper;

import java.util.ArrayList;
import java.util.List;
import mallorcatour.vector.IVector;

/**
 *
 * @author Andrew
 */
public class BaseVector implements IVector {

    public static final String NUMBER_SEPARATOR = ",";
    private List<Number> values;

    public BaseVector(List<Number> values) {
        this.values = values;
    }

    public BaseVector(double... values) {
        this.values = new ArrayList<Number>();
        for (double value : values) {
            this.values.add(value);
        }
    }

    public List<Number> getValues() {
        return values;
    }

    public static IVector valueOf(String vector) {
        final List<Number> result = new ArrayList<Number>();
        String[] numbers = vector.split(NUMBER_SEPARATOR);
        for (String number : numbers) {
            result.add(Double.parseDouble(number));
        }
        return new IVector() {

            public List<Number> getValues() {
                return result;
            }
        };
    }
}

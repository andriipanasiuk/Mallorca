/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.vector;

import java.util.List;

/**
 * Interface class that represents vector of values for neuro network(NN).
 * This vector can be used as a learning example for NN.
 * @author Andrew
 */
public interface IVector {

    /**
     * @return list of numbers from 0 to 1.
     */
    List<Number> getValues();
}

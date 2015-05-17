/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.interfaces;

/**
 * Interface for creating input vector for neural network from object of class T.
 * 
 * @author Andrew
 */
public interface IInputInterpreter<T> {

    double[] createInput(T object);
}

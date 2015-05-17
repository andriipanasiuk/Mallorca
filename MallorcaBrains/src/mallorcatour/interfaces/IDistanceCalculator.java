/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.interfaces;

/**
 *
 * @author Andrew
 */
public interface IDistanceCalculator<T> {

    double getDistance(T one, T other);
}

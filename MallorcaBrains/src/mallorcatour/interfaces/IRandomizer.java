/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.interfaces;

/**
 *
 * @author Andrew
 */
public interface IRandomizer {

    //random number from 0 to 1
    public double getRandom();

    //random number from a to b
    public double getRandom(double a, double b);

    public int getRandom(int a, int b);

    public double getGaussRandom(double a, double b);
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.util;

import java.util.Random;
import mallorcatour.interfaces.IRandomizer;

/**
 *
 * @author Andrew
 */
public class UniformRandomizer implements IRandomizer {

    private static Random random = new Random(System.nanoTime());

    public double getRandom() {
        return random.nextDouble();
    }

    public double getRandom(double a, double b) {
        if (b < a) {
            throw new IllegalArgumentException("Second argument must be bigger "
                    + "than the first!");
        }
        return a + (b - a) * random.nextDouble();
    }

    public int getRandom(int a, int b) {
        if (b < a) {
            throw new IllegalArgumentException("Second argument must be bigger "
                    + "than the first!");
        }
        return a + random.nextInt(b - a);
    }

    public double getGaussRandom(double a, double b) {
        if (b < a) {
            throw new IllegalArgumentException("Second argument must be bigger "
                    + "than the first!");
        }
        double middle = (a + b) / 2;
        double half = b - middle;
        double result = middle + random.nextGaussian() / 3 * half;
        if (result < a) {
            result = a;
        } else if (result >= b) {
            result = b - 0.0000001;
        }
        return result;
    }

}

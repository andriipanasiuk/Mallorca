/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.robot.recognizer;

import java.awt.Color;
import mallorcatour.interfaces.IDistanceCalculator;

/**
 *
 * @author Andrew
 */
public class ColorDistanceCalculator implements IDistanceCalculator<Color> {

    public double getDistance(Color one, Color other) {
        double result = 0;
        result += Math.pow(one.getRed() - other.getRed(), 2);
        result += Math.pow(one.getGreen() - other.getGreen(), 2);
        result += Math.pow(one.getBlue() - other.getBlue(), 2);
        return Math.sqrt(result);
    }
}

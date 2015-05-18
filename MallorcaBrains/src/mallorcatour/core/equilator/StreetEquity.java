/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.equilator;

/**
 *
 * @author Andrew
 */
public class StreetEquity {

    public double strength;
    public double draw;
    public double positivePotential;
    public double negativePotential;

    public static double calculateRealEquity(StreetEquity equity) {
        double realEquity = (equity.strength - equity.draw / 2) * (1 - equity.negativePotential)
                + (1 - equity.strength - equity.draw / 2) * equity.positivePotential;
        return realEquity;
    }

    public static double calculateRealEquity(double strength, double positive, double negative) {
        double realEquity = (strength) * (1 - negative) + (1 - strength) * positive;
        return realEquity;
    }
}

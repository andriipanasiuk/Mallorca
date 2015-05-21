/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.equilator;

import mallorcatour.util.DoubleUtils;

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

	@Override
	public String toString() {
		return "Strength: " + strength + "\n" + "Positive: " + positivePotential + "\nNegative: " + negativePotential;
	}

	public double winWillWin() {
		return DoubleUtils.digitsAfterComma(strength * (1 - negativePotential), 2);
	}

	public double winWillLose() {
		return DoubleUtils.digitsAfterComma(strength * negativePotential, 2);
	}

	public double loseWillWin() {
		return DoubleUtils.digitsAfterComma((1 - strength) * (positivePotential), 2);
	}

	public double loseWillLose() {
		return DoubleUtils.digitsAfterComma((1 - strength) * (1 - positivePotential), 2);
	}
}

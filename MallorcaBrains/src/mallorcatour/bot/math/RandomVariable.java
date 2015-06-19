package mallorcatour.bot.math;

import java.util.ArrayList;
import java.util.Map.Entry;

import mallorcatour.core.game.Action;
import mallorcatour.tools.DoubleUtils;
import mallorcatour.tools.Pair;

public class RandomVariable extends ArrayList<Pair<Double, Double>> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3513507226067514138L;
	private Double ev = null;
	private Double variance = null;

	public double getEV() {
		if (ev == null) {
			ev = calculateEV(this);
		}
		return ev;
	}

	public double getVariance() {
		if (variance == null) {
			variance = calculateVariance(this);
		}
		return variance;
	}

	public static RandomVariable create(double constant) {
		RandomVariable result = new RandomVariable();
		result.ev = constant;
		result.variance = 0d;
		result.add(1d, constant);
		return result;
	}

	public void add(double probability, double value) {
		add(new Pair<Double, Double>(probability, value));
	}

	public void add(double probability, RandomVariable variable) {
		for (Pair<Double, Double> pair : variable) {
			add(new Pair<Double, Double>(probability * pair.first, pair.second));
		}
	}

	public static double calculateEV(RandomVariable variable) {
		double result = 0;
		for (Pair<Double, Double> pair : variable) {
			result += pair.first * pair.second;
		}
		return result;
	}

	public void multiply(double value) {
		for (Pair<Double, Double> pair : this) {
			pair.first *= value;
		}
	}

	private static double calculateVariance(RandomVariable variable) {
		double ev = variable.getEV();
		double result = 0;
		for (Pair<Double, Double> pair : variable) {
			result += pair.first * Math.pow(pair.second - ev, 2);
		}
		return Math.sqrt(result);
	}

	@Override
	public String toString() {
		return "(ev " + DoubleUtils.digitsAfterComma(getEV(), 2) + " var "
				+ DoubleUtils.digitsAfterComma(getVariance(), 2) + " diff "
				+ DoubleUtils.digitsAfterComma(getEV() - getVariance(), 2) + " count " + size() + ")";
	}

	public String printProfitability(double investment) {
		return "(prftblty: " + (investment != 0 ? (DoubleUtils.digitsAfterComma(getEV() / investment, 2)) : "inf")
				+ " ev " + DoubleUtils.digitsAfterComma(getEV(), 2) + " var "
				+ DoubleUtils.digitsAfterComma(getVariance(), 2) + " diff "
				+ DoubleUtils.digitsAfterComma(getEV() - getVariance(), 2) + ")";
	}

	public double boundary() {
		return 1.65 * getVariance() / Math.sqrt(size());
	}

	public static RandomVariable maxEV(ActionDistribution actionDistribution) {
		double maxValue = -Double.MAX_VALUE;
		RandomVariable result = null;
		for (Entry<Action, RandomVariable> entry : actionDistribution.entrySet()) {
			RandomVariable item = entry.getValue();
			double ev = item.getEV();
			if (ev > maxValue) {
				result = item;
				maxValue = ev;
			}
		}
		if (result == null) {
			throw new RuntimeException();
		}
		return result;
	}

}
package mallorcatour.core.math;

import java.util.ArrayList;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.interfaces.IGameInfo;
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

	private double sum() {
		double sum = 0;
		for (Pair<Double, Double> pair : this) {
			sum += pair.first;
		}
		return sum;
	}

	public void normalize() {
		double sum = sum();
		for (Pair<Double, Double> pair : this) {
			pair.first = pair.first / sum;
		}
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
				+ DoubleUtils.digitsAfterComma(getVariance(), 2)
				+ " count " + size() + ")";
	}

	public String printProfitability(Action action, double ES, double bb) {
//		double investment = 0;
//		if (action.isPassive()) {
//			investment += action.getAmountToCall();
//		} else if (action.isAggressive()) {
//			investment += action.getAmountToCall();
//			investment += action.getAmount();
//		}
		return printProfitability(ES, bb);
	}

	public String printProfitability(IGameInfo gameInfo) {
		return printProfitability(gameInfo.getBankRollAtRisk(), gameInfo.getBigBlindSize());
	}

	public String printProfitability(double heroES, double bb) {
		StringBuilder builder = new StringBuilder();
		builder.append("(");
		builder.append("EV/BB: ");
		builder.append(DoubleUtils.digitsAfterComma(getEV() / bb, 2));
		builder.append(" EV/Var: ");
		builder.append(DoubleUtils.digitsAfterComma(getEV() / getVariance(), 2));
		builder.append(" Var/BB: ");
		builder.append(DoubleUtils.digitsAfterComma(getVariance() / bb, 2));
		builder.append(" EV: ");
		builder.append(DoubleUtils.digitsAfterComma(getEV(), 2));
		builder.append(" Var: ");
		builder.append(DoubleUtils.digitsAfterComma(getVariance(), 2));
		builder.append(")");
		return builder.toString();
	}

}
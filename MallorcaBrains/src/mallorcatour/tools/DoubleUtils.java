package mallorcatour.tools;

public class DoubleUtils {
	private static final double TEN = 10.0D;

	public static double digitsAfterComma(double value, int digitsAfterComma) {
		if (Double.isNaN(value)) {
			return Double.NaN;
		}
		double powerOfTen = Math.pow(TEN, digitsAfterComma);
		double result = (int) (value * powerOfTen) / powerOfTen;
		return result;
	}
}

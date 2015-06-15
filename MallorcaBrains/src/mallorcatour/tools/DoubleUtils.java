package mallorcatour.tools;

public class DoubleUtils {
	private static final double TEN = 10.0D;

	public static double digitsAfterComma(double value, int digitsAfterComma) {
		double powerOfTen = Math.pow(TEN, digitsAfterComma);
		double result = (int) (value * powerOfTen) / powerOfTen;
		return result;
	}
}

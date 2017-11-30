package mallorcatour.equilator.preflop.generator;

import mallorcatour.tools.DoubleUtils;
import mallorcatour.tools.MyFileWriter;

public class GeneratorUtils {

	public static void doubleArrayToFile(double[][] array, String arrayName, int sizex, int sizey) {
		MyFileWriter writer = MyFileWriter.prepareForWriting("PreflopGenerated.java", false);
		writer.addToFile("class PreflopGenerated {", true);
		writer.addToFile("static double[][] " + arrayName + " = new double[" + sizex + "][" + sizey + "];", true);
		int methodCount = 0;
		for (int i = 0; i < array.length; i++) {
			if (i % 10 == 0) {
				writer.addToFile("private static void method" + (i / 10 + 1) + "() {", true);
				methodCount++;
			}
			writer.addToFile(arrayName + "[" + i + "] = new double[] {", true);
			for (int j = 0; j < array[i].length; j++) {
				writer.addToFile(String.valueOf(DoubleUtils.digitsAfterComma(array[i][j], 2)), false);
				if (j != array[i].length - 1) {
					writer.addToFile(",", false);
				}
			}
			writer.addToFile("};\n", false);
			if (i % 10 == 9 || i == array.length - 1) {
				writer.addToFile("}", true);
			}
		}

		writer.addToFile("static {", true);
		for (int i = 1; i <= methodCount; i++) {
			writer.addToFile("method" + i + "();", true);
		}
		writer.addToFile("}", true);
		writer.addToFile("}", true);
		writer.endWriting();
	}

	public static void main(String... args) {
		double [][] preflopStrength = PreflopTableGenerator.generatePreflopTableStrengthArray();
		doubleArrayToFile(preflopStrength, "preflopStrength", 170, 170);
	}
}
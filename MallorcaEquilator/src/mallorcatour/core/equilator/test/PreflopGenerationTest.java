package mallorcatour.core.equilator.test;

import mallorcatour.core.equilator.preflop.EquilatorPreflop;
import mallorcatour.core.equilator.preflop.PreflopStrengthTable;

public class PreflopGenerationTest {

	static void test() {
		for (int i = 0; i < 170; i++) {
			for (int j = 0; j < 170; j++) {
				int file = (int) (EquilatorPreflop.preflopStrength[i][j] * 100);
				int code = (int) (PreflopStrengthTable.array[i][j] * 100);
				if (Math.abs(file - code) > 1) {
					throw new RuntimeException(EquilatorPreflop.preflopStrength[i][j] + " "
							+ PreflopStrengthTable.array[i][j] + " " + file + " " + code);
				}
			}
		}
	}

	public static void main(String... args) {
		test();
	}
}

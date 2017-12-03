package mallorcatour.equilator;

import mallorcatour.equilator.preflop.PreflopEquilatorImpl;
import mallorcatour.equilator.preflop.PreflopStrengthTable;

public class PreflopGenerationTest {

    private static PreflopEquilatorImpl preflopEquilator = new PreflopEquilatorImpl();

    public static void test() {
        for (int i = 0; i < 170; i++) {
            for (int j = 0; j < 170; j++) {
                int file = (int) (preflopEquilator.preflopStrength[i][j] * 100);
                int code = (int) (PreflopStrengthTable.array[i][j] * 100);
                if (Math.abs(file - code) > 1) {
                    throw new RuntimeException(preflopEquilator.preflopStrength[i][j] + " "
                            + PreflopStrengthTable.array[i][j] + " " + file + " " + code);
                }
            }
        }
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.preflop;

import mallorcatour.util.Log;

/**
 *
 * @author Andrew
 */
public class PreflopBotTest {

    private static void test1() {
        long hundred = 100000000000L;
        long sum = 0;
        for (int i = 0; i < 1000000; i++) {
            sum += 10 << 4;
        }
    }

    public static void main(String[] args) {
        test1();
        long start = System.nanoTime();
        test1();
        Log.d("Time: " + (System.nanoTime() - start) + " ns");
    }
}

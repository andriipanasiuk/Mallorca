/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.equilator;

import java.util.List;
import java.util.Random;

import mallorcatour.equilator.PokerEquilatorBrecher;
import mallorcatour.equilator.preflop.PreflopEquilatorImpl;
import mallorcatour.equilator.quick.EquilatorQuick;
import mallorcatour.equilator.quick.FlopCombinations;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.Deck;
import mallorcatour.core.game.state.StreetEquity;
import mallorcatour.tools.ArrayUtils;
import mallorcatour.tools.CollectionUtils;
import mallorcatour.tools.Log;

/**
 * Пока больше не тест, а песочница для эквилятора Брешера.
 * @author Andrew
 */
public class EquilatorSandbox {

    private static PokerEquilatorBrecher equilator = new PokerEquilatorBrecher();
    private static PreflopEquilatorImpl preflopEquilator = new PreflopEquilatorImpl();
    public static void testBrecherCorectness() {
        Card myCard1;
        Card myCard2;

        Card boardCard1 = Card.valueOf("3s");
        Card boardCard2 = Card.valueOf("8d");
        Card boardCard3 = Card.valueOf("7s");
        Card boardCard4 = Card.valueOf("Js");

        List<Card> nonUsed = Deck.getCards();
        nonUsed = CollectionUtils.subtract(nonUsed, boardCard1);
        nonUsed = CollectionUtils.subtract(nonUsed, boardCard2);
        nonUsed = CollectionUtils.subtract(nonUsed, boardCard3);
        nonUsed = CollectionUtils.subtract(nonUsed, boardCard4);

        long start = System.nanoTime();
        for (int i = 0; i < nonUsed.size(); i++) {
            for (int j = i + 1; j < nonUsed.size(); j++) {
                myCard1 = nonUsed.get(i);
                myCard2 = nonUsed.get(j);
                double oldEquity = equilator.strengthVsRandom(myCard1, myCard2, new Card[] { boardCard1,
                        boardCard2, boardCard3, boardCard4 });
                double equity = equilator.strengthVsRandom(myCard1, myCard2, new Card[] { boardCard1,
                        boardCard2, boardCard3, boardCard4 });
                if (Double.compare(oldEquity, equity) != 0) {
                    throw new RuntimeException("Strength failed. Old value: " + oldEquity + " New value: " + equity
                            + " Cards: " + myCard1 + " " + myCard2);
                }
            }
        }
        Log.d("Time: " + (System.nanoTime() - start) + " ns");
    }

    public static void testBrecherPotentialSpeed() {
        Card myCard1;
        Card myCard2;

        Card boardCard1 = Card.valueOf("3s");
        Card boardCard2 = Card.valueOf("8d");
        Card boardCard3 = Card.valueOf("7s");
        Card boardCard4 = Card.valueOf("Js");

        List<Card> nonUsed = Deck.getCards();
        nonUsed = CollectionUtils.subtract(nonUsed, boardCard1);
        nonUsed = CollectionUtils.subtract(nonUsed, boardCard2);
        nonUsed = CollectionUtils.subtract(nonUsed, boardCard3);
        nonUsed = CollectionUtils.subtract(nonUsed, boardCard4);

        long start = System.nanoTime();
        for (int i = 0; i < nonUsed.size(); i++) {
            for (int j = i + 1; j < nonUsed.size(); j++) {
                myCard1 = nonUsed.get(i);
                myCard2 = nonUsed.get(j);
                @SuppressWarnings("unused")
                StreetEquity equity = equilator.equityOnTurn(myCard1, myCard2, boardCard1, boardCard2,
                        boardCard3, boardCard4);
            }
        }
        Log.d("Time: " + (System.nanoTime() - start) + " ns");
    }

    public static void testBrecherSpeed() {
        Card myCard1;
        Card myCard2;

        Card boardCard1 = Card.valueOf("2s");
        Card boardCard2 = Card.valueOf("8d");
        Card boardCard3 = Card.valueOf("7s");
        Card boardCard4 = Card.valueOf("Js");

        List<Card> nonUsed = Deck.getCards();
        nonUsed = CollectionUtils.subtract(nonUsed, boardCard1);
        nonUsed = CollectionUtils.subtract(nonUsed, boardCard2);
        nonUsed = CollectionUtils.subtract(nonUsed, boardCard3);
        nonUsed = CollectionUtils.subtract(nonUsed, boardCard4);

        long start = System.currentTimeMillis();
        for (int c = 0; c < 48; c++)
            for (int i = 0; i < nonUsed.size(); i++) {
                for (int j = i + 1; j < nonUsed.size(); j++) {
                    myCard1 = nonUsed.get(i);
                    myCard2 = nonUsed.get(j);
                    @SuppressWarnings("unused")
                    StreetEquity equity = equilator.equityOnTurn(myCard1, myCard2, boardCard1,
                            boardCard2, boardCard3, boardCard4);
                }
            }
        Log.d("Brecher's time: " + (System.currentTimeMillis() - start) + " ms");
    }

    public static void testQuickSpeed() {
        Card myCard1;
        Card myCard2;

        Card flop1 = Card.valueOf("2s");
        Card flop2 = Card.valueOf("8d");
        Card flop3 = Card.valueOf("7s");

        List<Card> nonUsed = Deck.getCards();
        nonUsed = CollectionUtils.subtract(nonUsed, flop1);
        nonUsed = CollectionUtils.subtract(nonUsed, flop2);
        nonUsed = CollectionUtils.subtract(nonUsed, flop3);

        FlopCombinations combinations = new FlopCombinations(flop1.intValueForBrecher(), flop2.intValueForBrecher(),
                flop3.intValueForBrecher());
        combinations.init();
        long start = System.currentTimeMillis();
        for (int i = 0; i < nonUsed.size(); i++) {
            for (int j = i + 1; j < nonUsed.size(); j++) {
                myCard1 = nonUsed.get(i);
                myCard2 = nonUsed.get(j);
                @SuppressWarnings("unused")
                StreetEquity equity = EquilatorQuick.equityVsRandomFullPotential(myCard1.intValueForBrecher(),
                        myCard2.intValueForBrecher(), flop1.intValueForBrecher(), flop2.intValueForBrecher(),
                        flop3.intValueForBrecher(), combinations);
            }
        }
        Log.d("Quick's time: " + (System.currentTimeMillis() - start) + " ms");
    }

    public static void checkBrecherCorectness() {
        Card myCard1;
        Card myCard2;

        Card boardCard1 = Card.valueOf("2s");
        Card boardCard2 = Card.valueOf("8d");
        Card boardCard3 = Card.valueOf("7s");
        // Card boardCard4 = Card.valueOf("Js");
        // Card boardCard5 = Card.valueOf("Qd");

        List<Card> nonUsed = Deck.getCards();
        nonUsed = CollectionUtils.subtract(nonUsed, boardCard1);
        nonUsed = CollectionUtils.subtract(nonUsed, boardCard2);
        nonUsed = CollectionUtils.subtract(nonUsed, boardCard3);
        // nonUsed = CollectionUtils.subtract(nonUsed, boardCard4);
        // nonUsed = CollectionUtils.subtract(nonUsed, boardCard5);

        long start = System.currentTimeMillis();
        for (int i = 0; i < nonUsed.size(); i++) {
            for (int j = i + 1; j < nonUsed.size(); j++) {
                myCard1 = nonUsed.get(i);
                myCard2 = nonUsed.get(j);
                StreetEquity equity = equilator.equityOnFlopFull(myCard1, myCard2, boardCard1, boardCard2,
                        boardCard3, true);
                Log.d("Equity: " + equity.strength + " " + equity.positivePotential + " " + equity.negativePotential);
            }
        }
        Log.d("Brecher's time: " + (System.currentTimeMillis() - start) + " ms");
    }

    @SuppressWarnings("unused")
    private static void calculate() {
        Card myCard1 = Card.valueOf("Ad");
        Card myCard2 = Card.valueOf("Ac");

        Card boardCard1 = Card.valueOf("2s");
        Card boardCard2 = Card.valueOf("8d");
        Card boardCard3 = Card.valueOf("7s");

        List<Card> nonUsed = Deck.getCards();
        nonUsed = CollectionUtils.subtract(nonUsed, boardCard1);
        nonUsed = CollectionUtils.subtract(nonUsed, boardCard2);
        nonUsed = CollectionUtils.subtract(nonUsed, boardCard3);

        long start = System.currentTimeMillis();
        StreetEquity equity = equilator.equityOnFlopFull(myCard1, myCard2, boardCard1, boardCard2,
                boardCard3, true);
        Log.d("Equity: " + equity.strength + " " + equity.positivePotential + " " + equity.negativePotential);
        Log.d("Brecher's time: " + (System.currentTimeMillis() - start) + " ms");
    }

    private static String toString(int[] cards) {
        String result = "";
        for (int card : cards) {
            result += Card.valueOf(card);
            result += ", ";
        }
        return result;
    }

    private static int[] toBrecher(int[] cards) {
        int[] result = new int[cards.length];
        for (int i = 0; i < cards.length; i++) {
            result[i] = cards[i] % 4 * 13 + cards[i] / 4;
        }
        return result;
    }

    private static int[] dealCards(int how) {
        int[] result = new int[how];
        int count = 0;
        Random random = new Random();
        while (count < how) {
            int card = Deck.getIntCards()[random.nextInt(52)];
            if (!ArrayUtils.containsElement(result, card)) {
                result[count++] = card;
            }
        }
        return result;
    }

    public static void testPreflop() {
        Card card1 = Card.valueOf("8s");
        Card card2 = Card.valueOf("7d");
        double strength = preflopEquilator.strengthVsRandom(card1, card2);
        Log.d("Strength: " + strength);
    }

    static void testSpeed() {
        int[] deck = Deck.getIntCards();
        PreflopEquilatorImpl.LOGGING = false;
        long start = System.currentTimeMillis();
        for (int i = 0; i < deck.length; i++) {
            for (int j = i+1; j < deck.length; j++) {
                Card card1 = Card.valueOf(deck[i]);
                Card card2 = Card.valueOf(deck[j]);
                preflopEquilator.equityVsRandom(card1, card2);
            }
        }
        Log.d("Time: " + (System.currentTimeMillis() - start) + " ms");
    }

    static void testPreflopMain() {
        int[] deck = Deck.getIntCards();
        for (int i = 0; i < deck.length; i++) {
            for (int j = i+1; j < deck.length; j++) {
                Card card1 = Card.valueOf(deck[i]);
                Card card2 = Card.valueOf(deck[j]);
                Log.d(card1 + " " + card2);
                preflopEquilator.equityVsRandom(card1, card2);
            }
        }
    }

    public static void main(String... args){
        testBrecherSpeed();;
    }
}

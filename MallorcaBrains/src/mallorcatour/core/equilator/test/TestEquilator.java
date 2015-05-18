/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.equilator.test;

import java.util.List;
import java.util.Random;

import mallorcatour.core.equilator.StreetEquity;
import mallorcatour.core.equilator.brecher.PokerEquilatorBrecher;
import mallorcatour.core.equilator.unused.PokerEquilatorNew;
import mallorcatour.core.equilator.unused.PokerEquilatorSpears;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.Deck;
import mallorcatour.core.spectrum.Spectrum;
import mallorcatour.util.ArrayUtils;
import mallorcatour.util.CollectionUtils;
import mallorcatour.util.Log;

/**
 *
 * @author Andrew
 */
public class TestEquilator {

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
                double oldEquity = PokerEquilatorBrecher.strengthVsRandom(myCard1, myCard2,
                        new Card[]{boardCard1, boardCard2, boardCard3, boardCard4});
                double equity = PokerEquilatorBrecher.strengthVsRandom(myCard1, myCard2,
                        new Card[]{boardCard1, boardCard2, boardCard3, boardCard4});
                if (Double.compare(oldEquity, equity) != 0) {
                    throw new RuntimeException("Strength failed. Old value: " + oldEquity
                            + " New value: " + equity
                            + " Cards: " + myCard1 + " " + myCard2);
                }
            }
        }
        Log.d("Time: " + (System.nanoTime() - start) + " ns");
    }

    public static void testBrecherPotentialCorectness() {
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
                StreetEquity oldEquity = PokerEquilatorNew.equityOnTurn(myCard1, myCard2,
                        boardCard1, boardCard2, boardCard3, boardCard4);
                StreetEquity equity = PokerEquilatorBrecher.equityOnTurn(myCard1, myCard2,
                        boardCard1, boardCard2, boardCard3, boardCard4);
                if (Double.compare(equity.strength, oldEquity.strength) != 0) {
                    throw new RuntimeException("Strength failed. Old value: " + oldEquity.strength
                            + " New value: " + equity.strength
                            + " Cards: " + myCard1 + " " + myCard2);
                }
                if (Double.compare(equity.positivePotential, oldEquity.positivePotential) != 0) {
                    throw new RuntimeException("Positive failed. Old value: " + oldEquity.positivePotential
                            + " New value: " + equity.positivePotential
                            + " Cards: " + myCard1 + " " + myCard2);
                }
                if (Double.compare(equity.negativePotential, oldEquity.negativePotential) != 0) {
                    throw new RuntimeException("Negative failed. Old value: "
                            + oldEquity.negativePotential
                            + " New value: " + equity.negativePotential
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
				StreetEquity equity = PokerEquilatorBrecher.equityOnTurn(myCard1, myCard2,
                        boardCard1, boardCard2, boardCard3, boardCard4);
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
        for (int i = 0; i < nonUsed.size(); i++) {
            for (int j = i + 1; j < nonUsed.size(); j++) {
                myCard1 = nonUsed.get(i);
                myCard2 = nonUsed.get(j);
                @SuppressWarnings("unused")
				double equity = PokerEquilatorBrecher.strengthVsRandom(myCard1, myCard2,
                        new Card[]{boardCard1, boardCard2, boardCard3, boardCard4});
            }
        }
        Log.d("Brecher's time: " + (System.currentTimeMillis() - start) + " ms");
    }

    public static void checkBrecherCorectness() {
        Card myCard1;
        Card myCard2;

        Card boardCard1 = Card.valueOf("2s");
        Card boardCard2 = Card.valueOf("8d");
        Card boardCard3 = Card.valueOf("7s");
//        Card boardCard4 = Card.valueOf("Js");
//        Card boardCard5 = Card.valueOf("Qd");

        List<Card> nonUsed = Deck.getCards();
        nonUsed = CollectionUtils.subtract(nonUsed, boardCard1);
        nonUsed = CollectionUtils.subtract(nonUsed, boardCard2);
        nonUsed = CollectionUtils.subtract(nonUsed, boardCard3);
//        nonUsed = CollectionUtils.subtract(nonUsed, boardCard4);
//        nonUsed = CollectionUtils.subtract(nonUsed, boardCard5);

        long start = System.currentTimeMillis();
        for (int i = 0; i < nonUsed.size(); i++) {
            for (int j = i + 1; j < nonUsed.size(); j++) {
                myCard1 = nonUsed.get(i);
                myCard2 = nonUsed.get(j);
                StreetEquity equity = PokerEquilatorBrecher.fullEquityOnFlop(myCard1, myCard2,
                        boardCard1, boardCard2, boardCard3);
                Log.d("Equity: " + equity.strength + " " + equity.positivePotential
                        + " " + equity.negativePotential);
            }
        }
        Log.d("Brecher's time: " + (System.currentTimeMillis() - start) + " ms");
    }

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
        StreetEquity equity = PokerEquilatorBrecher.fullEquityOnFlop(myCard1, myCard2,
                boardCard1, boardCard2, boardCard3);
        Log.d("Equity: " + equity.strength + " " + equity.positivePotential
                + " " + equity.negativePotential);
        Log.d("Brecher's time: " + (System.currentTimeMillis() - start) + " ms");
    }

    public static void equalCombinationsBrecherAndNew() {
        int tests = 1000000;
        for (int i = 0; i < tests; i++) {
            int[] cardsForNative1 = dealCards(6);
            int[] cardsForNative2 = dealCards(6);
            int[] cardsForBrecher1 = toBrecher(cardsForNative1);
            int[] cardsForBrecher2 = toBrecher(cardsForNative2);

            long combinationNative1 = PokerEquilatorNew.combination(cardsForNative1);
            long combinationNative2 = PokerEquilatorNew.combination(cardsForNative2);
            long combinationBrecher1 = PokerEquilatorBrecher.combination(cardsForBrecher1);
            long combinationBrecher2 = PokerEquilatorBrecher.combination(cardsForBrecher2);
            double sign1 = Math.signum(combinationNative1 - combinationNative2);
            double sign2 = Math.signum(combinationBrecher1 - combinationBrecher2);
            if (sign1 * sign2 <= 0 && (sign1 != 0 || sign2 != 0)) {
                Log.d("Cards1: " + toString(cardsForNative1)
                        + " Cards2: " + toString(cardsForNative2));
                Log.d("Combination native 1: " + combinationNative1);
                Log.d("Combination native 2: " + combinationNative2);
                Log.d("Combination brecher 1: " + combinationBrecher1);
                Log.d("Combination brecher 2: " + combinationBrecher2);
                PokerEquilatorNew.combination(cardsForNative1);
                PokerEquilatorNew.combination(cardsForNative2);
                PokerEquilatorBrecher.combination(cardsForBrecher1);
                PokerEquilatorBrecher.combination(cardsForBrecher2);

                throw new RuntimeException();
            }
        }
    }

    public static void equalCombinationsBrecherAndSpears() {
        int tests = 1000000;
        for (int i = 0; i < tests; i++) {
            int[] cardsForSpears1 = dealCards(6);
            int[] cardsForSpears2 = dealCards(6);
            int[] cardsForBrecher1 = toBrecher(cardsForSpears1);
            int[] cardsForBrecher2 = toBrecher(cardsForSpears2);

            long combinationNative1 = PokerEquilatorSpears.combination(cardsForSpears1);
            long combinationNative2 = PokerEquilatorSpears.combination(cardsForSpears2);
            long combinationBrecher1 = PokerEquilatorBrecher.combination(cardsForBrecher1);
            long combinationBrecher2 = PokerEquilatorBrecher.combination(cardsForBrecher2);
            double sign1 = Math.signum(combinationNative1 - combinationNative2);
            double sign2 = Math.signum(combinationBrecher1 - combinationBrecher2);
            if (sign1 * sign2 <= 0 && (sign1 != 0 || sign2 != 0)) {
                Log.d("Cards1: " + toString(cardsForSpears1)
                        + " Cards2: " + toString(cardsForSpears2));
                Log.d("Combination spears 1: " + combinationNative1);
                Log.d("Combination spears 2: " + combinationNative2);
                Log.d("Combination brecher 1: " + combinationBrecher1);
                Log.d("Combination brecher 2: " + combinationBrecher2);
                PokerEquilatorSpears.combination(cardsForSpears1);
                PokerEquilatorSpears.combination(cardsForSpears2);
                PokerEquilatorBrecher.combination(cardsForBrecher1);
                PokerEquilatorBrecher.combination(cardsForBrecher2);
                throw new RuntimeException();
            }
        }
        Log.d("Brecher's and Spears well done!");
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
        Card card1 = Card.valueOf("As");
        Card card2 = Card.valueOf("Kd");
        double strength = PokerEquilatorBrecher.strengthVsSpectrum(card1, card2,
                new Card[]{}, Spectrum.random());
        Log.d("Strength: " + strength);
    }

    public static void main(String[] args) {
//        checkBrecherCorectness();
        calculate();
//        equalCombinationsBrecherAndSpears();
//        testSpearsSpeed();
//        testPreflop();
//        equalCombinationsBrecherAndNative();
//        testBrecherCorectness();
        //        testBrecherPotentialCorectness();
//        PokerEquilatorBrecher.time = 0;
//        testBrecherSpeed();
//        testBrecherPotentialSpeed();
//        Log.d("Time: " + PokerEquilatorBrecher.time);

//        equalSpeeds();
    }
}

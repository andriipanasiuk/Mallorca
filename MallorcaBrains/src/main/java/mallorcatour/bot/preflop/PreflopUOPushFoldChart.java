/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.preflop;

import java.util.HashMap;
import java.util.Map;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.HoleCards;

/**
 * Preflop chart for unopened when potToStack odds less than 0.1
 * Based on Nash!
 * @author Andrew
 */
public class PreflopUOPushFoldChart {

    //min ES for all-in
    private Map<Integer, Double> nonsuitedAdvices;
    private Map<Integer, Double> suitedAdvices;

    public PreflopUOPushFoldChart() {
        nonsuitedAdvices = new HashMap<Integer, Double>();
        suitedAdvices = new HashMap<Integer, Double>();
        init();
    }

    private void init() {
        initSuited();
        initNonsuited();
        initPairs();
    }

    private void initSuited() {
        put(HoleCards.valueOf("AsKs"), 10d);
        put(HoleCards.valueOf("AsQs"), 10d);
        put(HoleCards.valueOf("AsJs"), 10d);
        put(HoleCards.valueOf("AsTs"), 10d);
        put(HoleCards.valueOf("As9s"), 10d);
        put(HoleCards.valueOf("As8s"), 10d);
        put(HoleCards.valueOf("As7s"), 10d);
        put(HoleCards.valueOf("As6s"), 10d);
        put(HoleCards.valueOf("As5s"), 10d);
        put(HoleCards.valueOf("As4s"), 10d);
        put(HoleCards.valueOf("As3s"), 10d);
        put(HoleCards.valueOf("As2s"), 10d);
        ////////////////////
        put(HoleCards.valueOf("KsQs"), 10d);
        put(HoleCards.valueOf("KsJs"), 10d);
        put(HoleCards.valueOf("KsTs"), 10d);
        put(HoleCards.valueOf("Ks9s"), 10d);
        put(HoleCards.valueOf("Ks8s"), 10d);
        put(HoleCards.valueOf("Ks7s"), 10d);
        put(HoleCards.valueOf("Ks6s"), 10d);
        put(HoleCards.valueOf("Ks5s"), 10d);
        put(HoleCards.valueOf("Ks4s"), 10d);
        put(HoleCards.valueOf("Ks3s"), 10d);
        put(HoleCards.valueOf("Ks2s"), 10d);
        ////////////////////
        put(HoleCards.valueOf("QsJs"), 10d);
        put(HoleCards.valueOf("QsTs"), 10d);
        put(HoleCards.valueOf("Qs9s"), 10d);
        put(HoleCards.valueOf("Qs8s"), 10d);
        put(HoleCards.valueOf("Qs7s"), 10d);
        put(HoleCards.valueOf("Qs6s"), 10d);
        put(HoleCards.valueOf("Qs5s"), 9d);
        put(HoleCards.valueOf("Qs4s"), 9d);
        put(HoleCards.valueOf("Qs3s"), 8d);
        put(HoleCards.valueOf("Qs2s"), 8d);
        ////////////////////
        put(HoleCards.valueOf("JsTs"), 10d);
        put(HoleCards.valueOf("Js9s"), 10d);
        put(HoleCards.valueOf("Js8s"), 10d);
        put(HoleCards.valueOf("Js7s"), 9d);
        put(HoleCards.valueOf("Js6s"), 8d);
        put(HoleCards.valueOf("Js5s"), 7d);
        put(HoleCards.valueOf("Js4s"), 6d);
        put(HoleCards.valueOf("Js3s"), 6d);
        put(HoleCards.valueOf("Js2s"), 6d);
        ////////////////////
        put(HoleCards.valueOf("Ts9s"), 10d);
        put(HoleCards.valueOf("Ts8s"), 9d);
        put(HoleCards.valueOf("Ts7s"), 8d);
        put(HoleCards.valueOf("Ts6s"), 6d);
        put(HoleCards.valueOf("Ts5s"), 6d);
        put(HoleCards.valueOf("Ts4s"), 5d);
        put(HoleCards.valueOf("Ts3s"), 5d);
        put(HoleCards.valueOf("Ts2s"), 5d);
        ////////////////////
        put(HoleCards.valueOf("9s8s"), 9d);
        put(HoleCards.valueOf("9s7s"), 7d);
        put(HoleCards.valueOf("9s6s"), 6d);
        put(HoleCards.valueOf("9s5s"), 5d);
        put(HoleCards.valueOf("9s4s"), 5d);
        put(HoleCards.valueOf("9s3s"), 4d);
        put(HoleCards.valueOf("9s2s"), 4d);
        ////////////////////
        put(HoleCards.valueOf("8s7s"), 6d);
        put(HoleCards.valueOf("8s6s"), 6d);
        put(HoleCards.valueOf("8s5s"), 5d);
        put(HoleCards.valueOf("8s4s"), 4d);
        put(HoleCards.valueOf("8s3s"), 4d);
        put(HoleCards.valueOf("8s2s"), 4d);
        ////////////////////
        put(HoleCards.valueOf("7s6s"), 5d);
        put(HoleCards.valueOf("7s5s"), 5d);
        put(HoleCards.valueOf("7s4s"), 4d);
        put(HoleCards.valueOf("7s3s"), 4d);
        put(HoleCards.valueOf("7s2s"), 3d);
        ////////////////////
        put(HoleCards.valueOf("6s5s"), 5d);
        put(HoleCards.valueOf("6s4s"), 4d);
        put(HoleCards.valueOf("6s3s"), 4d);
        put(HoleCards.valueOf("6s2s"), 3d);
        ////////////////////
        put(HoleCards.valueOf("5s4s"), 4d);
        put(HoleCards.valueOf("5s3s"), 4d);
        put(HoleCards.valueOf("5s2s"), 3d);
        ////////////////////
        put(HoleCards.valueOf("4s3s"), 4d);
        put(HoleCards.valueOf("4s2s"), 3d);
        ////////////////////
        put(HoleCards.valueOf("3s2s"), 3d);
    }

    private void initNonsuited() {
        put(HoleCards.valueOf("AsKd"), 10d);
        put(HoleCards.valueOf("AsQd"), 10d);
        put(HoleCards.valueOf("AsJd"), 10d);
        put(HoleCards.valueOf("AsTd"), 10d);
        put(HoleCards.valueOf("As9d"), 10d);
        put(HoleCards.valueOf("As8d"), 10d);
        put(HoleCards.valueOf("As7d"), 10d);
        put(HoleCards.valueOf("As6d"), 10d);
        put(HoleCards.valueOf("As5d"), 10d);
        put(HoleCards.valueOf("As4d"), 10d);
        put(HoleCards.valueOf("As3d"), 10d);
        put(HoleCards.valueOf("As2d"), 10d);
        ////////////////////
        put(HoleCards.valueOf("KsQd"), 10d);
        put(HoleCards.valueOf("KsJd"), 10d);
        put(HoleCards.valueOf("KsTd"), 10d);
        put(HoleCards.valueOf("Ks9d"), 10d);
        put(HoleCards.valueOf("Ks8d"), 10d);
        put(HoleCards.valueOf("Ks7d"), 10d);
        put(HoleCards.valueOf("Ks6d"), 10d);
        put(HoleCards.valueOf("Ks5d"), 9d);
        put(HoleCards.valueOf("Ks4d"), 9d);
        put(HoleCards.valueOf("Ks3d"), 8d);
        put(HoleCards.valueOf("Ks2d"), 8d);
        ////////////////////
        put(HoleCards.valueOf("QsJd"), 10d);
        put(HoleCards.valueOf("QsTd"), 10d);
        put(HoleCards.valueOf("Qs9d"), 10d);
        put(HoleCards.valueOf("Qs8d"), 10d);
        put(HoleCards.valueOf("Qs7d"), 8d);
        put(HoleCards.valueOf("Qs6d"), 8d);
        put(HoleCards.valueOf("Qs5d"), 7d);
        put(HoleCards.valueOf("Qs4d"), 6d);
        put(HoleCards.valueOf("Qs3d"), 6d);
        put(HoleCards.valueOf("Qs2d"), 6d);
        ////////////////////
        put(HoleCards.valueOf("JsTd"), 10d);
        put(HoleCards.valueOf("Js9d"), 10d);
        put(HoleCards.valueOf("Js8d"), 8d);
        put(HoleCards.valueOf("Js7d"), 6d);
        put(HoleCards.valueOf("Js6d"), 6d);
        put(HoleCards.valueOf("Js5d"), 5d);
        put(HoleCards.valueOf("Js4d"), 5d);
        put(HoleCards.valueOf("Js3d"), 5d);
        put(HoleCards.valueOf("Js2d"), 4d);
        ///////////////////
        put(HoleCards.valueOf("Ts9d"), 9d);
        put(HoleCards.valueOf("Ts8d"), 7d);
        put(HoleCards.valueOf("Ts7d"), 6d);
        put(HoleCards.valueOf("Ts6d"), 5d);
        put(HoleCards.valueOf("Ts5d"), 4d);
        put(HoleCards.valueOf("Ts4d"), 4d);
        put(HoleCards.valueOf("Ts3d"), 4d);
        put(HoleCards.valueOf("Ts2d"), 4d);
        ////////////////////
        put(HoleCards.valueOf("9s8d"), 6d);
        put(HoleCards.valueOf("9s7d"), 5d);
        put(HoleCards.valueOf("9s6d"), 5d);
        put(HoleCards.valueOf("9s5d"), 4d);
        put(HoleCards.valueOf("9s4d"), 3d);
        put(HoleCards.valueOf("9s3d"), 3d);
        put(HoleCards.valueOf("9s2d"), 3d);
        ////////////////////
        put(HoleCards.valueOf("8s7d"), 5d);
        put(HoleCards.valueOf("8s6d"), 4d);
        put(HoleCards.valueOf("8s5d"), 4d);
        put(HoleCards.valueOf("8s4d"), 3d);
        put(HoleCards.valueOf("8s3d"), 3d);
        put(HoleCards.valueOf("8s2d"), 3d);
        ////////////////////
        put(HoleCards.valueOf("7s6d"), 4d);
        put(HoleCards.valueOf("7s5d"), 4d);
        put(HoleCards.valueOf("7s4d"), 3d);
        put(HoleCards.valueOf("7s3d"), 3d);
        put(HoleCards.valueOf("7s2d"), 1d);
        ////////////////////
        put(HoleCards.valueOf("6s5d"), 4d);
        put(HoleCards.valueOf("6s4d"), 3d);
        put(HoleCards.valueOf("6s3d"), 3d);
        put(HoleCards.valueOf("6s2d"), 1d);
        ////////////////////
        put(HoleCards.valueOf("5s4d"), 3d);
        put(HoleCards.valueOf("5s3d"), 3d);
        put(HoleCards.valueOf("5s2d"), 3d);
        ////////////////////
        put(HoleCards.valueOf("4s3d"), 3d);
        put(HoleCards.valueOf("4s2d"), 1d);
        ////////////////////
        put(HoleCards.valueOf("3s2d"), 1d);
    }

    private void initPairs() {
        put(HoleCards.valueOf("AsAd"), 10d);
        put(HoleCards.valueOf("KsKd"), 10d);
        put(HoleCards.valueOf("QsQd"), 10d);
        put(HoleCards.valueOf("JsJd"), 10d);
        put(HoleCards.valueOf("TsTd"), 10d);
        put(HoleCards.valueOf("9s9d"), 10d);
        put(HoleCards.valueOf("8s8d"), 10d);
        put(HoleCards.valueOf("7s7d"), 10d);
        put(HoleCards.valueOf("6s6d"), 10d);
        put(HoleCards.valueOf("5s5d"), 10d);
        put(HoleCards.valueOf("4s4d"), 10d);
        put(HoleCards.valueOf("3s3d"), 10d);
        put(HoleCards.valueOf("2s2d"), 10d);
    }

    private void put(HoleCards cards, double minEs) {
        if (cards.isSuited()) {
            suitedAdvices.put(cards.hashCodeForValues(), minEs);
        } else {
            nonsuitedAdvices.put(cards.hashCodeForValues(), minEs);
        }
    }

    public Action getAction(HoleCards cards, double countOfBBs) {
        if (countOfBBs > 10) {
            throw new IllegalArgumentException();
        }
        double bbs;
        if (cards.isSuited()) {
            bbs = suitedAdvices.get(cards.hashCodeForValues());

        } else {
            bbs = nonsuitedAdvices.get(cards.hashCodeForValues());
        }
        if (countOfBBs <= bbs) {
            return Action.allIn();
        } else {
            return Action.fold();
        }
    }
}

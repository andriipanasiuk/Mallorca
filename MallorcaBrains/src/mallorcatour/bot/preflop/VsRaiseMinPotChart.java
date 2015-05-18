/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.preflop;

import java.util.HashMap;
import java.util.Map;

import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.robot.controller.PokerPreferences;
import mallorcatour.util.Log;

/**
 * 
 * @author Andrew
 */
public class VsRaiseMinPotChart {

    private Map<Integer, Advice> nonsuitedAdvices;
    private Map<Integer, Advice> suitedAdvices;

    public VsRaiseMinPotChart() {
        nonsuitedAdvices = new HashMap<Integer, Advice>();
        suitedAdvices = new HashMap<Integer, Advice>();
        init();
    }

    private void init() {
        initSuited();
        initNonsuited();
        initPairs();
    }

    private void initSuited() {
        put(HoleCards.valueOf("AsKs"), NLPreflopChart.PERCENT_PASSIVE_10);
        put(HoleCards.valueOf("AsQs"), NLPreflopChart.PERCENT_PASSIVE_10);
        put(HoleCards.valueOf("AsJs"), NLPreflopChart.PERCENT_FOLD_10);
        put(HoleCards.valueOf("AsTs"), NLPreflopChart.PERCENT_FOLD_20);
        put(HoleCards.valueOf("As9s"), NLPreflopChart.PERCENT_FOLD_30);
        put(HoleCards.valueOf("As8s"), NLPreflopChart.PERCENT_FOLD_40);
        put(HoleCards.valueOf("As7s"), NLPreflopChart.PERCENT_FOLD_50);
        put(HoleCards.valueOf("As6s"), NLPreflopChart.PERCENT_FOLD_70);
        put(HoleCards.valueOf("As5s"), NLPreflopChart.PERCENT_FOLD_60);
        put(HoleCards.valueOf("As4s"), NLPreflopChart.PERCENT_FOLD_80);
        put(HoleCards.valueOf("As3s"), NLPreflopChart.PERCENT_FOLD_90);
        put(HoleCards.valueOf("As2s"), NLPreflopChart.PERCENT_FOLD_90);
        ////////////////////
        put(HoleCards.valueOf("KsQs"), NLPreflopChart.PERCENT_FOLD_30);
        put(HoleCards.valueOf("KsJs"), NLPreflopChart.PERCENT_FOLD_30);
        put(HoleCards.valueOf("KsTs"), NLPreflopChart.PERCENT_FOLD_50);
        put(HoleCards.valueOf("Ks9s"), NLPreflopChart.PERCENT_FOLD_60);
        put(HoleCards.valueOf("Ks8s"), NLPreflopChart.PERCENT_FOLD_90);
        put(HoleCards.valueOf("Ks7s"), NLPreflopChart.PERCENT_FOLD_90);
        put(HoleCards.valueOf("Ks6s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Ks5s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Ks4s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Ks3s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Ks2s"), NLPreflopChart.PERCENT_FOLD_100);
        ////////////////////
        put(HoleCards.valueOf("QsJs"), NLPreflopChart.PERCENT_FOLD_60);
        put(HoleCards.valueOf("QsTs"), NLPreflopChart.PERCENT_FOLD_80);
        put(HoleCards.valueOf("Qs9s"), NLPreflopChart.PERCENT_FOLD_90);
        put(HoleCards.valueOf("Qs8s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Qs7s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Qs6s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Qs5s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Qs4s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Qs3s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Qs2s"), NLPreflopChart.PERCENT_FOLD_100);
        ////////////////////
        put(HoleCards.valueOf("JsTs"), NLPreflopChart.PERCENT_FOLD_90);
        put(HoleCards.valueOf("Js9s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Js8s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Js7s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Js6s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Js5s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Js4s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Js3s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Js2s"), NLPreflopChart.PERCENT_FOLD_100);
        ////////////////////
        put(HoleCards.valueOf("Ts9s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Ts8s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Ts7s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Ts6s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Ts5s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Ts4s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Ts3s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Ts2s"), NLPreflopChart.PERCENT_FOLD_100);
        ////////////////////
        put(HoleCards.valueOf("9s8s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("9s7s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("9s6s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("9s5s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("9s4s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("9s3s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("9s2s"), NLPreflopChart.PERCENT_FOLD_100);
        ////////////////////
        put(HoleCards.valueOf("8s7s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("8s6s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("8s5s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("8s4s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("8s3s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("8s2s"), NLPreflopChart.PERCENT_FOLD_100);
        ////////////////////
        put(HoleCards.valueOf("7s6s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("7s5s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("7s4s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("7s3s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("7s2s"), NLPreflopChart.PERCENT_FOLD_100);
        ////////////////////
        put(HoleCards.valueOf("6s5s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("6s4s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("6s3s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("6s2s"), NLPreflopChart.PERCENT_FOLD_100);
        ////////////////////
        put(HoleCards.valueOf("5s4s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("5s3s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("5s2s"), NLPreflopChart.PERCENT_FOLD_100);
        ////////////////////
        put(HoleCards.valueOf("4s3s"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("4s2s"), NLPreflopChart.PERCENT_FOLD_100);
        ////////////////////
        put(HoleCards.valueOf("3s2s"), NLPreflopChart.PERCENT_FOLD_100);
    }

    private void initNonsuited() {
        put(HoleCards.valueOf("AsKd"), NLPreflopChart.PERCENT_PASSIVE_10);
        put(HoleCards.valueOf("AsQd"), NLPreflopChart.PERCENT_PASSIVE_10);
        put(HoleCards.valueOf("AsJd"), NLPreflopChart.PERCENT_FOLD_10);
        put(HoleCards.valueOf("AsTd"), NLPreflopChart.PERCENT_FOLD_20);
        put(HoleCards.valueOf("As9d"), NLPreflopChart.PERCENT_FOLD_30);
        put(HoleCards.valueOf("As8d"), NLPreflopChart.PERCENT_FOLD_40);
        put(HoleCards.valueOf("As7d"), NLPreflopChart.PERCENT_FOLD_50);
        put(HoleCards.valueOf("As6d"), NLPreflopChart.PERCENT_FOLD_70);
        put(HoleCards.valueOf("As5d"), NLPreflopChart.PERCENT_FOLD_60);
        put(HoleCards.valueOf("As4d"), NLPreflopChart.PERCENT_FOLD_80);
        put(HoleCards.valueOf("As3d"), NLPreflopChart.PERCENT_FOLD_90);
        put(HoleCards.valueOf("As2d"), NLPreflopChart.PERCENT_FOLD_90);
        ////////////////////
        put(HoleCards.valueOf("KsQd"), NLPreflopChart.PERCENT_FOLD_30);
        put(HoleCards.valueOf("KsJd"), NLPreflopChart.PERCENT_FOLD_30);
        put(HoleCards.valueOf("KsTd"), NLPreflopChart.PERCENT_FOLD_50);
        put(HoleCards.valueOf("Ks9d"), NLPreflopChart.PERCENT_FOLD_60);
        put(HoleCards.valueOf("Ks8d"), NLPreflopChart.PERCENT_FOLD_90);
        put(HoleCards.valueOf("Ks7d"), NLPreflopChart.PERCENT_FOLD_90);
        put(HoleCards.valueOf("Ks6d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Ks5d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Ks4d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Ks3d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Ks2d"), NLPreflopChart.PERCENT_FOLD_100);
        ////////////////////
        put(HoleCards.valueOf("QsJd"), NLPreflopChart.PERCENT_FOLD_60);
        put(HoleCards.valueOf("QsTd"), NLPreflopChart.PERCENT_FOLD_80);
        put(HoleCards.valueOf("Qs9d"), NLPreflopChart.PERCENT_FOLD_90);
        put(HoleCards.valueOf("Qs8d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Qs7d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Qs6d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Qs5d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Qs4d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Qs3d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Qs2d"), NLPreflopChart.PERCENT_FOLD_100);
        ////////////////////
        put(HoleCards.valueOf("JsTd"), NLPreflopChart.PERCENT_FOLD_90);
        put(HoleCards.valueOf("Js9d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Js8d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Js7d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Js6d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Js5d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Js4d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Js3d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Js2d"), NLPreflopChart.PERCENT_FOLD_100);
        ////////////////////
        put(HoleCards.valueOf("Ts9d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Ts8d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Ts7d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Ts6d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Ts5d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Ts4d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Ts3d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("Ts2d"), NLPreflopChart.PERCENT_FOLD_100);
        ////////////////////
        put(HoleCards.valueOf("9s8d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("9s7d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("9s6d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("9s5d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("9s4d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("9s3d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("9s2d"), NLPreflopChart.PERCENT_FOLD_100);
        ////////////////////
        put(HoleCards.valueOf("8s7d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("8s6d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("8s5d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("8s4d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("8s3d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("8s2d"), NLPreflopChart.PERCENT_FOLD_100);
        ////////////////////
        put(HoleCards.valueOf("7s6d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("7s5d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("7s4d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("7s3d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("7s2d"), NLPreflopChart.PERCENT_FOLD_100);
        ///////////////////
        put(HoleCards.valueOf("6s5d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("6s4d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("6s3d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("6s2d"), NLPreflopChart.PERCENT_FOLD_100);
        ////////////////////
        put(HoleCards.valueOf("5s4d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("5s3d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("5s2d"), NLPreflopChart.PERCENT_FOLD_100);
        ////////////////////
        put(HoleCards.valueOf("4s3d"), NLPreflopChart.PERCENT_FOLD_100);
        put(HoleCards.valueOf("4s2d"), NLPreflopChart.PERCENT_FOLD_100);
        ////////////////////
        put(HoleCards.valueOf("3s2d"), NLPreflopChart.PERCENT_FOLD_100);
    }

    private void initPairs() {
        put(HoleCards.valueOf("AsAd"), NLPreflopChart.PERCENT_PASSIVE_10);
        put(HoleCards.valueOf("KsKd"), NLPreflopChart.PERCENT_PASSIVE_10);
        put(HoleCards.valueOf("QsQd"), NLPreflopChart.PERCENT_PASSIVE_20);
        put(HoleCards.valueOf("JsJd"), NLPreflopChart.PERCENT_PASSIVE_30);
        put(HoleCards.valueOf("TsTd"), NLPreflopChart.PERCENT_PASSIVE_40);
        put(HoleCards.valueOf("9s9d"), NLPreflopChart.PERCENT_PASSIVE_50);
        put(HoleCards.valueOf("8s8d"), NLPreflopChart.PERCENT_PASSIVE_60);
        put(HoleCards.valueOf("7s7d"), NLPreflopChart.PERCENT_PASSIVE_60);
        put(HoleCards.valueOf("6s6d"), NLPreflopChart.PERCENT_FOLD_30);
        put(HoleCards.valueOf("5s5d"), NLPreflopChart.PERCENT_FOLD_50);
        put(HoleCards.valueOf("4s4d"), NLPreflopChart.PERCENT_FOLD_80);
        put(HoleCards.valueOf("3s3d"), NLPreflopChart.PERCENT_FOLD_90);
        put(HoleCards.valueOf("2s2d"), NLPreflopChart.PERCENT_FOLD_90);
    }

    private void put(HoleCards cards, Advice advice) {
        if (cards.isSuited()) {
            suitedAdvices.put(cards.hashCodeForValues(), advice);
        } else {
            nonsuitedAdvices.put(cards.hashCodeForValues(), advice);
        }
    }

    public Advice getAdvice(HoleCards cards) {
        Advice advice;
        if (cards.isSuited()) {
            advice = suitedAdvices.get(cards.hashCodeForValues());
            return advice;
        } else {
            advice = nonsuitedAdvices.get(cards.hashCodeForValues());
            return advice;
        }
    }
}

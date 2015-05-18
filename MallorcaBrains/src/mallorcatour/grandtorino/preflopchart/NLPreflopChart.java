/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.preflopchart;

import mallorcatour.game.advice.Advice;
import mallorcatour.game.core.Action;
import mallorcatour.game.core.HoleCards;
import mallorcatour.game.situation.LocalSituation;

/**
 *
 * @author Andrew
 */
public class NLPreflopChart implements IPreflopChart {

    public static Advice PERCENT_FOLD_0 = Advice.create(0, 0, 1);
    public static Advice PERCENT_FOLD_10 = Advice.create(0.1, 0, 0.9);
    public static Advice PERCENT_FOLD_20 = Advice.create(0.2, 0, 0.8);
    public static Advice PERCENT_FOLD_30 = Advice.create(0.3, 0, 0.7);
    public static Advice PERCENT_FOLD_40 = Advice.create(0.4, 0, 0.6);
    public static Advice PERCENT_FOLD_50 = Advice.create(0.5, 0, 0.5);
    public static Advice PERCENT_FOLD_60 = Advice.create(0.6, 0, 0.4);
    public static Advice PERCENT_FOLD_70 = Advice.create(0.7, 0, 0.3);
    public static Advice PERCENT_FOLD_80 = Advice.create(0.8, 0, 0.2);
    public static Advice PERCENT_FOLD_90 = Advice.create(0.9, 0, 0.1);
    public static Advice PERCENT_FOLD_100 = Advice.create(1, 0, 0);
    ///////////////
    public static Advice PERCENT_PASSIVE_0 = Advice.create(0, 0, 1);
    public static Advice PERCENT_PASSIVE_10 = Advice.create(0, 0.1, 0.9);
    public static Advice PERCENT_PASSIVE_20 = Advice.create(0, 0.2, 0.8);
    public static Advice PERCENT_PASSIVE_30 = Advice.create(0, 0.3, 0.7);
    public static Advice PERCENT_PASSIVE_40 = Advice.create(0, 0.4, 0.6);
    public static Advice PERCENT_PASSIVE_50 = Advice.create(0, 0.5, 0.5);
    public static Advice PERCENT_PASSIVE_60 = Advice.create(0, 0.6, 0.4);
    public static Advice PERCENT_PASSIVE_70 = Advice.create(0, 0.7, 0.3);
    public static Advice PERCENT_PASSIVE_80 = Advice.create(0, 0.8, 0.2);
    public static Advice PERCENT_PASSIVE_90 = Advice.create(0, 0.9, 0.1);
    public static Advice PERCENT_PASSIVE_100 = Advice.create(0, 1, 0);
    ///////////////////////
    private final static double minBBsInESForPushFold = 10;
    private PreflopUOMinPotChart uoMinPotChart;
    private PreflopUOPushFoldChart pushFoldChart;
    private VsRaiseMinPotChart vsRaiseChart;

    public NLPreflopChart() {
        uoMinPotChart = new PreflopUOMinPotChart();
        pushFoldChart = new PreflopUOPushFoldChart();
        vsRaiseChart = new VsRaiseMinPotChart();
    }

	public Action getAction(LocalSituation situation, HoleCards cards) {
		if (situation.isHeroOnButton() && situation.getHeroActionCount() == 0 && situation.getVillainActionCount() == 0) {
            double bbsInES = convertPotToStackOddsToBBInEffectiveStack(situation.getPotToStackOdds());
            if (bbsInES > minBBsInESForPushFold) {
                return uoMinPotChart.getAdvice(cards).getAction();
            } else {
                return pushFoldChart.getAction(cards, bbsInES);
            }
        } else if (!situation.isHeroOnButton()
                && situation.getHeroActionCount() == 0
                && situation.getVillainActionCount() == 1
                && situation.getVillainAggresionActionCount() == 1
                && !situation.wasHeroPreviousAggresive()
                && situation.wasVillainPreviousAggresive()
                && situation.getPotToStackOdds() < 0.4
                && situation.getPotOdds() < 0.4) {
            return vsRaiseChart.getAdvice(cards).getAction();
        }
        return null;
    }

    private double convertPotToStackOddsToBBInEffectiveStack(double potToStackOdds) {
        return 2 * (1d / potToStackOdds - 0.5);

    }
}

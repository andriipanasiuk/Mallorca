/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.preflop;

import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.stats.IPokerStats;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.tools.Log;

/**
 *
 * @author Andrew
 */
public class NLPreflopChart implements IAdvisor {

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

	private boolean unopened(LocalSituation situation) {
		return situation.isHeroOnButton() && situation.getHeroActionCount() == 0
				&& situation.getVillainActionCount() == 0;
	}
    @Override
	public IAdvice getAdvice(LocalSituation situation, HoleCards cards, IPlayerGameInfo gameInfo) {
		if (unopened(situation)) {
			double bbsInES = convertPotToStackOddsToBBInEffectiveStack(situation.getPotToStackOdds());
            if (bbsInES > minBBsInESForPushFold) {
                return uoMinPotChart.getAdvice(cards);
            } else {
            	Action action = pushFoldChart.getAction(cards, bbsInES);
            	Log.f("Decision by push/fold chart");
            	return action;
            }
		} else if (vsRaise(situation) && situation.getPotToStackOdds() < 0.4 && situation.getPotOdds() < 0.4) {
			return vsRaiseChart.getAdvice(cards);
		}
        return null;
    }

	private boolean vsRaise(LocalSituation situation) {
		return !situation.isHeroOnButton() && situation.getHeroActionCount() == 0
				&& situation.getVillainActionCount() == 1 && situation.getVillainAggresionActionCount() == 1
				&& !situation.wasHeroPreviousAggresive() && situation.wasVillainPreviousAggresive();
	}

	private double convertPotToStackOddsToBBInEffectiveStack(double potToStackOdds) {
        return 2 * (1d / potToStackOdds - 0.5);

    }

	@Override
	public String getName() {
		return "Preflop chart bot";
	}

	@Override
	public IPokerStats getStats() {
		throw new UnsupportedOperationException();
	}
}

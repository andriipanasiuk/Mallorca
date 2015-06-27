/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.math;

import java.util.Map.Entry;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IGameInfo;

/**
 *
 * @author Andrew
 */
public class LessVarianceActionFromMap extends BaseAdviceCreatorFromMap {

	public static ProfitCorrections correction;

	@Override
	public IAdvice create(ActionDistribution actionDistribution, IGameInfo gameInfo, HoleCards cards) {
		Entry<Action, RandomVariable> bestAction = getBestAction(actionDistribution, gameInfo, 0, true, cards);
		return bestAction.getKey();
	}

	public static RandomVariable getOptimal(ActionDistribution actionDistribution, IGameInfo gameInfo, 
			double investment, HoleCards cards) {
		Entry<Action, RandomVariable> bestAction = getBestAction(actionDistribution, gameInfo, investment, false, cards);
		return bestAction.getValue();
	}

	private static boolean profitabilityIsGood(ActionProfit actionProfit, double lowEV) {
		if (actionProfit == null) {
			return false;
		}
		if (actionProfit.evBBKoeff < lowEV) {
			return false;
		}
		double evVarKoeff = actionProfit.evVarKoeff;
		double varBBKoeff = actionProfit.varBBkoeff;
		return (evVarKoeff >= 0 && varBBKoeff <= 0) || 
				(evVarKoeff > 0.1 && varBBKoeff < 2) || 
				(evVarKoeff > 0.2 && varBBKoeff < 5) || 
				(evVarKoeff > 0.25 && varBBKoeff < 10) || 
				(evVarKoeff > 0.3 && varBBKoeff < 25) || 
				(evVarKoeff > 0.4 && varBBKoeff < 30) || 
				(evVarKoeff > 0.48 && varBBKoeff < 38) || 
				(evVarKoeff > 0.5 && varBBKoeff < 40) || 
				(evVarKoeff > 0.55 && varBBKoeff < 50) || 
				(evVarKoeff > 0.75 && varBBKoeff < 100);
	}

	private static class ActionProfit {
		double evBBKoeff, evVarKoeff, varBBkoeff, ev, variance;

		public void calculate(double bb) {
			evBBKoeff = ev / bb;
			varBBkoeff = variance / bb;
			evVarKoeff = ev / variance;
		}

		public void calculateEVVarChanged(double bb) {
			ev = evVarKoeff * variance;
			evBBKoeff = ev / bb;
			varBBkoeff = variance / bb;
		}
	}

	private static Entry<Action, RandomVariable> getBestAction(ActionDistribution actionDistribution, 
			IGameInfo gameInfo, double investment, boolean fromTop, HoleCards cards) {
		Entry<Action, RandomVariable> foldAction = null, passiveAction = null, raiseAction = null;
		ActionProfit passiveProfit = null, raiseProfit = null, foldProfit = null;
		double toCall = -1;
		for (Entry<Action, RandomVariable> entry : actionDistribution.entrySet()) {
			Action action = entry.getKey();
			ActionProfit actionProfit = new ActionProfit();
			if (action.isFold()) {
				foldAction = entry;
				foldProfit = actionProfit;
			} else if (action.isPassive()) {
				toCall = action.getAmountToCall();
				passiveAction = entry;
				passiveProfit = actionProfit;
			} else {
				raiseProfit = actionProfit;
				raiseAction = entry;
			}
			RandomVariable item = entry.getValue();
			double variance = item.getVariance();
			actionProfit.variance = variance;
			double ev = item.getEV();
			actionProfit.ev = ev;
			actionProfit.calculate(gameInfo.getBigBlindSize());
		}
		if (toCall == -1) {
			throw new RuntimeException("toCall cannot be -1 in this situation");
		}
		if (fromTop) {
			correctProfits(gameInfo, toCall, raiseProfit, passiveProfit);
		}
		if (profitabilityIsGood(raiseProfit, foldProfit.evBBKoeff)){
			return raiseAction;
		}
		if (profitabilityIsGood(passiveProfit, foldProfit.evBBKoeff) || toCall == 0) {
			return passiveAction;
		}
		return foldAction;
	}

	private static double getCorrection(boolean aggro, double toCall, boolean preflop) {
		if(correction == null){
			return 0;
		}
		if (preflop) {
			if (aggro) {
				if (toCall == 0) {
					return correction.getPreflopBet();
				} else {
					return correction.getPreflopRaise();
				}
			} else {
				if (toCall == 0) {
					return correction.getPreflopCheck();
				} else {
					return correction.getPreflopCall();
				}
			}
		} else {
			if (aggro) {
				if (toCall == 0) {
					return correction.getPostflopBet();
				} else {
					return correction.getPostflopRaise();
				}
			} else {
				if (toCall == 0) {
					return correction.getPostflopCheck();
				} else {
					return correction.getPostflopCall();
				}
			}
		}
	}

	private static void correctProfits(IGameInfo gameInfo, double toCall, ActionProfit raiseProfit,
			ActionProfit passiveProfit) {
		double bb = gameInfo.getBigBlindSize();
		if (raiseProfit != null) {
			raiseProfit.evVarKoeff += getCorrection(true, toCall, gameInfo.isPreFlop());
			raiseProfit.calculateEVVarChanged(bb);
		}
		passiveProfit.evVarKoeff += getCorrection(false, toCall, gameInfo.isPreFlop());
		passiveProfit.calculateEVVarChanged(bb);
	}
}

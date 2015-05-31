/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.math;

import java.util.HashMap;
import java.util.Map;

import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.math.IGameSolver;
import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.equilator.StreetEquity;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.Flop;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.core.spectrum.Spectrum;
import mallorcatour.util.CollectionUtils;
import mallorcatour.util.Log;

/**
 * 
 * @author Andrew
 */
public class NLGameSolver implements IGameSolver {

	private final static double NEARLY_ZERO = 0.0001;
	private final static double DEFAULT_POSITIVE_POTENTIAL = 0.1;
	private final static double DEFAULT_NEGATIVE_POTENTIAL = 0.1;
	private final static double TURN_POT_COEFF = 10.3;
	private final static double RIVER_POT_COEFF = 8.65;
	private final static double IP_ADDITIONAL_PROFIT_BB = 0.056;
	private final IAdvisor villainModeller;

	public NLGameSolver(IAdvisor villainModeller) {
		this.villainModeller = villainModeller;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mallorcatour.bot.math.IGameSolver#onSecondActionPreflop(int, int,
	 * int, int, double, double, double, mallorcatour.core.spectrum.Spectrum,
	 * mallorcatour.core.game.HoleCards, java.util.Map, boolean, double)
	 */
	@Override
	public Map<Action, Double> onSecondActionPreflop(int heroActions, int heroAggressiveActions, int villainActions,
			int villainAggressiveActions, double effectiveStack, double pot, double toCall, Spectrum villainSpectrum,
			HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton, double bigBlind) {
		return onSecondActionRecursive(heroActions, heroAggressiveActions, villainActions, villainAggressiveActions,
				effectiveStack, pot, toCall, 0, villainSpectrum, new Card[] {}, heroCards, strengthMap, isHeroOnButton,
				PokerStreet.PREFLOP, bigBlind, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mallorcatour.bot.math.IGameSolver#onSecondActionFlop(int, int, int,
	 * int, double, double, double, mallorcatour.core.spectrum.Spectrum,
	 * mallorcatour.core.game.Flop, mallorcatour.core.game.HoleCards,
	 * java.util.Map, boolean, double)
	 */
	@Override
	public Map<Action, Double> onSecondActionFlop(int heroActions, int heroAggressiveActions, int villainActions,
			int villainAggressiveActions, double effectiveStack, double pot, double toCall, Spectrum villainSpectrum,
			Flop flop, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton,
			double bigBlind) {
		return onSecondActionRecursive(heroActions, heroAggressiveActions, villainActions, villainAggressiveActions,
				effectiveStack, pot, toCall, 0, villainSpectrum, flop.toArray(), heroCards, strengthMap,
				isHeroOnButton, PokerStreet.FLOP, bigBlind, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mallorcatour.bot.math.IGameSolver#onSecondActionTurn(int, int, int,
	 * int, double, double, double, mallorcatour.core.spectrum.Spectrum,
	 * mallorcatour.core.game.Flop, mallorcatour.core.game.Card,
	 * mallorcatour.core.game.HoleCards, java.util.Map, boolean, double)
	 */
	@Override
	public Map<Action, Double> onSecondActionTurn(int heroActions, int heroAggressiveActions, int villainActions,
			int villainAggressiveActions, double effectiveStack, double pot, double toCall, Spectrum villainSpectrum,
			Flop flop, Card turn, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
			boolean isHeroOnButton, double bigBlind) {
		return onSecondActionRecursive(heroActions, heroAggressiveActions, villainActions, villainAggressiveActions,
				effectiveStack, pot, toCall, 0, villainSpectrum,
				new Card[] { flop.first, flop.second, flop.third, turn }, heroCards, strengthMap, isHeroOnButton,
				PokerStreet.TURN, bigBlind, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mallorcatour.bot.math.IGameSolver#onSecondActionRiver(int, int, int,
	 * int, double, double, double, mallorcatour.core.spectrum.Spectrum,
	 * mallorcatour.core.game.Flop, mallorcatour.core.game.Card,
	 * mallorcatour.core.game.Card, mallorcatour.core.game.HoleCards,
	 * java.util.Map, boolean, double)
	 */
	@Override
	public Map<Action, Double> onSecondActionRiver(int heroActions, int heroAggressiveActions, int villainActions,
			int villainAggressiveActions, double effectiveStack, double pot, double toCall, Spectrum villainSpectrum,
			Flop flop, Card turn, Card river, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
			boolean isHeroOnButton, double bigBlind) {
		return onSecondActionRecursive(heroActions, heroAggressiveActions, villainActions, villainAggressiveActions,
				effectiveStack, pot, toCall, 0, villainSpectrum, new Card[] { flop.first, flop.second, flop.third,
						turn, river }, heroCards, strengthMap, isHeroOnButton, PokerStreet.RIVER, bigBlind, 0);
	}

	private double calculateHeroCallProfit(double strength, double pot, double heroInvestment, double toCall,
			double bigBlind, PokerStreet street, boolean onButton, HoleCards heroCards, Card[] board,
			Spectrum villainSpectrum) {
		double passiveProfit = strength * (pot - heroInvestment) - (1 - strength) * (toCall + heroInvestment);
		if (street == PokerStreet.PREFLOP && passiveProfit > 0) {
			int add = onButton ? 1 : -1;
			passiveProfit += add * IP_ADDITIONAL_PROFIT_BB * bigBlind;
		} else if (street == PokerStreet.FLOP || street == PokerStreet.TURN) {
			StreetEquity equity = PokerEquilatorBrecher.equityVsSpectrum(heroCards, board, villainSpectrum);
			double realEquity = StreetEquity.calculateRealEquity(equity);
			double nextStreetPot;
			if (street == PokerStreet.FLOP) {
				nextStreetPot = TURN_POT_COEFF * bigBlind;
			} else {
				nextStreetPot = RIVER_POT_COEFF * bigBlind;
			}
			double additionalPassiveProfit = realEquity * nextStreetPot - nextStreetPot / 2;
			if (toCall != 0) {
				passiveProfit += additionalPassiveProfit;
			}
		}
		return passiveProfit;
	}

	private Map<Action, Double> onSecondActionRecursive(int heroActions, int heroAggressiveActions,
            int villainActions, int villainAggressiveActions,
            double effectiveStack, double pot, double toCall,
            double heroInvestment, Spectrum villainSpectrum,
            Card[] board, HoleCards heroCards, Map<HoleCards, StreetEquity> villainStrengthMap,
            boolean onButton, PokerStreet street, double bigBlind, int depth) {
        Map<Action, Double> result = new HashMap<Action, Double>();
        //if hero folds
		String prefix = "";
		for (int i = 0; i < depth; i++) {
			prefix += "	";
		}
        double foldProfit = -heroInvestment;
        Log.f(prefix + "If hero folds he wins " + foldProfit);
		result.put(Action.foldAction(), foldProfit);
		// if hero calls
		double strength = PokerEquilatorBrecher.strengthVsSpectrum(heroCards, board, villainSpectrum);
		double passiveProfit = calculateHeroCallProfit(strength, pot, heroInvestment, toCall, bigBlind, street,
				onButton, heroCards, board, villainSpectrum);
		Log.f(prefix + "If hero calls he wins " + passiveProfit);
        result.put(Action.callAction(toCall), passiveProfit);
        //if hero cannot raise
        if (effectiveStack == 0) {
        	Log.f(prefix + "Hero cannot raise");
            return result;
        }
        //if hero raises
        Action heroReraiseAction = Action.createRaiseAction(toCall, pot, effectiveStack);
        double amount;
        if (heroReraiseAction.isAllin()) {
            amount = effectiveStack;
        } else {
            amount = heroReraiseAction.getAmount();
        }
        Log.f(prefix + "If hero raise " + amount);

        double aggressiveProfit = calculateHeroActionProfit(amount,
                heroActions, heroAggressiveActions,
                villainActions, villainAggressiveActions, effectiveStack, pot,
                toCall, heroInvestment, villainSpectrum, board, heroCards,
                toCall != 0, true, onButton, street, villainStrengthMap, bigBlind, depth + 1);
        Log.f(prefix + "Hero wins " + aggressiveProfit);

        result.put(heroReraiseAction, aggressiveProfit);
        return result;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see mallorcatour.bot.math.IGameSolver#onFirstActionPreFlop(int, int,
	 * int, int, double, double, mallorcatour.core.spectrum.Spectrum,
	 * mallorcatour.core.game.HoleCards, java.util.Map, boolean, boolean,
	 * double)
	 */
	@Override
	public Map<Action, Double> onFirstActionPreFlop(int heroActions, int heroAggressiveActions, int villainActions,
			int villainAggressiveActions, double effectiveStack, double pot, Spectrum villainSpectrum,
			HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap, boolean wasVillainPreviousAggressive,
			boolean isHeroOnButton, double bigBlind) {
		Map<Action, Double> result = new HashMap<Action, Double>();

		// if hero folds
		double foldProfit = 0;
		result.put(Action.foldAction(), foldProfit);
		// if hero checks
		double callProfit = calculateHeroActionProfit(0, heroActions, heroAggressiveActions, villainActions,
				villainAggressiveActions, effectiveStack, pot, /* hero toCall */bigBlind / 2, 0, villainSpectrum,
				new Card[] {}, heroCards, wasVillainPreviousAggressive, false, isHeroOnButton, PokerStreet.PREFLOP,
				strengthMap, bigBlind, 0);
		result.put(Action.callAction(bigBlind / 2), callProfit);

		// if hero raises
		double raiseAmount = bigBlind;
		Action heroRaiseAction = Action.createRaiseAction(raiseAmount, -1);

		double aggressiveProfit = calculateHeroActionProfit(raiseAmount, heroActions, heroAggressiveActions,
				villainActions, villainAggressiveActions, effectiveStack, pot, bigBlind / 2, 0, villainSpectrum,
				new Card[] {}, heroCards, wasVillainPreviousAggressive, true, isHeroOnButton, PokerStreet.PREFLOP,
				strengthMap, bigBlind, 0);
		result.put(heroRaiseAction, aggressiveProfit);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mallorcatour.bot.math.IGameSolver#onFirstActionFlop(int, int, int,
	 * int, double, double, mallorcatour.core.spectrum.Spectrum,
	 * mallorcatour.core.game.Flop, mallorcatour.core.game.HoleCards,
	 * java.util.Map, boolean, boolean, double)
	 */
	@Override
	public Map<Action, Double> onFirstActionFlop(int heroActions, int heroAggressiveActions, int villainActions,
			int villainAggressiveActions, double effectiveStack, double pot, Spectrum villainSpectrum, Flop flop,
			HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap, boolean wasVillainPreviousAggressive,
			boolean isHeroOnButton, double bigBlind) {
		return onFirstAction(heroActions, heroAggressiveActions, villainActions, villainAggressiveActions,
				effectiveStack, pot, villainSpectrum, flop.toArray(), heroCards, strengthMap,
				wasVillainPreviousAggressive, isHeroOnButton, PokerStreet.FLOP, bigBlind);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mallorcatour.bot.math.IGameSolver#onFirstActionTurn(int, int, int,
	 * int, double, double, mallorcatour.core.spectrum.Spectrum,
	 * mallorcatour.core.game.Flop, mallorcatour.core.game.Card,
	 * mallorcatour.core.game.HoleCards, java.util.Map, boolean, boolean,
	 * double)
	 */
	@Override
	public Map<Action, Double> onFirstActionTurn(int heroActions, int heroAggressiveActions, int villainActions,
			int villainAggressiveActions, double effectiveStack, double pot, Spectrum villainSpectrum, Flop flop,
			Card turn, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
			boolean wasVillainPreviousAggressive, boolean isHeroOnButton, double bigBlind) {
		return onFirstAction(heroActions, heroAggressiveActions, villainActions, villainAggressiveActions,
				effectiveStack, pot, villainSpectrum, new Card[] { flop.first, flop.second, flop.third, turn },
				heroCards, strengthMap, wasVillainPreviousAggressive, isHeroOnButton, PokerStreet.TURN, bigBlind);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mallorcatour.bot.math.IGameSolver#onFirstActionRiver(int, int, int,
	 * int, double, double, mallorcatour.core.spectrum.Spectrum,
	 * mallorcatour.core.game.Flop, mallorcatour.core.game.Card,
	 * mallorcatour.core.game.Card, mallorcatour.core.game.HoleCards,
	 * java.util.Map, boolean, boolean, double)
	 */
	@Override
	public Map<Action, Double> onFirstActionRiver(int heroActions, int heroAggressiveActions, int villainActions,
			int villainAggressiveActions, double effectiveStack, double pot, Spectrum villainSpectrum, Flop flop,
			Card turn, Card river, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
			boolean wasVillainPreviousAggressive, boolean isHeroOnButton, double bigBlind) {
		return onFirstAction(heroActions, heroAggressiveActions, villainActions, villainAggressiveActions,
				effectiveStack, pot, villainSpectrum, new Card[] { flop.first, flop.second, flop.third, turn, river },
				heroCards, strengthMap, wasVillainPreviousAggressive, isHeroOnButton, PokerStreet.RIVER, bigBlind);
	}

	// effective stack by Andrew
	private Map<Action, Double> onFirstAction(int heroActions, int heroAggressiveActions, int villainActions,
			int villainAggressiveActions, double effectiveStack, double pot, Spectrum villainSpectrum, Card[] board,
			HoleCards heroCards, Map<HoleCards, StreetEquity> villainStrengthMap, boolean wasVillainPreviousAggressive,
			boolean isHeroOnButton, PokerStreet street, double bigBlind) {
		Map<Action, Double> result = new HashMap<Action, Double>();

		// if hero folds
		double foldProfit = 0;
		result.put(Action.foldAction(), foldProfit);
		// if hero checks
		Log.f("If hero checks");
		double checkProfit = calculateHeroActionProfit(0, heroActions, heroAggressiveActions, villainActions,
				villainAggressiveActions, effectiveStack, pot, 0, 0, villainSpectrum, board, heroCards,
				wasVillainPreviousAggressive, false, isHeroOnButton, street, villainStrengthMap, bigBlind, 0);
		result.put(Action.checkAction(), checkProfit);

		// if hero bets
		Action heroBetAction = Action.createBetAction(pot, effectiveStack);
		double betAmount;
		if (heroBetAction.isAllin()) {
			betAmount = effectiveStack;
		} else {
			betAmount = heroBetAction.getAmount();
		}
		Log.f("If hero raise " + betAmount);

		double aggressiveProfit = calculateHeroActionProfit(betAmount, heroActions, heroAggressiveActions,
				villainActions, villainAggressiveActions, effectiveStack, pot, 0, 0, villainSpectrum, board, heroCards,
				wasVillainPreviousAggressive, true, isHeroOnButton, street, villainStrengthMap, bigBlind, 0);

		Log.f("Hero wins " + aggressiveProfit);

		result.put(heroBetAction, aggressiveProfit);
		return result;
	}

	private double calculateHeroActionProfit(double heroActionAmount, int heroActions, int heroAggressiveActions,
			int villainActions, int villainAggressiveActions, double effectiveStack, double pot, double heroToCall,
			double heroInvestment, Spectrum villainSpectrum, Card[] board, HoleCards heroCards,
			boolean wasVillainPreviousAggressive, boolean wasHeroPreviousAggressive, boolean isHeroOnButton,
			PokerStreet street, Map<HoleCards, StreetEquity> villainStrengthMap, double bigBlind, int depth) {

		double villainToCall = heroActionAmount;
		double villainPot = pot + heroToCall + villainToCall;
		double villainEffectiveStack = effectiveStack - villainToCall;
		LocalSituation villainSituation = getVillainSituationWithoutStrength(street, !isHeroOnButton, villainToCall,
				villainPot, villainEffectiveStack, heroActions + 1, heroAggressiveActions
						+ (wasHeroPreviousAggressive ? 1 : 0), villainActions, villainAggressiveActions,
				wasVillainPreviousAggressive, wasHeroPreviousAggressive);
		String prefix = "";
		for (int i = 0; i < depth; i++) {
			prefix += "	";
		}
		Log.f(prefix + "Villain situation: " + villainSituation.toString());
		Spectrum foldSpectrum = new Spectrum();
		Spectrum passiveSpectrum = new Spectrum();
		Spectrum aggressiveSpectrum = new Spectrum();

		processVillainSpectrums(villainSpectrum, foldSpectrum, passiveSpectrum, aggressiveSpectrum, villainSituation,
				villainStrengthMap);

		double villainSpectrumWeight = villainSpectrum.summaryWeight();
		double foldEquity = foldSpectrum.summaryWeight();
		double passiveEquity = passiveSpectrum.summaryWeight();
		double aggressiveEquity = aggressiveSpectrum.summaryWeight();

		if (Math.abs(foldEquity + passiveEquity + aggressiveEquity - villainSpectrumWeight) > NEARLY_ZERO) {
			throw new RuntimeException("Equities must be equals in summary. " + "Fold equity: " + foldEquity
					+ " call equity: " + passiveEquity + " raise equity: " + aggressiveEquity + ". Summary: "
					+ villainSpectrumWeight);
		}
		double ifVillainFoldProfit = (pot - heroInvestment);

		Log.f(prefix + "Villain will fold in " + (int) (foldEquity / villainSpectrumWeight * 100) + "%");
		Log.f(prefix + "Hero wins " + ifVillainFoldProfit);

		// if villain calls
		double ifVillainPassiveProfit;
		Log.f(prefix + "Villain will call in " + (int) (passiveEquity / villainSpectrumWeight * 100) + "%");
		if (passiveSpectrum.isEmpty()) {
			ifVillainPassiveProfit = 0;
		} else {
			double strength = PokerEquilatorBrecher.strengthVsSpectrum(heroCards, board, passiveSpectrum);
			ifVillainPassiveProfit = strength * (pot - heroInvestment + villainToCall) - (1 - strength)
					* (heroInvestment + heroToCall + villainToCall);
			if (street == PokerStreet.PREFLOP && ifVillainPassiveProfit > 0) {
				int add = isHeroOnButton ? 1 : -1;
				ifVillainPassiveProfit += add * IP_ADDITIONAL_PROFIT_BB * bigBlind;
			} else if (street == PokerStreet.FLOP || street == PokerStreet.TURN) {
				StreetEquity equity = PokerEquilatorBrecher.equityVsSpectrum(heroCards, board, passiveSpectrum);
				double realEquity = StreetEquity.calculateRealEquity(equity);
				double nextStreetPot;
				if (street == PokerStreet.FLOP) {
					nextStreetPot = TURN_POT_COEFF * bigBlind;
				} else {
					nextStreetPot = RIVER_POT_COEFF * bigBlind;
				}
				double additionalVillainPassiveProfit = realEquity * nextStreetPot - nextStreetPot / 2;
				if (heroActionAmount != 0) {
					ifVillainPassiveProfit += additionalVillainPassiveProfit;
				}
			}
		}
		Log.f(prefix + "Hero wins " + ifVillainPassiveProfit);
		// if villain aggressives
		Action villainAggressiveAction = Action.createRaiseAction(villainToCall, pot + heroToCall + villainToCall,
				villainEffectiveStack);

		double villainReraiseAmount;
		if (villainAggressiveAction.isAllin()) {
			villainReraiseAmount = effectiveStack - villainToCall;
		} else {
			villainReraiseAmount = villainAggressiveAction.getAmount();
		}
		double ifVillainAggressiveProfit;
		if (aggressiveSpectrum.isEmpty()) {
			ifVillainAggressiveProfit = 0;
			Log.f(prefix + "Villain will not reraise");
		} else {
			Log.f(prefix + "Villain will reraise " + villainReraiseAmount + " in "
					+ (int) (aggressiveEquity / villainSpectrumWeight * 100) + "%");
			double effectiveStackAfterVillainAggressive = effectiveStack - villainToCall - villainReraiseAmount;
			double potAfterVillainAggressive = pot + heroToCall + 2 * villainToCall + villainReraiseAmount;
			Map<Action, Double> map = onSecondActionRecursive(heroActions + 1, heroAggressiveActions
					+ (wasHeroPreviousAggressive ? 1 : 0), villainActions + 1, villainAggressiveActions + 1,
					effectiveStackAfterVillainAggressive, potAfterVillainAggressive, villainReraiseAmount,
					heroInvestment + heroToCall + villainToCall, aggressiveSpectrum, board, heroCards,
					villainStrengthMap, isHeroOnButton, street, bigBlind, depth + 1);
			ifVillainAggressiveProfit = CollectionUtils.maxValue(map);
			Log.f(prefix + "Hero wins " + ifVillainAggressiveProfit);
		}
		double aggressiveProfit = (foldEquity / villainSpectrumWeight) * ifVillainFoldProfit
				+ (passiveEquity / villainSpectrumWeight) * ifVillainPassiveProfit
				+ (aggressiveEquity / villainSpectrumWeight) * ifVillainAggressiveProfit;
		return aggressiveProfit;
	}

	private void processVillainSpectrums(Spectrum villainSpectrum, Spectrum foldSpectrum, Spectrum passiveSpectrum,
			Spectrum aggressiveSpectrum, LocalSituation villainSituation, Map<HoleCards, StreetEquity> strengthMap) {
		for (HoleCards villainCards : villainSpectrum) {
			StreetEquity equity = strengthMap.get(villainCards);
			double strength = equity.strength;
			// TODO add positive and negative potentials to villain situation on
			// preflop and river
			villainSituation.setStrength(strength);
			if (villainSituation.getStreet() == LocalSituation.FLOP
					|| villainSituation.getStreet() == LocalSituation.TURN) {
				villainSituation.setPositivePotential(equity.positivePotential);
				villainSituation.setNegativePotential(equity.negativePotential);
			}
			IAdvice advice = villainModeller.getAdvice(villainSituation, villainCards, null);
			double oldWeight = villainSpectrum.getWeight(villainCards);
			foldSpectrum.add(villainCards, oldWeight * advice.getFold());
			passiveSpectrum.add(villainCards, oldWeight * advice.getPassive());
			aggressiveSpectrum.add(villainCards, oldWeight * advice.getAggressive());
		}
	}

	private LocalSituation getVillainSituationWithoutStrength(PokerStreet street, boolean villainOnButton,
			double toCall, double pot, double effectiveStack, int heroActions, int heroAggressiveActions,
			int villainActions, int villainAggressiveActions, boolean wasVillainPreviousAggressive,
			boolean wasHeroPreviousAggressive) {
		LocalSituation result = null;
		// potOdds
		double potOdds = toCall / (toCall + pot);
		if (street == PokerStreet.PREFLOP) {
			result = new LocalSituation(LocalSituation.PREFLOP, LimitType.NO_LIMIT);
		} else if (street == PokerStreet.FLOP) {
			// equities
			result = new LocalSituation(LocalSituation.FLOP, LimitType.NO_LIMIT);
			result.setPositivePotential(DEFAULT_POSITIVE_POTENTIAL);
			result.setNegativePotential(DEFAULT_NEGATIVE_POTENTIAL);
		} else if (street == PokerStreet.TURN) {
			// equities
			result = new LocalSituation(LocalSituation.TURN, LimitType.NO_LIMIT);
			result.setPositivePotential(DEFAULT_POSITIVE_POTENTIAL);
			result.setNegativePotential(DEFAULT_NEGATIVE_POTENTIAL);
		} else if (street == PokerStreet.RIVER) {
			// equities
			result = new LocalSituation(LocalSituation.RIVER, LimitType.NO_LIMIT);
		}
		result.setVillainActionCount(heroActions);
		result.setVillainAggresionActionCount(heroAggressiveActions);
		result.setHeroActionCount(villainActions);
		result.setHeroAggresionActionCount(villainAggressiveActions);
		result.wasOpponentPreviousAggresive(wasHeroPreviousAggressive);
		result.wasHeroPreviousAggresive(wasVillainPreviousAggressive);
		result.setPotOdds(potOdds);
		result.isOnButton(villainOnButton);
		Log.f("Pot: " + pot + " toCall: " + toCall + " effectiveStack: " + effectiveStack);
		result.setPotToStackOdds((pot + toCall) / (pot + toCall + effectiveStack));
		result.canRaise(effectiveStack != 0);
		return result;
	}
}

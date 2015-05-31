/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.math;

import java.util.HashMap;
import java.util.Map;

import mallorcatour.bot.math.AggressionInfoBuilder.AggressionInfo;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.math.IGameSolver;
import mallorcatour.core.equilator.EquilatorStrength;
import mallorcatour.core.equilator.StreetEquity;
import mallorcatour.core.equilator.preflop.EquilatorPreflop;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.Flop;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IAggressionInfo;
import mallorcatour.core.game.interfaces.IGameInfo;
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
	private final IAdvisor villainModeller;

	public NLGameSolver(IAdvisor villainModeller) {
		this.villainModeller = villainModeller;
	}

	@Override
	public Map<Action, Double> onSecondActionPreflop(IGameInfo gameInfo, IAggressionInfo aggressionInfo,
			double effectiveStack, double pot, double toCall, Spectrum villainSpectrum, HoleCards heroCards,
			Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton, double bigBlind) {
		return onSecondActionRecursive(gameInfo, aggressionInfo, effectiveStack, pot, toCall, 0, villainSpectrum,
				new Card[] {}, heroCards, strengthMap, isHeroOnButton, PokerStreet.PREFLOP, bigBlind, 0);
	}

	@Override
	public Map<Action, Double> onSecondActionFlop(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack,
			double pot, double toCall, Spectrum villainSpectrum, Flop flop, HoleCards heroCards,
			Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton, double bigBlind) {
		return onSecondActionRecursive(gameInfo, info, effectiveStack, pot, toCall, 0, villainSpectrum, flop.toArray(),
				heroCards, strengthMap, isHeroOnButton, PokerStreet.FLOP, bigBlind, 0);
	}

	@Override
	public Map<Action, Double> onSecondActionTurn(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack,
			double pot, double toCall, Spectrum villainSpectrum, Flop flop, Card turn, HoleCards heroCards,
			Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton, double bigBlind) {
		return onSecondActionRecursive(gameInfo, info, effectiveStack, pot, toCall, 0, villainSpectrum, new Card[] {
				flop.first, flop.second, flop.third, turn }, heroCards, strengthMap, isHeroOnButton, PokerStreet.TURN,
				bigBlind, 0);
	}

	@Override
	public Map<Action, Double> onSecondActionRiver(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack,
			double pot, double toCall, Spectrum villainSpectrum, Flop flop, Card turn, Card river, HoleCards heroCards,
			Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton, double bigBlind) {
		return onSecondActionRecursive(gameInfo, info, effectiveStack, pot, toCall, 0, villainSpectrum, new Card[] {
				flop.first, flop.second, flop.third, turn, river }, heroCards, strengthMap, isHeroOnButton,
				PokerStreet.RIVER, bigBlind, 0);
	}

	/**
	 * pot - after call
	 */
	private double calculatePassiveProfit(IGameInfo gameInfo, double pot, double heroInvestment, double toCall,
			double bigBlind, PokerStreet street, boolean onButton, HoleCards heroCards, Card[] board,
			Spectrum villainSpectrum, int depth) {
		double strength;
		if (board.length > 0) {
			StreetEquity equity = EquilatorStrength.equityVsSpectrumToRiver(heroCards.first, heroCards.second, board,
					villainSpectrum);
			strength = equity.strength;
		} else {
			strength = EquilatorPreflop.strengthVsSpectrum(heroCards.first, heroCards.second, villainSpectrum);
		}
		double previousStreetPot;
		if (street != PokerStreet.PREFLOP) {
			previousStreetPot = gameInfo.getPot(street.previous());
		} else {
			previousStreetPot = 0;
		}
		double win = pot - heroInvestment - previousStreetPot;
//		double win = pot - heroInvestment;
		double lose = heroInvestment;
		String prefix = "";
		for (int i = 0; i < depth; i++) {
			prefix += "	";
		}
		Log.f(prefix + "Win " + win + " in " + (int) (100 * strength));
		Log.f(prefix + "Lose " + lose + " in " + (int) (100 * (1 - strength)));
		double passiveProfit = strength * win - (1 - strength) * lose;
		return passiveProfit;
	}

	private Map<Action, Double> onSecondActionRecursive(IGameInfo gameInfo, IAggressionInfo info,
			double effectiveStack, double pot, double toCall, double heroInvestment, Spectrum villainSpectrum,
			Card[] board, HoleCards heroCards, Map<HoleCards, StreetEquity> villainStrengthMap, boolean onButton,
			PokerStreet street, double bigBlind, int depth) {
		Map<Action, Double> result = new HashMap<Action, Double>();
		// if hero folds
		String prefix = "";
		for (int i = 0; i < depth; i++) {
			prefix += "	";
		}
		double foldProfit = -heroInvestment;
		Log.f(prefix + "If hero folds he wins " + foldProfit);
		result.put(Action.foldAction(), foldProfit);
		// if hero calls
		double passiveProfit = calculatePassiveProfit(gameInfo, pot + toCall, heroInvestment + toCall, toCall,
				bigBlind, street, onButton, heroCards, board, villainSpectrum, depth);
		Log.f(prefix + "If hero calls he wins " + passiveProfit);
		result.put(Action.callAction(toCall), passiveProfit);
		// if hero cannot raise
		if (effectiveStack == 0) {
			Log.f(prefix + "Hero cannot raise");
			return result;
		}
		// if hero raises
		Action heroReraiseAction = Action.createRaiseAction(toCall, pot, effectiveStack);
		double amount;
		if (heroReraiseAction.isAllin()) {
			amount = effectiveStack;
		} else {
			amount = heroReraiseAction.getAmount();
		}
		Log.f(prefix + "If hero raise " + amount);

		double aggressiveProfit = calculateHeroActionProfit(gameInfo, info, amount, effectiveStack, pot + toCall
				+ amount, heroInvestment + toCall + amount, villainSpectrum, board, heroCards, toCall != 0, true,
				onButton, street, villainStrengthMap, bigBlind, depth + 1);
		Log.f(prefix + "Hero wins " + aggressiveProfit);

		result.put(heroReraiseAction, aggressiveProfit);
		return result;
	}

	@Override
	public Map<Action, Double> onFirstActionPreFlop(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack,
			double pot, Spectrum villainSpectrum, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
			boolean wasVillainPreviousAggressive, boolean isHeroOnButton, double bigBlind) {
		Map<Action, Double> result = new HashMap<Action, Double>();

		// if hero folds
		double foldProfit = 0;
		result.put(Action.foldAction(), foldProfit);
		// if hero call small blind
		double callProfit = calculateHeroActionProfit(gameInfo, info, 0, effectiveStack, pot + bigBlind / 2,
				bigBlind / 2, villainSpectrum, new Card[] {}, heroCards, wasVillainPreviousAggressive, false,
				isHeroOnButton, PokerStreet.PREFLOP, strengthMap, bigBlind, 0);
		result.put(Action.callAction(bigBlind / 2), callProfit);

		// if hero raises
		double raiseAmount = bigBlind;
		Action heroRaiseAction = Action.createRaiseAction(raiseAmount, -1);

		double aggressiveProfit = calculateHeroActionProfit(gameInfo, info, raiseAmount, effectiveStack, pot + bigBlind
				/ 2 + raiseAmount, bigBlind / 2 + raiseAmount, villainSpectrum, new Card[] {}, heroCards,
				wasVillainPreviousAggressive, true, isHeroOnButton, PokerStreet.PREFLOP, strengthMap, bigBlind, 0);
		result.put(heroRaiseAction, aggressiveProfit);
		return result;
	}

	@Override
	public Map<Action, Double> onFirstActionFlop(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack,
			double pot, Spectrum villainSpectrum, Flop flop, HoleCards heroCards,
			Map<HoleCards, StreetEquity> strengthMap, boolean wasVillainPreviousAggressive, boolean isHeroOnButton,
			double bigBlind) {
		return onFirstAction(gameInfo, info, effectiveStack, pot, villainSpectrum, flop.toArray(), heroCards,
				strengthMap, wasVillainPreviousAggressive, isHeroOnButton, PokerStreet.FLOP, bigBlind);
	}

	@Override
	public Map<Action, Double> onFirstActionTurn(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack,
			double pot, Spectrum villainSpectrum, Flop flop, Card turn, HoleCards heroCards,
			Map<HoleCards, StreetEquity> strengthMap, boolean wasVillainPreviousAggressive, boolean isHeroOnButton,
			double bigBlind) {
		return onFirstAction(gameInfo, info, effectiveStack, pot, villainSpectrum, new Card[] { flop.first,
				flop.second, flop.third, turn }, heroCards, strengthMap, wasVillainPreviousAggressive, isHeroOnButton,
				PokerStreet.TURN, bigBlind);
	}

	@Override
	public Map<Action, Double> onFirstActionRiver(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack,
			double pot, Spectrum villainSpectrum, Flop flop, Card turn, Card river, HoleCards heroCards,
			Map<HoleCards, StreetEquity> strengthMap, boolean wasVillainPreviousAggressive, boolean isHeroOnButton,
			double bigBlind) {
		return onFirstAction(gameInfo, info, effectiveStack, pot, villainSpectrum, new Card[] { flop.first,
				flop.second, flop.third, turn, river }, heroCards, strengthMap, wasVillainPreviousAggressive,
				isHeroOnButton, PokerStreet.RIVER, bigBlind);
	}

	private Map<Action, Double> onFirstAction(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack,
			double pot, Spectrum villainSpectrum, Card[] board, HoleCards heroCards,
			Map<HoleCards, StreetEquity> villainStrengthMap, boolean wasVillainPreviousAggressive,
			boolean isHeroOnButton, PokerStreet street, double bigBlind) {
		Map<Action, Double> result = new HashMap<Action, Double>();

		// if hero folds
		double foldProfit = 0;
		result.put(Action.foldAction(), foldProfit);
		// if hero checks
		Log.f("If hero checks");
		double checkProfit = calculateHeroActionProfit(gameInfo, info, 0, effectiveStack, pot, 0, villainSpectrum,
				board, heroCards, wasVillainPreviousAggressive, false, isHeroOnButton, street, villainStrengthMap,
				bigBlind, 1);
		result.put(Action.checkAction(), checkProfit);
		Log.f("Hero wins " + checkProfit);

		// if hero bets
		Action heroBetAction = Action.createBetAction(pot, effectiveStack);
		double betAmount;
		if (heroBetAction.isAllin()) {
			betAmount = effectiveStack;
		} else {
			betAmount = heroBetAction.getAmount();
		}
		Log.f("If hero raise " + betAmount);

		double aggressiveProfit = calculateHeroActionProfit(gameInfo, info, betAmount, effectiveStack, pot + betAmount,
				betAmount, villainSpectrum, board, heroCards, wasVillainPreviousAggressive, true, isHeroOnButton,
				street, villainStrengthMap, bigBlind, 1);

		Log.f("Hero wins " + aggressiveProfit);

		result.put(heroBetAction, aggressiveProfit);
		return result;
	}

	private double calculateHeroActionProfit(IGameInfo gameInfo, IAggressionInfo info, double heroActionAmount,
			double effectiveStack, double pot, double heroInvestment, Spectrum villainSpectrum, Card[] board,
			HoleCards heroCards, boolean wasVillainPreviousAggressive, boolean wasHeroPreviousAggressive,
			boolean isHeroOnButton, PokerStreet street, Map<HoleCards, StreetEquity> villainStrengthMap,
			double bigBlind, int depth) {

		double villainToCall = heroActionAmount;
		double villainPot = pot;
		double villainEffectiveStack = effectiveStack - villainToCall;
		String prefix = "";
		for (int i = 0; i < depth; i++) {
			prefix += "	";
		}
		AggressionInfo newInfo = AggressionInfoBuilder.build(info).plusHeroAction();
		if (wasHeroPreviousAggressive) {
			newInfo.plusHeroAggressiveAction();
		}
		LocalSituation villainSituation = getVillainSituationWithoutStrength(street, !isHeroOnButton, villainToCall,
				villainPot, villainEffectiveStack, newInfo, wasVillainPreviousAggressive, wasHeroPreviousAggressive,
				prefix);
		Log.f(prefix + "Pot: " + pot + " toCall: " + villainToCall + " effectiveStack: " + villainEffectiveStack);
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

		Log.f(prefix + "When villain fold in " + (int) (foldEquity / villainSpectrumWeight * 100) + "%");
		Log.f(prefix + "Hero wins " + ifVillainFoldProfit);

		// if villain calls
		double ifVillainPassiveProfit;
		Log.f(prefix + "When villain call in " + (int) (passiveEquity / villainSpectrumWeight * 100) + "%");
		if (passiveSpectrum.isEmpty()) {
			ifVillainPassiveProfit = 0;
		} else {
			ifVillainPassiveProfit = calculatePassiveProfit(gameInfo, pot + villainToCall, heroInvestment,
					heroActionAmount, bigBlind, street, isHeroOnButton, heroCards, board, passiveSpectrum, depth);
		}
		Log.f(prefix + "Hero wins " + ifVillainPassiveProfit);

		// if villain aggressives
		Action villainAggressiveAction = Action.createRaiseAction(villainToCall, pot, villainEffectiveStack);

		double villainReraiseAmount;
		if (villainAggressiveAction.isAllin()) {
			villainReraiseAmount = effectiveStack - villainToCall;
		} else {
			villainReraiseAmount = villainAggressiveAction.getAmount();
		}
		double ifVillainAggressiveProfit;
		if (aggressiveSpectrum.isEmpty()) {
			ifVillainAggressiveProfit = 0;
			Log.f(prefix + "Villain cannot reraise");
		} else {
			Log.f(prefix + "When villain reraise " + villainReraiseAmount + " in "
					+ (int) (aggressiveEquity / villainSpectrumWeight * 100) + "%");
			double effectiveStackAfterVillainAggressive = villainEffectiveStack - villainReraiseAmount;
			double potAfterVillainAggressive = pot + villainToCall + villainReraiseAmount;
			newInfo.plusVillainAction();
			newInfo.plusVillainAggressiveAction();
			Map<Action, Double> map = onSecondActionRecursive(gameInfo, newInfo, effectiveStackAfterVillainAggressive,
					potAfterVillainAggressive, villainReraiseAmount, heroInvestment, aggressiveSpectrum, board,
					heroCards, villainStrengthMap, isHeroOnButton, street, bigBlind, depth + 1);
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
			villainSituation.setStrength(strength);
			villainSituation.setPositivePotential(equity.positivePotential);
			villainSituation.setNegativePotential(equity.negativePotential);
			IAdvice advice = villainModeller.getAdvice(villainSituation, villainCards, null);
			double oldWeight = villainSpectrum.getWeight(villainCards);
			foldSpectrum.add(villainCards, oldWeight * advice.getFold());
			passiveSpectrum.add(villainCards, oldWeight * advice.getPassive());
			aggressiveSpectrum.add(villainCards, oldWeight * advice.getAggressive());
		}
	}

	private LocalSituation getVillainSituationWithoutStrength(PokerStreet street, boolean villainOnButton,
			double toCall, double pot, double effectiveStack, IAggressionInfo info,
			boolean wasVillainPreviousAggressive, boolean wasHeroPreviousAggressive, String prefix) {
		LocalSituation result = null;
		double potOdds = toCall / (toCall + pot);
		if (street == PokerStreet.PREFLOP) {
			result = new LocalSituation(LocalSituation.PREFLOP, LimitType.NO_LIMIT);
		} else if (street == PokerStreet.FLOP) {
			result = new LocalSituation(LocalSituation.FLOP, LimitType.NO_LIMIT);
			result.setPositivePotential(DEFAULT_POSITIVE_POTENTIAL);
			result.setNegativePotential(DEFAULT_NEGATIVE_POTENTIAL);
		} else if (street == PokerStreet.TURN) {
			result = new LocalSituation(LocalSituation.TURN, LimitType.NO_LIMIT);
			result.setPositivePotential(DEFAULT_POSITIVE_POTENTIAL);
			result.setNegativePotential(DEFAULT_NEGATIVE_POTENTIAL);
		} else if (street == PokerStreet.RIVER) {
			result = new LocalSituation(LocalSituation.RIVER, LimitType.NO_LIMIT);
		}
		result.setAggressionInfoOnlyCount(info);
		result.wasOpponentPreviousAggresive(wasHeroPreviousAggressive);
		result.wasHeroPreviousAggresive(wasVillainPreviousAggressive);
		result.setPotOdds(potOdds);
		result.isOnButton(villainOnButton);
		result.setPotToStackOdds((pot + toCall) / (pot + toCall + effectiveStack));
		result.canRaise(effectiveStack != 0);
		return result;
	}
}

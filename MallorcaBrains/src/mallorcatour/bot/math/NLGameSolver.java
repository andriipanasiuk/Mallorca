/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.math;

import java.util.HashMap;
import java.util.Map;

import mallorcatour.bot.C;
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
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IAggressionInfo;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.core.spectrum.Spectrum;
import mallorcatour.tools.CollectionUtils;
import mallorcatour.tools.Log;

/**
 * 
 * @author Andrew
 */
public class NLGameSolver implements IGameSolver {

	public static boolean CONSIDER_PREV_POT = false;
	private final static double NEARLY_ZERO = 0.0001;
	private final static double DEFAULT_POSITIVE_POTENTIAL = 0.1;
	private final static double DEFAULT_NEGATIVE_POTENTIAL = 0.1;
	private final IAdvisor villainModel;

	public NLGameSolver(IAdvisor villainModeller) {
		this.villainModel = villainModeller;
	}

	@Override
	public Map<Action, Double> onSecondActionPreflop(IGameInfo gameInfo, IAggressionInfo aggressionInfo,
			double effectiveStack, double pot, double toCall, Spectrum villainSpectrum, HoleCards heroCards,
			Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton, double bigBlind) {
		return onSecondActionRecursive(gameInfo, aggressionInfo, effectiveStack, pot, toCall, 0, villainSpectrum,
				new Card[] {}, heroCards, strengthMap, isHeroOnButton, PokerStreet.PREFLOP, 0);
	}

	@Override
	public Map<Action, Double> onSecondActionFlop(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack,
			double pot, double toCall, Spectrum villainSpectrum, Flop flop, HoleCards heroCards,
			Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton, double bigBlind) {
		return onSecondActionRecursive(gameInfo, info, effectiveStack, pot, toCall, 0, villainSpectrum, flop.toArray(),
				heroCards, strengthMap, isHeroOnButton, PokerStreet.FLOP, 0);
	}

	@Override
	public Map<Action, Double> onSecondActionTurn(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack,
			double pot, double toCall, Spectrum villainSpectrum, Flop flop, Card turn, HoleCards heroCards,
			Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton, double bigBlind) {
		return onSecondActionRecursive(gameInfo, info, effectiveStack, pot, toCall, 0, villainSpectrum, new Card[] {
				flop.first, flop.second, flop.third, turn }, heroCards, strengthMap, isHeroOnButton, PokerStreet.TURN,
				0);
	}

	@Override
	public Map<Action, Double> onSecondActionRiver(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack,
			double pot, double toCall, Spectrum villainSpectrum, Flop flop, Card turn, Card river, HoleCards heroCards,
			Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton, double bigBlind) {
		return onSecondActionRecursive(gameInfo, info, effectiveStack, pot, toCall, 0, villainSpectrum, new Card[] {
				flop.first, flop.second, flop.third, turn, river }, heroCards, strengthMap, isHeroOnButton,
				PokerStreet.RIVER, 0);
	}

	/**
	 * pot - after call
	 */
	private double calculatePassiveProfit(IGameInfo gameInfo, double pot, double heroInvestment, PokerStreet street,
			HoleCards heroCards, Card[] board, Spectrum villainSpectrum, int depth) {
		double strength;
		if (board.length > 0) {
			StreetEquity equity = EquilatorStrength.equityVsSpectrumToRiver(heroCards.first, heroCards.second, board,
					villainSpectrum);
			strength = equity.strength;
		} else {
			strength = EquilatorPreflop.strengthVsSpectrum(heroCards.first, heroCards.second, villainSpectrum);
		}
		double win = pot - heroInvestment;
		if (!CONSIDER_PREV_POT) {
			double previousStreetPot;
			if (street != PokerStreet.PREFLOP) {
				previousStreetPot = gameInfo.getPot(street.previous());
			} else {
				previousStreetPot = 0;
			}
			Log.f(depth, C.POT + " on prev. str.: " + previousStreetPot);
			win -= previousStreetPot;
		}
		double lose = heroInvestment;
		Log.f(depth, "Win " + win + " in " + (int) (100 * strength) + "%");
		Log.f(depth, "Lose " + lose + " in " + (int) (100 * (1 - strength)) + "%");
		double passiveProfit = strength * win - (1 - strength) * lose;
		return passiveProfit;
	}

	private Map<Action, Double> onSecondActionRecursive(IGameInfo gameInfo, IAggressionInfo info,
			double effectiveStack, double pot, double toCall, double heroInvestment, Spectrum villainSpectrum,
			Card[] board, HoleCards heroCards, Map<HoleCards, StreetEquity> villainStrengthMap, boolean onButton,
			PokerStreet street, int depth) {
		Map<Action, Double> result = new HashMap<Action, Double>();
		// if hero folds
		double foldProfit = -heroInvestment;
		Log.f(depth, "If " + C.HERO + " " + C.FOLD + " he " + C.WINS + " " + foldProfit);
		result.put(Action.fold(), foldProfit);
		// if hero calls
		Log.f(depth, "If " + C.HERO + " " + C.CALL + " " + toCall);
		double passiveProfit = calculatePassiveProfit(gameInfo, pot + toCall, heroInvestment + toCall, street,
				heroCards, board, villainSpectrum, depth + 1);
		Log.f(depth, C.HERO + " " + C.WINS + " " + passiveProfit);
		result.put(Action.callAction(toCall), passiveProfit);
		// if hero cannot raise
		if (effectiveStack == 0) {
			Log.f(depth, C.HERO + " cannot " + C.RAISE);
			return result;
		}
		// if hero raises
		Action heroReraiseAction = Action.createRaiseAction(toCall, pot, effectiveStack, 1);
		double amount = heroReraiseAction.getAmount();
		Log.f(depth, "If " + C.HERO + " raise " + amount);

		double potAfterAction = pot + toCall + amount;
		double heroInvestmentAfterAction = heroInvestment + toCall + amount;
		double effectiveStackAfterAction = effectiveStack - amount;
		double aggressiveProfit = calculateHeroActionProfit(gameInfo, info, amount, effectiveStackAfterAction,
				potAfterAction, heroInvestmentAfterAction, villainSpectrum, board, heroCards, toCall != 0, true,
				onButton, street, villainStrengthMap, depth + 1);
		Log.f(depth, C.HERO + " " + C.WINS + " " + aggressiveProfit);

		result.put(heroReraiseAction, aggressiveProfit);
		return result;
	}

	@Override
	public Map<Action, Double> onFirstActionPreFlop(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack,
			double pot, Spectrum villainSpectrum, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
			boolean wasVillainPreviousAggressive, boolean isHeroOnButton, double bigBlind) {
		Map<Action, Double> result = new HashMap<Action, Double>();

		double foldProfit = 0;
		result.put(Action.fold(), foldProfit);

		double toCall = bigBlind / 2;
		Log.f("If " + C.HERO + " " + C.CALL + " " + toCall);
		double callProfit = calculateHeroActionProfit(gameInfo, info, 0, effectiveStack, pot + toCall,
				toCall, villainSpectrum, new Card[] {}, heroCards, wasVillainPreviousAggressive, false,
				isHeroOnButton, PokerStreet.PREFLOP, strengthMap, 1);
		result.put(Action.callAction(toCall), callProfit);

		Log.f(C.HERO + " " + C.WINS + " " + callProfit);

		Action heroRaiseAction = Action.createRaiseAction(toCall, pot, effectiveStack, 1);
		double raiseAmount = heroRaiseAction.getAmount();

		Log.f("If " + C.HERO + " " + C.RAISE + " " + raiseAmount);

		double aggressiveProfit = calculateHeroActionProfit(gameInfo, info, raiseAmount, effectiveStack - raiseAmount,
				pot + toCall + raiseAmount, toCall + raiseAmount, villainSpectrum, new Card[] {}, heroCards,
				wasVillainPreviousAggressive, true, isHeroOnButton, PokerStreet.PREFLOP, strengthMap, 1);
		result.put(heroRaiseAction, aggressiveProfit);
		Log.f(C.HERO + " " + C.WINS + " " + aggressiveProfit);
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
		result.put(Action.fold(), foldProfit);
		// if hero checks
		Log.f("If " + C.HERO + " " + C.CHECK);
		double checkProfit = calculateHeroActionProfit(gameInfo, info, 0, effectiveStack, pot, 0, villainSpectrum,
				board, heroCards, wasVillainPreviousAggressive, false, isHeroOnButton, street, villainStrengthMap, 1);
		result.put(Action.checkAction(), checkProfit);
		Log.f(C.HERO + " " + C.WINS + " " + checkProfit);

		// if hero bets
		Action heroBetAction = Action.createBetAction(pot, effectiveStack, 0.75);
		double betAmount;
		if (heroBetAction.isAllin()) {
			betAmount = effectiveStack;
		} else {
			betAmount = heroBetAction.getAmount();
		}
		Log.f("If " + C.HERO + " " + C.RAISE + " " + betAmount);

		double aggressiveProfit = calculateHeroActionProfit(gameInfo, info, betAmount, effectiveStack - betAmount, pot
				+ betAmount, betAmount, villainSpectrum, board, heroCards, wasVillainPreviousAggressive, true, isHeroOnButton,
				street, villainStrengthMap, 1);

		Log.f(C.HERO + " " + C.WINS + " " + aggressiveProfit);

		result.put(heroBetAction, aggressiveProfit);
		return result;
	}

	private double calculateHeroActionProfit(IGameInfo gameInfo, IAggressionInfo info, double heroActionAmount,
			double effectiveStack, double pot, double heroInvestment, Spectrum villainSpectrum, Card[] board,
			HoleCards heroCards, boolean wasVillainPreviousAggressive, boolean wasHeroPreviousAggressive,
			boolean isHeroOnButton, PokerStreet street, Map<HoleCards, StreetEquity> villainStrengthMap,
			int depth) {

		double villainToCall = heroActionAmount;
		double villainPot = pot;
		double villainEffectiveStack = effectiveStack;
		AggressionInfo newInfo = AggressionInfoBuilder.build(info).plusHeroAction();
		if (wasHeroPreviousAggressive) {
			newInfo.plusHeroAggressiveAction();
		}
		LocalSituation villainSituation = getVillainSituationWithoutStrength(street, !isHeroOnButton, villainToCall,
				villainPot, villainEffectiveStack, newInfo, wasVillainPreviousAggressive, wasHeroPreviousAggressive);
		Log.f(depth, C.POT + ": " + pot + " " + C.TO_CALL + ": " + villainToCall + " " + C.EFFECTIVE_STACK + ": "
				+ villainEffectiveStack);
		Log.f(depth, C.VILLAIN + " " + C.SITUATION + ": " + villainSituation.toString());

		Spectrum foldSpectrum = new Spectrum();
		Spectrum passiveSpectrum = new Spectrum();
		Spectrum aggressiveSpectrum = new Spectrum();

		processVillainSpectrums(villainSpectrum, foldSpectrum, passiveSpectrum, aggressiveSpectrum, villainSituation,
				villainStrengthMap);

		double villainSpectrumWeight = villainSpectrum.summaryWeight();
		double foldWeight = foldSpectrum.summaryWeight();
		double passiveWeight = passiveSpectrum.summaryWeight();
		double aggressiveWeight = aggressiveSpectrum.summaryWeight();

		if (Math.abs(foldWeight + passiveWeight + aggressiveWeight - villainSpectrumWeight) > NEARLY_ZERO) {
			throw new RuntimeException("Equities must be equals in summary. " + "Fold equity: " + foldWeight
					+ " call equity: " + passiveWeight + " raise equity: " + aggressiveWeight + ". Summary: "
					+ villainSpectrumWeight);
		}
		double ifVillainFoldProfit = (pot - heroInvestment);

		Log.f(depth, "When " + C.VILLAIN + " " + C.FOLD + " in " + (int) (foldWeight / villainSpectrumWeight * 100)
				+ "%");
		Log.f(depth, C.HERO + " " + C.WINS + " " + ifVillainFoldProfit);

		double ifVillainPassiveProfit;
		if (passiveSpectrum.isEmpty()) {
			ifVillainPassiveProfit = 0;
			Log.f(depth, C.VILLAIN + " will not " + C.CALL);
		} else {
			Log.f(depth, "When " + C.VILLAIN + " " + C.CALL + " in "
					+ (int) (passiveWeight / villainSpectrumWeight * 100) + "%");
			ifVillainPassiveProfit = calculatePassiveProfit(gameInfo, pot + villainToCall, heroInvestment,
					street, heroCards, board, passiveSpectrum, depth + 1);
			Log.f(depth, C.HERO + " " + C.WINS + " " + ifVillainPassiveProfit);
		}

		double percent;
		if (villainToCall != 0 || street == PokerStreet.PREFLOP) {
			percent = 1;
		} else {
			percent = 0.75;
		}
		Action villainAggressiveAction = Action.createRaiseAction(villainToCall, pot, villainEffectiveStack, percent);
		double villainReraiseAmount = villainAggressiveAction.getAmount();

		double ifVillainAggressiveProfit;
		if (aggressiveSpectrum.isEmpty()) {
			ifVillainAggressiveProfit = 0;
			Log.f(depth, C.VILLAIN + " won't raise");
		} else {
			Log.f(depth, "When " + C.VILLAIN + " raise " + villainReraiseAmount + " in "
					+ (int) (aggressiveWeight / villainSpectrumWeight * 100) + "%");
			double effectiveStackAfterVillainAggressive = villainEffectiveStack - villainReraiseAmount;
			double potAfterVillainAggressive = pot + villainToCall + villainReraiseAmount;
			newInfo.plusVillainAction(true);
			Map<Action, Double> map = onSecondActionRecursive(gameInfo, newInfo, effectiveStackAfterVillainAggressive,
					potAfterVillainAggressive, villainReraiseAmount, heroInvestment, aggressiveSpectrum, board,
					heroCards, villainStrengthMap, isHeroOnButton, street, depth + 1);
			ifVillainAggressiveProfit = CollectionUtils.maxValue(map);
			Log.f(depth, C.HERO + " " + C.WINS + " " + ifVillainAggressiveProfit);
		}
		double profit = (foldWeight / villainSpectrumWeight) * ifVillainFoldProfit
				+ (passiveWeight / villainSpectrumWeight) * ifVillainPassiveProfit
				+ (aggressiveWeight / villainSpectrumWeight) * ifVillainAggressiveProfit;
		return profit;
	}

	private void processVillainSpectrums(Spectrum villainSpectrum, Spectrum foldSpectrum, Spectrum passiveSpectrum,
			Spectrum aggressiveSpectrum, LocalSituation villainSituation, Map<HoleCards, StreetEquity> strengthMap) {
		for (HoleCards villainCards : villainSpectrum) {
			StreetEquity equity = strengthMap.get(villainCards);
			double strength = equity.strength;
			villainSituation.setStrength(strength);
			villainSituation.setPositivePotential(equity.positivePotential);
			villainSituation.setNegativePotential(equity.negativePotential);
			IAdvice advice = villainModel.getAdvice(villainSituation, villainCards, null);
			double oldWeight = villainSpectrum.getWeight(villainCards);
			foldSpectrum.add(villainCards, oldWeight * advice.getFold());
			passiveSpectrum.add(villainCards, oldWeight * advice.getPassive());
			aggressiveSpectrum.add(villainCards, oldWeight * advice.getAggressive());
		}
	}

	private LocalSituation getVillainSituationWithoutStrength(PokerStreet street, boolean villainOnButton,
			double toCall, double pot, double effectiveStack, IAggressionInfo info,
			boolean wasVillainPreviousAggressive, boolean wasHeroPreviousAggressive) {
		double potOdds = toCall / (toCall + pot);
		LocalSituation result = new LocalSituation(street.intValue());
		result.setPositivePotential(DEFAULT_POSITIVE_POTENTIAL);
		result.setNegativePotential(DEFAULT_NEGATIVE_POTENTIAL);
		result.setAggressionInfoOnlyCount(info, true);
		result.wasOpponentPreviousAggresive(wasHeroPreviousAggressive);
		result.wasHeroPreviousAggresive(wasVillainPreviousAggressive);
		result.setPotOdds(potOdds);
		result.isOnButton(villainOnButton);
		result.setPotToStackOdds((pot + toCall) / (pot + toCall + effectiveStack));
		result.canRaise(effectiveStack != 0);
		return result;
	}
}

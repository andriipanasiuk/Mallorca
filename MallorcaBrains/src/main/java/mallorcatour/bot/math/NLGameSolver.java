package mallorcatour.bot.math;

import java.util.Map;

import mallorcatour.bot.C;
import mallorcatour.core.game.advice.Advisor;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.Flop;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IAggressionInfo;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.game.interfaces.PreflopEquilator;
import mallorcatour.core.game.interfaces.SpectrumEquilator;
import mallorcatour.core.game.state.HandState;
import mallorcatour.core.game.state.StreetEquity;
import mallorcatour.core.math.RandomVariable;
import mallorcatour.core.spectrum.Spectrum;
import mallorcatour.tools.Log;

/**
 * 
 * @author Andrew
 */
public class NLGameSolver implements IGameSolver {

	public static boolean LOGGING = true;
	public static boolean CONSIDER_PREV_POT = false;
	private final static double NEARLY_ZERO = 0.0001;
	private final static double DEFAULT_POSITIVE_POTENTIAL = 0.1;
	private final static double DEFAULT_NEGATIVE_POTENTIAL = 0.1;
	private final Advisor villainModel;
	//TODO
	private final PreflopEquilator preflopEquilator = null;
	private final SpectrumEquilator spectrumEquilator = null;

	public NLGameSolver(Advisor villainModel) {
		this.villainModel = villainModel;
	}

	@Override
	public ActionDistribution onSecondActionPreflop(IGameInfo gameInfo, IAggressionInfo aggressionInfo,
			double effectiveStack, double pot, double toCall, Spectrum villainSpectrum, HoleCards heroCards,
			Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton, double bigBlind) {
		return onSecondActionRecursive(gameInfo, aggressionInfo, effectiveStack, pot, toCall, 0, villainSpectrum,
				new Card[] {}, heroCards, strengthMap, isHeroOnButton, PokerStreet.PREFLOP, 0);
	}

	@Override
	public ActionDistribution onSecondActionFlop(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack,
			double pot, double toCall, Spectrum villainSpectrum, Flop flop, HoleCards heroCards,
			Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton, double bigBlind) {
		return onSecondActionRecursive(gameInfo, info, effectiveStack, pot, toCall, 0, villainSpectrum, flop.toArray(),
				heroCards, strengthMap, isHeroOnButton, PokerStreet.FLOP, 0);
	}

	@Override
	public ActionDistribution onSecondActionTurn(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack,
			double pot, double toCall, Spectrum villainSpectrum, Flop flop, Card turn, HoleCards heroCards,
			Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton, double bigBlind) {
		return onSecondActionRecursive(gameInfo, info, effectiveStack, pot, toCall, 0, villainSpectrum, new Card[] {
				flop.first, flop.second, flop.third, turn }, heroCards, strengthMap, isHeroOnButton, PokerStreet.TURN,
				0);
	}

	@Override
	public ActionDistribution onSecondActionRiver(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack,
			double pot, double toCall, Spectrum villainSpectrum, Flop flop, Card turn, Card river, HoleCards heroCards,
			Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton, double bigBlind) {
		return onSecondActionRecursive(gameInfo, info, effectiveStack, pot, toCall, 0, villainSpectrum, new Card[] {
				flop.first, flop.second, flop.third, turn, river }, heroCards, strengthMap, isHeroOnButton,
				PokerStreet.RIVER, 0);
	}

	protected RandomVariable calculatePassiveProfit(IGameInfo gameInfo, double pot, double heroInvestment,
			PokerStreet street, HoleCards heroCards, Card[] board, Spectrum villainSpectrum, boolean onButton, int depth) {
		return calculatePassiveProfit(gameInfo, pot, heroInvestment, street, heroCards, board, villainSpectrum, onButton, false, depth);
	}

	protected RandomVariable calculatePassiveProfit(IGameInfo gameInfo, double pot, double heroInvestment,
				PokerStreet street, HoleCards heroCards, Card[] board, Spectrum villainSpectrum, boolean onButton,
				boolean considerPrevPot, int depth) {
		double win = pot - heroInvestment;
		if (!considerPrevPot) {
			double previousStreetPot;
			if (street != PokerStreet.PREFLOP) {
				previousStreetPot = gameInfo.getPot(street.previous());
			} else {
				previousStreetPot = 0;
			}
			log(depth, C.POT + " on prev. str.: " + previousStreetPot);
			win -= previousStreetPot;
		}
		double lose = heroInvestment;
		double strength;
		if (board.length > 0) {
			strength = spectrumEquilator.strengthVsSpectrum(heroCards.first, heroCards.second, board, villainSpectrum);
		} else {
			strength = preflopEquilator.strengthVsSpectrum(heroCards.first, heroCards.second, villainSpectrum);
		}
		RandomVariable result = new RandomVariable();
		result.add(strength, win);
		result.add(1 - strength, -lose);
		// log(depth, "Win " + win + " in " + (int) (100 * strength) + "%");
		// log(depth, "Lose " + lose + " in " + (int) (100 * (1 - strength)) +
		// "%");
		return result;
	}

	private ActionDistribution onSecondActionRecursive(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack,
			double pot, double toCall, double heroInvestment, Spectrum villainSpectrum, Card[] board,
			HoleCards heroCards, Map<HoleCards, StreetEquity> villainStrengthMap, boolean onButton, PokerStreet street,
			int depth) {
		ActionDistribution result = new ActionDistribution();
		// if hero folds
		double foldProfit = -heroInvestment;
		log(depth, "If " + C.HERO + " " + C.FOLD + " he " + C.WINS + " " + (foldProfit));
		result.put(Action.fold(), RandomVariable.create(foldProfit));

		// if hero calls
		if (toCall != 0) {
			log(depth, "If " + C.HERO + " " + C.CALL + " " + toCall);
		} else {
			log(depth, "If " + C.HERO + " " + C.CHECK);
		}
		RandomVariable passiveProfit = calculatePassiveProfit(gameInfo, pot + toCall, heroInvestment + toCall, street,
				heroCards, board, villainSpectrum,onButton, depth + 1);
		log(depth, C.HERO + " " + C.WINS + " " + passiveProfit.printProfitability(gameInfo));
		result.put(Action.callAction(toCall), passiveProfit);
		// if hero cannot raise
		if (effectiveStack == 0) {
			log(depth, C.HERO + " cannot " + C.RAISE);
			return result;
		}
		// if hero raises
		double percent = getRaisePercent(toCall, street);
		Action heroReraiseAction = Action.createRaiseAction(toCall, pot, effectiveStack, percent);
		double amount = heroReraiseAction.getAmount();
		log(depth, "If " + C.HERO + " " + C.RAISE + " " + amount);

		double potAfterAction = pot + toCall + amount;
		double heroInvestmentAfterAction = heroInvestment + toCall + amount;
		double effectiveStackAfterAction = effectiveStack - amount;
		RandomVariable aggressiveProfit = calculateHeroActionProfit(gameInfo, info, amount, effectiveStackAfterAction,
				potAfterAction, heroInvestmentAfterAction, villainSpectrum, board, heroCards, toCall != 0, true,
				onButton, street, villainStrengthMap, depth + 1);
		log(depth, C.HERO + " " + C.WINS + " "	+ aggressiveProfit.printProfitability(gameInfo));

		result.put(heroReraiseAction, aggressiveProfit);
		return result;
	}

	@Override
	public ActionDistribution onFirstActionPreFlop(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack,
			double pot, Spectrum villainSpectrum, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
			boolean wasVillainPreviousAggressive, boolean isHeroOnButton, double bigBlind) {
		ActionDistribution result = new ActionDistribution();

		double foldProfit = 0;
		result.put(Action.fold(), RandomVariable.create(foldProfit));

		double toCall = bigBlind / 2;
		log("If " + C.HERO + " " + C.CALL + " " + toCall);
		RandomVariable callProfit = calculateHeroActionProfit(gameInfo, info, 0, effectiveStack, pot + toCall, toCall,
				villainSpectrum, new Card[] {}, heroCards, wasVillainPreviousAggressive, false, isHeroOnButton,
				PokerStreet.PREFLOP, strengthMap, 1);
		result.put(Action.callAction(toCall), callProfit);

		log(C.HERO + " " + C.WINS + " " + callProfit.printProfitability(gameInfo));

		double percent = getRaisePercent(toCall, PokerStreet.PREFLOP);
		Action heroRaiseAction = Action.createRaiseAction(toCall, pot, effectiveStack, percent);
		double raiseAmount = heroRaiseAction.getAmount();

		log("If " + C.HERO + " " + C.RAISE + " " + raiseAmount);

		RandomVariable aggressiveProfit = calculateHeroActionProfit(gameInfo, info, raiseAmount, effectiveStack
				- raiseAmount, pot + toCall + raiseAmount, toCall + raiseAmount, villainSpectrum, new Card[] {},
				heroCards, wasVillainPreviousAggressive, true, isHeroOnButton, PokerStreet.PREFLOP, strengthMap, 1);
		result.put(heroRaiseAction, aggressiveProfit);
		log(C.HERO + " " + C.WINS + " " + aggressiveProfit.printProfitability(gameInfo));
		return result;
	}

	@Override
	public ActionDistribution onFirstActionFlop(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack,
			double pot, Spectrum villainSpectrum, Flop flop, HoleCards heroCards,
			Map<HoleCards, StreetEquity> strengthMap, boolean wasVillainPreviousAggressive, boolean isHeroOnButton,
			double bigBlind) {
		return onFirstAction(gameInfo, info, effectiveStack, pot, villainSpectrum, flop.toArray(), heroCards,
				strengthMap, wasVillainPreviousAggressive, isHeroOnButton, PokerStreet.FLOP, bigBlind);
	}

	@Override
	public ActionDistribution onFirstActionTurn(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack,
			double pot, Spectrum villainSpectrum, Flop flop, Card turn, HoleCards heroCards,
			Map<HoleCards, StreetEquity> strengthMap, boolean wasVillainPreviousAggressive, boolean isHeroOnButton,
			double bigBlind) {
		return onFirstAction(gameInfo, info, effectiveStack, pot, villainSpectrum, new Card[] { flop.first,
				flop.second, flop.third, turn }, heroCards, strengthMap, wasVillainPreviousAggressive, isHeroOnButton,
				PokerStreet.TURN, bigBlind);
	}

	@Override
	public ActionDistribution onFirstActionRiver(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack,
			double pot, Spectrum villainSpectrum, Flop flop, Card turn, Card river, HoleCards heroCards,
			Map<HoleCards, StreetEquity> strengthMap, boolean wasVillainPreviousAggressive, boolean isHeroOnButton,
			double bigBlind) {
		return onFirstAction(gameInfo, info, effectiveStack, pot, villainSpectrum, new Card[] { flop.first,
				flop.second, flop.third, turn, river }, heroCards, strengthMap, wasVillainPreviousAggressive,
				isHeroOnButton, PokerStreet.RIVER, bigBlind);
	}

	private ActionDistribution onFirstAction(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack,
			double pot, Spectrum villainSpectrum, Card[] board, HoleCards heroCards,
			Map<HoleCards, StreetEquity> villainStrengthMap, boolean wasVillainPreviousAggressive,
			boolean isHeroOnButton, PokerStreet street, double bigBlind) {
		ActionDistribution result = new ActionDistribution();

		// if hero folds
		double foldProfit = 0;
		result.put(Action.fold(), RandomVariable.create(foldProfit));
		// if hero checks
		log("If " + C.HERO + " " + C.CHECK);
		RandomVariable checkProfit = calculateHeroActionProfit(gameInfo, info, 0, effectiveStack, pot, 0,
				villainSpectrum, board, heroCards, wasVillainPreviousAggressive, false, isHeroOnButton, street,
				villainStrengthMap, 1);
		result.put(Action.checkAction(), checkProfit);
		log(C.HERO + " " + C.WINS + " " + checkProfit.printProfitability(gameInfo));

		// if hero bets
		Action heroBetAction = Action.createBetAction(pot, effectiveStack, 0.75);
		double betAmount;
		if (heroBetAction.isAllin()) {
			betAmount = effectiveStack;
		} else {
			betAmount = heroBetAction.getAmount();
		}
		log("If " + C.HERO + " " + C.RAISE + " " + betAmount);

		RandomVariable aggressiveProfit = calculateHeroActionProfit(gameInfo, info, betAmount, effectiveStack
				- betAmount, pot + betAmount, betAmount, villainSpectrum, board, heroCards,
				wasVillainPreviousAggressive, true, isHeroOnButton, street, villainStrengthMap, 1);

		log(C.HERO + " " + C.WINS + " " + aggressiveProfit.printProfitability(gameInfo));

		result.put(heroBetAction, aggressiveProfit);
		return result;
	}

	private RandomVariable calculateHeroActionProfit(IGameInfo gameInfo, IAggressionInfo info, double heroActionAmount,
			double effectiveStack, double pot, double heroInvestment, Spectrum villainSpectrum, Card[] board,
			HoleCards heroCards, boolean wasVillainPreviousAggressive, boolean wasHeroPreviousAggressive,
			boolean onButton, PokerStreet street, Map<HoleCards, StreetEquity> villainStrengthMap, int depth) {

		RandomVariable result = new RandomVariable();

		double villainToCall = heroActionAmount;
		double villainPot = pot;
		double villainEffectiveStack = effectiveStack;
		AggressionInfoBuffer newInfo = AggressionInfoBuilder.build(info).plusHeroAction();
		if (wasHeroPreviousAggressive) {
			newInfo.plusHeroAggressiveAction();
		}
		HandState villainSituation = getVillainSituationWithoutStrength(street, !onButton, villainToCall,
				villainPot, villainEffectiveStack, newInfo, wasVillainPreviousAggressive, wasHeroPreviousAggressive);
		log(depth, C.POT + ": " + pot + " " + C.TO_CALL + ": " + villainToCall + " " + C.EFFECTIVE_STACK + ": "
				+ villainEffectiveStack);
		log(depth, C.VILLAIN + " " + C.SITUATION + ": " + villainSituation.toString());

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

		result.add((foldWeight / villainSpectrumWeight), ifVillainFoldProfit);

		log(depth, "When " + C.VILLAIN + " " + C.FOLD + " in " + (int) (foldWeight / villainSpectrumWeight * 100) + "%");
		log(depth, C.HERO + " " + C.WINS + " " + ifVillainFoldProfit);

		if (passiveSpectrum.isEmpty()) {
			log(depth, C.VILLAIN + " will not " + C.CALL);
		} else {
			double passiveActionChance = passiveWeight / villainSpectrumWeight;
			log(depth, "When " + C.VILLAIN + " " + C.CALL + " in " + (int) (passiveActionChance * 100) + "%");
			RandomVariable ifVillainPassiveProfit = calculatePassiveProfit(gameInfo, pot + villainToCall,
					heroInvestment, street, heroCards, board, passiveSpectrum, onButton, depth + 1);
			log(depth, C.HERO + " " + C.WINS + " " 
					+ ifVillainPassiveProfit.printProfitability(gameInfo.getBankRollAtRisk(), gameInfo.getBigBlindSize()));

			result.add(passiveActionChance, ifVillainPassiveProfit);
		}

		if (aggressiveSpectrum.isEmpty()) {
			log(depth, C.VILLAIN + " willnot raise");
			return result;
		}

		double percent = getRaisePercent(villainToCall, street);
		Action villainRaiseAction = Action.createRaiseAction(villainToCall, pot, villainEffectiveStack, percent);
		double villainRaiseAmount = villainRaiseAction.getAmount();
		double aggressiveChance = aggressiveWeight / villainSpectrumWeight;
		log(depth, "When " + C.VILLAIN + " raise " + villainRaiseAmount + " in " + (int) (aggressiveChance * 100)
				+ "%");
		double effectiveStackAfterVillainRaise = villainEffectiveStack - villainRaiseAmount;
		double potAfterVillainRaise = pot + villainToCall + villainRaiseAmount;
		newInfo.plusVillainAction(true);
		ActionDistribution actionDistribution = onSecondActionRecursive(gameInfo, newInfo,
				effectiveStackAfterVillainRaise, potAfterVillainRaise, villainRaiseAmount, heroInvestment,
				aggressiveSpectrum, board, heroCards, villainStrengthMap, onButton, street, depth + 1);
		RandomVariable ifVillainAggressiveProfit = LessVarianceActionFromMap.getOptimal(actionDistribution, gameInfo,
				heroInvestment, heroCards);
		result.add(aggressiveChance, ifVillainAggressiveProfit);
		log(depth, C.HERO + " " + C.WINS + " " + ifVillainAggressiveProfit);
		return result;
	}

	private double getRaisePercent(double toCall, PokerStreet street) {
		double percent;
		if (toCall != 0 || street == PokerStreet.PREFLOP) {
			percent = 1;
		} else {
			percent = 0.75;
		}
		return percent;
	}

	private static void log(String log) {
		log(0, log);
	}

	private static void log(int indent, String log) {
		if (LOGGING) {
			Log.f(indent, log);
		}
	}

	private void processVillainSpectrums(Spectrum villainSpectrum, Spectrum foldSpectrum, Spectrum passiveSpectrum,
                                         Spectrum aggressiveSpectrum, HandState villainSituation, Map<HoleCards, StreetEquity> strengthMap) {
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

	private HandState getVillainSituationWithoutStrength(PokerStreet street, boolean villainOnButton,
                                                         double toCall, double pot, double effectiveStack, IAggressionInfo info,
                                                         boolean wasVillainPreviousAggressive, boolean wasHeroPreviousAggressive) {
		double potOdds = toCall / (toCall + pot);
		HandState result = new HandState(street.intValue());
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

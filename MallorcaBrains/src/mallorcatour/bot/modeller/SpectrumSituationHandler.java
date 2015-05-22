/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.modeller;

import java.util.HashMap;
import java.util.Map;

import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.interfaces.IVillainModel;
import mallorcatour.bot.math.IVillainSpectrumHandler;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.math.StrengthManager;
import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.equilator.StreetEquity;
import mallorcatour.core.equilator.preflop.EquilatorPreflop;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.core.game.situation.SituationHandler;
import mallorcatour.core.spectrum.Spectrum;
import mallorcatour.util.DoubleUtils;
import mallorcatour.util.Log;

/**
 * Class for observing villain spectrum during the certain hand
 * 
 * @author Andrew
 */
public class SpectrumSituationHandler extends SituationHandler implements IVillainSpectrumHandler {

	private Spectrum villainSpectrum;
	private final IAdvisor villainModel;
	private final boolean modelPreflop, modelPostflop;
	// TODO use for FL
	@SuppressWarnings("unused")
	private int raisesOnStreet = 0;
	private final ISpectrumListener villainSpectrumListener;
	private final IDecisionListener villainDecisionListener;
	private final StrengthManager strengthManager;
	private Spectrum randomVillainSpectrum;
	protected final String DEBUG_PATH;

	public SpectrumSituationHandler(IVillainModel villainModeller, LimitType limitType, boolean modelPreflop,
			boolean modelPostflop, ISpectrumListener villainSpectrumListener,
			IDecisionListener villainDecisionListener, StrengthManager strengthManager, String debug) {
		this(villainModeller, limitType, modelPreflop, modelPostflop, villainSpectrumListener, villainDecisionListener,
				strengthManager, false, debug);
	}

	public SpectrumSituationHandler(IVillainModel villainModeller, LimitType limitType, boolean modelPreflop,
			boolean modelPostflop, ISpectrumListener villainSpectrumListener,
			IDecisionListener villainDecisionListener, StrengthManager strengthManager, boolean needFullFlopPotential,
			String debug) {
		super(limitType, needFullFlopPotential);
		this.DEBUG_PATH = debug;
		this.modelPreflop = modelPreflop;
		this.modelPostflop = modelPostflop;
		this.villainModel = villainModeller;
		this.villainSpectrumListener = villainSpectrumListener;
		this.villainDecisionListener = villainDecisionListener;
		this.strengthManager = strengthManager;
	}

	/**
	 * An event called to tell us our hole cards
	 * 
	 * @param c1
	 *            your first hole card
	 * @param c2
	 *            your second hole card
	 */
	@Override
	public void onHoleCards(Card c1, Card c2, String heroName, String villainName) {
		super.onHoleCards(c1, c2, heroName, villainName);
		strengthManager.onHoleCards(c1, c2, heroName, villainName);
		villainSpectrum = Spectrum.random();
		randomVillainSpectrum = Spectrum.random();
		if (modelPreflop) {
			villainSpectrum.remove(holeCard1);
			villainSpectrum.remove(holeCard2);
			randomVillainSpectrum.remove(holeCard1);
			randomVillainSpectrum.remove(holeCard2);
		}
	}

	private LocalSituation getVillainSituationWithoutStrength(double villainToCall) {
		LocalSituation result = null;
		boolean isOnButton;
		if (villainName.equals(gameInfo.getButtonName())) {
			isOnButton = true;
		} else {
			isOnButton = false;
		}
		// potOdds
		double toCall = villainToCall;
		double pot = gameInfo.getPotSize();
		double bankrollAtRisk = gameInfo.getBankRollAtRisk();
		double potOdds = toCall / (toCall + pot);

		if (gameInfo.isPreFlop()) {
			result = new LocalSituation(LocalSituation.PREFLOP, limitType);
		} else if (gameInfo.isFlop()) {
			result = new LocalSituation(LocalSituation.FLOP, limitType);
		} else if (gameInfo.isTurn()) {
			result = new LocalSituation(LocalSituation.TURN, limitType);
		} else if (gameInfo.isRiver()) {
			result = new LocalSituation(LocalSituation.RIVER, limitType);
		}
		result.setPositivePotential(LocalSituation.DEFAULT_POTENTIAL);
		result.setNegativePotential(LocalSituation.DEFAULT_POTENTIAL);
		result.setVillainAggresionActionCount(countOfHeroAggressive);
		result.setVillainActionCount(heroActionCount);
		result.setHeroAggresionActionCount(countOfOppAggressive);
		result.setHeroActionCount(villainActionCount);
		result.wasOpponentPreviousAggresive(wasHeroPreviousAggressive);
		result.wasHeroPreviousAggresive(wasVillainPreviousAggressive);
		result.setPotOdds(potOdds);
		result.isOnButton(isOnButton);
		result.setPotToStackOdds((pot + toCall) / (pot + toCall + bankrollAtRisk));
		result.setFLPotSize(1 - (2 * gameInfo.getBigBlindSize()) / pot);
		if (limitType == LimitType.NO_LIMIT) {
			result.canRaise(bankrollAtRisk > 0);
		} else {
			result.canRaise(gameInfo.canHeroRaise());
		}
		return result;
	}

	@Override
	public Spectrum getVillainSpectrum() {
		return villainSpectrum;
	}

	/**
	 * A new betting round has started.
	 */
	@Override
	public void onStageEvent(PokerStreet street) {
		super.onStageEvent(street);
		strengthManager.onStageEvent(street);
		if (street == PokerStreet.PREFLOP) {
			raisesOnStreet = 1;
		} else {
			raisesOnStreet = 0;
		}
		if (modelPostflop) {
			if (street == PokerStreet.FLOP) {
				villainSpectrum.remove(flop1);
				villainSpectrum.remove(flop2);
				villainSpectrum.remove(flop3);
			} else if (street == PokerStreet.TURN) {
				villainSpectrum.remove(turn);
			} else if (street == PokerStreet.RIVER) {
				villainSpectrum.remove(river);
			}
		}
	}

	private void logVillainSituation(LocalSituation situation, Action villainAction) {
		Log.f(DEBUG_PATH, "Villain situation: " + situation.toString());
	}

	private Map<HoleCards, Advice> calculateVillainAdvices(LocalSituation villainSituation, Action villainAction) {
		Map<HoleCards, Advice> villainAdvices = new HashMap<HoleCards, Advice>();
		Log.f(DEBUG_PATH, "---------------------");
		logVillainSituation(villainSituation, villainAction);
		for (HoleCards villainCards : villainSpectrum) {
			double strength = 0;
			StreetEquity equity;
			if (gameInfo.isPreFlop()) {
				equity = strengthManager.preflop.get(villainCards);
			} else if (gameInfo.isFlop()) {
				if (flop1 == null || flop2 == null || flop3 == null) {
					throw new NullPointerException();
				}
				equity = strengthManager.flop.get(villainCards);
			} else if (gameInfo.isTurn()) {
				if (turn == null) {
					throw new NullPointerException();
				}
				equity = strengthManager.turn.get(villainCards);
			} else if (gameInfo.isRiver()) {
				if (turn == null || river == null) {
					throw new NullPointerException();
				}
				equity = strengthManager.river.get(villainCards);
			} else {
				throw new IllegalStateException("Wrong street: " + gameInfo.getStage());
			}
			villainSituation.setStrength(strength);
			villainSituation.setPositivePotential(equity.positivePotential);
			villainSituation.setNegativePotential(equity.negativePotential);
			Advice advice = null;
			if (villainSituation.getStreet() == LocalSituation.PREFLOP) {
				advice = villainModel.getAdvice(villainSituation, villainCards);
			}
			villainAdvices.put(villainCards, advice);
		}
		return villainAdvices;
	}

	/**
	 * An villain action has been observed.
	 */
	@Override
	public void onVillainActed(Action action, double villainToCall) {
		LocalSituation villainSituation = getVillainSituationWithoutStrength(villainToCall);
		villainDecisionListener.onDecided(villainSituation, action);
		if ((modelPreflop && gameInfo.isPreFlop()) || (modelPostflop && gameInfo.isPostFlop())) {
			modifyVillainSpectrum(villainSituation, action);
			String log = logVillainSpectrum();
			villainSpectrumListener.onSpectrumChanged(villainSpectrum, log);
		}
		if (action.isAggressive()) {
			raisesOnStreet++;
		}
		super.onVillainActed(action, villainToCall);
	}

	@Override
	public void onHeroActed(Action action) {
		if (action.isAggressive()) {
			raisesOnStreet++;
		}
		super.onHeroActed(action);
	}

	private void modifyVillainSpectrum(LocalSituation villainSituation, Action villainAction) {
		// for fixed limit we'll be calculating preflop also
		if (limitType == LimitType.FIXED_LIMIT || gameInfo.isPostFlop()) {
			Map<HoleCards, Advice> opponentAdvices = calculateVillainAdvices(villainSituation, villainAction);
			for (HoleCards villainCards : opponentAdvices.keySet()) {
				Advice advice = opponentAdvices.get(villainCards);
				double percent;
				if (villainAction.isFold()) {
					percent = advice.getFold();
				} else if (villainAction.isAggressive()) {
					percent = advice.getAggressive();
				} else {
					percent = advice.getPassive();
				}
				double oldPercent = villainSpectrum.getWeight(villainCards);
				if (percent == 0) {
					villainSpectrum.remove(villainCards);
				} else {
					villainSpectrum.add(villainCards, percent * oldPercent);
				}
			}
			Log.f(DEBUG_PATH, "---------------------");
		}
		if (villainSpectrum.isEmpty()) {
			String log = "\nVillain spectrum is unknown. Continue playing vs random\n";
			villainSpectrum = randomVillainSpectrum;
			villainSpectrumListener.onSpectrumChanged(villainSpectrum, log);
		}
	}

	private String logVillainSpectrum() {
		String log = null;
		if (gameInfo.isPreFlop()) {
			log = "Strength: " + EquilatorPreflop.strengthVsSpectrum(holeCard1, holeCard2, villainSpectrum);
		} else if (gameInfo.isFlop()) {
			StreetEquity equity = PokerEquilatorBrecher.equityVsSpectrum(new HoleCards(holeCard1, holeCard2),
					new Card[] { flop1, flop2, flop3 }, villainSpectrum);
			log = "Strength: " + DoubleUtils.digitsAfterComma(equity.strength, 3) + " Positive: "
					+ DoubleUtils.digitsAfterComma(equity.positivePotential, 3) + " Negative: "
					+ DoubleUtils.digitsAfterComma(equity.negativePotential, 3);
		} else if (gameInfo.isTurn()) {
			StreetEquity equity = PokerEquilatorBrecher.equityVsSpectrum(new HoleCards(holeCard1, holeCard2),
					new Card[] { flop1, flop2, flop3, turn }, villainSpectrum);
			log = "Strength: " + DoubleUtils.digitsAfterComma(equity.strength, 3) + " Positive: "
					+ DoubleUtils.digitsAfterComma(equity.positivePotential, 3) + " Negative: "
					+ DoubleUtils.digitsAfterComma(equity.negativePotential, 3);
		} else if (gameInfo.isRiver()) {
			log = "Strength: "
					+ PokerEquilatorBrecher.strengthVsSpectrum(new HoleCards(holeCard1, holeCard2), new Card[] { flop1,
							flop2, flop3, turn, river }, villainSpectrum);
		}
		return log;
	}

	@Override
	public void onHandStarted(IGameInfo gameInfo) {
		super.onHandStarted(gameInfo);
		strengthManager.onHandStarted(gameInfo);
	}
}

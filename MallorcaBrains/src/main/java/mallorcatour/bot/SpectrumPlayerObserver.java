package mallorcatour.bot;

import java.util.HashMap;
import java.util.Map;

import mallorcatour.bot.interfaces.ISpectrumHolder;
import mallorcatour.core.game.advice.Advisor;
import mallorcatour.bot.math.StrengthManager;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.IHoleCardsObserver;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.core.game.interfaces.ISpectrumListener;
import mallorcatour.core.game.state.HandState;
import mallorcatour.core.game.state.StreetEquity;
import mallorcatour.core.spectrum.Spectrum;
import mallorcatour.core.game.state.observer.BaseHandStateObserver;
import mallorcatour.tools.Log;

/**
 * Helper player that tracks spectrum of hero or villain during the round.
 * Наблюдает за игрой конкретного игрока (чаще всего противника) и строит спектр его возможных карт
 * на основании его (игрока) предполагаемой модели и его ходов в текущей раздаче.
 * Очень важный класс, в котором содержится важная логика.
 * 
 * @author andriipanasiuk
 * 
 */
public class SpectrumPlayerObserver implements IGameObserver, ISpectrumHolder, IHoleCardsObserver {
	private Spectrum spectrum;
	private Spectrum randomSpectrum;
	private Advisor model;
	private IGameInfo gameInfo;
	private StrengthManager strengthManager;
	private ISpectrumListener spectrumListener;
	private final String hero;
	private final boolean trackHero;
	private BaseHandStateObserver stateObserver;
	private HandState handState;

	private final AdvisorListener adviceListener;

	public SpectrumPlayerObserver(Advisor model, AdvisorListener actionObserver, StrengthManager strengthManager,
								  String hero, boolean trackHero) {
		this.hero = hero;
		this.trackHero = trackHero;
		this.model = model;
		stateObserver = new BaseHandStateObserver(hero, trackHero);
		this.strengthManager = strengthManager;
		this.adviceListener = actionObserver;
	}

	private Map<HoleCards, IAdvice> calculateAdvices(HandState situation, Action action) {
		Map<HoleCards, IAdvice> advices = new HashMap<HoleCards, IAdvice>();
		for (HoleCards cards : spectrum) {
			StreetEquity equity;
			if (gameInfo.isPreFlop()) {
				equity = strengthManager.preflop.get(cards);
			} else if (gameInfo.isFlop()) {
				equity = strengthManager.flop.get(cards);
			} else if (gameInfo.isTurn()) {
				equity = strengthManager.turn.get(cards);
			} else if (gameInfo.isRiver()) {
				equity = strengthManager.river.get(cards);
			} else {
				throw new IllegalStateException("Incorrect street: " + gameInfo.getStage());
			}
			if (equity == null) {
				Log.d("Cards: " + cards);
			}
			situation.setStrength(equity.strength);
			situation.setPositivePotential(equity.positivePotential);
			situation.setNegativePotential(equity.negativePotential);
			IAdvice advice = model.getAdvice(situation, cards, null);
			advices.put(cards, advice);
		}
		return advices;
	}

	private void modifySpectrum(HandState situation, Action action) {
		Map<HoleCards, IAdvice> advices = calculateAdvices(situation, action);
		for (HoleCards cards : advices.keySet()) {
			IAdvice advice = advices.get(cards);
			double percent;
			if (action.isFold()) {
				percent = advice.getFold();
			} else if (action.isAggressive()) {
				percent = advice.getAggressive();
			} else {
				percent = advice.getPassive();
			}
			double oldPercent = spectrum.getWeight(cards);
			if (percent == 0) {
				spectrum.remove(cards);
			} else {
				spectrum.add(cards, percent * oldPercent);
			}
		}
		if (spectrum.isEmpty()) {
			spectrum = randomSpectrum;
			Log.f(C.SPECTRUM + " set to random");
		}
		Log.f(C.SPECTRUM + " " + C.WEIGHT + ": " + spectrum.normalizedWeight());
		Log.f(C.SPECTRUM + ": " + spectrum.toString());
		if (spectrumListener != null) {
			spectrumListener.onSpectrumChanged(spectrum, "");
		}
	}

	@Override
	public void onStageEvent(PokerStreet street) {
		stateObserver.onStageEvent(street);
		handState = stateObserver.getSituation();
		if (street == PokerStreet.FLOP) {
			spectrum.remove(gameInfo.getFlop().toArray());
			randomSpectrum.remove(gameInfo.getFlop().toArray());
		} else if (street == PokerStreet.TURN) {
			spectrum.remove(gameInfo.getTurn());
			randomSpectrum.remove(gameInfo.getTurn());
		} else if (street == PokerStreet.RIVER) {
			spectrum.remove(gameInfo.getRiver());
			randomSpectrum.remove(gameInfo.getRiver());
		}
	}

	@Override
	public void onHandStarted(IGameInfo gameInfo) {
		this.gameInfo = gameInfo;
		stateObserver.onHandStarted(gameInfo);
		if (gameInfo.onButton(hero) ^ !trackHero) {
			handState = stateObserver.getSituation();
		}
		spectrum = Spectrum.random();
		randomSpectrum = Spectrum.random();
	}

	@Override
	public void onHandEnded() {
		stateObserver.onHandEnded();
	}

	@Override
	public Spectrum getSpectrum() {
		return spectrum;
	}

	@Override
	public void onHoleCards(Card c1, Card c2) {
		spectrum.remove(c1, c2);
		randomSpectrum.remove(c1, c2);
	}

	@Override
	public void onActed(Action action, double toCall, String name) {
		stateObserver.onActed(action, toCall, name);
		if (name.equals(hero) ^ !trackHero) {
			Log.f(this.toString() + " he " + C.ACTED);
			String modelPlayer = trackHero ? gameInfo.getHero(hero).getName() : gameInfo.getVillain(hero).getName();
			Log.f(C.SITUATION + " of " + modelPlayer + " " + handState);
			modifySpectrum(handState, action);
			adviceListener.onAdvice(handState, action);
		} else if (!action.isFold()) {
			Log.f(this.toString() + " not him " + C.ACTED);
			handState = stateObserver.getSituation();
		}
	}

	@Override
	public String toString() {
		return "Observing " + (trackHero ? (C.HERO + " " + hero) : (C.VILLAIN + " " + gameInfo.getVillain(hero).name));
	}

}
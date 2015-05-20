package mallorcatour.bot.math;

import java.util.HashMap;
import java.util.Map;

import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.equilator.StreetEquity;
import mallorcatour.core.equilator.preflop.EquilatorPreflop;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.IGameObserver;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.spectrum.Spectrum;
import mallorcatour.util.Log;

public class StrengthManager implements IGameObserver {

	public Map<HoleCards, StreetEquity> preflop = new HashMap<>();
	public Map<HoleCards, StreetEquity> flop = new HashMap<>();
	public Map<HoleCards, StreetEquity> turn = new HashMap<>();
	public Map<HoleCards, StreetEquity> river = new HashMap<>();

	private Spectrum randomVillainSpectrum;
	private IGameInfo gameInfo;

	@SuppressWarnings("deprecation")
	@Override
	public void onHoleCards(Card c1, Card c2, String heroName, String villainName) {
		preflop = new HashMap<HoleCards, StreetEquity>();
		for (HoleCards holeCards : randomVillainSpectrum) {
			//TODO calculate potentials for preflop with new method
			double strength = EquilatorPreflop.strengthByFormula(holeCards.first, holeCards.second);
			StreetEquity eq = new StreetEquity();
			eq.strength = strength;
			preflop.put(holeCards, eq);
		}
	}

	@Override
	public void onStageEvent(PokerStreet street) {
		calculateStrengthMap(street);
	}

	private void calculateStrengthMap(PokerStreet street) {
		long start = System.currentTimeMillis();
		if (street == PokerStreet.FLOP) {
            randomVillainSpectrum.remove(gameInfo.getFlop().first);
            randomVillainSpectrum.remove(gameInfo.getFlop().second);
            randomVillainSpectrum.remove(gameInfo.getFlop().third);
        } else if (street == PokerStreet.TURN) {
            randomVillainSpectrum.remove(gameInfo.getTurn());
        } else if (street == PokerStreet.RIVER) {
            randomVillainSpectrum.remove(gameInfo.getRiver());
        }
		if (street == PokerStreet.FLOP) {
			flop = new HashMap<HoleCards, StreetEquity>();
			Map<Integer, StreetEquity> cache = new HashMap<Integer, StreetEquity>();
			for (HoleCards holeCards : randomVillainSpectrum) {
				double strength = PokerEquilatorBrecher.strengthOnFlop(holeCards.first, holeCards.second,
						gameInfo.getFlop().first, gameInfo.getFlop().second, gameInfo.getFlop().third);
				int hash = holeCards.hashCodeForValues();
				StreetEquity equity = cache.get(hash);
				if (equity == null) {
					equity = PokerEquilatorBrecher.equityOnFlop(holeCards.first, holeCards.second,
							gameInfo.getFlop().first, gameInfo.getFlop().second, gameInfo.getFlop().third);
					cache.put(hash, equity);
				}
				equity.strength = strength;
				flop.put(holeCards, equity);
			}
		} else if (street == PokerStreet.TURN) {
			turn = new HashMap<HoleCards, StreetEquity>();
			Map<Integer, StreetEquity> cache = new HashMap<Integer, StreetEquity>();
			for (HoleCards holeCards : randomVillainSpectrum) {
				double strength = PokerEquilatorBrecher.strengthOnTurn(holeCards.first, holeCards.second,
						gameInfo.getFlop().first, gameInfo.getFlop().second, gameInfo.getFlop().third,
						gameInfo.getTurn());
				int hash = holeCards.hashCodeForValues();
				StreetEquity eq = cache.get(hash);
				if (eq == null) {
					eq = PokerEquilatorBrecher.equityOnTurn(holeCards.first, holeCards.second,
							gameInfo.getFlop().first, gameInfo.getFlop().second, gameInfo.getFlop().third,
							gameInfo.getTurn());
					cache.put(hash, eq);
				}
				eq.strength = strength;
				turn.put(holeCards, eq);
			}
		} else if (street == PokerStreet.RIVER) {
			river = new HashMap<HoleCards, StreetEquity>();
			for (HoleCards holeCards : randomVillainSpectrum) {
				double strength = PokerEquilatorBrecher.strengthOnRiver(holeCards.first, holeCards.second,
						gameInfo.getFlop().first, gameInfo.getFlop().second, gameInfo.getFlop().third,
						gameInfo.getTurn(), gameInfo.getRiver());
				StreetEquity eq = new StreetEquity();
				eq.strength = strength;
				river.put(holeCards, eq);
			}
		}
		Log.d("Strength map for " + street + " was calculated in " + (System.currentTimeMillis() - start)
				+ " ms");
	}

	@Override
	public void onHandStarted(IGameInfo gameInfo) {
		this.gameInfo = gameInfo;
		randomVillainSpectrum = Spectrum.random();
	}

	@Override
	public void onVillainActed(Action action, double toCall) {
		//do nothing
	}

	@Override
	public void onHandEnded() {
		//do nothing
	}
}

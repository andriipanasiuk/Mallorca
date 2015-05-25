package mallorcatour.brains.math;

import java.util.HashMap;
import java.util.Map;

import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.equilator.StreetEquity;
import mallorcatour.core.equilator.preflop.EquilatorPreflop;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.game.interfaces.IPlayerGameObserver;
import mallorcatour.core.spectrum.Spectrum;
import mallorcatour.util.Log;

/**
 * @author andriipanasiuk
 *
 */
public class StrengthManager implements IPlayerGameObserver {

	public Map<HoleCards, StreetEquity> preflop = new HashMap<>();
	public Map<HoleCards, StreetEquity> flop = new HashMap<>();
	public Map<HoleCards, StreetEquity> turn = new HashMap<>();
	public Map<HoleCards, StreetEquity> river = new HashMap<>();

	private Spectrum randomSpectrum;
	private IGameInfo gameInfo;
	private final boolean needFlopFullPotential;

	public StrengthManager() {
		needFlopFullPotential = false;
	}

	public StrengthManager(boolean needFlopFullPotential) {
		this.needFlopFullPotential = needFlopFullPotential;
	}
	@Override
	public void onHoleCards(Card c1, Card c2, String villainName) {
		preflop = new HashMap<HoleCards, StreetEquity>();
		for (HoleCards holeCards : randomSpectrum) {
			StreetEquity eq = EquilatorPreflop.equityVsRandom(holeCards.first, holeCards.second);
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
            randomSpectrum.remove(gameInfo.getFlop().first);
            randomSpectrum.remove(gameInfo.getFlop().second);
            randomSpectrum.remove(gameInfo.getFlop().third);
        } else if (street == PokerStreet.TURN) {
            randomSpectrum.remove(gameInfo.getTurn());
        } else if (street == PokerStreet.RIVER) {
            randomSpectrum.remove(gameInfo.getRiver());
        }
		if (street == PokerStreet.FLOP) {
			flop = new HashMap<HoleCards, StreetEquity>();
			Map<Integer, StreetEquity> cache = new HashMap<Integer, StreetEquity>();
			for (HoleCards holeCards : randomSpectrum) {
				double strength = PokerEquilatorBrecher.strengthOnFlop(holeCards.first, holeCards.second,
						gameInfo.getFlop().first, gameInfo.getFlop().second, gameInfo.getFlop().third);
				int hash = holeCards.hashCodeForValues();
				StreetEquity equity = cache.get(hash);
				if (equity == null) {
					if (needFlopFullPotential) {
						equity = PokerEquilatorBrecher.equityOnFlopFull(holeCards.first, holeCards.second,
								gameInfo.getFlop(), true);
					} else {
						equity = PokerEquilatorBrecher.equityOnFlop(holeCards.first, holeCards.second,
								gameInfo.getFlop());
					}
					cache.put(hash, equity);
				}
				equity.strength = strength;
				flop.put(holeCards, equity);
			}
		} else if (street == PokerStreet.TURN) {
			turn = new HashMap<HoleCards, StreetEquity>();
			Map<Integer, StreetEquity> cache = new HashMap<Integer, StreetEquity>();
			for (HoleCards holeCards : randomSpectrum) {
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
			for (HoleCards holeCards : randomSpectrum) {
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
	public void onHandStarted(IPlayerGameInfo gameInfo) {
		this.gameInfo = gameInfo;
		randomSpectrum = Spectrum.random();
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

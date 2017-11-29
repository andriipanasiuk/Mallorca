package mallorcatour.bot.math;

import java.util.HashMap;
import java.util.Map;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.interfaces.Equilator;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.core.game.interfaces.PreflopEquilator;
import mallorcatour.core.game.situation.StreetEquity;
import mallorcatour.core.spectrum.Spectrum;

/**
 * @author andriipanasiuk
 *
 */
public class StrengthManager implements IGameObserver<IGameInfo> {

	public Map<HoleCards, StreetEquity> preflop = new HashMap<>();
	public Map<HoleCards, StreetEquity> flop = new HashMap<>();
	public Map<HoleCards, StreetEquity> turn = new HashMap<>();
	public Map<HoleCards, StreetEquity> river = new HashMap<>();

	private Spectrum randomSpectrum;
	private IGameInfo gameInfo;
	private final boolean needFlopFullPotential;
	private final Equilator equilator;
	private final PreflopEquilator preflopEquilator;

	public StrengthManager(Equilator equilator, PreflopEquilator preflopEquilator) {
		this.equilator = equilator;
		this.preflopEquilator = preflopEquilator;
		needFlopFullPotential = false;
	}

	public StrengthManager(boolean needFlopFullPotential, Equilator equilator, PreflopEquilator preflopEquilator) {
		this.needFlopFullPotential = needFlopFullPotential;
		this.equilator = equilator;
		this.preflopEquilator = preflopEquilator;
	}

	@Override
	public void onStageEvent(PokerStreet street) {
		if (street == PokerStreet.FLOP) {
			randomSpectrum.remove(gameInfo.getFlop().first);
			randomSpectrum.remove(gameInfo.getFlop().second);
			randomSpectrum.remove(gameInfo.getFlop().third);
		} else if (street == PokerStreet.TURN) {
			randomSpectrum.remove(gameInfo.getTurn());
		} else if (street == PokerStreet.RIVER) {
			randomSpectrum.remove(gameInfo.getRiver());
		}
		calculateStrengthMap(street);
	}

	@SuppressWarnings("unused")
	private void calculateStrengthMap(PokerStreet street) {
		long start = System.currentTimeMillis();
		if (street == PokerStreet.FLOP) {
			flop = new HashMap<HoleCards, StreetEquity>();
			Map<Integer, StreetEquity> cache = new HashMap<Integer, StreetEquity>();
			for (HoleCards holeCards : randomSpectrum) {
				double strength = equilator.strengthOnFlop(holeCards.first, holeCards.second,
						gameInfo.getFlop().first, gameInfo.getFlop().second, gameInfo.getFlop().third);
				int hash = holeCards.hashCodeForValues();
				StreetEquity equity = cache.get(hash);
				if (equity == null) {
					if (needFlopFullPotential) {
						equity = equilator.equityOnFlopFull(holeCards.first, holeCards.second,
								gameInfo.getFlop(), true);
					} else {
						equity = equilator.equityOnFlop(holeCards.first, holeCards.second,
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
				double strength = equilator.strengthOnTurn(holeCards.first, holeCards.second,
						gameInfo.getFlop().first, gameInfo.getFlop().second, gameInfo.getFlop().third,
						gameInfo.getTurn());
				int hash = holeCards.hashCodeForValues();
				StreetEquity eq = cache.get(hash);
				if (eq == null) {
					eq = equilator.equityOnTurn(holeCards.first, holeCards.second,
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
				double strength = equilator.strengthOnRiver(holeCards.first, holeCards.second,
						gameInfo.getFlop().first, gameInfo.getFlop().second, gameInfo.getFlop().third,
						gameInfo.getTurn(), gameInfo.getRiver());
				StreetEquity eq = new StreetEquity();
				eq.strength = strength;
				river.put(holeCards, eq);
			}
		}
	}

	@Override
	public void onHandStarted(IGameInfo gameInfo) {
		this.gameInfo = gameInfo;
		randomSpectrum = Spectrum.random();
		preflop = new HashMap<HoleCards, StreetEquity>();
		for (HoleCards holeCards : randomSpectrum) {
			StreetEquity eq = preflopEquilator.equityVsRandom(holeCards.first, holeCards.second);
			preflop.put(holeCards, eq);
		}
	}

	@Override
	public void onActed(Action action, double toCall, String name) {
		//do nothing
	}

	@Override
	public void onHandEnded() {
		//do nothing
	}
}
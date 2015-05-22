package mallorcatour.brains.math;

import java.util.Map;

import mallorcatour.core.equilator.StreetEquity;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.Flop;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.spectrum.Spectrum;

public interface IGameSolver {

	public Map<Action, Double> onSecondActionPreflop(int heroActions, int heroAggressiveActions, int villainActions,
			int villainAggressiveActions, double effectiveStack, double pot, double toCall, Spectrum villainSpectrum,
			HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton, double bigBlind);

	public Map<Action, Double> onSecondActionFlop(int heroActions, int heroAggressiveActions, int villainActions,
			int villainAggressiveActions, double effectiveStack, double pot, double toCall, Spectrum villainSpectrum,
			Flop flop, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton,
			double bigBlind);

	public Map<Action, Double> onSecondActionTurn(int heroActions, int heroAggressiveActions, int villainActions,
			int villainAggressiveActions, double effectiveStack, double pot, double toCall, Spectrum villainSpectrum,
			Flop flop, Card turn, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
			boolean isHeroOnButton, double bigBlind);

	public Map<Action, Double> onSecondActionRiver(int heroActions, int heroAggressiveActions, int villainActions,
			int villainAggressiveActions, double effectiveStack, double pot, double toCall, Spectrum villainSpectrum,
			Flop flop, Card turn, Card river, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
			boolean isHeroOnButton, double bigBlind);

	public Map<Action, Double> onFirstActionPreFlop(int heroActions, int heroAggressiveActions, int villainActions,
			int villainAggressiveActions, double effectiveStack, double pot, Spectrum villainSpectrum,
			HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap, boolean wasVillainPreviousAggressive,
			boolean isHeroOnButton, double bigBlind);

	public Map<Action, Double> onFirstActionFlop(int heroActions, int heroAggressiveActions, int villainActions,
			int villainAggressiveActions, double effectiveStack, double pot, Spectrum villainSpectrum, Flop flop,
			HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap, boolean wasVillainPreviousAggressive,
			boolean isHeroOnButton, double bigBlind);

	public Map<Action, Double> onFirstActionTurn(int heroActions, int heroAggressiveActions, int villainActions,
			int villainAggressiveActions, double effectiveStack, double pot, Spectrum villainSpectrum, Flop flop,
			Card turn, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
			boolean wasVillainPreviousAggressive, boolean isHeroOnButton, double bigBlind);

	public Map<Action, Double> onFirstActionRiver(int heroActions, int heroAggressiveActions, int villainActions,
			int villainAggressiveActions, double effectiveStack, double pot, Spectrum villainSpectrum, Flop flop,
			Card turn, Card river, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
			boolean wasVillainPreviousAggressive, boolean isHeroOnButton, double bigBlind);

}
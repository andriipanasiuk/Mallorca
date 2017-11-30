package mallorcatour.bot.math;

import java.util.Map;

import mallorcatour.core.game.Card;
import mallorcatour.core.game.Flop;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.interfaces.IAggressionInfo;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.game.state.StreetEquity;
import mallorcatour.core.spectrum.Spectrum;

public interface IGameSolver {

	public ActionDistribution onSecondActionPreflop(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack, double pot, double toCall,
			Spectrum villainSpectrum, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton, double bigBlind);

	public ActionDistribution onSecondActionFlop(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack, double pot, double toCall, Spectrum villainSpectrum,
			Flop flop, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton,
			double bigBlind);

	public ActionDistribution onSecondActionTurn(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack, double pot, double toCall, Spectrum villainSpectrum,
			Flop flop, Card turn, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
			boolean isHeroOnButton, double bigBlind);

	public ActionDistribution onSecondActionRiver(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack, double pot, double toCall, Spectrum villainSpectrum,
			Flop flop, Card turn, Card river, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
			boolean isHeroOnButton, double bigBlind);

	public ActionDistribution onFirstActionPreFlop(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack, double pot, Spectrum villainSpectrum,
			HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap, boolean wasVillainPreviousAggressive,
			boolean isHeroOnButton, double bigBlind);

	public ActionDistribution onFirstActionFlop(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack, double pot, Spectrum villainSpectrum, Flop flop,
			HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap, boolean wasVillainPreviousAggressive,
			boolean isHeroOnButton, double bigBlind);

	public ActionDistribution onFirstActionTurn(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack, double pot, Spectrum villainSpectrum, Flop flop,
			Card turn, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
			boolean wasVillainPreviousAggressive, boolean isHeroOnButton, double bigBlind);

	public ActionDistribution onFirstActionRiver(IGameInfo gameInfo, IAggressionInfo info, double effectiveStack, double pot, Spectrum villainSpectrum, Flop flop,
			Card turn, Card river, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
			boolean wasVillainPreviousAggressive, boolean isHeroOnButton, double bigBlind);

}
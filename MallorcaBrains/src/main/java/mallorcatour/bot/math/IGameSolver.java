package mallorcatour.bot.math;

import java.util.Map;

import mallorcatour.core.game.Card;
import mallorcatour.core.game.Flop;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.interfaces.IAggressionInfo;
import mallorcatour.core.game.interfaces.GameContext;
import mallorcatour.core.game.state.StreetEquity;
import mallorcatour.core.spectrum.Spectrum;

public interface IGameSolver {

	public ActionDistribution onSecondActionPreflop(GameContext gameInfo, IAggressionInfo info, double effectiveStack, double pot, double toCall,
                                                    Spectrum villainSpectrum, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton, double bigBlind);

	public ActionDistribution onSecondActionFlop(GameContext gameInfo, IAggressionInfo info, double effectiveStack, double pot, double toCall, Spectrum villainSpectrum,
                                                 Flop flop, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton,
                                                 double bigBlind);

	public ActionDistribution onSecondActionTurn(GameContext gameInfo, IAggressionInfo info, double effectiveStack, double pot, double toCall, Spectrum villainSpectrum,
                                                 Flop flop, Card turn, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
                                                 boolean isHeroOnButton, double bigBlind);

	public ActionDistribution onSecondActionRiver(GameContext gameInfo, IAggressionInfo info, double effectiveStack, double pot, double toCall, Spectrum villainSpectrum,
                                                  Flop flop, Card turn, Card river, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
                                                  boolean isHeroOnButton, double bigBlind);

	public ActionDistribution onFirstActionPreFlop(GameContext gameInfo, IAggressionInfo info, double effectiveStack, double pot, Spectrum villainSpectrum,
                                                   HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap, boolean wasVillainPreviousAggressive,
                                                   boolean isHeroOnButton, double bigBlind);

	public ActionDistribution onFirstActionFlop(GameContext gameInfo, IAggressionInfo info, double effectiveStack, double pot, Spectrum villainSpectrum, Flop flop,
                                                HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap, boolean wasVillainPreviousAggressive,
                                                boolean isHeroOnButton, double bigBlind);

	public ActionDistribution onFirstActionTurn(GameContext gameInfo, IAggressionInfo info, double effectiveStack, double pot, Spectrum villainSpectrum, Flop flop,
                                                Card turn, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
                                                boolean wasVillainPreviousAggressive, boolean isHeroOnButton, double bigBlind);

	public ActionDistribution onFirstActionRiver(GameContext gameInfo, IAggressionInfo info, double effectiveStack, double pot, Spectrum villainSpectrum, Flop flop,
                                                 Card turn, Card river, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
                                                 boolean wasVillainPreviousAggressive, boolean isHeroOnButton, double bigBlind);

}
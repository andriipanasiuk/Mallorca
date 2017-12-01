package mallorcatour.bot.math;

import mallorcatour.core.game.Card;
import mallorcatour.core.game.interfaces.GameContext;
import mallorcatour.core.game.state.HandState;
import mallorcatour.core.spectrum.Spectrum;

public interface IProfitCalculator {

	ActionDistribution getProfitMap(GameContext gameInfo, HandState situation, Card holeCard1, Card holeCard2,
									Spectrum villainSpectrum);

}
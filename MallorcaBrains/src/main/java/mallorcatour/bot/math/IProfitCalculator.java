package mallorcatour.bot.math;

import mallorcatour.core.game.Card;
import mallorcatour.core.game.interfaces.IAggressionInfo;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.spectrum.Spectrum;

public interface IProfitCalculator {

	ActionDistribution getProfitMap(IPlayerGameInfo gameInfo, IAggressionInfo situation, Card holeCard1, Card holeCard2,
			Spectrum villainSpectrum);

}
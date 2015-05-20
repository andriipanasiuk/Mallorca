package mallorcatour.bot.modeller;

import java.util.Map;

import mallorcatour.bot.math.StrengthManager;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.core.spectrum.Spectrum;

public interface IProfitCalculator {

	Map<Action, Double> getProfitMap(IGameInfo gameInfo, String heroName, LocalSituation situation, Card holeCard1,
			Card holeCard2, Spectrum villainSpectrum, StrengthManager strengthManager);

}
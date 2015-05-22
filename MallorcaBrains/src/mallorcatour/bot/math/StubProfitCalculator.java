package mallorcatour.bot.math;

import java.util.HashMap;
import java.util.Map;

import mallorcatour.brains.math.StrengthManager;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.interfaces.IAggressionInfo;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.spectrum.Spectrum;

public class StubProfitCalculator implements IProfitCalculator{

	@Override
	public Map<Action, Double> getProfitMap(IGameInfo gameInfo, String heroName, IAggressionInfo situation,
			Card holeCard1, Card holeCard2, Spectrum villainSpectrum, StrengthManager strengthManager) {
		Map<Action, Double> result = new HashMap<>();
		result.put(Action.foldAction(), 1D);
		result.put(Action.passive(), 1D);
		result.put(Action.aggressive(), 1D);
		return result;
	}

}

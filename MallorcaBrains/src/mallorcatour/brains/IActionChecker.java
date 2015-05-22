package mallorcatour.brains;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.core.spectrum.Spectrum;

public interface IActionChecker {
	IActionChecker EMPTY = new IActionChecker() {

		@Override
		public Action checkAction(Action action, LocalSituation situation, IGameInfo gameInfo, HoleCards cards,
				Spectrum villainSpectrum) {
			return action;
		}
	};

	Action checkAction(Action action, LocalSituation situation, IGameInfo gameInfo, HoleCards cards,
			Spectrum villainSpectrum);
}
package mallorcatour.brains;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.core.spectrum.Spectrum;

public interface IActionChecker {
	IActionChecker EMPTY = new IActionChecker() {

		@Override
		public Action checkAction(Action action, LocalSituation situation, IPlayerGameInfo gameInfo, HoleCards cards) {
			return action;
		}
	};

	Action checkAction(Action action, LocalSituation situation, IPlayerGameInfo gameInfo, HoleCards cards);
}
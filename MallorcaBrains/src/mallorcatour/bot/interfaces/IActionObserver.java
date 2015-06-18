package mallorcatour.bot.interfaces;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.situation.LocalSituation;

public interface IActionObserver {
	IActionObserver EMPTY = new IActionObserver() {

		@Override
		public void acted(LocalSituation situation, Action action) {
			// do nothing
		}
	};

	void acted(LocalSituation situation, Action action);
}
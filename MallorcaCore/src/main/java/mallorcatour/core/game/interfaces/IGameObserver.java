package mallorcatour.core.game.interfaces;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.PokerStreet;

/**
 * Сторонний наблюдатель за игрой. Он не видит карт игроков.
 */
public interface IGameObserver extends IObserver {

	IGameObserver EMPTY = new IGameObserver() {

		@Override
		public void onStageEvent(PokerStreet street) {

		}

		@Override
		public void onActed(Action action, double toCall, String name) {

		}

		@Override
		public void onHandStarted(GameContext gameInfo) {

		}

		@Override
		public void onHandEnded() {

		}
	};

	void onHandStarted(GameContext gameInfo);
}
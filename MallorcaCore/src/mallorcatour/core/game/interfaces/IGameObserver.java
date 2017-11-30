package mallorcatour.core.game.interfaces;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.PokerStreet;

/**
 * Интерфейс стороннего наблюдателя за игрой
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
		public void onHandStarted(IGameInfo gameInfo) {

		}

		@Override
		public void onHandEnded() {

		}
	};

	void onHandStarted(IGameInfo gameInfo);
}
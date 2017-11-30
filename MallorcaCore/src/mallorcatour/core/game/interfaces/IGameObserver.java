package mallorcatour.core.game.interfaces;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.PokerStreet;

public interface IGameObserver<P extends IGameInfo> {
	@SuppressWarnings("rawtypes")
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

	/**
	 * A new betting round has started. Not called on preflop.
	 */
	void onStageEvent(PokerStreet street);

	/**
	 * A new game has been started.
	 * 
	 * @param gi
	 *            the game stat information
	 */
	void onActed(Action action, double toCall, String name);

	void onHandStarted(P gameInfo);

	void onHandEnded();
}
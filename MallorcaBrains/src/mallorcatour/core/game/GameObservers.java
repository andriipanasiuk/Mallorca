package mallorcatour.core.game;

import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.game.interfaces.IGameObserver;

public class GameObservers implements IGameObserver {
	private IGameObserver[] array;

	public GameObservers(IGameObserver... array) {
		super();
		this.array = array;
	}

	@Override
	public void onStageEvent(PokerStreet street) {
		for (IGameObserver observer : array) {
			observer.onStageEvent(street);
		}
	}

	@Override
	public void onActed(Action action, double toCall, String name) {
		for (IGameObserver observer : array) {
			observer.onActed(action, toCall, name);
		}
	}

	@Override
	public void onHandStarted(IGameInfo gameInfo) {
		for (IGameObserver observer : array) {
			observer.onHandStarted(gameInfo);
		}
	}

	@Override
	public void onHandEnded() {
		for (IGameObserver observer : array) {
			observer.onHandEnded();
		}
	}
}
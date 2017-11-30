package mallorcatour.bot;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.IHoleCardsObserver;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.game.interfaces.IPlayerGameObserver;
import mallorcatour.core.game.state.HandStateHolder;

/**
 * Это некий активный наблюдатель за игрой, который запоминает карты, пристально следит за
 * текущим состоянием раздачи, видит карты игрока, но у него нет компетенции для того,
 * чтобы сделать правильный ход.
 */
public abstract class ObservingPlayer implements IPlayerGameObserver, IHoleCardsObserver {
	protected IPlayerGameInfo gameInfo;
	protected final String DEBUG_PATH;
	protected Card heroCard1, heroCard2;
	protected IGameObserver gameObserver;
	private IHoleCardsObserver cardsObserver;
	protected final String name;

	public ObservingPlayer(String name, String debug) {
		this.name = name;
		this.DEBUG_PATH = debug;
	}

	public void set(IGameObserver observer, IHoleCardsObserver cardsObserver) {
		this.gameObserver = observer;
		this.cardsObserver = cardsObserver;
	}

	/**
	 * An event called to tell us our hole cards and seat number
	 * 
	 * @param c1
	 *            your first hole card
	 * @param c2
	 *            your second hole card
	 */
	@Override
	public void onHoleCards(Card c1, Card c2) {
		this.heroCard1 = c1;
		this.heroCard2 = c2;
		cardsObserver.onHoleCards(c1, c2);
	}

	/**
	 * A new betting round has started.
	 */
	@Override
	public void onStageEvent(PokerStreet street) {
		gameObserver.onStageEvent(street);
	}

	/**
	 * A new game has been started.
	 */
	@Override
	public void onHandStarted(IPlayerGameInfo gameInfo) {
		this.gameInfo = gameInfo;
		gameObserver.onHandStarted(gameInfo);
	}

	@Override
	public void onActed(Action action, double toCall, String name) {
		if (!name.equals(this.name)) {
			gameObserver.onActed(action, toCall, name);
		}
	}

	@Override
	public void onHandEnded() {
		gameObserver.onHandEnded();
	}

}

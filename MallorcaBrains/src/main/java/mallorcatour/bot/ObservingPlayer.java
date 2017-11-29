package mallorcatour.bot;

import mallorcatour.bot.actionpreprocessor.NLActionPreprocessor;
import mallorcatour.core.player.interfaces.IPlayer;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.IHoleCardsObserver;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.interfaces.IActionPreprocessor;
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.game.situation.HandStateHolder;

public abstract class ObservingPlayer implements IPlayer {
	protected IPlayerGameInfo gameInfo;
	protected HandStateHolder situationHandler;
	protected final IActionPreprocessor actionPreprocessor;
	protected final String DEBUG_PATH;
	protected Card heroCard1, heroCard2;
	protected IGameObserver<IPlayerGameInfo> gameObserver;
	private IHoleCardsObserver cardsObserver;
	protected final String name;

	public ObservingPlayer(String name, String debug) {
		this.name = name;
		actionPreprocessor = new NLActionPreprocessor();
		this.DEBUG_PATH = debug;
	}

	public void set(HandStateHolder situationHandler, IGameObserver observer, IHoleCardsObserver cardsObserver) {
		this.situationHandler = situationHandler;
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
	 * @param seat
	 *            your seat number at the table
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
	 * 
	 * @param gi
	 *            the game stat information
	 */
	@Override
	public void onHandStarted(IPlayerGameInfo gameInfo) {
		this.gameInfo = gameInfo;
		gameObserver.onHandStarted(gameInfo);
	}

	@Override
	public void onActed(Action action, double toCall, String name) {
		if (!name.equals(getName())) {
			gameObserver.onActed(action, toCall, name);
		}
	}

	@Override
	public void onHandEnded() {
		gameObserver.onHandEnded();
	}

	@Override
	public String getName() {
		return (name != null) ? name : getDefaultName();
	}

	public abstract String getDefaultName();
}
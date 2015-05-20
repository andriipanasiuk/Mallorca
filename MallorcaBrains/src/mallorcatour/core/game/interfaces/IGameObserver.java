package mallorcatour.core.game.interfaces;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.PokerStreet;

public interface IGameObserver {

	/**
	 * An event called to tell us our hole cards and seat number
	 * @param c1 your first hole card
	 * @param c2 your second hole card
	 * @param seat your seat number at the table
	 */
	public void onHoleCards(Card c1, Card c2, String heroName, String villainName);


	/**
	 * A new betting round has started.
	 */
	public void onStageEvent(PokerStreet street);

	/**
	 * A new game has been started.
	 * @param gi the game stat information
	 */
	public void onHandStarted(IGameInfo gameInfo);

	/**
	 * An villain action has been observed.
	 */
	public void onVillainActed(Action action, double toCall);

	public void onHandEnded();

}
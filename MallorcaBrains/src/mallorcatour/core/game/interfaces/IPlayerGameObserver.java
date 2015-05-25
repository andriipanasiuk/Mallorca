package mallorcatour.core.game.interfaces;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;

public interface IPlayerGameObserver extends IGameObserver<IPlayerGameInfo> {

	/**
	 * An event called to tell us our hole cards and seat number
	 * @param c1 your first hole card
	 * @param c2 your second hole card
	 * @param seat your seat number at the table
	 */
	public void onHoleCards(Card c1, Card c2, String villainName);

	/**
	 * An villain action has been observed.
	 */
	public void onVillainActed(Action action, double toCall);


}
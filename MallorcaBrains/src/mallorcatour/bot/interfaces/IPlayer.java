/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.interfaces;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;

/**
 *
 * @author Andrew
 */
public interface IPlayer extends IGameObserver<IPlayerGameInfo> {

	/**
	 * An event called to tell us our hole cards and seat number
	 * @param c1 your first hole card
	 * @param c2 your second hole card
	 * @param seat your seat number at the table
	 */
	void onHoleCards(Card c1, Card c2, String villainName);

    Action getAction();

    String getName();

}

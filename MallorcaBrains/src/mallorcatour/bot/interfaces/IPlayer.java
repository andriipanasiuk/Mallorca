/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.interfaces;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.interfaces.IGameInfo;

/**
 *
 * @author Andrew
 */
public interface IPlayer {

    void onHandStarted(IGameInfo gameInfo, long handNumber);

    void onHoleCards(Card card1, Card card2, String heroName, String villainName);

    Action getAction();

    void onStageEvent(PokerStreet street);

    void onVillainActed(Action action, double toCall);
}

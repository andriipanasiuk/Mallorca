/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.bot;

import mallorcatour.game.core.Card;
import mallorcatour.game.core.Action;
import mallorcatour.game.core.PokerStreet;

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

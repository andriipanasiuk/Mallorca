/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.interfaces;

import mallorcatour.game.core.Card;
import mallorcatour.game.core.Action;
import mallorcatour.game.core.PokerStreet;
import mallorcatour.core.bot.IGameInfo;

/**
 *
 * @author Andrew
 */
public interface IGameHandler {

    void onHandStarted(IGameInfo gameInfo);

    void onHoleCards(Card card1, Card card2, String heroName, String villainName);

    void onHeroActed(Action action);

    void beforeHeroAction();

    void onVillainActed(Action action, double toCall);

    void onStageEvent(PokerStreet street);

    void onHandEnded();
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.interfaces;

import java.util.Date;
import java.util.List;
import mallorcatour.core.bot.LimitType;
import mallorcatour.game.core.Card;
import mallorcatour.game.core.Action;
import mallorcatour.game.hand.Hand;
import mallorcatour.game.core.PlayerInfo;

/**
 *
 * @author Andrew
 */
public interface IHandHandler {

    void onHandStarted(long id, Date date, List<PlayerInfo> players, String playerOnButton,
            double smallBlind, double bigBlind, LimitType limitType);

    void onFlop(Card flop1, Card flop2, Card flop3);

    void onTurn(Card turn);

    void onRiver(Card river);

    void onPlayerActed(String name, Action action);

    void onHandEnded();

    Hand buildHand();
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.hhparser.core;

import java.util.Date;
import java.util.List;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.Hand;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.PlayerInfo;

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

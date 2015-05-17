/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.game.hand;

import java.util.ArrayList;
import mallorcatour.game.core.Action;
import mallorcatour.game.core.PlayerAction;
import java.util.Date;
import java.util.List;
import mallorcatour.core.bot.LimitType;
import mallorcatour.game.core.Card;
import mallorcatour.game.core.PlayerInfo;
import mallorcatour.interfaces.IHandHandler;

/**
 *
 * @author Andrew
 */
public class BaseHandHandler implements IHandHandler {

    private List<Hand> hands;
    private Hand hand;

    public BaseHandHandler() {
        hands = new ArrayList<Hand>();
    }

    public void onHandStarted(long id, Date date, List<PlayerInfo> players, String playerOnButton,
            double smallBlind, double bigBlind, LimitType limitType) {
        hand = new Hand(id, date, players, playerOnButton, smallBlind, bigBlind, limitType);
    }

    public void onPlayerActed(String player, Action action) {
        hand.addAction(new PlayerAction(player, action));
    }

    public void onFlop(Card flop1, Card flop2, Card flop3) {
        hand.setFlop(flop1, flop2, flop3);
    }

    public void onTurn(Card turn) {
        hand.setTurn(turn);
    }

    public void onRiver(Card river) {
        hand.setRiver(river);
    }

    public void onHandEnded() {
        if (hand == null) {
            return;
        }
        hands.add(hand);
    }

    public Hand buildHand() {
        return hand;
    }

    public List<Hand> buildHands() {
        return hands;
    }
}

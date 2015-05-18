/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.hhparser.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.Hand;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.PlayerAction;
import mallorcatour.core.game.PlayerInfo;
import mallorcatour.core.game.interfaces.IHandHandler;

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

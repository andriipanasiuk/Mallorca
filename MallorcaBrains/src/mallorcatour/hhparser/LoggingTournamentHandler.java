/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.hhparser;

import java.util.Date;
import java.util.List;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.Hand;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.OpenPlayerInfo;
import mallorcatour.hhparser.core.ITournamentHandler;
import mallorcatour.util.Log;

/**
 *
 * @author Andrew
 */
public class LoggingTournamentHandler implements ITournamentHandler {

    public void onTournamentStart(Date date, int playerCount, String description) {
        Log.d("Started tournament");
        Log.d("Date of start = " + date);
        Log.d("Count of players = " + playerCount);
        Log.d("Info: " + description);
    }

    @Override
    public void onHandStarted(long id, Date date, List<OpenPlayerInfo> players,
            String playerOnButton, double smallBlind, double bigBlind, LimitType limitType) {
        Log.d("New hand. Date: " + date);
        Log.d("Hand id: " + id);
        Log.d("Limit type: " + limitType.toString());
        Log.d("Count of players = " + players.size());
        for (int i = 0; i < players.size(); i++) {
            OpenPlayerInfo playerInfo = players.get(i);
            Log.d(i + " player name: " + playerInfo.getName());
            Log.d(i + " player stack: " + playerInfo.getStack());
            Log.d(i + " player cards: " + playerInfo.getHoleCards());
            if (playerInfo.getName().equals(playerOnButton)) {
                Log.d("Player is on button.");
            }
        }
        Log.d("Blinds: $" + smallBlind + "/$" + bigBlind);
    }

    public void onPlayerActed(String player, Action action) {
        Log.d("Player " + player + " do " + action + " action. Amount = "
                + action.getAmount());
    }

    public void onFlop(Card flop1, Card flop2, Card flop3) {
        Log.d("Flop: " + flop1 + " " + flop2 + " " + flop3);
    }

    public void onTurn(Card turn) {
        Log.d("Turn: " + turn);
    }

    public void onRiver(Card river) {
        Log.d("River: " + river);
    }

    public void onHandEnded() {
        Log.d("Hand ended.");
    }

    public void onTournamentEnd(String winner) {
        Log.d("Tournament ended. Winner: " + winner);
    }

    public Hand buildHand() {
        Log.d("Building the hand");
        return null;
    }
}

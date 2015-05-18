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
import mallorcatour.core.game.PlayerInfo;
import mallorcatour.core.game.interfaces.IHandHandler;

/**
 *
 * @author Andrew
 */
public class BaseTournamentHandler implements ITournamentHandler {

    private List<Tournament> tournaments;
    private Tournament tournament;
    private IHandHandler handHandler;

    public BaseTournamentHandler(IHandHandler handHandler) {
        this.handHandler = handHandler;
        tournaments = new ArrayList<Tournament>();
    }

    public void onTournamentStart(Date date, int playerCount, String description) {
        tournament = new Tournament(date, description);
    }

    public void onHandStarted(long id, Date date, List<PlayerInfo> players,
            String playerOnButton, double smallBlind, double bigBlind, LimitType limitType) {
        handHandler.onHandStarted(id, date, players, playerOnButton, smallBlind, bigBlind, limitType);
    }

    public void onPlayerActed(String player, Action action) {
        handHandler.onPlayerActed(player, action);
    }

    public void onFlop(Card flop1, Card flop2, Card flop3) {
        handHandler.onFlop(flop1, flop2, flop3);
    }

    public void onTurn(Card turn) {
        handHandler.onTurn(turn);
    }

    public void onRiver(Card river) {
        handHandler.onRiver(river);
    }

    public void onHandEnded() {
        handHandler.onHandEnded();
        tournament.addHand(handHandler.buildHand());
    }

    public void onTournamentEnd(String winner) {
        tournaments.add(tournament);
    }

    public List<Tournament> buildTournaments() {
        return tournaments;
    }

    public Hand buildHand() {
        return handHandler.buildHand();
    }
}

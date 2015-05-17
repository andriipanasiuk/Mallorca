/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.game.hand;

import mallorcatour.game.core.Action;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mallorcatour.core.bot.LimitType;
import mallorcatour.game.core.Card;
import mallorcatour.game.core.PlayerInfo;
import mallorcatour.interfaces.IHandHandler;
import mallorcatour.interfaces.ITournamentHandler;

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

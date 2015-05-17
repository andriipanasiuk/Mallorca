/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.game.situation;

import java.util.ArrayList;
import java.util.List;
import mallorcatour.interfaces.ISituationHandler;
import mallorcatour.game.core.Card;
import mallorcatour.game.core.Action;
import mallorcatour.game.core.PokerStreet;
import mallorcatour.core.bot.IGameInfo;
import mallorcatour.core.bot.LimitType;
import mallorcatour.util.Pair;

/**
 *
 * @author Andrew
 */
public class PotSituationHandler implements ISituationHandler {

    protected IGameInfo gameInfo;  // general game information
    protected String heroName, villainName;
    protected LimitType limitType;
    double pot;
    private List<Pair<Double, Double>> flopTurnPot, turnRiverPot;

    public PotSituationHandler() {
        flopTurnPot = new ArrayList<Pair<Double, Double>>();
        turnRiverPot = new ArrayList<Pair<Double, Double>>();
    }

    public List<Pair<Double, Double>> getFlopTurnPots() {
        return flopTurnPot;
    }

    public List<Pair<Double, Double>> getTurnRiverPots() {
        return turnRiverPot;
    }

    /**
     * An event called to tell us our hole cards and seat number
     * @param c1 your first hole card
     * @param c2 your second hole card
     * @param seat your seat number at the table
     */
    public void onHoleCards(Card c1, Card c2, String heroName, String villainName) {
    }

    public LocalSituation onHeroSituation() {
        return null;
    }

    /**
     * A new betting round has started.
     */
    public void onStageEvent(PokerStreet street) {
        if (street == PokerStreet.TURN) {
            pot = gameInfo.getPotSize() / gameInfo.getBigBlindSize();
        }
        if (street == PokerStreet.RIVER) {
            flopTurnPot.add(new Pair(pot, gameInfo.getPotSize() / gameInfo.getBigBlindSize()));
            pot = gameInfo.getPotSize() / gameInfo.getBigBlindSize();
        }
    }

    /**
     * A new game has been started.
     * @param gi the game stat information
     */
    public void onHandStarted(IGameInfo gameInfo) {
        this.gameInfo = gameInfo;
        pot = -1;
    }

    /**
     * An villain action has been observed.
     */
    public void onVillainActed(Action action, double toCall) {
    }

    public void beforeHeroAction() {
        //do nothing
    }

    public void onHeroActed(Action action) {
    }

    public void onHandEnded() {
        if (gameInfo.isRiver()) {
            turnRiverPot.add(new Pair(pot, gameInfo.getPotSize() / gameInfo.getBigBlindSize()));
        }
        if (gameInfo.isTurn()) {
            flopTurnPot.add(new Pair(pot, gameInfo.getPotSize() / gameInfo.getBigBlindSize()));
        }

    }
}

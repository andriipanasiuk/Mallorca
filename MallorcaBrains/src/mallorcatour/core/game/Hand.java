/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class that represents poker hand.
 * @author Andrew
 */
public class Hand implements Serializable {

    private final static long serialVersionUID = 1L;
    private Date date;
    private LimitType limitType;
    private List<OpenPlayerInfo> players;
    private Card flop1, flop2, flop3;
    private Card turn, river;

    private String buttonPlayer;
    private double smallBlind, bigBlind;

    private List<PlayerAction> preflopActions;
    private List<PlayerAction> flopActions;
    private List<PlayerAction> turnActions;
    private List<PlayerAction> riverActions;
    private long id;

    public Hand(long id, Date date, List<OpenPlayerInfo> players, String button,
            double smallBlind, double bigBlind, LimitType limitType) {
        this.id = id;
        this.date = date;
        this.players = players;
        this.buttonPlayer = button;
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
        this.limitType = limitType;
        preflopActions = new ArrayList<PlayerAction>();
    }

    public void setFlop(Card flop1, Card flop2, Card flop3) {
        this.flop1 = flop1;
        this.flop2 = flop2;
        this.flop3 = flop3;
        flopActions = new ArrayList<PlayerAction>();
    }

    public void setTurn(Card turn) {
        this.turn = turn;
        turnActions = new ArrayList<PlayerAction>();
    }

    public void setRiver(Card river) {
        this.river = river;
        riverActions = new ArrayList<PlayerAction>();
    }

    public List<PlayerAction> getActions(PokerStreet street) {
        if (street == PokerStreet.PREFLOP) {
            return preflopActions;
        } else if (street == PokerStreet.FLOP) {
            return flopActions;
        } else if (street == PokerStreet.TURN) {
            return turnActions;
        } else if (street == PokerStreet.RIVER) {
            return riverActions;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void addAction(PlayerAction action) {
        if (getRiverActions() != null) {
            getRiverActions().add(action);
        } else if (getTurnActions() != null) {
            getTurnActions().add(action);
        } else if (getFlopActions() != null) {
            getFlopActions().add(action);
        } else {
            getPreflopActions().add(action);
        }
    }

    public List<OpenPlayerInfo> getPlayers() {
        return players;
    }

    public long getId() {
        return id;
    }

    public double getEffectiveStack() {
        double result = Double.MAX_VALUE;
        for (PlayerInfo playerInfo : players) {
            if (result > playerInfo.getStack()) {
                result = playerInfo.getStack();
            }
        }
        return result;
    }

    public OpenPlayerInfo getPlayerInfo(String name) {
        for (OpenPlayerInfo player : players) {
            if (player.getName().equals(name)) {
                return player;
            }
        }
        throw new IllegalArgumentException("There is no player with name " + name);
    }

    /**
     * @return the preflopActions
     */
    public List<PlayerAction> getPreflopActions() {
        return preflopActions;
    }

    /**
     * @return the flopActions
     */
    public List<PlayerAction> getFlopActions() {
        return flopActions;
    }

    /**
     * @return the turnActions
     */
    public List<PlayerAction> getTurnActions() {
        return turnActions;
    }

    /**
     * @return the riverActions
     */
    public List<PlayerAction> getRiverActions() {
        return riverActions;
    }

    /**
     * @return the flop
     */
    public Flop getFlop() {
        if (flop1 != null && flop2 != null && flop3 != null) {
            return new Flop(flop1, flop2, flop3);
        } else {
            return null;
        }
    }

    /**
     * @return the turn
     */
    public Card getTurn() {
        return turn;
    }

    /**
     * @return the river
     */
    public Card getRiver() {
        return river;
    }

    /**
     * @return the buttonPlayer
     */
    public String getButtonPlayer() {
        return buttonPlayer;
    }

    /**
     * @return the bigBlind
     */
    public double getBigBlind() {
        return bigBlind;
    }

    public boolean hasFlopActions() {
        return getFlopActions() != null && !getFlopActions().isEmpty();
    }

    public boolean hasTurnActions() {
        return getTurnActions() != null && !getTurnActions().isEmpty();
    }

    public boolean hasRiverActions() {
        return getRiverActions() != null && !getRiverActions().isEmpty();
    }

    /**
     * @return the smallBlind
     */
    public double getSmallBlind() {
        return smallBlind;
    }

    private void processActions(List<PlayerAction> actions, StringBuilder result) {
        for (PlayerAction playerAction : actions) {
            result.append(playerAction.getName());
            result.append(" ");
            Action action = playerAction.getAction();
            if (action.isFold()) {
                result.append("folds");
            } else if (action.isCall()) {
                result.append("calls ");
                result.append(action.getAmount());
            } else if (action.isCheck()) {
                result.append("checks");
            } else if (action.isAggressive()) {
                result.append("raises ");
                result.append(action.getAmount());
            }
            result.append("\n");
        }
    }

    public Date getStartingDate() {
        return date;
    }

    public LimitType getLimitType() {
        return limitType;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String smallBlindString = "", bigBlindString = "";
        for (OpenPlayerInfo player : players) {
            String name = player.getName();
            result.append(name);
            if (name.equals(getButtonPlayer())) {
                result.append(" *");
                smallBlindString = name + " posts small blind " + getBigBlind() / 2 + "\n";
            } else {
                bigBlindString = name + " posts big blind " + getBigBlind() + "\n";
            }
            result.append(" ");
            result.append(player.getStack());
            HoleCards holeCards = player.getHoleCards();
            if (holeCards != null) {
                result.append(" ");
                result.append(holeCards);
            }
            if (player.isSittingOut()) {
                result.append(" is sitting out");
            }
            result.append("\n");
        }
        result.append("\n");
        //
        result.append(smallBlindString);
        result.append(bigBlindString);

        processActions(getPreflopActions(), result);
        if (flop1 != null) {
            result.append("\n");
            result.append("FLOP: ");
            result.append(flop1);
            result.append(" ");
            result.append(flop2);
            result.append(" ");
            result.append(flop3);
            result.append("\n");
        } else {
            return result.toString();
        }
        processActions(getFlopActions(), result);
        if (turn != null) {
            result.append("\n");
            result.append("TURN: ");
            result.append(flop1);
            result.append(" ");
            result.append(flop2);
            result.append(" ");
            result.append(flop3);
            result.append(" ");
            result.append(turn);
            result.append("\n");
        } else {
            return result.toString();
        }
        processActions(getTurnActions(), result);
        if (river != null) {
            result.append("\n");
            result.append("RIVER: ");
            result.append(flop1);
            result.append(" ");
            result.append(flop2);
            result.append(" ");
            result.append(flop3);
            result.append(" ");
            result.append(turn);
            result.append(" ");
            result.append(river);
            result.append("\n");
        } else {
            return result.toString();
        }
        processActions(getRiverActions(), result);
        return result.toString();
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.controller;

import java.util.List;
import mallorcatour.core.bot.LimitType;
import mallorcatour.game.core.Card;
import mallorcatour.game.core.PokerStreet;
import mallorcatour.core.bot.IGameInfo;
import mallorcatour.grandtorino.robot.PlayerInfo;

/**
 *
 * @author Andrew
 */
public class HUGameInfo implements IGameInfo {

    LimitType limitType;
    int[] raisesOnStreet = new int[4];
    PokerStreet street;
    double ante = 0;
    double smallBlind, bigBlind;
    List<Card> boardCards;
    double pot;
    //on preflop
    double effectiveStack;
    //player infos
    PlayerInfo heroInfo, villainInfo;
    double bankrollAtRisk;

    public HUGameInfo() {
        for (int i = 0; i < 4; i++) {
            raisesOnStreet[i] = 0;
        }
    }

    public PokerStreet getStage() {
        return street;
    }

    public boolean isPreFlop() {
        return street == PokerStreet.PREFLOP;
    }

    public boolean isPostFlop() {
        return street != PokerStreet.PREFLOP;
    }

    public boolean isFlop() {
        return street == PokerStreet.FLOP;
    }

    public boolean isTurn() {
        return street == PokerStreet.TURN;
    }

    public boolean isRiver() {
        return street == PokerStreet.RIVER;
    }

    public double getSmallBlindSize() {
        return smallBlind;
    }

    public double getBigBlindSize() {
        return bigBlind;
    }

    public List<Card> getBoard() {
        return boardCards;
    }

    public double getPotSize() {
        return pot;
    }

    public String getButtonName() {
        if (heroInfo.isOnButton()) {
            return heroInfo.getName();
        } else {
            return villainInfo.getName();
        }
    }

    public double getHeroAmountToCall() {
        double betDifference = villainInfo.getBet() - heroInfo.getBet();
        if (betDifference <= 0) {
            return 0;
        } else {
            return Math.min(betDifference, heroInfo.getStack());
        }
    }

    private PlayerInfo getPlayerInfo(String name) {
        if (heroInfo.getName().equals(name)) {
            return heroInfo;
        } else {
            return villainInfo;
        }
    }

    public double getBankRoll(String name) {
        return getPlayerInfo(name).getStack();
    }

    public double getBankRollAtRisk() {
        return bankrollAtRisk;
    }

    public boolean canHeroRaise() {
        if (limitType == LimitType.NO_LIMIT) {
            boolean result = getBankRollAtRisk() > 0;
            return result;
        } else {
            return raisesOnStreet[street.intValue()] < 4;
        }
    }

    public int getNumRaises(){
        return raisesOnStreet[street.intValue()];
    }

    public LimitType getLimitType() {
        return limitType;
    }

    public List<mallorcatour.game.core.PlayerInfo> getPlayers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

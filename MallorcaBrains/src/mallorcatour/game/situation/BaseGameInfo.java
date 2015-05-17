/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.game.situation;

import java.util.List;
import mallorcatour.core.bot.LimitType;
import mallorcatour.game.core.Card;
import mallorcatour.game.core.PlayerInfo;
import mallorcatour.game.core.PokerStreet;
import mallorcatour.core.bot.IGameInfo;

/**
 *
 * @author Andrew
 */
public class BaseGameInfo implements IGameInfo {

    LimitType limitType;
    int[] raisesOnStreet = new int[4];
    List<PlayerInfo> players;
    PokerStreet street;
    String buttonName;
    boolean canHeroRaise;
    double pot;
    double bigBlind;
    double heroAmountToCall;
    List<Card> board;
    double bankrollAtRisk;

    public BaseGameInfo() {
        for (int i = 0; i < 4; i++) {
            raisesOnStreet[i] = 0;
        }
    }

    public double getBigBlindSize() {
        return bigBlind;
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

    public List<Card> getBoard() {
        return board;
    }

    public double getPotSize() {
        return pot;
    }

    public String getButtonName() {
        return buttonName;
    }

    public double getHeroAmountToCall() {
        return heroAmountToCall;
    }

    public double getBankRollAtRisk() {
        return bankrollAtRisk;
    }

    public boolean canHeroRaise() {
        if (limitType == LimitType.NO_LIMIT) {
            return getBankRollAtRisk() > 0;
        } else {
            return raisesOnStreet[street.intValue()] < 4;
        }
    }

    public double getBankRoll(String name) {
        for (PlayerInfo player : players) {
            if (player.getName().equals(name)) {
                return player.getStack();
            }
        }
        throw new IllegalArgumentException("There is no player with name " + name);
    }

    public int getNumRaises() {
        return raisesOnStreet[street.intValue()];
    }

    public LimitType getLimitType() {
        return limitType;
    }

    public List<PlayerInfo> getPlayers() {
        return players;
    }
}

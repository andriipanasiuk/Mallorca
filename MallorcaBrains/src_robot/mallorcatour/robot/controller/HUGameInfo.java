/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.controller;

import java.util.List;

import mallorcatour.core.game.Card;
import mallorcatour.core.game.Flop;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.robot.PlayerInfo;

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
    List<Card> board;
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
        return board;
    }

    public double getPotSize() {
        return pot;
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

	public List<mallorcatour.core.game.PlayerInfo> getPlayers() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public mallorcatour.core.game.PlayerInfo getPlayer(String name) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	@Override
	public Flop getFlop() {
		return new Flop(board.get(0), board.get(1), board.get(2));
	}

	@Override
	public Card getTurn() {
		return board.get(3);
	}

	@Override
	public Card getRiver() {
		return board.get(4);
	}

	@Override
	public boolean onButton() {
		return heroInfo.isOnButton();
	}
}

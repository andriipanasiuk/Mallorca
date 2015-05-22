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
import mallorcatour.robot.ExtPlayerInfo;

/**
 *
 * @author Andrew
 */
public class HUGameInfo implements IGameInfo {

    public LimitType limitType;
    public int[] raisesOnStreet = new int[4];
    public PokerStreet street;
    public double bigBlind;
    public List<Card> board;
    public double pot;
    public double amountToCall;
    public ExtPlayerInfo heroInfo, villainInfo;
    public double bankrollAtRisk;
	public static final int SITTING_OUT = -2;

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

    private ExtPlayerInfo getPlayerInfo(String name) {
        if (heroInfo.getName().equals(name)) {
            return heroInfo;
        } else {
            return villainInfo;
        }
    }

    public double getBankRoll(String name) {
        return getPlayerInfo(name).getStack();
    }

    @Override
    public boolean isVillainSitOut(){
    	return villainInfo.getStack() == HUGameInfo.SITTING_OUT;
    }

    @Override
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

    @Override
    public LimitType getLimitType() {
        return limitType;
    }

	@Override
	public mallorcatour.core.game.PlayerInfo getPlayer(String name) {
		throw new UnsupportedOperationException();
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

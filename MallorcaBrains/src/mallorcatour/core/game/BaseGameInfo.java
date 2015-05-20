/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.game;

import java.util.List;

import mallorcatour.core.game.interfaces.IGameInfo;

/**
 *
 * @author Andrew
 */
public class BaseGameInfo implements IGameInfo {

	public LimitType limitType;
	public int[] raisesOnStreet = new int[4];
    public List<PlayerInfo> players;
    public PokerStreet street;
    public String buttonName;
    public boolean canHeroRaise;
    public double pot;
    public double bigBlind;
    public double heroAmountToCall;
    public List<Card> board;
    public double bankrollAtRisk;

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

	@Override
	public PlayerInfo getPlayer(String name) {
		for (PlayerInfo info : players) {
			if (info.getName().equals(name)) {
				return info;
			}
		}
		throw new IllegalArgumentException("There is no player with name: " + name);
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
}

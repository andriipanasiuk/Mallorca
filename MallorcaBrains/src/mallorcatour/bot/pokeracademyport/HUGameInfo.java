/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.pokeracademyport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mallorcatour.core.game.Card;
import mallorcatour.core.game.Flop;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.OpenPlayerInfo;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.util.Log;

import com.biotools.meerkat.GameInfo;

/**
 *
 * @author Andrew
 */
public class HUGameInfo implements IPlayerGameInfo {

    private final GameInfo gameInfo;
    private final LimitType limitType;
    private int heroSeat;

    public HUGameInfo(GameInfo paGameInfo) {
        this.gameInfo = paGameInfo;
        this.limitType = LimitType.FIXED_LIMIT;
    }

    void setHeroSeat(int heroSeat) {
        this.heroSeat = heroSeat;
    }

    public PokerStreet getStage() {
        return PokerStreet.valueOf(gameInfo.getStage());
    }

    public boolean isPreFlop() {
        return gameInfo.isPreFlop();
    }

    public boolean isPostFlop() {
        return gameInfo.isPostFlop();
    }

    public boolean isFlop() {
        return gameInfo.isFlop();
    }

    public boolean isTurn() {
        return gameInfo.isTurn();
    }

    public boolean isRiver() {
        return gameInfo.isRiver();
    }

    public double getSmallBlindSize() {
        return gameInfo.getSmallBlindSize();
    }

    public double getBigBlindSize() {
        return gameInfo.getBigBlindSize();
    }

    public List<Card> getBoard() {
        Log.d("PAGameInfo Board: " + gameInfo.getBoard());
        Log.d("PAGameInfo Board last card index: " + gameInfo.getBoard().getLastCardIndex());

        String board = gameInfo.getBoard().toString();
        List<Card> result = new ArrayList<Card>();
        if (board.isEmpty()) {
            return result;
        }
        String[] cards = board.split(" ");
        try {
            for (String card : cards) {
                result.add(Card.valueOf(card));
            }
            Log.d("My board: " + result);
        } catch (Exception e) {
            Log.d("!!!!!!!!!!!!!!!!!!!!!!!");
            Log.d("Illegal board: " + board);
            Log.d("Illegal board: " + Arrays.toString(cards));
            Log.d("!!!!!!!!!!!!!!!!!!!!!!!");
            throw new RuntimeException(e);
        }
        return result;
    }

    public double getPotSize() {
        return gameInfo.getMainPotSize();
    }

    public double getAmountToCall() {
        return gameInfo.getPlayer(heroSeat).getAmountToCall();
    }

    public double getBankRoll(String name) {
        Log.d("HUGameInfo. getBankroll() for " + name);
        return gameInfo.getPlayer(name).getBankRoll();
    }

    public double getBankRollAtRisk() {
        return gameInfo.getBankRollAtRisk(heroSeat);
    }

    public boolean canHeroRaise() {
        return gameInfo.canRaise(heroSeat);
    }

    public LimitType getLimitType() {
        return limitType;
    }

    public List<mallorcatour.core.game.PlayerInfo> getPlayers() {
        throw new UnsupportedOperationException();
    }

    @Override
	public OpenPlayerInfo getVillain() {
    	throw new UnsupportedOperationException();
	}

    public int getNumRaises() {
        return gameInfo.getNumRaises();
    }

	@Override
	public Flop getFlop() {
		//TODO implement
		throw new UnsupportedOperationException();
	}

	@Override
	public Card getTurn() {
		//TODO implement
		throw new UnsupportedOperationException();
	}

	@Override
	public Card getRiver() {
		//TODO implement
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean onButton() {
		return gameInfo.getPlayer(heroSeat).isButton();
	}

	@Override
	public boolean isVillainSitOut() {
		return false;
	}

	@Override
	public OpenPlayerInfo getHero() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean onButton(String name) {
		return gameInfo.getPlayer(name).isButton();
	}

}

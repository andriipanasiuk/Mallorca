/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.bot;

import java.util.List;
import mallorcatour.game.core.Card;
import mallorcatour.game.core.PlayerInfo;
import mallorcatour.game.core.PokerStreet;

/**
 *
 * @author Andrew
 */
public interface IGameInfo {

    public static int SITTING_OUT = -2;

    public double getBigBlindSize();

    public PokerStreet getStage();

    public boolean isPreFlop();

    public boolean isPostFlop();

    public boolean isFlop();

    public boolean isTurn();

    public boolean isRiver();

    public List<Card> getBoard();

    public double getPotSize();

    public String getButtonName();

    public double getHeroAmountToCall();

    public double getBankRoll(String name);

    public double getBankRollAtRisk();

    public boolean canHeroRaise();

    public int getNumRaises();

    public LimitType getLimitType();

    List<PlayerInfo> getPlayers();
}

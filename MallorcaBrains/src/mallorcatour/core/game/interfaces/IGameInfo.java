/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.game.interfaces;

import java.util.List;

import mallorcatour.core.game.Card;
import mallorcatour.core.game.Flop;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.PlayerInfo;
import mallorcatour.core.game.PokerStreet;

/**
 * 
 * @author Andrew
 */
public interface IGameInfo {

	public double getBigBlindSize();

	public PokerStreet getStage();

	public boolean isPreFlop();

	public boolean isPostFlop();

	public boolean isFlop();

	public boolean isTurn();

	public boolean isRiver();

	public List<Card> getBoard();

	public Flop getFlop();

	public Card getTurn();

	public Card getRiver();

	public double getPotSize();

	public double getBankRollAtRisk();

	public int getNumRaises();

	public LimitType getLimitType();

	PlayerInfo getPlayer(String name);

}

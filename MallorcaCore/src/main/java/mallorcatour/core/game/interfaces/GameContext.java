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
 * Интерфейс обозначающий контекст текущей раздачи с позиции стороннего наблюдателя.
 */
public interface GameContext {

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

	public double getPot(PokerStreet street);

	public double getPotSize();

	public double getBankRollAtRisk();

	public int getNumRaises();

	public LimitType getLimitType();

	public boolean onButton(String name);

	public PlayerInfo getHero(String hero);

	public PlayerInfo getVillain(String hero);

	public double getAmountToCall(String hero);

}

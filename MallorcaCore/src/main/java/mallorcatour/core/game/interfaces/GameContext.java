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

	 double getBigBlindSize();

	 PokerStreet getStage();

	 boolean isPreFlop();

	 boolean isPostFlop();

	 boolean isFlop();

	 boolean isTurn();

	 boolean isRiver();

	 List<Card> getBoard();

	 Flop getFlop();

	 Card getTurn();

	 Card getRiver();

	 double getPot(PokerStreet street);

	 double getPotSize();

	 double getBankRollAtRisk();

	 int getNumRaises();

	 LimitType getLimitType();

	 boolean onButton(String name);

	 PlayerInfo getHero(String hero);

	 PlayerInfo getVillain(String hero);

	double getAmountToCall(String hero);

}

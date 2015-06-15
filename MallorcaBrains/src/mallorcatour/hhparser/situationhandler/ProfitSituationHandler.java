/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.hhparser.situationhandler;

import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.equilator.preflop.simple.EquilatorPreflopSimple;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.GameInfo;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.OpenPlayerInfo;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.tools.Log;

/**
 * 
 * @author Andrew
 */
//TODO test this class and fix bugs
public class ProfitSituationHandler implements IPlayer {

	private GameInfo gameInfo;;
	protected String heroName, villainName;
	private Card heroCard1, heroCard2;
	protected LimitType limitType;
	private double profit;
	private double expectedProfit;
	private double heroToCall;
	private boolean hasFlop;
	private String hero;

	public ProfitSituationHandler() {
	}

	/**
	 * An event called to tell us our hole cards and seat number
	 * 
	 * @param c1
	 *            your first hole card
	 * @param c2
	 *            your second hole card
	 * @param seat
	 *            your seat number at the table
	 */
	@Override
	public void onHoleCards(Card c1, Card c2) {
		heroCard1 = c1;
		heroCard2 = c2;
		profit = -1;
//		this.villainName = villainName;
		if (gameInfo.onButton()) {
			heroToCall = gameInfo.getBigBlindSize() / 2;
		} else {
			heroToCall = 0;
		}
		hasFlop = false;
	}

	/**
	 * A new betting round has started.
	 */
	@Override
	public void onStageEvent(PokerStreet street) {
		if (street == PokerStreet.FLOP) {
			hasFlop = true;
			double pot = gameInfo.getPotSize();
			OpenPlayerInfo villainInfo = getVillainInfo();
			double heroPreflopStrength = EquilatorPreflopSimple.strengthByFormula(heroCard1, heroCard2);
			double villainPreflopStrength = EquilatorPreflopSimple.strengthByFormula(villainInfo.getHoleCards().first,
					villainInfo.getHoleCards().second);
			expectedProfit = pot * heroPreflopStrength / (heroPreflopStrength + villainPreflopStrength) - (pot / 2);
		}
	}

	public boolean hasFlop() {
		return hasFlop;
	}

	/**
	 * A new game has been started.
	 * 
	 * @param gi
	 *            the game stat information
	 */
	@Override
	public void onHandStarted(IPlayerGameInfo gameInfo) {
		this.gameInfo = (GameInfo) gameInfo;
	}

	/**
	 * An villain action has been observed.
	 */
	@Override
	public void onActed(Action action, double toCall, String name) {
		if (!name.equals(hero)) {
			onVilllainActed(action, toCall);
		} else {
			onHeroActed(action);
		}
	}

	private void onVilllainActed(Action action, double toCall) {
		if (action.isFold()) {
			if (!gameInfo.isPreFlop()) {
				Log.d("Hero wins uncontested");
				profit = (gameInfo.getPotSize() - toCall) / 2;
				Log.d("Profit: " + profit);
			}
		} else if (action.isAggressive()) {
			heroToCall = action.getAmount();
		}
	}

	private void onHeroActed(Action action) {
		if (action.isFold()) {
			if (!gameInfo.isPreFlop()) {
				profit = -(gameInfo.getPotSize() - heroToCall) / 2;
				Log.d("Profit: " + profit);
			}
		} else if (action.isPassive()) {
			heroToCall = 0;
		}
	}

	public double getProfit() {
		return profit;
	}

	public double getExpectedProfit() {
		return expectedProfit;
	}

	private OpenPlayerInfo getVillainInfo() {
		OpenPlayerInfo villainInfo = gameInfo.getVillain();
		return villainInfo;
	}

	@Override
	public void onHandEnded() {
		if (profit == -1 && hasFlop) {
			double pot = gameInfo.getPotSize();
			int[] heroCards = new int[7];
			int[] villainCards = new int[7];
			heroCards[0] = heroCard1.intValueForBrecher();
			heroCards[1] = heroCard2.intValueForBrecher();

			OpenPlayerInfo villainInfo = getVillainInfo();
			villainCards[0] = villainInfo.getHoleCards().first.intValueForBrecher();
			villainCards[1] = villainInfo.getHoleCards().second.intValueForBrecher();
			for (int i = 2; i < 7; i++) {
				heroCards[i] = gameInfo.getBoard().get(i - 2).intValueForBrecher();
				villainCards[i] = gameInfo.getBoard().get(i - 2).intValueForBrecher();
			}
			int heroCombination = PokerEquilatorBrecher.combination(heroCards);
			int villainCombination = PokerEquilatorBrecher.combination(villainCards);
			if (heroCombination > villainCombination) {
				profit = pot / 2;
			} else if (heroCombination == villainCombination) {
				profit = 0;
			} else {
				profit = -pot / 2;
			}
			Log.d("Profit: " + profit);
		}
	}

	@Override
	public Action getAction() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}
}

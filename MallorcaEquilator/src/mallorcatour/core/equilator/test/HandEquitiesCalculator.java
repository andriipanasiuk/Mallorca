package mallorcatour.core.equilator.test;

import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.equilator.preflop.EquilatorPreflop;
import mallorcatour.core.game.Hand;
import mallorcatour.core.game.OpenPlayerInfo;
import mallorcatour.core.game.situation.StreetEquity;

public class HandEquitiesCalculator {

	public static HandEquities calculateEquities(final Hand hand, String heroName) {
	    double preflopEquity = -1;
	    double flopEquity = -1;
	    double flopPositiveEquity = -1;
	    double flopNegativeEquity = -1;
	    double turnEquity = -1;
	    double turnPositiveEquity = -1;
	    double turnNegativeEquity = -1;
	    double riverEquity = -1;
	    //calculating
	    preflopEquity = preflopEquity(hand, heroName);
	
	    if (hand.hasFlopActions()) {
	        StreetEquity equity = getFlopEquity(hand, heroName);
	        flopEquity = equity.strength;
	        flopPositiveEquity = equity.positivePotential;
	        flopNegativeEquity = equity.negativePotential;
	    }
	    //turn
	    if (hand.hasTurnActions()) {
	        StreetEquity equity = getTurnEquity(hand, heroName);
	        turnEquity = equity.strength;
	        turnPositiveEquity = equity.positivePotential;
	        turnNegativeEquity = equity.negativePotential;
	    }
	    //river
	    if (hand.hasRiverActions()) {
	        riverEquity = getRiverStrength(hand, heroName);
	    }
	    return new BaseHandEquities(preflopEquity, flopEquity,
	            flopPositiveEquity, flopNegativeEquity, turnEquity,
	            turnPositiveEquity, turnNegativeEquity, riverEquity);
	}
	
	 public static double getPreflopStrength(Hand hand, String player) {
	    	OpenPlayerInfo playerInfo = hand.getPlayerInfo(player);
			return EquilatorPreflop.strengthVsRandom(playerInfo.getHoleCards().first, playerInfo.getHoleCards().second);
	    }

	    public static StreetEquity getFlopEquity(Hand hand, String player) {
	    	OpenPlayerInfo playerInfo = hand.getPlayerInfo(player);
	        return PokerEquilatorBrecher.equityOnFlop(playerInfo.getHoleCards().first,
	                playerInfo.getHoleCards().second, hand.getFlop().first,
	                hand.getFlop().second, hand.getFlop().third);
	    }

	    public static StreetEquity getTurnEquity(Hand hand, String player) {
	    	OpenPlayerInfo playerInfo = hand.getPlayerInfo(player);
	        return PokerEquilatorBrecher.equityOnTurn(playerInfo.getHoleCards().first,
	                playerInfo.getHoleCards().second, hand.getFlop().first,
	                hand.getFlop().second, hand.getFlop().third, hand.getTurn());
	    }

	    public static double getRiverStrength(Hand hand, String player) {
	    	OpenPlayerInfo playerInfo = hand.getPlayerInfo(player);
	        return PokerEquilatorBrecher.strengthOnRiver(playerInfo.getHoleCards().first,
	                playerInfo.getHoleCards().second, hand.getFlop().first,
	                hand.getFlop().second, hand.getFlop().third,
	                hand.getTurn(), hand.getRiver());
	    }

	    public static double preflopEquity(Hand hand, String player) {
	    	OpenPlayerInfo playerInfo = hand.getPlayerInfo(player);
	        return EquilatorPreflop.strengthVsRandom(playerInfo.getHoleCards().first,
	                playerInfo.getHoleCards().second);
	    }

}
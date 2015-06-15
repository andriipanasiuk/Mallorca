/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.hhparser.core;


import java.util.List;

import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.equilator.StreetEquity;
import mallorcatour.core.equilator.preflop.EquilatorPreflop;
import mallorcatour.core.game.Hand;
import mallorcatour.core.game.OpenPlayerInfo;
import mallorcatour.core.game.PlayerAction;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.tools.Pair;

/**
 *
 * @author Andrew
 */
public class HandManager {

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

    /**
     * Calculates conclusion of the hand for
     * collecting statistics of some player
     * @param hand
     * @param heroName - player whos statistics we want
     * @return handConclusion @see HandConclusion
     */
    public HandConclusion getConclusion(Hand hand, String heroName) {
        HandConclusion result = new HandConclusion();
        result.contbet = calculateContBet(hand, heroName);
        result.vpip = calculateVpip(hand, heroName);
        result.pfr = calculatePfr(hand, heroName);
        result.aggression = calculatePostflopAF(hand, heroName);
        return result;
    }

    public static double calculateVpip(List<Hand> hands, String heroName) {
        int all = 0, vpip = 0;
        for (Hand hand : hands) {
            all++;
            int temp = calculateVpip(hand, heroName);
            if (temp != -1) {
                vpip += temp;
            }
        }
        return (double) vpip / all;
    }

    public static double calculatePfr(List<Hand> hands, String heroName) {
        int all = 0, pfr = 0;
        for (Hand hand : hands) {
            all++;
            int temp = calculatePfr(hand, heroName);
            if (temp != -1) {
                pfr += temp;
            }
        }
        return (double) pfr / all;
    }

    public static double calculatePostflopAF(List<Hand> hands, String heroName) {
        Pair<Integer, Integer> result = new Pair<Integer, Integer>(0, 0);
        for (Hand hand : hands) {
            Pair<Integer, Integer> temp = calculatePostflopAF(hand, heroName);
            result.first += temp.first;
            result.second += temp.second;
        }
        return (double) result.first / (result.second - result.first);
    }

    public static double calculatePostflopAggressionFrequency(
            List<Hand> hands, String heroName) {
        Pair<Integer, Integer> result = new Pair<Integer, Integer>(0, 0);
        for (Hand hand : hands) {
            Pair<Integer, Integer> temp = calculatePostflopAggressionFrequency(hand, heroName);
            result.first += temp.first;
            result.second += temp.second;
//            Log.d("Aggressin freq. " + result.first() + " " + result.second());
        }
        return (double) result.first / result.second;
    }

    public static double calculatePostflopFoldFrequency(
            List<Hand> hands, String heroName) {
        Pair<Integer, Integer> result = new Pair<Integer, Integer>(0, 0);
        for (Hand hand : hands) {
            Pair<Integer, Integer> temp = calculatePostflopFoldFrequency(hand, heroName);
            result.first += temp.first;
            result.second += temp.second;
        }
        return (double) result.first / result.second;
    }

    public static double calculateWTSD(List<Hand> hands, String heroName) {
        int all = 0, wtsd = 0;
        for (Hand hand : hands) {
            int temp = calculateWTSD(hand, heroName);
            if (temp == -1) {
                continue;
            }
            all++;
            wtsd += temp;
        }
        return (double) wtsd / all;
    }

    public static int calculateVpip(Hand hand, String heroName) {
        PlayerAction firstAction = getFirstAction(hand, heroName, PokerStreet.PREFLOP);
        if (firstAction == null) {
            return -1;
        }
        for (PlayerAction action : hand.getPreflopActions()) {
            if (action.getName().equals(heroName) && action.getAction().isFold()) {
                return 0;
            }
            if (action.getName().equals(heroName) && action.getAction().getAmount() != 0) {
                return 1;
            }
        }
        if (hand.hasFlopActions()) {
            for (PlayerAction action : hand.getFlopActions()) {
                if (action.getName().equals(heroName) && action.getAction().isFold()) {
                    return 0;
                }
                if (action.getName().equals(heroName) && action.getAction().getAmount() != 0) {
                    return 1;
                }
            }
        }
        if (hand.hasTurnActions()) {
            for (PlayerAction action : hand.getTurnActions()) {
                if (action.getName().equals(heroName) && action.getAction().isFold()) {
                    return 0;
                }
                if (action.getName().equals(heroName) && action.getAction().getAmount() != 0) {
                    return 1;
                }
            }
        }
        if (hand.hasRiverActions()) {
            for (PlayerAction action : hand.getRiverActions()) {
                if (action.getName().equals(heroName) && action.getAction().isFold()) {
                    return 0;
                }
                if (action.getName().equals(heroName) && action.getAction().getAmount() != 0) {
                    return 1;
                }
            }
        }
        return 0;
    }

    public static int calculatePfr(Hand hand, String heroName) {
        PlayerAction action = getFirstAction(hand, heroName, PokerStreet.PREFLOP);
        if (action == null) {
            return -1;
        } else if (action.getAction().isAggressive()) {
            return 1;
        } else {
            //hero does not raise
            return 0;
        }
    }

    /**
     *
     * @param hand
     * @param heroName
     * @return pair (count of hero aggressive actions, count of aggressive+call actions(no-fold)
     * hero actions)
     */
    public static Pair<Integer, Integer> calculatePostflopAF(Hand hand,
            String heroName) {
        Pair<Integer, Integer> result = new Pair<Integer, Integer>(0, 0);

        List<PlayerAction> actions;
        Pair<Integer, Integer> streetAggression;

        if (hand.hasFlopActions()) {
            actions = hand.getFlopActions();
            streetAggression = calculateAF(actions, heroName);
            result.first = result.first + streetAggression.first;
            result.second = result.second + streetAggression.second;
        }
        if (hand.hasTurnActions()) {
            actions = hand.getTurnActions();
            streetAggression = calculateAF(actions, heroName);
            result.first = result.first + streetAggression.first;
            result.second = result.second + streetAggression.second;
        }
        if (hand.hasRiverActions()) {
            actions = hand.getRiverActions();
            streetAggression = calculateAF(actions, heroName);
            result.first = result.first + streetAggression.first;
            result.second = result.second + streetAggression.second;
        }
        return result;
    }

    //(aggressive actions)/(all actions)
    public static Pair<Integer, Integer> calculatePostflopAggressionFrequency(
            Hand hand, String heroName) {
        Pair<Integer, Integer> result = new Pair<Integer, Integer>(0, 0);

        List<PlayerAction> actions;
        Pair<Integer, Integer> streetAggression;
        if (hand.hasFlopActions()) {
            actions = hand.getFlopActions();
            streetAggression = calculateAggressionFrequency(actions, heroName);
            result.first = result.first + streetAggression.first;
            result.second = result.second + streetAggression.second;
        }
        if (hand.hasTurnActions()) {
            actions = hand.getTurnActions();
            streetAggression = calculateAggressionFrequency(actions, heroName);
            result.first = result.first + streetAggression.first;
            result.second = result.second + streetAggression.second;
        }
        if (hand.hasRiverActions()) {
            actions = hand.getRiverActions();
            streetAggression = calculateAggressionFrequency(actions, heroName);
            result.first = result.first + streetAggression.first;
            result.second = result.second + streetAggression.second;
        }
        return result;
    }

    public static Pair<Integer, Integer> calculatePostflopFoldFrequency(
            Hand hand, String heroName) {
        Pair<Integer, Integer> result = new Pair<Integer, Integer>(0, 0);
        List<PlayerAction> actions;
        Pair<Integer, Integer> streetFold;
        if (hand.hasFlopActions()) {
            actions = hand.getFlopActions();
            streetFold = calculateFold(actions, heroName);
            result.first = result.first + streetFold.first;
            result.second = result.second + streetFold.second;
        }
        if (hand.hasTurnActions()) {
            actions = hand.getTurnActions();
            streetFold = calculateFold(actions, heroName);
            result.first = result.first + streetFold.first;
            result.second = result.second + streetFold.second;
        }
        if (hand.hasRiverActions()) {
            actions = hand.getRiverActions();
            streetFold = calculateFold(actions, heroName);
            result.first = result.first + streetFold.first;
            result.second = result.second + streetFold.second;
        }
        return result;
    }

    public static int calculateWTSD(Hand hand, String heroName) {
        if (hand.getPlayerInfo(heroName) == null) {
            throw new RuntimeException("There is no player " + heroName + " in hand");
        }
        if (hand.getFlop() == null) {
            return -1;
        }
        if (hand.getRiver() != null) {
            for (PlayerAction action : hand.getRiverActions()) {
                if (action.getAction().isFold()) {
                    return 0;
                }
            }
            return 1;
        }
        return 0;
    }

    private static Pair<Integer, Integer> calculateAF(
            List<PlayerAction> actions, String heroName) {
        Pair<Integer, Integer> result = new Pair<Integer, Integer>(0, 0);
        for (PlayerAction action : actions) {
            if (action.getName().equals(heroName)) {
                if (!action.getAction().isFold() && !action.getAction().isCheck()) {
                    result.second = result.second + 1;
                    if (action.getAction().isAggressive()) {
                        result.first = result.first + 1;
                    }
                }
            }
        }
        return result;
    }

    private static Pair<Integer, Integer> calculateAggressionFrequency(
            List<PlayerAction> actions, String heroName) {
        Pair<Integer, Integer> result = new Pair<Integer, Integer>(0, 0);
        for (PlayerAction action : actions) {
            if (action.getName().equals(heroName)) {
                result.second = result.second + 1;
                if (action.getAction().isAggressive()) {
                    result.first = result.first + 1;
                }
            }
        }
        return result;
    }

    private static Pair<Integer, Integer> calculateFold(
            List<PlayerAction> actions, String heroName) {
        Pair<Integer, Integer> result = new Pair<Integer, Integer>(0, 0);
        double toCall = 0;
        for (PlayerAction action : actions) {
            if (action.getName().equals(heroName)) {
                if (toCall != 0) {
                    result.second = result.second + 1;
                    if (action.getAction().isFold()) {
                        result.first = result.first + 1;
                    }
                }
            } else {
                toCall = action.getAction().getAmount();
            }
        }
        return result;
    }

    public static String getVillainName(Hand hand, String heroName) {
        List<OpenPlayerInfo> players = hand.getPlayers();
        if (players.get(0).getName().equals(heroName)) {
            return players.get(1).getName();
        } else if (players.get(1).getName().equals(heroName)) {
            return players.get(0).getName();
        } else {
            throw new IllegalArgumentException("There is no player: " + heroName
                    + " in hand");
        }
    }

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

    /**
     * Method for get last hero action in defined street.
     * Returns null if there is no hero action in this street.
     */
    private static PlayerAction getLastAction(Hand hand, String heroName,
            PokerStreet street) {
        List<PlayerAction> actionsOnStreet = hand.getActions(street);
        for (int i = actionsOnStreet.size() - 1; i >= 0; i--) {
            if (actionsOnStreet.get(i).getName().equals(heroName)) {
                return actionsOnStreet.get(i);
            }
        }
        return null;
    }

    /**
     * Method for get last hero action in defined street.
     * Returns null if there is no hero action in this street.
     */
    private static PlayerAction getFirstAction(Hand hand, String heroName,
            PokerStreet street) {
        List<PlayerAction> actionsOnStreet = hand.getActions(street);
        for (int i = 0; i < actionsOnStreet.size(); i++) {
            if (actionsOnStreet.get(i).getName().equals(heroName)) {
                return actionsOnStreet.get(i);
            }
        }
        return null;
    }

    /**
     * Returns -1 if there was no possibility to do contbet.
     * Returns 0 if there was  possibility but hero does not do cont bet.
     * Returns 1 if there was possibility and hero does contbet.
     */
    private static int calculateContBet(Hand hand, String heroName) {
        if (!hand.hasFlopActions()) {
            return -1;
        }
        List<PlayerAction> flopActions = hand.getFlopActions();
        PlayerAction lastHeroPreflop = getLastAction(hand, heroName, PokerStreet.PREFLOP);
        if (lastHeroPreflop == null || !lastHeroPreflop.getAction().isAggressive()) {
            return -1;
        }
        if (flopActions.get(0).getName().equals(heroName)) {
            if (flopActions.get(0).getAction().isAggressive()) {
                return 1;
            } else {
                return 0;
            }
        } else if (flopActions.get(0).getAction().isPassive()) {
            if (flopActions.get(1).getAction().isAggressive()) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }
}

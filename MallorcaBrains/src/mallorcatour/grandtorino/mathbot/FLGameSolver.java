/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.mathbot;

import java.util.HashMap;
import java.util.Map;
import mallorcatour.core.bot.LimitType;
import mallorcatour.core.equilator13.PokerEquilatorBrecher;
import mallorcatour.core.equilator13.StreetEquity;
import mallorcatour.game.advice.Advice;
import mallorcatour.game.core.Action;
import mallorcatour.game.core.Card;
import mallorcatour.game.core.Flop;
import mallorcatour.game.core.HoleCards;
import mallorcatour.game.core.PokerStreet;
import mallorcatour.game.core.Spectrum;
import mallorcatour.game.situation.LocalSituation;
import mallorcatour.game.situation.LocalSituationInterpreter;
import mallorcatour.grandtorino.nn.danielxn.DanielxnNeurals;
import mallorcatour.grandtorino.nn.modeller.BaseVillainModeller;
import mallorcatour.util.CollectionUtils;
import mallorcatour.util.Log;
import org.neuroph.core.NeuralNetwork;

/**
 *
 * @author Andrew
 */
public class FLGameSolver {

    private final static double NEARLY_ZERO = 0.0001;
    private final static double TURN_POT_COEFF = 4.91;
    private final static double RIVER_POT_COEFF = 3.14;
    private BaseVillainModeller villainModeller;
    private final int MIN_ADDITIONAL_PROFIT = 0;
    private static final double IP_ADDITIONAL_PROFIT_BB = 0.056;

    public FLGameSolver(BaseVillainModeller villainModeller) {
        this.villainModeller = villainModeller;
    }

    public Map<Action, Double> onSecondActionPreflop(int heroActions, int heroAggressiveActions,
            int villainActions, int villainAggressiveActions,
            double pot, double toCall,
            Spectrum villainSpectrum, HoleCards heroCards,
            Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton,
            int countOfRaises, double bigBlind) {
        return onSecondActionRecursive(heroActions, heroAggressiveActions,
                villainActions, villainAggressiveActions, pot,
                toCall, 0, villainSpectrum, new Card[]{}, heroCards,
                strengthMap, isHeroOnButton, PokerStreet.PREFLOP, countOfRaises, bigBlind);
    }

    public Map<Action, Double> onSecondActionFlop(int heroActions, int heroAggressiveActions,
            int villainActions, int villainAggressiveActions,
            double pot, double toCall,
            Spectrum villainSpectrum, Flop flop, HoleCards heroCards,
            Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton,
            int countOfRaises, double bigBlind) {
        return onSecondActionRecursive(heroActions, heroAggressiveActions,
                villainActions, villainAggressiveActions, pot,
                toCall, 0, villainSpectrum, flop.toArray(), heroCards,
                strengthMap, isHeroOnButton, PokerStreet.FLOP, countOfRaises, bigBlind);
    }

    public Map<Action, Double> onSecondActionTurn(int heroActions, int heroAggressiveActions,
            int villainActions, int villainAggressiveActions,
            double pot, double toCall,
            Spectrum villainSpectrum, Flop flop, Card turn, HoleCards heroCards,
            Map<HoleCards, StreetEquity> strengthMap, boolean isHeroOnButton,
            int countOfRaises, double bigBlind) {
        return onSecondActionRecursive(heroActions, heroAggressiveActions,
                villainActions, villainAggressiveActions, pot,
                toCall, 0, villainSpectrum,
                new Card[]{flop.first, flop.second, flop.third, turn},
                heroCards, strengthMap, isHeroOnButton, PokerStreet.TURN,
                countOfRaises, bigBlind);
    }

    public Map<Action, Double> onSecondActionRiver(int heroActions, int heroAggressiveActions,
            int villainActions, int villainAggressiveActions,
            double pot, double toCall,
            Spectrum villainSpectrum, Flop flop, Card turn, Card river,
            HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
            boolean isHeroOnButton, int countOfRaises, double bigBlind) {
        return onSecondActionRecursive(heroActions, heroAggressiveActions,
                villainActions, villainAggressiveActions, pot, toCall, 0, villainSpectrum,
                new Card[]{flop.first, flop.second, flop.third, turn, river},
                heroCards, strengthMap, isHeroOnButton, PokerStreet.RIVER,
                countOfRaises, bigBlind);
    }

    private Map<Action, Double> onSecondActionRecursive(int heroActions, int heroAggressiveActions,
            int villainActions, int villainAggressiveActions,
            double pot, double toCall,
            double heroInvestment, Spectrum villainSpectrum,
            Card[] board, HoleCards heroCards, Map<HoleCards, StreetEquity> villainStrengthMap,
            boolean isHeroOnButton, PokerStreet street, int countOfRaises, double bigBlind) {
        Map<Action, Double> result = new HashMap<Action, Double>();
        //if hero folds
        double foldProfit = -heroInvestment;
        result.put(Action.foldAction(), foldProfit);
        //if hero calls
        double strength = PokerEquilatorBrecher.strengthVsSpectrum(heroCards,
                board, villainSpectrum);
        double passiveProfit = strength * (pot - heroInvestment)
                - (1 - strength) * (toCall + heroInvestment);
        if (street == PokerStreet.PREFLOP && passiveProfit > 0) {
            int add = isHeroOnButton ? 1 : -1;
            passiveProfit += add * IP_ADDITIONAL_PROFIT_BB * bigBlind;
        } else if (street == PokerStreet.FLOP || street == PokerStreet.TURN) {
            StreetEquity equity = PokerEquilatorBrecher.equityVsSpectrum(heroCards,
                    board, villainSpectrum);
            double realEquity = StreetEquity.calculateRealEquity(equity);
            double nextStreetPot;
            if (street == PokerStreet.FLOP) {
                nextStreetPot = TURN_POT_COEFF * bigBlind;
            } else {
                nextStreetPot = RIVER_POT_COEFF * bigBlind;
            }
            double additionalPassiveProfit = realEquity * nextStreetPot - nextStreetPot / 2;
            if (additionalPassiveProfit > MIN_ADDITIONAL_PROFIT && toCall != 0) {
                passiveProfit += additionalPassiveProfit;
            }
        }
        result.put(Action.callAction(toCall), passiveProfit);
        //if hero cannot raise
        if (countOfRaises >= 4) {
            return result;
        }
        //if hero raises
        double raiseAmount;
        if (street == PokerStreet.PREFLOP || street == PokerStreet.FLOP) {
            raiseAmount = bigBlind;
        } else {
            raiseAmount = 2 * bigBlind;
        }
        Action heroReraiseAction = Action.createRaiseAction(toCall, -1);

        double aggressiveProfit = calculateHeroActionProfit(raiseAmount,
                heroActions, heroAggressiveActions,
                villainActions, villainAggressiveActions, pot,
                toCall, heroInvestment, villainSpectrum, board, heroCards,
                toCall != 0, true, isHeroOnButton, street, villainStrengthMap,
                countOfRaises + 1, bigBlind);

        result.put(heroReraiseAction, aggressiveProfit);
        return result;
    }

    public Map<Action, Double> onFirstActionPreFlop(int heroActions, int heroAggressiveActions,
            int villainActions, int villainAggressiveActions,
            double pot, Spectrum villainSpectrum,
            HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
            boolean wasVillainPreviousAggressive, boolean isHeroOnButton, double bigBlind) {
        Map<Action, Double> result = new HashMap<Action, Double>();

        //if hero folds
        double foldProfit = 0;
        result.put(Action.foldAction(), foldProfit);
        //if hero checks
        int countOfRaises = 1;
        double callProfit = calculateHeroActionProfit(0, heroActions,
                heroAggressiveActions, villainActions, villainAggressiveActions,
                pot, /*hero toCall*/ bigBlind / 2, /*hero investment*/ 0,
                villainSpectrum, new Card[]{}, heroCards, wasVillainPreviousAggressive,
                false, isHeroOnButton, PokerStreet.PREFLOP, strengthMap, countOfRaises, bigBlind);
        result.put(Action.callAction(bigBlind / 2), callProfit);

        //if hero raises
        double raiseAmount = bigBlind;
        Action heroRaiseAction = Action.createRaiseAction(raiseAmount, -1);

        double aggressiveProfit = calculateHeroActionProfit(raiseAmount, heroActions,
                heroAggressiveActions, villainActions, villainAggressiveActions,
                pot, bigBlind / 2, 0, villainSpectrum, new Card[]{}, heroCards,
                wasVillainPreviousAggressive, true, isHeroOnButton, PokerStreet.PREFLOP,
                strengthMap, countOfRaises + 1, bigBlind);
        result.put(heroRaiseAction, aggressiveProfit);
        return result;
    }

    public Map<Action, Double> onFirstActionFlop(int heroActions, int heroAggressiveActions,
            int villainActions, int villainAggressiveActions,
            double pot, Spectrum villainSpectrum,
            Flop flop, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
            boolean wasVillainPreviousAggressive, boolean isHeroOnButton, double bigBlind) {
        return onFirstAction(heroActions, heroAggressiveActions, villainActions,
                villainAggressiveActions, pot, villainSpectrum,
                flop.toArray(), heroCards, strengthMap, wasVillainPreviousAggressive,
                isHeroOnButton, PokerStreet.FLOP, bigBlind);
    }

    public Map<Action, Double> onFirstActionTurn(int heroActions, int heroAggressiveActions,
            int villainActions, int villainAggressiveActions,
            double pot, Spectrum villainSpectrum,
            Flop flop, Card turn, HoleCards heroCards, Map<HoleCards, StreetEquity> strengthMap,
            boolean wasVillainPreviousAggressive, boolean isHeroOnButton, double bigBlind) {
        return onFirstAction(heroActions, heroAggressiveActions, villainActions,
                villainAggressiveActions, pot, villainSpectrum,
                new Card[]{flop.first, flop.second, flop.third, turn}, heroCards,
                strengthMap, wasVillainPreviousAggressive,
                isHeroOnButton, PokerStreet.TURN, bigBlind);
    }

    public Map<Action, Double> onFirstActionRiver(int heroActions, int heroAggressiveActions,
            int villainActions, int villainAggressiveActions,
            double pot, Spectrum villainSpectrum,
            Flop flop, Card turn, Card river, HoleCards heroCards,
            Map<HoleCards, StreetEquity> strengthMap,
            boolean wasVillainPreviousAggressive, boolean isHeroOnButton, double bigBlind) {
        return onFirstAction(heroActions, heroAggressiveActions, villainActions,
                villainAggressiveActions, pot, villainSpectrum,
                new Card[]{flop.first, flop.second, flop.third, turn, river},
                heroCards, strengthMap, wasVillainPreviousAggressive, isHeroOnButton,
                PokerStreet.RIVER, bigBlind);
    }

    private Map<Action, Double> onFirstAction(int heroActions, int heroAggressiveActions,
            int villainActions, int villainAggressiveActions,
            double pot, Spectrum villainSpectrum,
            Card[] board, HoleCards heroCards,
            Map<HoleCards, StreetEquity> villainStrengthMap,
            boolean wasVillainPreviousAggressive,
            boolean isHeroOnButton, PokerStreet street, double bigBlind) {
//        Log.d("Villain spectrum: \n" + villainSpectrum.toString());
        Map<Action, Double> result = new HashMap<Action, Double>();

        //if hero folds
        double foldProfit = 0;
        result.put(Action.foldAction(), foldProfit);
        //if hero checks
        int countOfRaises = 0;
        double checkProfit = calculateHeroActionProfit(0, heroActions,
                heroAggressiveActions, villainActions, villainAggressiveActions,
                pot, 0, 0,
                villainSpectrum, board, heroCards, wasVillainPreviousAggressive,
                false, isHeroOnButton, street, villainStrengthMap, countOfRaises, bigBlind);
        result.put(Action.checkAction(), checkProfit);

        //if hero bets
        double betAmount;
        if (street == PokerStreet.PREFLOP || street == PokerStreet.FLOP) {
            betAmount = bigBlind;
        } else {
            betAmount = 2 * bigBlind;
        }
        Action heroBetAction = Action.createRaiseAction(betAmount, -1);
        countOfRaises = 1;
        double aggressiveProfit = calculateHeroActionProfit(betAmount, heroActions,
                heroAggressiveActions, villainActions, villainAggressiveActions,
                pot, 0, 0, villainSpectrum, board, heroCards,
                wasVillainPreviousAggressive, true, isHeroOnButton, street,
                villainStrengthMap, countOfRaises, bigBlind);

        result.put(heroBetAction, aggressiveProfit);
        return result;
    }

    private double calculateHeroActionProfit(double heroActionAmount,
            int heroActions, int heroAggressiveActions,
            int villainActions, int villainAggressiveActions,
            double pot, double heroToCall,
            double heroInvestment, Spectrum villainSpectrum,
            Card[] board, HoleCards heroCards, boolean wasVillainPreviousAggressive,
            boolean wasHeroPreviousAggressive, boolean isHeroOnButton,
            PokerStreet street, Map<HoleCards, StreetEquity> villainStrengthMap,
            int countOfRaises, double bigBlind) {

        double villainToCall = heroActionAmount;
        double villainPot = pot + heroToCall + villainToCall;
        LocalSituation villainSituation = getVillainSituationWithoutStrength(
                street, !isHeroOnButton, villainToCall, villainPot,
                heroActions + 1, heroAggressiveActions + (wasHeroPreviousAggressive ? 1 : 0),
                villainActions, villainAggressiveActions, wasVillainPreviousAggressive,
                wasHeroPreviousAggressive, countOfRaises, bigBlind);
//        Log.d("Villain situation: " + villainSituation.toString());
        Spectrum foldSpectrum = new Spectrum();
        Spectrum passiveSpectrum = new Spectrum();
        Spectrum aggressiveSpectrum = new Spectrum();
        //
        processVillainSpectrums(villainSpectrum, foldSpectrum, passiveSpectrum,
                aggressiveSpectrum, villainSituation, villainStrengthMap);
        //
        double villainSpectrumWeight = villainSpectrum.summaryWeight();
        double foldEquity = foldSpectrum.summaryWeight();
        double passiveEquity = passiveSpectrum.summaryWeight();
        double aggressiveEquity = aggressiveSpectrum.summaryWeight();

        if (Math.abs(foldEquity + passiveEquity + aggressiveEquity - villainSpectrumWeight) > NEARLY_ZERO) {
            throw new RuntimeException("Equities must be equals in summary. "
                    + "Fold equity: " + foldEquity + " call equity: " + passiveEquity
                    + " raise equity: " + aggressiveEquity + ". Summary: " + villainSpectrumWeight);
        }
        //if villain folds
        double ifVillainFoldProfit = (pot - heroInvestment);

        //if villain calls
        double strength = PokerEquilatorBrecher.strengthVsSpectrum(heroCards,
                board, passiveSpectrum);
        double ifVillainPassiveProfit = strength * (pot - heroInvestment + villainToCall)
                - (1 - strength) * (heroInvestment + heroToCall + villainToCall);
        if (street == PokerStreet.PREFLOP && ifVillainPassiveProfit > 0) {
            int add = isHeroOnButton ? 1 : -1;
            ifVillainPassiveProfit += add * IP_ADDITIONAL_PROFIT_BB * bigBlind;
        } else if (street == PokerStreet.FLOP || street == PokerStreet.TURN) {
            StreetEquity equity = PokerEquilatorBrecher.equityVsSpectrum(heroCards,
                    board, passiveSpectrum);
            double realEquity = StreetEquity.calculateRealEquity(equity);
            double nextStreetPot;
            if (street == PokerStreet.FLOP) {
                nextStreetPot = TURN_POT_COEFF * bigBlind;
            } else {
                nextStreetPot = RIVER_POT_COEFF * bigBlind;
            }
            double additionalVillainPassiveProfit = realEquity * nextStreetPot - nextStreetPot / 2;
            if (additionalVillainPassiveProfit > MIN_ADDITIONAL_PROFIT && heroActionAmount != 0) {
                ifVillainPassiveProfit += additionalVillainPassiveProfit;
            }
        }
        if (passiveSpectrum.isEmpty()) {
            ifVillainPassiveProfit = 0;
        }
        double ifVillainAggressiveProfit = 0;
        //if villain can raise
        if (countOfRaises < 4) {
            double villainReraiseAmount;
            if (street == PokerStreet.PREFLOP || street == PokerStreet.FLOP) {
                villainReraiseAmount = bigBlind;
            } else {
                villainReraiseAmount = 2 * bigBlind;
            }
            double potAfterVillainAggressive = pot + heroToCall + 2 * villainToCall + villainReraiseAmount;

            if (aggressiveEquity == 0) {
                ifVillainAggressiveProfit = 0;
            } else {
                Map<Action, Double> map = onSecondActionRecursive(heroActions + 1,
                        heroAggressiveActions + (wasHeroPreviousAggressive ? 1 : 0),
                        villainActions + 1, villainAggressiveActions + 1,
                        potAfterVillainAggressive,
                        villainReraiseAmount, heroInvestment + heroToCall + villainToCall,
                        aggressiveSpectrum, board, heroCards, villainStrengthMap,
                        isHeroOnButton, street, countOfRaises + 1, bigBlind);
                ifVillainAggressiveProfit = CollectionUtils.maxValue(map);
            }
        }
        double heroActionProfit = (foldEquity / villainSpectrumWeight) * ifVillainFoldProfit
                + (passiveEquity / villainSpectrumWeight) * ifVillainPassiveProfit
                + (aggressiveEquity / villainSpectrumWeight) * ifVillainAggressiveProfit;
        if (heroInvestment == 0) {
            Log.d("If Hero " + (heroActionAmount == 0 ? "checks" : ("raises " + heroActionAmount)) + " |");
            Log.d("Villain will fold in " + foldEquity / villainSpectrumWeight + " percents");
            Log.d("Villain will call in " + passiveEquity / villainSpectrumWeight + " percents");
            Log.d("Villain will reraise in " + aggressiveEquity / villainSpectrumWeight + " percents");
        }
        return heroActionProfit;
    }

    private void processVillainSpectrums(Spectrum villainSpectrum,
            Spectrum foldSpectrum, Spectrum passiveSpectrum,
            Spectrum aggressiveSpectrum, LocalSituation villainSituation,
            Map<HoleCards, StreetEquity> strengthMap) {
        for (HoleCards villainCards : villainSpectrum) {
            StreetEquity equity = strengthMap.get(villainCards);
            villainSituation.setStrength(equity.strength);
            if (villainSituation.getStreet() == LocalSituation.FLOP
                    || villainSituation.getStreet() == LocalSituation.TURN) {
                villainSituation.setPositivePotential(equity.positivePotential);
                villainSituation.setNegativePotential(equity.negativePotential);
            }
            Advice advice = villainModeller.getAdvice(villainSituation, villainCards);
            double oldWeight = villainSpectrum.getWeight(villainCards);
            foldSpectrum.add(villainCards, oldWeight * advice.getFold());
            passiveSpectrum.add(villainCards, oldWeight * advice.getPassive());
            aggressiveSpectrum.add(villainCards, oldWeight * advice.getAggressive());
        }
    }

    private LocalSituation getVillainSituationWithoutStrength(PokerStreet street,
            boolean villainOnButton, double toCall, double pot,
            int heroActions, int heroAggressiveActions,
            int villainActions, int villainAggressiveActions,
            boolean wasVillainPreviousAggressive, boolean wasHeroPreviousAggressive,
            int countOfRaises, double bigBlind) {
        LocalSituation result = null;
        //potOdds
        double potOdds = toCall / (toCall + pot);

        if (street == PokerStreet.PREFLOP) {
            result = new LocalSituation(LocalSituation.PREFLOP, LimitType.FIXED_LIMIT);
        } else if (street == PokerStreet.FLOP) {
            //equities
            result = new LocalSituation(LocalSituation.FLOP, LimitType.FIXED_LIMIT);
            result.setPositivePotential(LocalSituation.DEFAULT_POTENTIAL);
            result.setNegativePotential(LocalSituation.DEFAULT_POTENTIAL);
        } else if (street == PokerStreet.TURN) {
            //equities
            result = new LocalSituation(LocalSituation.TURN, LimitType.FIXED_LIMIT);
            result.setPositivePotential(LocalSituation.DEFAULT_POTENTIAL);
            result.setNegativePotential(LocalSituation.DEFAULT_POTENTIAL);
        } else if (street == PokerStreet.RIVER) {
            //equities
            result = new LocalSituation(LocalSituation.RIVER, LimitType.FIXED_LIMIT);
        }
        result.setLocalOpponentAggresion(heroActions != 0 ? (double) heroAggressiveActions / heroActions : 0);
        result.setLocalAggresion(villainActions != 0 ? (double) villainAggressiveActions / villainActions : 0);
        result.wasOpponentPreviousAggresive(wasHeroPreviousAggressive);
        result.wasHeroPreviousAggresive(wasVillainPreviousAggressive);
        result.setPotOdds(potOdds);
        result.isOnButton(villainOnButton);
        result.setFLPotSize(1 - 2 * bigBlind / pot);
        result.canRaise(countOfRaises < 4);
        return result;
    }

    public static void main(String[] args) {
        {
            NeuralNetwork nn = NeuralNetwork.load("bots/sparbot/preflop.mlp");
            LocalSituation result = new LocalSituation(LocalSituation.PREFLOP, LimitType.FIXED_LIMIT);
            result.setStrength(0.1);
            result.setLocalOpponentAggresion(0.5);
            result.setLocalAggresion(0.5);
            result.wasOpponentPreviousAggresive(true);
            result.wasHeroPreviousAggresive(true);
            result.setPotOdds(0.25);
            result.isOnButton(false);
            result.setFLPotSize(0.2);
            result.canRaise(true);

            Log.d("Vector dimension: " + result.getValues().size());

            nn.setInput(new LocalSituationInterpreter().createInput(result));
            int c = nn.getLayerAt(0).getNeuronsCount();
            Log.d("Count: " + c);
            c = nn.getLayerAt(1).getNeuronsCount();
            Log.d("Count: " + c);
            c = nn.getLayerAt(2).getNeuronsCount();
            Log.d("Count: " + c);
        }
//        NeuralNetwork nn = NeuralNetwork.load("bots/danielxn/preflop.mlp");
        NeuralNetwork nn = new DanielxnNeurals().getPreflopNN();
        
        LocalSituation result = new LocalSituation(LocalSituation.PREFLOP, LimitType.NO_LIMIT);
        result.setStrength(0.1);
        result.setLocalOpponentAggresion(0.5);
        result.setLocalAggresion(0.5);
        result.wasOpponentPreviousAggresive(true);
        result.wasHeroPreviousAggresive(true);
        result.setPotOdds(0.25);
        result.setPotToStackOdds(0.3);
        result.isOnButton(false);
        result.setFLPotSize(0.2);
        result.canRaise(true);

        Log.d("Vector dimension: " + result.getValues().size());

        nn.setInput(new LocalSituationInterpreter().createInput(result));
        int c = nn.getLayerAt(0).getNeuronsCount();
        Log.d("Count: " + c);
        c = nn.getLayerAt(1).getNeuronsCount();
        Log.d("Count: " + c);
        c = nn.getLayerAt(2).getNeuronsCount();
        Log.d("Count: " + c);


        //        c = new DanielxnNeurals().getFlopNN().getLayerAt(0).getNeuronsCount();
//        Log.d("Count: " + c);
    }
}

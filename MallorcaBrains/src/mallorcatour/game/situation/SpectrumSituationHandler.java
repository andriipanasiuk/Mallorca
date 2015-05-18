/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.game.situation;

import java.util.HashMap;
import java.util.Map;

import mallorcatour.core.bot.LimitType;
import mallorcatour.game.core.Card;
import mallorcatour.game.advice.Advice;
import mallorcatour.game.core.Action;
import mallorcatour.game.core.HoleCards;
import mallorcatour.game.core.PokerStreet;
import mallorcatour.game.core.Spectrum;
import mallorcatour.core.equilator13.EquilatorPreflop;
import mallorcatour.core.equilator13.PokerEquilatorBrecher;
import mallorcatour.core.equilator13.StreetEquity;
import mallorcatour.game.core.Card.Suit;
import mallorcatour.game.core.Flop;
import mallorcatour.grandtorino.mathbot.FLGameSolver;
import mallorcatour.grandtorino.mathbot.NLGameSolver;
import mallorcatour.grandtorino.nn.modeller.BaseVillainModeller;
import mallorcatour.interfaces.IPokerNN;
import mallorcatour.util.CollectionUtils;
import mallorcatour.util.DoubleUtils;
import mallorcatour.util.Log;

/**
 *
 * @author Andrew
 */
public class SpectrumSituationHandler extends SituationHandler {

    private final NLGameSolver NLGameSolver;
    private final FLGameSolver FLGameSolver;
    protected Spectrum villainSpectrum;
    private final IPokerNN villainModellingNN;
    private final boolean modelPreflop, modelPostflop;
    private Map<HoleCards, StreetEquity> preflopStrengthMap;
    private Map<HoleCards, StreetEquity> flopStrengthMap;
    private Map<HoleCards, StreetEquity> turnStrengthMap;
    private Map<HoleCards, StreetEquity> riverStrengthMap;
    private int raisesOnStreet = 0;
    private final ISpectrumListener villainSpectrumListener;
    private final IDecisionListener villainDecisionListener;
    private Spectrum randomVillainSpectrum;
    private final String DEBUG_PATH;

    public SpectrumSituationHandler(BaseVillainModeller villainModeller,
            LimitType limitType, boolean modelPreflop, boolean modelPostflop,
            ISpectrumListener villainSpectrumListener,
            IDecisionListener villainDecisionListener,
            String debug) {
        super(limitType);
        this.DEBUG_PATH = debug;
        this.modelPreflop = modelPreflop;
        this.modelPostflop = modelPostflop;
        this.villainModellingNN = villainModeller;
        NLGameSolver = new NLGameSolver(villainModeller);
        FLGameSolver = new FLGameSolver(villainModeller);
        this.villainSpectrumListener = villainSpectrumListener;
        this.villainDecisionListener = villainDecisionListener;
    }

    /**
     * An event called to tell us our hole cards 
     * @param c1 your first hole card
     * @param c2 your second hole card
     */
    @Override
    public void onHoleCards(Card c1, Card c2, String heroName, String villainName) {
        super.onHoleCards(c1, c2, heroName, villainName);
        villainSpectrum = Spectrum.random();
        randomVillainSpectrum = Spectrum.random();
        if (modelPreflop) {
            villainSpectrum.remove(holeCard1);
            villainSpectrum.remove(holeCard2);
            randomVillainSpectrum.remove(holeCard1);
            randomVillainSpectrum.remove(holeCard2);

            preflopStrengthMap = new HashMap<HoleCards, StreetEquity>();
            for (HoleCards holeCards : randomVillainSpectrum) {
				double strength = EquilatorPreflop.strengthByFormula(
						holeCards.first, holeCards.second);
                StreetEquity eq = new StreetEquity();
                eq.strength = strength;
                preflopStrengthMap.put(holeCards, eq);
            }
        }
    }

    private LocalSituation getVillainSituationWithoutStrength(double villainToCall) {
        LocalSituation result = null;
        boolean isOnButton;
        if (villainName.equals(gameInfo.getButtonName())) {
            isOnButton = true;
        } else {
            isOnButton = false;
        }
        //potOdds
        double toCall = villainToCall;
        double pot = gameInfo.getPotSize();
        double bankrollAtRisk = gameInfo.getBankRollAtRisk();
        double potOdds = toCall / (toCall + pot);

        if (gameInfo.isPreFlop()) {
            result = new LocalSituation(LocalSituation.PREFLOP, limitType);
        } else if (gameInfo.isFlop()) {
            //equities
            result = new LocalSituation(LocalSituation.FLOP, limitType);
            result.setPositivePotential(LocalSituation.DEFAULT_POTENTIAL);
            result.setNegativePotential(LocalSituation.DEFAULT_POTENTIAL);
        } else if (gameInfo.isTurn()) {
            //equities
            result = new LocalSituation(LocalSituation.TURN, limitType);
            result.setPositivePotential(LocalSituation.DEFAULT_POTENTIAL);
            result.setNegativePotential(LocalSituation.DEFAULT_POTENTIAL);
        } else if (gameInfo.isRiver()) {
            //equities
            result = new LocalSituation(LocalSituation.RIVER, limitType);
        }
        // TODO deal with aggression count
        result.setLocalOpponentAggresion(heroActionCount != 0 ? (double) countOfHeroAggressive / heroActionCount : 0);
        result.setLocalAggresion(villainActionCount != 0 ? (double) countOfOppAggressive / villainActionCount : 0);
        result.wasOpponentPreviousAggresive(wasHeroPreviousAggressive);
        result.wasHeroPreviousAggresive(wasVillainPreviousAggressive);
        result.setPotOdds(potOdds);
        result.isOnButton(isOnButton);
        result.setPotToStackOdds((pot + toCall) / (pot + toCall + bankrollAtRisk));
        result.setFLPotSize(1 - (2 * gameInfo.getBigBlindSize()) / pot);
        if (limitType == LimitType.NO_LIMIT) {
            result.canRaise(bankrollAtRisk > 0);
        } else {
            result.canRaise(gameInfo.canHeroRaise());
        }
        return result;
    }

    public Spectrum getVillainSpectrum() {
        return villainSpectrum;
    }

    /**
     * A new betting round has started.
     */
    @Override
    public void onStageEvent(PokerStreet street) {
        super.onStageEvent(street);
        if (street == PokerStreet.PREFLOP) {
            raisesOnStreet = 1;
        } else {
            raisesOnStreet = 0;
        }
        if (modelPostflop) {
            if (street == PokerStreet.FLOP) {
                villainSpectrum.remove(flop1);
                villainSpectrum.remove(flop2);
                villainSpectrum.remove(flop3);
                randomVillainSpectrum.remove(flop1);
                randomVillainSpectrum.remove(flop2);
                randomVillainSpectrum.remove(flop3);
            } else if (street == PokerStreet.TURN) {
                villainSpectrum.remove(turn);
                randomVillainSpectrum.remove(turn);
            } else if (street == PokerStreet.RIVER) {
                villainSpectrum.remove(river);
                randomVillainSpectrum.remove(river);
            }
            calculateStrengthMap(street);
        }
    }

    private void logVillainSituation(LocalSituation situation, Action villainAction) {
        Log.f(DEBUG_PATH, "Villain situation: " + situation.toString());
    }

    private Map<HoleCards, Advice> calculateVillainAdvices(
            LocalSituation villainSituation,
            Action villainAction) {
        Map<HoleCards, Advice> villainAdvices = new HashMap<HoleCards, Advice>();
        Log.f(DEBUG_PATH, "---------------------");
        logVillainSituation(villainSituation, villainAction);
        for (HoleCards villainCards : villainSpectrum) {
            double strength = 0;
            if (gameInfo.isPreFlop()) {
				strength = EquilatorPreflop.strengthByFormula(villainCards.first, villainCards.second);
            } else if (gameInfo.isFlop()) {
                if (flop1 == null || flop2 == null || flop3 == null) {
                    throw new NullPointerException();
                }
                StreetEquity eq = flopStrengthMap.get(villainCards);
                strength = eq.strength;
                villainSituation.setPositivePotential(eq.positivePotential);
                villainSituation.setNegativePotential(eq.negativePotential);
            } else if (gameInfo.isTurn()) {
                if (turn == null) {
                    throw new NullPointerException();
                }
                StreetEquity eq = turnStrengthMap.get(villainCards);
                strength = eq.strength;
                villainSituation.setPositivePotential(eq.positivePotential);
                villainSituation.setNegativePotential(eq.negativePotential);
            } else if (gameInfo.isRiver()) {
                if (turn == null || river == null) {
                    throw new NullPointerException();
                }
                StreetEquity eq = riverStrengthMap.get(villainCards);
                strength = eq.strength;
            }
            villainSituation.setStrength(strength);
//            int cacheIndex = (int) (strength * 1000);
            Advice advice = null;
//            = cache.get(cacheIndex);
            if (villainCards.isRanked()) {
                int t = 0;
            }
            if (villainCards.first.equals(Card.valueOf("7d")) && villainCards.first.equals(Card.valueOf("7s"))) {
                int t = 0;
            }
            if (villainCards.first.equals(Card.valueOf("8d")) && villainCards.first.equals(Card.valueOf("8s"))) {
                int t = 0;
            }
            if (advice == null || villainSituation.getStreet() == LocalSituation.PREFLOP) {
                advice = villainModellingNN.getAdvice(villainSituation, villainCards);
//                cache.put(cacheIndex, advice);
            }
            villainAdvices.put(villainCards, advice);
        }
        return villainAdvices;
    }

    /**
     * An villain action has been observed.
     */
    @Override
    public void onVillainActed(Action action, double villainToCall) {
        LocalSituation villainSituation = getVillainSituationWithoutStrength(villainToCall);
        villainDecisionListener.onDecided(villainSituation, action);
        if ((modelPreflop && gameInfo.isPreFlop())
                || (modelPostflop && gameInfo.isPostFlop())) {
            modifyVillainSpectrum(villainSituation, action);
            logVillainSpectrum();
        }
        if (action.isAggressive()) {
            raisesOnStreet++;
        }
        super.onVillainActed(action, villainToCall);
    }

    @Override
    public void onHeroActed(Action action) {
        if (action.isAggressive()) {
            raisesOnStreet++;
        }
        super.onHeroActed(action);
    }

    private void calculateStrengthMap(PokerStreet street) {
        long start = System.currentTimeMillis();
        if (street == PokerStreet.FLOP) {
            flopStrengthMap = new HashMap<HoleCards, StreetEquity>();
            Map<Integer, StreetEquity> cache = new HashMap<Integer, StreetEquity>();
            for (HoleCards holeCards : randomVillainSpectrum) {
                double strength = PokerEquilatorBrecher.strengthOnFlop(
                        holeCards.first, holeCards.second, flop1, flop2, flop3);
                int hash = holeCards.hashCodeForValues();
                StreetEquity equity = cache.get(hash);
                if (equity == null) {
                    equity = PokerEquilatorBrecher.equityOnFlop(holeCards.first,
                            holeCards.second, flop1, flop2, flop3);
                    cache.put(hash, equity);
                }
                equity.strength = strength;
                flopStrengthMap.put(holeCards, equity);
            }
        } else if (street == PokerStreet.TURN) {
            turnStrengthMap = new HashMap<HoleCards, StreetEquity>();
            Map<Integer, StreetEquity> cache = new HashMap<Integer, StreetEquity>();
            for (HoleCards holeCards : randomVillainSpectrum) {
                double strength = PokerEquilatorBrecher.strengthOnTurn(
                        holeCards.first, holeCards.second, flop1, flop2, flop3, turn);
                int hash = holeCards.hashCodeForValues();
                StreetEquity eq = cache.get(hash);
                if (eq == null) {
                    eq = PokerEquilatorBrecher.equityOnTurn(holeCards.first,
                            holeCards.second, flop1, flop2, flop3, turn);
                    cache.put(hash, eq);
                }
                eq.strength = strength;
                turnStrengthMap.put(holeCards, eq);
            }
        } else if (street == PokerStreet.RIVER) {
            riverStrengthMap = new HashMap<HoleCards, StreetEquity>();
            for (HoleCards holeCards : randomVillainSpectrum) {
                double strength = PokerEquilatorBrecher.strengthOnRiver(
                        holeCards.first, holeCards.second, flop1, flop2, flop3, turn, river);
                StreetEquity eq = new StreetEquity();
                eq.strength = strength;
                riverStrengthMap.put(holeCards, eq);
            }
        }
        Log.f(DEBUG_PATH, "Strength map for " + street
                + " was calculated in " + (System.currentTimeMillis() - start) + " ms");
    }

    private void modifyVillainSpectrum(LocalSituation villainSituation,
            Action villainAction) {
        //for fixed limit we'll be calculating preflop also
        if (limitType == LimitType.FIXED_LIMIT || gameInfo.isPostFlop()) {
            Map<HoleCards, Advice> opponentAdvices = calculateVillainAdvices(
                    villainSituation, villainAction);
            for (HoleCards villainCards : opponentAdvices.keySet()) {
                if (villainCards.hashCodeForValues() == 104
                        && villainCards.first.getSuit() == Suit.SPADES
                        && villainCards.second.getSuit() == Suit.CLUBS) {
                    int i = 0;
                }
                Advice advice = opponentAdvices.get(villainCards);
                double percent;
                if (villainAction.isFold()) {
                    percent = advice.getFold();
                } else if (villainAction.isAggressive()) {
                    percent = advice.getAggressive();
                } else {
                    percent = advice.getPassive();
                }
                double oldPercent = villainSpectrum.getWeight(villainCards);
                if (percent == 0) {
                    villainSpectrum.remove(villainCards);
                } else {
                    villainSpectrum.add(villainCards, percent * oldPercent);
                }
            }
            Log.f(DEBUG_PATH, "---------------------");
        }
        if (villainSpectrum.isEmpty()) {
            String log = "\nVillain spectrum is unknown. Continue playing vs random\n";
            villainSpectrum = randomVillainSpectrum;
            villainSpectrumListener.onSpectrumChanged(villainSpectrum, log);
        }
    }

    private void logVillainSpectrum() {
        String log = null;
        if (gameInfo.isPreFlop()) {
            log = "Strength: "
                    + PokerEquilatorBrecher.strengthVsSpectrum(new HoleCards(holeCard1, holeCard2),
                    new Card[]{}, villainSpectrum);
        } else if (gameInfo.isFlop()) {
            StreetEquity equity = PokerEquilatorBrecher.equityVsSpectrum(new HoleCards(holeCard1, holeCard2),
                    new Card[]{flop1, flop2, flop3}, villainSpectrum);
            log = "Strength: " + DoubleUtils.digitsAfterComma(equity.strength, 3)
                    + " Positive: " + DoubleUtils.digitsAfterComma(equity.positivePotential, 3)
                    + " Negative: " + DoubleUtils.digitsAfterComma(equity.negativePotential, 3);
        } else if (gameInfo.isTurn()) {
            StreetEquity equity = PokerEquilatorBrecher.equityVsSpectrum(new HoleCards(holeCard1, holeCard2),
                    new Card[]{flop1, flop2, flop3, turn}, villainSpectrum);
            log = "Strength: " + DoubleUtils.digitsAfterComma(equity.strength, 3)
                    + " Positive: " + DoubleUtils.digitsAfterComma(equity.positivePotential, 3)
                    + " Negative: " + DoubleUtils.digitsAfterComma(equity.negativePotential, 3);
        } else if (gameInfo.isRiver()) {
            log = "Strength: "
                    + PokerEquilatorBrecher.strengthVsSpectrum(new HoleCards(holeCard1, holeCard2),
                    new Card[]{flop1, flop2, flop3, turn, river}, villainSpectrum);
        }
        villainSpectrumListener.onSpectrumChanged(villainSpectrum, log);
    }

    public Map<Action, Double> getProfitMap() {
        if (limitType == LimitType.NO_LIMIT) {
            Map<Action, Double> result = getNLProfitMap();
            return result;
        } else {
            Map<Action, Double> result = getFLProfitMap();
            return result;
        }
    }

    private Map<Action, Double> getNLProfitMap() {
        if (gameInfo.isPreFlop()) {
            if (gameInfo.getButtonName().equals(heroName)
                    && gameInfo.getHeroAmountToCall() == gameInfo.getBigBlindSize() / 2) {
                return NLGameSolver.onFirstActionPreFlop(heroActionCount,
                        countOfHeroAggressive, villainActionCount, countOfOppAggressive,
                        gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(),
                        villainSpectrum, new HoleCards(holeCard1, holeCard2),
                        preflopStrengthMap,/*was villain aggressive*/ false,
                        true, gameInfo.getBigBlindSize());
            } else {
                return NLGameSolver.onSecondActionPreflop(heroActionCount,
                        countOfHeroAggressive, villainActionCount, countOfOppAggressive,
                        gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(),
                        gameInfo.getHeroAmountToCall(), villainSpectrum,
                        new HoleCards(holeCard1, holeCard2), preflopStrengthMap,
                        gameInfo.getButtonName().equals(heroName), gameInfo.getBigBlindSize());
            }
        } else if (gameInfo.isFlop()) {
            if (gameInfo.getHeroAmountToCall() == 0
                    && !gameInfo.getButtonName().equals(heroName)) {
                Map<Action, Double> map = NLGameSolver.onFirstActionFlop(heroActionCount,
                        countOfHeroAggressive, villainActionCount, countOfOppAggressive,
                        gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(),
                        villainSpectrum, new Flop(flop1, flop2, flop3),
                        new HoleCards(holeCard1, holeCard2), flopStrengthMap,
                        wasVillainPreviousAggressive,
                        gameInfo.getButtonName().equals(heroName), gameInfo.getBigBlindSize());
                double profit = CollectionUtils.maxValue(map);
                Log.d("Hero's first action on flop " + map.toString());
                Log.d("\nHero profit: " + profit);
                return map;
            } else {
                Map<Action, Double> map = NLGameSolver.onSecondActionFlop(heroActionCount,
                        countOfHeroAggressive, villainActionCount, countOfOppAggressive,
                        gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(),
                        gameInfo.getHeroAmountToCall(),
                        villainSpectrum, new Flop(flop1, flop2, flop3),
                        new HoleCards(holeCard1, holeCard2), flopStrengthMap,
                        gameInfo.getButtonName().equals(heroName), gameInfo.getBigBlindSize());
                double profit = CollectionUtils.maxValue(map);
                Log.d("Flop. Second action. " + map.toString());
                Log.d("\nHero profit: " + profit);
                return map;
            }
        } else if (gameInfo.isTurn()) {
            if (gameInfo.getHeroAmountToCall() == 0
                    && !gameInfo.getButtonName().equals(heroName)) {
                Map<Action, Double> map = NLGameSolver.onFirstActionTurn(heroActionCount,
                        countOfHeroAggressive, villainActionCount, countOfOppAggressive,
                        gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(),
                        villainSpectrum, new Flop(flop1, flop2, flop3), turn,
                        new HoleCards(holeCard1, holeCard2), turnStrengthMap,
                        wasVillainPreviousAggressive,
                        gameInfo.getButtonName().equals(heroName), gameInfo.getBigBlindSize());
                double profit = CollectionUtils.maxValue(map);
                Log.d("Hero's first action on turn " + map.toString());
                Log.d("\nHero profit: " + profit);
                return map;
            } else {
                Map<Action, Double> map = NLGameSolver.onSecondActionTurn(heroActionCount,
                        countOfHeroAggressive, villainActionCount, countOfOppAggressive,
                        gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(),
                        gameInfo.getHeroAmountToCall(),
                        villainSpectrum, new Flop(flop1, flop2, flop3), turn,
                        new HoleCards(holeCard1, holeCard2), turnStrengthMap,
                        gameInfo.getButtonName().equals(heroName), gameInfo.getBigBlindSize());
                double profit = CollectionUtils.maxValue(map);
                Log.d("Turn. Second action. " + map.toString());
                Log.d("\nHero profit: " + profit);
                return map;
            }
        } else if (gameInfo.isRiver()) {
            if (gameInfo.getHeroAmountToCall() == 0
                    && !gameInfo.getButtonName().equals(heroName)) {
                Map<Action, Double> map = NLGameSolver.onFirstActionRiver(heroActionCount,
                        countOfHeroAggressive, villainActionCount, countOfOppAggressive,
                        gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(),
                        villainSpectrum, new Flop(flop1, flop2, flop3), turn, river,
                        new HoleCards(holeCard1, holeCard2), riverStrengthMap,
                        wasVillainPreviousAggressive,
                        gameInfo.getButtonName().equals(heroName), gameInfo.getBigBlindSize());
                double profit = CollectionUtils.maxValue(map);
                Log.f(DEBUG_PATH, "Hero's first action on river: " + map.toString());
                Log.d("\nHero profit: " + profit);
                return map;
            } else {
                Map<Action, Double> map = NLGameSolver.onSecondActionRiver(heroActionCount,
                        countOfHeroAggressive, villainActionCount, countOfOppAggressive,
                        gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(),
                        gameInfo.getHeroAmountToCall(),
                        villainSpectrum, new Flop(flop1, flop2, flop3), turn, river,
                        new HoleCards(holeCard1, holeCard2), riverStrengthMap,
                        gameInfo.getButtonName().equals(heroName), gameInfo.getBigBlindSize());
                double profit = CollectionUtils.maxValue(map);
                Log.d("River. Second action: " + map.toString());
                Log.f(DEBUG_PATH, "River. Second action: " + map.toString());
                Log.d("\nHero profit: " + profit);
                return map;
            }
        } else {
            throw new RuntimeException();
        }
    }

    private Map<Action, Double> getFLProfitMap() {
        Log.d("getFLProfitMap. Pot: " + gameInfo.getPotSize());
        if (gameInfo.isPreFlop()) {
            if (gameInfo.getButtonName().equals(heroName)
                    && gameInfo.getHeroAmountToCall() == gameInfo.getBigBlindSize() / 2) {
                Map<Action, Double> map = FLGameSolver.onFirstActionPreFlop(heroActionCount,
                        countOfHeroAggressive, villainActionCount, countOfOppAggressive,
                        gameInfo.getPotSize(), villainSpectrum,
                        new HoleCards(holeCard1, holeCard2),
                        preflopStrengthMap, /*was villain aggressive*/ false,
                        /*is Hero on button*/ true, gameInfo.getBigBlindSize());
                double profit = CollectionUtils.maxValue(map);
                Log.d("Hero's second action on preflop " + map.toString());
                Log.d("\nHero profit: " + profit);
                return map;
            } else {
                Map<Action, Double> map = FLGameSolver.onSecondActionPreflop(heroActionCount,
                        countOfHeroAggressive, villainActionCount, countOfOppAggressive,
                        gameInfo.getPotSize(), gameInfo.getHeroAmountToCall(),
                        villainSpectrum, new HoleCards(holeCard1, holeCard2),
                        preflopStrengthMap, gameInfo.getButtonName().equals(heroName),
                        raisesOnStreet, gameInfo.getBigBlindSize());
                double profit = CollectionUtils.maxValue(map);
                Log.d("Hero's second action on preflop " + map.toString());
                Log.d("\nHero profit: " + profit);
                return map;
            }
        } else if (gameInfo.isFlop()) {
            if (gameInfo.getHeroAmountToCall() == 0
                    && !gameInfo.getButtonName().equals(heroName)) {
                Map<Action, Double> map = FLGameSolver.onFirstActionFlop(heroActionCount,
                        countOfHeroAggressive, villainActionCount, countOfOppAggressive,
                        gameInfo.getPotSize(), villainSpectrum,
                        new Flop(flop1, flop2, flop3), new HoleCards(holeCard1, holeCard2),
                        flopStrengthMap, wasVillainPreviousAggressive,
                        gameInfo.getButtonName().equals(heroName),
                        gameInfo.getBigBlindSize());
                double profit = CollectionUtils.maxValue(map);
                Log.d("Hero's first action on flop " + map.toString());
                Log.d("\nHero profit: " + profit);
                return map;
            } else {
                Map<Action, Double> map = FLGameSolver.onSecondActionFlop(heroActionCount,
                        countOfHeroAggressive, villainActionCount, countOfOppAggressive,
                        gameInfo.getPotSize(), gameInfo.getHeroAmountToCall(),
                        villainSpectrum, new Flop(flop1, flop2, flop3),
                        new HoleCards(holeCard1, holeCard2), flopStrengthMap,
                        gameInfo.getButtonName().equals(heroName),
                        raisesOnStreet, gameInfo.getBigBlindSize());
                double profit = CollectionUtils.maxValue(map);
                Log.d("Flop. Second action. " + map.toString());
                Log.d("\nHero profit: " + profit);
                return map;
            }
        } else if (gameInfo.isTurn()) {
            if (gameInfo.getHeroAmountToCall() == 0
                    && !gameInfo.getButtonName().equals(heroName)) {
                Map<Action, Double> map = FLGameSolver.onFirstActionTurn(heroActionCount,
                        countOfHeroAggressive, villainActionCount, countOfOppAggressive,
                        gameInfo.getPotSize(), villainSpectrum,
                        new Flop(flop1, flop2, flop3), turn,
                        new HoleCards(holeCard1, holeCard2), turnStrengthMap,
                        wasVillainPreviousAggressive,
                        gameInfo.getButtonName().equals(heroName), gameInfo.getBigBlindSize());
                double profit = CollectionUtils.maxValue(map);
                Log.d("Hero's first action on turn " + map.toString());
                Log.d("\nHero profit: " + profit);
                return map;
            } else {
                Map<Action, Double> map = FLGameSolver.onSecondActionTurn(heroActionCount,
                        countOfHeroAggressive, villainActionCount, countOfOppAggressive,
                        gameInfo.getPotSize(), gameInfo.getHeroAmountToCall(),
                        villainSpectrum, new Flop(flop1, flop2, flop3), turn,
                        new HoleCards(holeCard1, holeCard2), turnStrengthMap,
                        gameInfo.getButtonName().equals(heroName),
                        raisesOnStreet, gameInfo.getBigBlindSize());
                double profit = CollectionUtils.maxValue(map);
                Log.d("Turn. Second action. " + map.toString());
                Log.d("\nHero profit: " + profit);
                return map;
            }
        } else if (gameInfo.isRiver()) {
            if (gameInfo.getHeroAmountToCall() == 0
                    && !gameInfo.getButtonName().equals(heroName)) {
                Map<Action, Double> map = FLGameSolver.onFirstActionRiver(heroActionCount,
                        countOfHeroAggressive, villainActionCount, countOfOppAggressive,
                        gameInfo.getPotSize(), villainSpectrum,
                        new Flop(flop1, flop2, flop3), turn, river,
                        new HoleCards(holeCard1, holeCard2), riverStrengthMap,
                        wasVillainPreviousAggressive, gameInfo.getButtonName().equals(heroName),
                        gameInfo.getBigBlindSize());
                double profit = CollectionUtils.maxValue(map);
                Log.f(DEBUG_PATH, "Hero's first action on river: " + map.toString());
                Log.d("Hero's first action on river: " + map.toString());
                Log.d("\nHero profit: " + profit);
                return map;
            } else {
                Map<Action, Double> map = FLGameSolver.onSecondActionRiver(heroActionCount,
                        countOfHeroAggressive, villainActionCount, countOfOppAggressive,
                        gameInfo.getPotSize(), gameInfo.getHeroAmountToCall(),
                        villainSpectrum, new Flop(flop1, flop2, flop3), turn, river,
                        new HoleCards(holeCard1, holeCard2), riverStrengthMap,
                        gameInfo.getButtonName().equals(heroName), raisesOnStreet, gameInfo.getBigBlindSize());
                double profit = CollectionUtils.maxValue(map);
                Log.d("River. Second action: " + map.toString());
                Log.f(DEBUG_PATH, "River. Second action: " + map.toString());
                Log.d("\nHero profit: " + profit);
                return map;
            }
        } else {
            //no reachable
            throw new RuntimeException();
        }
    }
}

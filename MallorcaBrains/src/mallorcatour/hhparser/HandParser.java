/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.hhparser;

import java.util.ArrayList;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.BaseGameInfo;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.Flop;
import mallorcatour.core.game.Hand;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.PlayerAction;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.situation.ISituationHandler;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.hhparser.core.HandManager;

import java.util.List;

import mallorcatour.neural.core.PokerLearningExample;

/**
 *
 * @author Andrew
 */
public class HandParser {

    private BaseGameInfo gameInfo;
    private double effectiveStack;
    private double pot;
    private double amountToCall;
    private ISituationHandler handler;
    private Card holeCard1, holeCard2;
    private String heroName, villainName;
    private double bankrollAtRisk;
    private List<Card> board;
    private List<LocalSituation> situations;
    private Action heroPreviousAction;
    private List<PokerLearningExample> learningExamples;

    private void doParsing(Hand hand) {
        newHand(hand);
        handler.onStageEvent(PokerStreet.PREFLOP);
        List<PlayerAction> preflopActions = hand.getPreflopActions();
        for (PlayerAction action : preflopActions) {
            processAction(action);
        }
        if (hand.getFlop() != null) {
            gameInfo.street = PokerStreet.FLOP;
            Flop flop = hand.getFlop();
            board.add(flop.first);
            board.add(flop.second);
            board.add(flop.third);
            handler.onStageEvent(PokerStreet.FLOP);
        }
        if (hand.hasFlopActions()) {
            List<PlayerAction> flopActions = hand.getFlopActions();
            for (PlayerAction action : flopActions) {
                processAction(action);
            }
        }
        if (hand.getTurn() != null) {
            gameInfo.street = PokerStreet.TURN;
            Card turn = hand.getTurn();
            board.add(turn);
            handler.onStageEvent(PokerStreet.TURN);
        }
        if (hand.hasTurnActions()) {
            List<PlayerAction> turnActions = hand.getTurnActions();
            for (PlayerAction action : turnActions) {
                processAction(action);
            }
        }
        if (hand.getRiver() != null) {
            gameInfo.street = PokerStreet.RIVER;
            Card river = hand.getRiver();
            board.add(river);
            handler.onStageEvent(PokerStreet.RIVER);
        }
        if (hand.hasRiverActions()) {
            List<PlayerAction> riverActions = hand.getRiverActions();
            for (PlayerAction action : riverActions) {
                processAction(action);
            }
        }
        handler.onHandEnded();
    }

    public List<LocalSituation> parse(Hand hand, String heroName,
            ISituationHandler handler) {
        heroPreviousAction = null;
        this.handler = handler;
        this.heroName = heroName;
        villainName = HandManager.getVillainName(hand, heroName);
        situations = new ArrayList<LocalSituation>();
        learningExamples = new ArrayList<PokerLearningExample>();
        doParsing(hand);
        return situations;
    }

    public List<PokerLearningExample> parseWithActions(Hand hand, String heroName,
            ISituationHandler handler) {
        heroPreviousAction = null;
        this.handler = handler;
        this.heroName = heroName;
        villainName = HandManager.getVillainName(hand, heroName);
        situations = new ArrayList<LocalSituation>();
        learningExamples = new ArrayList<PokerLearningExample>();
        doParsing(hand);
        return learningExamples;
    }

    private void processAction(PlayerAction action) {
        if (action.getName().equals(heroName)) {
            onHeroAction(action.getAction());
        } else {
            double toCall = 0;
            if (heroPreviousAction != null) {
                if (heroPreviousAction.isAggressive()) {
                    toCall = heroPreviousAction.getAmount();
                }
            } else {
                toCall = gameInfo.pot / 1.5;
            }
            onVillainAction(action.getAction(), toCall);
        }
        if (action.getAction().isAggressive()) {
            gameInfo.raisesOnStreet[gameInfo.street.intValue()]++;
        }
    }

    private void newHand(Hand hand) {
        gameInfo = new BaseGameInfo();
        gameInfo.players = hand.getPlayers();
        initPot(hand);
        gameInfo.buttonName = hand.getButtonPlayer();
        gameInfo.street = PokerStreet.PREFLOP;
        gameInfo.raisesOnStreet[gameInfo.street.intValue()] = 1;
        //recognizing hole cards
        HoleCards holeCards = hand.getPlayerInfo(heroName).getHoleCards();
        if (holeCards != null) {
            holeCard1 = holeCards.first;
            holeCard2 = holeCards.second;
        }
        //init board cards
        board = new ArrayList<Card>();
        gameInfo.board = board;
        //callbacks
        handler.onHandStarted(gameInfo);
        handler.onHoleCards(holeCard1, holeCard2, heroName, villainName);
    }

    private void onVillainAction(Action action, double toCall) {
        processPot(action);
        handler.onVillainActed(action, toCall);
    }

    private Advice createFromAction(Action action) {
        double[] values = new double[3];
        if (action.isFold()) {
            values[0] = 1;
        } else if (action.isPassive()) {
            values[1] = 1;
        } else if (action.isAggressive()) {
            values[2] = 1;
        } else {
            throw new RuntimeException();
        }
        return Advice.create(values);
    }

    private void onHeroAction(Action action) {
        LocalSituation situation = handler.onHeroSituation();
        situations.add(situation);
        learningExamples.add(new PokerLearningExample(situation, createFromAction(action)));
        processPot(action);
        heroPreviousAction = action;
        handler.onHeroActed(action);
    }

    private void processPot(Action action) {
        if (action.isAggressive()) {
            pot += amountToCall;
            double effectiveStackBeforeRaise = effectiveStack - pot / 2;
            double amount = action.getAmount();
            if (effectiveStackBeforeRaise < amount) {
                bankrollAtRisk = 0;
                pot += effectiveStackBeforeRaise;
                amountToCall = effectiveStackBeforeRaise;
            } else {
                bankrollAtRisk -= amount;
                pot += amount;
                amountToCall = amount;
            }
        } else if (action.isPassive()) {
            pot += amountToCall;
            amountToCall = 0;
        } else {
            return;
        }
        gameInfo.pot = pot;
        gameInfo.heroAmountToCall = amountToCall;
        gameInfo.bankrollAtRisk = bankrollAtRisk;
    }

    private void initPot(Hand hand) {
        effectiveStack = hand.getEffectiveStack();
        gameInfo.bigBlind = hand.getBigBlind();
        gameInfo.limitType = hand.getLimitType();
        pot = hand.getBigBlind() + hand.getSmallBlind();
        bankrollAtRisk = effectiveStack - hand.getBigBlind();
        amountToCall = hand.getBigBlind() - hand.getSmallBlind();
        gameInfo.pot = pot;
        gameInfo.heroAmountToCall = amountToCall;
        gameInfo.bankrollAtRisk = bankrollAtRisk;
    }
}

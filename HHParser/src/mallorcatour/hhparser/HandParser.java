package mallorcatour.hhparser;

import java.util.ArrayList;
import java.util.List;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.Flop;
import mallorcatour.core.game.GameInfo;
import mallorcatour.core.game.Hand;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.PlayerAction;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.state.HandState;
import mallorcatour.core.game.state.observer.StrengthHandStateObserver;
import mallorcatour.hhparser.core.HandManager;
import mallorcatour.neural.core.PokerLearningExample;

public class HandParser {

    private GameInfo gameInfo;
    private double effectiveStack;
    private double pot;
    private double amountToCall;
    private Card holeCard1, holeCard2;
    private String heroName, villainName;
    private double bankrollAtRisk;
    private List<Card> board;
    private List<HandState> situations;
    private Action heroPreviousAction;
    private List<PokerLearningExample> learningExamples;

    private void doParsing(Hand hand, StrengthHandStateObserver stateObserver) {
        newHand(hand, stateObserver);
        stateObserver.onStageEvent(PokerStreet.PREFLOP);
        List<PlayerAction> preflopActions = hand.getPreflopActions();
        for (PlayerAction action : preflopActions) {
            processAction(action, stateObserver);
        }
        if (hand.getFlop() != null) {
            gameInfo.changeStreet(PokerStreet.FLOP);
            Flop flop = hand.getFlop();
            board.add(flop.first);
            board.add(flop.second);
            board.add(flop.third);
            stateObserver.onStageEvent(PokerStreet.FLOP);
        }
        if (hand.hasFlopActions()) {
            List<PlayerAction> flopActions = hand.getFlopActions();
            for (PlayerAction action : flopActions) {
                processAction(action, stateObserver);
            }
        }
        if (hand.getTurn() != null) {
            gameInfo.changeStreet(PokerStreet.TURN);
            Card turn = hand.getTurn();
            board.add(turn);
            stateObserver.onStageEvent(PokerStreet.TURN);
        }
        if (hand.hasTurnActions()) {
            List<PlayerAction> turnActions = hand.getTurnActions();
            for (PlayerAction action : turnActions) {
                processAction(action, stateObserver);
            }
        }
        if (hand.getRiver() != null) {
            gameInfo.changeStreet(PokerStreet.RIVER);
            Card river = hand.getRiver();
            board.add(river);
            stateObserver.onStageEvent(PokerStreet.RIVER);
        }
        if (hand.hasRiverActions()) {
            List<PlayerAction> riverActions = hand.getRiverActions();
            for (PlayerAction action : riverActions) {
                processAction(action, stateObserver);
            }
        }
        stateObserver.onHandEnded();
    }

	public List<HandState> parse(Hand hand, String heroName, StrengthHandStateObserver handler) {
        heroPreviousAction = null;
        this.heroName = heroName;
        villainName = HandManager.getVillainName(hand, heroName);
        situations = new ArrayList<HandState>();
        learningExamples = new ArrayList<PokerLearningExample>();
        doParsing(hand, handler);
        return situations;
    }

	public List<PokerLearningExample> parseWithActions(Hand hand, String heroName, StrengthHandStateObserver handler) {
        heroPreviousAction = null;
        this.heroName = heroName;
        villainName = HandManager.getVillainName(hand, heroName);
        situations = new ArrayList<HandState>();
        learningExamples = new ArrayList<PokerLearningExample>();
        doParsing(hand, handler);
        return learningExamples;
    }

    private void processAction(PlayerAction action, StrengthHandStateObserver stateObserver) {
        if (action.getName().equals(heroName)) {
            onHeroAction(action.getAction(), stateObserver);
        } else {
            double toCall = 0;
            if (heroPreviousAction != null) {
                if (heroPreviousAction.isAggressive()) {
                    toCall = heroPreviousAction.getAmount();
                }
            } else {
                toCall = gameInfo.pot / 1.5;
            }
            onVillainAction(action.getAction(), toCall, stateObserver);
        }
        if (action.getAction().isAggressive()) {
            gameInfo.raisesOnStreet[gameInfo.getStage().intValue()]++;
        }
    }

    private void newHand(Hand hand, StrengthHandStateObserver stateObserver) {
        gameInfo = new GameInfo();
        gameInfo.heroInfo = hand.getPlayerInfo(heroName);
        gameInfo.villainInfo = hand.getPlayerInfo(villainName);
        initPot(hand);
		if (heroName.equals(hand.getButtonPlayer())) {
			gameInfo.onButton = true;
		} else {
			gameInfo.onButton = false;
		}
        gameInfo.changeStreet(PokerStreet.PREFLOP);
        gameInfo.raisesOnStreet[gameInfo.getStage().intValue()] = 1;
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
        stateObserver.onHandStarted(gameInfo);
        stateObserver.onHoleCards(holeCard1, holeCard2);
    }

    private void onVillainAction(Action action, double toCall, StrengthHandStateObserver stateObserver) {
        processPot(action);
        stateObserver.onActed(action, toCall, villainName);
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

    private void onHeroAction(Action action, StrengthHandStateObserver stateObserver) {
        HandState situation = stateObserver.getSituation();
        situations.add(situation);
        learningExamples.add(new PokerLearningExample(situation, createFromAction(action)));
        processPot(action);
        heroPreviousAction = action;
        stateObserver.onActed(action, -1, heroName);
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

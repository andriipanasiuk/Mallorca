package mallorcatour.bot.neural;

import java.util.Map;
import java.util.Map.Entry;

import mallorcatour.bot.actionpreprocessor.FLActionPreprocessor;
import mallorcatour.bot.actionpreprocessor.NLActionPreprocessor;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.modeller.BaseSpectrumSituationHandler;
import mallorcatour.bot.modeller.BaseVillainModeller;
import mallorcatour.bot.modeller.FLSpectrumSituationHandler;
import mallorcatour.bot.modeller.NLSpectrumSituationHandler;
import mallorcatour.bot.preflop.FLPreflopChart;
import mallorcatour.bot.preflop.IPreflopChart;
import mallorcatour.bot.preflop.NLPreflopChart;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.interfaces.IActionPreprocessor;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.game.situation.IDecisionListener;
import mallorcatour.core.game.situation.IPokerNN;
import mallorcatour.core.game.situation.ISpectrumListener;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.core.vector.VectorUtils;
import mallorcatour.robot.humanadvisor.IHumanAdvisor;
import mallorcatour.util.Log;

/** 
 * This bot currently does not use global parameters.
 * 
 * @author Andrew
 */
public class GrandtorinoBot implements IPlayer {

    private final static double MIN_VALUE_FOR_CALL_DECISION = 10;
    private final static double MIN_VALUE_FOR_BET_DECISION = 10;
    @SuppressWarnings("unused")
	private String heroName, villainName;
    private Card heroCard1, heroCard2;
    private boolean isHumanAdvisor;
    private IGameInfo gameInfo;  // general game information
    private final BaseSpectrumSituationHandler situationHandler;
    private final IPokerNN pokerNN;
    private final IPreflopChart preflopBot;
    private final IHumanAdvisor humanAdvisor;
    private final IActionPreprocessor actionPreprocessor;
    private final LimitType limitType;
    private final String DEBUG_PATH;

    public GrandtorinoBot(IPokerNN neuralNetwork, BaseVillainModeller villainModeller,
            LimitType limitType, ISpectrumListener spectrumListener,
            IDecisionListener villainDecisionListener, IHumanAdvisor humanAdvisor,
            boolean isHumanAdvisor, boolean modelPreflop, boolean modelPostflop, String debug) {
        this.pokerNN = neuralNetwork;
        this.limitType = limitType;
        this.DEBUG_PATH = debug;
        if (limitType == LimitType.NO_LIMIT) {
            actionPreprocessor = new NLActionPreprocessor();
            preflopBot = new NLPreflopChart();
            situationHandler = new NLSpectrumSituationHandler(villainModeller,
            		modelPreflop, modelPostflop, spectrumListener,
            		villainDecisionListener, DEBUG_PATH);
        } else {
            actionPreprocessor = new FLActionPreprocessor();
            preflopBot = new FLPreflopChart();
            situationHandler = new FLSpectrumSituationHandler(villainModeller,
            		modelPreflop, modelPostflop, spectrumListener,
            		villainDecisionListener, DEBUG_PATH);
        }
        this.isHumanAdvisor = isHumanAdvisor;
        this.humanAdvisor = humanAdvisor;
    }

    /**
     * An event called to tell us our hole cards and seat number
     * @param c1 your first hole card
     * @param c2 your second hole card
     * @param seat your seat number at the table
     */
    public void onHoleCards(Card c1, Card c2, String heroName, String villainName) {
        situationHandler.onHoleCards(c1, c2, heroName, villainName);
        heroCard1 = c1;
        heroCard2 = c2;
        this.heroName = heroName;
        this.villainName = villainName;
    }

    /**
     * Requests an Action from the player
     * Called when it is the hero's turn to act.
     */
    public Action getAction() {
        LocalSituation situation = situationHandler.onHeroSituation();

        Log.f(DEBUG_PATH, "=========  Decision-making  =========");
        Log.f(DEBUG_PATH, "Situation: " + VectorUtils.toString(situation));

        Action action = null;
        //if villain is sitting out
        if (gameInfo.getBankRoll(villainName) == IGameInfo.SITTING_OUT) {
            Log.f(DEBUG_PATH, "Villain is sitting out");
            double percent = 0.5;
            action = Action.createRaiseAction(percent
                    * (gameInfo.getPotSize() + gameInfo.getHeroAmountToCall()), percent);
        } else //for human advisor
        if (isHumanAdvisor && gameInfo.isPreFlop()) {
            action = humanAdvisor.getHumanAction(gameInfo);
            Log.f(DEBUG_PATH, "Human action: " + action.toString());
        } else {
            //for preflop. Bot will make decision by preflop chart.
            if (gameInfo.isPreFlop()) {
                action = preflopBot.getAction(situation, new HoleCards(heroCard1, heroCard2));
                if (action != null) {
                    action = actionPreprocessor.preprocessAction(action, gameInfo, villainName);
                }
            }
            //if there is no preflop OR action is no in chart
            if (action == null) {
                Advice advice = pokerNN.getAdvice(situation, new HoleCards(heroCard1, heroCard2));
                Log.f(DEBUG_PATH, "Advice: " + advice.toString());
                action = advice.getAction();
                action = actionPreprocessor.preprocessAction(action, gameInfo, villainName);
                Log.f(DEBUG_PATH, "Action: " + action.toString());
                //check river action with MathBot
                if (limitType == LimitType.NO_LIMIT) {
                    action = checkRiverAction(action);
                }
            }
        }
        Log.f(DEBUG_PATH, "===============  End  ==============");
        situationHandler.onHeroActed(action);
        return action;
    }

    private Action checkRiverAction(Action action) {
        //check if we have positive EV for call on river
        if (gameInfo.isRiver() && gameInfo.getHeroAmountToCall() > 0) {
            Map<Action, Double> map = situationHandler.getProfitMap();
            if (action.isFold()) {
                for (Entry<Action, Double> entry : map.entrySet()) {
                    if (entry.getKey().isPassive()
                            && entry.getValue() >= MIN_VALUE_FOR_CALL_DECISION) {
                        return Action.callAction(gameInfo.getHeroAmountToCall());
                    }
                }
            } else if (action.isPassive()) {
                for (Entry<Action, Double> entry : map.entrySet()) {
                    if (entry.getKey().isPassive()
                            && entry.getValue() < MIN_VALUE_FOR_CALL_DECISION) {
                        return Action.foldAction();
                    }
                }
            }
        } //check if we have positive EV for bet
        else if (gameInfo.isRiver() && gameInfo.getHeroAmountToCall() == 0) {
            Map<Action, Double> map = situationHandler.getProfitMap();
            if (action.isAggressive()) {
                for (Entry<Action, Double> entry : map.entrySet()) {
                    if (entry.getKey().isAggressive()
                            && entry.getValue() < MIN_VALUE_FOR_BET_DECISION) {
                        return Action.checkAction();
                    }
                }
            }
        }
        return action;
    }

    /**
     * A new betting round has started.
     */
    public void onStageEvent(PokerStreet street) {
        situationHandler.onStageEvent(street);
    }

    /**
     * A new game has been started.
     * @param gameInfo the game start information
     */
    public void onHandStarted(IGameInfo gameInfo, long handNumber) {
        this.gameInfo = gameInfo;
        situationHandler.onHandStarted(gameInfo);
    }

    /**
     * An villain action has been observed.
     */
    public void onVillainActed(Action action, double toCall) {
        situationHandler.onVillainActed(action, toCall);
    }
}

package mallorcatour.grandtorino.mathbot;

import java.util.Map;
import mallorcatour.game.advice.Advice;
import mallorcatour.game.core.Card;
import mallorcatour.game.core.Action;
import mallorcatour.game.core.PokerStreet;
import mallorcatour.core.bot.IGameInfo;
import mallorcatour.core.bot.IPlayer;
import mallorcatour.core.bot.LimitType;
import mallorcatour.core.bot.NLActionPreprocessor;
import mallorcatour.game.core.HoleCards;
import mallorcatour.game.situation.IDecisionListener;
import mallorcatour.game.situation.ISpectrumListener;
import mallorcatour.game.situation.LocalSituation;
import mallorcatour.game.situation.SpectrumSituationHandler;
import mallorcatour.grandtorino.nn.modeller.BasePokerNN;
import mallorcatour.grandtorino.nn.modeller.BaseVillainModeller;
import mallorcatour.grandtorino.nn.danielxn.DanielxnNeurals;
import mallorcatour.interfaces.IActionPreprocessor;
import mallorcatour.interfaces.IPokerNN;
import mallorcatour.util.Log;

/** 
 * This bot currently does not use global parameters.
 * 
 * @author Andrew
 */
public class MixBot implements IPlayer {

    private IGameInfo gameInfo;  // general game information
    private String heroName, villainName;
    private Card heroCard1, heroCard2;
    private final IActionPreprocessor actionPreprocessor;
    private final BaseAdviceCreatorFromMap adviceCreator;
    private final String DEBUG_PATH;
    private final IPokerNN postflopNN;
    private final SpectrumSituationHandler situationHandler;

    public MixBot(IPokerNN postflopNN, BaseVillainModeller villainModeller,
            ISpectrumListener listener,
            IDecisionListener decisionListener, String debug) {
        this.postflopNN = postflopNN;
        adviceCreator = new AdviceCreatorFromMap();
        situationHandler = new SpectrumSituationHandler(villainModeller,
                LimitType.NO_LIMIT, true, false, listener, decisionListener, debug);
        actionPreprocessor = new NLActionPreprocessor();
        this.DEBUG_PATH = debug;
    }

    /**
     * An event called to tell us our hole cards and seat number
     * @param c1 your first hole card
     * @param c2 your second hole card
     * @param seat your seat number at the table
     */
    public void onHoleCards(Card c1, Card c2, String heroName, String villainName) {
        situationHandler.onHoleCards(c1, c2, heroName, villainName);
        this.heroCard1 = c1;
        this.heroCard2 = c2;
        this.heroName = heroName;
        this.villainName = villainName;
    }

    /**
     * Requests an Action from the player
     * Called when it is the Player's turn to act.
     */
    public Action getAction() {
        Advice advice;
        Action action = null;
        Log.f(DEBUG_PATH, "=========  Decision-making  =========");
        if (gameInfo.getBankRoll(villainName) == IGameInfo.SITTING_OUT) {
            Log.f(DEBUG_PATH, "Villain is sitting out");
            double percent = 0.5;
            action = Action.createRaiseAction(percent
                    * (gameInfo.getPotSize() + gameInfo.getHeroAmountToCall()), percent);
        } else if (gameInfo.isPostFlop()) {
            LocalSituation situation = situationHandler.onHeroSituation();
            advice = postflopNN.getAdvice(situation, new HoleCards(heroCard1, heroCard2));
            action = advice.getAction();
            Log.f(DEBUG_PATH, "Advice: " + advice.toString());
            action = actionPreprocessor.preprocessAction(action, gameInfo, villainName);
        } else {
            //preflop
            Map<Action, Double> map = situationHandler.getProfitMap();
            Log.f(DEBUG_PATH, "Map<Action, Profit>: " + map.toString());
            advice = adviceCreator.create(map);
            action = advice.getAction();
            Log.f(DEBUG_PATH, "Advice: " + advice.toString());
            action = actionPreprocessor.preprocessAction(action, gameInfo, villainName);
        }
        Log.f(DEBUG_PATH, "Action: " + action.toString());
        Log.f(DEBUG_PATH, "=========  End  =========");
        situationHandler.onHeroActed(action);
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
     * @param gi the game stat information
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

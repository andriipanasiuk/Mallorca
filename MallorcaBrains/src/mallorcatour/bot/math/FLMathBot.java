package mallorcatour.bot.math;

import java.util.Map;

import mallorcatour.bot.actionpreprocessor.FLActionPreprocessor;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.modeller.BaseSpectrumSituationHandler;
import mallorcatour.bot.modeller.BaseVillainModeller;
import mallorcatour.bot.modeller.FLSpectrumSituationHandler;
import mallorcatour.core.equilator.brecher.PokerEquilatorBrecher;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.interfaces.IActionPreprocessor;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.game.situation.IDecisionListener;
import mallorcatour.core.game.situation.ISpectrumListener;
import mallorcatour.util.Log;

/** 
 * This bot currently does not use global parameters.
 * 
 * @author Andrew
 */
public class FLMathBot implements IPlayer {

    private BaseAdviceCreatorFromMap adviceCreator;
    private IGameInfo gameInfo;  // general game information
    private BaseSpectrumSituationHandler situationHandler;
    @SuppressWarnings("unused")
	private String heroName, villainName;
    private Card heroCard1, heroCard2;
    private IActionPreprocessor actionPreprocessor;
    private final String DEBUG_PATH;

    public FLMathBot(BaseVillainModeller villainModeller,
            ISpectrumListener spectrumListener,
            IDecisionListener decisionListener, String debug) {
        adviceCreator = new AdviceCreatorFromMap();
        situationHandler = new FLSpectrumSituationHandler(villainModeller, 
        		true, true, spectrumListener, decisionListener, debug);
        actionPreprocessor = new FLActionPreprocessor();
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
        } else {
            Map<Action, Double> map = situationHandler.getProfitMap();
            Log.f(DEBUG_PATH, "Map<Action, Profit>: " + map.toString());
            saveSpectrum();
            advice = adviceCreator.create(map);
            action = advice.getAction();
            Log.f(DEBUG_PATH, "Advice: " + advice.toString());
            Log.f(DEBUG_PATH, "Action: " + action.toString());
            action = actionPreprocessor.preprocessAction(action, gameInfo, villainName);
            if (gameInfo.isRiver()) {
                action = checkRiver(action);
            }
        }
        Log.f(DEBUG_PATH, "=========  End  =========");
        situationHandler.onHeroActed(action);
        return action;
    }

    private void saveSpectrum() {
        //do nothing
    }

    private Action checkRiver(Action action) {
        if (action.isFold() && gameInfo.getHeroAmountToCall() > 0) {
            Card[] board = new Card[5];
            int i = 0;
            Log.d("FLMathBot Check river()");
            Log.d("Board: " + gameInfo.getBoard());
            for (Card c : gameInfo.getBoard()) {
                board[i++] = c;
            }
            double strength = PokerEquilatorBrecher.strengthVsSpectrum(
                    heroCard1, heroCard2, board, situationHandler.getVillainSpectrum());
            if (Math.abs(strength - gameInfo.getHeroAmountToCall() / gameInfo.getPotSize()) < 0.1) {
                Log.f(DEBUG_PATH, "Hero must fold but call by pot odds");
                return Action.callAction(gameInfo.getHeroAmountToCall());
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

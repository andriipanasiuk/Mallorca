package mallorcatour.bot.math;

import java.util.Map;

import mallorcatour.bot.actionpreprocessor.NLActionPreprocessor;
import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.modeller.VillainModel;
import mallorcatour.bot.modeller.SpectrumSituationHandler;
import mallorcatour.bot.preflop.IPreflopChart;
import mallorcatour.bot.preflop.NLPreflopChart;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.math.StrengthManager;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.brains.neural.gusxensen.GusXensen;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.interfaces.IActionPreprocessor;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.util.Log;

/** 
 * This bot currently does not use global parameters.
 * 
 * @author Andrew
 */
public class NLMathBot implements IPlayer {

    private BaseAdviceCreatorFromMap adviceCreator;
    private IGameInfo gameInfo;;
    private SpectrumSituationHandler situationHandler;
    private StrengthManager strengthManager;
    private IAdvisor preflopAdvisor;
    private IProfitCalculator profitCalculator;
    private IPreflopChart preflopBot;
    private Card heroCard1, heroCard2;
    private IActionPreprocessor actionPreprocessor;
    private final String DEBUG_PATH;

	public NLMathBot(VillainModel villainModeller, ISpectrumListener listener,
			IDecisionListener decisionListener, String debug) {
		adviceCreator = new AdviceCreatorFromMap();
		strengthManager = new StrengthManager(false);
		situationHandler = new SpectrumSituationHandler(villainModeller, LimitType.NO_LIMIT, true, true, listener,
				decisionListener, strengthManager, true, debug);
		GusXensen player = new GusXensen();
		preflopAdvisor = new NeuralAdvisor(player, player, "Gus Xensen");
        preflopBot = new NLPreflopChart();
        actionPreprocessor = new NLActionPreprocessor();
        this.DEBUG_PATH = debug;
    }

    /**
     * An event called to tell us our hole cards and seat number
     * @param c1 your first hole card
     * @param c2 your second hole card
     * @param seat your seat number at the table
     */
    public void onHoleCards(Card c1, Card c2, String villainName) {
        situationHandler.onHoleCards(c1, c2, villainName);
        this.heroCard1 = c1;
        this.heroCard2 = c2;
    }

    /**
     * Requests an Action from the player
     * Called when it is the Player's turn to act.
     */
    public Action getAction() {
    	LocalSituation situation = situationHandler.onHeroSituation();
        Advice advice;
        Action action = null;
        Log.f(DEBUG_PATH, "=========  Decision-making  =========");
        if (gameInfo.isVillainSitOut()) {
            Log.f(DEBUG_PATH, "Villain is sitting out");
            double percent = 0.5;
            action = Action.createRaiseAction(percent
                    * (gameInfo.getPotSize() + gameInfo.getHeroAmountToCall()), percent);
		} else if (gameInfo.isPostFlop()) {
			Map<Action, Double> map = profitCalculator.getProfitMap(gameInfo, situation, heroCard1,
					heroCard2, situationHandler.getVillainSpectrum(), strengthManager);
			Log.f(DEBUG_PATH, "Map<Action, Profit>: " + map.toString());
			advice = adviceCreator.create(map);
            action = advice.getAction();
            Log.f(DEBUG_PATH, "Advice: " + advice.toString());
            Log.f(DEBUG_PATH, "Action: " + action.toString());
            action = actionPreprocessor.preprocessAction(action, gameInfo);
        } else //preflop
        {
            Log.f(DEBUG_PATH, "Preflop: " + situation.toString());
            if (gameInfo.isPreFlop() && gameInfo.isVillainSitOut()) {
                action = preflopBot.getAction(situation, new HoleCards(heroCard1, heroCard2));
                if (action != null) {
                    action = actionPreprocessor.preprocessAction(action, gameInfo);
                }
            }
            if (action == null) {
                advice = preflopAdvisor.getAdvice(situation, new HoleCards(heroCard1, heroCard2));
                action = advice.getAction();
                Log.f(DEBUG_PATH, "Advice: " + advice.toString());
                action = actionPreprocessor.preprocessAction(action, gameInfo);
            }
        }
        Log.f(DEBUG_PATH, "=========  End  =========");
        situationHandler.onHeroActed(action);
        return action;
    }

    /**
     * A new betting round has started.
     */
    public void onStageEvent(PokerStreet street) {
    	strengthManager.onStageEvent(street);
        situationHandler.onStageEvent(street);
    }

    /**
     * A new game has been started.
     * @param gi the game stat information
     */
    @Override
    public void onHandStarted(IGameInfo gameInfo) {
        this.gameInfo = gameInfo;
        strengthManager.onHandStarted(gameInfo);
        situationHandler.onHandStarted(gameInfo);
    }

    /**
     * An villain action has been observed.
     */
    public void onVillainActed(Action action, double toCall) {
    	strengthManager.onVillainActed(action, toCall);
        situationHandler.onVillainActed(action, toCall);
    }

	@Override
	public void onHandEnded() {
		strengthManager.onHandEnded();
		situationHandler.onHandEnded();
	}
}

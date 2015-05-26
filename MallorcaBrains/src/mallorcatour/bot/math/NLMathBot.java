package mallorcatour.bot.math;

import java.util.Map;

import mallorcatour.bot.actionpreprocessor.NLActionPreprocessor;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumHolder;
import mallorcatour.bot.preflop.NLPreflopChart;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.brains.neural.gusxensen.GusXensen;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IActionPreprocessor;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.game.situation.ISituationHandler;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.util.Log;

/**
 * This bot currently does not use global parameters.
 * 
 * @author Andrew
 */
public class NLMathBot implements IPlayer {

	private BaseAdviceCreatorFromMap adviceCreator;
	private IPlayerGameInfo gameInfo;
	private final ISituationHandler situationHandler;
	private final ISpectrumHolder villainSpectrumHolder;
	private final IAdvisor preflopAdvisor;
	private final IProfitCalculator profitCalculator;
	private final IAdvisor preflopBot;
	private Card heroCard1, heroCard2;
	private final IActionPreprocessor actionPreprocessor;
	private final String DEBUG_PATH;

	public NLMathBot(IProfitCalculator profitCalculator, ISituationHandler situationHandler, ISpectrumHolder villainSpectrumHolder,
			String debug) {
		this.profitCalculator = profitCalculator;
		this.situationHandler = situationHandler;
		this.villainSpectrumHolder = villainSpectrumHolder;
		adviceCreator = new AdviceCreatorFromMap();
		GusXensen player = new GusXensen();
		preflopAdvisor = new NeuralAdvisor(player, player, "Gus Xensen");
		preflopBot = new NLPreflopChart();
		actionPreprocessor = new NLActionPreprocessor();
		this.DEBUG_PATH = debug;
	}

	/**
	 * An event called to tell us our hole cards and seat number
	 * 
	 * @param c1
	 *            your first hole card
	 * @param c2
	 *            your second hole card
	 * @param seat
	 *            your seat number at the table
	 */
	@Override
	public void onHoleCards(Card c1, Card c2, String villainName) {
		this.heroCard1 = c1;
		this.heroCard2 = c2;
		situationHandler.onHoleCards(c1, c2, villainName);
	}

	/**
	 * Requests an Action from the player Called when it is the Player's turn to
	 * act.
	 */
	@Override
	public Action getAction() {
		LocalSituation situation = situationHandler.onHeroSituation();
		IAdvice advice;
		Action action = null;
		Log.f(DEBUG_PATH, "=========  Decision-making  =========");
		if (gameInfo.isVillainSitOut()) {
			Log.f(DEBUG_PATH, "Villain is sitting out");
			double percent = 0.5;
			action = Action.createRaiseAction(percent * (gameInfo.getPotSize() + gameInfo.getHeroAmountToCall()),
					percent);
		} else if (gameInfo.isPostFlop()) {
			Map<Action, Double> map = profitCalculator.getProfitMap(gameInfo, situation, heroCard1, heroCard2,
					villainSpectrumHolder.getSpectrum());
			Log.f(DEBUG_PATH, "Map<Action, Profit>: " + map.toString());
			advice = adviceCreator.create(map);
			action = advice.getAction();
			Log.f(DEBUG_PATH, "Advice: " + advice.toString());
			Log.f(DEBUG_PATH, "Action: " + action.toString());
			action = actionPreprocessor.preprocessAction(action, gameInfo);
		} else {
			Log.f(DEBUG_PATH, "Preflop situation: " + situation.toString());
			advice = preflopBot.getAdvice(situation, new HoleCards(heroCard1, heroCard2), null);
			if (advice != null) {
				Log.f(DEBUG_PATH, "Advice from preflop chart");
			}
			if (advice == null) {
				advice = preflopAdvisor.getAdvice(situation, new HoleCards(heroCard1, heroCard2), null);
			}
			Log.f(DEBUG_PATH, "Advice: " + advice.toString());
			action = advice.getAction();
			action = actionPreprocessor.preprocessAction(action, gameInfo);
		}
		Log.f(DEBUG_PATH, "=========  End  =========");
		situationHandler.onHeroActed(action);
		return action;
	}

	/**
	 * A new betting round has started.
	 */
	@Override
	public void onStageEvent(PokerStreet street) {
		situationHandler.onStageEvent(street);
	}

	/**
	 * A new game has been started.
	 * 
	 * @param gi
	 *            the game stat information
	 */
	@Override
	public void onHandStarted(IPlayerGameInfo gameInfo) {
		this.gameInfo = gameInfo;
		situationHandler.onHandStarted(gameInfo);
	}

	/**
	 * An villain action has been observed.
	 */
	@Override
	public void onVillainActed(Action action, double toCall) {
		situationHandler.onVillainActed(action, toCall);
	}

	@Override
	public void onHandEnded() {
		situationHandler.onHandEnded();
	}

	@Override
	public String getName() {
		return "FLMathBot";
	}
}

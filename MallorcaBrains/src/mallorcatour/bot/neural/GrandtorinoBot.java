package mallorcatour.bot.neural;

import java.util.ArrayList;
import java.util.List;

import mallorcatour.bot.actionpreprocessor.NLActionPreprocessor;
import mallorcatour.bot.interfaces.IExternalAdvisor;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.preflop.NLPreflopChart;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.IAdvisor;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.IHoleCardsObserver;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IActionPreprocessor;
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.game.situation.ISituationHandler;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.core.vector.VectorUtils;
import mallorcatour.util.Log;

/**
 * This bot currently does not use global parameters.
 * 
 * @author Andrew
 */
public class GrandtorinoBot implements IPlayer {

	private IPlayerGameInfo gameInfo;
	private Card heroCard1, heroCard2;
	private IActionChecker actionChecker;
	private String name;
	private final IAdvisor advisor;
	private final IAdvisor preflopBot;
	private final IActionPreprocessor actionPreprocessor;
	private final String DEBUG_PATH;
	private final List<IExternalAdvisor> externalHelpers = new ArrayList<>();
	private ISituationHandler situationHandler;
	private IGameObserver gameObserver;
	private IHoleCardsObserver cardsObserver;

	public void set(IGameObserver gameObserver, ISituationHandler situationHandler, IHoleCardsObserver cardsObserver) {
		this.gameObserver = gameObserver;
		this.situationHandler = situationHandler;
		this.cardsObserver = cardsObserver;
	}

	public void set(IActionChecker actionChecker) {
		this.actionChecker = actionChecker;
	}

	public GrandtorinoBot(IAdvisor neuralNetwork, LimitType limitType, String debug) {
		this.advisor = neuralNetwork;
		this.DEBUG_PATH = debug;
		actionPreprocessor = new NLActionPreprocessor();
		preflopBot = new NLPreflopChart();
	}

	public void addExternalHelper(IExternalAdvisor advisor) {
		externalHelpers.add(advisor);
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
	public void onHoleCards(Card c1, Card c2) {
		cardsObserver.onHoleCards(c1, c2);
		heroCard1 = c1;
		heroCard2 = c2;
	}

	private Action getActionInternal(LocalSituation situation, HoleCards holeCards) {

		Action action = null;

		IAdvice advice = null;
		if (gameInfo.isPreFlop()) {
			advice = preflopBot.getAdvice(situation, holeCards, null);
			if (advice != null) {
				Log.f(DEBUG_PATH, "Advice from preflop bot: " + advice);
			}
		}
		if (advice == null) {
			advice = advisor.getAdvice(situation, holeCards, null);
			Log.f(DEBUG_PATH, "Advice: " + advice);
		}
		action = advice.getAction();
		action = actionPreprocessor.preprocessAction(action, gameInfo);
		Log.f(DEBUG_PATH, "Action: " + action);
		action = actionChecker.checkAction(action, situation, gameInfo, holeCards);
		return action;
	}

	/**
	 * Requests an Action from the player. Called when it is the hero's turn to
	 * act.
	 */
	@Override
	public Action getAction() {
		Action action = null;
		for (IExternalAdvisor advisor : externalHelpers) {
			action = advisor.getAction(gameInfo);
			if (action != null) {
				return action;
			}
		}
		LocalSituation situation = situationHandler.getSituation();

		Log.f(DEBUG_PATH, "=========  Decision-making  =========");
		Log.f(DEBUG_PATH, "Cards: [" + heroCard1 + " " + heroCard2 + "] " + gameInfo.getBoard());
		Log.f(DEBUG_PATH, "Situation: " + VectorUtils.toString(situation));
		action = getActionInternal(situation, new HoleCards(heroCard1, heroCard2));
		Log.f(DEBUG_PATH, "===============  End  ==============");

		gameObserver.onActed(action, gameInfo.getAmountToCall(), getName());
		return action;
	}

	/**
	 * A new betting round has started.
	 */
	public void onStageEvent(PokerStreet street) {
		gameObserver.onStageEvent(street);
	}

	/**
	 * A new game has been started.
	 * 
	 * @param gameInfo
	 *            the game start information
	 */
	@Override
	public void onHandStarted(IPlayerGameInfo gameInfo) {
		this.gameInfo = gameInfo;
		gameObserver.onHandStarted(gameInfo);
	}

	@Override
	public void onActed(Action action, double toCall, String name) {
		// callback about his action bot send himself
		if (!name.equals(getName())) {
			gameObserver.onActed(action, toCall, name);
		}
	}

	@Override
	public void onHandEnded() {
		gameObserver.onHandEnded();
	}

	@Override
	public String getName() {
		return (name != null) ? name : "NeuralBot";
	}

	public void setName(String name) {
		this.name = name;
	}

}

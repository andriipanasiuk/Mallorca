package mallorcatour.bot.neural;

import java.util.ArrayList;
import java.util.List;

import mallorcatour.bot.actionpreprocessor.NLActionPreprocessor;
import mallorcatour.bot.interfaces.IExternalAdvisor;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumHolder;
import mallorcatour.bot.preflop.NLPreflopChart;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.IAdvisor;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IActionPreprocessor;
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

	protected IPlayerGameInfo gameInfo;
	private Card heroCard1, heroCard2;
	private final ISituationHandler situationHandler;
	private final ISpectrumHolder villainSpectrumHolder;
	private final IAdvisor advisor;
	private final IActionChecker actionChecker;
	private final IAdvisor preflopBot;
	private final IActionPreprocessor actionPreprocessor;
	private final String DEBUG_PATH;
	private final List<IExternalAdvisor> externalHelpers = new ArrayList<>();

	public GrandtorinoBot(IAdvisor neuralNetwork, ISituationHandler situationHandler,
			ISpectrumHolder villainSpectrumHolder, IActionChecker actionChecker, LimitType limitType, String debug) {
		this.advisor = neuralNetwork;
		this.DEBUG_PATH = debug;
		this.actionChecker = actionChecker;
		this.villainSpectrumHolder = villainSpectrumHolder;
		this.situationHandler = situationHandler;
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
	public void onHoleCards(Card c1, Card c2, String villainName) {
		situationHandler.onHoleCards(c1, c2, villainName);
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
		action = actionChecker.checkAction(action, situation, gameInfo, holeCards, villainSpectrumHolder.getSpectrum());
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
		LocalSituation situation = situationHandler.onHeroSituation();

		Log.f(DEBUG_PATH, "=========  Decision-making  =========");
		Log.f(DEBUG_PATH, "Situation: " + VectorUtils.toString(situation));
		action = getActionInternal(situation, new HoleCards(heroCard1, heroCard2));
		Log.f(DEBUG_PATH, "===============  End  ==============");
		situationHandler.onHeroActed(situation, action);
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
	 * 
	 * @param gameInfo
	 *            the game start information
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
		// TODO remove toCall parameter from here
		situationHandler.onVillainActed(action, toCall);
	}

	@Override
	public void onHandEnded() {
		// do nothing
	}

	@Override
	public String getName() {
		return "NeuralBot";
	}
}

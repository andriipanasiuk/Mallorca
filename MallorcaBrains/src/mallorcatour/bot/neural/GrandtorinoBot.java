package mallorcatour.bot.neural;

import java.util.ArrayList;
import java.util.List;

import mallorcatour.bot.ObservingPlayer;
import mallorcatour.bot.interfaces.IExternalAdvisor;
import mallorcatour.bot.preflop.NLPreflopChart;
import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.IAdvisor;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.core.vector.VectorUtils;
import mallorcatour.util.Log;

/**
 * This bot currently does not use global parameters.
 * 
 * @author Andrew
 */
public class GrandtorinoBot extends ObservingPlayer {

	private IActionChecker actionChecker;
	private final IAdvisor advisor;
	private final IAdvisor preflopBot;
	private final List<IExternalAdvisor> externalHelpers = new ArrayList<>();

	public void set(IActionChecker actionChecker) {
		this.actionChecker = actionChecker;
	}

	public GrandtorinoBot(IAdvisor neuralNetwork, LimitType limitType, String name, String debug) {
		super(name, debug);
		this.advisor = neuralNetwork;
		preflopBot = new NLPreflopChart();
	}

	public void addExternalHelper(IExternalAdvisor advisor) {
		externalHelpers.add(advisor);
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

	@Override
	public String getDefaultName() {
		return "NeuralBot";
	}

}

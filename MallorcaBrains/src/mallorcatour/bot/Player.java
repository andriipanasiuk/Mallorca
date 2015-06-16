package mallorcatour.bot;

import mallorcatour.brains.IActionChecker;
import mallorcatour.brains.IAdvisor;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.tools.Log;

/**
 * This bot currently does not use global parameters.
 * 
 * @author Andrew
 */
public class Player extends ObservingPlayer {

	private final IAdvisor preflopAdvisor;
	private final IAdvisor postflopAdvisor;
	private final IAdvisor preflopChart;
	private IActionChecker actionChecker;

	public Player(IAdvisor preflopAdvisor, IAdvisor preflopChart, IAdvisor commonAdvisor, IActionChecker actionChecker,
			String name, String debug) {
		super(name, debug);
		if (commonAdvisor == null || commonAdvisor == IAdvisor.UNSUPPORTED) {
			throw new IllegalArgumentException("Common advisor must not be null");
		}
		this.preflopAdvisor = preflopAdvisor;
		this.postflopAdvisor = commonAdvisor;
		this.preflopChart = preflopChart;
		this.actionChecker = actionChecker;
	}

	/**
	 * Requests an Action from the player Called when it is the Player's turn to
	 * act.
	 */
	@Override
	public Action getAction() {
		LocalSituation situation = situationHandler.getSituation();
		IAdvice advice = null;
		HoleCards cards = new HoleCards(heroCard1, heroCard2);
		Log.f(DEBUG_PATH, "=========  Decision-making  =========");
		Log.f(DEBUG_PATH, C.SITUATION + ": " + situation.toString());
		if (gameInfo.isPreFlop()) {
			advice = preflopChart.getAdvice(situation, cards, gameInfo);
			if (advice != null) {
				Log.f(DEBUG_PATH, "Advice from preflop chart");
			}
			if (advice == null) {
				advice = preflopAdvisor.getAdvice(situation, cards, gameInfo);
			}
		}
		if (advice == null) {
			advice = postflopAdvisor.getAdvice(situation, cards, gameInfo);
		}
		Log.f(DEBUG_PATH, C.ADVICE + ": " + advice.toString());
		Action action = advice.getAction();
		action = actionPreprocessor.preprocessAction(action, gameInfo);
		Log.f(DEBUG_PATH, C.ACTION + ": " + action.toString());
		Log.f(DEBUG_PATH, "=========  End  =========");
		action = actionChecker.checkAction(action, situation, gameInfo, cards);
		gameObserver.onActed(action, gameInfo.getAmountToCall(), getName());
		return action;
	}

	@Override
	public String getDefaultName() {
		return "NLMathBot";
	}

	@Override
	public String toString(){
		return getName();
	}
}

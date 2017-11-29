package mallorcatour.bot;

import mallorcatour.Advisor;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.situation.HandState;
import mallorcatour.tools.Log;

/**
 * This bot currently does not use global parameters.
 * 
 * @author Andrew
 */
public class Player extends ObservingPlayer {

	private final Advisor preflopAdvisor;
	private final Advisor postflopAdvisor;
	private final Advisor preflopChart;
	private AdvisorListener student = AdvisorListener.NONE;

	public Player(Advisor preflopAdvisor, Advisor preflopChart, Advisor commonAdvisor,
				  String name, String debug) {
		super(name, debug);
		if (commonAdvisor == null || commonAdvisor == Advisor.UNSUPPORTED) {
			throw new IllegalArgumentException("Common advisor must not be null");
		}
		this.preflopAdvisor = preflopAdvisor;
		this.postflopAdvisor = commonAdvisor;
		this.preflopChart = preflopChart;
	}

	public void setStudent(AdvisorListener student) {
		this.student = student;
	}

	/**
	 * Requests an Action from the player Called when it is the Player's turn to
	 * act.
	 */
	@Override
	public Action getAction() {
		long time = System.currentTimeMillis();
		HandState situation = situationHandler.getSituation();
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

		student.onAdvice(situation, advice);

		Log.f(DEBUG_PATH, C.ADVICE + ": " + advice.toString());
		Action action = advice.getAction();
		action = actionPreprocessor.preprocessAction(action, gameInfo);
		Log.f(DEBUG_PATH, C.ACTION + ": " + action.toString());
		Log.f(DEBUG_PATH, "Decision from " + this + " in " + (System.currentTimeMillis() - time) + " ms");
		Log.f(DEBUG_PATH, "=========  End  =========");
		gameObserver.onActed(action, gameInfo.getAmountToCall(), getName());
		return action;
	}

	@Override
	public String getDefaultName() {
		return "NLMathBot";
	}

	@Override
	public String toString() {
		return getName();
	}
}

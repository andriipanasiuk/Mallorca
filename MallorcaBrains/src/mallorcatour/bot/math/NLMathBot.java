package mallorcatour.bot.math;

import java.util.Map;

import mallorcatour.bot.interfaces.ISpectrumHolder;
import mallorcatour.bot.preflop.NLPreflopChart;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.brains.neural.gusxensen.GusXensen;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.util.Log;

/**
 * This bot currently does not use global parameters.
 * 
 * @author Andrew
 */
public class NLMathBot extends ObservingPlayer {

	private ISpectrumHolder villainSpectrumHolder;
	private final IAdvisor preflopAdvisor;
	private final IAdvisor preflopBot;
	private BaseAdviceCreatorFromMap adviceCreator;
	private final IProfitCalculator profitCalculator;

	public NLMathBot(IProfitCalculator profitCalculator, String name, String debug,
			ISpectrumHolder villainSpectrumHolder) {
		super(name, debug);
		this.profitCalculator = profitCalculator;
		this.villainSpectrumHolder = villainSpectrumHolder;
		adviceCreator = new AdviceCreatorFromMap();
		GusXensen player = new GusXensen();
		preflopAdvisor = new NeuralAdvisor(player, player, "Gus Xensen");
		preflopBot = new NLPreflopChart();
	}

	/**
	 * Requests an Action from the player Called when it is the Player's turn to
	 * act.
	 */
	@Override
	public Action getAction() {
		LocalSituation situation = situationHandler.getSituation();
		IAdvice advice;
		Action action = null;
		Log.f(DEBUG_PATH, "=========  Decision-making  =========");
		if (gameInfo.isPostFlop()) {
			Map<Action, Double> map = profitCalculator.getProfitMap(gameInfo, situation, heroCard1, heroCard2,
					villainSpectrumHolder.getSpectrum());
			Log.f(DEBUG_PATH, "Map<Action, Profit>: " + map.toString());
			advice = adviceCreator.create(map);
		} else {
			Log.f(DEBUG_PATH, "Preflop situation: " + situation.toString());
			advice = preflopBot.getAdvice(situation, new HoleCards(heroCard1, heroCard2), null);
			if (advice != null) {
				Log.f(DEBUG_PATH, "Advice from preflop chart");
			}
			if (advice == null) {
				advice = preflopAdvisor.getAdvice(situation, new HoleCards(heroCard1, heroCard2), null);
			}
		}
		Log.f(DEBUG_PATH, "Advice: " + advice.toString());
		action = advice.getAction();
		Log.f(DEBUG_PATH, "Action: " + action.toString());
		action = actionPreprocessor.preprocessAction(action, gameInfo);
		Log.f(DEBUG_PATH, "=========  End  =========");
		gameObserver.onActed(action, gameInfo.getAmountToCall(), getName());
		return action;
	}

	@Override
	public String getName() {
		return name != null ? name : "NLMathBot";
	}

}

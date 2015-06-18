/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.modeller;

import mallorcatour.bot.interfaces.IActionObserver;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.brains.neural.cuba.Cuba;
import mallorcatour.brains.neural.gusxensen.GusXensen;
import mallorcatour.brains.stats.PokerStatInfo;
import mallorcatour.brains.stats.StatCalculator;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.ExactAdvice;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.game.interfaces.IPokerStats;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.tools.Log;

/**
 * 
 * @author Andrew
 */
public class PlayerStatModel implements IAdvisor, IActionObserver {

	private static IAdvisor DEFAULT_NL_NEURAL;
	private static IAdvisor CUBA_NL_NEURAL;
	static {
		GusXensen player = new GusXensen();
		DEFAULT_NL_NEURAL = new NeuralAdvisor(player, player, "Gus Xensen");
		Cuba cuba = new Cuba();
		CUBA_NL_NEURAL = new NeuralAdvisor(cuba, cuba, "Cuba");
	}

	private IAdvisor currentNeural;
	private final String DEBUG_PATH;
	private final PokerStatInfo pokerStatInfo = new PokerStatInfo();

	private int handsCount;

	public PlayerStatModel(String debug) {
		currentNeural = CUBA_NL_NEURAL;
		this.DEBUG_PATH = debug;
	}

	@Override
	public IAdvice getAdvice(LocalSituation situation, HoleCards cards, IPlayerGameInfo gameInfo) {
		IAdvice result = currentNeural.getAdvice(situation, cards, gameInfo);
		return result;
	}

	@SuppressWarnings("unused")
	private void chooseModellingNeural() {
		double minError = Double.MAX_VALUE;
		PokerStatsDistance distance = new PokerStatsDistance();
		double error = distance.getDistance(this.getStats(), DEFAULT_NL_NEURAL.getStats());
		// TODO add real choosing of neural to model opp
		currentNeural = DEFAULT_NL_NEURAL;
		Log.f(DEBUG_PATH, "Modelling by " + currentNeural.getName());
		Log.f(DEBUG_PATH, "Distance to modelling bot: " + minError);
	}

	@Override
	public String getName() {
		return "Player model";
	}

	@Override
	public IPokerStats getStats() {
		return pokerStatInfo;
	}

	@Override
	public void acted(LocalSituation situation, Action action) {
		StatCalculator.changeStat(situation, new ExactAdvice(action), pokerStatInfo);
	}

	public void addHandPlayed() {
		handsCount++;
	}

	public int getHandsCount() {
		return handsCount;
	}

}

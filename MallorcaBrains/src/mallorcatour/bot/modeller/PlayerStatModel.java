/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.modeller;

import java.util.List;

import mallorcatour.bot.villainobserver.IActionObserver;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.brains.neural.gusxensen.GusXensen;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.game.interfaces.IPokerStats;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.neural.core.PokerLearningExample;
import mallorcatour.tools.Log;
import mallorcatour.tools.Pair;

/**
 * 
 * @author Andrew
 */
public class PlayerStatModel implements IAdvisor, IActionObserver, IPokerStats {

	private static IAdvisor DEFAULT_NL_NEURAL;
	static {
		GusXensen player = new GusXensen();
		DEFAULT_NL_NEURAL = new NeuralAdvisor(player, player, "Gus Xensen");
	}

	static final int REQUIRED_HANDS_FOR_MODELLING = 5;
	private IAdvisor currentNeural;
	private final String DEBUG_PATH;

	private int handsCount;
	private List<PokerLearningExample> situations;
	private Pair<Integer, Integer> vpip = new Pair<Integer, Integer>(0, 0);
	private Pair<Integer, Integer> pfr = new Pair<Integer, Integer>(0, 0);
	private Pair<Integer, Integer> aggressionFrequency = new Pair<Integer, Integer>(0, 0);
	private Pair<Integer, Integer> foldFrequency = new Pair<Integer, Integer>(0, 0);

	public PlayerStatModel(String debug) {
		currentNeural = DEFAULT_NL_NEURAL;
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
		double error = distance.getDistance(this, DEFAULT_NL_NEURAL.getStats());
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
		return this;
	}

	@Override
	public void acted(LocalSituation situation, Action advice) {
		// TODO change stats
	}

	public void addHandPlayed() {
		handsCount++;
	}

	public int getHandsCount() {
		return handsCount;
	}

	public void addPokerLearningExample(PokerLearningExample example) {
		situations.add(example);
	}

	public void addPokerLearningExamples(List<PokerLearningExample> examples) {
		situations.addAll(examples);
	}

	public List<PokerLearningExample> getExamples() {
		return situations;
	}

	public void addVpipInfo(Pair<Integer, Integer> vpip) {
		this.vpip.first += vpip.first;
		this.vpip.second += vpip.second;
	}

	public void addAggressionFrequencyInfo(Pair<Integer, Integer> aggressionFreq) {
		this.aggressionFrequency.first += aggressionFreq.first;
		this.aggressionFrequency.second += aggressionFreq.second;
	}

	public void addFoldInfo(Pair<Integer, Integer> fold) {
		this.foldFrequency.first += fold.first;
		this.foldFrequency.second += fold.second;
	}

	@Override
	public double getFoldFrequency() {
		return (double) foldFrequency.first / foldFrequency.second;
	}

	@Override
	public double getAggressionFrequency() {
		return (double) aggressionFrequency.first / aggressionFrequency.second;
	}

	@Override
	public double getVpip() {
		return (double) vpip.first / vpip.second;
	}

	@Override
	public double getPfr() {
		return (double) pfr.first / pfr.second;
	}
}

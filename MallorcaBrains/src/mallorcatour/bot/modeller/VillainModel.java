/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.modeller;

import mallorcatour.bot.villainobserver.IVillainListener;
import mallorcatour.bot.villainobserver.VillainStatistics;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.brains.neural.gusxensen.GusXensen;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.tools.Log;

/**
 * 
 * @author Andrew
 */
public class VillainModel implements IAdvisor, IVillainListener {

	// TODO create correct neurals with different players or change villain
	// modelling approach
	private static IAdvisor DEFAULT_NL_NEURAL;
	static {
		GusXensen player = new GusXensen();
		DEFAULT_NL_NEURAL = new NeuralAdvisor(player, player, "Gus Xensen");
	}
	private static final int REQUIRED_HANDS_FOR_MODELLING = 5;
	private VillainStatistics villainStatistics;
	private IAdvisor currentVillainNeural;
	private final PreflopSpectrumModeller preflopVillainModeller;
	private boolean isVillainKnown;
	private final String DEBUG_PATH;

	public VillainModel(LimitType limitType, String debug) {
		preflopVillainModeller = new PreflopSpectrumModeller();
		if(limitType == LimitType.FIXED_LIMIT){
			throw new UnsupportedOperationException("FL is unsupported currently");
		}
		currentVillainNeural = DEFAULT_NL_NEURAL;
		this.DEBUG_PATH = debug;
	}

	@Override
	public IAdvice getAdvice(LocalSituation situation, HoleCards cards, IPlayerGameInfo gameInfo) {
		int street = situation.getStreet();
		if (street == LocalSituation.PREFLOP && isVillainKnown && villainStatistics.isPreflopLearned()) {
			return preflopVillainModeller.getAdvice(situation, cards, villainStatistics.getPrefloNeuralNetwork());
		}
		IAdvice result = currentVillainNeural.getAdvice(situation, cards, null);
		return result;
	}

	@Override
	public void onVillainChange(VillainStatistics villain) {
		villainStatistics = villain;
		Log.f(DEBUG_PATH,
				"Villain changed. Name: " + villainStatistics.getName() + ". Aggression frequency: "
						+ villainStatistics.getAggressionFrequency() + ". Fold frequency: "
						+ villainStatistics.getFoldFrequency() + ". AF: " + villainStatistics.getAggressionFactor()
						+ ". Wtsd: " + villainStatistics.getWtsd() + ". Hands: " + villainStatistics.getHandsCount());
		if (villainStatistics.getHandsCount() >= REQUIRED_HANDS_FOR_MODELLING) {
			chooseModellingNeural();
		}
	}

	@SuppressWarnings("unused")
	private void chooseModellingNeural() {
		double minError = Double.MAX_VALUE;
		PokerStatsDistance distance = new PokerStatsDistance();
		double error = distance.getDistance(villainStatistics, DEFAULT_NL_NEURAL);
		//TODO add real choosing of neural to model opp
		currentVillainNeural = DEFAULT_NL_NEURAL;
		Log.f(DEBUG_PATH, "Modelling by " + currentVillainNeural.getName());
		Log.f(DEBUG_PATH, "Distance to modelling bot: " + minError);
	}

	@Override
	public void onVillainKnown(boolean known) {
		isVillainKnown = known;
		if (!known) {
			currentVillainNeural = DEFAULT_NL_NEURAL;
		}
	}

	@Override
	public double getAggressionFactor() {
		return villainStatistics.getAggressionFactor();
	}

	@Override
	public double getWtsd() {
		return villainStatistics.getWtsd();
	}

	@Override
	public double getAggressionFrequency() {
		return villainStatistics.getAggressionFrequency();
	}

	@Override
	public double getFoldFrequency() {
		return villainStatistics.getFoldFrequency();
	}

	@Override
	public String getName() {
		return villainStatistics.getName();
	}
}

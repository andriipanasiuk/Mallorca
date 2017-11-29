/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.modeller;

import mallorcatour.bot.C;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.brains.neural.checkburn.CheckBurn;
import mallorcatour.brains.neural.cuba.Cuba;
import mallorcatour.brains.neural.dafish.Dafish;
import mallorcatour.brains.neural.dafish2.Dafish2;
import mallorcatour.brains.neural.france.France;
import mallorcatour.brains.neural.germany.Germany;
import mallorcatour.brains.neural.pbx.Pbx;
import mallorcatour.brains.stats.IPokerStats;
import mallorcatour.brains.stats.PokerStatInfo;
import mallorcatour.brains.stats.StatCalculator;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.tools.DoubleUtils;
import mallorcatour.tools.Log;

/**
 * 
 * @author Andrew
 */
public class PlayerStatModel implements IAdvisor, AdvisorListener {

	public static IAdvisor CUBA_NL_NEURAL;
	public static IAdvisor GERMANY_NL_NEURAL;
	public static IAdvisor CHECKBURN_NL_NEURAL;
	public static IAdvisor DAFISH_NL_NEURAL;
	public static IAdvisor DAFISH2_NL_NEURAL;
	public static IAdvisor PBX_NL_NEURAL;
	public static IAdvisor random = new RandomAdvisor();
	public static IAdvisor[] neurals;

	private static final int MODEL_EVERY_SITUATION = 10;

	static {
		Cuba cuba = new Cuba();
		CUBA_NL_NEURAL = new NeuralAdvisor(cuba, cuba, "Cu" + "ba");
		Germany germany = new Germany();
		GERMANY_NL_NEURAL = new NeuralAdvisor(germany, germany, "Ger" + "many");
		CheckBurn checkBurn = new CheckBurn();
		CHECKBURN_NL_NEURAL = new NeuralAdvisor(checkBurn, checkBurn, "Chec" + "kBurn");
		Pbx pbx = new Pbx();
		PBX_NL_NEURAL = new NeuralAdvisor(pbx, pbx, "P" + "BX");
		Dafish dafish = new Dafish();
		DAFISH_NL_NEURAL = new NeuralAdvisor(dafish, dafish, "DaF" + "ish");
		Dafish2 dafish2 = new Dafish2();
		DAFISH2_NL_NEURAL = new NeuralAdvisor(dafish2, dafish2, "DaF" + "ish2");
		neurals = new IAdvisor[] { CUBA_NL_NEURAL, GERMANY_NL_NEURAL, CHECKBURN_NL_NEURAL,
				DAFISH_NL_NEURAL,DAFISH2_NL_NEURAL, PBX_NL_NEURAL };
	}

	private IAdvisor currentNeural;
	private final String DEBUG_PATH;
	private final PokerStatInfo pokerStatInfo = new PokerStatInfo();
	private final boolean chooseNeural;
	private int situationCount;
	private int handsCount;

	public PlayerStatModel(String debug) {
		this(false, debug);
	}

	public PlayerStatModel(IAdvisor currentAdvisor, boolean choose, String debug) {
		this.currentNeural = currentAdvisor;
		this.chooseNeural = choose;
		this.DEBUG_PATH = debug;
	}

	public PlayerStatModel(IAdvisor currentAdvisor, boolean choose) {
		this.currentNeural = currentAdvisor;
		this.chooseNeural = choose;
		this.DEBUG_PATH = "";
	}

	public PlayerStatModel(IAdvisor currentAdvisor, String debug) {
		this(currentAdvisor, false, debug);
	}

	public PlayerStatModel(boolean choose, String debug) {
		this(random, choose, debug);
	}

	@Override
	public IAdvice getAdvice(LocalSituation situation, HoleCards cards, IPlayerGameInfo gameInfo) {
		IAdvice result = currentNeural.getAdvice(situation, cards, gameInfo);
		return result;
	}

	private void chooseModellingNeural() {
		double minError = Double.MAX_VALUE;
		PokerStatsDistance distance = new PokerStatsDistance();
		for (IAdvisor neural : neurals) {
			double error = distance.getDistance(this.getStats(), neural.getStats());
			Log.f(DEBUG_PATH, "Difference with " + neural.getName() + ": " + error);
			if (error < minError) {
				minError = error;
				currentNeural = neural;
			}
		}
		Log.f(DEBUG_PATH, "Modelling " + currentNeural.getName() + " after " + situationCount + " situ-ns");
		Log.f(DEBUG_PATH, "Difference " + C.STATS + ": " + DoubleUtils.digitsAfterComma(minError, 2));
	}

	@Override
	public String getName() {
		return "Player model by " + currentNeural.getName();
	}

	@Override
	public IPokerStats getStats() {
		return pokerStatInfo;
	}

	@Override
	public void onAdvice(LocalSituation situation, IAdvice advice) {
		situationCount++;
		StatCalculator.changeStat(situation, advice, pokerStatInfo);
		if (situationCount % MODEL_EVERY_SITUATION == 0) {
			Log.f(DEBUG_PATH, C.VILLAIN + " " + C.STATS + ": " + pokerStatInfo);
			if (chooseNeural) {
				chooseModellingNeural();
			}
		}
	}

	public void addHandPlayed() {
		handsCount++;
	}

	public int getHandsCount() {
		return handsCount;
	}

}

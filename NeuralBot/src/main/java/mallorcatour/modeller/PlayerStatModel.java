/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.modeller;

import mallorcatour.bot.C;
import mallorcatour.Advisor;
import mallorcatour.core.game.situation.HandState;
import mallorcatour.neural.bot.NeuralAdvisorImpl;
import mallorcatour.neural.bot.checkburn.CheckBurn;
import mallorcatour.neural.bot.cuba.Cuba;
import mallorcatour.neural.bot.dafish.Dafish;
import mallorcatour.neural.bot.dafish2.Dafish2;
import mallorcatour.neural.bot.germany.Germany;
import mallorcatour.neural.bot.pbx.Pbx;
import mallorcatour.stats.PokerStats;
import mallorcatour.stats.PokerStatsBuffer;
import mallorcatour.stats.StatCalculator;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.tools.DoubleUtils;
import mallorcatour.tools.Log;

/**
 * Класс, который моделирует игрока (чаще всего противника), по статистике его действий.
 * В зависимости от предыдущих действий игрока этот класс моделирует игрока одним из "нейронных" ботов.
 * То есть мы будем предугадывать будущие действия соперника,
 * а также строить его текущий спектр возможных карт
 * на основании его прошлых действий в текущей раздаче.
 */
public class PlayerStatModel implements NeuralAdvisor, AdvisorListener {

	public static NeuralAdvisor CUBA_NL_NEURAL;
	public static NeuralAdvisor GERMANY_NL_NEURAL;
	public static NeuralAdvisor CHECKBURN_NL_NEURAL;
	public static NeuralAdvisor DAFISH_NL_NEURAL;
	public static NeuralAdvisor DAFISH2_NL_NEURAL;
	public static NeuralAdvisor PBX_NL_NEURAL;
	public static Advisor random = new RandomAdvisor();
	public static NeuralAdvisor[] neurals;

	private static final int MODEL_EVERY_SITUATION = 10;

	static {
		Cuba cuba = new Cuba();
		CUBA_NL_NEURAL = new NeuralAdvisorImpl(cuba, cuba.getStats(), "Cu" + "ba");
		Germany germany = new Germany();
		GERMANY_NL_NEURAL = new NeuralAdvisorImpl(germany, germany.getStats(), "Ger" + "many");
		CheckBurn checkBurn = new CheckBurn();
		CHECKBURN_NL_NEURAL = new NeuralAdvisorImpl(checkBurn, checkBurn.getStats(), "Chec" + "kBurn");
		Pbx pbx = new Pbx();
		PBX_NL_NEURAL = new NeuralAdvisorImpl(pbx, pbx.getStats(), "P" + "BX");
		Dafish dafish = new Dafish();
		DAFISH_NL_NEURAL = new NeuralAdvisorImpl(dafish, dafish.getStats(), "DaF" + "ish");
		Dafish2 dafish2 = new Dafish2();
		DAFISH2_NL_NEURAL = new NeuralAdvisorImpl(dafish2, dafish2.getStats(), "DaF" + "ish2");
		neurals = new NeuralAdvisor[] { CUBA_NL_NEURAL, GERMANY_NL_NEURAL, CHECKBURN_NL_NEURAL,
				DAFISH_NL_NEURAL,DAFISH2_NL_NEURAL, PBX_NL_NEURAL };
	}

	private Advisor currentNeural;
	private final String DEBUG_PATH;
	private final PokerStatsBuffer pokerStats = new PokerStatsBuffer();
	private final boolean chooseNeural;
	private int situationCount;
	private int handsCount;

	public PlayerStatModel(String debug) {
		this(false, debug);
	}

	public PlayerStatModel(Advisor currentAdvisor, boolean choose, String debug) {
		this.currentNeural = currentAdvisor;
		this.chooseNeural = choose;
		this.DEBUG_PATH = debug;
	}

	public PlayerStatModel(Advisor currentAdvisor, boolean choose) {
		this.currentNeural = currentAdvisor;
		this.chooseNeural = choose;
		this.DEBUG_PATH = "";
	}

	public PlayerStatModel(Advisor currentAdvisor, String debug) {
		this(currentAdvisor, false, debug);
	}

	public PlayerStatModel(boolean choose, String debug) {
		this(random, choose, debug);
	}

	@Override
	public IAdvice getAdvice(HandState situation, HoleCards cards, IPlayerGameInfo gameInfo) {
		IAdvice result = currentNeural.getAdvice(situation, cards, gameInfo);
		return result;
	}

	private void chooseModellingNeural() {
		double minError = Double.MAX_VALUE;
		PokerStatsDistance distance = new PokerStatsDistance();
		for (NeuralAdvisor neural : neurals) {
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
	public PokerStats getStats() {
		return pokerStats;
	}

	@Override
	public void onAdvice(HandState situation, IAdvice advice) {
		situationCount++;
		StatCalculator.changeStat(situation, advice, pokerStats);
		if (situationCount % MODEL_EVERY_SITUATION == 0) {
			Log.f(DEBUG_PATH, C.VILLAIN + " " + C.STATS + ": " + pokerStats);
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

package mallorcatour.brains.test;

import mallorcatour.bot.AdvisorListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.math.LessVarianceActionFromMap;
import mallorcatour.bot.math.NLFullMathBotFactory;
import mallorcatour.bot.math.NLGameSolver;
import mallorcatour.bot.modeller.PlayerStatModel;
import mallorcatour.bot.random.RandomBot;
import mallorcatour.bot.sklansky.PushBot;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.neural.cuba.CubaFactory;
import mallorcatour.brains.neural.france.FranceFactory;
import mallorcatour.brains.neural.germany.GermanyFactory;
import mallorcatour.brains.neural.gusxensen.GusXensenFactory;
import mallorcatour.brains.stats.PokerStatInfo;
import mallorcatour.core.game.engine.GameEngine.CashSummary;
import mallorcatour.core.game.engine.GameEngine.TournamentSummary;
import mallorcatour.core.game.engine.GameEngine;
import mallorcatour.core.game.engine.PredefinedGameEngine;
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.robot.controller.PokerPreferences;
import mallorcatour.tools.DateUtils;
import mallorcatour.tools.Log;

public class BotVsBot {

	public static void main(String... args) {
		String DEBUG_PATH = PokerPreferences.DEBUG_PATTERN + DateUtils.getDate(false) + ".txt";
		Log.DEBUG_PATH = DEBUG_PATH;

		NLGameSolver.LOGGING = true;
		NLFullMathBotFactory fullMathBotFactory = new NLFullMathBotFactory();
		PokerStatInfo pokerStats = new PokerStatInfo();
		WritingStudent student = new WritingStudent();
		PlayerStatModel villainModel = new PlayerStatModel(PlayerStatModel.GUSXENSEN_NL_NEURAL, false, DEBUG_PATH);
		IPlayer fullMathBot = fullMathBotFactory.createBot(villainModel, ISpectrumListener.EMPTY, villainModel,
				pokerStats, "Full MathBot", DEBUG_PATH);

		GermanyFactory germanyFactory = new GermanyFactory();
		IPlayer neuralGermany = germanyFactory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY,
				AdvisorListener.NONE, AdvisorListener.NONE, "Germany", DEBUG_PATH);

		FranceFactory franceFactory = new FranceFactory();
		IPlayer neuralFrance = franceFactory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY,
				AdvisorListener.NONE, AdvisorListener.NONE, "France", DEBUG_PATH);

		GusXensenFactory factory = new GusXensenFactory();
		IPlayer neuralGusXensen = factory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY,
				AdvisorListener.NONE, AdvisorListener.NONE, "Gus Xensen", DEBUG_PATH);

		CubaFactory cubaFactory = new CubaFactory();
		IPlayer neuralCuba = cubaFactory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY, AdvisorListener.NONE,
				AdvisorListener.NONE, "Cuba", DEBUG_PATH);

		IPlayer random = new RandomBot("RandomBot", DEBUG_PATH);
		IPlayer pushBot = new PushBot("PushBot", DEBUG_PATH);
		PredefinedGameEngine engine = new PredefinedGameEngine(fullMathBot, neuralCuba, IGameObserver.EMPTY, DEBUG_PATH);
//		engine.cards(fullMathBot, "7s3s").button(fullMathBot);
		GameEngine.BLINDS_CHANGE = 30;

		CashSummary cashSummary = engine.playCash(10);
		Log.d("MathBot stats: " + pokerStats.toString());
		Log.d("Villain stats: " + villainModel.getStats().toString());
		Log.d(cashSummary.winner + " " + cashSummary.gain);
		if (true) {
			return;
		}
		int count1 = 0, count2 = 0;
		int handCount = 0;
		for (int i = 0; i < 10; i++) {
			TournamentSummary summary = engine.playGame();
			if (summary.winner.equals(fullMathBot.getName())) {
				count1++;
			} else {
				count2++;
			}
			handCount += summary.handsCount;
			Log.d("Game # " + i + " has been played. Hands " + summary.handsCount);
			Log.d(count1 + " " + count2);
			Log.d("MathBot stats: " + pokerStats.toString());
			Log.d("Villain stats: " + villainModel.getStats().toString());
		}
		// student.printAnalysis();
		// student.save("France");
		// student.reset();
		// }
		Log.d("Totally played " + handCount + " hands");
		Log.d(count1 + " " + count2);
	}
}

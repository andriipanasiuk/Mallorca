package mallorcatour.brains.test;

import mallorcatour.bot.AdvisorListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.math.NLFullMathBotFactory;
import mallorcatour.bot.math.NLGameSolver;
import mallorcatour.bot.modeller.PlayerStatModel;
import mallorcatour.bot.random.RandomBot;
import mallorcatour.bot.sklansky.PushBot;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.neural.cuba.CubaFactory;
import mallorcatour.brains.neural.germany.GermanyFactory;
import mallorcatour.brains.neural.gusxensen.GusXensenFactory;
import mallorcatour.brains.stats.PokerStatInfo;
import mallorcatour.core.game.engine.GameEngine.TournamentSummary;
import mallorcatour.core.game.engine.PredefinedGameEngine;
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.robot.controller.PokerPreferences;
import mallorcatour.tools.DateUtils;
import mallorcatour.tools.Log;

public class BotVsBot {

	public static void main(String... args) {
		String DEBUG_PATH = PokerPreferences.DEBUG_PATTERN + DateUtils.getDate(false) + ".txt";
		Log.DEBUG_PATH = DEBUG_PATH;

		NLGameSolver.CONSIDER_PREV_POT = true;
		NLFullMathBotFactory fullMathBotFactory = new NLFullMathBotFactory();
		PokerStatInfo pokerStats = new PokerStatInfo();
		WritingStudent student = new WritingStudent();
		PlayerStatModel villainModel = new PlayerStatModel(PlayerStatModel.GERMANY_NL_NEURAL, DEBUG_PATH);
		IPlayer fullMathBot = fullMathBotFactory.createBot(villainModel, ISpectrumListener.EMPTY, villainModel,
				student, "Full MathBot", DEBUG_PATH);

		GermanyFactory germanyFactory = new GermanyFactory();
		IPlayer neuralGermany = germanyFactory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY,
				AdvisorListener.NONE, AdvisorListener.NONE, "Germany", DEBUG_PATH);

		GusXensenFactory factory = new GusXensenFactory();
		IPlayer neuralGusXensen = factory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY,
				AdvisorListener.NONE, AdvisorListener.NONE, "Gus Xensen", DEBUG_PATH);

		CubaFactory cubaFactory = new CubaFactory();
		IPlayer neuralCuba = cubaFactory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY, AdvisorListener.NONE,
				pokerStats, "Cuba", DEBUG_PATH);

		IPlayer random = new RandomBot("RandomBot", DEBUG_PATH);
		IPlayer pushBot = new PushBot("PushBot", DEBUG_PATH);
		PredefinedGameEngine engine = new PredefinedGameEngine(fullMathBot, neuralGermany, IGameObserver.EMPTY,
				DEBUG_PATH);
		// engine.button(fullMathBot).cards(fullMathBot,
		// "TsTc").cards(neuralStandart, "KcAs").stack(fullMathBot, 1800)
		// .stack(neuralStandart, 1980).flop("KsKdJc");
		int count1 = 0, count2 = 0;
		// engine.playRound();
		int handCount = 0;
		for (int j = 0; j < 100; j++) {
			for (int i = 0; i < 100; i++) {
				TournamentSummary summary = engine.playGame();
				if (summary.winner.equals(fullMathBot.getName())) {
					count1++;
				} else {
					count2++;
				}
				handCount += summary.handsCount;
				Log.d("Game # " + i + " has been played. Hands " + summary.handsCount);
				Log.d(count1 + " " + count2);
			}
			student.printAnalysis();
			student.save("France");
			student.reset();
		}
		Log.d("MathBot stats: " + pokerStats.toString());
		Log.d("Villain stats: " + villainModel.getStats().toString());
		Log.d("Totally played " + handCount + " hands");
		Log.d(count1 + " " + count2);
	}
}

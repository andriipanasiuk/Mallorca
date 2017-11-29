package mallorcatour.game;

import mallorcatour.Advisor;
import mallorcatour.bot.math.NLFullMathBotFactory;
import mallorcatour.bot.math.NLGameSolver;
import mallorcatour.bot.random.RandomBot;
import mallorcatour.bot.sklansky.PushBot;
import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.engine.GameEngine;
import mallorcatour.core.game.engine.GameEngine.CashSummary;
import mallorcatour.core.game.engine.GameEngine.TournamentSummary;
import mallorcatour.core.game.engine.PredefinedGameEngine;
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.core.game.interfaces.ISpectrumListener;
import mallorcatour.core.player.interfaces.IPlayer;
import mallorcatour.neural.bot.checkburn.CheckBurnFactory;
import mallorcatour.neural.bot.cuba.CubaFactory;
import mallorcatour.neural.bot.dafish2.Dafish2Factory;
import mallorcatour.neural.bot.france.FranceFactory;
import mallorcatour.neural.bot.germany.GermanyFactory;
import mallorcatour.neural.bot.gusxensen.GusXensenFactory;
import mallorcatour.neural.bot.pbx.PbxFactory;
import mallorcatour.modeller.PlayerStatModel;
import mallorcatour.stats.PokerStatsBuffer;
import mallorcatour.tools.DateUtils;
import mallorcatour.tools.Log;

/**
 * Песочница для игры ботов между собой.
 */
public class BotVsBot {

	static RandomBot random;
	static PushBot pushBot;
	static IPlayer neuralCuba;
	static IPlayer neuralGusXensen;
	static IPlayer neuralFrance;
	static IPlayer neuralGermany;
	static IPlayer checkBurn;
	static IPlayer pbx;
	static IPlayer dafish2;
	static IPlayer fullMathBot;

	private static void createBots(String DEBUG_PATH, AdvisorListener student) {
		NLFullMathBotFactory fullMathBotFactory = new NLFullMathBotFactory();
		PlayerStatModel villainModel = new PlayerStatModel(DEBUG_PATH);
		fullMathBot = fullMathBotFactory.createBot(villainModel, ISpectrumListener.EMPTY, villainModel, student,
				"Full MathBot", DEBUG_PATH);

		CheckBurnFactory checkBurnFactory = new CheckBurnFactory();
		checkBurn = checkBurnFactory.createBot(Advisor.UNSUPPORTED, ISpectrumListener.EMPTY, AdvisorListener.NONE,
				AdvisorListener.NONE, "CheckBurn", DEBUG_PATH);

		GermanyFactory germanyFactory = new GermanyFactory();
		neuralGermany = germanyFactory.createBot(Advisor.UNSUPPORTED, ISpectrumListener.EMPTY, AdvisorListener.NONE,
				AdvisorListener.NONE, "Germany", DEBUG_PATH);

		FranceFactory franceFactory = new FranceFactory();
		neuralFrance = franceFactory.createBot(Advisor.UNSUPPORTED, ISpectrumListener.EMPTY, AdvisorListener.NONE,
				AdvisorListener.NONE, "France", DEBUG_PATH);

		GusXensenFactory factory = new GusXensenFactory();
		neuralGusXensen = factory.createBot(Advisor.UNSUPPORTED, ISpectrumListener.EMPTY, AdvisorListener.NONE,
				AdvisorListener.NONE, "Gus Xensen", DEBUG_PATH);

		CubaFactory cubaFactory = new CubaFactory();
		neuralCuba = cubaFactory.createBot(Advisor.UNSUPPORTED, ISpectrumListener.EMPTY, AdvisorListener.NONE,
				AdvisorListener.NONE, "Cuba", DEBUG_PATH);

		PbxFactory pbxFactory = new PbxFactory();
		pbx = pbxFactory.createBot(Advisor.UNSUPPORTED, ISpectrumListener.EMPTY, AdvisorListener.NONE,
				AdvisorListener.NONE, "PBX", DEBUG_PATH);

		Dafish2Factory dafish2Factory = new Dafish2Factory();
		dafish2 = dafish2Factory.createBot(Advisor.UNSUPPORTED, ISpectrumListener.EMPTY, AdvisorListener.NONE,
				AdvisorListener.NONE, "DaFish2", DEBUG_PATH);

		random = new RandomBot("RandomBot", DEBUG_PATH);
		pushBot = new PushBot("PushBot", DEBUG_PATH);

	}

	public static void main(String... args) {
		String DEBUG_PATH = "./logs/log" + DateUtils.getDate(false) + ".txt";
		Log.DEBUG_PATH = DEBUG_PATH;
		NLGameSolver.LOGGING = true;

		PokerStatsBuffer pokerStats = new PokerStatsBuffer();
		WritingAdvisorListener student = new WritingAdvisorListener();
		createBots(DEBUG_PATH, pokerStats);

		PredefinedGameEngine engine = new PredefinedGameEngine(fullMathBot, dafish2, IGameObserver.EMPTY,
				new PokerEquilatorBrecher(), DEBUG_PATH);
		GameEngine.BLINDS_CHANGE = 10;

		playSngs(engine, student, pokerStats);
	}

	private static void playSngs(GameEngine engine, WritingAdvisorListener student, PokerStatsBuffer stats) {
		int count1 = 0, count2 = 0;
		int handCount = 0;
		for (int i = 0; i < 3600; i++) {
			student.reset();
			TournamentSummary summary = engine.playGame();
			if (summary.winner.equals(fullMathBot.getName())) {
				count1++;
			} else {
				count2++;
			}
			handCount += summary.handsCount;
			Log.d("Game # " + i + " has been played. Hands " + summary.handsCount);
			Log.d(count1 + " " + count2);
			Log.d(stats.toString());
			student.save("Dafish2");
		}
		Log.d("Totally played " + handCount + " hands");
		Log.d(count1 + " " + count2);
	}

	private static void checkStats(GameEngine engine, PokerStatsBuffer pokerStats) {
		CashSummary cashSummary = engine.playCash(300);
		Log.f("stats.txt", "MathBot stats: " + pokerStats.toString());
		Log.d("MathBot stats: " + pokerStats.toString());
		Log.d(cashSummary.winner + " " + cashSummary.gain);
		Log.f("stats.txt", "============");
		pokerStats.reset();
	}
}

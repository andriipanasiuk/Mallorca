package mallorcatour.brains;

import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.player.interfaces.IPlayer;
import mallorcatour.bot.math.LessVarianceActionFromMap;
import mallorcatour.bot.math.NLFullMathBotFactory;
import mallorcatour.bot.math.NLGameSolver;
import mallorcatour.bot.modeller.PlayerStatModel;
import mallorcatour.bot.random.RandomBot;
import mallorcatour.bot.sklansky.PushBot;
import mallorcatour.brains.modeller.creator.SkelbotCorrections;
import mallorcatour.brains.neural.checkburn.CheckBurnFactory;
import mallorcatour.brains.neural.cuba.CubaFactory;
import mallorcatour.brains.neural.dafish2.Dafish2Factory;
import mallorcatour.brains.neural.france.FranceFactory;
import mallorcatour.brains.neural.germany.GermanyFactory;
import mallorcatour.brains.neural.gusxensen.GusXensenFactory;
import mallorcatour.brains.neural.pbx.PbxFactory;
import mallorcatour.brains.stats.PokerStatInfo;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.engine.GameEngine;
import mallorcatour.core.game.engine.GameEngine.CashSummary;
import mallorcatour.core.game.engine.GameEngine.TournamentSummary;
import mallorcatour.core.game.engine.PredefinedGameEngine;
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.core.game.interfaces.ISpectrumListener;
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
		checkBurn = checkBurnFactory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY, AdvisorListener.NONE,
				AdvisorListener.NONE, "CheckBurn", DEBUG_PATH);

		GermanyFactory germanyFactory = new GermanyFactory();
		neuralGermany = germanyFactory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY, AdvisorListener.NONE,
				AdvisorListener.NONE, "Germany", DEBUG_PATH);

		FranceFactory franceFactory = new FranceFactory();
		neuralFrance = franceFactory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY, AdvisorListener.NONE,
				AdvisorListener.NONE, "France", DEBUG_PATH);

		GusXensenFactory factory = new GusXensenFactory();
		neuralGusXensen = factory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY, AdvisorListener.NONE,
				AdvisorListener.NONE, "Gus Xensen", DEBUG_PATH);

		CubaFactory cubaFactory = new CubaFactory();
		neuralCuba = cubaFactory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY, AdvisorListener.NONE,
				AdvisorListener.NONE, "Cuba", DEBUG_PATH);

		PbxFactory pbxFactory = new PbxFactory();
		pbx = pbxFactory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY, AdvisorListener.NONE,
				AdvisorListener.NONE, "PBX", DEBUG_PATH);

		Dafish2Factory dafish2Factory = new Dafish2Factory();
		dafish2 = dafish2Factory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY, AdvisorListener.NONE,
				AdvisorListener.NONE, "DaFish2", DEBUG_PATH);

		random = new RandomBot("RandomBot", DEBUG_PATH);
		pushBot = new PushBot("PushBot", DEBUG_PATH);

	}

	public static void main(String... args) {
		String DEBUG_PATH = "./logs/log" + DateUtils.getDate(false) + ".txt";
		Log.DEBUG_PATH = DEBUG_PATH;
		NLGameSolver.LOGGING = true;

		PokerStatInfo pokerStats = new PokerStatInfo();
		WritingAdvisorListener student = new WritingAdvisorListener();
		createBots(DEBUG_PATH, pokerStats);

		PredefinedGameEngine engine = new PredefinedGameEngine(fullMathBot, dafish2, IGameObserver.EMPTY,
				new PokerEquilatorBrecher(), DEBUG_PATH);
		GameEngine.BLINDS_CHANGE = 10;

		SkelbotCorrections corrections = new SkelbotCorrections();
		LessVarianceActionFromMap.correction = corrections;
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				corrections.postflopCall += (i - 5) * 0.02;
				corrections.postflopRaise += (j - 5) * 0.02;
				Log.d(i + " " + j);
				checkStats(engine, pokerStats);
			}
		}
		if (true) {
			return;
		}
		playSngs(engine, student, pokerStats);
	}

	private static void playSngs(GameEngine engine, WritingAdvisorListener student, PokerStatInfo stats) {
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

	private static void checkStats(GameEngine engine, PokerStatInfo pokerStats) {
		CashSummary cashSummary = engine.playCash(300);
		Log.f("stats.txt", "MathBot stats: " + pokerStats.toString());
		Log.d("MathBot stats: " + pokerStats.toString());
		Log.d(cashSummary.winner + " " + cashSummary.gain);
		Log.f("stats.txt", "============");
		pokerStats.reset();
	}
}

package mallorcatour.brains.test;

import mallorcatour.bot.IStudent;
import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.math.NLFullMathBotFactory;
import mallorcatour.bot.random.RandomBot;
import mallorcatour.bot.sklansky.PushBot;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.neural.cuba.CubaFactory;
import mallorcatour.brains.neural.gusxensen.GusXensenAggressiveFactory;
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

		NLFullMathBotFactory fullMathBotFactory = new NLFullMathBotFactory();
		PokerStatInfo pokerStats = new PokerStatInfo();
//		 WritingStudent student = new WritingStudent("Temp");
		IPlayer fullMathBot = fullMathBotFactory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY,
				IDecisionListener.EMPTY, pokerStats, "Full MathBot", DEBUG_PATH);

		GusXensenFactory factory = new GusXensenFactory();
		IPlayer neuralGusXensen = factory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY,
				IDecisionListener.EMPTY, IStudent.NONE, "Grantorino Up", DEBUG_PATH);

		CubaFactory cubaFactory = new CubaFactory();
		IPlayer neuralCuba = cubaFactory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY,
				IDecisionListener.EMPTY, pokerStats, "Cuba", DEBUG_PATH);

		IPlayer neuralGusXensenAggressive = new GusXensenAggressiveFactory().createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY,
				IDecisionListener.EMPTY, IStudent.NONE, "Gus Xensen Aggressive", DEBUG_PATH);
		IPlayer random = new RandomBot("RandomBot", DEBUG_PATH);
		IPlayer pushBot = new PushBot("PushBot", DEBUG_PATH);
		PredefinedGameEngine engine = new PredefinedGameEngine(fullMathBot, neuralCuba, IGameObserver.EMPTY,
				DEBUG_PATH);
		// engine.button(fullMathBot).cards(fullMathBot,
		// "TsTc").cards(neuralStandart, "KcAs").stack(fullMathBot, 1800)
		// .stack(neuralStandart, 1980).flop("KsKdJc");
		int count1 = 0, count2 = 0;
		// engine.playRound();
		// for (int j = 0; j < 100; j++) {
		for (int i = 0; i < 20; i++) {
			TournamentSummary summary = engine.playGame();
			if (summary.winner.equals(fullMathBot.getName())) {
				count1++;
			} else {
				count2++;
			}
			Log.d("Game # " + i + " has been played. Hands " + summary.handsCount);
			Log.d(count1 + " " + count2);
		}
		// student.save();
		// student.reset();
		// }
		Log.d(pokerStats.toString());
		Log.d(count1 + " " + count2);
	}
}

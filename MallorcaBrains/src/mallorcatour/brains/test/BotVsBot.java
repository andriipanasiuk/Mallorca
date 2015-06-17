package mallorcatour.brains.test;

import mallorcatour.bot.IStudent;
import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.math.NLFullMathBotFactory;
import mallorcatour.bot.math.NLPostflopMathBotFactory;
import mallorcatour.bot.modeller.VillainModel;
import mallorcatour.bot.neural.NeuralAggroBotFactory;
import mallorcatour.bot.neural.NeuralBotFactory;
import mallorcatour.bot.random.RandomBot;
import mallorcatour.bot.sklansky.PushBot;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.brains.neural.gusxensen.GusXensen;
import mallorcatour.brains.neural.student.WritingStudent;
import mallorcatour.core.game.LimitType;
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
		NLFullMathBotFactory nlMathBotFactory = new NLFullMathBotFactory();
		NLPostflopMathBotFactory nlPostflopMathBotFactory = new NLPostflopMathBotFactory();
		IPlayer postflopMathBot = nlPostflopMathBotFactory.createBot(new VillainModel(LimitType.NO_LIMIT, DEBUG_PATH), ISpectrumListener.EMPTY, 
				IDecisionListener.EMPTY, IStudent.NONE, "MathBot", DEBUG_PATH);

		GusXensen gusXensen = new GusXensen();
		WritingStudent student = new WritingStudent("Cuba");
		NeuralAdvisor villainModel = new NeuralAdvisor(gusXensen, gusXensen, "GusXensen model");
		IPlayer fullMathBot = nlMathBotFactory.createBot(villainModel, ISpectrumListener.EMPTY,
				IDecisionListener.EMPTY, student, "Full MathBot", DEBUG_PATH);

		NeuralBotFactory factory = new NeuralBotFactory();
		IPlayer neuralStandart = factory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY, IDecisionListener.EMPTY,
				IStudent.NONE, "Grantorino Up", DEBUG_PATH);
		IPlayer neuralAggro = new NeuralAggroBotFactory().createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY,
				IDecisionListener.EMPTY, IStudent.NONE, "Grantorino Down", DEBUG_PATH);
		IPlayer random = new RandomBot("RandomBot", DEBUG_PATH);
		IPlayer pushBot = new PushBot("PushBot", DEBUG_PATH);
		PredefinedGameEngine engine = new PredefinedGameEngine(fullMathBot, neuralStandart, IGameObserver.EMPTY,
				DEBUG_PATH);
//		engine.button(fullMathBot).cards(fullMathBot, "TsTc").cards(neuralStandart, "KcAs").stack(fullMathBot, 1800)
//				.stack(neuralStandart, 1980).flop("KsKdJc");
		int count1 = 0, count2 = 0;
//		engine.playRound();
//		for (int i = 0; i < 0; i++) {
		for (int i = 0; i < 10; i++) {
			TournamentSummary summary = engine.playGame();
			if (summary.winner.equals(fullMathBot.getName())) {
				count1++;
			} else {
				count2++;
			}
			Log.d("Game # " + i + " has been played. Hands " + summary.handsCount);
			Log.d(count1 + " " + count2);
		}
		student.save();
		Log.d(count1 + " " + count2);
	}
}

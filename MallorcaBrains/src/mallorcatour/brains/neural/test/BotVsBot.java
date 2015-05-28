package mallorcatour.brains.neural.test;

import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.math.NLMathBotFactory;
import mallorcatour.bot.modeller.VillainModel;
import mallorcatour.bot.neural.NeuralAggroBotFactory;
import mallorcatour.bot.neural.NeuralBotFactory;
import mallorcatour.brains.IAdvisor;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.engine.GameEngine;
import mallorcatour.core.game.engine.GameEngine.TournamentSummary;
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.robot.controller.PokerPreferences;
import mallorcatour.util.DateUtils;
import mallorcatour.util.Log;

public class BotVsBot {

	public static void main(String... args) {
		String DEBUG_PATH = PokerPreferences.DEBUG_PATTERN + DateUtils.getDate(false) + ".txt";
		Log.DEBUG_PATH = DEBUG_PATH;
		NLMathBotFactory nlMathBotFactory = new NLMathBotFactory();
		IPlayer mathBot = nlMathBotFactory.createBot(new VillainModel(LimitType.NO_LIMIT, DEBUG_PATH), ISpectrumListener.EMPTY, 
				IDecisionListener.EMPTY, "MathBot", DEBUG_PATH);
		NeuralBotFactory factory = new NeuralBotFactory();
		IPlayer playerUp = factory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY, IDecisionListener.EMPTY,
				"Grantorino Up", DEBUG_PATH);
		IPlayer playerDown = new NeuralAggroBotFactory().createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY,
				IDecisionListener.EMPTY, "Grantorino Down", DEBUG_PATH);
		GameEngine engine = new GameEngine(mathBot, playerDown, IGameObserver.EMPTY, DEBUG_PATH);

		int count1 = 0, count2 = 0;
		for (int i = 0; i < 1000; i++) {
			TournamentSummary summary = engine.gameCycle();
			if (summary.winner.equals(playerUp.getName())) {
				count1++;
			} else {
				count2++;
			}
			Log.d("Game # " + i + " has been played. Hands " + summary.handsCount);
			Log.d(count1 + " " + count2);
		}
		Log.d(count1 + " " + count2);
	}
}

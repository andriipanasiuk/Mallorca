package mallorcatour.bot.neural;

import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.brains.IAdvisor;
import mallorcatour.core.game.engine.GameEngine;
import mallorcatour.core.game.engine.GameEngine.TournamentSummary;
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.robot.controller.PokerPreferences;
import mallorcatour.util.DateUtils;
import mallorcatour.util.Log;

public class BotVsBot {

	public static void main(String... args) {
		String DEBUG_PATH = PokerPreferences.DEBUG_PATTERN + DateUtils.getDate(false) + ".txt";
		NeuralBotFactory factory = new NeuralBotFactory();
		IPlayer playerUp = factory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY, IDecisionListener.EMPTY,
				"Grantorino Up", DEBUG_PATH);
		IPlayer playerDown = new NeuralBotFactory().createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY,
				IDecisionListener.EMPTY, "Grantorino Down", DEBUG_PATH);
		GameEngine engine = new GameEngine(playerDown, playerUp, IGameObserver.EMPTY, DEBUG_PATH);

		int count1 = 0, count2 = 0;
		for (int i = 0; i < 100; i++) {
			TournamentSummary summary = engine.gameCycle();
			if (summary.winner.equals(playerUp.getName())) {
				count1++;
			} else {
				count2++;
			}
			Log.d("Game # " + i + " has been played. Hands " + summary.handsCount);
		}
		Log.d(count1 + " " + count2);
	}
}

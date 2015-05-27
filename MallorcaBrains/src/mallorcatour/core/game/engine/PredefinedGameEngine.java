package mallorcatour.core.game.engine;

import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.OpenPlayerInfo;
import mallorcatour.core.game.interfaces.IGameObserver;

public class PredefinedGameEngine extends GameEngine {

	public IPlayer player;

	public PredefinedGameEngine(IPlayer player1, IPlayer player2, IGameObserver observer, String debug) {
		super(player1, player2, observer, debug);
	}

	@Override
	protected void dealButton(IPlayer player, OpenPlayerInfo playerInfo) {
		if (player == this.player) {
			playerInfo.isOnButton = true;
			otherThan(playerInfo).isOnButton = false;
		} else {
			super.dealButton(player, playerInfo);
		}
	}

	@Override
	public void dealCards(IPlayer player, OpenPlayerInfo playerInfo) {
		if (player == this.player) {
			HoleCards cards = HoleCards.valueOf("9s6c");
			playerInfo.setHoleCards(cards.first, cards.second);
			player.onHoleCards(cards.first, cards.second);
		} else {
			super.dealCards(player, playerInfo);
		}
	}
}
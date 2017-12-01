package mallorcatour.core.game.engine;

import java.util.ArrayList;
import java.util.List;

import mallorcatour.core.game.Card;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.OpenPlayerInfo;
import mallorcatour.core.game.PlayerInfo;
import mallorcatour.core.game.interfaces.HandEvaluator;
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.core.player.interfaces.Player;
import mallorcatour.tools.SimplePair;

public class PredefinedGameEngine extends GameEngine {

	private Player buttonPlayer;
	private List<SimplePair<Player, String>> predefinedCards = new ArrayList<>();
	private List<SimplePair<Player, Double>> predefinedStacks = new ArrayList<>();
	private String flop;

	public PredefinedGameEngine(Player player1, Player player2, IGameObserver observer,
								HandEvaluator equilator, String debug) {
		super(player1, player2, observer, equilator, debug);
	}

	public PredefinedGameEngine button(Player player) {
		this.buttonPlayer = player;
		return this;
	}

	@Override
	protected void setStartStack(Player player, PlayerInfo playerInfo) {
		for (SimplePair<Player, Double> pair : predefinedStacks) {
			if (player == pair.first) {
				playerInfo.stack = pair.second;
				return;
			}
		}
		super.setStartStack(player, playerInfo);
	}

	@Override
	protected void dealButton(int handNumber, Player player, OpenPlayerInfo playerInfo) {
		if (this.buttonPlayer != null) {
			playerInfo.isOnButton = (player == this.buttonPlayer);
			otherThan(playerInfo).isOnButton = (player != this.buttonPlayer);
		} else {
			super.dealButton(handNumber, player, playerInfo);
		}
	}

	public PredefinedGameEngine cards(Player player, String cards) {
		this.predefinedCards.add(new SimplePair<Player, String>(player, cards));
		return this;
	}

	public PredefinedGameEngine stack(Player player, double stack) {
		this.predefinedStacks.add(new SimplePair<Player, Double>(player, stack));
		return this;
	}

	@Override
	public void dealCards(Player player, OpenPlayerInfo playerInfo) {
		for (SimplePair<Player, String> card : predefinedCards) {
			if (player == card.first) {
				HoleCards cards = HoleCards.valueOf(card.second);
				playerInfo.setHoleCards(cards.first, cards.second);
				player.onHoleCards(cards.first, cards.second);
				return;
			}
		}
		super.dealCards(player, playerInfo);
	}

	public PredefinedGameEngine flop(String flop) {
		this.flop = flop;
		return this;
	}

	@Override
	protected void dealFlop() {
		if (flop == null) {
			super.dealFlop();
			return;
		}
		Card flop1 = Card.valueOf(flop.substring(0, 2));
		Card flop2 = Card.valueOf(flop.substring(2, 4));
		Card flop3 = Card.valueOf(flop.substring(4, 6));
		nonUsedCards.remove(flop1);
		nonUsedCards.remove(flop2);
		nonUsedCards.remove(flop3);
		boardCards.add(flop1);
		boardCards.add(flop2);
		boardCards.add(flop3);
	}

}
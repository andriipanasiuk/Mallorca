package mallorcatour.core.game.engine;

import java.util.ArrayList;
import java.util.List;

import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.Deck;
import mallorcatour.core.game.Flop;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.OpenPlayerInfo;
import mallorcatour.core.game.PlayerGameInfoAdapter;
import mallorcatour.core.game.PlayerInfo;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.interfaces.IRandomizer;
import mallorcatour.util.Log;
import mallorcatour.util.SimplePair;
import mallorcatour.util.UniformRandomizer;

public class GameEngine implements IGameInfo {

	public static interface EngineListener {
		void onPlayerActed(Action action, PlayerInfo player);

	}

	public static class ActionResult {
		public static final int START_ROUND = -1;
		public static final int END_OF_HAND = 0;
		public static final int NEXT_STAGE = 1;
		public static final int ANOTHER_PLAYER = 2;
		public static final int START_STREET = 3;
	}

	private EngineListener listener = null;

	private Flop predefinedFlop = null;
	private Card predefinedTurn = null;
	private Card predefinedRiver = null;

	private final double startingStack = 2000;
	private final static double BIG_BLIND = 20;
	private final IPlayer player1, player2;
	private String DEBUG_PATH = "hh.txt";
	private IRandomizer randomizer = new UniformRandomizer();

	private boolean tradeOpened;
	private long currentHandNumber = -1;
	private LimitType limitType;

	private double pot;
	private List<Card> nonUsedCards;
	private PokerStreet currentStreet;
	private List<Card> boardCards;
	private PlayerInfo lastMovePlayer;
	private OpenPlayerInfo playerInfo1, playerInfo2;

	public GameEngine(IPlayer player1, IPlayer player2, String debug) {
		this.player1 = player1;
		this.player2 = player2;
		this.DEBUG_PATH = debug;
		playerInfo1 = new OpenPlayerInfo(player1.getName(), startingStack);
		playerInfo2 = new OpenPlayerInfo(player2.getName(), startingStack - 300);
	}

	public void setLimitType(LimitType limitType) {
		this.limitType = limitType;
	}

	protected void dealButton(IPlayer player, PlayerInfo playerInfo) {
		boolean flip = currentHandNumber % 2 == 1;
		playerInfo.isOnButton = flip;
		otherThan(playerInfo).isOnButton = !flip;
	}

	public void deal() {
		tradeOpened = false;
		currentHandNumber++;
		currentStreet = PokerStreet.PREFLOP;
		nonUsedCards = new ArrayList<Card>(Deck.getCards());

		dealButton(player1, playerInfo1);

		playerInfo1.stack -= BIG_BLIND / 2;
		playerInfo2.stack -= BIG_BLIND / 2;

		if (playerInfo1.isOnButton()) {
			playerInfo2.stack -= BIG_BLIND / 2;
		} else {
			playerInfo1.stack -= BIG_BLIND / 2;
		}
		playerInfo1.bet = BIG_BLIND / 2 * (playerInfo1.isOnButton() ? 1 : 2);
		playerInfo2.bet = BIG_BLIND / 2 * (playerInfo2.isOnButton() ? 1 : 2);
		pot = playerInfo1.bet + playerInfo2.bet;
		playerInfo1.bet = playerInfo1.bet;
		playerInfo2.bet = playerInfo2.bet;

		boardCards = new ArrayList<Card>();

		player1.onHandStarted(new PlayerGameInfoAdapter(this, playerInfo1, playerInfo2));
		player2.onHandStarted(new PlayerGameInfoAdapter(this, playerInfo2, playerInfo1));

		dealCards(player1, playerInfo1);
		dealCards(player2, playerInfo2);
		gameCycle();
	}

	protected void dealCards(IPlayer player, OpenPlayerInfo playerInfo) {
		HoleCards player1Cards = dealHoleCards();
		playerInfo.setHoleCards(player1Cards.first, player1Cards.second);
		player.onHoleCards(player1Cards.first, player1Cards.second);
	}

	void dealFlop(String one, String two, String three) {
		Card flop1 = Card.valueOf(one);
		Card flop2 = Card.valueOf(two);
		Card flop3 = Card.valueOf(three);
		nonUsedCards.remove(flop1);
		nonUsedCards.remove(flop2);
		nonUsedCards.remove(flop3);
		boardCards.add(flop1);
		boardCards.add(flop2);
		boardCards.add(flop3);

	}

	private void dealFlop(Flop flop) {
		Card flop1 = flop.first;
		Card flop2 = flop.second;
		Card flop3 = flop.third;
		nonUsedCards.remove(flop1);
		nonUsedCards.remove(flop2);
		nonUsedCards.remove(flop3);
		boardCards.add(flop1);
		boardCards.add(flop2);
		boardCards.add(flop3);

	}

	protected HoleCards dealHoleCards() {
		Card card1 = nonUsedCards.remove(randomizer.getRandom(0, nonUsedCards.size()));
		Card card2 = nonUsedCards.remove(randomizer.getRandom(0, nonUsedCards.size()));
		return new HoleCards(card1, card2);
	}

	private void dealFlop() {
		Card flop1 = nonUsedCards.get(randomizer.getRandom(0, nonUsedCards.size()));
		nonUsedCards.remove(flop1);
		Card flop2 = nonUsedCards.get(randomizer.getRandom(0, nonUsedCards.size()));
		nonUsedCards.remove(flop2);
		Card flop3 = nonUsedCards.get(randomizer.getRandom(0, nonUsedCards.size()));
		nonUsedCards.remove(flop3);
		boardCards.add(flop1);
		boardCards.add(flop2);
		boardCards.add(flop3);
	}

	private void calculateWinner() {
		if (playerInfo1.bet == playerInfo2.bet) {
			List<Card> player1Cards = new ArrayList<Card>(boardCards);
			List<Card> player2Cards = new ArrayList<Card>(boardCards);
			player1Cards.add(playerInfo1.holeCard1);
			player1Cards.add(playerInfo1.holeCard2);
			player2Cards.add(playerInfo2.holeCard1);
			player2Cards.add(playerInfo2.holeCard2);
			long player1Combination = PokerEquilatorBrecher.combination(Card.convertToIntBrecherArray(player1Cards));
			long player2Combination = PokerEquilatorBrecher.combination(Card.convertToIntBrecherArray(player2Cards));
			if (player1Combination > player2Combination) {
				endOfHand(playerInfo1);
			} else if (player1Combination < player2Combination) {
				endOfHand(playerInfo2);
			}
		} else if (playerInfo1.bet > playerInfo2.bet) {
			endOfHand(playerInfo1);
		} else {
			endOfHand(playerInfo2);
		}
	}

	private void zeroBets() {
		playerInfo1.bet = 0;
		playerInfo2.bet = 0;
	}

	private int changeStreet() {
		tradeOpened = false;
		if (currentStreet == PokerStreet.RIVER) {
			return ActionResult.END_OF_HAND;
		} else if (currentStreet == PokerStreet.PREFLOP) {
			if (predefinedFlop != null) {
				dealFlop(predefinedFlop);
			} else {
				dealFlop();
			}
		} else if (currentStreet == PokerStreet.FLOP) {
			if (predefinedTurn != null) {
				dealOneCard(predefinedTurn);
			} else {
				dealOneCard();
			}
		} else {
			if (predefinedRiver != null) {
				dealOneCard(predefinedRiver);
			} else {
				dealOneCard();
			}
		}
		currentStreet = currentStreet.next();
		zeroBets();
		return ActionResult.START_STREET;
	}

	protected IPlayer otherThan(IPlayer player) {
		if (player == player1) {
			return player2;
		} else {
			return player1;
		}
	}

	protected PlayerInfo otherThan(PlayerInfo player) {
		if (player == playerInfo1) {
			return playerInfo2;
		} else {
			return playerInfo1;
		}
	}

	private int playerAction(IPlayer player, PlayerInfo playerInfo) {
		PlayerInfo other = otherThan(playerInfo);
		double toCall = other.bet - playerInfo.bet;
		Action action = player.getAction();
		if (action.isAggressive() && action.getAmount() <= 0) {
			action.setAmount(Math.max(BIG_BLIND, toCall));
		}
		if (action.isAggressive() && action.getAmount() > playerInfo.stack - toCall) {
			action.setAmount(playerInfo.stack - toCall);
		}
		int result = playerActed(action, playerInfo);
		player.onActed(action, toCall, player.getName());
		otherThan(player).onActed(action, toCall, player.getName());
		if (listener != null) {
			listener.onPlayerActed(action, playerInfo);
		}
		return result;
	}

	private void dealOneCard() {
		Card card = nonUsedCards.get(randomizer.getRandom(0, nonUsedCards.size()));
		nonUsedCards.remove(card);
		boardCards.add(card);
	}

	void dealOneCard(String card) {
		Card c = Card.valueOf(card);
		nonUsedCards.remove(c);
		boardCards.add(c);
	}

	private void dealOneCard(Card c) {
		nonUsedCards.remove(c);
		boardCards.add(c);
	}

	private void endOfHand(PlayerInfo winner) {
		if (winner != null) {
			Log.d("End of hand. Winner: " + winner.name);
			winner.stack += pot;
		}
		pot = 0;
		zeroBets();
		player1.onHandEnded();
		player2.onHandEnded();
	}

	private SimplePair<IPlayer, PlayerInfo> getPlayer(boolean onButton) {
		if (playerInfo1.isOnButton() ^ !onButton) {
			return new SimplePair<IPlayer, PlayerInfo>(player1, playerInfo1);
		} else {
			return new SimplePair<IPlayer, PlayerInfo>(player2, playerInfo2);
		}
	}

	private void gameCycle() {
		int result = ActionResult.START_ROUND;
		while (result != ActionResult.END_OF_HAND) {
			if (result == ActionResult.START_ROUND) {
				SimplePair<IPlayer, PlayerInfo> button = getPlayer(true);
				result = playerAction(button.first, button.second);
			} else if (result == ActionResult.NEXT_STAGE) {
				result = changeStreet();
				if (result != ActionResult.END_OF_HAND) {
					player1.onStageEvent(currentStreet);
					player2.onStageEvent(currentStreet);
				}
			} else if (result == ActionResult.START_STREET) {
				if (getBankRollAtRisk() > 0) {
					SimplePair<IPlayer, PlayerInfo> button = getPlayer(false);
					result = playerAction(button.first, button.second);
				} else {
					result = ActionResult.NEXT_STAGE;
				}
			} else if (result == ActionResult.ANOTHER_PLAYER) {
				if (lastMovePlayer == playerInfo1) {
					result = playerAction(player2, playerInfo2);
				} else {
					result = playerAction(player1, playerInfo1);
				}
			}
		}
		calculateWinner();
	}

	private int playerActed(Action action, PlayerInfo playerInfo) {
		lastMovePlayer = playerInfo;
		Log.f(DEBUG_PATH, playerInfo.name + " " + action.getActString());
		double toCall = otherThan(playerInfo).bet - playerInfo.bet;
		if (action.isFold()) {
			return ActionResult.END_OF_HAND;
		} else if (action.isPassive()) {
			pot += action.getAmount();
			if (action.getAmount() <= playerInfo1.stack) {
				playerInfo.bet += action.getAmount();
				playerInfo.stack -= action.getAmount();
			} else {
				playerInfo.bet += playerInfo.stack;
				playerInfo.stack = 0;
			}
			if (tradeOpened) {
				return ActionResult.NEXT_STAGE;
			} else {
				tradeOpened = true;
				return ActionResult.ANOTHER_PLAYER;
			}
		} else if (action.isAggressive()) {
			double plusAmount = (toCall) + action.getAmount();
			playerInfo.bet += plusAmount;
			pot += plusAmount;
			playerInfo.stack -= plusAmount;
			tradeOpened = true;
			return ActionResult.ANOTHER_PLAYER;
		} else {
			Log.f(DEBUG_PATH, "Incorrect action: " + action);
			throw new RuntimeException("Incorrect action: " + action);
		}
	}

	@Override
	public double getBigBlindSize() {
		return BIG_BLIND;
	}

	@Override
	public PokerStreet getStage() {
		return currentStreet;
	}

	@Override
	public boolean isPreFlop() {
		return currentStreet == PokerStreet.PREFLOP;
	}

	@Override
	public boolean isPostFlop() {
		return currentStreet != PokerStreet.PREFLOP;
	}

	@Override
	public boolean isFlop() {
		return currentStreet == PokerStreet.FLOP;
	}

	@Override
	public boolean isTurn() {
		return currentStreet == PokerStreet.TURN;
	}

	@Override
	public boolean isRiver() {
		return currentStreet == PokerStreet.RIVER;
	}

	@Override
	public List<Card> getBoard() {
		return boardCards;
	}

	@Override
	public Flop getFlop() {
		if (boardCards.size() < 3) {
			return Flop.empty();
		}
		return new Flop(boardCards.get(0), boardCards.get(1), boardCards.get(2));
	}

	@Override
	public Card getTurn() {
		return boardCards.size() >= 4 ? boardCards.get(3) : null;
	}

	@Override
	public Card getRiver() {
		return boardCards.size() == 5 ? boardCards.get(4) : null;
	}

	@Override
	public double getPotSize() {
		return pot;
	}

	@Override
	public double getBankRollAtRisk() {
		double betDifference = playerInfo1.bet - playerInfo2.bet;
		if (betDifference > 0) {
			return Math.min(playerInfo1.stack, playerInfo2.stack - betDifference);
		} else {
			return Math.min(playerInfo1.stack - (-betDifference), playerInfo2.stack);
		}
	}

	@Override
	public int getNumRaises() {
		throw new UnsupportedOperationException();
	}

	@Override
	public LimitType getLimitType() {
		return limitType;
	}

	public void setListener(EngineListener listener) {
		this.listener = listener;
	}

	@Override
	public boolean onButton(String name) {
		if (name.equals(playerInfo1.name)) {
			return playerInfo1.isOnButton;
		} else {
			return playerInfo2.isOnButton;
		}
	}

}
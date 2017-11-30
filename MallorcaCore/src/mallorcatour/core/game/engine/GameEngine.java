package mallorcatour.core.game.engine;

import java.util.ArrayList;
import java.util.List;

import mallorcatour.core.game.interfaces.HandEvaluator;
import mallorcatour.core.player.interfaces.IPlayer;
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
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.tools.IRandomizer;
import mallorcatour.tools.Log;
import mallorcatour.tools.SimplePair;
import mallorcatour.tools.UniformRandomizer;

public class GameEngine implements IGameInfo {

	public static int BLINDS_CHANGE = 10;

	public static class ActionResult {
		public static final int START_ROUND = -1;
		public static final int SHOWDOWN = 0;
		public static final int TRADE_FINISHED = 1;
		public static final int ANOTHER_PLAYER = 2;
		public static final int START_TRADING = 3;
		public static final int UNCONTESTED = 4;
	}

	private IGameObserver gameObserver;

	private final double startingStack = 2000;
	private final double START_BIG_BLIND = 20;
	private final double[] BLINDS = new double[] { 20, 30, 40, 50, 60, 80, 100, 120, 160, 200, 240, 300, 400, 500, 600,
			800, 1000, 1200, 1600, 2000, 2400 };
	private double BIG_BLIND = 20;
	private final IPlayer player1, player2;
	private String DEBUG_PATH = "hh.txt";
	private IRandomizer randomizer = new UniformRandomizer();

	private int firstButtonFlip;

	private boolean tradeOpened;
	private LimitType limitType;

	private double[] potOnStreet = new double[4];
	private double pot;
	protected List<Card> nonUsedCards;
	protected List<Card> boardCards;
	private PokerStreet currentStreet;
	private PlayerInfo lastMovePlayer;
	private OpenPlayerInfo playerInfo1, playerInfo2;
	private final HandEvaluator equilator;

	public GameEngine(IPlayer player1, IPlayer player2, IGameObserver observer, HandEvaluator equilator, String debug) {
		this.player1 = player1;
		this.player2 = player2;
		this.gameObserver = observer;
		this.equilator = equilator;
		this.DEBUG_PATH = debug;
		playerInfo1 = new OpenPlayerInfo(player1.getName());
		playerInfo2 = new OpenPlayerInfo(player2.getName(), startingStack);
	}

	public void setLimitType(LimitType limitType) {
		this.limitType = limitType;
	}

	protected void setStartStack(IPlayer player, PlayerInfo playerInfo) {
		playerInfo.stack = startingStack;
	}

	protected void dealButton(int handNumber, IPlayer player, OpenPlayerInfo playerInfo) {
		boolean flip = handNumber % 2 == firstButtonFlip;
		playerInfo.isOnButton = flip;
		otherThan(playerInfo).isOnButton = !flip;
	}

	private HandSummary deal(int handNumber, boolean changeBlind) {
		tradeOpened = false;
		if (changeBlind) {
			BIG_BLIND = BLINDS[(handNumber - 1) / BLINDS_CHANGE];
		}else{
			BIG_BLIND = BLINDS[0];
		}

		currentStreet = PokerStreet.PREFLOP;
		nonUsedCards = new ArrayList<Card>(Deck.getCards());

		dealButton(handNumber, player2, playerInfo2);


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
		gameObserver.onHandStarted(this);

		dealCards(player1, playerInfo1);
		dealCards(player2, playerInfo2);

		Log.f(DEBUG_PATH, "*************************");
		Log.f(DEBUG_PATH, playerInfo1.name + " " + playerInfo1.stack + (playerInfo1.isOnButton ? " *" : "")
				+ " " + playerInfo1.holeCard1 + " " + playerInfo1.holeCard2);
		Log.f(DEBUG_PATH, playerInfo2.name + " " + playerInfo2.stack + (playerInfo2.isOnButton ? " *" : "")
				+ " " + playerInfo2.holeCard1 + " " + playerInfo2.holeCard2);
		Log.f(DEBUG_PATH, "");
		Log.f(DEBUG_PATH, playerInfo1.name + " posts " + (playerInfo1.isOnButton ? BIG_BLIND / 2 : BIG_BLIND));
		Log.f(DEBUG_PATH, playerInfo2.name + " posts " + (playerInfo2.isOnButton ? BIG_BLIND / 2 : BIG_BLIND));
		Log.f(DEBUG_PATH, "");

		return roundCycle();
	}

	public static class TournamentSummary {
		public String winner;
		public int handsCount;
	}

	public static class CashSummary {
		public String winner;
		public double gain;
	}

	public static class HandSummary {
		public String winner;
		public double payout;
	}

	public CashSummary playCash(int handCount) {
		firstButtonFlip = randomizer.getRandom(0, 2);
		String winner = null;
		double gain = 0;
		for (int i = 0; i < handCount; i++) {
			setStartStack(player1, playerInfo1);
			setStartStack(player2, playerInfo2);
			HandSummary handSummary = deal(i + 1, false);
			if (winner != null) {
				if (handSummary.winner != null) {
					if (handSummary.winner.equals(winner)) {
						gain += handSummary.payout;
					} else {
						gain -= handSummary.payout;
					}
				}
			} else {
				winner = handSummary.winner;
				gain += handSummary.payout;
			}
		}
		CashSummary summary = new CashSummary();
		summary.winner = winner;
		summary.gain = gain;
		return summary;
	}

	public TournamentSummary playGame() {
		firstButtonFlip = randomizer.getRandom(0, 2);
		BIG_BLIND = START_BIG_BLIND;
		setStartStack(player1, playerInfo1);
		setStartStack(player2, playerInfo2);
		int handNumber = 1;
		while (playerInfo1.stack > 0 && playerInfo2.stack > 0) {
			deal(handNumber++, true);
		}
		if (playerInfo1.stack != 4000 && playerInfo2.stack != 4000) {
			Log.d(playerInfo1.stack + " " + playerInfo2.stack);
		}
		TournamentSummary result = new TournamentSummary();
		if (playerInfo1.stack > playerInfo2.stack) {
			result.winner = playerInfo1.name;
		} else {
			result.winner = playerInfo2.name;
		}
		result.handsCount = handNumber;
		return result;
	}

	protected void dealCards(IPlayer player, OpenPlayerInfo playerInfo) {
		HoleCards player1Cards = dealHoleCards();
		playerInfo.setHoleCards(player1Cards.first, player1Cards.second);
		player.onHoleCards(player1Cards.first, player1Cards.second);
	}

	protected HoleCards dealHoleCards() {
		Card card1 = nonUsedCards.remove(randomizer.getRandom(0, nonUsedCards.size()));
		Card card2 = nonUsedCards.remove(randomizer.getRandom(0, nonUsedCards.size()));
		return new HoleCards(card1, card2);
	}

	protected void dealFlop() {
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

	private HandSummary calculateWinner(int result) {
		if (result == ActionResult.SHOWDOWN) {
			List<Card> player1Cards = new ArrayList<Card>(boardCards);
			List<Card> player2Cards = new ArrayList<Card>(boardCards);
			player1Cards.add(playerInfo1.holeCard1);
			player1Cards.add(playerInfo1.holeCard2);
			player2Cards.add(playerInfo2.holeCard1);
			player2Cards.add(playerInfo2.holeCard2);
			long player1Combination = equilator.evaluate(Card.convertToIntBrecherArray(player1Cards));
			long player2Combination = equilator.evaluate(Card.convertToIntBrecherArray(player2Cards));
			if (player1Combination > player2Combination) {
				return endOfHand(playerInfo1, result);
			} else if (player1Combination < player2Combination) {
				return endOfHand(playerInfo2, result);
			} else {
				return endOfHand(null, result);
			}
		} else if (result == ActionResult.UNCONTESTED && lastMovePlayer == playerInfo2) {
			return endOfHand(playerInfo1, result);
		} else if (result == ActionResult.UNCONTESTED) {
			return endOfHand(playerInfo2, result);
		} else {
			throw new RuntimeException("Shouldn't be");
		}
	}

	private void zeroBets() {
		playerInfo1.bet = 0;
		playerInfo2.bet = 0;
	}

	private int changeStreet() {
		tradeOpened = false;
		if (currentStreet == PokerStreet.RIVER) {
			return ActionResult.SHOWDOWN;
		} else if (currentStreet == PokerStreet.PREFLOP) {
			dealFlop();
		} else if (currentStreet == PokerStreet.FLOP) {
			dealOneCard();
		} else {
			dealOneCard();
		}
		Log.f(DEBUG_PATH, "");
		Log.f(DEBUG_PATH, "Board: " + boardCards + " Pot: " + pot);
		potOnStreet[currentStreet.intValue()] = pot;
		currentStreet = currentStreet.next();
		zeroBets();
		return ActionResult.START_TRADING;
	}

	protected IPlayer otherThan(IPlayer player) {
		if (player == player1) {
			return player2;
		} else {
			return player1;
		}
	}

	protected OpenPlayerInfo otherThan(OpenPlayerInfo player) {
		if (player == playerInfo1) {
			return playerInfo2;
		} else {
			return playerInfo1;
		}
	}

	private int playerAction(IPlayer player, OpenPlayerInfo playerInfo) {
		PlayerInfo other = otherThan(playerInfo);
		double toCall = Math.min(other.bet - playerInfo.bet, playerInfo.stack);
		Action action = player.getAction();
		if (action.isAggressive() && action.getAmount() <= 0) {
			action.setAmount(toCall);
		}
		if (action.isAggressive() && getBankRollAtRisk() <= 0) {
			action = Action.callAction(toCall);
		}
		if (action.isAggressive() && action.getAmount() > getBankRollAtRisk()) {
			action.setAmount(getBankRollAtRisk());
		}
		int result = playerActed(action, playerInfo);
		otherThan(player).onActed(action, toCall, player.getName());
		gameObserver.onActed(action, -1, player.getName());
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

	private HandSummary endOfHand(OpenPlayerInfo winner, int actionResult) {
		Log.f(DEBUG_PATH, playerInfo1.name + " " + playerInfo1.holeCard1 + " " + playerInfo1.holeCard2);
		Log.f(DEBUG_PATH, playerInfo2.name + " " + playerInfo2.holeCard1 + " " + playerInfo2.holeCard2);
		HandSummary result = new HandSummary();
		if (winner != null) {
			if (actionResult == ActionResult.UNCONTESTED) {
				result.payout = (pot - lastToCall) / 2;
			} else {
				result.payout = pot / 2;
			}
			result.winner = winner.name;
			winner.stack += pot;
			Log.f(DEBUG_PATH, winner.name + " wins " + pot);
		} else {
			result.payout = 0;
			playerInfo1.stack += pot / 2;
			playerInfo2.stack += pot / 2;
			Log.f(DEBUG_PATH, "Draw Pot: " + pot);
		}
		Log.f(DEBUG_PATH, "");
		pot = 0;
		zeroBets();
		player1.onHandEnded();
		player2.onHandEnded();
		gameObserver.onHandEnded();
		return result;
	}

	private SimplePair<IPlayer, OpenPlayerInfo> getPlayer(boolean onButton) {
		if (playerInfo1.isOnButton() ^ !onButton) {
			return new SimplePair<IPlayer, OpenPlayerInfo>(player1, playerInfo1);
		} else {
			return new SimplePair<IPlayer, OpenPlayerInfo>(player2, playerInfo2);
		}
	}

	private HandSummary roundCycle() {
		int result = ActionResult.START_ROUND;
		while (result != ActionResult.SHOWDOWN && result != ActionResult.UNCONTESTED) {
			if (result == ActionResult.START_ROUND) {
				SimplePair<IPlayer, OpenPlayerInfo> actingPlayer = getPlayer(true);
				result = playerAction(actingPlayer.first, actingPlayer.second);
			} else if (result == ActionResult.TRADE_FINISHED) {
				result = changeStreet();
				if (result != ActionResult.SHOWDOWN) {
					player1.onStageEvent(currentStreet);
					player2.onStageEvent(currentStreet);
					gameObserver.onStageEvent(currentStreet);
				}
			} else if (result == ActionResult.START_TRADING) {
				if (getBankRollAtRisk() > 0) {
					SimplePair<IPlayer, OpenPlayerInfo> actingPlayer = getPlayer(false);
					result = playerAction(actingPlayer.first, actingPlayer.second);
				} else {
					result = ActionResult.TRADE_FINISHED;
				}
			} else if (result == ActionResult.ANOTHER_PLAYER) {
				if (lastMovePlayer == playerInfo1) {
					result = playerAction(player2, playerInfo2);
				} else {
					result = playerAction(player1, playerInfo1);
				}
			}
		}
		return calculateWinner(result);
	}

	double lastToCall;

	private int playerActed(Action action, OpenPlayerInfo playerInfo) {
		lastMovePlayer = playerInfo;
		Log.f(DEBUG_PATH, playerInfo.name + " " + action.getActString());
		double toCall = otherThan(playerInfo).bet - playerInfo.bet;
		lastToCall = toCall;
		if (action.isFold()) {
			return ActionResult.UNCONTESTED;
		} else if (action.isPassive()) {
			pot += action.getAmount();
			if (action.getAmount() <= playerInfo.stack) {
				playerInfo.bet += action.getAmount();
				playerInfo.stack -= action.getAmount();
			} else {
				playerInfo.bet += playerInfo.stack;
				playerInfo.stack = 0;
			}
			if (tradeOpened) {
				return ActionResult.TRADE_FINISHED;
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

	@Override
	public boolean onButton(String name) {
		if (name.equals(playerInfo1.name)) {
			return playerInfo1.isOnButton;
		} else if (name.equals(playerInfo2.name)) {
			return playerInfo2.isOnButton;
		} else {
			throw new RuntimeException("Wrong name: " + name);
		}
	}

	@Override
	public PlayerInfo getHero(String hero) {
		return playerInfo1.name.equals(hero) ? playerInfo1 : playerInfo2;
	}

	@Override
	public PlayerInfo getVillain(String hero) {
		return playerInfo1.name.equals(hero) ? playerInfo2 : playerInfo1;
	}

	@Override
	public double getAmountToCall(String hero) {
		return Math.max(0, getVillain(hero).bet - getHero(hero).bet);
	}

	@Override
	public double getPot(PokerStreet street) {
		return potOnStreet[street.intValue()];
	}

}
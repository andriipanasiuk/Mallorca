package golden;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mallorcatour.bot.Bot;
import mallorcatour.bot.math.ActionDistribution;
import mallorcatour.bot.math.EVAdvisor;
import mallorcatour.bot.math.IProfitCalculator;
import mallorcatour.bot.math.NLMathBotFactory;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.PlayerInfo;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.engine.GameEngine;
import mallorcatour.core.game.interfaces.GameContext;
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.core.math.RandomVariable;
import mallorcatour.core.player.interfaces.Player;
import mallorcatour.equilator.PokerEquilatorBrecher;
import mallorcatour.tools.UniformRandomizer;

final class DecisionHarness implements IGameObserver {

	static final String BOT = "MathBot";
	static final String OPPONENT = "Scripted";
	static final String[] STREETS = { "preflop", "flop", "turn", "river" };

	final List<Object> decisions = new ArrayList<>();
	final Map<String, Integer> decisionsByStreet = new LinkedHashMap<>();
	final Map<String, Integer> actionClasses = new LinkedHashMap<>();
	final Map<String, Integer> chosenKinds = new LinkedHashMap<>();
	int gamesPlayed;
	int handsPlayed;
	int handsDropped;
	int decisionsDropped;
	int illegalChosen;
	int gamesStoppedByBlindSchedule;
	final List<String> notes = new ArrayList<>();

	private GameEngine engine;
	private ShadowHand shadow;
	private List<Object> observations;
	private List<Object> pending;
	private boolean handValid;
	private int handNumber;
	private int seed;
	private ActionDistribution captured;

	DecisionHarness() {
		for (String street : STREETS) {
			decisionsByStreet.put(street, 0);
		}
	}

	ShadowHand shadow() {
		return shadow;
	}

	void playGame(int seed) throws ReflectiveOperationException {
		this.seed = seed;
		this.handNumber = 0;
		Field random = UniformRandomizer.class.getDeclaredField("random");
		random.setAccessible(true);
		random.set(null, new Random(seed));
		NLMathBotFactory factory = new NLMathBotFactory(new UniformRandomAdvisor());
		Player bot = factory.createBot(AdvisorListener.NONE, AdvisorListener.NONE, BOT, "");
		captureProfitMaps(bot);
		Player recordingBot = new RecordingBot(bot);
		Player opponent = new ScriptedOpponent(OPPONENT, 1_000_003L * seed, this);
		engine = new GameEngine(recordingBot, opponent, this, new PokerEquilatorBrecher(), "");
		try {
			engine.playGame();
		} catch (ArrayIndexOutOfBoundsException e) {
			gamesStoppedByBlindSchedule++;
		}
		gamesPlayed++;
	}

	boolean enough(int perStreet, int total) {
		for (int count : decisionsByStreet.values()) {
			if (count < perStreet) {
				return false;
			}
		}
		return decisions.size() >= total;
	}

	private void captureProfitMaps(Player bot) throws ReflectiveOperationException {
		Field advisorField = Bot.class.getDeclaredField("postflopAdvisor");
		advisorField.setAccessible(true);
		EVAdvisor advisor = (EVAdvisor) advisorField.get(bot);
		Field calculatorField = EVAdvisor.class.getDeclaredField("profitCalculator");
		calculatorField.setAccessible(true);
		IProfitCalculator original = (IProfitCalculator) calculatorField.get(advisor);
		calculatorField.set(advisor, (IProfitCalculator) (gameInfo, situation, card1, card2, spectrum) -> {
			ActionDistribution distribution = original.getProfitMap(gameInfo, situation, card1, card2, spectrum);
			captured = distribution;
			return distribution;
		});
	}

	private static long chips(double value) {
		if (value != Math.rint(value)) {
			throw new NonIntegerChips(value);
		}
		return (long) value;
	}

	private static String street(PokerStreet street) {
		return STREETS[street.intValue()];
	}

	private static List<Object> cards(List<Card> cards) {
		List<Object> result = new ArrayList<>();
		for (Card card : cards) {
			result.add(card.toString());
		}
		return result;
	}

	private int seatOf(String name) {
		return BOT.equals(name) ? 0 : 1;
	}

	@Override
	public void onHandStarted(GameContext gameInfo) {
		handNumber++;
		handsPlayed++;
		observations = new ArrayList<>();
		pending = new ArrayList<>();
		handValid = true;
		PlayerInfo hero = engine.getHero(BOT);
		PlayerInfo villain = engine.getHero(OPPONENT);
		try {
			long bigBlind = chips(engine.getBigBlindSize());
			long heroStack = chips(hero.stack + hero.bet);
			long villainStack = chips(villain.stack + villain.bet);
			boolean heroOnButton = engine.onButton(BOT);
			shadow = new ShadowHand(bigBlind, heroStack, villainStack);
			if (heroStack < bigBlind || villainStack < bigBlind) {
				handValid = false;
			}
			int button = heroOnButton ? 0 : 1;
			shadow.put(button, bigBlind / 2);
			shadow.put(1 - button, bigBlind);
			observations.add(Json.object("type", "handStarted", "handNumber", handNumber, "heroOnButton", heroOnButton,
					"heroStack", heroStack, "villainStack", villainStack, "smallBlind", bigBlind / 2, "bigBlind",
					bigBlind));
			checkConsistency();
		} catch (NonIntegerChips e) {
			handValid = false;
			shadow = new ShadowHand(1, 0, 0);
		}
	}

	void onHeroCards(Card c1, Card c2) {
		observations.add(Json.object("type", "holeCards", "cards", Json.array(c1.toString(), c2.toString())));
	}

	@Override
	public void onStageEvent(PokerStreet street) {
		shadow.newStreet();
		observations.add(Json.object("type", "street", "street", street(street), "board", cards(engine.getBoard())));
	}

	@Override
	public void onActed(Action action, double toCall, String name) {
		if (!handValid) {
			return;
		}
		int seat = seatOf(name);
		try {
			PlayerInfo info = engine.getHero(name);
			long betAfter = chips(info.bet);
			long before = shadow.bets[seat];
			Map<String, Object> converted;
			long amount;
			if (action.isFold()) {
				converted = Json.object("kind", "fold");
				amount = 0;
			} else if (action.isAggressive()) {
				converted = Json.object("kind", "raise", "to", betAfter);
				amount = betAfter - before;
				if (!shadow.isLegal(seat, "raise", betAfter)) {
					handValid = false;
					notes.add("seed " + seed + " hand " + handNumber + ": " + name + " raise to " + betAfter
							+ " is not legal in the TS engine");
				}
				shadow.raiseTo(seat, betAfter);
			} else if (shadow.toCall(seat) == 0) {
				converted = Json.object("kind", "check");
				amount = 0;
			} else {
				converted = Json.object("kind", "call");
				amount = Math.min(shadow.toCall(seat), shadow.stacks[seat]);
				shadow.put(seat, amount);
			}
			observations.add(Json.object("type", "acted", "who", seat == 0 ? "hero" : "villain", "action", converted,
					"amount", amount));
			checkConsistency();
		} catch (NonIntegerChips e) {
			handValid = false;
		}
	}

	private void checkConsistency() {
		PlayerInfo hero = engine.getHero(BOT);
		PlayerInfo villain = engine.getHero(OPPONENT);
		boolean same = shadow.stacks[0] == hero.stack && shadow.stacks[1] == villain.stack
				&& shadow.bets[0] == hero.bet && shadow.bets[1] == villain.bet && shadow.pot() == engine.getPotSize();
		if (!same && handValid) {
			handValid = false;
			notes.add("seed " + seed + " hand " + handNumber + ": Java engine state " + hero.stack + "/" + hero.bet
					+ " " + villain.stack + "/" + villain.bet + " pot " + engine.getPotSize()
					+ " differs from the TS model " + shadow.stacks[0] + "/" + shadow.bets[0] + " " + shadow.stacks[1]
					+ "/" + shadow.bets[1] + " pot " + shadow.pot());
		}
	}

	@Override
	public void onHandEnded() {
		if (handValid) {
			for (Object decision : pending) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) decision;
				@SuppressWarnings("unchecked")
				Map<String, Object> java = (Map<String, Object>) map.get("java");
				String street = (String) java.get("street");
				decisionsByStreet.put(street, decisionsByStreet.get(street) + 1);
				@SuppressWarnings("unchecked")
				Map<String, Object> ev = (Map<String, Object>) map.get("evByAction");
				StringBuilder key = new StringBuilder(street);
				for (Map.Entry<String, Object> entry : ev.entrySet()) {
					if (entry.getValue() != null) {
						key.append(' ').append(entry.getKey());
					}
				}
				increment(actionClasses, key.toString());
				@SuppressWarnings("unchecked")
				Map<String, Object> chosen = (Map<String, Object>) map.get("chosen");
				increment(chosenKinds, street + " " + chosen.get("kind"));
				map.put("id", "s" + seed + "-h" + map.remove("hand") + "-d" + map.remove("index"));
				decisions.add(reorder(map));
			}
		} else {
			handsDropped++;
			decisionsDropped += pending.size();
		}
	}

	private static Map<String, Object> reorder(Map<String, Object> map) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (String key : new String[] { "id", "observations", "legal", "evByAction", "chosen", "java" }) {
			result.put(key, map.get(key));
		}
		return result;
	}

	private static void increment(Map<String, Integer> map, String key) {
		Integer value = map.get(key);
		map.put(key, value == null ? 1 : value + 1);
	}

	Action decide(Player bot) {
		Map<String, Object> legal = shadow.legal(0);
		List<Object> seen = new ArrayList<>(observations);
		String street = street(engine.getStage());
		Map<String, Object> java = Json.object("street", street, "pot", engine.getPotSize(), "amountToCall",
				engine.getAmountToCall(BOT), "bankRollAtRisk", engine.getBankRollAtRisk(), "bigBlind",
				engine.getBigBlindSize());
		captured = null;
		Action action = bot.getAction();
		if (captured == null) {
			throw new IllegalStateException("The EV advisor did not compute a profit map");
		}
		Map<String, Object> ev = Json.object("fold", null, "passive", null, "aggressive", null);
		for (Map.Entry<Action, RandomVariable> entry : captured.entrySet()) {
			Action key = entry.getKey();
			String kind = key.isFold() ? "fold" : key.isPassive() ? "passive" : "aggressive";
			if (ev.get(kind) != null) {
				throw new IllegalStateException("Two actions of class " + kind);
			}
			ev.put(kind, entry.getValue().getEV());
			if (key.isAggressive()) {
				java.put("aggressiveAmount", key.getAmount());
			}
		}
		Map<String, Object> chosen;
		long to = 0;
		String kind;
		if (action.isFold()) {
			kind = "fold";
			chosen = Json.object("kind", kind);
		} else if (action.isPassive()) {
			kind = action.getAmount() == 0 ? "check" : "call";
			chosen = Json.object("kind", kind);
		} else {
			kind = "raise";
			to = shadow.bets[1] + Math.round(action.getAmount());
			if (action.getAmount() != Math.rint(action.getAmount())) {
				notes.add("seed " + seed + " hand " + handNumber + ": raise amount " + action.getAmount()
						+ " is not an integer");
			}
			chosen = Json.object("kind", kind, "to", to);
		}
		java.put("javaAction", action.toString());
		if (shadow.isLegal(0, kind, to) && handValid) {
			Map<String, Object> decision = Json.object("hand", handNumber, "index", pending.size() + 1,
					"observations", seen, "legal", legal, "evByAction", ev, "chosen", chosen, "java", java);
			pending.add(decision);
		} else if (handValid) {
			illegalChosen++;
			notes.add("seed " + seed + " hand " + handNumber + ": chosen " + kind + (kind.equals("raise") ? " to " + to
					: "") + " is not legal in the TS engine, legal " + legal);
		}
		return action;
	}

	private final class RecordingBot implements Player {
		private final Player bot;

		RecordingBot(Player bot) {
			this.bot = bot;
		}

		@Override
		public Action getAction() {
			return decide(bot);
		}

		@Override
		public String getName() {
			return bot.getName();
		}

		@Override
		public void onHandStarted(GameContext gameInfo) {
			bot.onHandStarted(gameInfo);
		}

		@Override
		public void onStageEvent(PokerStreet street) {
			bot.onStageEvent(street);
		}

		@Override
		public void onActed(Action action, double toCall, String name) {
			bot.onActed(action, toCall, name);
		}

		@Override
		public void onHandEnded() {
			bot.onHandEnded();
		}

		@Override
		public void onHoleCards(Card c1, Card c2) {
			bot.onHoleCards(c1, c2);
			onHeroCards(c1, c2);
		}
	}

	private static final class NonIntegerChips extends RuntimeException {
		private static final long serialVersionUID = 1L;

		NonIntegerChips(double value) {
			super("Not an integer chip amount: " + value);
		}
	}
}

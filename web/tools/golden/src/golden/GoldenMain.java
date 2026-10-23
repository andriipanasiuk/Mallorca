package golden;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mallorcatour.core.game.Card;
import mallorcatour.core.game.Deck;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.state.StreetEquity;
import mallorcatour.core.spectrum.Spectrum;
import mallorcatour.equilator.PokerEquilatorBrecher;
import mallorcatour.equilator.preflop.PreflopEquilatorImpl;
import mallorcatour.equilator.preflop.PreflopStrengthTable;

public final class GoldenMain {

	private static final int HAND_CLASSES = 169;
	private static final double UNUSED = -1;
	private static final int EQUITY_ENTRIES = 100;
	private static final long EQUITY_SEED = 20261023L;
	private static final double[] WEIGHTS = { 0.25, 0.5, 1 };
	private static final int MIN_DECISIONS_PER_STREET = 40;
	private static final int MIN_DECISIONS = 200;
	private static final int MAX_GAMES = 500;

	private GoldenMain() {
	}

	public static void main(String[] args) throws Exception {
		Path web = Paths.get(args[0]).toAbsolutePath().normalize();
		PrintStream report = System.err;
		System.setOut(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) {
			}
		}));

		Json.write(web.resolve("src/brains/fixtures/preflop-strength-java.json"), preflopTable(report));
		report.println("preflop-strength-java.json: " + HAND_CLASSES + "x" + HAND_CLASSES);

		List<Object> equity = equityEntries();
		Json.write(web.resolve("src/equity/fixtures/equity.json"), equity);
		report.println("equity.json: " + equity.size() + " entries");

		DecisionHarness harness = new DecisionHarness();
		int seed = 1;
		while (!harness.enough(MIN_DECISIONS_PER_STREET, MIN_DECISIONS)) {
			if (seed > MAX_GAMES) {
				throw new IllegalStateException("Not enough decisions after " + MAX_GAMES + " games");
			}
			harness.playGame(seed++);
		}
		Json.write(web.resolve("src/brains/fixtures/decisions.json"), harness.decisions);
		report.println("decisions.json: " + harness.decisions.size() + " decisions " + harness.decisionsByStreet);
		report.println("games " + harness.gamesPlayed + ", hands " + harness.handsPlayed + ", hands dropped "
				+ harness.handsDropped + ", decisions dropped " + harness.decisionsDropped + ", illegal chosen "
				+ harness.illegalChosen + ", games stopped by the blind schedule "
				+ harness.gamesStoppedByBlindSchedule);
		report.println("action classes " + harness.actionClasses);
		report.println("chosen " + harness.chosenKinds);
		for (String note : harness.notes) {
			report.println("note: " + note);
		}
	}

	private static List<Object> preflopTable(PrintStream report) {
		double[][] table = new PreflopEquilatorImpl().preflopStrength;
		if (table != PreflopStrengthTable.array) {
			throw new IllegalStateException("PreflopEquilatorImpl does not use PreflopStrengthTable.array");
		}
		double[][] fromFile = ((PreflopEquilatorImpl) PreflopEquilatorImpl.createFromFile()).preflopStrength;
		double maxDifference = 0;
		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < table[i].length; j++) {
				maxDifference = Math.max(maxDifference, Math.abs(table[i][j] - fromFile[i][j]));
			}
		}
		report.println("preflop.eql equals PreflopStrengthTable.array: " + Arrays.deepEquals(table, fromFile)
				+ ", max difference " + maxDifference);
		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < table[i].length; j++) {
				if ((i >= HAND_CLASSES || j >= HAND_CLASSES) && table[i][j] != UNUSED) {
					throw new IllegalStateException("Unexpected value outside 169x169 at " + i + "," + j);
				}
			}
		}
		List<Object> rows = new ArrayList<>();
		for (int i = 0; i < HAND_CLASSES; i++) {
			List<Object> row = new ArrayList<>();
			for (int j = 0; j < HAND_CLASSES; j++) {
				row.add(table[i][j]);
			}
			rows.add(row);
		}
		return rows;
	}

	private static List<Object> equityEntries() {
		Random random = new Random(EQUITY_SEED);
		PokerEquilatorBrecher equilator = new PokerEquilatorBrecher();
		List<Object> entries = new ArrayList<>();
		for (int i = 0; i < EQUITY_ENTRIES; i++) {
			int boardSize = 3 + i % 3;
			int rangeKind = (i / 3) % 3;
			List<Card> deck = new ArrayList<>(Deck.getCards());
			Collections.shuffle(deck, random);
			Card hero1 = deck.get(0);
			Card hero2 = deck.get(1);
			Card[] board = deck.subList(2, 2 + boardSize).toArray(new Card[0]);
			List<Card> dead = deck.subList(0, 2 + boardSize);
			Spectrum spectrum = new Spectrum();
			StringBuilder range = new StringBuilder();
			List<Card> all = Deck.getCards();
			for (int a = 0; a < all.size(); a++) {
				for (int b = a + 1; b < all.size(); b++) {
					Card first = all.get(a);
					Card second = all.get(b);
					if (dead.contains(first) || dead.contains(second)) {
						continue;
					}
					double weight = weight(rangeKind, first, second, random);
					if (weight == 0) {
						continue;
					}
					spectrum.add(new HoleCards(first, second), weight);
					if (range.length() > 0) {
						range.append(' ');
					}
					String text = weight == Math.rint(weight) ? Long.toString((long) weight) : Double.toString(weight);
					range.append(first).append(second).append(':').append(text);
				}
			}
			double strength;
			double positive;
			double negative;
			if (boardSize == 5) {
				strength = equilator.strengthVsSpectrum(hero1, hero2, board, spectrum);
				positive = 0;
				negative = 0;
			} else {
				StreetEquity equity = PokerEquilatorBrecher.equityVsSpectrum(new HoleCards(hero1, hero2), board,
						spectrum);
				strength = equity.strength;
				positive = equity.positivePotential;
				negative = equity.negativePotential;
			}
			Map<String, Object> entry = Json.object("hero", hero1 + " " + hero2, "board", join(board), "range",
					range.toString(), "strength", strength, "positivePotential", positive, "negativePotential", negative);
			entries.add(entry);
		}
		return entries;
	}

	private static double weight(int rangeKind, Card first, Card second, Random random) {
		if (rangeKind == 0) {
			return 1;
		}
		if (rangeKind == 1) {
			return random.nextDouble() < 0.3 ? WEIGHTS[random.nextInt(WEIGHTS.length)] : 0;
		}
		int high = Math.max(first.getValue().intValue(), second.getValue().intValue());
		int low = Math.min(first.getValue().intValue(), second.getValue().intValue());
		if (high == low) {
			return 1;
		}
		if (low >= 8) {
			return first.isSuitedWith(second) ? 1 : 0.5;
		}
		if (high == 12 && first.isSuitedWith(second)) {
			return 0.25;
		}
		return 0;
	}

	private static String join(Card[] cards) {
		StringBuilder builder = new StringBuilder();
		for (Card card : cards) {
			if (builder.length() > 0) {
				builder.append(' ');
			}
			builder.append(card);
		}
		return builder.toString();
	}
}

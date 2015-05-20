package mallorcatour.core.equilator.quick;

import static mallorcatour.core.equilator.PokerEquilatorBrecher.LOGGING;
import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.equilator.StreetEquity;
import mallorcatour.core.game.Card;
import mallorcatour.util.Log;

public class EquilatorQuick {

	/**
	 * @param heroCard1
	 * @param heroCard2
	 * @param flop1
	 * @param flop2
	 * @param flop3
	 * @param combinations
	 * @return
	 */
	public static StreetEquity equityVsRandomFullPotential(int heroCard1, int heroCard2, int flop1, int flop2,
			int flop3, FlopCombinations combinations) {
		boolean positive = false, drawBoolean;

		int deckSize = 52;
		int count = 0, wins = 0, draw = 0;
		double countPositive = 0, winsPositive = 0;
		double countNegative = 0, winsNegative = 0;
		int heroFlopCombination = combinations.get(heroCard1, heroCard2);
		for (int opponentCard1 = 0; opponentCard1 < deckSize; opponentCard1++) {
			if (heroCard1 == opponentCard1 || heroCard2 == opponentCard1 || flop1 == opponentCard1
					|| flop2 == opponentCard1 || flop3 == opponentCard1) {
				continue;
			}
			for (int opponentCard2 = opponentCard1 + 1; opponentCard2 < deckSize; opponentCard2++) {
				if (heroCard1 == opponentCard2 || heroCard2 == opponentCard2 || flop1 == opponentCard2
						|| flop2 == opponentCard2 || flop3 == opponentCard2) {
					continue;
				}
				int opponentFlopCombination = combinations.get(opponentCard1, opponentCard2);
				if (heroFlopCombination < opponentFlopCombination) {
					positive = true;
					drawBoolean = false;
				} else if (heroFlopCombination > opponentFlopCombination) {
					positive = false;
					wins++;
					drawBoolean = false;
				} else {
					draw++;
					drawBoolean = true;
				}
				count++;
				int heroCombination;
				int opponentCombination;
				for (int turn = 0; turn < deckSize; turn++) {
					if (heroCard1 == turn || heroCard2 == turn) {
						continue;
					}
					if (turn == opponentCard1 || turn == opponentCard2) {
						continue;
					}
					if (flop1 == turn || flop2 == turn || flop3 == turn) {
						continue;
					}
					for (int river = 0; river < deckSize; river++) {
						if (heroCard1 == river || heroCard2 == river) {
							continue;
						}
						if (river == opponentCard1 || river == opponentCard2) {
							continue;
						}
						if (flop1 == river || flop2 == river || flop3 == river) {
							continue;
						}
						if (river == turn) {
							continue;
						}
						heroCombination = combinations.get(heroCard1, heroCard2, turn, river);
						opponentCombination = combinations.get(opponentCard1, opponentCard2, turn, river);

						if (drawBoolean) {
							countPositive += 0.5;
							countNegative += 0.5;
							if (heroCombination > opponentCombination) {
								winsPositive += 0.5;
							}
							if (heroCombination < opponentCombination) {
								winsNegative += 0.5;
							}
						} else if (positive) {
							countPositive++;
							if (heroCombination > opponentCombination) {
								winsPositive++;
							}
							if (heroCombination == opponentCombination) {
								winsPositive += 0.5;
							}
						} else {
							countNegative++;
							if (heroCombination < opponentCombination) {
								winsNegative++;
							}
							if (heroCombination == opponentCombination) {
								winsNegative += 0.5;
							}
						}
					}
				}
			}
		}
		if (LOGGING) {
			Log.d("Wins = " + wins + " Count = " + count + " Draw = " + draw);
			Log.d("WinsPositive = " + winsPositive + " CountPositive = " + countPositive);
			Log.d("WinsNegative = " + winsNegative + " CountNegative = " + countNegative);
		}
		StreetEquity result = new StreetEquity();
		result.positivePotential = (countPositive != 0) ? winsPositive / countPositive : 0;
		result.negativePotential = (countNegative != 0) ? winsNegative / countNegative : 0;
		result.strength = ((double) draw / 2 + wins) / count;
		result.draw = (double) draw / count;
		return result;
	}

	public static void main(String... args) {
		int heroCard1 = 7;
		int heroCard2 = 34;

		int flop1 = 13;
		int flop2 = 23;
		int flop3 = 25;

		PokerEquilatorBrecher.LOGGING = false;
		FlopCombinations combinations = new FlopCombinations(flop1, flop2, flop3);
		combinations.init();
		long start = System.currentTimeMillis();
		int[] array = new int[] { 2, 3, 7, 34, flop1, flop2, flop3 };
		for (long i = 0; i < 1100 * 50 * 50; i++) {
			PokerEquilatorBrecher.combination(array);
		}
		Log.d("Time of combination calculation: " + (System.currentTimeMillis() - start) + " ms");
		testArrayGet(combinations);
		start = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++)
			equityVsRandomFullPotential(heroCard1, heroCard2, flop1, flop2, flop3, combinations);
		Log.d("Time: " + (System.currentTimeMillis() - start) + " ms");
		start = System.currentTimeMillis();
		for (int i = 0; i < 10; i++)
			PokerEquilatorBrecher.equityVsRandomFullPotential(Card.valueOf(heroCard1), Card.valueOf(heroCard2),
					new Card[] { Card.valueOf(flop1), Card.valueOf(flop2), Card.valueOf(flop3) });
		Log.d("Time: " + (System.currentTimeMillis() - start) + " ms");
	}

	private static void testArrayGet(FlopCombinations combinations) {
		long start;
		start = System.currentTimeMillis();
		@SuppressWarnings("unused")
		int j;
		for (long i = 0; i < 1100L * 2*1100 * 50 * 50; i++) {
			j = combinations.sevenCards[2][3][7][34];
		}
		Log.d("Time of combinations.get: " + (System.currentTimeMillis() - start) + " ms");
	}
}

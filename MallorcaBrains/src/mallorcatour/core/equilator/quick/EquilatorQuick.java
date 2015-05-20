package mallorcatour.core.equilator.quick;

import static mallorcatour.core.equilator.PokerEquilatorBrecher.LOGGING;
import static mallorcatour.core.equilator.PokerEquilatorBrecher.encode;
import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.equilator.StreetEquity;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.Deck;
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
		int[] nonUsedCards = Deck.getIntCardsForBrecher();

		int count = 0, wins = 0, draw = 0;
		double countPositive = 0, winsPositive = 0;
		double countNegative = 0, winsNegative = 0;
		long heroCardsKey = encode(heroCard1, heroCard2);
		long heroTurnCardsKey, heroRiverCardsKey;
		long oppCardsKey, oppTurnCardsKey, oppRiverCardsKey;
		int heroFlopCombination = combinations.fiveCards.get(heroCardsKey);
		for (int opponentCard1 = 0; opponentCard1 < nonUsedCards.length; opponentCard1++) {
			if (heroCard1 == opponentCard1 || heroCard2 == opponentCard1 || flop1 == opponentCard1
					|| flop2 == opponentCard1 || flop3 == opponentCard1) {
				continue;
			}
			for (int opponentCard2 = opponentCard1 + 1; opponentCard2 < nonUsedCards.length; opponentCard2++) {
				if (heroCard1 == opponentCard2 || heroCard2 == opponentCard2 || flop1 == opponentCard2
						|| flop2 == opponentCard2 || flop3 == opponentCard2) {
					continue;
				}
				oppCardsKey = encode(opponentCard1, opponentCard2);
				int opponentCombinationWithoutAdditional = combinations.fiveCards.get(oppCardsKey);
				if (heroFlopCombination < opponentCombinationWithoutAdditional) {
					positive = true;
					drawBoolean = false;
				} else if (heroFlopCombination > opponentCombinationWithoutAdditional) {
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
				for (int turn = 0; turn < nonUsedCards.length; turn++) {
					if (heroCard1 == turn || heroCard2 == turn) {
						continue;
					}
					if (turn == opponentCard1 || turn == opponentCard2) {
						continue;
					}
					if (flop1 == turn || flop2 == turn || flop3 == turn) {
						continue;
					}
					oppTurnCardsKey = encode(oppCardsKey, turn);
					heroTurnCardsKey = encode(heroCardsKey, turn);
					for (int river = 0; river < nonUsedCards.length; river++) {
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
						heroRiverCardsKey = encode(heroTurnCardsKey, river);
						oppRiverCardsKey = encode(oppTurnCardsKey, river);
						heroCombination = combinations.sevenCards.get(heroRiverCardsKey);
						opponentCombination = combinations.sevenCards.get(oppRiverCardsKey);

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
		result.positivePotential = (countPositive != 0) ? (double) winsPositive / countPositive : 1;
		result.negativePotential = (countNegative != 0) ? (double) winsNegative / countNegative : 1;
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
		long key=encode(2, 3, 7, 34);
		for (int i = 0; i < 10*1100 * 50 * 50; i++) {
			combinations.sevenCards.get(key);
		}
		Log.d("Time map.get: " + (System.currentTimeMillis() - start) + " ms");
		int[][][][] combArray = new int[52][52][52][52];
		start = System.currentTimeMillis();
		for (int i = 0; i < 10*1100 * 50 * 50; i++) {
			int a = combArray[2][3][7][34];
		}
		Log.d("Time array get: " + (System.currentTimeMillis() - start) + " ms");
		start = System.currentTimeMillis();
		int[] array = new int[]{2,3,7,34,flop1, flop2, flop3};
		for (long i = 0; i < 1100 * 50 * 50; i++) {
			PokerEquilatorBrecher.combination(array);
		}
		Log.d("Time: " + (System.currentTimeMillis() - start) + " ms");
		start = System.currentTimeMillis();
//		for (int i = 0; i < 10; i++)
//			equityVsRandomFullPotential(heroCard1, heroCard2, flop1, flop2, flop3, combinations);
//		Log.d("Time: " + (System.currentTimeMillis() - start) + " ms");
		start = System.currentTimeMillis();
		for (int i = 0; i < 10; i++)
			PokerEquilatorBrecher.equityVsRandomFullPotential(Card.valueOf(heroCard1), 
					Card.valueOf(heroCard2),
					new Card[]{
					Card.valueOf(flop1), 
					Card.valueOf(flop2), 
					Card.valueOf(flop3)});
		Log.d("Time: " + (System.currentTimeMillis() - start) + " ms");
	}
}

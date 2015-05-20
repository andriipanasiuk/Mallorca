package mallorcatour.core.equilator.quick;

import static mallorcatour.core.equilator.PokerEquilatorBrecher.combination;
import static mallorcatour.core.equilator.PokerEquilatorBrecher.encode;
import mallorcatour.util.Log;

public class FlopCombinations {

	private final long flopKey;

	int flop1, flop2, flop3;

	public FlopCombinations(int flop1, int flop2, int flop3) {
		this.flop1 = flop1;
		this.flop2 = flop2;
		this.flop3 = flop3;
		this.flopKey = encode(flop1, flop2, flop3);
	}

	public int[][] fiveCards = new int[52][52];
	public int[][][] sixCards = new int[52][52][52];
	public int[][][][] sevenCards = new int[52][52][52][52];

	public void init() {
		long start = System.currentTimeMillis();
		int combination;
		int deckSize = 52;
		int count = 0;
		long twoCardsKey, threeCardsKey, fourCardsKey;
		for (int card1 = 0; card1 < deckSize; card1++) {
			if (card1 == flop1 || card1 == flop2 || card1 == flop3) {
				continue;
			}
			for (int card2 = 0; card2 < deckSize; card2++) {
				if (card2 == card1) {
					continue;
				}
				if (card2 == flop1 || card2 == flop2 || card2 == flop3) {
					continue;
				}
				twoCardsKey = encode(card1, card2);
				combination = combination(encode(flopKey, twoCardsKey), 5);
				fiveCards[card1][card2] = combination;
				for (int turn = 0; turn < deckSize; turn++) {
					if (turn == card1 || turn == card2) {
						continue;
					}
					if (turn == flop1 || turn == flop2 || turn == flop3) {
						continue;
					}
					threeCardsKey = encode(twoCardsKey, turn);
					combination = combination(encode(flopKey, threeCardsKey), 6);
					sixCards[card1][card2][turn] = combination;
					for (int river = 0; river < deckSize; river++) {
						if (river == card1 || river == card2) {
							continue;
						}
						if (river == turn) {
							continue;
						}
						if (river == flop1 || river == flop2 || river == flop3) {
							continue;
						}
						count++;
						fourCardsKey = encode(threeCardsKey, river);
						combination = combination(encode(flopKey, fourCardsKey), 7);
						sevenCards[card1][card2][turn][river] = combination;
					}
				}
			}
		}
		Log.d("Generated " + count + " combinations in " + (System.currentTimeMillis() - start) + " ms");
	}

	public int get(int card1, int card2) {
		return fiveCards[card1][card2];
	}

	public int get(int card1, int card2, int card3) {
		return sixCards[card1][card2][card3];
	}


	public int get(int card1, int card2, int card3, int card4) {
		return sevenCards[card1][card2][card3][card4];
	}

	public static void main(String... args) {
		long start = System.currentTimeMillis();
		for (int i = 0; i < 1; i++) {
			FlopCombinations combinations = new FlopCombinations(23, 25, 35);
			combinations.init();
		}
		Log.d("Time: " + (System.currentTimeMillis() - start) + " ms");
	}
}

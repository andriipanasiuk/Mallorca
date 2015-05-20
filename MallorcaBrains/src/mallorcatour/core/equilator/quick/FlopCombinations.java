package mallorcatour.core.equilator.quick;

import static mallorcatour.core.equilator.PokerEquilatorBrecher.combination;
import static mallorcatour.core.equilator.PokerEquilatorBrecher.encode;

import java.util.HashMap;
import java.util.Map;

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

	public Map<Long, Integer> fiveCards = new HashMap<>();
	public Map<Long, Integer> sixCards = new HashMap<>();
	public Map<Long, Integer> sevenCards = new HashMap<>();

	public void init() {
		long start = System.currentTimeMillis();
		int combination;
		long twoCardsKey, threeCardsKey, fourCardsKey;
		for (int card1 = 0; card1 < 52; card1++) {
			if (card1 == flop1 || card1 == flop2 || card1 == flop3) {
				continue;
			}
			for (int card2 = card1 + 1; card2 < 52; card2++) {
				if (card2 == flop1 || card2 == flop2 || card2 == flop3) {
					continue;
				}
				twoCardsKey = encode(card1, card2);
				combination = combination(encode(flopKey, twoCardsKey), 5);
				fiveCards.put(twoCardsKey, combination);
				for (int turn = card2 + 1; turn < 52; turn++) {
					if (turn == flop1 || turn == flop2 || turn == flop3) {
						continue;
					}
					threeCardsKey = encode(twoCardsKey, turn);
					combination = combination(encode(flopKey, threeCardsKey), 6);
					sixCards.put(threeCardsKey, combination);
					for (int river = turn + 1; river < 52; river++) {
						if (river == flop1 || river == flop2 || river == flop3) {
							continue;
						}
						if (card1 == 2 && card2 == 3 && turn == 7 && river == 34) {
							int i = 0;
						}
						fourCardsKey = encode(threeCardsKey, river);
						if (fourCardsKey == 17179869199L) {
							Log.d("!!!!!");
						}
						combination = combination(encode(flopKey, fourCardsKey), 7);
						sevenCards.put(fourCardsKey, combination);
					}
				}
			}
		}
		Log.d("Generated " + (fiveCards.size() + sixCards.size() + sevenCards.size()) + " combinations in "
				+ (System.currentTimeMillis() - start) + " ms");
	}

	public static void main(String... args) {
		long start = System.currentTimeMillis();
		for (int i = 0; i < 1; i++) {
			FlopCombinations combinations = new FlopCombinations(23, 25, 35);
			combinations.init();
			Log.d("Size: " + combinations.fiveCards.size());
			Log.d("Size: " + combinations.sixCards.size());
			Log.d("Size: " + combinations.sevenCards.size());
		}
		Log.d("Time: " + (System.currentTimeMillis() - start) + " ms");
	}
}

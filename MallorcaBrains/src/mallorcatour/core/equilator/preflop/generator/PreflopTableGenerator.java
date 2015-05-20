package mallorcatour.core.equilator.preflop.generator;

import mallorcatour.core.equilator.spears.PokerEquilatorSpears;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.Card.Suit;
import mallorcatour.core.game.Card.Value;
import mallorcatour.util.Log;
import mallorcatour.util.SerializatorUtils;
import static mallorcatour.core.equilator.preflop.EquilatorPreflop.*;

public class PreflopTableGenerator {

	static void generatePreflopTableStrength() {
		for (int i = 0; i < preflopStrength.length; i++) {
			for (int j = 0; j < preflopStrength[0].length; j++) {
				preflopStrength[i][j] = -1;
			}
		}
		int count = 0;
		long start = System.currentTimeMillis();
		for (Value heroValue1 : Value.getValues()) {
			for (Value heroValue2 : Value.getValues()) {
				if (heroValue1.intValue() < heroValue2.intValue()) {
					continue;
				}
				Card heroCard1 = new Card(heroValue1, Suit.CLUBS);
				Card heroCard2 = new Card(heroValue2, Suit.DIAMONDS);
				int heroHash = HoleCards
						.hashCodeForValues(heroCard1, heroCard2);
				for (Value villainValue1 : Value.getValues()) {
					for (Value villainValue2 : Value.getValues()) {
						if (villainValue1.intValue() < villainValue2.intValue()) {
							continue;
						}
						Card villainCard1 = new Card(villainValue1, Suit.HEARTS);
						Card villainCard2 = new Card(villainValue2, Suit.SPADES);
						int villainHash = HoleCards.hashCodeForValues(
								villainCard1, villainCard2);
						if (preflopStrength[villainHash][heroHash] != -1) {
							continue;
						}
						double s = PreflopTableGenerator.calculateStrength(heroCard1, heroCard2,
								villainCard1, villainCard2);
						preflopStrength[heroHash][villainHash] = s;
						preflopStrength[villainHash][heroHash] = 1 - s;
						count++;
						Log.d(count + "");
					}
				}
			}
		}
		Log.d("Calculated " + count + " strengthes");
		Log.d("Time: " + (System.currentTimeMillis() - start) + " ms");
		SerializatorUtils.save("preflop.eql", preflopStrength);
	}

	public static double calculateStrength(Card heroCard1, Card heroCard2,
			Card villainCard1, Card villainCard2) {
		int[] heroCardsI = new int[7];
		int[] villainCardsI = new int[7];
		heroCardsI[0] = heroCard1.intValue();
		heroCardsI[1] = heroCard2.intValue();
		villainCardsI[0] = villainCard1.intValue();
		villainCardsI[1] = villainCard2.intValue();
		int wins = 0, draw = 0, count = 0;
		for (int i1 = 0; i1 < 52; i1++) {
			if (i1 == heroCardsI[0] || i1 == heroCardsI[1]
					|| i1 == villainCardsI[0] || i1 == villainCardsI[1]) {
				continue;
			}
			for (int i2 = i1 + 1; i2 < 52; i2++) {
				if (i2 == heroCardsI[0] || i2 == heroCardsI[1]
						|| i2 == villainCardsI[0] || i2 == villainCardsI[1]) {
					continue;
				}
				for (int i3 = i2 + 1; i3 < 52; i3++) {
					if (i3 == heroCardsI[0] || i3 == heroCardsI[1]
							|| i3 == villainCardsI[0] || i3 == villainCardsI[1]) {
						continue;
					}
					for (int i4 = i3 + 1; i4 < 52; i4++) {
						if (i4 == heroCardsI[0] || i4 == heroCardsI[1]
								|| i4 == villainCardsI[0]
								|| i4 == villainCardsI[1]) {
							continue;
						}
						for (int i5 = i4 + 1; i5 < 52; i5++) {
							if (i5 == heroCardsI[0] || i5 == heroCardsI[1]
									|| i5 == villainCardsI[0]
									|| i5 == villainCardsI[1]) {
								continue;
							}
							heroCardsI[2] = i1;
							heroCardsI[3] = i2;
							heroCardsI[4] = i3;
							heroCardsI[5] = i4;
							heroCardsI[6] = i5;
							//
							villainCardsI[2] = i1;
							villainCardsI[3] = i2;
							villainCardsI[4] = i3;
							villainCardsI[5] = i4;
							villainCardsI[6] = i5;
							int heroCombination = PokerEquilatorSpears
									.combination(heroCardsI);
							int villainCombination = PokerEquilatorSpears
									.combination(villainCardsI);
							if (heroCombination > villainCombination) {
								wins++;
							} else if (heroCombination == villainCombination) {
								draw++;
							}
							count++;
						}
					}
				}
			}
		}
		return ((double) draw / 2 + wins) / count;
	}
}

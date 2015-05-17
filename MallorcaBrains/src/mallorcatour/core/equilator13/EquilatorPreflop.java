package mallorcatour.core.equilator13;

import mallorcatour.core.equilator13.unused.PokerEquilatorSpears;
import mallorcatour.game.core.Card;
import mallorcatour.game.core.Deck;
import mallorcatour.game.core.HoleCards;
import mallorcatour.game.core.Spectrum;
import mallorcatour.game.core.Card.Suit;
import mallorcatour.game.core.Card.Value;
import mallorcatour.util.Log;
import mallorcatour.util.SerializatorUtils;

public class EquilatorPreflop {

	public static boolean LOGGING = false;
	private static final String PREFLOP_STRENGTH_TABLE_PATH = "equilator\\preflop.eql";
	private static int MAX_PREFLOP_STRENGTH = EquilatorPreflop.preflopStrengthForSorted(
			Card.valueOf("Ah"), Card.valueOf("Ac"));
	private static double[][] preflopStrength = new double[170][170];

	static {
		preflopStrength = SerializatorUtils.load(
				PREFLOP_STRENGTH_TABLE_PATH, double[][].class);
	}

	private static int combination(Card card1, Card card2) {
		return combination(card1.intValue(), card2.intValue());
	}

	private static int combination(int card1, int card2) {
		int result = 0;
		int value1 = card1/4;
		int value2 = card2/4;
		int suit1 = card1%4;
		int suit2 = card2%4;
		if (value1== value2) {
			result += 100000;
			result += value1;
			return result;
		} else {
			if (card1 > card2) {
				result += value1 * 1000;
				result += value2 * 10;
			} else {
				result += value2 * 1000;
				result += value1 * 10;
			}
			if (suit1 == suit2) {
				result += 1;
			}
			return result;
		}
	}

	public static StreetEquity equityVsRandom(Card card1, Card card2) {
		int[] deck = Deck.getIntCards();
		int wins = 0, count = 0;
		double positive = 0, countPositive = 0, negative = 0, countNegative = 0;
		int heroCombination = combination(card1, card2);
		for (int i1 = 0; i1 < deck.length; i1++) {
			int opponentCard1 = deck[i1];
			if (opponentCard1 == card1.intValue()
					|| opponentCard1 == card2.intValue()) {
				continue;
			}
			for (int i2 = i1 + 1; i2 < deck.length; i2++) {
				int opponentCard2 = deck[i2];
				if (opponentCard2 == card1.intValue()
						|| opponentCard2 == card2.intValue()) {
					continue;
				}
				int villainCombination = combination(opponentCard1, opponentCard2);
				count++;
				int villainHash = HoleCards.hashCodeForValues(
						Card.valueOf(opponentCard1),
						Card.valueOf(opponentCard2));
				int heroHash = HoleCards.hashCodeForValues(card1, card2);
				double strengthToRiver = preflopStrength(heroHash, villainHash);
				if (villainCombination < heroCombination) {
					wins++;
					countNegative++;
					negative += (1 - strengthToRiver);
				} else {
					countPositive++;
					positive += strengthToRiver;
				}
			}
		}
		StreetEquity result = new StreetEquity();
		result.strength = (double) wins / count;
		result.positivePotential = countPositive != 0 ? positive
				/ countPositive : 0;
		result.negativePotential = countNegative != 0 ? negative
				/ countNegative : 0;
		if (LOGGING) {
			Log.d("Strength: " + result.strength + " Neg.pot.: "
					+ result.negativePotential + " Pos.pot.: "
					+ result.positivePotential);
			Log.d("Wins: " + wins + " Count: " + count);
			Log.d("Positive: " + positive + " Positive count: " + countPositive);
			Log.d("Negative: " + negative + " Negative count: " + countNegative);
		}
		return result;
	}

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
						double s = EquilatorPreflop.calculateStrength(heroCard1, heroCard2,
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

	private static double preflopStrength(HoleCards heroCards,
			HoleCards villainCards) {
		int heroHash = heroCards.hashCodeForValues();
		int villainHash = villainCards.hashCodeForValues();
		return preflopStrength[heroHash][villainHash];
	}

	public static double preflopStrength(int heroHash, int villainHash) {
		return preflopStrength[heroHash][villainHash];
	}

	/**
	 * Calculates preflop strength based on formula.
	 * 
	 * @param holeCard1
	 * @param holeCard2
	 * @deprecated use this{@link #strengthVsRandom(Card, Card)} instead
	 * @return
	 */
	@Deprecated
	public static double strengthByFormula(Card holeCard1, Card holeCard2) {
		if (holeCard1.compareTo(holeCard2) == 0) {
			throw new IllegalArgumentException(
					"Hole cards must not be equals. " + "First: "
							+ holeCard1 + "; second: " + holeCard2);
		}
		int strength;
		if (holeCard1.compareTo(holeCard2) > 0) {
			strength = EquilatorPreflop.preflopStrengthForSorted(holeCard1, holeCard2);
		} else {
			strength = EquilatorPreflop.preflopStrengthForSorted(holeCard2, holeCard1);
		}
		return ((double) strength) / (MAX_PREFLOP_STRENGTH);
	}

	public static double strengthVsRandom(Card heroCard1, Card heroCard2) {
		Spectrum spectrum = Spectrum.random();
		spectrum.remove(heroCard1, heroCard2);
		return strengthVsSpectrum(heroCard1, heroCard2, spectrum);
	}

	public static double strengthVsSpectrum(Card heroCard1, Card heroCard2, Spectrum villainSpectrum) {
		double count = 0, wins = 0;
		int log = 0;
		for (HoleCards villainHoleCards : villainSpectrum) {
			log++;
			double strength = preflopStrength(new HoleCards(heroCard1,
					heroCard2), villainHoleCards);
			double frequency = villainSpectrum.getWeight(villainHoleCards);
			wins += strength * frequency;
			count += frequency;
		}
		Log.d("Calculated vs " + log + " villain cards");
		return ((double) wins) / count;
	}

	/**
	 * First card must be bigger than the second
	 * @param holeCard1
	 * @param holeCard2
	 * @return
	 */
	private static int preflopStrengthForSorted(Card holeCard1, Card holeCard2) {
		if (!holeCard1.sameValue(holeCard2)) {
			if (!holeCard1.isSuitedWith(holeCard2)) {
				return holeCard1.getValue().intValue() * 2
						+ holeCard2.getValue().intValue();
			} else {
				return holeCard1.getValue().intValue() * 2
						+ holeCard2.getValue().intValue() + 2;
			}
		} else {
			return holeCard1.getValue().intValue() * 2
					+ holeCard2.getValue().intValue() + 22;
		}
	}

	private static double calculateStrength(Card heroCard1, Card heroCard2,
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

	static void testMain() {
		int[] deck = Deck.getIntCards();
		for (int i = 0; i < deck.length; i++) {
			for (int j = i+1; j < deck.length; j++) {
				Card card1 = Card.valueOf(deck[i]);
				Card card2 = Card.valueOf(deck[j]);
				Log.d(card1 + " " + card2);
				equityVsRandom(card1, card2);
			}
		}
	}

	static void testSpeed() {
		int[] deck = Deck.getIntCards();
		LOGGING = false;
		long start = System.currentTimeMillis();
		for (int i = 0; i < deck.length; i++) {
			for (int j = i+1; j < deck.length; j++) {
				Card card1 = Card.valueOf(deck[i]);
				Card card2 = Card.valueOf(deck[j]);
				equityVsRandom(card1, card2);
			}
		}
		Log.d("Time: " + (System.currentTimeMillis() - start) + " ms");
	}

	public static void main(String... args) {
//		testMain();
		double strength = strengthVsRandom(Card.valueOf("As"), Card.valueOf("Ah"));
		Log.d("Strength: " + strength);
//		equityVsRandom(Card.valueOf("5s"), Card.valueOf("6h"));
	}

}
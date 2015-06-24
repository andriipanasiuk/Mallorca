package mallorcatour.core.equilator.preflop;

import java.util.Map.Entry;

import mallorcatour.bot.math.RandomVariable;
import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.equilator.StreetEquity;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.Deck;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.spectrum.Spectrum;
import mallorcatour.tools.Log;
import mallorcatour.tools.SerializatorUtils;

public class EquilatorPreflop {

	public static enum LoadFrom {
		FILE, CODE
	}
	public static LoadFrom loadFrom = LoadFrom.CODE;
	public static boolean LOGGING = false;
	private static final String PREFLOP_STRENGTH_TABLE_PATH = "prefl" + "op.eql";
	public static double[][] preflopStrength = new double[170][170];

	static {
		if (loadFrom == LoadFrom.FILE) {
			preflopStrength = SerializatorUtils.load(
					EquilatorPreflop.class.getResourceAsStream(PREFLOP_STRENGTH_TABLE_PATH), double[][].class);
		} else if (loadFrom == LoadFrom.CODE) {
			preflopStrength = PreflopGenerated.preflopStrength;
		}
	}

	private static int combination(Card card1, Card card2) {
		return combination(card1.intValue(), card2.intValue());
	}

	private static int combination(int card1, int card2) {
		int result = 0;
		int value1 = card1 / 4;
		int value2 = card2 / 4;
		int suit1 = card1 % 4;
		int suit2 = card2 % 4;
		if (value1 == value2) {
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

	private static double preflopStrength(HoleCards heroCards,
			HoleCards villainCards) {
		int heroHash = heroCards.hashCodeForValues();
		int villainHash = villainCards.hashCodeForValues();
		return preflopStrength[heroHash][villainHash];
	}

	private static double preflopStrength(int heroHash, int villainHash) {
		return preflopStrength[heroHash][villainHash];
	}

	public static double strengthVsRandom(Card heroCard1, Card heroCard2) {
		Spectrum spectrum = Spectrum.random();
		spectrum.remove(heroCard1, heroCard2);
		return strengthVsSpectrum(heroCard1, heroCard2, spectrum);
	}

	public static double strengthVsSpectrum(Card heroCard1, Card heroCard2, Spectrum villainSpectrum) {
		double count = 0, wins = 0;
		int log = 0;
		HoleCards heroCards = new HoleCards(heroCard1, heroCard2);
		for (HoleCards villainHoleCards : villainSpectrum) {
			log++;
			double strength = preflopStrength(heroCards, villainHoleCards);
			double frequency = villainSpectrum.getWeight(villainHoleCards);
			wins += strength * frequency;
			count += frequency;
		}
		if (PokerEquilatorBrecher.LOGGING) {
			Log.d("Calculated vs " + log + " villain cards");
		}
		return ((double) wins) / count;
	}

	public static RandomVariable profitVsSpectrum(Card heroCard1, Card heroCard2, Spectrum spectrum, double win,
			double lose) {
		int log = 0;
		RandomVariable result = new RandomVariable();
		HoleCards heroCards = new HoleCards(heroCard1, heroCard2);
		double sumWeight = spectrum.summaryWeight();
		for (Entry<HoleCards, Double> item : spectrum.weightsIterator()) {
			log++;
			HoleCards villainHoleCards = item.getKey();
			double weight = item.getValue();
			double strength = preflopStrength(heroCards, villainHoleCards);
			result.add(strength * (weight / sumWeight), win);
			result.add((1 - strength) * (weight / sumWeight), -lose);
		}
		if (PokerEquilatorBrecher.LOGGING) {
			Log.d("Calculated vs " + log + " villain cards");
		}
		return result;
	}

}
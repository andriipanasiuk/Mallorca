package mallorcatour.core.equilator;

import static mallorcatour.core.equilator.PokerEquilatorBrecher.LOGGING;
import static mallorcatour.core.equilator.PokerEquilatorBrecher.combination;

import java.util.Map.Entry;

import mallorcatour.core.game.Card;
import mallorcatour.core.game.Deck;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.spectrum.Spectrum;
import mallorcatour.tools.ArrayUtils;
import mallorcatour.tools.Log;

public class EquilatorStrength {

	public static StreetEquity equityVsSpectrumToRiver(Card heroCard1, Card heroCard2, Card[] boardCards,
			Spectrum spectrum) {
		int boardSize = boardCards.length;
		if (boardSize < 3) {
			throw new IllegalArgumentException("Cannot calculate for empty board");
		}
		int[] allHeroCards = new int[7];
		int[] allOpponentCards = new int[7];

		allHeroCards[0] = heroCard1.intValueForBrecher();
		allHeroCards[1] = heroCard2.intValueForBrecher();
		int i = 0;
		for (Card boardCard : boardCards) {
			allHeroCards[2 + i] = boardCard.intValueForBrecher();
			allOpponentCards[2 + i] = boardCard.intValueForBrecher();
			i++;
		}
		int[] nonUsedCards = Deck.getIntCardsForBrecher();

		double count = 0, wins = 0, draw = 0;
		for (Entry<HoleCards, Double> entry : spectrum.weightsIterator()) {
			HoleCards cards = entry.getKey();
			double weight = entry.getValue();
			int opponentCard1 = cards.first.intValueForBrecher();
			int opponentCard2 = cards.second.intValueForBrecher();
			if (ArrayUtils.containsElement(allHeroCards, opponentCard1)) {
				continue;
			}
			if (ArrayUtils.containsElement(allHeroCards, opponentCard2)) {
				continue;
			}
			allOpponentCards[0] = opponentCard1;
			allOpponentCards[1] = opponentCard2;
			int myCombination;
			int opponentCombination;
			boolean enumerateTurn = boardSize == 3;
			boolean enumerateRiver = boardSize <= 4;
			int turns = enumerateTurn ? nonUsedCards.length : 1;
			int rivers = enumerateRiver ? nonUsedCards.length : 1;
			for (int t = 0; t < turns; t++) {
				int turn;
				if (enumerateTurn) {
					turn = nonUsedCards[t];
					if (ArrayUtils.containsElement(allHeroCards, turn)) {
						continue;
					}
					if (turn == opponentCard1 || turn == opponentCard2) {
						continue;
					}
					allHeroCards[5] = turn;
					allOpponentCards[5] = turn;
				}
				for (int r = 0; r < rivers; r++) {
					if (enumerateRiver) {
						int river = nonUsedCards[r];
						if (ArrayUtils.containsElement(allHeroCards, river)) {
							continue;
						}
						if (river == opponentCard1 || river == opponentCard2) {
							continue;
						}
						allHeroCards[6] = river;
						allOpponentCards[6] = river;
					}
					myCombination = combination(allHeroCards, true);
					opponentCombination = combination(allOpponentCards, true);
					if (myCombination < opponentCombination) {
					} else if (myCombination > opponentCombination) {
						wins += weight;
					} else {
						draw += weight;
					}
					count += weight;
				}
			}
		}
		if (LOGGING) {
			Log.d("Wins = " + wins + " Count = " + count + " Draw = " + draw);
		}
		StreetEquity result = new StreetEquity();
		result.strength = ((double) draw / 2 + wins) / count;
		result.draw = (double) draw / count;
		return result;
	}

	public static void main(String... args) {
		LOGGING = true;
		Spectrum spectrum = Spectrum.empty();
		spectrum.add(HoleCards.valueOf("AdJs"), 0.5);
		spectrum.add(HoleCards.valueOf("6dTd"), 0.2);
		spectrum.add(HoleCards.valueOf("7dKd"), 0.7);
		spectrum.add(HoleCards.valueOf("8dTd"), 0.4);
		StreetEquity equity = equityVsSpectrumToRiver(
				Card.valueOf("As"),
				Card.valueOf("Qs"),
				new Card[] { Card.valueOf("Jc"), Card.valueOf("Qc"), Card.valueOf("6c"), Card.valueOf("8c"),
						Card.valueOf("9d") }, spectrum);
		Log.d("Result: " + equity.strength);
	}
}
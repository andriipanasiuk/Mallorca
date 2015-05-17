/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.equilator13;

import mallorcatour.game.core.Card;
import mallorcatour.game.core.Deck;
import mallorcatour.game.core.Flop;
import mallorcatour.game.core.HoleCards;
import mallorcatour.game.core.Spectrum;
import mallorcatour.util.ArrayUtils;
import mallorcatour.util.Log;

import com.stevebrecher.poker.HandEval;

/**
 *
 * @author техно
 */
public class PokerEquilatorBrecher {

	public static long time = 0;
	public static long timeCount = 0;

	public static final boolean LOGGING = false;

	private static long encode(long board, int[] cards) {
		long result = board;
		for (int i = 0; i < cards.length; i++) {
			result |= (0x1L << (cards[i]));
		}
		return result;
	}

	public static long encode(int[] brecherCards) {
		long result = 0;
		for (int i = 0; i < brecherCards.length; i++) {
			result |= (0x1L << (brecherCards[i]));
		}
		return result;
	}

	public static int combination(int[] allCards) {
		timeCount++;
		long key = encode(allCards);
		if (allCards.length == 5) {
			return HandEval.hand5Eval(key);
		} else if (allCards.length == 6) {
			return HandEval.hand6Eval(key);
		} else if (allCards.length == 7) {
			return HandEval.hand7Eval(key);
		} else {
			throw new RuntimeException();
		}
	}

	public static int combination(long boardKey, int[] holeCards, int how) {
		timeCount++;
		if (how == 5) {
			return HandEval.hand5Eval(encode(boardKey, holeCards));
		} else if (how == 6) {
			return HandEval.hand6Eval(encode(boardKey, holeCards));
		} else if (how == 7) {
			return HandEval.hand7Eval(encode(boardKey, holeCards));
		} else {
			throw new RuntimeException();
		}
	}

	public static double strengthVsRandom(Card heroCard1, Card heroCard2,
			Card[] boardCards) {
		int boardSize = boardCards.length;
		int[] heroHoleCards = new int[2];
		int[] villainHoleCards = new int[2];
		int[] intBoardCards = new int[boardSize];

		heroHoleCards[0] = heroCard1.intValueForBrecher();
		heroHoleCards[1] = heroCard2.intValueForBrecher();
		for (int i = 0; i < boardSize; i++) {
			intBoardCards[i] = boardCards[i].intValueForBrecher();
		}
		long boardKey = encode(intBoardCards);
		//
		int[] nonUsedCards = Deck.getIntCardsForBrecher();
		int count = 0, wins = 0, draw = 0;
		int myCombination = combination(boardKey, heroHoleCards, boardSize + 2);
		int opponentCombination;
		for (int i = 0; i < nonUsedCards.length; i++) {
			int opponentCard1 = nonUsedCards[i];
			if (ArrayUtils.containsElement(heroHoleCards, opponentCard1)
					|| ArrayUtils.containsElement(intBoardCards, opponentCard1)) {
				continue;
			}
			for (int j = i + 1; j < nonUsedCards.length; j++) {
				int opponentCard2 = nonUsedCards[j];
				if (ArrayUtils.containsElement(heroHoleCards, opponentCard2)
						|| ArrayUtils.containsElement(intBoardCards,
								opponentCard2)) {
					continue;
				}
				villainHoleCards[0] = opponentCard1;
				villainHoleCards[1] = opponentCard2;
				opponentCombination = combination(boardKey, villainHoleCards,
						boardSize + 2);
				if (myCombination > opponentCombination) {
					wins++;
				} else if (myCombination == opponentCombination) {
					draw++;
				}
				count++;
			}
		}
		if (LOGGING) {
			Log.d("equityVsRandom() calculates " + count + " games. Wins = "
					+ wins);
		}
		return ((double) draw / 2 + wins) / count;
	}

	public static double strengthVsSpectrum(Card heroCard1, Card heroCard2,
			Card[] boardCards, Spectrum opponentSpectrum) {
		if (boardCards.length == 0) {
			return EquilatorPreflop.strengthVsSpectrum(heroCard1,
					heroCard2, opponentSpectrum);
		} else {
			int boardSize = boardCards.length;
			int[] allMyCards = new int[2 + boardSize];
			int[] allOpponentCards = new int[2 + boardSize];
			allMyCards[0] = heroCard1.intValueForBrecher();
			allMyCards[1] = heroCard2.intValueForBrecher();
			for (int i = 0; i < boardSize; i++) {
				allMyCards[2 + i] = boardCards[i].intValueForBrecher();
				allOpponentCards[2 + i] = boardCards[i].intValueForBrecher();
			}
			//
			double count = 0, wins = 0, draw = 0;
			int myCombination = combination(allMyCards);

			for (HoleCards opponentHoleCards : opponentSpectrum) {
				Card opponentCard1 = opponentHoleCards.first;
				Card opponentCard2 = opponentHoleCards.second;
				allOpponentCards[0] = opponentCard1.intValueForBrecher();
				allOpponentCards[1] = opponentCard2.intValueForBrecher();
				double frequency = opponentSpectrum
						.getWeight(opponentHoleCards);
				int opponentCombination;
				opponentCombination = combination(allOpponentCards);
				if (myCombination > opponentCombination) {
					wins += frequency;
				} else if (myCombination < opponentCombination) {
				} else {
					draw += frequency;
				}
				count += frequency;
			}
			// Log.d("equityVsRandom() wins = " + wins + " draw = " + draw +
			// " count = " + count);
			return ((double) draw / 2 + wins) / count;
		}
	}

	public static StreetEquity equityOnFlop(Card myCard1, Card myCard2,
			Card flop1, Card flop2, Card flop3) {
		return equityVsRandom(myCard1, myCard2, new Card[] { flop1, flop2,
				flop3 });
	}

	public static StreetEquity fullEquityOnFlop(Card myCard1, Card myCard2,
			Card flop1, Card flop2, Card flop3) {
		return equityVsRandomFullPotential(myCard1, myCard2, new Card[] {
				flop1, flop2, flop3 });
	}

	public static StreetEquity equityOnTurn(Card myCard1, Card myCard2,
			Card flop1, Card flop2, Card flop3, Card turn) {
		return equityVsRandom(myCard1, myCard2, new Card[] { flop1, flop2,
				flop3, turn });
	}

	public static double strengthOnFlop(Card myCard1, Card myCard2, Card flop1,
			Card flop2, Card flop3) {
		return strengthVsRandom(myCard1, myCard2, new Card[] { flop1, flop2,
				flop3 });
	}

	public static double strengthOnTurn(Card myCard1, Card myCard2, Card flop1,
			Card flop2, Card flop3, Card turn) {
		return strengthVsRandom(myCard1, myCard2, new Card[] { flop1, flop2,
				flop3, turn });
	}

	public static double strengthOnRiver(Card myCard1, Card myCard2,
			Card flop1, Card flop2, Card flop3, Card turn, Card river) {
		return strengthVsRandom(myCard1, myCard2, new Card[] { flop1, flop2,
				flop3, turn, river });
	}

	public static StreetEquity equityOnFlopVsSpectrum(Card myCard1,
			Card myCard2, Card flop1, Card flop2, Card flop3, Spectrum spektr) {
		return equityVsSpectrum(myCard1, myCard2, new Card[] { flop1, flop2,
				flop3 }, spektr);
	}

	public static StreetEquity equityVsSpectrum(HoleCards heroCards, Flop flop,
			Spectrum spektr) {
		return equityVsSpectrum(heroCards.first, heroCards.second, new Card[] {
				flop.first, flop.second, flop.third }, spektr);
	}

	public static StreetEquity equityVsSpectrum(HoleCards heroCards, Flop flop,
			Card turn, Spectrum spektr) {
		return equityVsSpectrum(heroCards.first, heroCards.second, new Card[] {
				flop.first, flop.second, flop.third, turn }, spektr);
	}

	public static StreetEquity equityVsSpectrum(HoleCards heroCards, Flop flop,
			Card turn, Card river, Spectrum spektr) {
		return equityVsSpectrum(heroCards.first, heroCards.second, new Card[] {
				flop.first, flop.second, flop.third, turn, river }, spektr);
	}

	public static StreetEquity equityOnTurnVsSpectrum(Card myCard1,
			Card myCard2, Card flop1, Card flop2, Card flop3, Card turn,
			Spectrum spektr) {
		return equityVsSpectrum(myCard1, myCard2, new Card[] { flop1, flop2,
				flop3, turn }, spektr);
	}

	public static double strengthOnRiverVsSpectrum(Card myCard1, Card myCard2,
			Card flop1, Card flop2, Card flop3, Card turn, Card river,
			Spectrum spektr) {
		return strengthVsSpectrum(myCard1, myCard2, new Card[] { flop1, flop2,
				flop3, turn, river }, spektr);
	}

	public static double strengthVsSpectrum(HoleCards heroCards, Card[] board,
			Spectrum spectrum) {
		if (board.length == 0) {
			return EquilatorPreflop.strengthVsSpectrum(heroCards.first, heroCards.second,
					spectrum);
		} else {
			return strengthVsSpectrum(heroCards.first, heroCards.second, board,
					spectrum);
		}
	}

	private static StreetEquity equityVsRandom(Card heroCard1, Card heroCard2,
			Card[] boardCards) {
		long start = System.currentTimeMillis();
		boolean positive = false, drawBoolean;
		int boardSize = boardCards.length;
		int[] allHeroCards = new int[2 + boardSize + 1];
		int[] allOpponentCards = new int[2 + boardSize + 1];
		int[] allHeroCardsWithoutAdditional = new int[2 + boardSize];
		int[] allOpponentCardsWithoutAdditional = new int[2 + boardSize];

		allHeroCards[0] = heroCard1.intValueForBrecher();
		allHeroCards[1] = heroCard2.intValueForBrecher();
		allHeroCardsWithoutAdditional[0] = heroCard1.intValueForBrecher();
		allHeroCardsWithoutAdditional[1] = heroCard2.intValueForBrecher();
		int i = 0;
		for (Card boardCard : boardCards) {
			allHeroCards[2 + i] = boardCard.intValueForBrecher();
			allOpponentCards[2 + i] = boardCard.intValueForBrecher();
			allHeroCardsWithoutAdditional[2 + i] = boardCard
					.intValueForBrecher();
			allOpponentCardsWithoutAdditional[2 + i] = boardCard
					.intValueForBrecher();
			i++;
		}
		int[] nonUsedCards = Deck.getIntCardsForBrecher();

		int count = 0, wins = 0, draw = 0;
		double countPositive = 0, winsPositive = 0;
		double countNegative = 0, winsNegative = 0;
		int myCombinationWithoutAdditional = combination(allHeroCardsWithoutAdditional);
		for (int f = 0; f < nonUsedCards.length; f++) {
			int opponentCard1 = nonUsedCards[f];
			if (ArrayUtils.containsElement(allHeroCardsWithoutAdditional,
					opponentCard1)) {
				continue;
			}
			for (int s = f + 1; s < nonUsedCards.length; s++) {
				int opponentCard2 = nonUsedCards[s];
				if (ArrayUtils.containsElement(allHeroCardsWithoutAdditional,
						opponentCard2)) {
					continue;
				}
				allOpponentCardsWithoutAdditional[0] = opponentCard1;
				allOpponentCardsWithoutAdditional[1] = opponentCard2;
				allOpponentCards[0] = opponentCard1;
				allOpponentCards[1] = opponentCard2;
				int opponentCombinationWithoutAdditional = combination(allOpponentCardsWithoutAdditional);
				if (myCombinationWithoutAdditional < opponentCombinationWithoutAdditional) {
					positive = true;
					drawBoolean = false;
				} else if (myCombinationWithoutAdditional > opponentCombinationWithoutAdditional) {
					positive = false;
					wins++;
					drawBoolean = false;
				} else {
					draw++;
					drawBoolean = true;
				}
				count++;
				int myCombination;
				int opponentCombination;
				for (int t = 0; t < nonUsedCards.length; t++) {
					int turn = nonUsedCards[t];
					if (ArrayUtils.containsElement(
							allHeroCardsWithoutAdditional, turn)) {
						continue;
					}
					if (turn == opponentCard1 || turn == opponentCard2) {
						continue;
					}
					allHeroCards[2 + boardSize] = turn;
					allOpponentCards[2 + boardSize] = turn;
					myCombination = combination(allHeroCards);
					opponentCombination = combination(allOpponentCards);
					// compare the calculated or cached combinations
					if (drawBoolean) {
						countPositive += 0.5;
						countNegative += 0.5;
						if (myCombination > opponentCombination) {
							winsPositive += 0.5;
						}
						if (myCombination < opponentCombination) {
							winsNegative += 0.5;
						}
					} else if (positive) {
						countPositive++;
						if (myCombination > opponentCombination) {
							winsPositive++;
						}
						if (myCombination == opponentCombination) {
							winsPositive += 0.5;
						}
					} else {
						countNegative++;
						if (myCombination < opponentCombination) {
							winsNegative++;
						}
						if (myCombination == opponentCombination) {
							winsNegative += 0.5;
						}
					}
				}
			}
		}
		if (LOGGING) {
			Log.d("streetEquity() works in "
					+ (System.currentTimeMillis() - start) + " ms");
			Log.d("Wins = " + wins + " Count = " + count + " Draw = " + draw);
			Log.d("WinsPositive = " + winsPositive + " CountPositive = "
					+ countPositive);
			Log.d("WinsNegative = " + winsNegative + " CountNegative = "
					+ countNegative);
		}
		StreetEquity result = new StreetEquity();
		result.positivePotential = (countPositive != 0) ? (double) winsPositive
				/ countPositive : 1;
		result.negativePotential = (countNegative != 0) ? (double) winsNegative
				/ countNegative : 1;
		result.strength = ((double) draw / 2 + wins) / count;
		result.draw = (double) draw / count;
		return result;
	}

	private static StreetEquity equityVsRandomFullPotential(Card heroCard1,
			Card heroCard2, Card[] boardCards) {
		boolean positive = false, drawBoolean;
		int boardSize = boardCards.length;
		if (boardSize != 3) {
			throw new IllegalArgumentException(
					"For full potential must be 3 baord cards");
		}
		int[] allHeroCards = new int[2 + boardSize + 2];
		int[] allOpponentCards = new int[2 + boardSize + 2];
		int[] allHeroCardsWithoutAdditional = new int[2 + boardSize];
		int[] allOpponentCardsWithoutAdditional = new int[2 + boardSize];

		allHeroCards[0] = heroCard1.intValueForBrecher();
		allHeroCards[1] = heroCard2.intValueForBrecher();
		allHeroCardsWithoutAdditional[0] = heroCard1.intValueForBrecher();
		allHeroCardsWithoutAdditional[1] = heroCard2.intValueForBrecher();
		int i = 0;
		for (Card boardCard : boardCards) {
			allHeroCards[2 + i] = boardCard.intValueForBrecher();
			allOpponentCards[2 + i] = boardCard.intValueForBrecher();
			allHeroCardsWithoutAdditional[2 + i] = boardCard
					.intValueForBrecher();
			allOpponentCardsWithoutAdditional[2 + i] = boardCard
					.intValueForBrecher();
			i++;
		}
		int[] nonUsedCards = Deck.getIntCardsForBrecher();

		int count = 0, wins = 0, draw = 0;
		double countPositive = 0, winsPositive = 0;
		double countNegative = 0, winsNegative = 0;
		int myCombinationWithoutAdditional = combination(allHeroCardsWithoutAdditional);
		for (int f = 0; f < nonUsedCards.length; f++) {
			int opponentCard1 = nonUsedCards[f];
			if (ArrayUtils.containsElement(allHeroCardsWithoutAdditional,
					opponentCard1)) {
				continue;
			}
			for (int s = f + 1; s < nonUsedCards.length; s++) {
				int opponentCard2 = nonUsedCards[s];
				if (ArrayUtils.containsElement(allHeroCardsWithoutAdditional,
						opponentCard2)) {
					continue;
				}
				allOpponentCardsWithoutAdditional[0] = opponentCard1;
				allOpponentCardsWithoutAdditional[1] = opponentCard2;
				allOpponentCards[0] = opponentCard1;
				allOpponentCards[1] = opponentCard2;
				int opponentCombinationWithoutAdditional = combination(allOpponentCardsWithoutAdditional);
				if (myCombinationWithoutAdditional < opponentCombinationWithoutAdditional) {
					positive = true;
					drawBoolean = false;
				} else if (myCombinationWithoutAdditional > opponentCombinationWithoutAdditional) {
					positive = false;
					wins++;
					drawBoolean = false;
				} else {
					draw++;
					drawBoolean = true;
				}
				count++;
				int myCombination;
				int opponentCombination;
				for (int t = 0; t < nonUsedCards.length; t++) {
					int turn = nonUsedCards[t];
					if (ArrayUtils.containsElement(
							allHeroCardsWithoutAdditional, turn)) {
						continue;
					}
					if (turn == opponentCard1 || turn == opponentCard2) {
						continue;
					}
					allHeroCards[2 + boardSize] = turn;
					allOpponentCards[2 + boardSize] = turn;
					for (int r = 0; r < nonUsedCards.length; r++) {
						int river = nonUsedCards[r];
						if (ArrayUtils.containsElement(
								allHeroCardsWithoutAdditional, river)) {
							continue;
						}
						if (river == opponentCard1 || river == opponentCard2
								|| river == turn) {
							continue;
						}
						allHeroCards[3 + boardSize] = river;
						allOpponentCards[3 + boardSize] = river;
						myCombination = combination(allHeroCards);
						opponentCombination = combination(allOpponentCards);
						// compare the calculated or cached combinations
						if (drawBoolean) {
							countPositive += 0.5;
							countNegative += 0.5;
							if (myCombination > opponentCombination) {
								winsPositive += 0.5;
							}
							if (myCombination < opponentCombination) {
								winsNegative += 0.5;
							}
						} else if (positive) {
							countPositive++;
							if (myCombination > opponentCombination) {
								winsPositive++;
							}
							if (myCombination == opponentCombination) {
								winsPositive += 0.5;
							}
						} else {
							countNegative++;
							if (myCombination < opponentCombination) {
								winsNegative++;
							}
							if (myCombination == opponentCombination) {
								winsNegative += 0.5;
							}
						}
					}
				}
			}
		}
		if (LOGGING) {
			Log.d("Wins = " + wins + " Count = " + count + " Draw = " + draw);
			Log.d("WinsPositive = " + winsPositive + " CountPositive = "
					+ countPositive);
			Log.d("WinsNegative = " + winsNegative + " CountNegative = "
					+ countNegative);
		}
		StreetEquity result = new StreetEquity();
		result.positivePotential = (countPositive != 0) ? (double) winsPositive
				/ countPositive : 1;
		result.negativePotential = (countNegative != 0) ? (double) winsNegative
				/ countNegative : 1;
		result.strength = ((double) draw / 2 + wins) / count;
		result.draw = (double) draw / count;
		return result;
	}

	public static StreetEquity equityVsSpectrum(HoleCards heroCards,
			Card[] boardCards, Spectrum spectrum) {
		return equityVsSpectrum(heroCards.first, heroCards.second, boardCards,
				spectrum);
	}

	private static StreetEquity equityVsSpectrum(Card heroCard1,
			Card heroCard2, Card[] boardCards, Spectrum spektr) {
		long start = System.currentTimeMillis();
		boolean positive;
		int boardSize = boardCards.length;
		int[] allHeroCards = new int[2 + boardSize + 1];
		int[] allOpponentCards = new int[2 + boardSize + 1];
		int[] allHeroCardsWithoutAdditional = new int[2 + boardSize];
		int[] allOpponentCardsWithoutAdditional = new int[2 + boardSize];

		allHeroCards[0] = heroCard1.intValueForBrecher();
		allHeroCards[1] = heroCard2.intValueForBrecher();
		allHeroCardsWithoutAdditional[0] = heroCard1.intValueForBrecher();
		allHeroCardsWithoutAdditional[1] = heroCard2.intValueForBrecher();
		int i = 0;
		for (Card boardCard : boardCards) {
			allHeroCards[2 + i] = boardCard.intValueForBrecher();
			allOpponentCards[2 + i] = boardCard.intValueForBrecher();
			allHeroCardsWithoutAdditional[2 + i] = boardCard
					.intValueForBrecher();
			allOpponentCardsWithoutAdditional[2 + i] = boardCard
					.intValueForBrecher();
			i++;
		}
		int[] nonUsedCards = Deck.getIntCardsForBrecher();

		double count = 0, draw = 0, wins = 0;
		double countPositive = 0, winsPositive = 0;
		double countNegative = 0, winsNegative = 0;
		long myCombinationWithoutAdditional = combination(allHeroCardsWithoutAdditional);
		for (HoleCards villainCards : spektr) {
			double frequency = spektr.getWeight(villainCards);
			Card opponentCard1 = villainCards.first;
			Card opponentCard2 = villainCards.second;
			allOpponentCardsWithoutAdditional[0] = opponentCard1
					.intValueForBrecher();
			allOpponentCardsWithoutAdditional[1] = opponentCard2
					.intValueForBrecher();
			allOpponentCards[0] = opponentCard1.intValue();
			allOpponentCards[1] = opponentCard2.intValue();
			long oppCombination = combination(allOpponentCardsWithoutAdditional);
			if (myCombinationWithoutAdditional < oppCombination) {
				positive = true;
			} else if (myCombinationWithoutAdditional > oppCombination) {
				positive = false;
				wins += frequency;
			} else {
				draw += frequency;
				count += frequency;
				continue;
			}
			count += frequency;
			int myCombination;
			int opponentCombination;
			for (int turn : nonUsedCards) {
				if (ArrayUtils.containsElement(allHeroCardsWithoutAdditional,
						turn)
						|| ArrayUtils.containsElement(
								allOpponentCardsWithoutAdditional, turn)) {
					continue;
				}
				allHeroCards[2 + boardSize] = turn;
				allOpponentCards[2 + boardSize] = turn;
				myCombination = combination(allHeroCards);
				opponentCombination = combination(allOpponentCards);
				// compare the calculated or cached combinations
				if (positive) {
					countPositive += frequency;
					if (myCombination > opponentCombination) {
						winsPositive += frequency;
					}
				} else {
					countNegative += frequency;
					if (myCombination < opponentCombination) {
						winsNegative += frequency;
					}
				}
			}
		}
		if (LOGGING) {
			Log.d("streetEquity() works in "
					+ (System.currentTimeMillis() - start) + " ms");
			Log.d("Wins = " + wins + " Count = " + count);
			Log.d("WinsPositive = " + winsPositive + " CountPositive = "
					+ countPositive);
			Log.d("WinsNegative = " + winsNegative + " CountNegative = "
					+ countNegative);
		}
		StreetEquity result = new StreetEquity();
		result.positivePotential = (countPositive != 0) ? (double) winsPositive
				/ countPositive : 1;
		result.negativePotential = (countNegative != 0) ? (double) winsNegative
				/ countNegative : 1;
		result.strength = ((double) draw / 2 + wins) / count;
		result.draw = (double) draw / count;
		return result;
	}

	public static void main(String[] args) {
		// for (Value value1 : Value.getValues()) {
		// for (Value value2 : Value.getValues()) {
		// if (value1.intValue() < value2.intValue()) {
		// continue;
		// }
		// Card c1 = new Card(value1, Suit.CLUBS);
		// Card c2 = new Card(value2, Suit.DIAMONDS);
		// Log.d(new HoleCards(c1, c2).hashCodeForValues() + "");
		// }
		// }
		// double stre = calculateStrength(Card.valueOf("As"),
		// Card.valueOf("Kd"),
		// Card.valueOf("Jc"), Card.valueOf("Jh"));
		// Log.d("Preflop strength: " + stre);
		// generatePreflopTableStrength();
		Spectrum spectrum = Spectrum.random();
		Card c1 = Card.valueOf("4s");
		Card c2 = Card.valueOf("5d");
		Card f1 = Card.valueOf("Ah");
		Card f2 = Card.valueOf("6d");
		Card f3 = Card.valueOf("7d");
		spectrum.remove(c1);
		spectrum.remove(c2);
		long time = System.currentTimeMillis();
		int[] cards = new int[] { 2, 3, 4, 5, 6, 7, 8 };
		for (int i = 0; i < 170 * 50 * 50 * 2 * 170; i++) {
			combination(cards);
		}
		Log.d("Time: " + (System.currentTimeMillis() - time));
		Log.d(strengthOnFlop(c1, c2, f1, f2, f3) + "");

	}
}

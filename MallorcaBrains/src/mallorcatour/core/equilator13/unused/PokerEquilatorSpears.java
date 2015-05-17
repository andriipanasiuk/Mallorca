/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.equilator13.unused;

import mallorcatour.core.equilator13.StreetEquity;
import mallorcatour.game.core.Card;
import mallorcatour.game.core.Deck;
import mallorcatour.game.core.Flop;
import mallorcatour.game.core.HoleCards;
import mallorcatour.game.core.Spectrum;
import mallorcatour.util.ArrayUtils;
import pokerai.game.eval.spears2p2.StateTableEvaluator;

/**
 *
 * @author техно
 */
public class PokerEquilatorSpears {

    public static long time = 0;
    public static long timeCount = 0;
    private static int MIN_PREFLOP_STRENGTH = preflopStrengthForSorted(Card.valueOf("3s"), Card.valueOf("2d"));
    private static int MAX_PREFLOP_STRENGTH = preflopStrengthForSorted(Card.valueOf("Ah"), Card.valueOf("Ac"));

    static {
        StateTableEvaluator.initialize();
    }

    public static int combination(int[] allCards) {
        int numCards = allCards.length;
        {
            // Find the hand value
            int u = 53;
            for (int ii = 0; ii < numCards; ++ii) {
                u = StateTableEvaluator.handRanks[u + allCards[ii] + 1];
            }
            // Extra lookup pre-river
            if (numCards < 7) {
                throw new IllegalArgumentException();
//                return StateTableEvaluator.handRanks[u + 1];
            }
            return u;
        }
    }

    //First card bigger than the second
    private static int preflopStrengthForSorted(Card holeCard1, Card holeCard2) {
        if (!holeCard1.sameValue(holeCard2)) {
            if (!holeCard1.isSuitedWith(holeCard2)) {
                return holeCard1.getValue().intValue() * 2 + holeCard2.getValue().intValue();
            } else {
                return holeCard1.getValue().intValue() * 2 + holeCard2.getValue().intValue() + 2;
            }
        } else {
            return holeCard1.getValue().intValue() * 2 + holeCard2.getValue().intValue() + 22;
        }
    }

    public static double preflopStrength(Card holeCard1, Card holeCard2) {
        if (holeCard1.compareTo(holeCard2) == 0) {
            throw new IllegalArgumentException("Hole cards must not be equals. "
                    + "First: " + holeCard1 + "; second: " + holeCard2);
        }
        int strength;
        if (holeCard1.compareTo(holeCard2) > 0) {
            strength = preflopStrengthForSorted(holeCard1, holeCard2);
        } else {
            strength = preflopStrengthForSorted(holeCard2, holeCard1);
        } //divide to maximum preflop strength
        return ((double) strength) / (MAX_PREFLOP_STRENGTH);
    }

    private static double preflopStrengthVsSpectrum(Card heroCard1, Card heroCard2,
            Spectrum villainSpectrum) {
        double myStrength = preflopStrength(heroCard1, heroCard2);
        double count = 0, wins = 0;

        for (HoleCards opponentHoleCards : villainSpectrum) {
            Card opponentCard1 = opponentHoleCards.first;
            Card opponentCard2 = opponentHoleCards.second;
            double villainStrength = preflopStrength(opponentCard1, opponentCard2);
            double frequency = villainSpectrum.getWeight(opponentHoleCards);
            count += frequency * (myStrength + villainStrength);
            wins += myStrength * frequency;
        }
        return ((double) wins) / count;
    }

    public static double strengthVsRandom(Card heroCard1, Card heroCard2, Card[] boardCards) {
        int boardSize = boardCards.length;
        int[] heroAllCards = new int[2 + boardSize];
        int[] villainAllCards = new int[2 + boardSize];

        heroAllCards[0] = heroCard1.intValue();
        heroAllCards[1] = heroCard2.intValue();
        for (int i = 0; i < boardSize; i++) {
            heroAllCards[2 + i] = boardCards[i].intValue();
            villainAllCards[2 + i] = boardCards[i].intValue();
        }
        //
        int[] nonUsedCards = Deck.getIntCards();
        int count = 0, wins = 0, draw = 0;
        int myCombination = combination(heroAllCards);
        int opponentCombination;
        for (int i = 0; i < nonUsedCards.length; i++) {
            int opponentCard1 = nonUsedCards[i];
            if (ArrayUtils.containsElement(heroAllCards, opponentCard1)) {
                continue;
            }
            for (int j = i + 1; j < nonUsedCards.length; j++) {
                int opponentCard2 = nonUsedCards[j];
                if (ArrayUtils.containsElement(heroAllCards, opponentCard2)) {
                    continue;
                }
                villainAllCards[0] = opponentCard1;
                villainAllCards[1] = opponentCard2;
                opponentCombination = combination(villainAllCards);
                if (myCombination > opponentCombination) {
                    wins++;
                } else if (myCombination == opponentCombination) {
                    draw++;
                }
                count++;
            }
        }
//        Log.d("equityVsRandom() calculates " + count + " games. Wins = " + wins);
        return ((double) draw / 2 + wins) / count;
    }

    public static double strengthVsSpectrum(Card heroCard1, Card heroCard2,
            Card[] boardCards, Spectrum opponentSpectrum) {
        if (boardCards.length == 0) {
            return preflopStrengthVsSpectrum(heroCard1, heroCard2, opponentSpectrum);
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
                double frequency = opponentSpectrum.getWeight(opponentHoleCards);
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
//        Log.d("equityVsRandom() wins = " + wins + " draw = " + draw + " count = " + count);
            return ((double) draw / 2 + wins) / count;
        }
    }

    public static StreetEquity equityOnFlop(Card myCard1, Card myCard2, Card flop1, Card flop2, Card flop3) {
        return equityVsRandom(myCard1, myCard2, new Card[]{flop1, flop2, flop3});
    }

    public static StreetEquity equityOnTurn(Card myCard1, Card myCard2, Card flop1, Card flop2, Card flop3, Card turn) {
        return equityVsRandom(myCard1, myCard2, new Card[]{flop1, flop2, flop3, turn});
    }

    public static double strengthOnFlopVsRandom(Card myCard1, Card myCard2,
            Card flop1, Card flop2, Card flop3) {
        return strengthVsRandom(myCard1, myCard2, new Card[]{flop1, flop2, flop3});
    }

    public static double strengthOnTurnVsRandom(Card myCard1, Card myCard2,
            Card flop1, Card flop2, Card flop3, Card turn) {
        return strengthVsRandom(myCard1, myCard2, new Card[]{flop1, flop2, flop3, turn});
    }

    public static double strengthOnRiverVsRandom(Card myCard1, Card myCard2, Card flop1, Card flop2, Card flop3, Card turn, Card river) {
        return strengthVsRandom(myCard1, myCard2, new Card[]{flop1, flop2, flop3, turn, river});
    }

    public static StreetEquity equityOnFlopVsSpectrum(Card myCard1, Card myCard2,
            Card flop1, Card flop2, Card flop3, Spectrum spektr) {
        return equityVsSpectrum(myCard1, myCard2, new Card[]{flop1, flop2, flop3}, spektr);
    }

    public static StreetEquity equityVsSpectrum(HoleCards heroCards,
            Flop flop, Spectrum spektr) {
        return equityVsSpectrum(heroCards.first, heroCards.second,
                new Card[]{flop.first, flop.second, flop.third}, spektr);
    }

    public static StreetEquity equityVsSpectrum(HoleCards heroCards,
            Flop flop, Card turn, Spectrum spektr) {
        return equityVsSpectrum(heroCards.first, heroCards.second,
                new Card[]{flop.first, flop.second, flop.third, turn}, spektr);
    }

    public static StreetEquity equityVsSpectrum(HoleCards heroCards,
            Flop flop, Card turn, Card river, Spectrum spektr) {
        return equityVsSpectrum(heroCards.first, heroCards.second,
                new Card[]{flop.first, flop.second, flop.third, turn, river}, spektr);
    }

    public static StreetEquity equityOnTurnVsSpectrum(Card myCard1, Card myCard2,
            Card flop1, Card flop2, Card flop3, Card turn, Spectrum spektr) {
        return equityVsSpectrum(myCard1, myCard2, new Card[]{flop1, flop2, flop3, turn}, spektr);
    }

    public static double strengthOnRiverVsSpectrum(Card myCard1, Card myCard2,
            Card flop1, Card flop2, Card flop3, Card turn, Card river, Spectrum spektr) {
        return strengthVsSpectrum(myCard1, myCard2, new Card[]{flop1, flop2, flop3, turn, river}, spektr);
    }

    public static double strengthVsSpectrum(HoleCards heroCards, Card[] board, Spectrum spectrum) {
        if (board.length == 0) {
            return preflopStrengthVsSpectrum(heroCards.first, heroCards.second, spectrum);
        } else {
            return strengthVsSpectrum(heroCards.first, heroCards.second, board, spectrum);
        }
    }

    private static StreetEquity equityVsRandom(Card heroCard1, Card heroCard2, Card[] boardCards) {
        boolean positive;
        //initializing hero and opp cards
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
            allHeroCardsWithoutAdditional[2 + i] = boardCard.intValueForBrecher();
            allOpponentCardsWithoutAdditional[2 + i] = boardCard.intValueForBrecher();
            i++;
        }
        //initializing cards in the deck
        int[] nonUsedCards = Deck.getIntCardsForBrecher();

        int count = 0, wins = 0, draw = 0;
        int countPositive = 0, winsPositive = 0;
        int countNegative = 0, winsNegative = 0;
        //calculate my combination without one more card
        int myCombinationWithoutAdditional = combination(allHeroCardsWithoutAdditional);
        for (int f = 0; f < nonUsedCards.length; f++) {
            int opponentCard1 = nonUsedCards[f];
            if (ArrayUtils.containsElement(allHeroCardsWithoutAdditional, opponentCard1)) {
                continue;
            }
            for (int s = f + 1; s < nonUsedCards.length; s++) {
                int opponentCard2 = nonUsedCards[s];
                if (ArrayUtils.containsElement(allHeroCardsWithoutAdditional, opponentCard2)) {
                    continue;
                }
                allOpponentCardsWithoutAdditional[0] = opponentCard1;
                allOpponentCardsWithoutAdditional[1] = opponentCard2;
                allOpponentCards[0] = opponentCard1;
                allOpponentCards[1] = opponentCard2;
                int opponentCombinationWithoutAdditional = combination(allOpponentCardsWithoutAdditional);
                if (myCombinationWithoutAdditional < opponentCombinationWithoutAdditional) {
                    positive = true;
                } else if (myCombinationWithoutAdditional > opponentCombinationWithoutAdditional) {
                    positive = false;
                    wins++;
                } else {
                    count++;
                    draw++;
                    continue;
                }
                count++;
                int myCombination;
                int opponentCombination;
                for (int t = 0; t < nonUsedCards.length; t++) {
                    int turn = nonUsedCards[t];
                    if (ArrayUtils.containsElement(allHeroCardsWithoutAdditional, turn)) {
                        continue;
                    }
                    if (turn == opponentCard1 || turn == opponentCard2) {
                        continue;
                    }
                    allHeroCards[2 + boardSize] = turn;
                    allOpponentCards[2 + boardSize] = turn;
                    myCombination = combination(allHeroCards);
                    opponentCombination = combination(allOpponentCards);
                    //compare the calculated or cached combinations
                    if (positive) {
                        countPositive++;
                        if (myCombination > opponentCombination) {
                            winsPositive++;
                        }
                    } else {
                        countNegative++;
                        if (myCombination < opponentCombination) {
                            winsNegative++;
                        }
                    }
                }
            }
        }
//        Log.d("streetEquity() works in " + (System.currentTimeMillis() - start) + " ms");
//        Log.d("Wins = " + wins + " Count = " + count + " Draw = " + draw);
//        Log.d("WinsPositive = " + winsPositive + " CountPositive = " + countPositive);
//        Log.d("WinsNegative = " + winsNegative + " CountNegative = " + countNegative);
        StreetEquity result = new StreetEquity();
        result.positivePotential = (countPositive != 0) ? (double) winsPositive / countPositive : 1;
        result.negativePotential = (countNegative != 0) ? (double) winsNegative / countNegative : 1;
        result.strength = ((double) draw / 2 + wins) / count;
        result.draw = (double) draw / count;
        return result;
    }

    public static StreetEquity equityVsSpectrum(HoleCards heroCards,
            Card[] boardCards, Spectrum spectrum) {
        return equityVsSpectrum(heroCards.first, heroCards.second, boardCards,
                spectrum);
    }

    private static StreetEquity equityVsSpectrum(Card heroCard1, Card heroCard2,
            Card[] boardCards, Spectrum spektr) {
        boolean positive;
        //initializing hero and opp cards
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
            allHeroCardsWithoutAdditional[2 + i] = boardCard.intValueForBrecher();
            allOpponentCardsWithoutAdditional[2 + i] = boardCard.intValueForBrecher();
            i++;
        }
        //initializing cards in the deck
        int[] nonUsedCards = Deck.getIntCardsForBrecher();

        double count = 0, draw = 0, wins = 0;
        double countPositive = 0, winsPositive = 0;
        double countNegative = 0, winsNegative = 0;
        //calculate my combination without one more card
        long myCombinationWithoutAdditional = combination(allHeroCardsWithoutAdditional);
        for (HoleCards villainCards : spektr) {
            double frequency = spektr.getWeight(villainCards);
            Card opponentCard1 = villainCards.first;
            Card opponentCard2 = villainCards.second;
            allOpponentCardsWithoutAdditional[0] = opponentCard1.intValueForBrecher();
            allOpponentCardsWithoutAdditional[1] = opponentCard2.intValueForBrecher();
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
                if (ArrayUtils.containsElement(allHeroCardsWithoutAdditional, turn)
                        || ArrayUtils.containsElement(allOpponentCardsWithoutAdditional, turn)) {
                    continue;
                }
                allHeroCards[2 + boardSize] = turn;
                allOpponentCards[2 + boardSize] = turn;
                myCombination = combination(allHeroCards);
                opponentCombination = combination(allOpponentCards);
                //compare the calculated or cached combinations
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
//        Log.d("streetEquity() works in " + (System.currentTimeMillis() - start) + " ms");
//        Log.d("Wins = " + wins + " Count = " + count);
//        Log.d("WinsPositive = " + winsPositive + " CountPositive = " + countPositive);
//        Log.d("WinsNegative = " + winsNegative + " CountNegative = " + countNegative);
        StreetEquity result = new StreetEquity();
        result.positivePotential = (countPositive != 0) ? (double) winsPositive / countPositive : 1;
        result.negativePotential = (countNegative != 0) ? (double) winsNegative / countNegative : 1;
        result.strength = ((double) draw / 2 + wins) / count;
        result.draw = (double) draw / count;
        return result;
    }
}

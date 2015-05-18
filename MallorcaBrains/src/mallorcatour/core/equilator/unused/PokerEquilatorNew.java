/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.equilator.unused;

import mallorcatour.core.equilator.StreetEquity;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.Deck;
import mallorcatour.core.game.Flop;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.Card.Suit;
import mallorcatour.core.game.Card.Value;
import mallorcatour.core.spectrum.Spectrum;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mallorcatour.util.ArrayUtils;
import mallorcatour.util.CollectionUtils;
import mallorcatour.util.Log;

/**
 *
 * @author техно
 */
public class PokerEquilatorNew {

    private static final int HUNDRED = 100;
    private static final int HUNDRED_IN_POWER_4 = 100000000;
    private static final int HUNDRED_IN_POWER_3 = 1000000;
    public static long time = 0;

    //not obviously sorted
    private static int getSuitOfFlash(int[] allCards) {
        int[] flashCards = new int[allCards.length];
        for (int i = 0; i < allCards.length; i++) {
            flashCards[i] = allCards[i] % 4;
        }
        Arrays.sort(flashCards);
        for (int i = 0; i < flashCards.length - 4; i++) {
            if (flashCards[i] == flashCards[i + 4]) {
                return flashCards[i];
            }
        }
        return -1;
    }

    //not obviously sorted
    private static long isFlash(int[] allCards) {
        int[] copy = new int[allCards.length];
        int i = 0;
        for (int value : allCards) {
            copy[i++] = value;
        }
        int flashSuit = getSuitOfFlash(copy);
        if (flashSuit == -1) {
            return -1;
        }
        int sovpad = 0;
        int sum = 0;
        Arrays.sort(copy);
        for (int j = copy.length - 1; j >= 0; j--) {
            if (copy[j] % 4 == flashSuit) {
                sovpad++;
                sum += copy[j] / 4 * Math.pow(100, (5 - sovpad));
                if (sovpad == 5) {
                    return 60000000000L + sum;
                }
            }
        }
        return -1;
    }

    //not obviously sorted
    private static long isBicycleStraight(int[] cardValues) {
        if (ArrayUtils.containsElement(cardValues, Value.FIVE.intValue())
                && ArrayUtils.containsElement(cardValues, Value.FOUR.intValue())
                && ArrayUtils.containsElement(cardValues, Value.THREE.intValue())
                && ArrayUtils.containsElement(cardValues, Value.TWO.intValue())
                && ArrayUtils.containsElement(cardValues, Value.ACE.intValue())) {
            return 50500000000L;
        }
        return -1;
    }

    //must be sorted
    private static long isStraight(int[] cardValues) {
        int length = cardValues.length;
        long bicycleStraight = isBicycleStraight(cardValues);
        for (int i = 0; i <= length - 5; i++) {
            int oneDownValue = cardValues[i] - 1;
            if (oneDownValue == 1) {
                return bicycleStraight;
            }
            if (!ArrayUtils.containsElement(cardValues, oneDownValue)) {
                continue;
            }
            int twoDown = oneDownValue - 1;
            if (twoDown == 1) {
                return bicycleStraight;
            }
            if (!ArrayUtils.containsElement(cardValues, twoDown)) {
                continue;
            }
            int threeDown = twoDown - 1;
            if (threeDown == 1) {
                return bicycleStraight;
            }
            if (!ArrayUtils.containsElement(cardValues, threeDown)) {
                continue;
            }
            int fourDown = threeDown - 1;
            if (fourDown == 1) {
                return bicycleStraight;
            }
            if (!ArrayUtils.containsElement(cardValues, fourDown)) {
                continue;
            }
            return 50000000000L + cardValues[i] * HUNDRED_IN_POWER_4;
        }
        return bicycleStraight;
    }

    //must be sorted
    private static long isPair(int[] intValues) {
        int sum = 0;
        for (int i = 0; i < intValues.length - 1; i++) {
            if (intValues[i] != intValues[i + 1]) {
                sum += intValues[i] * (int) Math.pow(HUNDRED, 2 - i);
            } else {
                for (int j = i + 2; j < Math.min(5, intValues.length); j++) {
                    sum += intValues[j] * Math.pow(HUNDRED, 2 - (j - 2));
                }
                sum += intValues[i] * HUNDRED_IN_POWER_4;
                return 20000000000L + sum;
            }
        }
        return -1;
    }

    //must be sorted
    public static long isTwoPairs(int[] allCards, int highPairValue, long set) {
        if ((set == -1) && isCare(allCards) == -1) {
            int[] withoutHighPair = ArrayUtils.removeAll(allCards, highPairValue);
            int highNonPairCard = withoutHighPair[0];
            long secondPairValue = isPair(withoutHighPair);
            if (secondPairValue == -1) {
                return -1;
            }
            int highSecondPairValue = (int) (secondPairValue / HUNDRED_IN_POWER_4 % HUNDRED);
            if (highNonPairCard == highSecondPairValue) {
                int[] withoutPairs = ArrayUtils.removeAll(withoutHighPair, highSecondPairValue);
                highNonPairCard = withoutPairs[0];
            }
            return 30000000000L + highPairValue * HUNDRED_IN_POWER_4
                    + highSecondPairValue * HUNDRED_IN_POWER_3
                    + highNonPairCard;
        }
        return -1;
    }

    //must be sorted
    private static long isFullHouse(int[] allCards, int setCardValue, long care) {
        if (care == -1) {
            int[] withoutSetCard = ArrayUtils.removeAll(allCards, setCardValue);
            long pairValue = isPair(withoutSetCard);
            if (pairValue != -1) {
                long pairCard = pairValue / HUNDRED_IN_POWER_4 % HUNDRED;
                return 70000000000L + setCardValue * HUNDRED_IN_POWER_4
                        + pairCard * HUNDRED_IN_POWER_3;
            }
        }
        return -1;
    }

    private static int[] prepareValueIntArray(int[] allCards) {
        int[] result = new int[allCards.length];
        int i = 0;
        for (int card : allCards) {
            result[i++] = card / 4;
        }
        Arrays.sort(result);
        return ArrayUtils.reverse(result);
    }

    //must be sorted
    private static long isSet(int[] copy) {
        int sum = 0;
        for (int i = 0; i < copy.length - 2; i++) {
            if (copy[i] != copy[i + 2]) {
                sum += copy[i] * Math.pow(100, 1 - i);
            } else {
                for (int j = i + 3; j < 5; j++) {
                    sum += copy[j] * Math.pow(100, 1 - (j - 3));
                }
                sum += copy[i] * HUNDRED_IN_POWER_4;
                return 40000000000L + sum;
            }
        }
        return -1;
    }

    //must be sorted
    private static long isCare(int[] cardValues) {
        int sum = 0;

        for (int i = 0; i < cardValues.length - 3; i++) {
            if (cardValues[i] != cardValues[i + 3]) {
                sum += cardValues[i] * Math.pow(HUNDRED, 0 - i);
            } else {
                for (int j = i + 4; j
                        < 5; j++) {
                    sum += cardValues[j] * Math.pow(HUNDRED, 0 - (j - 4));
                }
                sum += cardValues[i] * HUNDRED_IN_POWER_4;
                return 80000000000L + sum;
            }
        }
        return -1;
    }

    //not obviously sorted
    private static long isStraightFlash(int[] allCards) {
        int flashSuit = getSuitOfFlash(allCards);
        int count = 0;
        if (flashSuit != -1) {
            for (int card : allCards) {
                if (card % 4 == flashSuit) {
                    count++;
                }
            }
            int[] copy = new int[count];
            int i = 0;
            for (int card : allCards) {
                if (card % 4 == flashSuit) {
                    copy[i++] = card / 4;
                }
            }
            Arrays.sort(copy);
            copy = ArrayUtils.reverse(copy);
            long straightValue = isStraight(copy);
            if (straightValue != -1) {
                return 90000000000L + straightValue / HUNDRED_IN_POWER_4 % HUNDRED * HUNDRED_IN_POWER_4;
            }
        }
        return -1;
    }

    private static long isRoyalFlash(long straightFlash) {
        int highCard = (int) (straightFlash / HUNDRED_IN_POWER_4 % HUNDRED);
        if (Value.valueOf(highCard) == Value.ACE) {
            return 100000000000L;
        }
        return -1;
    }

    //must be sorted
    private static long getHighCardCombination(int[] cardRanges) {
        long sum = 0;
        for (int i = 0; i < 5; i++) {
            sum += cardRanges[i] * Math.pow(HUNDRED, 4 - i);
        }
        return sum;
    }

    public static long combination(int[] allCards) {
        long pair = -1, twoPairs = -1, set = -1, straight = -1, flash = -1,
                fullHouse = -1, care = -1, straightFlash = -1, royalFlash = -1;

        int[] cardRanges = prepareValueIntArray(allCards);

        pair = isPair(cardRanges);
        if (pair != -1) {
            //if is pair
            int pairValue = (int) (pair / HUNDRED_IN_POWER_4 % HUNDRED);
            set = isSet(cardRanges);
            twoPairs = isTwoPairs(cardRanges, pairValue, set);
            if (set != -1) {
                //if is set
                int setCard = (int) (set / HUNDRED_IN_POWER_4 % HUNDRED);
                care = isCare(cardRanges);
                fullHouse = isFullHouse(cardRanges, setCard, care);
            }
        }
        flash = isFlash(allCards);
        straight = isStraight(cardRanges);
        if (flash != -1 && straight != -1) {
            straightFlash = isStraightFlash(allCards);
            if (straightFlash != -1) {
                royalFlash = isRoyalFlash(straightFlash);
            }
        }
        long result = Collections.max(Arrays.asList(new Long[]{pair, twoPairs,
                    set, straight, flash, fullHouse, care, straightFlash,
                    royalFlash}));

        if (result != -1) {
            return result;
        } else {
            return getHighCardCombination(cardRanges);
        }
    }

    public static long combinationWithFlash(int[] allCards, long flashValue) {
        if (flashValue == -1) {
            throw new IllegalArgumentException("Flash value must be not -1!");
        }
        long set = -1, flash = flashValue, fullHouse = -1, care = -1,
                straightFlash = -1, royalFlash = -1;
        int[] cardRanges = prepareValueIntArray(allCards);

        set = isSet(cardRanges);
        if (set != -1) {
            //if is set
            int setCard = (int) (set / HUNDRED_IN_POWER_4 % HUNDRED);
            care = isCare(cardRanges);
            fullHouse = isFullHouse(cardRanges, setCard, care);
        }
        straightFlash = isStraightFlash(allCards);
        if (straightFlash != -1) {
            royalFlash = isRoyalFlash(straightFlash);
        }
        long result = Collections.max(Arrays.asList(new Long[]{flash, fullHouse,
                    care, straightFlash, royalFlash}));
        return result;
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
        return (double) strength / preflopStrengthForSorted(Card.valueOf("Ah"), Card.valueOf("As"));
    }

    private static boolean canBeFlash(Card[] boardCards) {
        int length = boardCards.length;
        Card[] sortedBySuit = Arrays.copyOf(boardCards, length);
        Arrays.sort(sortedBySuit, Suit.getComparator());
        for (int i = 0; i < length - 2; i++) {
            if (sortedBySuit[i].getSuit() == sortedBySuit[i + 2].getSuit()) {
                return true;
            }
        }
        return false;
    }

    public static double strengthVsRandom(Card heroCard1, Card heroCard2, Card[] boardCards) {
        int boardSize = boardCards.length;
        int[] allMyCards = new int[2 + boardSize];
        int[] allOpponentCards = new int[2 + boardSize];
        allMyCards[0] = heroCard1.intValue();
        allMyCards[1] = heroCard2.intValue();
        for (int i = 0; i < boardSize; i++) {
            allMyCards[2 + i] = boardCards[i].intValue();
            allOpponentCards[2 + i] = boardCards[i].intValue();
        }
        boolean canBeFlash = canBeFlash(boardCards);
        //init cache
        byte[] cache = new byte[52 * 52];
        byte defaultValue = 3;
        for (int i = 0; i < cache.length; i++) {
            cache[i] = defaultValue;
        }
        //
        List<Card> nonUsedCards = CollectionUtils.subtract(Deck.getCards(), boardCards);
        nonUsedCards = CollectionUtils.subtract(nonUsedCards, heroCard1);
        nonUsedCards = CollectionUtils.subtract(nonUsedCards, heroCard2);
        int count = 0, wins = 0;
        long myCombination = combination(allMyCards);
        Collections.sort(nonUsedCards);
        for (int i = 0; i < nonUsedCards.size(); i++) {
            for (int j = i + 1; j < nonUsedCards.size(); j++) {
                Card opponentCard1 = nonUsedCards.get(i);
                Card opponentCard2 = nonUsedCards.get(j);
                allOpponentCards[0] = opponentCard1.intValue();
                allOpponentCards[1] = opponentCard2.intValue();
                long opponentCombination;
                long flashValue;
                if (canBeFlash && (flashValue = isFlash(allOpponentCards)) != -1) {
                    opponentCombination = combinationWithFlash(allOpponentCards, flashValue);
                    if (myCombination > opponentCombination) {
                        wins++;
                    }
                } else {
                    int hashCode = opponentCard1.getValue().intValue() * 15
                            + opponentCard2.getValue().intValue();
                    byte cachedResult = cache[hashCode];
                    if (cachedResult == defaultValue) {
                        opponentCombination = combination(allOpponentCards);
                        if (myCombination > opponentCombination) {
                            wins++;
                            cache[hashCode] = 1;
                        } else {
                            cache[hashCode] = 0;
                        }
                    } else {
                        wins += cachedResult;
                    }
                }
                count++;
            }
        }
        Log.d("equityVsRandom() calculates " + count + " games. Wins = " + wins);
        return (double) wins / count;
    }

    public static double strengthVsSpectrum(Card heroCard1, Card heroCard2,
            Card[] boardCards, Spectrum opponentSpektr) {
        int boardSize = boardCards.length;
        int[] allMyCards = new int[2 + boardSize];
        int[] allOpponentCards = new int[2 + boardSize];
        allMyCards[0] = heroCard1.intValue();
        allMyCards[1] = heroCard2.intValue();
        for (int i = 0; i < boardSize; i++) {
            allMyCards[2 + i] = boardCards[i].intValue();
            allOpponentCards[2 + i] = boardCards[i].intValue();
        }
        boolean canBeFlash = canBeFlash(boardCards);
        //init cache
        byte[] cache = new byte[52 * 52];
        byte defaultValue = 3;
        for (int i = 0; i < cache.length; i++) {
            cache[i] = defaultValue;
        }
        //
        double count = 0, wins = 0;
        long myCombination = combination(allMyCards);
        for (HoleCards opponentHoleCards : opponentSpektr) {
            Card opponentCard1 = opponentHoleCards.first;
            Card opponentCard2 = opponentHoleCards.second;
            allOpponentCards[0] = opponentCard1.intValue();
            allOpponentCards[1] = opponentCard2.intValue();
            double frequency = opponentSpektr.getWeight(opponentHoleCards);
            long opponentCombination;
            long flashValue;
            if (canBeFlash && (flashValue = isFlash(allOpponentCards)) != -1) {
                opponentCombination = combinationWithFlash(allOpponentCards, flashValue);
                if (myCombination > opponentCombination) {
                    wins += frequency;
                }
            } else {
                int hashCode = opponentCard1.getValue().intValue() * 15
                        + opponentCard2.getValue().intValue();
                byte cachedResult = cache[hashCode];
                if (cachedResult == defaultValue) {
                    opponentCombination = combination(allOpponentCards);
                    if (myCombination > opponentCombination) {
                        wins += frequency;
                        cache[hashCode] = 1;
                    } else {
                        cache[hashCode] = 0;
                    }
                } else {
                    wins += cachedResult * frequency;
                }
            }
            count += frequency;
        }
        Log.d("equityVsRandom() wins = " + wins + " count = " + count);
        return (double) wins / count;
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
        return strengthVsSpectrum(heroCards.first, heroCards.second, board, spectrum);
    }

    private static StreetEquity equityVsRandom(Card heroCard1, Card heroCard2, Card[] boardCards) {
        long start = System.currentTimeMillis();
        boolean positive;
        //initializing cache
        byte defaultCacheValue = -1;
        int cacheSize = 100;
        long[][] cacheForOpponentCombination = new long[cacheSize][52 * 52];
        long[] cacheForHeroCombination = new long[cacheSize];
        for (int k = 0; k < cacheSize; k++) {
            cacheForHeroCombination[k] = defaultCacheValue;
            for (int n = 0; n < cacheForOpponentCombination[k].length; n++) {
                cacheForOpponentCombination[k][n] = defaultCacheValue;
            }
        }
        //initializing hero and opp cards
        int boardSize = boardCards.length;
        int[] allHeroCards = new int[2 + boardSize + 1];
        int[] allOpponentCards = new int[2 + boardSize + 1];
        int[] allHeroCardsWithoutAdditional = new int[2 + boardSize];
        int[] allOpponentCardsWithoutAdditional = new int[2 + boardSize];

        allHeroCards[0] = heroCard1.intValue();
        allHeroCards[1] = heroCard2.intValue();
        allHeroCardsWithoutAdditional[0] = heroCard1.intValue();
        allHeroCardsWithoutAdditional[1] = heroCard2.intValue();
        int i = 0;
        for (Card boardCard : boardCards) {
            allHeroCards[2 + i] = boardCard.intValue();
            allOpponentCards[2 + i] = boardCard.intValue();
            allHeroCardsWithoutAdditional[2 + i] = boardCard.intValue();
            allOpponentCardsWithoutAdditional[2 + i] = boardCard.intValue();
            i++;
        }
        //initializing cards in the deck
        List<Card> nonUsedCards = CollectionUtils.subtract(Deck.getCards(), boardCards);
        nonUsedCards = CollectionUtils.subtract(nonUsedCards, heroCard1);
        nonUsedCards = CollectionUtils.subtract(nonUsedCards, heroCard2);

        int count = 0, wins = 0;
        int countPositive = 0, winsPositive = 0;
        int countNegative = 0, winsNegative = 0;
        //calculate my combination without one more card
        long myCombinationWithoutAdditional = combination(allHeroCardsWithoutAdditional);
        for (int f = 0; f < nonUsedCards.size(); f++) {
            for (int s = f + 1; s < nonUsedCards.size(); s++) {
                Card opponentCard1 = nonUsedCards.get(f);
                Card opponentCard2 = nonUsedCards.get(s);
                allOpponentCardsWithoutAdditional[0] = opponentCard1.intValue();
                allOpponentCardsWithoutAdditional[1] = opponentCard2.intValue();
                allOpponentCards[0] = opponentCard1.intValue();
                allOpponentCards[1] = opponentCard2.intValue();
                long oppCombination = combination(allOpponentCardsWithoutAdditional);
                if (myCombinationWithoutAdditional < oppCombination) {
                    positive = true;
                } else if (myCombinationWithoutAdditional > oppCombination) {
                    positive = false;
                    wins++;
                } else {
                    count++;
                    continue;
                }
                count++;
                for (Card turn : nonUsedCards) {
                    if (turn == opponentCard1 || turn == opponentCard2) {
                        continue;
                    }
                    allHeroCards[2 + boardSize] = turn.intValue();
                    allOpponentCards[2 + boardSize] = turn.intValue();

                    //get hero combination from cache
                    long myCombination;
                    myCombination = cacheForHeroCombination[turn.intValue()];
                    if (myCombination == defaultCacheValue) {
                        myCombination = combination(allHeroCards);
                        cacheForHeroCombination[turn.intValue()] = myCombination;
                    }
                    //get opponent combination from cache
                    long opponentCombination = defaultCacheValue;
                    long flashValue = isFlash(allOpponentCards);
                    if (flashValue == -1) {
                        int hashCode = opponentCard1.getValue().intValue() * 15
                                + opponentCard2.getValue().intValue();
                        opponentCombination = cacheForOpponentCombination[turn.getValue().intValue()][hashCode];
                        if (opponentCombination == defaultCacheValue) {
                            opponentCombination = combination(allOpponentCards);
                            cacheForOpponentCombination[turn.getValue().intValue()][hashCode] = opponentCombination;
                        }
                    } else {
                        opponentCombination = combinationWithFlash(allOpponentCards, flashValue);
                    }
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
//        Log.d("Wins = " + wins + " Count = " + count);
//        Log.d("WinsPositive = " + winsPositive + " CountPositive = " + countPositive);
//        Log.d("WinsNegative = " + winsNegative + " CountNegative = " + countNegative);
        StreetEquity result = new StreetEquity();
        result.positivePotential = (countPositive != 0) ? (double) winsPositive / countPositive : 1;
        result.negativePotential = (countNegative != 0) ? (double) winsNegative / countNegative : 1;
        result.strength = (double) wins / count;
        return result;
    }

    public static StreetEquity equityVsSpectrum(HoleCards heroCards,
            Card[] boardCards, Spectrum spektr) {
        return equityVsSpectrum(heroCards.first, heroCards.second, boardCards,
                spektr);
    }

    private static StreetEquity equityVsSpectrum(Card heroCard1, Card heroCard2,
            Card[] boardCards, Spectrum spektr) {
        long start = System.currentTimeMillis();
        boolean positive;
        //initializing cache
        byte defaultCacheValue = -1;
        int cacheSize = 100;
        long[][] cacheForOpponentCombination = new long[cacheSize][52 * 52];
        long[] cacheForHeroCombination = new long[cacheSize];
        for (int k = 0; k < cacheSize; k++) {
            cacheForHeroCombination[k] = defaultCacheValue;
            for (int n = 0; n < cacheForOpponentCombination[k].length; n++) {
                cacheForOpponentCombination[k][n] = defaultCacheValue;
            }
        }
        //initializing hero and opp cards
        int boardSize = boardCards.length;
        int[] allHeroCards = new int[2 + boardSize + 1];
        int[] allOpponentCards = new int[2 + boardSize + 1];
        int[] allHeroCardsWithoutAdditional = new int[2 + boardSize];
        int[] allOpponentCardsWithoutAdditional = new int[2 + boardSize];

        allHeroCards[0] = heroCard1.intValue();
        allHeroCards[1] = heroCard2.intValue();
        allHeroCardsWithoutAdditional[0] = heroCard1.intValue();
        allHeroCardsWithoutAdditional[1] = heroCard2.intValue();
        int i = 0;
        for (Card boardCard : boardCards) {
            allHeroCards[2 + i] = boardCard.intValue();
            allOpponentCards[2 + i] = boardCard.intValue();
            allHeroCardsWithoutAdditional[2 + i] = boardCard.intValue();
            allOpponentCardsWithoutAdditional[2 + i] = boardCard.intValue();
            i++;
        }
        //initializing cards in the deck
        List<Card> nonUsedCards = CollectionUtils.subtract(Deck.getCards(), boardCards);
        nonUsedCards = CollectionUtils.subtract(nonUsedCards, heroCard1);
        nonUsedCards = CollectionUtils.subtract(nonUsedCards, heroCard2);

        double count = 0, wins = 0;
        double countPositive = 0, winsPositive = 0;
        double countNegative = 0, winsNegative = 0;
        //calculate my combination without one more card
        long myCombinationWithoutAdditional = combination(allHeroCardsWithoutAdditional);
        for (HoleCards villainCards : spektr) {
            double frequency = spektr.getWeight(villainCards);
            Card opponentCard1 = villainCards.first;
            Card opponentCard2 = villainCards.second;
            allOpponentCardsWithoutAdditional[0] = opponentCard1.intValue();
            allOpponentCardsWithoutAdditional[1] = opponentCard2.intValue();
            allOpponentCards[0] = opponentCard1.intValue();
            allOpponentCards[1] = opponentCard2.intValue();
            long oppCombination = combination(allOpponentCardsWithoutAdditional);
            if (myCombinationWithoutAdditional < oppCombination) {
                positive = true;
            } else if (myCombinationWithoutAdditional > oppCombination) {
                positive = false;
                wins += frequency;
            } else {
                count += frequency;
                continue;
            }
            count += frequency;
            for (Card turn : nonUsedCards) {
                if (turn == opponentCard1 || turn == opponentCard2) {
                    continue;
                }
                allHeroCards[2 + boardSize] = turn.intValue();
                allOpponentCards[2 + boardSize] = turn.intValue();

                //get hero combination from cache
                long myCombination;
                myCombination = cacheForHeroCombination[turn.intValue()];
                if (myCombination == defaultCacheValue) {
                    myCombination = combination(allHeroCards);
                    cacheForHeroCombination[turn.intValue()] = myCombination;
                }
                //get opponent combination from cache
                long opponentCombination = defaultCacheValue;
                long flashValue = isFlash(allOpponentCards);
                if (flashValue == -1) {
                    int hashCode = opponentCard1.getValue().intValue() * 15
                            + opponentCard2.getValue().intValue();
                    opponentCombination = cacheForOpponentCombination[turn.getValue().intValue()][hashCode];
                    if (opponentCombination == defaultCacheValue) {
                        opponentCombination = combination(allOpponentCards);
                        cacheForOpponentCombination[turn.getValue().intValue()][hashCode] = opponentCombination;
                    }
                } else {
                    opponentCombination = combinationWithFlash(allOpponentCards, flashValue);
                }
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
        result.strength = (double) wins / count;
        return result;
    }

    private static double potentialWithOneCard(Card heroCard1, Card heroCard2,
            List<Card> boardCards, boolean positive) {
        time = 0;
        long start = System.currentTimeMillis();
        //initializing cache
        byte defaultCacheValue = -1;
        int cacheSize = 100;
        long[][] cacheForOpponentCombination = new long[cacheSize][52 * 52];
        long[] cacheForHeroCombination = new long[cacheSize];
        for (int k = 0; k < cacheSize; k++) {
            cacheForHeroCombination[k] = defaultCacheValue;
            for (int n = 0; n < cacheForOpponentCombination[k].length; n++) {
                cacheForOpponentCombination[k][n] = defaultCacheValue;
            }
        }
        //initializing hero and opp cards
        int boardSize = boardCards.size();
        int[] allHeroCards = new int[2 + boardSize + 1];
        int[] allOpponentCards = new int[2 + boardSize + 1];
        int[] allHeroCardsWithoutAdditional = new int[2 + boardSize];
        int[] allOpponentCardsWithoutAdditional = new int[2 + boardSize];

        allHeroCards[0] = heroCard1.intValue();
        allHeroCards[1] = heroCard2.intValue();
        allHeroCardsWithoutAdditional[0] = heroCard1.intValue();
        allHeroCardsWithoutAdditional[1] = heroCard2.intValue();
        int i = 0;
        for (Card boardCard : boardCards) {
            allHeroCards[2 + i] = boardCard.intValue();
            allOpponentCards[2 + i] = boardCard.intValue();
            allHeroCardsWithoutAdditional[2 + i] = boardCard.intValue();
            allOpponentCardsWithoutAdditional[2 + i] = boardCard.intValue();
            i++;
        }
        //initializing cards in the deck
        List<Card> nonUsedCards = CollectionUtils.subtract(Deck.getCards(), boardCards);
        nonUsedCards = CollectionUtils.subtract(nonUsedCards, heroCard1);
        nonUsedCards = CollectionUtils.subtract(nonUsedCards, heroCard2);

        int count = 0, wins = 0;
        //calculate my combination without one more card
        long myCombinationWithoutAdditional = combination(allHeroCardsWithoutAdditional);
        for (int f = 0; f < nonUsedCards.size(); f++) {
            for (int s = f + 1; s < nonUsedCards.size(); s++) {
                Card opponentCard1 = nonUsedCards.get(f);
                Card opponentCard2 = nonUsedCards.get(s);
                allOpponentCardsWithoutAdditional[0] = opponentCard1.intValue();
                allOpponentCardsWithoutAdditional[1] = opponentCard2.intValue();
                allOpponentCards[0] = opponentCard1.intValue();
                allOpponentCards[1] = opponentCard2.intValue();
                if ((positive && myCombinationWithoutAdditional < combination(allOpponentCardsWithoutAdditional))
                        || (!positive && myCombinationWithoutAdditional > combination(allOpponentCardsWithoutAdditional))) {
                    for (Card turn : nonUsedCards) {
                        if (turn == opponentCard1 || turn == opponentCard2) {
                            continue;
                        }
                        allHeroCards[2 + boardSize] = turn.intValue();
                        allOpponentCards[2 + boardSize] = turn.intValue();

                        //get hero combination from cache
                        long myCombination;
                        myCombination = cacheForHeroCombination[turn.intValue()];
                        if (myCombination == defaultCacheValue) {
                            myCombination = combination(allHeroCards);
                            cacheForHeroCombination[turn.intValue()] = myCombination;
                        }
                        //get opponent combination from cache
                        long opponentCombination = defaultCacheValue;
                        long flashValue = isFlash(allOpponentCards);
                        if (flashValue == -1) {
                            int hashCode = opponentCard1.getValue().intValue() * 15
                                    + opponentCard2.getValue().intValue();
                            opponentCombination = cacheForOpponentCombination[turn.getValue().intValue()][hashCode];
                            if (opponentCombination == defaultCacheValue) {
                                opponentCombination = combination(allOpponentCards);
                                cacheForOpponentCombination[turn.getValue().intValue()][hashCode] = opponentCombination;
                            }
                        } else {
                            opponentCombination = combinationWithFlash(allOpponentCards, flashValue);
                        }
                        //compare the calculated or cached combinations
                        if ((positive && myCombination > opponentCombination)
                                || (!positive && myCombination < opponentCombination)) {
                            wins++;
                        }
                        count++;
                    }
                }
            }
        }
//        Log.d("potentialWithOneCard() works in " + (System.currentTimeMillis() - start) + " ms");
//        Log.d("Wins = " + wins + " Count = " + count);
        return (count != 0) ? (double) wins / count : 1;
    }

    public static double negativeFromFlopToTurn(Card myCard1, Card myCard2, Card flop1, Card flop2, Card flop3) {
        return potentialWithOneCard(myCard1, myCard2, Arrays.asList(new Card[]{flop1, flop2, flop3}), false);
    }

    public static double positiveFromFlopToTurn(Card myCard1, Card myCard2, Card flop1, Card flop2, Card flop3) {
        return potentialWithOneCard(myCard1, myCard2, Arrays.asList(new Card[]{flop1, flop2, flop3}), true);
    }

    public static double positiveFromTurnToRiver(Card myCard1, Card myCard2, Card flop1, Card flop2, Card flop3, Card turn) {
        return potentialWithOneCard(myCard1, myCard2, Arrays.asList(new Card[]{flop1, flop2, flop3, turn}), true);
    }

    public static double negativeFromTurnToRiver(Card myCard1, Card myCard2, Card flop1, Card flop2, Card flop3, Card turn) {
        return potentialWithOneCard(myCard1, myCard2, Arrays.asList(new Card[]{flop1, flop2, flop3, turn}), false);
    }
}

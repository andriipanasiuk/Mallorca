package mallorcatour.core.equilator.preflop.simple;

import mallorcatour.core.game.Card;

public class EquilatorPreflopSimple {

	private static int MAX_PREFLOP_STRENGTH = preflopStrengthForSorted(Card.valueOf("Ah"), Card.valueOf("Ac"));

	/**
	 * Calculates preflop strength based on formula.
	 * 
	 * @param holeCard1
	 * @param holeCard2
	 * @return
	 */
	public static double strengthByFormula(Card holeCard1, Card holeCard2) {
		if (holeCard1.compareTo(holeCard2) == 0) {
			throw new IllegalArgumentException("Hole cards must not be equals. " + "First: " + holeCard1 + "; second: "
					+ holeCard2);
		}
		int strength;
		if (holeCard1.compareTo(holeCard2) > 0) {
			strength = preflopStrengthForSorted(holeCard1, holeCard2);
		} else {
			strength = preflopStrengthForSorted(holeCard2, holeCard1);
		}
		return ((double) strength) / (MAX_PREFLOP_STRENGTH);
	}

	/**
	 * First card must be bigger than the second
	 * 
	 * @param holeCard1
	 * @param holeCard2
	 * @return
	 */
	static int preflopStrengthForSorted(Card holeCard1, Card holeCard2) {
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

}
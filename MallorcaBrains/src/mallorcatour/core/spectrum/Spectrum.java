package mallorcatour.core.spectrum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mallorcatour.core.game.Card;
import mallorcatour.core.game.Deck;
import mallorcatour.core.game.HoleCards;

public class Spectrum implements Iterable<HoleCards>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<HoleCards, Double> weights;

	public Spectrum() {
		this.weights = new HashMap<>();
	}

	public boolean isEmpty() {
		return this.weights.isEmpty();
	}

	public static Spectrum random() {
		Spectrum result = new Spectrum();
		List<Card> cards = Deck.getCards();
		for (int i = 0; i < cards.size(); i++) {
			for (int j = i + 1; j < cards.size(); j++) {
				result.weights.put(new HoleCards((Card) cards.get(i), (Card) cards.get(j)), Double.valueOf(1.0D));
			}
		}
		return result;
	}

	public void remove(Card card) {
		List<HoleCards> cardsForRemove = new ArrayList<HoleCards>();
		for (HoleCards cards : this) {
			if (cards.hasCard(card)) {
				cardsForRemove.add(cards);
			}
		}
		for (HoleCards cards : cardsForRemove)
			remove(cards);
	}

	public void remove(Card... cards) {
		List<HoleCards> cardsForRemove = new ArrayList<>();
		for (HoleCards holeCards : this) {
			for (Card card : cards) {
				if (holeCards.hasCard(card)) {
					cardsForRemove.add(holeCards);
					break;
				}
			}
		}
		for (HoleCards holeCards : cardsForRemove)
			remove(holeCards);
	}

	public void add(HoleCards cards, double weight) {
		if (weight != 0.0D)
			this.weights.put(cards, Double.valueOf(weight));
	}

	public void add(HoleCards cards) {
		this.weights.put(cards, Double.valueOf(1.0D));
	}

	public void remove(HoleCards holeCards) {
		this.weights.remove(holeCards);
	}

	public void add(String cards) {
		Card.Value firstRange = Card.Value.valueOf(cards.charAt(0));
		Card.Value secondRange = Card.Value.valueOf(cards.charAt(1));
		boolean all = cards.length() == 2;
		if (!all) {
			boolean suited = cards.charAt(2) == 's';
			if (suited) {
				for (Card.Suit suit : Card.Suit.getSuits()) {
					this.weights.put(new HoleCards(new Card(firstRange, suit), new Card(secondRange, suit)),
							Double.valueOf(1.0D));
				}
			} else
				for (Card.Suit suit1 : Card.Suit.getSuits()) {
					for (Card.Suit suit2 : Card.Suit.getSuits())
						if (suit1 != suit2) {
							this.weights.put(new HoleCards(new Card(firstRange, suit1), new Card(secondRange, suit2)),
									Double.valueOf(1.0D));
						}
				}
		} else {
			for (Card.Suit suit1 : Card.Suit.getSuits()) {
				for (Card.Suit suit2 : Card.Suit.getSuits())
					if ((firstRange != secondRange) || (suit1 != suit2)) {
						this.weights.put(new HoleCards(new Card(firstRange, suit1), new Card(secondRange, suit2)),
								Double.valueOf(1.0D));
					}
			}
		}
	}

	public double getWeight(HoleCards cards) {
		Double result = (Double) this.weights.get(cards);
		if (result == null) {
			return 0.0D;
		}
		return result.doubleValue();
	}

	public double summaryWeight() {
		double sum = 0.0D;
		for (Map.Entry<HoleCards, Double> entry : this.weights.entrySet()) {
			sum += ((Double) entry.getValue()).doubleValue();
		}
		return sum;
	}

	public double maxWeight() {
		double maxValue = Double.MIN_VALUE;
		for (Map.Entry<HoleCards, Double> entry : this.weights.entrySet()) {
			if (maxValue < ((Double) entry.getValue()).doubleValue()) {
				maxValue = ((Double) entry.getValue()).doubleValue();
			}
		}
		return maxValue;
	}

	public Iterator<HoleCards> iterator() {
		return this.weights.keySet().iterator();
	}

	public int size() {
		return this.weights.size();
	}

	public String toString() {
		Set<Map.Entry<HoleCards, Double>> set = this.weights.entrySet();
		List<Map.Entry<HoleCards, Double>> list = new ArrayList<>(set);
		Collections.sort(list, new Comparator<Map.Entry<HoleCards, Double>>() {
			public int compare(Map.Entry<HoleCards, Double> o1, Map.Entry<HoleCards, Double> o2) {
				return Double.compare(((Double) o1.getValue()).doubleValue(), ((Double) o2.getValue()).doubleValue());
			}

		});
		Collections.reverse(list);
		StringBuilder result = new StringBuilder();
		for (Map.Entry<HoleCards, Double> item : list) {
			result.append(item.getKey());
			result.append(": ");
			result.append(item.getValue());
			result.append("\n");
		}
		return result.toString();
	}

}

package mallorcatour.core.game;

import java.io.Serializable;

public class HoleCards implements Serializable {
	private static final long serialVersionUID = -5889114050917400932L;
	public Card first;
	public Card second;

	public HoleCards(Card card1, Card card2) {
		if (card1.compareTo(card2) == 0) {
			throw new IllegalArgumentException("Cards must not be equals!");
		}
		this.first = card1;
		this.second = card2;
	}

	public static HoleCards valueOf(String holeCards) {
		Card c1 = Card.valueOf(holeCards.substring(0, 2));
		Card c2 = Card.valueOf(holeCards.substring(2, 4));
		return new HoleCards(c1, c2);
	}

	public boolean hasCard(Card card) {
		return (this.first.equals(card)) || (this.second.equals(card));
	}

	public boolean isSuited() {
		return this.first.isSuitedWith(this.second);
	}

	public boolean isRanked() {
		return this.first.getValue() == this.second.getValue();
	}

	public String toString() {
		return this.second.toString() + " " + this.first.toString();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof HoleCards)) {
			return false;
		}
		HoleCards other = (HoleCards) obj;
		if (hashCode() != other.hashCode()) {
			return false;
		}
		return true;
	}

	public int hashCodeForValues() {
		if (this.first.compareTo(this.second) > 0) {
			return this.first.getValue().intValue() * 13 + this.second.getValue().intValue();
		}
		return this.second.getValue().intValue() * 13 + this.first.getValue().intValue();
	}

	public static int hashCodeForValues(Card card1, Card card2) {
		if (card1.compareTo(card2) > 0) {
			return card1.getValue().intValue() * 13 + card2.getValue().intValue();
		}
		return card2.getValue().intValue() * 13 + card1.getValue().intValue();
	}

	public int hashCode() {
		if (this.first.compareTo(this.second) > 0) {
			return this.first.intValue() * 70 + this.second.intValue();
		}
		return this.second.intValue() * 70 + this.first.intValue();
	}
}

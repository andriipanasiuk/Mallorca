package mallorcatour.game.core;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class Card implements Comparable<Card>, Serializable {
	private static final long serialVersionUID = 1L;
	private Value value;
	private Suit suit;
	private int intValue;
	private int intValueForBrecher;

	public Card(Value value, Suit suit) {
		this.value = value;
		this.suit = suit;
		this.intValue = (value.intValue * 4 + suit.intValue);
		this.intValueForBrecher = (value.intValue + suit.intValue * 13);
	}

	public Card(int intValue, int intSuit) {
		this.value = Value.valueOf(intValue);
		this.suit = Suit.valueOf(intSuit);
		this.intValue = (this.value.intValue * 4 + this.suit.intValue);
		this.intValueForBrecher = (this.value.intValue + this.suit.intValue * 13);
	}

	public static Card valueOf(String card) {
		if (card.length() != 2) {
			throw new IllegalArgumentException("Card need 2 symbols. " + card + ": illegal!");
		}

		Value value = Value.valueOf(card.charAt(0));
		Suit suit = Suit.valueOf(card.charAt(1));
		return new Card(value, suit);
	}

	public static Card valueOf(int value) {
		return new Card(Value.valueOf(value / 4), Suit.valueOf(value % 4));
	}

	public static Card valueOfBrecher(int value) {
		return new Card(Value.valueOf(value % 13), Suit.valueOf(value / 13));
	}

	public boolean isSuitedWith(Card other) {
		return this.suit == other.suit;
	}

	public String toString() {
		return this.value.toString() + this.suit.toString();
	}

	public int compareTo(Card other) {
		int compareValue = this.value.compareTo(other.value);
		if (compareValue != 0) {
			return compareValue;
		}
		return this.suit.compareTo(other.suit);
	}

	public boolean sameValue(Card other) {
		return this.value.intValue == other.value.intValue;
	}

	public int intValue() {
		return this.intValue;
	}

	public int intValueForBrecher() {
		return this.intValueForBrecher;
	}

	public Value getValue() {
		return this.value;
	}

	public Suit getSuit() {
		return this.suit;
	}

	public int hashCode() {
		int hash = 5;
		hash = 43 * hash + (this.value != null ? this.value.intValue : 0);
		hash = 43 * hash + (this.suit != null ? this.suit.intValue : 0);
		return hash;
	}

	public boolean equals(Object other) {
		if (!(other instanceof Card)) {
			return false;
		}
		Card otherCard = (Card) other;
		return (this.value == otherCard.value) && (this.suit == otherCard.suit);
	}

	public static int[] convertToIntArray(Card[] cards) {
		int[] result = new int[cards.length];
		for (int i = 0; i < cards.length; i++) {
			result[i] = cards[i].intValue();
		}
		return result;
	}

	public static int[] convertToIntArray(List<Card> cards) {
		int[] result = new int[cards.size()];
		for (int i = 0; i < cards.size(); i++) {
			result[i] = ((Card) cards.get(i)).intValue();
		}
		return result;
	}

	public static int[] convertToIntBrecherArray(List<Card> cards) {
		int[] result = new int[cards.size()];
		for (int i = 0; i < cards.size(); i++) {
			result[i] = ((Card) cards.get(i)).intValueForBrecher();
		}
		return result;
	}

	public static class Value implements Comparable<Value>, Serializable {
		private static final long serialVersionUID = 1L;
		private final String value;
		private final int intValue;
		public static final Value TWO = new Value("2", 0);
		public static final Value THREE = new Value("3", 1);
		public static final Value FOUR = new Value("4", 2);
		public static final Value FIVE = new Value("5", 3);
		public static final Value SIX = new Value("6", 4);
		public static final Value SEVEN = new Value("7", 5);
		public static final Value EIGHT = new Value("8", 6);
		public static final Value NINE = new Value("9", 7);
		public static final Value TEN = new Value("T", 8);
		public static final Value JACK = new Value("J", 9);
		public static final Value QUEEN = new Value("Q", 10);
		public static final Value KING = new Value("K", 11);
		public static final Value ACE = new Value("A", 12);
		private static List<Value> listForCompare = Collections.unmodifiableList(Arrays.asList(new Value[] { TWO,
				THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE }));

		private Value(String value, int intValue) {
			this.value = value;
			this.intValue = intValue;
		}

		public String toString() {
			return String.valueOf(this.value);
		}

		public static Value valueOf(int intValue) {
			return (Value) listForCompare.get(intValue);
		}

		public static Value valueOf(String value) {
			return valueOf(value.charAt(0));
		}

		public static Value valueOf(char valueChar) {
			switch (valueChar) {
			case '2':
				return TWO;
			case '3':
				return THREE;
			case '4':
				return FOUR;
			case '5':
				return FIVE;
			case '6':
				return SIX;
			case '7':
				return SEVEN;
			case '8':
				return EIGHT;
			case '9':
				return NINE;
			case 'T':
				return TEN;
			case 'J':
				return JACK;
			case 'Q':
				return QUEEN;
			case 'K':
				return KING;
			case 'A':
				return ACE;
			case ':':
			case ';':
			case '<':
			case '=':
			case '>':
			case '?':
			case '@':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
			case 'G':
			case 'H':
			case 'I':
			case 'L':
			case 'M':
			case 'N':
			case 'O':
			case 'P':
			case 'R':
			case 'S':
			}
			throw new IllegalArgumentException("Error in string representation of value");
		}

		public int compareTo(Value other) {
			return this.intValue - other.intValue;
		}

		public static Comparator<Card> getComparator() {
			return new Comparator<Card>() {
				public int compare(Card c1, Card c2) {
					return c1.value.compareTo(c2.value);
				}
			};
		}

		public static Comparator<Value> getReverseComparator() {
			return new Comparator<Card.Value>() {
				public int compare(Card.Value v1, Card.Value v2) {
					return v2.intValue - v1.intValue;
				}
			};
		}

		public static List<Value> getValues() {
			return listForCompare;
		}

		public int intValue() {
			return this.intValue;
		}

		public Value getOneUpValue() {
			int index = listForCompare.indexOf(this);
			if (index != listForCompare.size() - 1) {
				return (Value) listForCompare.get(listForCompare.indexOf(this) + 1);
			}
			return null;
		}

		public Value getOneDownValue() {
			int index = listForCompare.indexOf(this);
			if (index != 0) {
				return (Value) listForCompare.get(listForCompare.indexOf(this) - 1);
			}
			return null;
		}
	}

	public static class Suit implements Comparable<Suit>, Serializable {
		private static final long serialVersionUID = 1L;
		private String suit;
		private int intValue;
		public static final Suit HEARTS = new Suit("Hearts", 3);
		public static final Suit DIAMONDS = new Suit("Diamonds", 2);
		public static final Suit CLUBS = new Suit("Clubs", 1);
		public static final Suit SPADES = new Suit("Spades", 0);
		private static final List<Suit> listForCompare = Collections.unmodifiableList(Arrays.asList(new Suit[] {
				SPADES, CLUBS, DIAMONDS, HEARTS }));

		private Suit(String suit, int intValue) {
			this.suit = suit;
			this.intValue = intValue;
		}

		public static Suit valueOf(String suit) {
			return valueOf(suit.charAt(0));
		}

		public int intValue() {
			return this.intValue;
		}

		public static Suit valueOf(char suitChar) {
			switch (suitChar) {
			case 'c':
				return CLUBS;
			case 's':
				return SPADES;
			case 'd':
				return DIAMONDS;
			case 'h':
				return HEARTS;
			}
			throw new IllegalArgumentException("Error in string representation of suit");
		}

		public static Suit valueOf(int value) {
			return (Suit) listForCompare.get(value);
		}

		public static List<Suit> getSuits() {
			return listForCompare;
		}

		public String toString() {
			return String.valueOf(this.suit.charAt(0)).toLowerCase();
		}

		public int compareTo(Suit other) {
			return this.intValue - other.intValue;
		}

		public static Comparator<Card> getComparator() {
			return new Comparator<Card>() {
				public int compare(Card card1, Card card2) {
					int compareSuit = card1.suit.compareTo(card2.suit);
					if (compareSuit != 0) {
						return compareSuit;
					}
					return card1.value.compareTo(card2.value);
				}
			};
		}
	}
}

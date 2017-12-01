package mallorcatour.core.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mallorcatour.core.game.Card.Value;

public class Deck {
	private static List<Card> allDeck = Collections.unmodifiableList(initCards());
	private static int[] intDeck;
	private static int[] intBrecherDeck;
	private List<Card> currentDeck;

	private static List<Card> initCards() {
		List<Card> result = new ArrayList<>();
		for (Value value:Card.Value.getValues()) {
			for (Card.Suit suit : Card.Suit.getSuits())
				result.add(new Card(value, suit));
		}
		return result;
	}

	public static int[] getIntCards() {
		return intDeck;
	}

	public static int[] getIntCardsForBrecher() {
		return intBrecherDeck;
	}

	private static void initIntDeck() {
		intDeck = new int[allDeck.size()];
		intBrecherDeck = new int[allDeck.size()];
		for (int i = 0; i < allDeck.size(); i++) {
			intDeck[i] = ((Card) allDeck.get(i)).intValue();
			intBrecherDeck[i] = ((Card) allDeck.get(i)).intValue();
		}
	}

	public static List<Card> getCards() {
		return allDeck;
	}

	public void refresh() {
		this.currentDeck = new ArrayList<>(allDeck);
	}

	public void shuffle() {
		Collections.shuffle(this.currentDeck);
	}

	static {
		initIntDeck();
	}
}

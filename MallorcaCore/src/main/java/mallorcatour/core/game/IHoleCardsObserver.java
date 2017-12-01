package mallorcatour.core.game;

public interface IHoleCardsObserver {
	IHoleCardsObserver EMPTY = (c1, c2) -> {
        // do nothing
    };

	void onHoleCards(Card c1, Card c2);
}
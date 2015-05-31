package mallorcatour.core.game;

public interface IHoleCardsObserver {
	public static final IHoleCardsObserver EMPTY = new IHoleCardsObserver() {

		@Override
		public void onHoleCards(Card c1, Card c2) {
			// do nothing
		}
	};

	void onHoleCards(Card c1, Card c2);
}
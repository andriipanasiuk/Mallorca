package mallorcatour.core.game;


public class HoleCardsObservers implements IHoleCardsObserver {

	private IHoleCardsObserver[] observers;

	public HoleCardsObservers(IHoleCardsObserver... observers) {
		this.observers = observers;
	}

	@Override
	public void onHoleCards(Card c1, Card c2) {
		for (IHoleCardsObserver observer : observers) {
			observer.onHoleCards(c1, c2);
		}
	}

}
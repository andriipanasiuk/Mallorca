package mallorcatour.bot.neural;

import mallorcatour.core.game.Card;

public interface IHoleCardsObserver {
	void onHoleCards(Card c1, Card c2);
}
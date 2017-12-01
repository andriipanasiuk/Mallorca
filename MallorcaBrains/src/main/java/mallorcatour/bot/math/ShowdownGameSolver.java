package mallorcatour.bot.math;

import java.util.List;

import mallorcatour.core.game.advice.Advisor;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.Deck;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.interfaces.GameContext;
import mallorcatour.core.math.RandomVariable;
import mallorcatour.core.spectrum.Spectrum;

public class ShowdownGameSolver extends NLGameSolver {

	public ShowdownGameSolver(Advisor villainModeller) {
		super(villainModeller);
	}

	@SuppressWarnings("unused")
	@Override
	protected RandomVariable calculatePassiveProfit(GameContext gameInfo, double pot, double heroInvestment,
                                                    PokerStreet street, HoleCards heroCards, Card[] board, Spectrum villainSpectrum, boolean onButton, int depth) {
		if (street == PokerStreet.RIVER) {
			return super.calculatePassiveProfit(gameInfo, pot, heroInvestment, street, heroCards, board,
					villainSpectrum, onButton, true, depth);
		}
		if (street == PokerStreet.PREFLOP) {
			return super.calculatePassiveProfit(gameInfo, pot, heroInvestment, street, heroCards, board,
					villainSpectrum, onButton, true, depth);
		}
		RandomVariable result = new RandomVariable();
		int nextCardCount = 52 - 2 - board.length;
		List<Card> cards = Deck.getCards();
		for (int i = 0; i < 52; i++) {
			Card card = cards.get(i);
			//TODO finish this class
		}
		return null;
	}
}
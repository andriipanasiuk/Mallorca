package mallorcatour.bot.math;

import java.util.List;

import mallorcatour.brains.IAdvisor;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.Deck;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.spectrum.Spectrum;

public class ShowdownGameSolver extends NLGameSolver {

	public ShowdownGameSolver(IAdvisor villainModeller) {
		super(villainModeller);
	}

	@Override
	protected RandomVariable calculatePassiveProfit(IGameInfo gameInfo, double pot, double heroInvestment,
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
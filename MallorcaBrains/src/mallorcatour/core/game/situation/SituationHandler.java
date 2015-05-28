/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.game.situation;

import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.equilator.StreetEquity;
import mallorcatour.core.equilator.preflop.EquilatorPreflop;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.IHoleCardsObserver;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.PokerStreet;

/**
 * 
 * @author Andrew
 */
public class SituationHandler extends NoStrengthSituationHandler implements IHoleCardsObserver {

	private Card flop1, flop2, flop3, turn, river;
	private Card holeCard1, holeCard2;
	private final boolean needFullPotentialOnFlop;
	private double strength, positivePotential, negativePotential;

	public SituationHandler(LimitType limitType, String hero) {
		super(limitType, hero, true);
		needFullPotentialOnFlop = false;
	}

	public SituationHandler(LimitType limitType, boolean needFullPotentialOnFlop, String hero) {
		super(limitType, hero, true);
		this.needFullPotentialOnFlop = needFullPotentialOnFlop;
	}

	@Override
	public void onHoleCards(Card c1, Card c2) {
		this.holeCard1 = c1;
		this.holeCard2 = c2;
		StreetEquity equity = EquilatorPreflop.equityVsRandom(holeCard1, holeCard2);
		strength = equity.strength;
		positivePotential = equity.positivePotential;
		negativePotential = equity.negativePotential;
	}

	@Override
	public LocalSituation getSituation() {
		LocalSituation result = super.getSituation();
		result.setStrength(strength);
		result.setPositivePotential(positivePotential);
		result.setNegativePotential(negativePotential);
		return result;
	}

	@Override
	public void onStageEvent(PokerStreet street) {
		super.onStageEvent(street);
		if (street == PokerStreet.FLOP) {
			flop1 = gameInfo.getBoard().get(0);
			flop2 = gameInfo.getBoard().get(1);
			flop3 = gameInfo.getBoard().get(2);
			StreetEquity flopEquity;
			if (needFullPotentialOnFlop) {
				flopEquity = PokerEquilatorBrecher.equityOnFlopFull(holeCard1, holeCard2, flop1, flop2, flop3, true);
			} else {
				flopEquity = PokerEquilatorBrecher.equityOnFlop(holeCard1, holeCard2, flop1, flop2, flop3);
			}
			strength = flopEquity.strength;
			positivePotential = flopEquity.positivePotential;
			negativePotential = flopEquity.negativePotential;
		} else if (street == PokerStreet.TURN) {
			turn = gameInfo.getBoard().get(3);
			StreetEquity turnEquity = PokerEquilatorBrecher.equityOnTurn(holeCard1, holeCard2, flop1, flop2, flop3,
					turn);
			strength = turnEquity.strength;
			positivePotential = turnEquity.positivePotential;
			negativePotential = turnEquity.negativePotential;
		} else if (street == PokerStreet.RIVER) {
			river = gameInfo.getBoard().get(4);
			strength = PokerEquilatorBrecher.strengthOnRiver(holeCard1, holeCard2, flop1, flop2, flop3, turn, river);
			positivePotential = 0;
			negativePotential = 0;
		}
	}

}

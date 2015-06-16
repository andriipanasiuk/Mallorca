package mallorcatour.core.game;

import java.util.List;

import mallorcatour.core.game.interfaces.IPlayerGameInfo;

public abstract class GameInfoAdapter implements IPlayerGameInfo {
	public LimitType limitType;
	public int[] raisesOnStreet = new int[4];
	public boolean canHeroRaise;
	protected PokerStreet street;
	public double pot;
	public double bigBlind;
	public double heroAmountToCall;
	public List<Card> board;
	public double bankrollAtRisk;
	public boolean onButton;
	private double[] potOnStreet = new double[4];

	public void resetStreetPots() {
		for (int i = 0; i < potOnStreet.length; i++) {
			potOnStreet[i] = -1;
		}
	}

	public void changeStreet(PokerStreet newStreet) {
		if (newStreet != PokerStreet.PREFLOP) {
			potOnStreet[this.street.intValue()] = pot;
		}
		this.street = newStreet;
	}

	@Override
	public double getPot(PokerStreet street) {
		return potOnStreet[street.intValue()];
	}

	@Override
	public double getBigBlindSize() {
		return bigBlind;
	}

	@Override
	public PokerStreet getStage() {
		return street;
	}

	@Override
	public boolean isPreFlop() {
		return street == PokerStreet.PREFLOP;
	}

	@Override
	public boolean isPostFlop() {
		return street != PokerStreet.PREFLOP;
	}

	@Override
	public boolean isFlop() {
		return street == PokerStreet.FLOP;
	}

	public boolean isTurn() {
		return street == PokerStreet.TURN;
	}

	public boolean isRiver() {
		return street == PokerStreet.RIVER;
	}

	@Override
	public List<Card> getBoard() {
		return board;
	}

	@Override
	public double getPotSize() {
		return pot;
	}

	@Override
	public double getAmountToCall() {
		return heroAmountToCall;
	}

	@Override
	public double getBankRollAtRisk() {
		return bankrollAtRisk;
	}

	@Override
	public boolean canHeroRaise() {
		if (limitType == LimitType.NO_LIMIT) {
			return getBankRollAtRisk() > 0;
		} else {
			return raisesOnStreet[street.intValue()] < 4;
		}
	}

	@Override
	public int getNumRaises() {
		return raisesOnStreet[street.intValue()];
	}

	@Override
	public LimitType getLimitType() {
		return limitType;
	}

	@Override
	public Flop getFlop() {
		return new Flop(board.get(0), board.get(1), board.get(2));
	}

	@Override
	public Card getTurn() {
		return board.get(3);
	}

	@Override
	public Card getRiver() {
		return board.get(4);
	}

	@Override
	public boolean onButton() {
		return onButton;
	}

	@Override
	public boolean isVillainSitOut() {
		return false;
	}
}
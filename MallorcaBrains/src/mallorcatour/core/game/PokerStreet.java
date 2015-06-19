package mallorcatour.core.game;

import java.io.Serializable;

import mallorcatour.bot.C;

public class PokerStreet implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private int intValue;
	public static final int PREFLOP_VALUE = 0;
	public static final int FLOP_VALUE = 1;
	public static final int TURN_VALUE = 2;
	public static final int RIVER_VALUE = 3;
	public static final PokerStreet PREFLOP = new PokerStreet(C.PREFLOP, PREFLOP_VALUE);
	public static final PokerStreet FLOP = new PokerStreet(C.FLOP, FLOP_VALUE);
	public static final PokerStreet TURN = new PokerStreet(C.TURN, TURN_VALUE);
	public static final PokerStreet RIVER = new PokerStreet(C.RIVER, RIVER_VALUE);

	private PokerStreet(String name, int intValue) {
		this.name = name;
		this.intValue = intValue;
	}

	public static PokerStreet valueOf(int intValue) {
		switch (intValue) {
		case 0:
			return PREFLOP;
		case 1:
			return FLOP;
		case 2:
			return TURN;
		case 3:
			return RIVER;
		}
		throw new IllegalArgumentException("Illegal int value of street; only 0, 1, 2, 3 are legal");
	}

	public PokerStreet next() {
		if (intValue == 0) {
			return PokerStreet.FLOP;
		} else if (intValue == 1) {
			return PokerStreet.TURN;
		} else if (intValue == 2) {
			return PokerStreet.RIVER;
		} else if (intValue == 3) {
			return null;
		} else {
			throw new IllegalStateException("Shouln't be");
		}
	}

	public PokerStreet previous() {
		if (intValue == 0) {
			return null;
		} else if (intValue == 1) {
			return PokerStreet.PREFLOP;
		} else if (intValue == 2) {
			return PokerStreet.FLOP;
		} else if (intValue == 3) {
			return TURN;
		} else {
			throw new IllegalStateException("Shouln't be");
		}
	}

	public String toString() {
		return this.name;
	}

	public int intValue() {
		return this.intValue;
	}

	public boolean equals(Object obj) {
		if (obj != this) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		int hash = 7;
		hash = 43 * hash + this.name.hashCode();
		return hash;
	}
}

package mallorcatour.core.game;

import java.io.Serializable;

public class PokerStreet implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private int intValue;
	public static final PokerStreet PREFLOP = new PokerStreet("preflop", 0);
	public static final PokerStreet FLOP = new PokerStreet("flop", 1);
	public static final PokerStreet TURN = new PokerStreet("turn", 2);
	public static final PokerStreet RIVER = new PokerStreet("river", 3);

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

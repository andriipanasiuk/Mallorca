package mallorcatour.core.game;

import java.io.Serializable;

public class PlayerInfo implements Serializable {
	private static final long serialVersionUID = -4125178498692602233L;
	private String name;
	private double stack;
	private Card holeCard1;
	private Card holeCard2;
	private boolean isSittingOut;

	public PlayerInfo(String name, double stack, Card holeCard1, Card holeCard2) {
		this.name = name;
		this.stack = stack;
		this.holeCard1 = holeCard1;
		this.holeCard2 = holeCard2;
	}

	public PlayerInfo(String name, double stack, boolean isSittingOut) {
		this.name = name;
		this.stack = stack;
		this.isSittingOut = isSittingOut;
	}

	public PlayerInfo(String name, double stack) {
		this(name, stack, false);
	}

	public void setHoleCards(Card first, Card second) {
		this.holeCard1 = first;
		this.holeCard2 = second;
	}

	public boolean isSittingOut() {
		return this.isSittingOut;
	}

	public double getStack() {
		return this.stack;
	}

	public String getName() {
		return this.name;
	}

	public HoleCards getHoleCards() {
		if ((this.holeCard1 != null) && (this.holeCard2 != null)) {
			return new HoleCards(this.holeCard1, this.holeCard2);
		}
		return null;
	}
}

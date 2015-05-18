package mallorcatour.core.game;

public class Flop {
	public Card first;
	public Card second;
	public Card third;

	public Flop(Card flop1, Card flop2, Card flop3) {
		this.first = flop1;
		this.second = flop2;
		this.third = flop3;
	}

	public Card[] toArray() {
		return new Card[] { this.first, this.second, this.third };
	}
}

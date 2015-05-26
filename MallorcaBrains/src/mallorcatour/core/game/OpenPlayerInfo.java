package mallorcatour.core.game;

public class OpenPlayerInfo extends PlayerInfo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7635447535829425630L;
	private Card holeCard1;
	private Card holeCard2;

	public OpenPlayerInfo(String name, double stack) {
		super(name, stack);
	}

	public OpenPlayerInfo(String name, double stack, Card holeCard1, Card holeCard2) {
		super(name, stack);
		this.holeCard1 = holeCard1;
		this.holeCard2 = holeCard2;
	}

	public OpenPlayerInfo(String name, double stack, boolean isSittingOut) {
		super(name, stack, isSittingOut);
	}

	public void setHoleCards(Card first, Card second) {
		this.holeCard1 = first;
		this.holeCard2 = second;
	}

	public HoleCards getHoleCards() {
		if ((this.holeCard1 != null) && (this.holeCard2 != null)) {
			return new HoleCards(this.holeCard1, this.holeCard2);
		}
		return null;
	}

}
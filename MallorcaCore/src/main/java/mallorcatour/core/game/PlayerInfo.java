package mallorcatour.core.game;

import java.io.Serializable;

public class PlayerInfo extends BasePlayerInfo implements Serializable {
	private static final long serialVersionUID = -4125178498692602233L;
	private boolean isSittingOut;
	public boolean isOnButton;
	public double bet;

	public PlayerInfo(String name, double stack, boolean isSittingOut) {
		super(name, stack);
		this.isSittingOut = isSittingOut;
	}

	public PlayerInfo(String name, double stack) {
		this(name, stack, false);
	}

	public PlayerInfo(String name) {
		super(name);
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

	public boolean isOnButton() {
		return isOnButton;
	}

	public double getBet() {
		return bet;
	}
}

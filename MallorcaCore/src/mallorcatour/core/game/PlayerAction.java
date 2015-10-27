package mallorcatour.core.game;

import java.io.Serializable;

public final class PlayerAction implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private Action action;

	public PlayerAction(String name, Action action) {
		this.name = name;
		this.action = action;
	}

	public String getName() {
		return this.name;
	}

	public Action getAction() {
		return this.action;
	}

	public String toString() {
		return this.name + " " + this.action.toString();
	}
}

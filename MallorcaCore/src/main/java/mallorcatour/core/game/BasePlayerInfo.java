package mallorcatour.core.game;

/**
 * Информация об игроке за столом, которая видна всем наблюдателям.
 */
public abstract class BasePlayerInfo {
	public String name;
	public double stack;

	public BasePlayerInfo(String name, double stack) {
		this.name = name;
		this.stack = stack;
	}

	public BasePlayerInfo(String name) {
		this.name = name;
	}

}
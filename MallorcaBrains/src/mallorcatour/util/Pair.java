package mallorcatour.util;

import java.io.Serializable;

public class Pair<F extends Serializable, S extends Serializable> implements Serializable {
	private static final long serialVersionUID = 1L;
	public F first;
	public S second;

	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}

	public String toString() {
		return this.first.toString() + " " + this.second.toString();
	}
}

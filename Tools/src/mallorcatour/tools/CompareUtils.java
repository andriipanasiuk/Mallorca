package mallorcatour.tools;

import java.util.Comparator;

public class CompareUtils {
	public static Comparator<Object> reverse(final Comparator<Object> comparator) {
		return new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				return comparator.compare(o2, o1);
			}
		};
	}
}

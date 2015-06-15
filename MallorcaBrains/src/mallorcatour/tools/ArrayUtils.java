package mallorcatour.tools;

public class ArrayUtils {
	public static int[] removeAll(int[] array, int elementToRemove) {
		int newCount = array.length;
		for (int value : array) {
			if (elementToRemove == value) {
				newCount--;
			}
		}
		int[] result = new int[newCount];
		int i = 0;
		for (int value : array) {
			if (elementToRemove != value) {
				result[(i++)] = value;
			}
		}
		return result;
	}

	public static boolean containsElement(int[] array, int element) {
		for (int value : array) {
			if (value == element) {
				return true;
			}
		}
		return false;
	}

	public static <T> boolean containsElement(T[] array, T element) {
		for (Object value : array) {
			if (value.equals(element)) {
				return true;
			}
		}
		return false;
	}

	public static int[] reverse(int[] array) {
		int length = array.length;
		int[] result = new int[length];
		int i = length - 1;
		for (int value : array) {
			result[(i--)] = value;
		}
		return result;
	}

	public static void sortAscending(int[] array) {
		sortAscending(array, 0, array.length);
	}

	public static void sortDescendingForShort(int[] array) {
		int len = array.length;
		int off = 0;
		if (len >= 8) {
			throw new IllegalArgumentException();
		}
		for (int i = off; i < len + off; i++)
			for (int j = i; (j > off) && (array[(j - 1)] < array[j]); j--)
				swap(array, j, j - 1);
	}

	private static void sortAscending(int[] x, int off, int len) {
		if (len < 7) {
			for (int i = off; i < len + off; i++) {
				for (int j = i; (j > off) && (x[(j - 1)] > x[j]); j--) {
					swap(x, j, j - 1);
				}
			}
			return;
		}

		int m = off + (len >> 1);
		if (len > 7) {
			int l = off;
			int n = off + len - 1;
			if (len > 40) {
				int s = len / 8;
				l = med3(x, l, l + s, l + 2 * s);
				m = med3(x, m - s, m, m + s);
				n = med3(x, n - 2 * s, n - s, n);
			}
			m = med3(x, l, m, n);
		}
		int v = x[m];

		int a = off;
		int b = a;
		int c = off + len - 1;
		int d = c;
		while (true) {
			if ((b <= c) && (x[b] <= v)) {
				if (x[b] == v) {
					swap(x, a++, b);
				}
				b++;
			} else {
				while ((c >= b) && (x[c] >= v)) {
					if (x[c] == v) {
						swap(x, c, d--);
					}
					c--;
				}
				if (b > c) {
					break;
				}
				swap(x, b++, c--);
			}
		}

		int n = off + len;
		int s = Math.min(a - off, b - a);
		vecswap(x, off, b - s, s);
		s = Math.min(d - c, n - d - 1);
		vecswap(x, b, n - s, s);

		if ((s = b - a) > 1) {
			sortAscending(x, off, s);
		}
		if ((s = d - c) > 1)
			sortAscending(x, n - s, s);
	}

	private static void vecswap(int[] x, int a, int b, int n) {
		for (int i = 0; i < n; b++) {
			swap(x, a, b);

			i++;
			a++;
		}
	}

	private static int med3(int[] x, int a, int b, int c) {
		return x[a] > x[c] ? c : x[b] > x[c] ? b : x[a] < x[b] ? a : x[a] < x[c] ? c : x[b] < x[c] ? b : a;
	}

	private static void swap(int[] x, int a, int b) {
		int t = x[a];
		x[a] = x[b];
		x[b] = t;
	}
}

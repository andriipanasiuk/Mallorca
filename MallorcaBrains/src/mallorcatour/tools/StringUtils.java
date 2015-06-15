package mallorcatour.tools;

public class StringUtils {
	public static String between(String find, String string1, String string2) {
		int from = find.indexOf(string1) + string1.length();
		return between(find, from, string2);
	}

	public static String between(String find, int indexFrom, String string2) {
		int from = indexFrom;
		int to = find.indexOf(string2, from);
		if (to == -1) {
			return find.substring(from);
		}
		return find.substring(from, to);
	}

	public static int[] parseIntArray(String string, String separator) {
		String[] array = string.split(separator);
		int[] result = new int[array.length];
		int i = 0;
		for (String intValue : array) {
			result[(i++)] = Integer.parseInt(intValue);
		}
		return result;
	}
}

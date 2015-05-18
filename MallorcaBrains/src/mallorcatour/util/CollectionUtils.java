package mallorcatour.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CollectionUtils {

	public static <T> List<T> subtract(List<T> list1, List<T> list2) {
		List<T> result = new ArrayList<>();
		for (T element : list1) {
			if (!list2.contains(element)) {
				result.add(element);
			}
		}
		return result;
	}

	public static <T> List<T> subtract(List<T> list1, T[] list2) {
		List<T> result = new ArrayList<>();
		for (T element : list1) {
			if (!ArrayUtils.containsElement(list2, element)) {
				result.add(element);
			}
		}
		return result;
	}

	public static <T> List<T> subtract(List<T> list1, T removeElement) {
		List<T> result = new ArrayList<>();
		for (T element : list1) {
			if (!element.equals(removeElement)) {
				result.add(element);
			}
		}
		return result;
	}

	public static <T> void removeAll(List<T> list, T element) {
		while (list.contains(element))
			list.remove(element);
	}

	public static <T> double maxValue(Map<T, Double> map) {
		double result = Double.MIN_VALUE;
		for (Map.Entry<T, Double> entry : map.entrySet()) {
			if (((Double) entry.getValue()).doubleValue() > result) {
				result = ((Double) entry.getValue()).doubleValue();
			}
		}
		return result;
	}
}

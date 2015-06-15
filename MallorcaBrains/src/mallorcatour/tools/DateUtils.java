package mallorcatour.tools;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateUtils {
	private static final Map<String, Integer> monthes = new HashMap<>();
	private static final String DATE_FORMAT = "yyyy_MM_dd_HH_mm_ss";

	public static String getDate(boolean withMillis) {
		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		Date date = new Date();
		String result = dateFormat.format(date);
		if (withMillis) {
			result = result + "_" + System.currentTimeMillis();
		}
		return result;
	}

	public static Date parsePADate(String dateStr) {
		dateStr = preprocessDate(dateStr);
		SimpleDateFormat dateFormat = new SimpleDateFormat("M dd, yyyy - HH:mm:ss");
		Date result = null;
		try {
			result = dateFormat.parse(dateStr);
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
		return result;
	}

	private static String preprocessDate(String date) {
		String result = null;
		for (String month : monthes.keySet()) {
			if (date.contains(month)) {
				result = date.replace(month, String.valueOf(monthes.get(month)));
				return result;
			}
		}
		throw new RuntimeException("Some problem with month in date");
	}

	public static Date parseDate(String date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		Date result = null;
		try {
			result = format.parse(date);
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
		return result;
	}

	public static Date parseDate(String dateStr, String dateFormat) {
		SimpleDateFormat format = new SimpleDateFormat(dateFormat);
		Date result = null;
		try {
			result = format.parse(dateStr);
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
		return result;
	}

	public static long difference(Date firstDate, Date secondDate) {
		return Math.abs(firstDate.getTime() - secondDate.getTime());
	}

	static {
		monthes.put("January", Integer.valueOf(1));
		monthes.put("February", Integer.valueOf(2));
		monthes.put("March", Integer.valueOf(3));
		monthes.put("April", Integer.valueOf(4));
		monthes.put("May", Integer.valueOf(5));
		monthes.put("June", Integer.valueOf(6));
		monthes.put("July", Integer.valueOf(7));
		monthes.put("August", Integer.valueOf(8));
		monthes.put("September", Integer.valueOf(9));
		monthes.put("October", Integer.valueOf(10));
		monthes.put("November", Integer.valueOf(11));
		monthes.put("December", Integer.valueOf(12));
	}
}

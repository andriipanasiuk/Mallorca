package mallorcatour.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ReaderUtils {
	public static List<String> readLines(BufferedReader reader, int lineCount) {
		List<String> result = new ArrayList<>();
		for (int i = 0; i < lineCount; i++) {
			result.add(readLineFrom(reader));
		}
		return result;
	}

	public static void skipLines(BufferedReader reader, int lineCount) {
		for (int i = 0; i < lineCount; i++)
			readLineFrom(reader);
	}

	public static String readLineFrom(BufferedReader reader) {
		String result = null;
		try {
			result = reader.readLine();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return result;
	}

	public static BufferedReader initReader(String filename) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
		}
		return reader;
	}

	public static BufferedReader initReader(InputStream stream) {
		BufferedReader reader = null;
		reader = new BufferedReader(new InputStreamReader(stream));
		return reader;
	}
}

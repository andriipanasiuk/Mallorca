package mallorcatour.util;

import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {
	private static FileWriter fileWriter;

	public static void prepareForWriting(String path, boolean append) {
		try {
			fileWriter = new FileWriter(path, append);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void addToFile(String addString, boolean toNewLine) {
		if (fileWriter == null)
			throw new IllegalArgumentException("You need prepare file to writing first!");
		try {
			if (toNewLine)
				fileWriter.append("\n" + addString);
			else
				fileWriter.append(addString);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void endWriting() {
		try {
			fileWriter.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}

package mallorcatour.util;

import java.io.FileWriter;
import java.io.IOException;

public class MyFileWriter {
	private FileWriter fileWriter;

	private MyFileWriter(String path, boolean append) {
		try {
			this.fileWriter = new FileWriter(path, append);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static MyFileWriter prepareForWriting(String path, boolean append) {
		return new MyFileWriter(path, append);
	}

	public void addToFile(String addString, boolean toNewLine) {
		if (this.fileWriter == null)
			throw new IllegalArgumentException("You need prepare file to writing first!");
		try {
			if (toNewLine)
				this.fileWriter.append("\n" + addString);
			else
				this.fileWriter.append(addString);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void endWriting() {
		try {
			this.fileWriter.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}

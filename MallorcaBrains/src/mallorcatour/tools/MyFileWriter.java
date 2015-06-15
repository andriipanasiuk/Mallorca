package mallorcatour.tools;

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

	public static MyFileWriter prepareForWriting(String path, boolean appendBool) {
		return new MyFileWriter(path, appendBool);
	}

	public void append(String append) {
		if (this.fileWriter == null)
			throw new IllegalArgumentException("You need prepare file to writing first!");
		try {
			this.fileWriter.append(append);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void addToFile(String addString, boolean toNewLine) {
		if (this.fileWriter == null)
			throw new IllegalArgumentException("You need prepare file to writing first!");
		try {
			if (toNewLine)
				this.fileWriter.append(FileUtils.LINE_SEPARATOR + addString);
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

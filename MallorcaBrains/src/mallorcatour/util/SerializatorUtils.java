package mallorcatour.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializatorUtils {
	public static void save(String filePath, Serializable object) {
		ObjectOutputStream out = null;
		try {
			File file = new File(filePath);
			if (file.exists()) {
				file.delete();
			}
			out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			out.writeObject(object);
			out.flush();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
		}
	}

	public static <T extends Serializable> T load(String filePath, Class<T> clazz) {
		try {
			File file = new File(filePath);
			if (!file.exists()) {
				throw new FileNotFoundException("Cannot find file: " + filePath);
			}
			return load(new BufferedInputStream(new FileInputStream(filePath)), clazz);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T load(InputStream stream, Class<T> clazz) {
		ObjectInputStream oistream = null;
		try {
			oistream = new ObjectInputStream(stream);
			T result = (T) oistream.readObject();
			return result;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} catch (ClassNotFoundException cnfe) {
			throw new RuntimeException(cnfe);
		} finally {
			if (oistream != null)
				try {
					oistream.close();
				} catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
		}
	}
}

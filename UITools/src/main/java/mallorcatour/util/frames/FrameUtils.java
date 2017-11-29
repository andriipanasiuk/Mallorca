package mallorcatour.util.frames;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;

public class FrameUtils {
	public static String openFileChooser(Component parent, String curDirectory) {
		JFileChooser chooser = new JFileChooser(curDirectory);
		int returnVal = chooser.showOpenDialog(parent);
		if (returnVal == 0) {
			return chooser.getSelectedFile().getAbsolutePath();
		}
		return null;
	}

	public static File openFileChooser2(Component parent, String curDirectory) {
		JFileChooser chooser = new JFileChooser(curDirectory);
		int returnVal = chooser.showOpenDialog(parent);
		if (returnVal == 0) {
			return chooser.getSelectedFile();
		}
		return null;
	}

	public static String openDirectoryChooser(Component parent, String curDirectory) {
		JFileChooser chooser = new JFileChooser(curDirectory);
		chooser.setFileSelectionMode(1);
		int returnVal = chooser.showOpenDialog(parent);
		if (returnVal == 0) {
			return chooser.getCurrentDirectory().getAbsolutePath();
		}
		return null;
	}

	public static String openFileChooser(Component parent) {
		JFileChooser chooser = new JFileChooser();
		int returnVal = chooser.showOpenDialog(parent);
		if (returnVal == 0) {
			return chooser.getSelectedFile().getAbsolutePath();
		}
		return null;
	}

	public static String[] openFileChooserForMultipleFiles(Component parent, String curDirectory) {
		JFileChooser chooser = new JFileChooser(curDirectory);
		chooser.setMultiSelectionEnabled(true);
		int returnVal = chooser.showOpenDialog(parent);
		if (returnVal == 0) {
			File[] files = chooser.getSelectedFiles();
			String[] result = new String[files.length];
			for (int i = 0; i < files.length; i++) {
				result[i] = files[i].getAbsolutePath();
			}
			return result;
		}
		return null;
	}
}

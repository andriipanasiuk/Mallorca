package mallorcatour.robot.util;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class ImageUtils {
	private static final String DEBUG_DIRECTORY = "temp";
	public static final Rectangle screenRectangle = new Rectangle(1280, 800);

	public static Point isPartOf(BufferedImage allImage, BufferedImage partImage) {
		return isPartOf(allImage, partImage, IImageProcessor.EmptyProcessor);
	}

	public static Point isPartOf(BufferedImage allImage, BufferedImage subImage, IImageProcessor preprocess) {
		preprocess.processImage(allImage);
		preprocess.processImage(subImage);

		int wholeImageWidth = allImage.getWidth();
		int wholeImageHeight = allImage.getHeight();
		int subImageWidth = subImage.getWidth();
		int subImageHeight = subImage.getHeight();

		for (int allHeightIndex = 0; (allHeightIndex < wholeImageHeight) && (allHeightIndex + subImageHeight <= wholeImageHeight); allHeightIndex++) {
			outer: for (int allWidthIndex = 0; (allWidthIndex < wholeImageWidth) && (allWidthIndex + subImageWidth <= wholeImageWidth); allWidthIndex++) {
				for (int subImageWidthIndex = 0; subImageWidthIndex < subImageWidth; subImageWidthIndex++) {
					for (int subImageHeightIndex = 0; subImageHeightIndex < subImageHeight; subImageHeightIndex++) {
						int allColor = allImage
								.getRGB(allWidthIndex + subImageWidthIndex, allHeightIndex + subImageHeightIndex);
						int partColor = subImage.getRGB(subImageWidthIndex, subImageHeightIndex);
						if (allColor != partColor) {
							break outer;
						}
					}
				}

				return new Point(allWidthIndex, allHeightIndex);
			}
		}
		return null;
	}

	public static Point isPartOf(BufferedImage allImage, BufferedImage partImage, Rectangle rect) {
		return isPartOf(allImage.getSubimage(rect.x, rect.y, rect.width, rect.height), partImage);
	}

	public static BufferedImage fromFile(String filename) {
		try {
			return ImageIO.read(new File(filename));
		} catch (IOException ex) {
			throw new RuntimeException("Some problem with file: " + filename, ex);
		}
	}

	public static void toFile(BufferedImage image, String filename, boolean debug) {
		if (image == null) {
			throw new IllegalArgumentException("ImageUtils cannot write null image to file");
		}
		File toFile = null;
		if (debug) {
			File tempDir = new File(DEBUG_DIRECTORY);
			tempDir.mkdir();
			toFile = new File(tempDir, filename);
		} else {
			toFile = new File(filename);
		}
		try {
			ImageIO.write(image, "png", toFile);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static List<Point> getAllEntries(BufferedImage allImage, BufferedImage partImage) {
		List<Point> result = new ArrayList<>();
		Point p = new Point(0, 0);
		BufferedImage imageForSearchIn = allImage;
		while (true) {
			int width = imageForSearchIn.getWidth();
			int height = imageForSearchIn.getHeight();
			p = isPartOf(imageForSearchIn, partImage);
			if (p == null) {
				break;
			}
			result.add(p);
			imageForSearchIn.setRGB(p.x, p.y, partImage.getRGB(0, 0) + 1);
			imageForSearchIn = imageForSearchIn.getSubimage(0, p.y, width, height - p.y);
		}
		return result;
	}

	public static Point isOnScreen(String path) {
		return isOnScreen(fromFile(path));
	}

	public static Point isOnScreen(String path, Rectangle rect) {
		Point p = isOnScreen(fromFile(path), rect);
		return p;
	}

	public static Point isOnScreen(BufferedImage partImage, Rectangle rect) {
		BufferedImage screenImage = getScreenCapture(rect);

		return isPartOf(screenImage, partImage);
	}

	public static Point isOnScreen(BufferedImage partImage) {
		return isOnScreen(partImage, screenRectangle);
	}

	public static BufferedImage getScreenCapture(Rectangle rect) {
		BufferedImage screenImage;
		try {
			screenImage = new Robot().createScreenCapture(rect);
		} catch (AWTException ex) {
			throw new RuntimeException(ex);
		}
		return screenImage;
	}

	public static BufferedImage getScreenCapture() {
		BufferedImage screenImage;
		try {
			screenImage = new Robot().createScreenCapture(screenRectangle);
		} catch (AWTException ex) {
			throw new RuntimeException(ex);
		}
		return screenImage;
	}

	public static boolean waitForImage(int delay, String path) {
		return waitForImage(delay, path, 2147483647);
	}

	public static boolean waitForImage(int delay, String path, Rectangle rect) {
		return waitForImage(delay, path, 2147483647, rect);
	}

	public static boolean waitForImage(int delay, String path, int timeout, Rectangle rect) {
		Point p = isOnScreen(path, rect);
		int waitingTime = 0;
		while (p == null) {
			if (waitingTime > timeout)
				return false;
			try {
				Thread.sleep(delay);
			} catch (InterruptedException ex) {
				throw new RuntimeException(ex);
			}
			waitingTime += delay;
			p = isOnScreen(path);
		}
		return true;
	}

	public static boolean waitForImage(int delay, String path, int timeout) {
		return waitForImage(delay, path, timeout, screenRectangle);
	}

	public static void fillByColor(BufferedImage image, int color) {
		for (int i = 0; i < image.getWidth(); i++)
			for (int j = 0; j < image.getHeight(); j++)
				image.setRGB(i, j, color);
	}

	public static BufferedImage getSubimage(BufferedImage allImage, Rectangle rect) {
		BufferedImage result = allImage.getSubimage(rect.x, rect.y, rect.width, rect.height);
		return result;
	}

	public static BufferedImage createImage(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		BufferedImage result = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
		return result;
	}

	public static abstract interface SearchedImage {
		public abstract BufferedImage getSearchedImage();

		public abstract Rectangle getRectangle();
	}
}

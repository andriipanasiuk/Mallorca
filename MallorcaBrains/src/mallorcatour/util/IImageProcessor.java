package mallorcatour.util;

import java.awt.image.BufferedImage;

public abstract interface IImageProcessor {
	public static final IImageProcessor EmptyProcessor = new IImageProcessor() {
		public void processImage(BufferedImage image) {
		}
	};

	public abstract void processImage(BufferedImage paramBufferedImage);
}

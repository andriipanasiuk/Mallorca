package mallorcatour.util.robot;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class RetainColorProcessor implements IImageProcessor {
	private final Color color;
	private final Color bgColor;

	public RetainColorProcessor(Color colorToRetain, Color bgColor) {
		this.color = colorToRetain;
		this.bgColor = bgColor;
	}

	public void processImage(BufferedImage image) {
		for (int x = 0; x < image.getWidth(); x++)
			for (int y = 0; y < image.getHeight(); y++)
				if (this.color.getRGB() != image.getRGB(x, y))
					image.setRGB(x, y, this.bgColor.getRGB());
	}
}

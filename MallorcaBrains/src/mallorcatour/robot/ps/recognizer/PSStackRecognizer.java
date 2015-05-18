/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.ps.recognizer;

import java.awt.Color;
import java.awt.image.BufferedImage;

import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.recognizer.core.AbstractNumberRecognizer;
import mallorcatour.robot.recognizer.assets.IDigitAssets;
import mallorcatour.robot.recognizer.assets.ITableAssets;
import mallorcatour.util.IImageProcessor;
import mallorcatour.util.ImageUtils;

/**
 *
 * @author Andrew
 */
class PSStackRecognizer extends AbstractNumberRecognizer {

    private static final Color DIGIT_NOT_LIGHTED_COLOR = new Color(192, 192, 192);
    private static final Color DIGIT_LIGHTED_COLOR = new Color(32, 32, 32);
    private static final Color BG_COLOR = Color.WHITE;
    private IDigitAssets assets;
    private ITableAssets tableAssets;

    public PSStackRecognizer(IDigitAssets assets, ITableAssets tableAssets) {
        this.assets = assets;
        this.tableAssets = tableAssets;
    }

    public void changeAssets(IDigitAssets assets) {
        this.assets = assets;
    }

    public void changeTableAssets(ITableAssets assets) {
        this.tableAssets = assets;
    }
    private final IImageProcessor STACK_PREPROCESSOR = new IImageProcessor() {

        public void processImage(BufferedImage image) {
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    int rgb = image.getRGB(x, y);
                    if (rgb == DIGIT_LIGHTED_COLOR.getRGB()) {
                        image.setRGB(x, y, DIGIT_NOT_LIGHTED_COLOR.getRGB());
                    } else if (rgb != DIGIT_NOT_LIGHTED_COLOR.getRGB()) {
                        image.setRGB(x, y, BG_COLOR.getRGB());
                    }
                }
            }
        }
    };

    public int getStack(BufferedImage image) {
        if (ImageUtils.isPartOf(image, tableAssets.getSitoutImage(), STACK_PREPROCESSOR) != null) {
            return IGameInfo.SITTING_OUT;
        }
        return getNumber(image);

    }

    @Override
    protected IImageProcessor getPreprocessor() {
        return STACK_PREPROCESSOR;
    }

    public BufferedImage[] getDigitImages() {
        return assets.getDigitImages();
    }
}

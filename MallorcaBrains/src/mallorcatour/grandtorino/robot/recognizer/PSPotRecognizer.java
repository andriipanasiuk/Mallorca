/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.robot.recognizer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import mallorcatour.grandtorino.robot.recognizer.assets.IDigitAssets;
import mallorcatour.recognizer.core.AbstractNumberRecognizer;
import mallorcatour.util.IImageProcessor;
import mallorcatour.util.RetainColorProcessor;

/**
 *
 * @author Andrew
 */
class PSPotRecognizer extends AbstractNumberRecognizer {

    private IDigitAssets assets;

    public PSPotRecognizer(IDigitAssets assets) {
        this.assets = assets;
    }

    public void changeAssets(IDigitAssets assets) {
        this.assets = assets;
    }

    int getPot(BufferedImage image) {
        return getNumber(image);
    }

    @Override
    protected IImageProcessor getPreprocessor() {
        return new RetainColorProcessor(Color.BLACK, Color.WHITE);
    }

    public BufferedImage[] getDigitImages() {
        return assets.getDigitImages();
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.pa.recognizer;

import java.awt.image.BufferedImage;
import mallorcatour.recognizer.core.AbstractNumberRecognizer;
import mallorcatour.robot.recognizer.assets.IDigitAssets;

/**
 *
 * @author Andrew
 */
public class PAPotRecognizer extends AbstractNumberRecognizer {

    private IDigitAssets assets;

    public PAPotRecognizer(IDigitAssets assets) {
        this.assets = assets;
    }
    public int getPot(BufferedImage image) {
        return getNumber(image);
    }

    @Override
    public BufferedImage[] getDigitImages() {
        return assets.getDigitImages();
    }
}

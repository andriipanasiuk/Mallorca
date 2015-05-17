/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.pa.robot.recognizer;

import java.awt.image.BufferedImage;
import mallorcatour.grandtorino.robot.recognizer.assets.IDigitAssets;
import mallorcatour.recognizer.core.AbstractNumberRecognizer;

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

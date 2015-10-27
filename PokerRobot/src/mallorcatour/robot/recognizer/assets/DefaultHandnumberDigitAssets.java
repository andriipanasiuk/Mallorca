/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.recognizer.assets;

import java.awt.image.BufferedImage;

/**
 *
 * @author Andrew
 */
public class DefaultHandnumberDigitAssets extends AbstractDigitAssets {

    private static final String PATH_BASE = "assets/ps/default/hand_";
    private static final BufferedImage[] digitImages;

    static {
        digitImages = createImageArray(PATH_BASE);
    }

    public BufferedImage[] getDigitImages() {
        return digitImages;
    }
}

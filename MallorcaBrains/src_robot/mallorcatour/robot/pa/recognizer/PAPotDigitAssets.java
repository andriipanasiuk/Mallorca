/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.pa.recognizer;

import mallorcatour.robot.recognizer.assets.*;

import java.awt.image.BufferedImage;

/**
 *
 * @author Andrew
 */
class PAPotDigitAssets extends AbstractDigitAssets {

    private static final String PATH_BASE = "assets/pa/pot_";
    private static final BufferedImage[] digitImages;

    static {
        digitImages = createImageArray(PATH_BASE);
    }

    public BufferedImage[] getDigitImages() {
        return digitImages;
    }
}

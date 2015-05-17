/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.robot.recognizer.assets;

import java.awt.image.BufferedImage;
import mallorcatour.util.ImageUtils;

/**
 *
 * @author Andrew
 */
public abstract class AbstractDigitAssets implements IDigitAssets {

    protected static BufferedImage[] createImageArray(String pathBase) {
        BufferedImage[] result = new BufferedImage[10];
        for (int i = 0; i <= 9; i++) {
            result[i] = ImageUtils.fromFile(pathBase + i + ".png");
        }
        return result;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.ps.recognizer;

import java.awt.image.BufferedImage;

import mallorcatour.robot.recognizer.assets.ITableAssets;
import mallorcatour.robot.util.ImageUtils;

/**
 *
 * @author Andrew
 */
class PSButtonRecognizer {

    private ITableAssets tableAssets;

    public PSButtonRecognizer(ITableAssets tableAssets) {
        this.tableAssets = tableAssets;
    }

    public void changeAssets(ITableAssets assets) {
        this.tableAssets = assets;
    }

    public boolean isButton(BufferedImage image) {
        if (ImageUtils.isPartOf(image, tableAssets.getButtonImage()) != null) {
            return true;
        }
        return false;
    }
}

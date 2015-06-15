/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.ps.recognizer;

import java.awt.Color;
import java.awt.image.BufferedImage;

import mallorcatour.interfaces.IDistanceCalculator;
import mallorcatour.robot.recognizer.ColorDistanceCalculator;
import mallorcatour.tools.Log;

/**
 *
 * @author Andrew
 */
class PSActionButtonRecognizer {

    private final static Color ACTION_BUTTON_BG_COLOR = new Color(148, 66, 15);
    private final static IDistanceCalculator<Color> distance = new ColorDistanceCalculator();
    private final static double MAX_DISTANCE = 40;

    public boolean isButton(BufferedImage image) {
        double summary = 0;
        int count = 0;
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                summary += distance.getDistance(new Color(image.getRGB(i, j)),
                        ACTION_BUTTON_BG_COLOR);
                count++;
            }
        }
        double average = summary / count;
        Log.d("Average distance to default color: " + average);
        return average < MAX_DISTANCE;
    }
}

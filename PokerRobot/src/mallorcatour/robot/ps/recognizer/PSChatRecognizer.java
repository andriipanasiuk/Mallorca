/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.ps.recognizer;

import java.awt.Color;
import java.awt.image.BufferedImage;

import mallorcatour.tools.Log;

/**
 *
 * @author Andrew
 */
class PSChatRecognizer {

    private final static int CHAT_BG_COLOR_1 = new Color(238, 230, 195).getRGB();
    private final static int CHAT_BG_COLOR_2 = new Color(255, 246, 207).getRGB();
    private final static double THRESHOLD = 0.7;

    public boolean checkChat(BufferedImage chatImage) {
        int normalBGCount = 0, allCount = 0;
        for (int i = 0; i < chatImage.getWidth(); i++) {
            for (int j = 0; j < chatImage.getHeight(); j++) {
                int rgb = chatImage.getRGB(i, j);
                if (rgb == CHAT_BG_COLOR_1 || rgb == CHAT_BG_COLOR_2) {
                    normalBGCount++;
                }
                allCount++;
            }
        }
        double ratio = (double) normalBGCount / allCount;
        Log.d("checkChat. BG count: " + normalBGCount + " All count: " + allCount);
        Log.d("checkChat. Ratio: " + ratio);
        return ratio > THRESHOLD;
    }
}

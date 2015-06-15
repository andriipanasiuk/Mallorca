/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mouselogger;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;

import mallorcatour.tools.ReaderUtils;
import mallorcatour.util.robot.ImageUtils;

/**
 *
 * @author Andrew
 */
public class MouseLogProcessor {

    public static void toImageFile(String pathToLog, String pathToImage) {
        BufferedImage image = new BufferedImage(1200, 800, BufferedImage.TYPE_INT_RGB);
        ImageUtils.fillByColor(image, Color.WHITE.getRGB());
        BufferedReader reader = ReaderUtils.initReader(pathToLog);
        String buffer = ReaderUtils.readLineFrom(reader);
        Point point = null;
        while (buffer != null) {
            if (buffer.equals("")) {
            } else if (buffer.equals("Click")) {
                image.setRGB(point.x, point.y, Color.RED.getRGB());
            } else {
                point = parseMouseLog(buffer);
                try {
                    image.setRGB(point.x, point.y, Color.BLACK.getRGB());
                } catch (Exception ex) {
                    int i = 0 + 2;
                }
            }
            buffer = ReaderUtils.readLineFrom(reader);
        }
        ImageUtils.toFile(image, pathToImage, false);
    }

    private static Point parseMouseLog(String log) {
        int fromX, toX, fromY, toY;
        fromX = log.indexOf("X: ") + "X: ".length();
        fromY = log.indexOf("Y: ") + "Y: ".length();
        toX = fromY - 4;
        toY = log.indexOf(" Time");
        int x = Integer.parseInt(log.substring(fromX, toX));
        int y = 0;
        try {
            y = Integer.parseInt(log.substring(fromY, toY));
        } catch (Exception e) {
            int i = 0 + 2;
        }
        return new Point(x, y);
    }
}

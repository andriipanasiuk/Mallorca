/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.recognizer.core;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mallorcatour.grandtorino.controller.PokerPreferences;
import mallorcatour.util.DateUtils;
import mallorcatour.util.IImageProcessor;
import mallorcatour.util.ImageUtils;

/**
 *
 * @author Andrew
 */
public abstract class AbstractNumberRecognizer {

    private static class Pair implements Comparable<Pair> {

        int number;
        int positionX;

        Pair(int number, int positionX) {
            this.number = number;
            this.positionX = positionX;
        }

        public int compareTo(Pair o) {
            if (this.positionX > o.positionX) {
                return 1;
            } else if (this.positionX == o.positionX) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    /**
     * Should be overrided by recognizers that need preprocess images before
     * recognizing process.
     * @return IImageProcessor
     */
    protected IImageProcessor getPreprocessor() {
        return IImageProcessor.EmptyProcessor;
    }

    public abstract BufferedImage[] getDigitImages();

    private void preprocess(BufferedImage image) {
        getPreprocessor().processImage(image);
    }

    private List<Pair> searchForAllNumbers(BufferedImage image) {
        List<Pair> result = new ArrayList<Pair>();

        preprocess(image);
        if (PokerPreferences.LOG_IMAGE_RECOGNITION) {
            ImageUtils.toFile(image, "getNumber_" + DateUtils.getDate(true) + ".png", true);
        }
        for (int digit = 0; digit < getDigitImages().length; digit++) {
            BufferedImage numberImage = getDigitImages()[digit];
            preprocess(numberImage);
            List<Point> positions = ImageUtils.getAllEntries(image,
                    numberImage);
            for (Point position : positions) {
                result.add(new Pair(digit, position.x));
            }
        }
        return result;
    }

    //return recognized number from image.
    protected int getNumber(BufferedImage image) {
        List<Pair> numberPositions = searchForAllNumbers(image);
        Collections.sort(numberPositions);

        int result = 0;
        for (int i = numberPositions.size() - 1; i >= 0; i--) {
            result += numberPositions.get(i).number * Math.pow(10, numberPositions.size() - 1 - i);
        }
        return result;
    }

    //return recognized number from image.
    protected long getNumberLong(BufferedImage image) {
        List<Pair> numberPositions = searchForAllNumbers(image);
        Collections.sort(numberPositions);

        long result = 0;
        for (int i = numberPositions.size() - 1; i >= 0; i--) {
            result += numberPositions.get(i).number * Math.pow(10, numberPositions.size() - 1 - i);
        }
        return result;
    }
}

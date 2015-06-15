/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.pa.recognizer;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import mallorcatour.core.game.advice.Advice;
import mallorcatour.tools.Log;
import mallorcatour.util.robot.ImageUtils;
import mallorcatour.util.robot.RecognizerUtils;

/**
 *
 * @author Andrew
 */
public class AdviceRecognizer {

    //dimensions
    private final int ADVICE_SIZE = 58;
    private final int ADVICE_MARGIN_X = -146;
    private final int ADVICE_MARGIN_Y = -17;
    private Point advisorPosition;
    //colors
    private final int ADVICE_BACKGROUND_COLOR = -12763842;
    private final int FOLD_COLOR = -1688516;
    private final int PASSIVE_COLOR = -12829466;
    private final int AGGRESSIVE_COLOR = -12793796;
    //path to advisor image pattern
    private final String ADVISOR_PATH = "assets/pa/advisor.png";
    private final Rectangle ADVISOR_RECTANGLE = new Rectangle(915, 352, 100, 100);
    private final Rectangle ADVISOR_RECTANGLE_POKER_GENIUS = new Rectangle(915, 502, 100, 100);
    private final int WAITING_FOR_ADVICE_DELAY = 150;

    public void reset(Point topLeftPoint) {
		Rectangle advisorRectangle = RecognizerUtils.getGlobalRectangle(
				ADVISOR_RECTANGLE_POKER_GENIUS, topLeftPoint);
		ImageUtils.toFile(ImageUtils.getScreenCapture(advisorRectangle),
				"debug_" + System.currentTimeMillis() + ".png", true);
		advisorPosition = ImageUtils.isOnScreen(ADVISOR_PATH, advisorRectangle);
		if (advisorPosition == null) {
			throw new IllegalStateException("There is no advisor on the screen");
		}
        advisorPosition.translate(topLeftPoint.x, topLeftPoint.y);
        advisorPosition.translate(ADVISOR_RECTANGLE_POKER_GENIUS.x, ADVISOR_RECTANGLE_POKER_GENIUS.y);
        Log.d("Advisor position: " + advisorPosition.toString());
    }

    private BufferedImage getAdviceImage() {
        BufferedImage adviceImage = ImageUtils.getScreenCapture(new Rectangle(
                advisorPosition.x + ADVICE_MARGIN_X, advisorPosition.y + ADVICE_MARGIN_Y,
                ADVICE_SIZE, ADVICE_SIZE));

        if (adviceImage.getRGB(ADVICE_SIZE / 2, ADVICE_SIZE / 2) == ADVICE_BACKGROUND_COLOR) {
            Log.d("There is no advice on the screen");
            return null;
        }
        return adviceImage;
    }

    boolean isAdviceOnScreen() {
        BufferedImage adviceImage = ImageUtils.getScreenCapture(new Rectangle(
                advisorPosition.x + ADVICE_MARGIN_X, advisorPosition.y + ADVICE_MARGIN_Y,
                ADVICE_SIZE, ADVICE_SIZE));
        return (adviceImage.getRGB(ADVICE_SIZE / 2, ADVICE_SIZE / 2) != ADVICE_BACKGROUND_COLOR);
    }

    public boolean waitForAdvice(int timeout) {
        int waitingTime = 0;
        while (!isAdviceOnScreen()) {
            try {
                Thread.sleep(WAITING_FOR_ADVICE_DELAY);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            waitingTime += WAITING_FOR_ADVICE_DELAY;
            if (waitingTime > timeout) {
                Log.d("waitForAdvice(): waiting time exceeds timeout");
                return false;
            }
        }
        return true;
    }

    public Advice getAdvice() {
        int foldCount = 0, passiveCount = 0, aggressiveCount = 0;
        BufferedImage adviceImage = getAdviceImage();
        if (adviceImage == null) {
            return null;
        }
        for (int i = 0; i < adviceImage.getWidth(); i++) {
            for (int j = 0; j < adviceImage.getHeight(); j++) {
                switch (adviceImage.getRGB(i, j)) {
                    case FOLD_COLOR:
                        foldCount++;
                        break;
                    case PASSIVE_COLOR:
                        passiveCount++;
                        break;
                    case AGGRESSIVE_COLOR:
                        aggressiveCount++;
                        break;
                }
            }
        }
        Log.d("Count of fold color: " + foldCount);
        Log.d("Count of passive color: " + passiveCount);
        Log.d("Count of aggressive color: " + aggressiveCount);
        return Advice.create(foldCount, passiveCount, aggressiveCount);
    }
}

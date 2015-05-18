/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.pa.recognizer;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import mallorcatour.robot.recognizer.OnMyActionListener;
import mallorcatour.util.ImageUtils;
import mallorcatour.util.ImageUtils.SearchedImage;
import mallorcatour.util.Log;
import mallorcatour.util.RecognizerUtils;
import mallorcatour.util.ThreadUtils;

/**
 *
 * @author Andrew
 */
public class PATableRecognizer extends BaseTableRecognizer {

	// path to image patterns
	private final static String RAISE_PATH = "assets/pa/raise.png";
	private final static String BET_PATH = "assets/pa/bet.png";
	private final static String BUTTON_PATH = "assets/pa/button.png";
	private final static String TOP_LEFT_PATH = "assets/pa/top_left.png";
	private final String PROCEED_BUTTON_PATH = "assets/pa/proceed_button.png";
	private final String FOLD_BUTTON_PATH = "assets/pa/fold.png";
	private final static int WAIT_FOR_MY_ACTION_TIMEOUT = 15000;
	private final static int WAIT_FOR_MY_ACTION_DELAY = 100;
	private final static int TOP_LEFT_MARGIN_X = -10;
	private final static int TOP_LEFT_MARGIN_Y = -8;
	private final static Rectangle PROCEED_BUTTON_RECTANGLE = new Rectangle(
			445, 439, 45, 19);
	private final static Rectangle FOLD_BUTTON_RECTANGLE = new Rectangle(150,
			668, 200, 75);
	// rectangles
	private final static Rectangle DEAL_BUTTON_RECTANGLE = new Rectangle(361,
			675, 170, 50);
	private final static Rectangle POT_RECTANGLE = new Rectangle(83, 330, 135,
			44);
	private static final Rectangle START_GAME_RECTANGLE = new Rectangle(464,
			645, 170, 50);
	private static final Rectangle AGGRESSIVE_ACTION_RECT = new Rectangle(496,
			674, 200, 75);

	private Point topLeftPoint;
	private final PAPotRecognizer potRecognizer;
	private boolean waitedForMyAction;

	public PATableRecognizer() {
		potRecognizer = new PAPotRecognizer(new PAPotDigitAssets());
	}

	public void reset() {
		topLeftPoint = ImageUtils.isOnScreen(TOP_LEFT_PATH);
		if (topLeftPoint == null) {
			throw new IllegalStateException(
					"There is no PAAcademy on the screen");
		} else {
			topLeftPoint.translate(TOP_LEFT_MARGIN_X, TOP_LEFT_MARGIN_Y);
		}
		Log.d("PA Academy position is " + topLeftPoint.toString());
	}

	public double getPot() {
		return potRecognizer.getPot(ImageUtils.getScreenCapture(RecognizerUtils
				.getGlobalRectangle(POT_RECTANGLE, topLeftPoint)));
	}

	public int getMyStack() {
		throw new UnsupportedOperationException();
	}

	public int getOpponentStack() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected String getMyActionPath() {
		return BUTTON_PATH;
	}

	@Override
	protected String getRaisePath() {
		return RAISE_PATH;
	}

	@Override
	protected String getBetPath() {
		return BET_PATH;
	}

	@Override
	public Rectangle getMyActionRectangle(boolean global) {
		throw new RuntimeException();
	}

	@Override
	protected Rectangle getActivateRectangle(boolean global) {
		Rectangle rect = new Rectangle(841, 16, 100, 50);
		if (global) {
			rect.translate(topLeftPoint.x, topLeftPoint.y);
		}
		return rect;
	}

	@Override
	public Point getTopLeftPoint() {
		return topLeftPoint;
	}

	@Override
	protected Rectangle getAggressiveRectangle(boolean global) {
		Rectangle result = new Rectangle(AGGRESSIVE_ACTION_RECT);
		if (global) {
			result.translate(topLeftPoint.x, topLeftPoint.y);
		}
		return result;
	}

	public Rectangle getStartGameRectangle(boolean global) {
		Log.d("getStartTournamentRectangle()");
		Rectangle result = new Rectangle(START_GAME_RECTANGLE);
		if (global) {
			result.translate(topLeftPoint.x, topLeftPoint.y);
		}
		return result;
	}

	public Rectangle getDealButtonRectangle(boolean global) {
		Rectangle rect = new Rectangle(DEAL_BUTTON_RECTANGLE);
		if (global) {
			rect.translate(topLeftPoint.x, topLeftPoint.y);
		}
		return rect;
	}

	public Rectangle getEndTournamentRectangle(boolean global) {
		Rectangle rect = new Rectangle(335, 425, 150, 75);
		if (global) {
			rect.translate(topLeftPoint.x, topLeftPoint.y);
		}
		return rect;
	}

	public void waitForMyAction(OnMyActionListener listener) {
		List<SearchedImage> images = new ArrayList<SearchedImage>();
		images.add(new SearchedImage() {

			public BufferedImage getSearchedImage() {
				return ImageUtils.fromFile(FOLD_BUTTON_PATH);
			}

			public Rectangle getRectangle() {
				return RecognizerUtils.getGlobalRectangle(
						FOLD_BUTTON_RECTANGLE, topLeftPoint);
			}
		});
		int waitingTime = 0;
		waitedForMyAction = true;
		while (waitedForMyAction) {
			for (int i = 0; i < images.size(); i++) {
				SearchedImage image = images.get(i);
				Point p = ImageUtils.isOnScreen(image.getSearchedImage(),
						image.getRectangle());
				if (p != null) {
					if (i == 0) {
						listener.onMyAction(OnMyActionListener.MY_ACTION);
					} else if (i == 1) {
						listener.onMyAction(OnMyActionListener.PA_PROCEED_WINDOW);
					}
					return;
				}
			}
			if (waitingTime > WAIT_FOR_MY_ACTION_TIMEOUT) {
				listener.onMyAction(OnMyActionListener.TIMEOUT);
				return;
			}
			ThreadUtils.sleep(WAIT_FOR_MY_ACTION_DELAY);
			waitingTime += WAIT_FOR_MY_ACTION_DELAY;
		}
	}

	public void stopWaitingForMyAction() {
		waitedForMyAction = false;
	}
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.pa;

import java.awt.Point;

import mallorcatour.robot.pa.recognizer.AdviceRecognizer;
import mallorcatour.robot.pa.recognizer.PATableRecognizer;
import mallorcatour.robot.recognizer.OnMyActionListener;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutorService;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.util.ExecutorUtils;
import mallorcatour.util.Log;
import mallorcatour.util.OnExceptionListener;
import mallorcatour.util.robot.ImageUtils;
import mallorcatour.util.robot.RecognizerUtils;
import mallorcatour.util.robot.RobotUtils;

/**
 *
 * @author Andrew
 */
public class PAGameRobot {

    private ExecutorService executor;
    private static final String BACK_TO_LOBBY_PATH = "assets/poker_genius/back_to_lobby.png";
    private static final String START_GAME_PATH = "assets/poker_genius/start_game.png";
    private static final String LOAD_TABLE_PATH = "assets/pa/load_table.png";
    private static final String DEAL_BUTTON_PATH = "assets/pa/deal.png";
    private static final String HANDS_PATH = "assets/pa/hands.png";
    private static final String IS_IN_FOCUS_PATH = "assets/poker_genius/in_focus.png";
    private static final String PROCEED_BUTTON_PATH = "assets/pa/proceed_button.png";
    private static final Rectangle PROCEED_BUTTON_RECTANGLE = new Rectangle(445, 439, 45, 19);
    private static final Point SET_BANKROLL_POINT = new Point(400, 605);
    private static final Rectangle IS_IN_FOCUS_RECTANGLE = new Rectangle(0, 730, 600, 70);
    private static final Rectangle IS_IN_FOCUS_RECTANGLE_VERTICAL = new Rectangle(1300, 0, 60, 750);
    private static final Rectangle HANDS_RECTANGLE = new Rectangle(830, 107, 112, 36);
    //time variables
    private static final int WAITING_FOR_ADVICE_TIMEOUT = 4000;
    private static final int PAUSE_BEFORE_ACTION = 0;
    private boolean paused = false;
    //recognizers
    private final AdviceRecognizer adviceRecognizer;
    private final PATableRecognizer tableRecognizer;
    private final LimitType limitType;
    private final GameType gameType;
    private PARobotListener listener;

    public enum GameType {

        CASH,
        SNG
    }

    public PAGameRobot(LimitType limitType, GameType gameType) {
        executor = ExecutorUtils.newSingleThreadExecutor(OnExceptionListener.EMPTY);
        tableRecognizer = new PATableRecognizer();
        adviceRecognizer = new AdviceRecognizer();
        this.limitType = limitType;
        this.gameType = gameType;
    }

    public void setListener(PARobotListener listener) {
        this.listener = listener;
    }

    private void init() {
        resetTableRecognizer();
        Point topLeft = tableRecognizer.getTopLeftPoint();
        Rectangle startGame = tableRecognizer.getStartGameRectangle(true);
        if (gameType == GameType.SNG) {
        	Log.d("Top left point: " + topLeft + " Start game rect: " + startGame);
        	ImageUtils.toFile(ImageUtils.getScreenCapture(startGame), "startgame_before_wait.png", true);
            ImageUtils.waitForImage(1000, START_GAME_PATH, startGame);
            ImageUtils.toFile(ImageUtils.getScreenCapture(startGame), "startgame_after_wait.png", true);
            RobotUtils.pressMouseOnImage(START_GAME_PATH, startGame);
        } else if (gameType == GameType.CASH) {
            ImageUtils.waitForImage(1000, LOAD_TABLE_PATH, startGame);
            RobotUtils.pressMouseOnImage(LOAD_TABLE_PATH, startGame);

        }
        ImageUtils.waitForImage(1200, DEAL_BUTTON_PATH, tableRecognizer.getDealButtonRectangle(true));
        if (gameType == GameType.CASH) {
            RobotUtils.pressRightMouse(200, SET_BANKROLL_POINT.x + topLeft.x,
                    SET_BANKROLL_POINT.y + topLeft.y);
            RobotUtils.delay(1000);
            RobotUtils.pressLeftMouse(0, SET_BANKROLL_POINT.x + topLeft.x + 10,
                    SET_BANKROLL_POINT.y + topLeft.y + 10);
            RobotUtils.delay(4000);
            RobotUtils.clickButton("2");
            RobotUtils.clickButton("0");
            RobotUtils.clickButton("0");
            RobotUtils.clickButton("0");
            RobotUtils.clickButton("0");
            RobotUtils.pressLeftMouse(2000, 363 + topLeft.x,
                    420 + topLeft.y);
            RobotUtils.delay(4000);
        }
        ImageUtils.waitForImage(1200, DEAL_BUTTON_PATH, tableRecognizer.getDealButtonRectangle(true));

        RobotUtils.pressMouseOnImage(DEAL_BUTTON_PATH, tableRecognizer.getDealButtonRectangle(true));
        RobotUtils.delay(100);
        listener.onGameStarted();
    }

    private void resetTableRecognizer() {
        tableRecognizer.reset();
    }

    private void resetAdviceRecognizer() {
        adviceRecognizer.reset(tableRecognizer.getTopLeftPoint());
    }

    private void activateGameWindow() {
        RobotUtils.pressMouseOnImage(HANDS_PATH,
                RecognizerUtils.getGlobalRectangle(HANDS_RECTANGLE, tableRecognizer.getTopLeftPoint()));
    }

    private boolean isInFocus() {
        return ImageUtils.isOnScreen(IS_IN_FOCUS_PATH, IS_IN_FOCUS_RECTANGLE_VERTICAL) != null;
    }

    private void resumeGameInCurrentThread() {
        listener.onGameResumed();
        if (paused) {
            activateGameWindow();
        }
        paused = false;
        while (!paused) {
        	Log.d("waiting for myAction");
            tableRecognizer.waitForMyAction(new OnMyActionListener() {

                public void onMyAction(int response) {
                	Log.d("myAction response: " + response);
                    if (response == OnMyActionListener.MY_ACTION) {
                        resetAdviceRecognizer();
                        adviceRecognizer.waitForAdvice(WAITING_FOR_ADVICE_TIMEOUT);
                        Advice advice = adviceRecognizer.getAdvice();
                        listener.onAction(advice);
                        Action myAction = advice.getAction();
                        RobotUtils.delay(PAUSE_BEFORE_ACTION);
                        if (!isInFocus()) {
                            Log.d("Now robot will be activate window PA");
                            activateGameWindow();
                        }
                        Log.d(advice.toString());
                        doAction(myAction);
                    } else if (response == OnMyActionListener.PA_PROCEED_WINDOW) {
                        RobotUtils.pressMouseOnImage(PROCEED_BUTTON_PATH,
                                RecognizerUtils.getGlobalRectangle(PROCEED_BUTTON_RECTANGLE,
                                tableRecognizer.getTopLeftPoint()));
                    } else if (response == OnMyActionListener.TIMEOUT) {
                        if (endTournament()) {
                            Log.d("Game over");
                            listener.onGameEnded();
                            paused = true;
                        } else {
                            RobotUtils.pressMouseOnImage(PROCEED_BUTTON_PATH,
                                    RecognizerUtils.getGlobalRectangle(PROCEED_BUTTON_RECTANGLE,
                                    tableRecognizer.getTopLeftPoint()));
                        }
                    }
                }
            });
        }
        if (paused) {
            listener.onGamePaused();
        } else {
            throw new RuntimeException("After advice not found there is no "
                    + "Back to lobby button on the screen!");
        }
    }

    private void doAction(Action myAction) {
        if (myAction.isFold()) {
            doFold();
        } else if (myAction.isPassive()) {
            doPassiveAction();
        } else if (myAction.isAggressive()) {
            doAggressiveAction();
        }
    }

    private boolean endTournament() {
        return RobotUtils.pressMouseOnImage(BACK_TO_LOBBY_PATH);
    }

    private int calculateRaiseAmount() {
        if (tableRecognizer.isBetAction()) {
            double pot = tableRecognizer.getPot();
            return (int) Action.createBetAction(pot, Double.MAX_VALUE).getAmount();
        } else {
            return -1;
        }
    }

    public void resumeGame() {
        executor.submit(new Runnable() {

            public void run() {
                resumeGameInCurrentThread();
            }
        });
    }

    public void startGame() {
        executor.submit(new Runnable() {

            public void run() {
                init();
                resumeGameInCurrentThread();
            }
        });
    }

    public void pauseGame() {
        paused = true;
        tableRecognizer.stopWaitingForMyAction();
    }

    private void doAggressiveAction() {
    	Log.d("doAggressive");
        if (limitType == LimitType.FIXED_LIMIT) {
            RobotUtils.clickButton("R");
        } else if (limitType == LimitType.NO_LIMIT) {
			if (tableRecognizer.isBetAction()) {
				RobotUtils.clickButton("H");
			} else {
				RobotUtils.clickButton("P");
			}
			// TODO calculate correct amount of raise. Now pot numbers are not
			// correctly recognized.
        }
    }

	@SuppressWarnings("unused")
	private void doBetAction(int amount) {
		RobotUtils.clickButton("E");
		RobotUtils.delay(100);
		String raise = String.valueOf(amount);
		for (int i = 0; i < raise.length(); i++) {
			RobotUtils.clickButton(String.valueOf(raise.charAt(i)));
		}
		RobotUtils.clickButton(KeyEvent.VK_ENTER);
		RobotUtils.clickButton("R");
	}

    private void doPassiveAction() {
    	Log.d("doPassive");
        RobotUtils.clickButton("C");
    }

    private void doFold() {
    	Log.d("doFold");
        RobotUtils.clickButton("F");
    }
}

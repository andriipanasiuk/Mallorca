/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.ps;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import mallorcatour.bot.interfaces.IGameController;
import mallorcatour.bot.villainobserver.IVillainObserver;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.LimitType;
import mallorcatour.robot.ActionSynchronizer;
import mallorcatour.robot.ChatTracker;
import mallorcatour.robot.HumanColorTableInteractor;
import mallorcatour.robot.ITableInteractor;
import mallorcatour.robot.KeyboardTableInteractor;
import mallorcatour.robot.PlayerInfo;
import mallorcatour.robot.controller.PokerPreferences;
import mallorcatour.robot.hardwaremanager.MouseClickLimiter;
import mallorcatour.robot.hardwaremanager.MouseDragLimiter;
import mallorcatour.robot.interfaces.IGameRobot;
import mallorcatour.robot.ps.recognizer.PSTableRecognizer;
import mallorcatour.robot.recognizer.ITableListener;
import mallorcatour.util.ExecutorUtils;
import mallorcatour.util.IRectangleHolder;
import mallorcatour.util.Log;
import mallorcatour.util.OnExceptionListener;
import mallorcatour.util.RecognizerUtils;
import mallorcatour.util.ThreadUtils;

/**
 *
 * @author Andrew
 */
public class PSGameRobot implements IGameRobot, ITableListener {

    private static final int WAIT_AFTER_ACTION = 1000;
    private static final int WAIT_FOR_MY_ACTION_DELAY = 750;
    private static final int WAIT_FOR_MY_ACTION_TIMEOUT = 120000;
    private static final boolean LOG_TIME = true;
    private long currentHandNumber = -1;
    private boolean paused = true;
    private boolean isGameStarted = false;
    private PlayerInfo heroInfo, villainInfo;
    private ITableInteractor tableInteractor;
    private final ExecutorService executor;
    private final PSTableRecognizer tableRecognizer;
    private final IGameController controller;
    private final LimitType limitType;
    private final String DEBUG_PATH;
    private final IVillainObserver villainObserver;
    private final IRectangleHolder limitedClickZone;
    private final ChatTracker chatTracker;
    private final String heroName;

    public PSGameRobot(IGameController controller, LimitType limitType,
            String debugPath, String heroName, IVillainObserver villainObserver) {
        this.controller = controller;
        this.limitType = limitType;
        this.tableRecognizer = new PSTableRecognizer();
        this.chatTracker = new ChatTracker(this, tableRecognizer);
        this.villainObserver = villainObserver;
        this.DEBUG_PATH = debugPath;
        this.heroName = heroName;
        this.tableInteractor = new KeyboardTableInteractor(debugPath, heroName,
                this, tableRecognizer);
        this.limitedClickZone = new IRectangleHolder() {

            public Rectangle getRectangle() {
                return RecognizerUtils.getGlobalRectangle(
                        tableRecognizer.getActionButtonsRectangle(),
                        tableRecognizer.getTopLeftPosition());
            }
        };
        this.executor = ExecutorUtils.newSingleThreadExecutor(new OnExceptionListener() {

            public void onException(Exception e) {
                MouseClickLimiter.removeBan(limitedClickZone);
            }
        });
    }

    public void changeInteractor(boolean human) {
        if (human) {
            this.tableInteractor = new HumanColorTableInteractor(DEBUG_PATH, heroName,
                    this, tableRecognizer);
        } else {
            this.tableInteractor = new KeyboardTableInteractor(DEBUG_PATH, heroName,
                    this, tableRecognizer);
        }
    }

    public void onTableVisible(Point topLeftPoint, Dimension dimension) {
        Log.d("Method called: onTableVisible");
        tableRecognizer.onTableVisible(topLeftPoint, dimension);
    }

    public boolean isSupportedDimension(Dimension dimension) {
        return tableRecognizer.isSupportedDimension(dimension);
    }

    public void onTableInvisible() {
        Log.d("Method called: onTableInvisible");
        tableRecognizer.onTableInvisible();
    }

    private void endSession() {
        Log.d("PSGameRobot " + this + " Session ended.");
        villainObserver.endSession();
        villainInfo.changeName(PokerPreferences.DEFAULT_VILLAIN_NAME);
    }

    private void checkForMyAction() {
        ActionSynchronizer.beforeLogic();
        boolean isFoldButton = tableRecognizer.isFoldButton();
        ActionSynchronizer.endOfLogic();
        if (isFoldButton) {
            isGameStarted = true;
            myAction();
            ThreadUtils.sleep(WAIT_AFTER_ACTION);
        } else {
            ActionSynchronizer.beforeLogic();
            boolean isEmptySeat = tableRecognizer.isEmptySeat();
            ActionSynchronizer.endOfLogic();
            if (isEmptySeat && isGameStarted) {
                Log.d("PSGameRobot " + PSGameRobot.this + ": Empty seat");
                endSession();
                isGameStarted = false;
            }
        }
    }

    private void resumeGameInCurrentThread() {
        while (!paused) {
            checkForMyAction();
            ThreadUtils.sleep(WAIT_FOR_MY_ACTION_DELAY);
        }
    }

    private void myAction() {
        Log.d("Method called: myAction");
        long start = System.currentTimeMillis();
        Card holeCard1 = null, holeCard2 = null;
        ActionSynchronizer.beforeLogic();
        MouseDragLimiter.switchDragging(false);
        tableRecognizer.scrapTable();
        MouseDragLimiter.switchDragging(true);
        long handNumber = tableRecognizer.getHandNumber();
        if (currentHandNumber != handNumber) {
            //recognizing hole cards
            HoleCards holeCards = tableRecognizer.getHoleCards();
            holeCard1 = holeCards.first;
            holeCard2 = holeCards.second;

            //recognizing button
            boolean heroOnButton = tableRecognizer.isHeroOnButton();
            boolean villainOnButton = !heroOnButton;
            //creating new player infos
            heroInfo = new PlayerInfo(heroName, heroOnButton);
            villainInfo = new PlayerInfo(PokerPreferences.DEFAULT_VILLAIN_NAME, villainOnButton);
        }
        //recognizing board cards
        List<Card> boardCards = tableRecognizer.getBoardCards();
        //recognizing stacks
        heroInfo.stack = tableRecognizer.getHeroStack();
        villainInfo.stack = tableRecognizer.getVillainStack();
        //recognizing bets
        heroInfo.bet = tableRecognizer.getHeroBet();
        villainInfo.bet = tableRecognizer.getVillainBet();
        //recognizing pot
        double pot = tableRecognizer.getPot();
        //logging
        Log.f(DEBUG_PATH, "PSGameRobot " + PSGameRobot.this + ": My action");
        Log.f(DEBUG_PATH, "PSGameRobot. Pot: " + pot);
        Log.f(DEBUG_PATH, "PSGameRobot. Hero bet: " + heroInfo.bet);
        Log.f(DEBUG_PATH, "PSGameRobot. Villain bet: " + villainInfo.bet);
        Log.f(DEBUG_PATH, "PSGameRobot. Hero stack: " + heroInfo.stack);
        Log.f(DEBUG_PATH, "PSGameRobot. Villain stack: " + villainInfo.stack);
        Log.f(DEBUG_PATH, "PSGameRobot. Hand number: " + handNumber);
        Log.f(DEBUG_PATH, "PSGameRobot. Hole cards: " + holeCard1 + " " + holeCard2);
        Log.f(DEBUG_PATH, "PSGameRobot. Board cards: " + boardCards);
        Log.d("PSGameRobot " + PSGameRobot.this + ": My action");
        Log.d("PSGameRobot " + this + " Pot: " + pot);
        Log.d("PSGameRobot " + this + " Hero bet: " + heroInfo.bet);
        Log.d("PSGameRobot " + this + " Villain bet: " + villainInfo.bet);
        Log.d("PSGameRobot " + this + " Hero stack: " + heroInfo.stack);
        Log.d("PSGameRobot " + this + " Villain stack: " + villainInfo.stack);
        Log.d("PSGameRobot " + this + " Hand number: " + handNumber);
        Log.d("PSGameRobot " + this + " Hole cards: " + holeCard1 + " " + holeCard2);
        Log.d("PSGameRobot " + this + " Board cards: " + boardCards);

        ActionSynchronizer.endOfLogic();
        ThreadUtils.sleep(50);
        ActionSynchronizer.beforeLogic();
        long start1, recognizingTime, timeDecisionMaking;
        Action action;
        recognizingTime = (System.currentTimeMillis() - start);
        if (currentHandNumber != handNumber) {
            if (currentHandNumber != -1) {
                villainObserver.onHandPlayed(currentHandNumber);
                if (villainObserver.isVillainKnown()) {
                    String newVillainName = villainObserver.getCurrentVillain().getName();
                    villainInfo.changeName(newVillainName);
                }
            }
            currentHandNumber = handNumber;
            controller.onNewHand(currentHandNumber,
                    Arrays.asList(new PlayerInfo[]{heroInfo, villainInfo}),
                    holeCard1, holeCard2, boardCards, pot, limitType);
        }

        start1 = System.currentTimeMillis();
        action = controller.onMyAction(boardCards, pot);
        timeDecisionMaking = System.currentTimeMillis() - start1;
        ActionSynchronizer.endOfLogic();

        tableInteractor.doAction(action);

        if (LOG_TIME) {
            Log.f(DEBUG_PATH, "\n========  Time logging =======");
            Log.f(DEBUG_PATH, "Time of recognizing the table: "
                    + recognizingTime + " ms");
            Log.f(DEBUG_PATH, "Time of bot decision-making: "
                    + timeDecisionMaking + " ms");
            Log.f(DEBUG_PATH, "Time of all action: "
                    + (System.currentTimeMillis() - start) + "ms");
            Log.f(DEBUG_PATH, "============   End  ===========\n");
        }
    }

    public synchronized void resumeGame(final boolean activate) {
        Log.d("Robot " + this + " resumes game");
        paused = false;
        MouseClickLimiter.ban(limitedClickZone);
        chatTracker.resume();
        tableRecognizer.reset();
        executor.submit(new Runnable() {

            public void run() {
                tableInteractor.onCreate(activate);
                resumeGameInCurrentThread();
            }
        });
    }

    public void pauseGame() {
        Log.d("Robot " + this.toString() + " pauses game");
        paused = true;
        MouseClickLimiter.removeBan(limitedClickZone);
    }

    public void stopGame() {
        Log.d("Robot " + this.toString() + " stops the game");
        paused = true;
        MouseClickLimiter.removeBan(limitedClickZone);
        if (isGameStarted) {
            endSession();
        }
    }

    public synchronized boolean isPlaying() {
        return !paused;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + hashCode();
    }
}

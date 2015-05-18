/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.ps;

import mallorcatour.robot.ActionSynchronizer;
import mallorcatour.robot.ResultListener;
import mallorcatour.robot.controller.HUGameController;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.villainobserver.IVillainObserver;
import mallorcatour.core.game.LimitType;
import mallorcatour.bot.modeller.BaseVillainModeller;
import mallorcatour.util.DateUtils;
import mallorcatour.util.ExecutorUtils;
import mallorcatour.util.ImageUtils;
import mallorcatour.util.Log;
import mallorcatour.util.OnExceptionListener;
import mallorcatour.util.StringUtils;
import mallorcatour.util.ThreadUtils;
import mallorcatour.util.mp3player.Alarm;

/**
 *
 * @author Andrew
 */
public class PSTableDirector {

    private final static BufferedImage FINISHED_1 = ImageUtils.fromFile("assets/ps/finished1.png");
    private final static BufferedImage FINISHED_2 = ImageUtils.fromFile("assets/ps/finished2.png");
    private final static String DEBUG_BASE_PATH = "grandtorino_debug_";
    private final static String HOLDEM = "Hold'em";
    public final static String LOGGED_IN = "Logged In";
    private final static String TOURNAMENT = "Tournament";
    private final static String POKERSTARS = "PokerStars";
    private final static String LOBBY = "Lobby";
    private final static String NO_LIMIT = "No Limit";
    private final static String NL = "NL";
    private final static int PUSK_HEIGHT = 40;
    private final static int SCAN_TABLES_EVERY = 300;
    private final static int MAX_BOTTOM_MARGIN = 12;
    private final static int MAX_LEFT_MARGIN = 23;
    private final static int MAX_RIGHT_MARGIN = 25;
    private final static int MAX_TOP_MARGIN = 20;
    private final static int MAX_INTERSECTION_WIDTH = 23;
    private final static int MAX_INTERSECTION_HEIGHT = 12;
    private final static Runtime runtime = Runtime.getRuntime();
    private final boolean PLAY_MODE = true;
    private int tableCount;
    private int currentWindowIndex;
    private boolean allTableVisible;
    private volatile boolean isStopped = true;
    private boolean isPaused = true;
    private boolean isBlocked = false;
    private final Map<HWND, TableInfo> tables;
    private final Set<HWND> activatedTables;
    private final Dimension screenSize;
    private final User32 user32 = User32.INSTANCE;
    private final Object lock = new Object();
    private final ExecutorService executor;
    private IBotFactory FLbotFactory, NLbotFactory;
    private final ResultListener resultListener;

    private static class TableInfo {

        PSGameRobot robot;
        String windowText;
        Rectangle rectangle;
    }

    public PSTableDirector(Dimension screenSize) {
        tables = new HashMap<HWND, TableInfo>();
        this.screenSize = screenSize;
        activatedTables = new HashSet<HWND>();
        executor = ExecutorUtils.newSingleThreadExecutor(OnExceptionListener.EMPTY);
        resultListener = new ResultListener();
    }

    public void changeInteractor(boolean human) {
        for (TableInfo table : tables.values()) {
            table.robot.changeInteractor(human);
        }
    }

    private long memoryUsed() {
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public void setFLBotFactory(IBotFactory factory) {
        this.FLbotFactory = factory;
    }

    public void setNLBotFactory(IBotFactory factory) {
        this.NLbotFactory = factory;
    }

    public void start() {
        isPaused = false;
        isStopped = false;
        executor.submit(new Runnable() {

            public void run() {
                while (!isStopped) {
                    synchronized (lock) {
                        tableCount = 0;
                        currentWindowIndex = 0;
                        allTableVisible = true;
                        activatedTables.clear();
                        Log.d("Method called: enum windows");
                        ActionSynchronizer.beforeLogic();
                        user32.EnumWindows(new CreatingTablesCallback(), null);
                        deleteFinishedTables();
                        long start = System.currentTimeMillis();
                        Log.d("Method called: enum windows");
                        user32.EnumWindows(new ScaningTablesCallback(), null);
                        ActionSynchronizer.endOfLogic();
                        Log.d("Time of scanning tables enumWindows: "
                                + (System.currentTimeMillis() - start) + " ms");
                        if (allTableVisible) {
                            isBlocked = false;
                            Log.d("Tables are visible:)");
                            Alarm.stop();
                            if (!isPaused) {
                                resumeGame();
                            } else {
                                Log.d("Game is paused");
                            }
                        }
                    }
                    Log.d("Table count: " + tableCount);
                    ThreadUtils.sleep(SCAN_TABLES_EVERY);
                }
            }
        });
    }

    private class CreatingTablesCallback implements WNDENUMPROC {

        public boolean callback(HWND windowId, Pointer arg1) {
            char[] windowTextC = new char[512];
            user32.GetWindowText(windowId, windowTextC, 512);
            String windowText = Native.toString(windowTextC);
            if (windowText.contains(HOLDEM) && needRobot(windowText)) {
                if (!tables.containsKey(windowId)) {
                    String shortTableName = processTableName(windowText);
                    RECT rectangle = new RECT();
                    user32.GetWindowRect(windowId, rectangle);
                    Point topLeftPoint = new Point(rectangle.left, rectangle.top);
                    Dimension dimension = new Dimension(rectangle.right - rectangle.left,
                            rectangle.bottom - rectangle.top);
                    Rectangle newRectangle = new Rectangle(topLeftPoint, dimension);
                    addNewRobot(windowId, newRectangle, windowText, shortTableName);
                }
                activatedTables.add(windowId);
            }
            return true;
        }
    }

    private class ScaningTablesCallback implements WNDENUMPROC {

        public boolean callback(HWND windowId, Pointer arg1) {
            if (!user32.IsWindowVisible(windowId)) {
                return true;
            }
            RECT rectangle = new RECT();
            user32.GetWindowRect(windowId, rectangle);
            char[] windowTextC = new char[512];
            user32.GetWindowText(windowId, windowTextC, 512);

            String windowText = Native.toString(windowTextC);
            if (rectangle.left == -32000 && !windowText.contains(HOLDEM)) {
                return true;
            }
            if (windowText.isEmpty()) {
                return true;
            }
            Point topLeftPoint;
            Dimension dimension;
            if (!isFullScreen(rectangle)) {
                topLeftPoint = new Point(rectangle.left, rectangle.top);
                dimension = new Dimension(rectangle.right - rectangle.left,
                        rectangle.bottom - rectangle.top);
            } else {
                topLeftPoint = new Point(0, 0);
                dimension = new Dimension(rectangle.right - 8,
                        rectangle.bottom - 8);
            }
            Rectangle newRectangle = new Rectangle(topLeftPoint, dimension);

            if (windowText.equals(POKERSTARS)) {
                ThreadUtils.sleep(500);
                if (checkPSWindow(newRectangle)) {
                    Log.d("Finded info window about tournament end");
                }
            }
            if (rectangle.left != -32000) {
                checkIntersection(windowText, windowId, newRectangle);
            }
            if (windowText.contains(HOLDEM) && needRobot(windowText)) {
                Log.d(currentWindowIndex + " " + windowText + " " + rectangle);
                tableCount++;
                TableInfo current = tables.get(windowId);
                if (current == null) {
                    System.err.println("========= ERROR ===========");
                    System.err.println("Tables.get(windowId) == null");
                    System.err.println("Time: " + DateUtils.getDate(false));
                    System.err.println("Window text: " + windowText);
                    System.err.println("Window id: " + windowId);
                    System.err.println("Table count: " + tables.size());
                    System.err.println("Tables: " + tables.toString());
                    System.err.println("========= ERROR ===========");
                    return true;
                }
                current.rectangle = newRectangle;
                PSGameRobot robot = current.robot;

                if (rectangle.left == -32000) {
                    Log.d(windowText + " is minimized");
                    blockGame(robot);
                } else if (newRectangle.x < 0 - MAX_LEFT_MARGIN) {
                    Log.d(windowText + " is left off the screen");
                    blockGame(robot);
                } else if (newRectangle.y < 0 - MAX_TOP_MARGIN) {
                    Log.d(windowText + " is top off the screen");
                    blockGame(robot);
                } else if (newRectangle.x + newRectangle.width
                        > screenSize.width + MAX_RIGHT_MARGIN) {
                    Log.d(windowText + " is right off the screen");
                    blockGame(robot);
                } else if (newRectangle.y + newRectangle.height
                        > screenSize.height - PUSK_HEIGHT + MAX_BOTTOM_MARGIN) {
                    Log.d(windowText + " is bottom off the screen");
                    blockGame(robot);
                } else {
                    if (robot.isSupportedDimension(dimension)) {
                        robot.onTableVisible(topLeftPoint, dimension);
                    } else {
                        Log.d(windowText + " has unsupported dimension");
                        blockGame(robot);
                    }
                }
            }
            return true;
        }
    }

    private void checkIntersection(String windowText, HWND windowId,
            Rectangle newRectangle) {
        for (HWND tableWindowId : tables.keySet()) {
            TableInfo tableInfo = tables.get(tableWindowId);
            Rectangle2D intersection = newRectangle.createIntersection(tableInfo.rectangle);
            if (!tableWindowId.equals(windowId)
                    && intersection.getWidth() > MAX_INTERSECTION_WIDTH
                    && intersection.getHeight() > MAX_INTERSECTION_HEIGHT
                    && tableCount < tables.size()) {
                Log.d(windowText + " intersects with PS table " + tableInfo.windowText);
                Log.d("PS table rectangle: " + tableInfo.rectangle);
                Log.d("Intersecting window's rectangle: " + newRectangle);
                blockGame(tableInfo.robot);
                for (TableInfo table : tables.values()) {
                    table.robot.onTableInvisible();
                }
            }
        }
        currentWindowIndex++;
    }

    private boolean needRobot(String windowText) {
        return !PLAY_MODE || windowText.contains(LOGGED_IN);
    }

    private boolean checkPSWindow(Rectangle rectangle) {
        BufferedImage screenImage = ImageUtils.getScreenCapture(rectangle);
        if (ImageUtils.isPartOf(screenImage, FINISHED_1) != null) {
            resultListener.onTournamentWin();
            Log.d("CheckPSWindow. Finished on the first place");
            return true;
        } else if (ImageUtils.isPartOf(screenImage, FINISHED_2) != null) {
            resultListener.onTournamentWin();
            Log.d("CheckPSWindow. Finished on the second place");
            return true;
        } else {
            return false;
        }
    }

    private boolean isFullScreen(RECT rectangle) {
        boolean result = (rectangle.left == -8
                && rectangle.top == -8
                && rectangle.right == screenSize.width + 8
                && rectangle.bottom == screenSize.height - PUSK_HEIGHT + 8);
        return result;
    }

    private String processTableName(String tableName) {
        String result = null;
        if (tableName.contains(TOURNAMENT)) {
            int tournamentNumber = Integer.parseInt(StringUtils.between(tableName, TOURNAMENT + " ", " "));
            result = TOURNAMENT + "_" + tournamentNumber;
        } else {
            result = StringUtils.between(tableName, 0, " -");
            result = result.replaceAll(" ", "_");
        }
        return result;
    }

    private LimitType parseLimitType(String tableName) {
        LimitType limitType;
        if (tableName.contains(NO_LIMIT) || tableName.contains(NL)) {
            limitType = LimitType.NO_LIMIT;
        } else {
            limitType = LimitType.FIXED_LIMIT;
        }
        return limitType;
    }

    private IPlayer createPlayer(BaseVillainModeller villainModeller,
            LimitType limitType, String debug) {
        IPlayer player;
        if (limitType == LimitType.FIXED_LIMIT) {
            player = FLbotFactory.createBot(villainModeller,
                    ISpectrumListener.EMPTY, IDecisionListener.EMPTY, debug);
        } else if (limitType == LimitType.NO_LIMIT) {
            player = NLbotFactory.createBot(villainModeller,
                    ISpectrumListener.EMPTY, IDecisionListener.EMPTY, debug);
        } else {
            throw new RuntimeException();
        }
        return player;
    }

    private void addNewRobot(HWND windowId, Rectangle rectangle,
            String fullTableName, String shortTableName) {
        long start = System.currentTimeMillis();
        long startMemory = memoryUsed();
        LimitType limitType = parseLimitType(fullTableName);
        Log.d("Creating player for " + limitType + " table " + fullTableName);
        String debug = DEBUG_BASE_PATH + DateUtils.getDate(false) + "_"
                + shortTableName + ".txt";
        BaseVillainModeller villainModeller = new BaseVillainModeller(limitType, debug);
        IPlayer player = createPlayer(villainModeller, limitType, debug);
        String heroName = StringUtils.between(fullTableName, LOGGED_IN + " as ", "\n");
        IVillainObserver villainObserver = new PSVillainObserver(villainModeller,
                limitType, fullTableName, heroName, debug);
        HUGameController controller = new HUGameController(player, heroName, debug);
        final PSGameRobot robot = new PSGameRobot(controller, limitType, debug,
                heroName, villainObserver);
        TableInfo tableInfo = new TableInfo();
        tableInfo.robot = robot;
        tableInfo.windowText = fullTableName;
        tableInfo.rectangle = rectangle;

        tables.put(windowId, tableInfo);
        if (!isPaused && !isBlocked) {
            robot.resumeGame(true);
        }
        Log.d("Memory used before creating the robot: " + startMemory + " B");
        Log.d("Memory used after creating the robot: " + memoryUsed() + " B");
        Log.d("Time of creating the robot: " + (System.currentTimeMillis() - start) + " ms");
    }

    private void deleteFinishedTables() {
        Set<HWND> forDelete = new HashSet<HWND>();
        for (Entry<HWND, TableInfo> entry : tables.entrySet()) {
            if (!activatedTables.contains(entry.getKey())) {
                entry.getValue().robot.stopGame();
                Log.d("Stopping game from delete nonused robots");
                forDelete.add(entry.getKey());
            }
        }
        if (!forDelete.isEmpty()) {
            for (HWND key : forDelete) {
                tables.remove(key);
            }
            int size = forDelete.size();
            Log.d("There was deleted " + size + " robot" + (size > 1 ? "s." : "."));
            Log.d("Memory used after delete robot: " + memoryUsed() + " B");
        }
    }

    private void blockGame(PSGameRobot robot) {
        Log.d("Game is blocked for " + tables.size() + " tables!");
        allTableVisible = false;
        if (!isPaused) {
            Alarm.alarm();
        }
        isBlocked = true;
        for (TableInfo table : tables.values()) {
            if (table.robot.isPlaying()) {
                table.robot.pauseGame();
            }
        }
        robot.onTableInvisible();
    }

    public void pauseGame() {
        synchronized (lock) {
            isPaused = true;
            Alarm.stop();
            for (TableInfo table : tables.values()) {
                if (table.robot.isPlaying()) {
                    table.robot.pauseGame();
                }
            }
            Log.d("Game is paused on " + tables.size() + " tables by user.");
        }
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void resumeGame() {
        isPaused = false;
        for (TableInfo table : tables.values()) {
            if (!table.robot.isPlaying()) {
                table.robot.resumeGame(false);
            }
        }
        Log.d("Game is resumed on " + tables.size() + " tables.");
    }

    public void stop() {
        synchronized (lock) {
            isStopped = true;
            isPaused = true;
            for (TableInfo table : tables.values()) {
                if (table.robot.isPlaying()) {
                    table.robot.pauseGame();
                }
            }
            tables.clear();
        }
    }
}

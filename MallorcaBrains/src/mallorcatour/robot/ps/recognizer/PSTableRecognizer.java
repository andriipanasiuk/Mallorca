/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.ps.recognizer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

import mallorcatour.core.game.Card;
import mallorcatour.core.game.HoleCards;
import mallorcatour.robot.recognizer.ITableListener;
import mallorcatour.robot.recognizer.assets.DefaultBetDigitAssets;
import mallorcatour.robot.recognizer.assets.DefaultCardAssets;
import mallorcatour.robot.recognizer.assets.DefaultHandnumberDigitAssets;
import mallorcatour.robot.recognizer.assets.DefaultPotDigitAssets;
import mallorcatour.robot.recognizer.assets.DefaultStackDigitAssets;
import mallorcatour.robot.recognizer.assets.DefaultTableAssets;
import mallorcatour.robot.recognizer.assets.ITableAssets;
import mallorcatour.robot.recognizer.assets.MinSizeBetDigitAssets;
import mallorcatour.robot.recognizer.assets.MinSizeCardAssets;
import mallorcatour.robot.recognizer.assets.MinSizeHandnumberDigitAssets;
import mallorcatour.robot.recognizer.assets.MinSizePotDigitAssets;
import mallorcatour.robot.recognizer.assets.MinSizeStackDigitAssets;
import mallorcatour.robot.recognizer.assets.MinSizeTableAssets;
import mallorcatour.util.DateUtils;
import mallorcatour.util.robot.ImageUtils;
import mallorcatour.util.robot.RecognizerUtils;
import mallorcatour.util.robot.ImageUtils.SearchedImage;
import mallorcatour.util.Log;

/**
 *
 * @author Andrew
 */
public class PSTableRecognizer implements ITableListener {

    public final static Dimension DEFAULT_TABLE_SIZE = new Dimension(808, 584);
    public final static Dimension MIN_TABLE_SIZE = new Dimension(491, 365);
    private final static int TOP_LEFT_MARGIN_X = 7;
    private final static int TOP_LEFT_MARGIN_Y = 29;
    private final static int WIDTH_MARGIN = 15;
    private final static int HEIGHT_MARGIN = 37;
    private final static int MIN_TABLE_WIDTH_TO = 534;
    private final static int DEFAULT_TABLE_WIDTH_TO = 882;
    private final static int DEFAULT_TABLE_WIDTH_FROM = 745;
    private final static String TOP_LEFT_PATH = "assets/ps/top_left.png";
    private final static int MIN_SIZE = 0;
    private final static int DEFAULT_SIZE = 3;
    //stack rectangles
    private Rectangle VILLAIN_STACK_RECTANGLE;
    private Rectangle HERO_STACK_RECTANGLE;
    //bet rectangles
    private Rectangle VILLAIN_BET_RECTANGLE;
    private Rectangle HERO_BET_RECTANGLE;
    //pot rectangle
    private Rectangle POT_RECTANGLE;
    //hand number rectangles
    private Rectangle HAND_NUMBER_RECTANGLE;
    //button rectangle
    private Rectangle HERO_DEALER_RECTANGLE;
    //cards rectangles
    private Rectangle HOLE_CARDS_RECTANGLE;
    private Rectangle BOARD_CARDS_RECTANGLE;
    //action buttons' rectanges
    private Rectangle FOLD_BUTTON_RECTANGLE;
    private Rectangle PASSIVE_BUTTON_RECTANGLE;
    private Rectangle AGGRESSIVE_BUTTON_RECTANGLE;
    private Rectangle ACTION_BUTTONs_RECTANGLE;
    //
    private Point END_TOURNAMENT_BUTTON_POINT;
    private Rectangle ACTIVATE_RECTANGLE;
    private Rectangle VILLAIN_EMPTY_SEAT_RECTANGLE;
    private Rectangle CHAT_RECTANGLE;
    //is in focus rectangle
    private final static Rectangle IS_IN_FOCUS_RECTANGLE =
            new Rectangle(200, 0, 5, 5);
    private Point topLeftPoint;
    private Dimension tableDimension;
    //recognizers
    private PSChatRecognizer chatRecognizer;
    private PSCardRecognizer cardRecognizer;
    private PSPotRecognizer potRecognizer;
    private PSStackRecognizer stackRecognizer;
    private PSBetRecognizer betRecognizer;
    private PSButtonRecognizer buttonRecognizer;
    private PSHandNumberRecognizer handNumberRecognizer;
    private PSActionButtonRecognizer actionButtonRecognizer;
    private BufferedImage screenCapture;
    private boolean isTableRecognized = false;
    private final Object lock = new Object();
    private ITableAssets tableAssets;
    private int currentSize = 3;
    private final SearchedImage emptySeatImage;

    public PSTableRecognizer() {
        tableAssets = new DefaultTableAssets();
        cardRecognizer = new PSCardRecognizer(new DefaultCardAssets());
        potRecognizer = new PSPotRecognizer(new DefaultPotDigitAssets());
        stackRecognizer = new PSStackRecognizer(new DefaultStackDigitAssets(), tableAssets);
        betRecognizer = new PSBetRecognizer(new DefaultBetDigitAssets());
        buttonRecognizer = new PSButtonRecognizer(tableAssets);
        handNumberRecognizer = new PSHandNumberRecognizer(new DefaultHandnumberDigitAssets());
        chatRecognizer = new PSChatRecognizer();
        actionButtonRecognizer = new PSActionButtonRecognizer();
        emptySeatImage = new SearchedImage() {

            public BufferedImage getSearchedImage() {
                return tableAssets.getEmptySeatImage();
            }

            public Rectangle getRectangle() {
                return RecognizerUtils.getGlobalRectangle(VILLAIN_EMPTY_SEAT_RECTANGLE, topLeftPoint);
            }
        };
    }

    public Point getTopLeftPosition() {
        checkVisibility();
        return topLeftPoint;
    }

    public Dimension getDimension() {
        checkVisibility();
        return tableDimension;
    }

    private void lock() {
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void unlock() {
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void reset() {
        isTableRecognized = false;
        if (screenCapture != null) {
            screenCapture.flush();
        }
    }

    public void scrapTable() {
        if (screenCapture != null) {
            screenCapture.flush();
        }
        screenCapture = ImageUtils.getScreenCapture(new Rectangle(topLeftPoint, tableDimension));
    }

    @Deprecated
    public void scrapTable(String path) {
        if (screenCapture != null) {
            screenCapture.flush();
        }
        screenCapture = ImageUtils.fromFile(path);
        Point point =
                ImageUtils.isPartOf(screenCapture, ImageUtils.fromFile(TOP_LEFT_PATH));
        if (point == null) {
            throw new RuntimeException();
        }
        point.translate(-13, -12);
        onTableVisible(point, MIN_TABLE_SIZE);
    }

    public boolean isSupportedDimension(Dimension dimension) {
        if (dimension.width > DEFAULT_TABLE_WIDTH_TO) {
            return false;
        } else if (dimension.width > MIN_TABLE_WIDTH_TO
                && dimension.width < DEFAULT_TABLE_WIDTH_FROM) {
            return false;
        }
        return true;
    }

    public Point getEndTournamentButton() {
        checkVisibility();
        return END_TOURNAMENT_BUTTON_POINT;
    }

    public Rectangle getActivateRectangle() {
        checkVisibility();
        return ACTIVATE_RECTANGLE;
    }

    public Rectangle getFoldButtonRectangle() {
        checkVisibility();
        return FOLD_BUTTON_RECTANGLE;
    }

    public Rectangle getPassiveButtonRectangle() {
        checkVisibility();
        return PASSIVE_BUTTON_RECTANGLE;
    }

    public Rectangle getAggressiveButtonRectangle() {
        checkVisibility();
        return AGGRESSIVE_BUTTON_RECTANGLE;
    }

    public Rectangle getActionButtonsRectangle() {
        checkVisibility();
        return ACTION_BUTTONs_RECTANGLE;
    }

    private void checkVisibility() {
        if (!isTableRecognized) {
            lock();
        }
    }

    public HoleCards getHoleCards() {
        checkVisibility();
        BufferedImage cardsImage =
                ImageUtils.getSubimage(screenCapture, HOLE_CARDS_RECTANGLE);
        List<Card> result = cardRecognizer.getCards(cardsImage, 2);
        if (result.size() != 2) {
            ImageUtils.toFile(cardsImage, "error_hole_cards_" + DateUtils.getDate(true) + ".png", false);
            ImageUtils.toFile(ImageUtils.getScreenCapture(), "screen_error_hole_cards_"
                    + DateUtils.getDate(true) + ".png", false);
            throw new RuntimeException("Must be 2 hole cards, but we have "
                    + result.size());
        }
        return new HoleCards(result.get(0), result.get(1));
    }

    public List<Card> getBoardCards() {
        checkVisibility();
        BufferedImage boardImage = ImageUtils.getSubimage(screenCapture, BOARD_CARDS_RECTANGLE);
        int cardsCount = cardRecognizer.getCardsCount(boardImage);
        return cardRecognizer.getCards(boardImage, cardsCount);
    }

    public int getCardsCount() {
        checkVisibility();
        return cardRecognizer.getCardsCount(
                ImageUtils.getSubimage(screenCapture, BOARD_CARDS_RECTANGLE));
    }

    public boolean isHeroOnButton() {
        checkVisibility();
        return buttonRecognizer.isButton(ImageUtils.getSubimage(screenCapture, HERO_DEALER_RECTANGLE));
    }

    public long getHandNumber() {
        checkVisibility();
        return handNumberRecognizer.getHandNumber(ImageUtils.getSubimage(screenCapture, HAND_NUMBER_RECTANGLE));
    }

    public double getVillainBet() {
        checkVisibility();
        return betRecognizer.getBet(ImageUtils.getSubimage(screenCapture, VILLAIN_BET_RECTANGLE));
    }

    public double getHeroBet() {
        checkVisibility();
        return betRecognizer.getBet(ImageUtils.getSubimage(screenCapture, HERO_BET_RECTANGLE));
    }

    public double getVillainStack() {
        checkVisibility();
        int stack = stackRecognizer.getStack(ImageUtils.getSubimage(screenCapture, VILLAIN_STACK_RECTANGLE));
        return stack;
    }

    public double getHeroStack() {
        checkVisibility();
        return stackRecognizer.getStack(ImageUtils.getSubimage(screenCapture, HERO_STACK_RECTANGLE));
    }

    public double getPot() {
        checkVisibility();
        return potRecognizer.getPot(ImageUtils.getSubimage(screenCapture, POT_RECTANGLE));
    }

    public boolean isInFocus() {
        checkVisibility();
        Log.d("Method called: isInFocus");
        Rectangle rect = RecognizerUtils.getGlobalRectangle(IS_IN_FOCUS_RECTANGLE, topLeftPoint);
        BufferedImage image = ImageUtils.getScreenCapture(rect);
        Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        Rectangle tableRectangle = new Rectangle(topLeftPoint, tableDimension);
        boolean isMouseOnTable = tableRectangle.contains(mouseLocation);
        Log.d("isMouseOnTable: " + isMouseOnTable);
        boolean isGreenFrame = new Color(image.getRGB(2, 2)).getGreen() > 180;
        Log.d("isGreenFrame: " + isGreenFrame);
        return isMouseOnTable && isGreenFrame;
    }

    public boolean checkChat() {
        checkVisibility();
        Log.d("Method called: checkChat");
        BufferedImage chatImage = ImageUtils.getScreenCapture(
                RecognizerUtils.getGlobalRectangle(CHAT_RECTANGLE, topLeftPoint));
        ImageUtils.toFile(chatImage, "chat_" + DateUtils.getDate(false) + ".png", true);
        return chatRecognizer.checkChat(chatImage);
    }

    public boolean isPassiveButton() {
        checkVisibility();
        Log.d("Method called: isPassiveButton");
        return actionButtonRecognizer.isButton(
                ImageUtils.getSubimage(screenCapture, PASSIVE_BUTTON_RECTANGLE));
    }

    public boolean isAggressiveButton() {
        checkVisibility();
        Log.d("Method called: isAggressiveButton");
        return actionButtonRecognizer.isButton(
                ImageUtils.getSubimage(screenCapture, AGGRESSIVE_BUTTON_RECTANGLE));
    }

    public boolean isFoldButton() {
        checkVisibility();
        Log.d("Method called: isFoldButton");
        Rectangle globalFoldButtonRectangle =
                RecognizerUtils.getGlobalRectangle(FOLD_BUTTON_RECTANGLE, topLeftPoint);
        BufferedImage screenImage = ImageUtils.getScreenCapture(globalFoldButtonRectangle);
        boolean result = actionButtonRecognizer.isButton(screenImage);
        return result;
    }

    public boolean isEmptySeat() {
        checkVisibility();
        Log.d("Method called: isEmptySeat");
        Point point = ImageUtils.isOnScreen(emptySeatImage.getSearchedImage(),
                emptySeatImage.getRectangle());
        return point != null;
    }

    //coordinates are raw
    public void onTableVisible(Point point, Dimension dimension) {
        Log.d("PSTableRecognizer.onTableVisible() start");
        boolean sizeChanged = false;
        if (this.topLeftPoint == null
                || !(point.x + TOP_LEFT_MARGIN_X == this.topLeftPoint.x
                && point.y + TOP_LEFT_MARGIN_Y == this.topLeftPoint.y)) {
            this.topLeftPoint = new Point(point.x + TOP_LEFT_MARGIN_X,
                    point.y + TOP_LEFT_MARGIN_Y);
            Log.d("Top left changes: " + topLeftPoint);
            sizeChanged = true;
        }
        if (this.tableDimension == null
                || !(dimension.width - WIDTH_MARGIN == tableDimension.width
                && dimension.height - HEIGHT_MARGIN == tableDimension.height)) {
            this.tableDimension = new Dimension(dimension.width - WIDTH_MARGIN,
                    dimension.height - HEIGHT_MARGIN);
            changeDimensions();
            Log.d("Table dimension changes: " + tableDimension);
            int newSize = getCurrentSize(dimension);
            if (currentSize != newSize) {
                Log.d("Table size changes: " + newSize);
                changeAssets(newSize);
                currentSize = newSize;
            }
            sizeChanged = true;
        }
        isTableRecognized = true;
        Log.d("PSTableRecognizer.onTableVisible() before unlock");
        unlock();
        Log.d("PSTableRecognizer.onTableVisible() end");
    }

    private void changeDimensions() {
        VILLAIN_STACK_RECTANGLE = PSRectangles.getVillainStackRectangle(tableDimension);
        HERO_STACK_RECTANGLE = PSRectangles.getHeroStackRectangle(tableDimension);
        VILLAIN_BET_RECTANGLE = PSRectangles.getVillainBetRectangle(tableDimension);
        HERO_BET_RECTANGLE = PSRectangles.getHeroBetRectangle(tableDimension);
        POT_RECTANGLE = PSRectangles.getPotRectangle(tableDimension);
        HAND_NUMBER_RECTANGLE = PSRectangles.getHandNumberRectangle(tableDimension);
        HERO_DEALER_RECTANGLE = PSRectangles.getHeroDealerRectangle(tableDimension);
        HOLE_CARDS_RECTANGLE = PSRectangles.getHoleCardsRectangle(tableDimension);
        BOARD_CARDS_RECTANGLE = PSRectangles.getBoardCardsRectangle(tableDimension);
        FOLD_BUTTON_RECTANGLE = PSRectangles.getFoldButtonRectangle(tableDimension);
        PASSIVE_BUTTON_RECTANGLE = PSRectangles.getPassiveButtonRectangle(tableDimension);
        AGGRESSIVE_BUTTON_RECTANGLE = PSRectangles.getAggressiveButtonRectangle(tableDimension);
        ACTION_BUTTONs_RECTANGLE = PSRectangles.getActionButtonsRectangle(tableDimension);
        END_TOURNAMENT_BUTTON_POINT = PSRectangles.getEndTournamentPoint(tableDimension);
        ACTIVATE_RECTANGLE = PSRectangles.getActivateRectangle(tableDimension);
        VILLAIN_EMPTY_SEAT_RECTANGLE = PSRectangles.getVillainEmptySeatRectangle(tableDimension);
        CHAT_RECTANGLE = PSRectangles.getChatRectangle(tableDimension);
    }

    private void changeAssets(int size) {
        if (size == MIN_SIZE) {
            tableAssets = new MinSizeTableAssets();
            cardRecognizer.changeAssets(new MinSizeCardAssets());
            potRecognizer.changeAssets(new MinSizePotDigitAssets());
            stackRecognizer.changeAssets(new MinSizeStackDigitAssets());
            stackRecognizer.changeTableAssets(tableAssets);
            betRecognizer.changeAssets(new MinSizeBetDigitAssets());
            buttonRecognizer.changeAssets(tableAssets);
            handNumberRecognizer.changeAssets(new MinSizeHandnumberDigitAssets());
        } else if (size == DEFAULT_SIZE) {
            tableAssets = new DefaultTableAssets();
            cardRecognizer.changeAssets(new DefaultCardAssets());
            potRecognizer.changeAssets(new DefaultPotDigitAssets());
            stackRecognizer.changeAssets(new DefaultStackDigitAssets());
            stackRecognizer.changeTableAssets(tableAssets);
            betRecognizer.changeAssets(new DefaultBetDigitAssets());
            buttonRecognizer.changeAssets(tableAssets);
            handNumberRecognizer.changeAssets(new DefaultHandnumberDigitAssets());
        } else {
            throw new IllegalArgumentException();
        }
    }

    private int getCurrentSize(Dimension dimension) {
        if (!isSupportedDimension(dimension)) {
            throw new IllegalArgumentException("Unsupported dimension: " + dimension);
        }
        if (dimension.width <= MIN_TABLE_WIDTH_TO) {
            return MIN_SIZE;
        } else if (dimension.width >= DEFAULT_TABLE_WIDTH_FROM
                && dimension.width <= DEFAULT_TABLE_WIDTH_TO) {
            return DEFAULT_SIZE;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void onTableInvisible() {
        isTableRecognized = false;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.ps.recognizer;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

/**
 *
 * @author Andrew
 */
public final class PSRectangles {

    public final static Rectangle.Double VILLAIN_STACK_RECTANGLE =
            new Rectangle.Double(0.045, 0.5, 0.116, 16);
    public final static Rectangle.Double HERO_STACK_RECTANGLE =
            new Rectangle.Double(0.845, 0.5, 0.116, 16);
    //bet rectangles
    public final static Rectangle.Double VILLAIN_BET_RECTANGLE =
            new Rectangle.Double(0.16, 0.41, 129, 31);
    public final static Rectangle.Double HERO_BET_RECTANGLE =
            new Rectangle.Double(0.615, 0.41, 175, 31);
    //pot rectangle
    public final static Rectangle.Double POT_RECTANGLE =
            new Rectangle.Double(0.485, 0.014, 67, 21);
    //hand number rectangles
    public final static Rectangle.Double HAND_NUMBER_RECTANGLE =
            new Rectangle.Double(0.04, 0.009, 108, 15);
    //button rectangle
    public final static Rectangle.Double HERO_DEALER_RECTANGLE =
            new Rectangle.Double(0.8, 0.47, 32, 27);
    //cards rectangles
    public final static Rectangle.Double HOLE_CARDS_RECTANGLE =
            new Rectangle.Double(0.86, 0.19, 0.13, 0.13);
    public final static Rectangle.Double BOARD_CARDS_RECTANGLE =
            new Rectangle.Double(0.336, 0.28, 0.345, 0.135);
    public final static Rectangle.Double FOLD_BUTTON_RECTANGLE =
            new Rectangle.Double(0.5245, 0.911, 0.124, 0.064);
    public final static Rectangle.Double PASSIVE_BUTTON_RECTANGLE =
            new Rectangle.Double(0.688, 0.911, 0.124, 0.064);
    public final static Rectangle.Double AGGRESSIVE_BUTTON_RECTANGLE =
            new Rectangle.Double(0.8575, 0.911, 0.124, 0.064);
    public final static Rectangle.Double ACTION_BUTTONs_RECTANGLE =
            new Rectangle.Double(0.506, 0.75, 0.494, 0.25);
    public final static Rectangle.Double ACTIVATE_RECTANGLE =
            new Rectangle.Double(0.21, 0.54, 0.55, 0.17);
    public final static Rectangle.Double VILLAIN_EMPTY_SEAT_RECTANGLE =
            new Rectangle.Double(0.046, 0.317, 0.103, 0.15);
    public final static Point.Double END_TOURNAMENT_POINT =
            new Point.Double(0.5, 0.58);
    public final static Rectangle.Double END_TOURNAMENT_RECTANGLE =
            new Rectangle.Double(0.34, 0.42, 0.216, 0.137);
    public final static Rectangle.Double CHAT_RECTANGLE =
            new Rectangle.Double(11, 0.786, 0.493, 0.207);

    public static Rectangle getVillainStackRectangle(Dimension tableDimension) {
        return new Rectangle((int) (VILLAIN_STACK_RECTANGLE.x * tableDimension.width),
                (int) (VILLAIN_STACK_RECTANGLE.y * tableDimension.height),
                (int) (VILLAIN_STACK_RECTANGLE.width * tableDimension.width),
                (int) (VILLAIN_STACK_RECTANGLE.height));
    }

    public static Rectangle getHeroStackRectangle(Dimension tableDimension) {
        return new Rectangle((int) (HERO_STACK_RECTANGLE.x * tableDimension.width),
                (int) (HERO_STACK_RECTANGLE.y * tableDimension.height),
                (int) (HERO_STACK_RECTANGLE.width * tableDimension.width),
                (int) (HERO_STACK_RECTANGLE.height));
    }

    public static Rectangle getVillainBetRectangle(Dimension tableDimension) {
        return new Rectangle((int) (VILLAIN_BET_RECTANGLE.x * tableDimension.width),
                (int) (VILLAIN_BET_RECTANGLE.y * tableDimension.height),
                (int) (VILLAIN_BET_RECTANGLE.width),
                (int) VILLAIN_BET_RECTANGLE.height);
    }

    public static Rectangle getHeroBetRectangle(Dimension tableDimension) {
        return new Rectangle((int) (HERO_BET_RECTANGLE.x * tableDimension.width),
                (int) (HERO_BET_RECTANGLE.y * tableDimension.height),
                (int) (HERO_BET_RECTANGLE.width),
                (int) (HERO_BET_RECTANGLE.height));
    }

    public static Rectangle getPotRectangle(Dimension tableDimension) {
        return new Rectangle((int) (POT_RECTANGLE.x * tableDimension.width),
                (int) (POT_RECTANGLE.y * tableDimension.height),
                (int) (POT_RECTANGLE.width),
                (int) (POT_RECTANGLE.height));
    }

    public static Rectangle getHandNumberRectangle(Dimension tableDimension) {
        return new Rectangle((int) (HAND_NUMBER_RECTANGLE.x * tableDimension.width),
                (int) (HAND_NUMBER_RECTANGLE.y * tableDimension.height),
                (int) (HAND_NUMBER_RECTANGLE.width),
                (int) (HAND_NUMBER_RECTANGLE.height));
    }

    public static Rectangle getHeroDealerRectangle(Dimension tableDimension) {
        return new Rectangle((int) (HERO_DEALER_RECTANGLE.x * tableDimension.width),
                (int) (HERO_DEALER_RECTANGLE.y * tableDimension.height),
                (int) (HERO_DEALER_RECTANGLE.width),
                (int) (HERO_DEALER_RECTANGLE.height));
    }

    public static Rectangle getHoleCardsRectangle(Dimension tableDimension) {
        return new Rectangle((int) (HOLE_CARDS_RECTANGLE.x * tableDimension.width),
                (int) (HOLE_CARDS_RECTANGLE.y * tableDimension.height),
                (int) (HOLE_CARDS_RECTANGLE.width * tableDimension.width),
                (int) (HOLE_CARDS_RECTANGLE.height * tableDimension.height));
    }

    public static Rectangle getBoardCardsRectangle(Dimension tableDimension) {
        return new Rectangle((int) (BOARD_CARDS_RECTANGLE.x * tableDimension.width),
                (int) (BOARD_CARDS_RECTANGLE.y * tableDimension.height),
                (int) (BOARD_CARDS_RECTANGLE.width * tableDimension.width) + 1,
                (int) (BOARD_CARDS_RECTANGLE.height * tableDimension.height) + 1);
    }

    public static Rectangle getFoldButtonRectangle(Dimension tableDimension) {
        return new Rectangle((int) (FOLD_BUTTON_RECTANGLE.x * tableDimension.width),
                (int) (FOLD_BUTTON_RECTANGLE.y * tableDimension.height),
                (int) (FOLD_BUTTON_RECTANGLE.width * tableDimension.width),
                (int) (FOLD_BUTTON_RECTANGLE.height * tableDimension.height));
    }

    public static Rectangle getPassiveButtonRectangle(Dimension tableDimension) {
        return new Rectangle((int) (PASSIVE_BUTTON_RECTANGLE.x * tableDimension.width),
                (int) (PASSIVE_BUTTON_RECTANGLE.y * tableDimension.height),
                (int) (PASSIVE_BUTTON_RECTANGLE.width * tableDimension.width),
                (int) (PASSIVE_BUTTON_RECTANGLE.height * tableDimension.height));
    }

    public static Rectangle getAggressiveButtonRectangle(Dimension tableDimension) {
        return new Rectangle((int) (AGGRESSIVE_BUTTON_RECTANGLE.x * tableDimension.width),
                (int) (AGGRESSIVE_BUTTON_RECTANGLE.y * tableDimension.height),
                (int) (AGGRESSIVE_BUTTON_RECTANGLE.width * tableDimension.width),
                (int) (AGGRESSIVE_BUTTON_RECTANGLE.height * tableDimension.height));
    }

    public static Rectangle getActionButtonsRectangle(Dimension tableDimension) {
        return new Rectangle((int) (ACTION_BUTTONs_RECTANGLE.x * tableDimension.width),
                (int) (ACTION_BUTTONs_RECTANGLE.y * tableDimension.height),
                (int) (ACTION_BUTTONs_RECTANGLE.width * tableDimension.width),
                (int) (ACTION_BUTTONs_RECTANGLE.height * tableDimension.height));
    }

    public static Point getEndTournamentPoint(Dimension tableDimension) {
        return new Point((int) (END_TOURNAMENT_POINT.x * tableDimension.width),
                (int) (END_TOURNAMENT_RECTANGLE.y * tableDimension.height));
    }

    public static Rectangle getActivateRectangle(Dimension tableDimension) {
        return new Rectangle((int) (ACTIVATE_RECTANGLE.x * tableDimension.width),
                (int) (ACTIVATE_RECTANGLE.y * tableDimension.height),
                (int) (ACTIVATE_RECTANGLE.width * tableDimension.width),
                (int) (ACTIVATE_RECTANGLE.height * tableDimension.height));
    }

    public static Rectangle getVillainEmptySeatRectangle(Dimension tableDimension) {
        return new Rectangle((int) (VILLAIN_EMPTY_SEAT_RECTANGLE.x * tableDimension.width),
                (int) (VILLAIN_EMPTY_SEAT_RECTANGLE.y * tableDimension.height),
                (int) (VILLAIN_EMPTY_SEAT_RECTANGLE.width * tableDimension.width),
                (int) (VILLAIN_EMPTY_SEAT_RECTANGLE.height * tableDimension.height));
    }

    public static Rectangle getEndTournamentRectangle(Dimension tableDimension) {
        return new Rectangle((int) (END_TOURNAMENT_RECTANGLE.x * tableDimension.width),
                (int) (END_TOURNAMENT_RECTANGLE.y * tableDimension.height),
                (int) (END_TOURNAMENT_RECTANGLE.width * tableDimension.width),
                (int) (END_TOURNAMENT_RECTANGLE.height * tableDimension.height));
    }

    public static Rectangle getChatRectangle(Dimension tableDimension) {
        return new Rectangle((int) (CHAT_RECTANGLE.x),
                (int) (CHAT_RECTANGLE.y * tableDimension.height) + 30,
                (int) (CHAT_RECTANGLE.width * tableDimension.width) - 30,
                (int) (CHAT_RECTANGLE.height * tableDimension.height) - 36);
    }
}

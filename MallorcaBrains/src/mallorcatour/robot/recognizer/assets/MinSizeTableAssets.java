/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.recognizer.assets;

import java.awt.image.BufferedImage;
import mallorcatour.util.ImageUtils;

/**
 *
 * @author Andrew
 */
public class MinSizeTableAssets implements ITableAssets {

    private static final String SIT_OUT_PATH = "assets/ps/1/sit_out.png";
    private static final BufferedImage SIT_OUT_IMAGE;
    private static final String BUTTON_PATH = "assets/ps/1/button.png";
    private static final BufferedImage BUTTON_IMAGE;
    private static final String FOLD_BUTTON_PATH = "assets/ps/1/fold_button.png";
    private static final BufferedImage FOLD_BUTTON_IMAGE;
    private static final String EMPTY_SEAT_PATH = "assets/ps/1/empty_seat.png";
    private static final BufferedImage EMPTY_SEAT_IMAGE;
    private static final String END_TOURNAMENT_PATH = "assets/ps/1/end_tournament.png";
    private static final BufferedImage END_TOURNAMENT_IMAGE;

    static {
        SIT_OUT_IMAGE = ImageUtils.fromFile(SIT_OUT_PATH);
        BUTTON_IMAGE = ImageUtils.fromFile(BUTTON_PATH);
        FOLD_BUTTON_IMAGE = ImageUtils.fromFile(FOLD_BUTTON_PATH);
        EMPTY_SEAT_IMAGE = ImageUtils.fromFile(EMPTY_SEAT_PATH);
        END_TOURNAMENT_IMAGE = ImageUtils.fromFile(END_TOURNAMENT_PATH);
    }

    public BufferedImage getSitoutImage() {
        return SIT_OUT_IMAGE;
    }

    public BufferedImage getFoldButtonImage() {
        return FOLD_BUTTON_IMAGE;
    }

    public BufferedImage getButtonImage() {
        return BUTTON_IMAGE;
    }

    public BufferedImage getEmptySeatImage() {
        return EMPTY_SEAT_IMAGE;
    }

    public BufferedImage getEndTournamentImage() {
        return END_TOURNAMENT_IMAGE;
    }
}

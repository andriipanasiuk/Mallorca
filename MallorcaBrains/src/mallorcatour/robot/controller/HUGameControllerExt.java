/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.controller;

import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.core.game.Action;

/**
 *
 * @author Andrew
 */
public class HUGameControllerExt extends HUGameController {

    public HUGameControllerExt(IPlayer player, String heroName, String DEBUG_PATH) {
        super(player, heroName, DEBUG_PATH);
    }

    public void onVillainActed(Action action, double toCall) {
        player.onVillainActed(action, toCall);
    }
}

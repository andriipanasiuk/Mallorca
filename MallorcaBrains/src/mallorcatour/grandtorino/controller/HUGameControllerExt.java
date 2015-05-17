/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.controller;

import mallorcatour.core.bot.IPlayer;
import mallorcatour.game.core.Action;

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

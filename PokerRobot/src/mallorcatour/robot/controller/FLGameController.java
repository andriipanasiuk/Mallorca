package mallorcatour.robot.controller;

import mallorcatour.core.game.Action;
import mallorcatour.core.player.interfaces.Player;

public class FLGameController extends HUGameController<FLGameInfo> {

    public FLGameController(Player player, String heroName, String DEBUG_PATH) {
        super(player, heroName, DEBUG_PATH);
    }

    @Override
    protected void onHeroActed(Action action) {
        super.onHeroActed(action);
        if (action.isAggressive()) {
            gameInfo.raisesOnStreet[currentStreet.intValue()]++;
        }
    }
}

package mallorcatour.robot.controller;

import mallorcatour.core.game.GameInfo;
import mallorcatour.robot.ExtPlayerInfo;

public class HUGameInfo extends GameInfo {

    public int[] raisesOnStreet = new int[4];
    public ExtPlayerInfo heroInfo, villainInfo;
	public static final int SITTING_OUT = -2;

    public HUGameInfo() {
        for (int i = 0; i < 4; i++) {
            raisesOnStreet[i] = 0;
        }
    }

    double getAmountToCall() {
        double betDifference = villainInfo.getBet() - heroInfo.getBet();
        if (betDifference <= 0) {
            return 0;
        } else {
            return Math.min(betDifference, heroInfo.getStack());
        }
    }

}

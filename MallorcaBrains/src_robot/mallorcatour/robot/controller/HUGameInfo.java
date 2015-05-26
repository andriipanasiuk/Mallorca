/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.controller;

import mallorcatour.core.game.BaseGameInfo;
import mallorcatour.core.game.LimitType;
import mallorcatour.robot.ExtPlayerInfo;

/**
 *
 * @author Andrew
 */
public class HUGameInfo extends BaseGameInfo {

    public int[] raisesOnStreet = new int[4];
    public ExtPlayerInfo heroInfo, villainInfo;
	public static final int SITTING_OUT = -2;

    public HUGameInfo() {
        for (int i = 0; i < 4; i++) {
            raisesOnStreet[i] = 0;
        }
    }

    @Override
    public double getHeroAmountToCall() {
        double betDifference = villainInfo.getBet() - heroInfo.getBet();
        if (betDifference <= 0) {
            return 0;
        } else {
            return Math.min(betDifference, heroInfo.getStack());
        }
    }

    @Override
    public boolean isVillainSitOut(){
    	return villainInfo.getStack() == HUGameInfo.SITTING_OUT;
    }

    @Override
    public boolean canHeroRaise() {
        if (limitType == LimitType.NO_LIMIT) {
            boolean result = getBankRollAtRisk() > 0;
            return result;
        } else {
            return raisesOnStreet[street.intValue()] < 4;
        }
    }

    public int getNumRaises(){
        return raisesOnStreet[street.intValue()];
    }

	@Override
	public mallorcatour.core.game.PlayerInfo getVillain() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean onButton() {
		return heroInfo.isOnButton();
	}
}

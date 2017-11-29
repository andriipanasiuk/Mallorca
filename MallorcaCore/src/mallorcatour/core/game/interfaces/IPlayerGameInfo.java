package mallorcatour.core.game.interfaces;

import mallorcatour.core.game.OpenPlayerInfo;

public interface IPlayerGameInfo extends IGameInfo {

	boolean isVillainSitOut();

	boolean canHeroRaise();

	OpenPlayerInfo getVillain();

	OpenPlayerInfo getHero();

	boolean onButton();
	double getAmountToCall();
}
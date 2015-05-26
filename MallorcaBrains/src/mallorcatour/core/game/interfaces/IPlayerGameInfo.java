package mallorcatour.core.game.interfaces;

import mallorcatour.core.game.OpenPlayerInfo;
import mallorcatour.core.game.PlayerInfo;

public interface IPlayerGameInfo<P extends PlayerInfo> extends IGameInfo {
	public double getHeroAmountToCall();

	boolean isVillainSitOut();

	public boolean canHeroRaise();

	public boolean onButton();

	P getVillain();

	OpenPlayerInfo getHero();

}
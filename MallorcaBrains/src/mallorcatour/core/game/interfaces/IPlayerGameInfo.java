package mallorcatour.core.game.interfaces;

import mallorcatour.core.game.PlayerInfo;

public interface IPlayerGameInfo extends IGameInfo {
	public double getHeroAmountToCall();

	boolean isVillainSitOut();

	public boolean canHeroRaise();

	public boolean onButton();

	PlayerInfo getVillain();

	PlayerInfo getHero();

}
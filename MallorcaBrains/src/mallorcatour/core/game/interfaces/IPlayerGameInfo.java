package mallorcatour.core.game.interfaces;

public interface IPlayerGameInfo extends IGameInfo {
	public double getHeroAmountToCall();

	boolean isVillainSitOut();

	public boolean canHeroRaise();

	public boolean onButton();

}
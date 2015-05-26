package mallorcatour.core.game;

public class OpenGameInfo extends GameInfoAdapter<OpenPlayerInfo>{

	public OpenPlayerInfo heroInfo, villainInfo;
	@Override
	public OpenPlayerInfo getVillain() {
		return villainInfo;
	}

	@Override
	public OpenPlayerInfo getHero() {
		return heroInfo;
	}
}
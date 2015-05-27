/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.game;

/**
 * 
 * @author Andrew
 */
public class GameInfo extends GameInfoAdapter {

	public OpenPlayerInfo heroInfo;
	public OpenPlayerInfo villainInfo;

	public GameInfo() {
	}

	@Override
	public OpenPlayerInfo getVillain() {
		return villainInfo;
	}

	@Override
	public OpenPlayerInfo getHero() {
		return heroInfo;
	}

	@Override
	public boolean onButton(String name) {
		if (name.equals(heroInfo.name)) {
			return heroInfo.isOnButton;
		} else {
			return villainInfo.isOnButton;
		}
	}

}

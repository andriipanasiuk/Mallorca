/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.game;



/**
 *
 * @author Andrew
 */
public class GameInfo extends GameInfoAdapter<PlayerInfo> {

	public OpenPlayerInfo heroInfo;
	public PlayerInfo villainInfo;

    public GameInfo() {
    }


    @Override
	public PlayerInfo getVillain() {
		return villainInfo;
	}

    @Override
	public OpenPlayerInfo getHero() {
		return heroInfo;
	}

}

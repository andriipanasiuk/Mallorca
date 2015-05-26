package mallorcatour.robot.ps;

import mallorcatour.bot.interfaces.IExternalAdvisor;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;

public class VsSitoutBot implements IExternalAdvisor {

	@Override
	public Action getAction(IPlayerGameInfo gameInfo) {
		if (gameInfo.isVillainSitOut()) {
			double percent = 0.5;
			return Action
					.createRaiseAction(percent * (gameInfo.getPotSize() + gameInfo.getHeroAmountToCall()), percent);
		}
		return null;
	}

}
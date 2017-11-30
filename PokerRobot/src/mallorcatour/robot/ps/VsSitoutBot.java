package mallorcatour.robot.ps;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.Advisor;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.game.state.HandState;

public class VsSitoutBot implements Advisor {

	@Override
	public IAdvice getAdvice(HandState situation, HoleCards cards, IPlayerGameInfo gameInfo) {
		if (gameInfo.isVillainSitOut()) {
			double percent = 0.5;
			return Action
					.createRaiseAction(percent * (gameInfo.getPotSize() + gameInfo.getAmountToCall()), percent);
		}
		return null;
	}

	@Override
	public String toString() {
		return "VsSitoutBot";
	}
}
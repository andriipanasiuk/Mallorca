package mallorcatour.core.game.advice;

import java.util.Arrays;
import java.util.List;

import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.Advisor;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.game.state.HandState;

public class UnitedAdvisor implements Advisor {
    private List<Advisor> advisors;

    public UnitedAdvisor(Advisor... advisors) {
        this.advisors = Arrays.asList(advisors);
    }

    @Override
    public IAdvice getAdvice(HandState situation, HoleCards cards, IPlayerGameInfo gameInfo) {
        for (Advisor advisor : advisors) {
            IAdvice advice = advisor.getAdvice(situation, cards, gameInfo);
            if (advice != null) {
                return advice;
            }
        }
        return null;
    }
}

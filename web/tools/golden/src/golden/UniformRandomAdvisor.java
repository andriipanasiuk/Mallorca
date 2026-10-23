package golden;

import java.util.ArrayList;
import java.util.List;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.Advisor;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.GameContext;
import mallorcatour.core.game.state.HandState;

public final class UniformRandomAdvisor implements Advisor {

	private static final IAdvice FACING_BET = new Probabilities(1.0 / 3, 1.0 / 3, 1.0 / 3);
	private static final IAdvice NOT_FACING_BET = new Probabilities(0, 0.5, 0.5);

	@Override
	public IAdvice getAdvice(HandState situation, HoleCards cards, GameContext gameInfo) {
		return situation.getAmountToCall() > 0 ? FACING_BET : NOT_FACING_BET;
	}

	private static final class Probabilities implements IAdvice {
		private final double fold;
		private final double passive;
		private final double aggressive;

		Probabilities(double fold, double passive, double aggressive) {
			this.fold = fold;
			this.passive = passive;
			this.aggressive = aggressive;
		}

		@Override
		public Action getAction() {
			throw new IllegalStateException("The villain model is never sampled");
		}

		@Override
		public double getFold() {
			return fold;
		}

		@Override
		public double getPassive() {
			return passive;
		}

		@Override
		public double getAggressive() {
			return aggressive;
		}

		@Override
		public List<Number> getValues() {
			List<Number> values = new ArrayList<>();
			values.add(fold);
			values.add(passive);
			values.add(aggressive);
			return values;
		}
	}
}

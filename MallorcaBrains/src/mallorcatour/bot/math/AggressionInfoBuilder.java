package mallorcatour.bot.math;

import mallorcatour.core.game.interfaces.IAggressionInfo;

public class AggressionInfoBuilder {
	public static AggressionInfoBuilder.AggressionInfo build(IAggressionInfo info) {
		return new AggressionInfo(info);
	}

	public static class AggressionInfo implements IAggressionInfo {

		private int heroAggresionActionCount = 0;
		private int villainAggresionActionCount = 0;
		private int heroActionCount = 0;
		private int villainActionCount = 0;
		private boolean wasHeroPreviousAggresive;
		private boolean wasOpponentPreviousAggresive;

		public AggressionInfo(IAggressionInfo info) {
			this.heroActionCount = info.getHeroActionCount();
			this.villainActionCount = info.getVillainActionCount();
			this.heroAggresionActionCount = info.getHeroAggresionActionCount();
			this.villainAggresionActionCount = info.getVillainAggresionActionCount();
			this.wasHeroPreviousAggresive = info.wasHeroPreviousAggresive();
			this.wasOpponentPreviousAggresive = info.wasVillainPreviousAggresive();
		}

		public AggressionInfoBuilder.AggressionInfo plusHeroAction() {
			heroActionCount++;
			return this;
		}

		public AggressionInfoBuilder.AggressionInfo plusHeroAggressiveAction() {
			heroAggresionActionCount++;
			return this;
		}

		public AggressionInfoBuilder.AggressionInfo plusVillainAction(boolean aggressive) {
			if(aggressive) {
				villainAggresionActionCount++;
			}
			villainActionCount++;
			return this;
		}

		public int getHeroAggresionActionCount() {
			return heroAggresionActionCount;
		}

		public AggressionInfoBuilder.AggressionInfo setHeroAggresionActionCount(int heroAggresionActionCount) {
			this.heroAggresionActionCount = heroAggresionActionCount;
			return this;
		}

		public int getVillainAggresionActionCount() {
			return villainAggresionActionCount;
		}

		public AggressionInfoBuilder.AggressionInfo setVillainAggresionActionCount(int villainAggresionActionCount) {
			this.villainAggresionActionCount = villainAggresionActionCount;
			return this;
		}

		public int getHeroActionCount() {
			return heroActionCount;
		}

		public AggressionInfoBuilder.AggressionInfo setHeroActionCount(int heroActionCount) {
			this.heroActionCount = heroActionCount;
			return this;
		}

		public int getVillainActionCount() {
			return villainActionCount;
		}

		public AggressionInfoBuilder.AggressionInfo setVillainActionCount(int villainActionCount) {
			this.villainActionCount = villainActionCount;
			return this;
		}

		public boolean wasHeroPreviousAggresive() {
			return wasHeroPreviousAggresive;
		}

		public AggressionInfoBuilder.AggressionInfo setWasHeroPreviousAggresive(boolean wasHeroPreviousAggresive) {
			this.wasHeroPreviousAggresive = wasHeroPreviousAggresive;
			return this;
		}

		public boolean wasVillainPreviousAggresive() {
			return wasOpponentPreviousAggresive;
		}

		public AggressionInfoBuilder.AggressionInfo setWasOpponentPreviousAggresive(boolean wasOpponentPreviousAggresive) {
			this.wasOpponentPreviousAggresive = wasOpponentPreviousAggresive;
			return this;
		}

		@Override
		@Deprecated
		public double getHeroAggresionFrequency() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		@Deprecated
		public double getVillainAggresionFrequency() {
			// TODO Auto-generated method stub
			return 0;
		}
	}
}
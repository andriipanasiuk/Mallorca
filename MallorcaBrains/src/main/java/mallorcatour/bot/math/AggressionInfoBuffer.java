package mallorcatour.bot.math;

import mallorcatour.core.game.interfaces.IAggressionInfo;

/**
 * Mutable реализация интерфейса предоставления данных об активных действиях игроков в текущей раздаче.
 */
public class AggressionInfoBuffer implements IAggressionInfo {

    private int heroAggresionActionCount = 0;
    private int villainAggresionActionCount = 0;
    private int heroActionCount = 0;
    private int villainActionCount = 0;
    private boolean wasHeroPreviousAggresive;
    private boolean wasOpponentPreviousAggresive;

    public AggressionInfoBuffer(IAggressionInfo info) {
        this.heroActionCount = info.getHeroActionCount();
        this.villainActionCount = info.getVillainActionCount();
        this.heroAggresionActionCount = info.getHeroAggresionActionCount();
        this.villainAggresionActionCount = info.getVillainAggresionActionCount();
        this.wasHeroPreviousAggresive = info.wasHeroPreviousAggresive();
        this.wasOpponentPreviousAggresive = info.wasVillainPreviousAggresive();
    }

    public AggressionInfoBuffer plusHeroAction() {
        heroActionCount++;
        return this;
    }

    public AggressionInfoBuffer plusHeroAggressiveAction() {
        heroAggresionActionCount++;
        return this;
    }

    public AggressionInfoBuffer plusVillainAction(boolean aggressive) {
        if(aggressive) {
            villainAggresionActionCount++;
        }
        villainActionCount++;
        return this;
    }

    public int getHeroAggresionActionCount() {
        return heroAggresionActionCount;
    }

    public AggressionInfoBuffer setHeroAggresionActionCount(int heroAggresionActionCount) {
        this.heroAggresionActionCount = heroAggresionActionCount;
        return this;
    }

    public int getVillainAggresionActionCount() {
        return villainAggresionActionCount;
    }

    public AggressionInfoBuffer setVillainAggresionActionCount(int villainAggresionActionCount) {
        this.villainAggresionActionCount = villainAggresionActionCount;
        return this;
    }

    public int getHeroActionCount() {
        return heroActionCount;
    }

    public AggressionInfoBuffer setHeroActionCount(int heroActionCount) {
        this.heroActionCount = heroActionCount;
        return this;
    }

    public int getVillainActionCount() {
        return villainActionCount;
    }

    public AggressionInfoBuffer setVillainActionCount(int villainActionCount) {
        this.villainActionCount = villainActionCount;
        return this;
    }

    public boolean wasHeroPreviousAggresive() {
        return wasHeroPreviousAggresive;
    }

    public AggressionInfoBuffer setWasHeroPreviousAggresive(boolean wasHeroPreviousAggresive) {
        this.wasHeroPreviousAggresive = wasHeroPreviousAggresive;
        return this;
    }

    public boolean wasVillainPreviousAggresive() {
        return wasOpponentPreviousAggresive;
    }

    public AggressionInfoBuffer setWasOpponentPreviousAggresive(boolean wasOpponentPreviousAggresive) {
        this.wasOpponentPreviousAggresive = wasOpponentPreviousAggresive;
        return this;
    }

}

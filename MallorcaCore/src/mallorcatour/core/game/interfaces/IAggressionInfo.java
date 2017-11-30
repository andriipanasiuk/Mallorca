package mallorcatour.core.game.interfaces;

/**
 * Содержит информацию об агрессивных действиях игроков в текущей раздаче.
 */
public interface IAggressionInfo {

	public int getHeroAggresionActionCount();

	public int getVillainAggresionActionCount();

	public int getHeroActionCount();

	public int getVillainActionCount();

	/**
	 * @return the wasOpponentPreviousAggresive
	 */
	public boolean wasVillainPreviousAggresive();

	/**
	 * @return the wasIPreviousAggresive
	 */
	public boolean wasHeroPreviousAggresive();

}
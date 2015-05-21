package mallorcatour.core.game.interfaces;

public interface IAggressionInfo {

	public int getHeroAggresionActionCount();

	public int getVillainAggresionActionCount();

	/**
	 * @return the localAggresion
	 */
	@Deprecated
	public double getHeroAggresionFrequency();

	public int getHeroActionCount();

	public int getVillainActionCount();

	/**
	 * @return the localOpponentAggresion
	 */
	@Deprecated
	public double getVillainAggresionFrequency();

	/**
	 * @return the wasOpponentPreviousAggresive
	 */
	public boolean wasVillainPreviousAggresive();

	/**
	 * @return the wasIPreviousAggresive
	 */
	public boolean wasHeroPreviousAggresive();

}
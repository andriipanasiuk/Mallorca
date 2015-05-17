/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.game.hand;

/**
 *
 * @author Andrew
 */
public interface HandEquities {

    /**
     * @return the preflopEquity
     */
    public double getPreflopEquity();

    /**
     * @return the flopEquity
     */
    public double getFlopEquity();

    /**
     * @return the turnEquity
     */
    public double getTurnEquity();

    /**
     * @return the riverEquity
     */
    public double getRiverEquity();

    /**
     * @return the flopPositivePotential
     */
    public double getFlopPositivePotential();

    /**
     * @return the flopNegativePotential
     */
    public double getFlopNegativePotential();

    /**
     * @return the turnPositivePotential
     */
    public double getTurnPositivePotential();

    /**
     * @return the turnNegativePotential
     */
    public double getTurnNegativePotential();
}

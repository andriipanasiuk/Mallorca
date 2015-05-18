/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.villainobserver;

/**
 *
 * @author Andrew
 */
public interface IVillainListener {

    void onVillainChange(VillainStatistics villain);

    void onVillainKnown(boolean known);
}

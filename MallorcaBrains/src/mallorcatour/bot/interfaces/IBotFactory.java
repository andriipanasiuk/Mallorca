/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.interfaces;


/**
 *
 * @author Andrew
 */
public interface IBotFactory {

    IPlayer createBot(IVillainModeller modeller, ISpectrumListener spectrumListener,
            IDecisionListener decisionListener, String debug);
}

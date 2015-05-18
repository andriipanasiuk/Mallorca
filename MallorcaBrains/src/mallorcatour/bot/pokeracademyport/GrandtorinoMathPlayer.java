/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.pokeracademyport;

import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.math.FLMathBot;
import mallorcatour.core.spectrum.Spectrum;
import mallorcatour.util.Log;

/**
 *
 * @author Andrew
 */
public class GrandtorinoMathPlayer extends AbstractPABot {

    IPlayer myBot;

    public GrandtorinoMathPlayer() {
        super("MathBot");
        myBot = new FLMathBot(getVillainModeller(), new ISpectrumListener() {

            public void onSpectrumChanged(Spectrum spectrum, String log) {
                String str = spectrum.toString();
                if (!str.isEmpty()) {
                    Log.f(getDebugPath(), "Villain spectrum: " + str.substring(0, str.indexOf("\n")));
                }
            }
        }, getDecisionListener(), getDebugPath());
    }

    @Override
    protected IPlayer getRealBot() {
        return myBot;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.pa.port.bot;

import mallorcatour.core.bot.IPlayer;
import mallorcatour.core.bot.LimitType;
import mallorcatour.game.situation.IDecisionListener;
import mallorcatour.game.situation.ISpectrumListener;
import mallorcatour.grandtorino.bot.GrandtorinoBot;
import mallorcatour.grandtorino.bot.IHumanAdvisor;
import mallorcatour.grandtorino.nn.modeller.BasePokerNN;
import mallorcatour.grandtorino.nn.modeller.BaseVillainModeller;
import mallorcatour.grandtorino.nn.sparbotFL.SparbotNeurals;

/**
 *
 * @author Andrew
 */
public class GrandtorinoSparbotPlayer extends AbstractPABot {

    IPlayer myBot;

    public GrandtorinoSparbotPlayer() {
        super("GrandSparbot");
        myBot = new GrandtorinoBot(new BasePokerNN(new SparbotNeurals(), false, 20),
                new BaseVillainModeller(LimitType.FIXED_LIMIT, getDebugPath()),
                LimitType.FIXED_LIMIT, ISpectrumListener.EMPTY,
                IDecisionListener.EMPTY, IHumanAdvisor.EMPTY,
                /*isHumanAdvisor*/ false,/*modelling*/ false, false, getDebugPath());
    }

    @Override
    protected IPlayer getRealBot() {
        return myBot;
    }
}

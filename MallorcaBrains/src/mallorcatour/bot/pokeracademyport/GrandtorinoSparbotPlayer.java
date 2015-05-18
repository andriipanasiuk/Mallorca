/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.pokeracademyport;

import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.neural.GrandtorinoBot;
import mallorcatour.core.game.LimitType;
import mallorcatour.bot.modeller.BasePokerNN;
import mallorcatour.bot.modeller.BaseVillainModeller;
import mallorcatour.grandtorino.nn.sparbotFL.SparbotNeurals;
import mallorcatour.robot.humanadvisor.IHumanAdvisor;

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

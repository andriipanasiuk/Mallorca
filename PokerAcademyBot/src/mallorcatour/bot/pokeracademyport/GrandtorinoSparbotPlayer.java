/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.pokeracademyport;

import mallorcatour.bot.interfaces.IPlayer;

/**
 *
 * @author Andrew
 */
public class GrandtorinoSparbotPlayer extends AbstractPABot {

    IPlayer myBot;

    public GrandtorinoSparbotPlayer() {
		super("GrandSparbot");
		// TODO add real neural using
		throw new UnsupportedOperationException();
    }

    @Override
    protected IPlayer getRealBot() {
        return myBot;
    }
}

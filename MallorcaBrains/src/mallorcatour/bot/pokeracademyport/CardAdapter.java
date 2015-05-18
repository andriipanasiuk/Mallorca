/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.pokeracademyport;

import com.biotools.meerkat.Card;

/**
 *
 * @author Andrew
 */
public class CardAdapter {

    public static mallorcatour.core.game.Card createFromPACard(Card c) {
        return mallorcatour.core.game.Card.valueOf(c.toString());
    }
}

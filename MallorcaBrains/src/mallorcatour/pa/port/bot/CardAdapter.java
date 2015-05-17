/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.pa.port.bot;

import com.biotools.meerkat.Card;

/**
 *
 * @author Andrew
 */
public class CardAdapter {

    public static mallorcatour.game.core.Card createFromPACard(Card c) {
        return mallorcatour.game.core.Card.valueOf(c.toString());
    }
}

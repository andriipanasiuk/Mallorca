/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.recognizer.assets;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mallorcatour.core.game.Card;
import mallorcatour.core.game.Deck;
import mallorcatour.util.robot.ImageUtils;

/**
 *
 * @author Andrew
 */
public class DefaultCardAssets implements ICardAssets {

    private static final String PATH_BASE = "assets/ps/default/cards/";
    private static Map<Card, BufferedImage> map = new HashMap<Card, BufferedImage>();

    private static void init() {
        List<Card> deck = Deck.getCards();
        for (Card card : deck) {
            String path = getPath(card);
            BufferedImage cardImage = ImageUtils.fromFile(path);
            map.put(card, cardImage);
        }
    }

    private static String getPath(Card card) {
        return PATH_BASE + card.toString() + ".png";
    }

    static {
        init();
    }

    public BufferedImage getCardImage(Card card) {
        return map.get(card);
    }
}

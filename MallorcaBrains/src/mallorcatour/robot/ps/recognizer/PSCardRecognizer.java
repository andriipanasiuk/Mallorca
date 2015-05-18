/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.ps.recognizer;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mallorcatour.core.game.Card;
import mallorcatour.core.game.Deck;
import mallorcatour.robot.controller.PokerPreferences;
import mallorcatour.robot.recognizer.assets.ICardAssets;
import mallorcatour.util.DateUtils;
import mallorcatour.util.ImageUtils;
import mallorcatour.util.Log;

/**
 *
 * @author Andrew
 */
public class PSCardRecognizer {

    private ICardAssets cardAssets;

    public PSCardRecognizer(ICardAssets assets) {
        this.cardAssets = assets;
    }

    public void changeAssets(ICardAssets assets) {
        this.cardAssets = assets;
    }

    private static class CardPosition implements Comparable<CardPosition> {

        Card card;
        int xPosition;

        CardPosition(Card card, int xPosition) {
            this.card = card;
            this.xPosition = xPosition;
        }

        public int compareTo(CardPosition other) {
            return this.xPosition - other.xPosition;
        }
    }

    public List<Card> getCards(BufferedImage cardsImage, int max) {
        List<Card> result = new ArrayList<Card>();
        if (max == 0) {
            return result;
        }
        List<CardPosition> cardPositions = new ArrayList<CardPosition>();
        //for debug
        if (PokerPreferences.LOG_IMAGE_RECOGNITION) {
            ImageUtils.toFile(cardsImage, "cards_" + DateUtils.getDate(true) + ".png", true);
        }
        //
        List<Card> deck = Deck.getCards();
        for (Card card : deck) {
            BufferedImage cardImage = cardAssets.getCardImage(card);
            Point cardPosition = ImageUtils.isPartOf(cardsImage, cardImage);
            if (cardPosition != null) {
                cardPositions.add(new CardPosition(card, cardPosition.x));
                if (cardPositions.size() == max) {
                    break;
                }
            }
        }
        Collections.sort(cardPositions);
        for (CardPosition cardPosition : cardPositions) {
            result.add(cardPosition.card);
        }
        return result;
    }

    public int getCardsCount(BufferedImage cardsImage) {
        int result = 0;
        int width = cardsImage.getWidth() / 5;
        for (int i = 0; i < 5; i++) {
            if (isCard(cardsImage.getSubimage(i * width, 0, width, cardsImage.getHeight()))) {
                result++;
            }
        }
        return result;
    }

    private boolean isCard(BufferedImage image) {
        int whitePixels = 0;
        int allPixels = 0;
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                if (image.getRGB(i, j) == Color.WHITE.getRGB()) {
                    whitePixels++;
                }
                allPixels++;
            }
        }
        Log.d("White: " + whitePixels + ". All: " + allPixels);
        double proportion = (double) whitePixels / allPixels;
        return proportion > 0.25;
    }
}

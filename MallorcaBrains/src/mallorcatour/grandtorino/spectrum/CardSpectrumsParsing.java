/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.spectrum;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import mallorcatour.core.game.Card;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.Card.Suit;
import mallorcatour.core.game.Card.Value;
import mallorcatour.core.spectrum.Spectrum;
import mallorcatour.util.ImageUtils;
import mallorcatour.util.Log;
import mallorcatour.util.RecognizerUtils;

/**
 * Класс для вычисления спектра из окна Poker Academy
 * @author Andrew
 */
public class CardSpectrumsParsing {

	//TODO протестировать на программе PokerGenius
    private static final Rectangle SPECTRUM_RECTANGLE = new Rectangle(19, 248, 180, 180);

    public static Spectrum parse() {
        Point p = ImageUtils.isOnScreen("assets/pa/hand_evaluator.png");
        if (p == null) {
            throw new RuntimeException();
        }
        BufferedImage spectrumImage = ImageUtils.getScreenCapture(RecognizerUtils.getGlobalRectangle(SPECTRUM_RECTANGLE, p));
        ImageUtils.toFile(spectrumImage, "spectrum.png", false);
        double sectionWidth = (double) spectrumImage.getWidth() / 13;
        double sectionHeight = (double) spectrumImage.getHeight() / 13;
        Spectrum spectrum = new Spectrum();
        double sum = 0;
        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 13; j++) {
                Color c = new Color(spectrumImage.getRGB((int) (sectionWidth / 2 + j * sectionWidth),
                        (int) (sectionHeight / 2 + i * sectionHeight)));
                spectrumImage.setRGB((int) (sectionWidth / 2 + j * sectionWidth),
                        (int) (sectionHeight / 2 + i * sectionHeight), Color.RED.getRGB());
                double probability = (double) (255 - c.getBlue()) / 255;
                if (j > i) {
                    for (Suit suit : Suit.getSuits()) {
                        spectrum.add(new HoleCards(new Card(Value.valueOf(12 - i), suit),
                                new Card(Value.valueOf(12 - j), suit)), probability);
                    }
                    sum += 4 * probability;
                } else if (i == j) {
                    for (int f = 0; f < 4; f++) {
                        for (int s = f + 1; s < 4; s++) {
                            spectrum.add(new HoleCards(new Card(Value.valueOf(12 - i), Suit.valueOf(f)),
                                    new Card(Value.valueOf(12 - i), Suit.valueOf(s))), probability);
                        }
                    }
                    sum += 6 * probability;
                } else {
                    for (int f = 0; f < 4; f++) {
                        for (int s = 0; s < 4; s++) {
                            if (f == s) {
                                continue;
                            }
                            spectrum.add(new HoleCards(new Card(Value.valueOf(12 - j), Suit.valueOf(f)),
                                    new Card(Value.valueOf(12 - i), Suit.valueOf(s))), probability);
                        }
                    }
                    sum += 12 * probability;
                }
                System.out.print(probability + " ");
            }
            System.out.println("");
        }
        Log.d("Sum = " + spectrum.summaryWeight() / 1326);
        ImageUtils.toFile(spectrumImage, "spectrum_yellow.png", false);
        return spectrum;
    }
}

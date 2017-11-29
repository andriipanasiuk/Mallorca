/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.villainobserver;

/**
 *
 * @author Andrew
 */
public interface IVillainObserver {

    IVillainObserver EMPTY = new IVillainObserver() {

        public void onHandPlayed(long handNumber) {
            //do nothing
        }

        public boolean isVillainKnown() {
            return false;
        }

        public PokerPlayer getCurrentVillain() {
            throw new IllegalStateException("Villain is unknown!");
        }

        public void endSession() {
            //do nothing
        }
    };

    void endSession();

    void onHandPlayed(long handNumber);

    boolean isVillainKnown();

    public interface PokerPlayer {

        String getName();
    }
    PokerPlayer getCurrentVillain();
}

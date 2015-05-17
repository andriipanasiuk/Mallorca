/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.game.hand;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class Tournament {

    private Date startDate;
    private String description;
    private List<Hand> hands;

    public Tournament(Date startDate, String description) {
        this.startDate = startDate;
        this.description = description;
        hands = new ArrayList<Hand>();
    }

    @Override
    public String toString() {
        return "Tournament " + description + " " + getStartingDate();
    }

    public void addHand(Hand h) {
        hands.add(h);
    }

    public List<Hand> getHands() {
        return hands;
    }

    /**
     * @return the startDate
     */
    public Date getStartingDate() {
        return startDate;
    }
}

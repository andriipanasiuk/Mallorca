/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.interfaces;

import java.util.Date;

/**
 *
 * @author Andrew
 */
public interface ITournamentHandler extends IHandHandler {

    void onTournamentStart(Date date, int playerCount, String description);

    void onTournamentEnd(String winner);
}

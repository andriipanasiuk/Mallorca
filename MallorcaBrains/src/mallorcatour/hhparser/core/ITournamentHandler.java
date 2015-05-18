/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.hhparser.core;

import java.util.Date;

import mallorcatour.core.game.interfaces.IHandHandler;

/**
 *
 * @author Andrew
 */
public interface ITournamentHandler extends IHandHandler {

    void onTournamentStart(Date date, int playerCount, String description);

    void onTournamentEnd(String winner);
}

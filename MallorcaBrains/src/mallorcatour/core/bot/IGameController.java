/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.bot;

import java.util.List;
import mallorcatour.game.core.Card;
import mallorcatour.game.core.Action;
import mallorcatour.grandtorino.robot.PlayerInfo;

/**
 *
 * @author Andrew
 */
public interface IGameController {

    void onNewHand(long handNumber, List<PlayerInfo> players, Card holeCard1,
            Card holeCard2, List<Card> board, double pot, LimitType limitType);

    Action onMyAction(List<Card> boardCards,
            double pot);

}

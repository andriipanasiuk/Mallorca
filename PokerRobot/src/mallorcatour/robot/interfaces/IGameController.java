/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.interfaces;

import java.util.List;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.LimitType;
import mallorcatour.robot.ExtPlayerInfo;

/**
 *
 * @author Andrew
 */
public interface IGameController {

    void onNewHand(long handNumber, List<ExtPlayerInfo> players, Card holeCard1,
            Card holeCard2, List<Card> board, double pot, LimitType limitType);

    Action onMyAction(List<Card> boardCards,
            double pot);

}

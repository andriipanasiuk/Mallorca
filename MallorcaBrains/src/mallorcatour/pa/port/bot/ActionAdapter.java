/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.pa.port.bot;

import com.biotools.meerkat.Action;
import mallorcatour.util.Log;

/**
 *
 * @author Andrew
 */
public class ActionAdapter {

    public static mallorcatour.game.core.Action createFromPAAction(Action action) {
        mallorcatour.game.core.Action result;
        if (action.isBetOrRaise()) {
            result = mallorcatour.game.core.Action.raiseAction(action.getAmount());
        } else if (action.isCheck()) {
            result = mallorcatour.game.core.Action.checkAction();
        } else if (action.isCall()) {
            result = mallorcatour.game.core.Action.callAction(action.getToCall());
        } else if (action.isFold()) {
            result = mallorcatour.game.core.Action.foldAction();
        } else {
            Log.d("Strange action: " + action.toString());
            throw new RuntimeException();
        }
        return result;
    }

    public static Action createPAAction(mallorcatour.game.core.Action action, double toCall) {
        if (action.isAggressive()) {
            return new Action(Action.RAISE, toCall, action.getAmount());
        } else if (action.isCall()) {
            return new Action(Action.CALL, toCall, action.getAmount());
        } else if (action.isCheck()) {
            return new Action(Action.CHECK, toCall, 0);
        } else if (action.isFold()) {
            return new Action(Action.FOLD, toCall, 0);
        } else {
            Log.d("Strange action: " + action);
            throw new RuntimeException();
        }
    }
}

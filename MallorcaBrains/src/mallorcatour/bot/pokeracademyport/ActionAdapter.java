/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.pokeracademyport;

import com.biotools.meerkat.Action;

import mallorcatour.tools.Log;

/**
 *
 * @author Andrew
 */
public class ActionAdapter {

    public static mallorcatour.core.game.Action createFromPAAction(Action action) {
        mallorcatour.core.game.Action result;
        if (action.isBetOrRaise()) {
            result = mallorcatour.core.game.Action.raiseAction(action.getAmount());
        } else if (action.isCheck()) {
            result = mallorcatour.core.game.Action.checkAction();
        } else if (action.isCall()) {
            result = mallorcatour.core.game.Action.callAction(action.getToCall());
        } else if (action.isFold()) {
            result = mallorcatour.core.game.Action.fold();
        } else {
            Log.d("Strange action: " + action.toString());
            throw new RuntimeException();
        }
        return result;
    }

    public static Action createPAAction(mallorcatour.core.game.Action action, double toCall) {
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

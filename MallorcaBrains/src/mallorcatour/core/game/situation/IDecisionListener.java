/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.game.situation;

import mallorcatour.core.game.Action;

/**
 *
 * @author Andrew
 */
public interface IDecisionListener {

    public static final IDecisionListener EMPTY = new IDecisionListener() {

        public void onDecided(LocalSituation situation, Action action) {
            //do nothing
        }
    };

    public void onDecided(LocalSituation situation, Action action);
}

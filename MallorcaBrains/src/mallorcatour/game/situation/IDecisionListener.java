/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.game.situation;

import mallorcatour.game.core.Action;

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

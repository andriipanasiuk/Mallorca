package mallorcatour.core.game.advice;

import mallorcatour.core.game.state.HandState;


/**
 * Interface for listening advice in every state.
 * Something like AdviceListener. Consider rename this interface.
 * @author andriipanasiuk
 *
 */
public interface AdvisorListener {
	AdvisorListener NONE = (situation, advice) -> {
        //do nothing
    };
	void onAdvice(HandState situation, IAdvice advice);
}
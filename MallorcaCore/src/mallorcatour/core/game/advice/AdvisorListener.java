package mallorcatour.core.game.advice;

import mallorcatour.core.game.situation.HandState;


/**
 * Interface for listening advice in every situation.
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
package mallorcatour.core.game.advice;

import mallorcatour.core.game.situation.LocalSituation;


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
	void onAdvice(LocalSituation situation, IAdvice advice);
}
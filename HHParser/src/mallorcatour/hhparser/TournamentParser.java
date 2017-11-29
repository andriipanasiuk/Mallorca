/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.hhparser;

import java.util.ArrayList;
import java.util.List;

import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.equilator.preflop.PreflopEquilatorImpl;
import mallorcatour.core.game.Hand;
import mallorcatour.core.game.situation.HandState;
import mallorcatour.hhparser.core.Tournament;
import mallorcatour.neural.core.PokerLearningExample;
import mallorcatour.core.game.situation.observer.SituationHandler;

/**
 * 
 * @author Andrew
 */
public class TournamentParser {

	public static List<PokerLearningExample> parseLocalSituations(Tournament tournament, String heroName) {
		SituationHandler situationHandler = new SituationHandler(true, heroName, new PokerEquilatorBrecher(),
				new PreflopEquilatorImpl());
		return parseLocalSituations(tournament, heroName, situationHandler);
	}

	public static List<PokerLearningExample> parseLocalSituations(Tournament tournament, String heroName,
			SituationHandler handler) {
		List<PokerLearningExample> result = new ArrayList<PokerLearningExample>();

		HandParser parser = new HandParser();
		for (Hand hand : tournament.getHands()) {
			List<HandState> situations = parser.parse(hand, heroName, handler);
			for (HandState situation : situations) {
				PokerLearningExample example = new PokerLearningExample(situation);
				example.setHand(hand);
				result.add(example);
			}
		}
		return result;
	}
}

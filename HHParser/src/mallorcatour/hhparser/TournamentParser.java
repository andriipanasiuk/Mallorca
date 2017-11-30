/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.hhparser;

import java.util.ArrayList;
import java.util.List;

import mallorcatour.equilator.PokerEquilatorBrecher;
import mallorcatour.equilator.preflop.PreflopEquilatorImpl;
import mallorcatour.core.game.Hand;
import mallorcatour.core.game.state.HandState;
import mallorcatour.core.game.state.observer.StrengthHandStateObserver;
import mallorcatour.hhparser.core.Tournament;
import mallorcatour.neural.core.PokerLearningExample;

/**
 * 
 * @author Andrew
 */
public class TournamentParser {

	public static List<PokerLearningExample> parseLocalSituations(Tournament tournament, String heroName) {
		StrengthHandStateObserver stateObserver = new StrengthHandStateObserver(true, heroName, new PokerEquilatorBrecher(),
				new PreflopEquilatorImpl());
		return parseLocalSituations(tournament, heroName, stateObserver);
	}

	public static List<PokerLearningExample> parseLocalSituations(Tournament tournament, String heroName,
			StrengthHandStateObserver handler) {
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

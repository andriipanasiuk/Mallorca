/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.game.hand;

import java.util.ArrayList;
import java.util.List;
import mallorcatour.core.bot.LimitType;
import mallorcatour.game.situation.HandParser;
import mallorcatour.game.situation.LocalSituation;
import mallorcatour.game.situation.SituationHandler;
import mallorcatour.interfaces.ISituationHandler;
import mallorcatour.neuronetworkwrapper.PokerLearningExample;

/**
 *
 * @author Andrew
 */
public class TournamentParser {

    public static List<PokerLearningExample> parseLocalSituations(Tournament tournament,
            String heroName) {
        return parseLocalSituations(tournament, heroName,
                new SituationHandler(LimitType.NO_LIMIT));
    }

    public static List<PokerLearningExample> parseLocalSituations(
            Tournament tournament, String heroName, ISituationHandler handler) {
        List<PokerLearningExample> result = new ArrayList<PokerLearningExample>();

        HandParser parser = new HandParser();
        for (Hand hand : tournament.getHands()) {
            List<LocalSituation> situations = parser.parse(hand, heroName, handler);
            for (LocalSituation situation : situations) {
                PokerLearningExample example = new PokerLearningExample(situation);
                example.setHand(hand);
                result.add(example);
            }
        }
        return result;
    }
}

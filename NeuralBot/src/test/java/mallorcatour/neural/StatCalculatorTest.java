package mallorcatour.neural;

import java.io.File;
import java.util.List;

import mallorcatour.neural.core.PokerLearningExample;
import mallorcatour.core.stats.PokerStats;
import mallorcatour.core.stats.StatCalculator;
import mallorcatour.tools.Log;

/**
 * Class for calculating poker stats of different players by the list of PokerLearningExample
 * @author andriipanasiuk
 *
 */
public class StatCalculatorTest {

    //TODO consider to move away this class from hhparser to separate project
    public static void main(String... args) {
        {
            String path = "Dafish2";
            List<PokerLearningExample> list = PokerLEIO.readFromDir(new File(path));
            PokerStats stats = StatCalculator.calculate(list);
            Log.d(stats.toString());
        }
        {
            String path = "opponent_models/PBX";
            List<PokerLearningExample> list = PokerLEIO.readFromDir(new File(path));
            PokerStats stats = StatCalculator.calculate(list);
            Log.d(stats.toString());
        }
        {
            String path = "opponent_models/Germany";
            List<PokerLearningExample> list = PokerLEIO.readFromDir(new File(path));
            PokerStats stats = StatCalculator.calculate(list);
            Log.d(stats.toString());
        }
    }

}

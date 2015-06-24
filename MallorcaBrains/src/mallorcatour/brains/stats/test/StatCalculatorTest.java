package mallorcatour.brains.stats.test;

import java.io.File;
import java.util.List;

import mallorcatour.brains.stats.StatCalculator;
import mallorcatour.core.game.interfaces.IPokerStats;
import mallorcatour.hhparser.SituationIO;
import mallorcatour.neural.core.PokerLearningExample;
import mallorcatour.tools.Log;

public class StatCalculatorTest {

	public static void main(String... args) {
		{
			String path = "CheckCheckBurn";
			List<PokerLearningExample> list = SituationIO.readFromDir(new File(path));
			IPokerStats stats = StatCalculator.calculate(list);
			Log.d(stats.toString());
		}
		{
			String path = "PBX";
			List<PokerLearningExample> list = SituationIO.readFromDir(new File(path));
			IPokerStats stats = StatCalculator.calculate(list);
			Log.d(stats.toString());
		}
		{
			String path = "Germany";
			List<PokerLearningExample> list = SituationIO.readFromDir(new File(path));
			IPokerStats stats = StatCalculator.calculate(list);
			Log.d(stats.toString());
		}
		{
			String path = "GusXensen";
			List<PokerLearningExample> list = SituationIO.readFromDir(new File(path));
			IPokerStats stats = StatCalculator.calculate(list);
			Log.d(stats.toString());
		}
	}

}

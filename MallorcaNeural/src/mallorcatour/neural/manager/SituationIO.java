package mallorcatour.neural.manager;

import java.io.File;
import java.util.List;

import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.situation.HandState;
import mallorcatour.neural.core.PokerLearningExample;
import mallorcatour.tools.Log;
import mallorcatour.tools.MyFileWriter;

public class SituationIO {

	/**
	 * Write list of poker examples (HandState -> Advice) to 4 files for
	 * preflop, flop, turn and river in predefined directory
	 */
	public static void writeToFiles(File directory, boolean addToExamples, List<PokerLearningExample> examples) {
		MyFileWriter preflopWriter = MyFileWriter.prepareForWriting(directory.getAbsolutePath() + "/preflop.txt",
				addToExamples);
		MyFileWriter flopWriter = MyFileWriter.prepareForWriting(directory.getAbsolutePath() + "/flop.txt",
				addToExamples);
		MyFileWriter turnWriter = MyFileWriter.prepareForWriting(directory.getAbsolutePath() + "/turn.txt",
				addToExamples);
		MyFileWriter riverWriter = MyFileWriter.prepareForWriting(directory.getAbsolutePath() + "/river.txt",
				addToExamples);

		int preflop = 0, flop = 0, turn = 0, river = 0;
		for (int index = 0; index < examples.size(); index++) {
			HandState situation = examples.get(index).getInput();
			IAdvice advice = examples.get(index).getOutput();
			switch (situation.getStreet()) {
			case PokerStreet.PREFLOP_VALUE:
				preflop++;
				LEManager.toFile(preflopWriter, situation, advice);
				break;
			case PokerStreet.FLOP_VALUE:
				flop++;
				LEManager.toFile(flopWriter, situation, advice);
				break;
			case PokerStreet.TURN_VALUE:
				turn++;
				LEManager.toFile(turnWriter, situation, advice);
				break;
			case PokerStreet.RIVER_VALUE:
				river++;
				LEManager.toFile(riverWriter, situation, advice);
				break;
			}
		}
		Log.d("Preflop: " + preflop);
		Log.d("Flop: " + flop);
		Log.d("Turn: " + turn);
		Log.d("River: " + river);
		preflopWriter.endWriting();
		flopWriter.endWriting();
		turnWriter.endWriting();
		riverWriter.endWriting();
	}

}

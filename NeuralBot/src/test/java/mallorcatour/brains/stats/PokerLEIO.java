package mallorcatour.brains.stats;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.advice.AdviceCreator;
import mallorcatour.core.game.state.HandState;
import mallorcatour.neural.core.LearningExample;
import mallorcatour.neural.core.PokerLearningExample;
import mallorcatour.tools.Log;
import mallorcatour.tools.ReaderUtils;

/**
 * Class for I/O operations with PokerLearningExample class
 * @author andriipanasiuk
 *
 */
public class PokerLEIO {

	public static List<PokerLearningExample> readFromDir(File directory) {
		List<PokerLearningExample> examples = new ArrayList<>();
		String preflopPath = directory.getAbsolutePath() + "/preflop.txt";
		String flopPath = directory.getAbsolutePath() + "/flop.txt";
		String turnPath = directory.getAbsolutePath() + "/turn.txt";
		String riverPath = directory.getAbsolutePath() + "/river.txt";
		readFromFile(preflopPath, PokerStreet.PREFLOP, examples);
		readFromFile(flopPath, PokerStreet.FLOP, examples);
		readFromFile(turnPath, PokerStreet.TURN, examples);
		readFromFile(riverPath, PokerStreet.RIVER, examples);
		return examples;
	}

	private static void readFromFile(String path, PokerStreet street, List<PokerLearningExample> examples) {
		BufferedReader reader = ReaderUtils.initReader(path);
		int count = 0;
		String buffer;
		AdviceCreator adviceCreator = new AdviceCreator();
		while ((buffer = ReaderUtils.readLineFrom(reader)) != null) {
			if (buffer.isEmpty()) {
				continue;
			}
			count++;
			try {
				LearningExample example = LearningExample.valueOf(buffer);
				HandState situation = new HandState(example.getInput(), street.intValue());
				Advice advice = adviceCreator.create(true, example.getOutput().asArray());
				PokerLearningExample pokerExample = new PokerLearningExample(situation, advice);
				examples.add(pokerExample);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		Log.d("Read " + count + " examples from " + path);
	}

	public static void main(String... args) {
		List<PokerLearningExample> list = readFromDir(new File("Cuba"));
		for (int i = 0; i < 10; i++) {
			HandState situation = list.get(i).getSituation();
			Log.d("Street: " + PokerStreet.valueOf(situation.getStreet()));
			Log.d(situation.toString());
		}
	}

}

package mallorcatour.hhparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.advice.AdviceCreator;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.neural.core.LearningExample;
import mallorcatour.neural.core.PokerLearningExample;
import mallorcatour.neural.manager.LEManager;
import mallorcatour.tools.Log;
import mallorcatour.tools.MyFileWriter;
import mallorcatour.tools.ReaderUtils;

public class SituationIO {

	/**
	 * Write list of poker examples (LocalSituation -> Advice) to 4 files for
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
			LocalSituation situation = examples.get(index).getInput();
			Advice advice = examples.get(index).getOutput();
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

	public static void readFromFile(String path, PokerStreet street, List<PokerLearningExample> examples) {
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
				LocalSituation situation = new LocalSituation(example.getInput(), street.intValue());
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
			LocalSituation situation = list.get(i).getSituation();
			Log.d("Street: " + PokerStreet.valueOf(situation.getStreet()));
			Log.d(situation.toString());
		}
	}
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.hhparser;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mallorcatour.core.game.Hand;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.core.game.situation.SituationHandler;
import mallorcatour.hhparser.core.Tournament;
import mallorcatour.neural.core.PokerLearningExample;
import mallorcatour.neural.manager.LEManager;
import mallorcatour.tools.DateUtils;
import mallorcatour.tools.Log;
import mallorcatour.tools.MyFileWriter;

/**
 *
 * @author Andrew
 */
public class NNConverter {

    private final static String ADVICES = "advices_";
    //max time difference between start of tournament and advice filename in ms
    private final static long MAX_TIME_DIFFERENCE = 20000;
    private final static String HERO_NAME = "Andrew";

    public static List<PokerLearningExample> parseLocalSituations(
            List<Hand> hands, String heroName, SituationHandler handler) {
        List<PokerLearningExample> result = new ArrayList<PokerLearningExample>();
        HandParser parser = new HandParser();

        for (Hand hand : hands) {
            List<LocalSituation> handSituations = parser.parse(hand, heroName, handler);
            for (LocalSituation situation : handSituations) {
                PokerLearningExample example = new PokerLearningExample(situation);
                example.setHand(hand);
                result.add(example);
            }
        }
        return result;
    }

    public static List<PokerLearningExample> parseLocalSituationsWithoutAdvices(
            List<Hand> hands, String heroName, SituationHandler handler) {
        List<PokerLearningExample> result = new ArrayList<PokerLearningExample>();
        HandParser parser = new HandParser();

        int count = 0;
        for (Hand hand : hands) {
            count++;
            List<PokerLearningExample> handSituations = parser.parseWithActions(hand, heroName, handler);
            for (PokerLearningExample example : handSituations) {
                example.setHand(hand);
                result.add(example);
            }
		}
		Log.d("Parsed "  +count + " hands");
        return result;
    }

    public static List<PokerLearningExample> localSituationsToFile(File directory, Tournament tournament,
            String pathToAdvices, SituationHandler handler) {
        boolean addToExamples = true;
        String adviceFilename = getAdviceFile(tournament, pathToAdvices);

        Log.d("Tournament start: " + tournament.getStartingDate());
        if (adviceFilename == null) {
            Log.d("There is no advices for this tournament");
            return null;
        }
        Log.d("Advice filename: " + adviceFilename);

        List<Advice> advices = AdviceReader.readAdvices(adviceFilename);

        List<PokerLearningExample> examples = TournamentParser.parseLocalSituations(
                tournament, HERO_NAME, handler);
        if (examples.size() != advices.size()) {
            Log.d("Count of advices in file " + adviceFilename
                    + " and situations in tournament are not equals!\n"
                    + "Situations in tournament: " + examples.size()
                    + ". Advices count: " + advices.size());
            return null;
        }
		for (int index = 0; index < examples.size(); index++) {
			examples.get(index).setAdvice(advices.get(index));
		}
        writeToFiles(directory, addToExamples, examples);
        return examples;
    }

	/**
	 * Write list of poker examples (LocalSituation -> Advice) to 4 files for
	 * preflop, flop, turn and river in predefined directory
	 */
	public static void writeToFiles(File directory, boolean addToExamples, List<PokerLearningExample> examples) {
		MyFileWriter preflopWriter = MyFileWriter.prepareForWriting(directory.getAbsolutePath() + "/preflop.txt",
				addToExamples);
		MyFileWriter flopWriter = MyFileWriter.prepareForWriting(directory.getAbsolutePath() + "/flop.txt", addToExamples);
		MyFileWriter turnWriter = MyFileWriter.prepareForWriting(directory.getAbsolutePath() + "/turn.txt", addToExamples);
		MyFileWriter riverWriter = MyFileWriter.prepareForWriting(directory.getAbsolutePath() + "/river.txt", addToExamples);

        for (int index = 0; index < examples.size(); index++) {
            LocalSituation situation = examples.get(index).getInput();
            Advice advice = examples.get(index).getOutput();
            switch (situation.getStreet()) {
                case LocalSituation.PREFLOP:
                    LEManager.toFile(preflopWriter, situation, advice);
                    break;
                case LocalSituation.FLOP:
                    LEManager.toFile(flopWriter, situation, advice);
                    break;
                case LocalSituation.TURN:
                    LEManager.toFile(turnWriter, situation, advice);
                    break;
                case LocalSituation.RIVER:
                    LEManager.toFile(riverWriter, situation, advice);
                    break;
            }
        }
        preflopWriter.endWriting();
        flopWriter.endWriting();
        turnWriter.endWriting();
        riverWriter.endWriting();
	}

    public static List<PokerLearningExample> localSituationsToFile(File dir, List<Hand> hands,
            String pathToAdvices, SituationHandler handler) {
        boolean addToExamples = true;
        String adviceFilename = getAdviceFile(hands.get(0).getStartingDate(), pathToAdvices);

        if (adviceFilename == null) {
            Log.d("There is no advices for this tournament");
            return null;
        }
        Log.d("Advice filename: " + adviceFilename);

        List<Advice> advices = AdviceReader.readAdvices(adviceFilename);

        List<PokerLearningExample> examples = parseLocalSituations(
                hands, HERO_NAME, handler);
        if (examples.size() != advices.size()) {
            Log.d("Count of advices in file " + adviceFilename
                    + " and situations in handhistory are not equals!\n"
                    + "Situations in handhistory: " + examples.size()
                    + ". Advices count: " + advices.size());
            return null;
        }
        for (int index = 0; index < examples.size(); index++) {
        	examples.get(index).setAdvice(advices.get(index));
        }
        writeToFiles(dir, addToExamples, examples);
        return examples;
    }

    private static String getAdviceFile(Tournament tournament, String pathToAdvices) {
        Date tournamentDate = tournament.getStartingDate();
        File adviceDir = new File(pathToAdvices);
        if (!adviceDir.isDirectory()) {
            throw new IllegalArgumentException(pathToAdvices + " is not a directory");
        }
        Date fileDate = null;
        File[] files = adviceDir.listFiles();
        for (File file : files) {
            String filename = file.getName();
            if (filename.startsWith(ADVICES)) {
                fileDate = DateUtils.parseDate(filename.substring(ADVICES.length(),
                        filename.indexOf(".txt")));
                if (DateUtils.difference(fileDate, tournamentDate) < MAX_TIME_DIFFERENCE) {
                    return file.getAbsolutePath();
                }
            }
        }
        return null;
    }

    private static String getAdviceFile(Date date, String pathToAdvices) {
        File adviceDir = new File(pathToAdvices);
        if (!adviceDir.isDirectory()) {
            throw new IllegalArgumentException(pathToAdvices + " is not a directory");
        }
        File[] files = adviceDir.listFiles();

        Date fileDate = null;

        for (File file : files) {
            String filename = file.getName();
            if (filename.startsWith(ADVICES)) {
                fileDate = DateUtils.parseDate(filename.substring(ADVICES.length(),
                        filename.indexOf(".txt")));
                if (DateUtils.difference(fileDate, date) < MAX_TIME_DIFFERENCE
                        && fileDate.getTime() < date.getTime()) {
                    return file.getAbsolutePath();
                }
            }
        }
        return null;
    }
}

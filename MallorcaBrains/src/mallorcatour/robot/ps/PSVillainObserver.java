/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.ps;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mallorcatour.bot.villainobserver.IVillainListener;
import mallorcatour.bot.villainobserver.IVillainObserver;
import mallorcatour.bot.villainobserver.VillainStatistics;
import mallorcatour.core.game.Hand;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.core.game.situation.NoStrengthSituationHandler;
import mallorcatour.hhparser.HandParser;
import mallorcatour.hhparser.PSHHParser;
import mallorcatour.hhparser.core.BaseHandHandler;
import mallorcatour.hhparser.core.HandManager;
import mallorcatour.neuronetworkwrapper.LEManager;
import mallorcatour.neuronetworkwrapper.PokerLearningExample;
import mallorcatour.util.Log;
import mallorcatour.util.Pair;
import mallorcatour.util.ReaderUtils;
import mallorcatour.util.SerializatorUtils;
import mallorcatour.util.StringUtils;

import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.MomentumBackpropagation;

/**
 *
 * @author Andrew
 */
public class PSVillainObserver implements IVillainObserver {

    private static String HANDHISTORY_PATH_BASE;
    private final static String VILLAIN_STATISTICS_PATH = "villains\\";
    private final static String TOURNAMENT = "Tournament";
    private final static int LEARN_PREFLOP_EVERY_HAND = 5;
    private final static int ITERATION_COUNT = 15000;
    private final String HANDHISTORY_PATH;
    private VillainStatistics currentVillain;
    private boolean isVillainKnown;
    private final IVillainListener listener;
    private final String tableName;
    private final String DEBUG_PATH;
    private final int preflopInputSize;
    private final String heroName;

    static {
        init();
    }

    private static void init() {
        BufferedReader reader = ReaderUtils.initReader("handhistory.txt");
        try {
            HANDHISTORY_PATH_BASE = reader.readLine();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public PSVillainObserver(IVillainListener listener, LimitType limitType,
            String tableName, String heroName, String debug) {
        if (listener == null) {
            throw new NullPointerException();
        }
        Log.d("Create PSVillainObserver. Table name: " + tableName);
        Log.d("Create PSVillainObserver. Hero name: " + heroName);
        HANDHISTORY_PATH = HANDHISTORY_PATH_BASE + heroName;
        Log.d("Handhistory path: " + HANDHISTORY_PATH);
        this.listener = listener;
        this.tableName = tableName;
        this.heroName = heroName;
		this.DEBUG_PATH = debug;
		// TODO change to constant values from LocalSituation
		if (limitType == LimitType.FIXED_LIMIT) {
			preflopInputSize = 7;
		} else {
			preflopInputSize = 8;
		}
    }

    public void onHandPlayed(long handNumber) {
        File currentHHFile = getHHFile();
        if (currentHHFile == null) {
            return;
        }
        Log.f(DEBUG_PATH, "\n-------------------------");
        Log.f(DEBUG_PATH, "HH file: " + currentHHFile.getName());
        BaseHandHandler handHandler = new BaseHandHandler();
        PSHHParser.parseHandHistory(currentHHFile.getAbsolutePath(), handHandler);
        List<Hand> hands = handHandler.buildHands();
        int size = hands.size();
        Log.f(DEBUG_PATH, "Count of hands in file: " + size);
        Hand lastHand;
        List<Hand> handsForParsing = new ArrayList<Hand>();
        int i = 0;
        do {
            i++;
            if (i > size) {
                throw new RuntimeException("In file " + currentHHFile.getAbsolutePath()
                        + " there are " + size + " hands. i = " + i);
            }
            lastHand = hands.get(size - i);
            handsForParsing.add(lastHand);
        } while (lastHand.getId() != handNumber);
        Log.f(DEBUG_PATH, "Played " + handsForParsing.size() + " hands");
        for (Hand hand : handsForParsing) {
            String villainName = HandManager.getVillainName(hand, heroName);
            if (currentVillain == null) {
                isVillainKnown = true;
                currentVillain = loadVillainStatistics(villainName);
                Log.f(DEBUG_PATH, "New villain: " + currentVillain.getName());
                listener.onVillainKnown(true);
            }
            if (!currentVillain.getName().equals(villainName)) {
                saveCurrentVillain();
                currentVillain = loadVillainStatistics(villainName);
            }
            //if player is sitting out this hand not add to his statistics
            if (hand.getPlayerInfo(villainName).isSittingOut()) {
                continue;
            }
            List<PokerLearningExample> examples = new HandParser().parseWithActions(
                    hand, villainName,
                    new NoStrengthSituationHandler(hand.getLimitType()));
            currentVillain.addHandPlayed();
            currentVillain.addPokerLearningExamples(examples);
            currentVillain.addAggressionInfo(HandManager.calculatePostflopAF(hand, villainName));
            currentVillain.addAggressionFrequencyInfo(HandManager.calculatePostflopAggressionFrequency(hand, villainName));
            currentVillain.addFoldInfo(HandManager.calculatePostflopFoldFrequency(hand, villainName));
            int wtsd = HandManager.calculateWTSD(hand, villainName);
            if (wtsd != -1) {
                currentVillain.addWtsdInfo(new Pair<Integer, Integer>(wtsd, 1));
            }
            if (currentVillain.getHandsCount() % LEARN_PREFLOP_EVERY_HAND == 0) {
                learnPreflopNN(currentVillain);
                saveCurrentVillain();
            }
        }
        listener.onVillainChange(currentVillain);
    }

    public void endSession() {
        if (currentVillain != null) {
            saveCurrentVillain();
            currentVillain = null;
            listener.onVillainKnown(false);
        }
    }

    private void saveCurrentVillain() {
        String path = VILLAIN_STATISTICS_PATH + currentVillain.getName() + ".stat";
        SerializatorUtils.save(path, currentVillain);
        Log.d("Villain " + currentVillain.getName() + " was saved to " + path);
    }

    private VillainStatistics loadVillainStatistics(String villainName) {
        File file = new File(VILLAIN_STATISTICS_PATH);
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.getName().equals(villainName + ".stat")) {
                return SerializatorUtils.load(f.getAbsolutePath(), VillainStatistics.class);
            }
        }
        return new VillainStatistics(villainName);
    }

    private File getHHFile() {
        File handhistoryDir = new File(HANDHISTORY_PATH);
        if (!handhistoryDir.exists()) {
            return null;
        }
        File[] files = handhistoryDir.listFiles();
        File result = null;
        if (tableName.contains(TOURNAMENT)) {
            int tournamentNumber = Integer.parseInt(StringUtils.between(tableName, TOURNAMENT + " ", " "));
            String find = "T" + tournamentNumber;
            for (File file : files) {
                if (file.getName().contains(find)) {
                    return file;
                }
            }
            System.err.println("There is no HH file for table: " + tableName);
            return null;
//            throw new IllegalArgumentException("There is no HH file for table: " + tableName);
        } else {
            long maxTime = 0;
            String table = StringUtils.between(tableName, 0, " -");
            for (File file : files) {
                if (file.getName().contains(table)) {
                    long lastModifiedTime = file.lastModified();
                    if (lastModifiedTime > maxTime) {
                        maxTime = lastModifiedTime;
                        result = file;
                    }
                }
            }
            if (result == null) {
                System.err.println("There is no HH file for table: " + tableName);
                return null;
//              throw new IllegalArgumentException("There is no HH file for table: " + tableName);
            }
            return result;

        }
    }

    private void learnPreflopNN(VillainStatistics villainStatistics) {
        long start = System.currentTimeMillis();
        List<PokerLearningExample> examples = villainStatistics.getExamples();
        MultiLayerPerceptron nn = new MultiLayerPerceptron(preflopInputSize, 10, 3);
        List<PokerLearningExample> preflopExamples = new ArrayList<PokerLearningExample>();
        for (PokerLearningExample example : examples) {
            if (example.getSituation().getStreet() == LocalSituation.PREFLOP) {
                preflopExamples.add(example);
            }
		}
		MomentumBackpropagation rule = new MomentumBackpropagation();
		rule.setMaxIterations(ITERATION_COUNT / preflopExamples.size());
		nn.learn(LEManager.createTrainingSet(preflopExamples), rule);
		villainStatistics.setPreflopNeuralNetwork(nn);
        villainStatistics.setPreflopLearned(true);
        Log.f(DEBUG_PATH, "Villain's preflop MLP was learned in "
                + (System.currentTimeMillis() - start) + " ms");
    }

    public boolean isVillainKnown() {
        return isVillainKnown;
    }

    public VillainStatistics getCurrentVillain() {
        if (!isVillainKnown) {
            throw new IllegalStateException("Villain is unknown!");
        }
        return currentVillain;
    }

    public static void main(String[] args) {
        PSVillainObserver.init();
        String folder = HANDHISTORY_PATH_BASE + "grandtorino";
        Log.d(folder);
        Log.d("Files: " + new File(folder).listFiles().length);
    }

}

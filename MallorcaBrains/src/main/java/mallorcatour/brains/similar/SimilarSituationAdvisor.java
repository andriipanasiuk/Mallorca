package mallorcatour.brains.similar;

import java.util.ArrayList;
import java.util.List;

import mallorcatour.advice.creator.SmartAdviceCreator;
import mallorcatour.advice.creator.SmartPostflopAdviceCreator;
import mallorcatour.advice.creator.SmartRiverAdviceCreator;
import mallorcatour.brains.HavingStats;
import mallorcatour.brains.neural.IPokerNeurals;
import mallorcatour.brains.neural.ISituationData;
import mallorcatour.brains.neural.NeuralAdvisor;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.advice.AdviceCreator;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.core.game.situation.LocalSituationDistance;
import mallorcatour.neural.core.PokerExamples;
import mallorcatour.neural.core.PokerLearningExample;
import mallorcatour.neural.manager.LEManager;
import mallorcatour.tools.Log;
import mallorcatour.tools.SerializatorUtils;

public class SimilarSituationAdvisor extends NeuralAdvisor {
	private final ISituationData situationData;
	private List<PokerLearningExample> allSituations;
	private List<PokerLearningExample> preflopSituations, flopSituations, turnSituations, riverSituations;
	private int MIN_COUNT_OF_SIMILAR_SITUATIONS = 4;
	private final static double[] DEGREE_OF_SIMILARITY = new double[4];

	public SimilarSituationAdvisor(IPokerNeurals nnStreaming, HavingStats stats, ISituationData situationData,
			String name) {
		super(nnStreaming, stats, name);
		this.situationData = situationData;
		DEGREE_OF_SIMILARITY[0] = 0.05;
		DEGREE_OF_SIMILARITY[1] = 0.05;
		DEGREE_OF_SIMILARITY[2] = 0.1;
		DEGREE_OF_SIMILARITY[3] = 0.1;
		initSituations();
	}

	public SimilarSituationAdvisor(IPokerNeurals nnStreaming, HavingStats stats, ISituationData situationData,
			int minCountOfSimilar, String name) {
		super(nnStreaming, stats, name);
		this.situationData = situationData;
		this.MIN_COUNT_OF_SIMILAR_SITUATIONS = minCountOfSimilar;
	}

	private void initSituations() {
		allSituations = SerializatorUtils.load(situationData.getSituationStream(), PokerExamples.class).getExamples();
		preflopSituations = new ArrayList<PokerLearningExample>();
		flopSituations = new ArrayList<PokerLearningExample>();
		turnSituations = new ArrayList<PokerLearningExample>();
		riverSituations = new ArrayList<PokerLearningExample>();

		for (PokerLearningExample situation : allSituations) {
			int street = situation.getInput().getStreet();
			if (street == PokerStreet.PREFLOP_VALUE) {
				preflopSituations.add(situation);
			} else if (street == PokerStreet.FLOP_VALUE) {
				flopSituations.add(situation);
			} else if (street == PokerStreet.TURN_VALUE) {
				turnSituations.add(situation);
			} else if (street == PokerStreet.RIVER_VALUE) {
				riverSituations.add(situation);
			}
		}
		Log.d("BasePokerNN. Situations inited");
	}

	@Override
	public IAdvice getAdvice(LocalSituation situation, HoleCards cards, IPlayerGameInfo gameInfo) {
		List<PokerLearningExample> examplesForUse = null;
		AdviceCreator adviceCreator = null;
		int street = situation.getStreet();
		switch (street) {
		case 0:
			examplesForUse = preflopSituations;
			adviceCreator = new SmartAdviceCreator();
			break;
		case 1:
			examplesForUse = flopSituations;
			adviceCreator = new SmartPostflopAdviceCreator();
			break;
		case 2:
			examplesForUse = turnSituations;
			adviceCreator = new SmartPostflopAdviceCreator();
			break;
		case 3:
			examplesForUse = riverSituations;
			adviceCreator = new SmartRiverAdviceCreator(situation.getPotOdds() == 0);
			break;
		default:
			throw new IllegalArgumentException("Illegal street: " + street);
		}
		Advice result;
		long start = System.currentTimeMillis();
		result = processSimilarity(examplesForUse, situation, adviceCreator);
		Log.d("Search for similar situations longs " + (System.currentTimeMillis() - start) + " ms");
		if (result != null) {
			return result;
		}
		return result;
	}

	private Advice processSimilarity(List<PokerLearningExample> examplesForUse, LocalSituation current,
			AdviceCreator creator) {
		double similarityDegree = DEGREE_OF_SIMILARITY[current.getStreet()];
		List<PokerLearningExample> similarSituations = LEManager.getSimilarSituations(examplesForUse, current,
				similarityDegree, new LocalSituationDistance());
		if (similarSituations.size() >= MIN_COUNT_OF_SIMILAR_SITUATIONS) {
			return Advice.create(creator, current.canRaise(), LEManager.getAverageOutput(similarSituations));
		}
		return null;
	}
}
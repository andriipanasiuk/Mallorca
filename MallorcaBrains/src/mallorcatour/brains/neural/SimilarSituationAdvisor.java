package mallorcatour.brains.neural;

import java.util.ArrayList;
import java.util.List;

import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.advice.AdviceCreator;
import mallorcatour.core.game.advice.SmartAdviceCreator;
import mallorcatour.core.game.advice.SmartPostflopAdviceCreator;
import mallorcatour.core.game.advice.SmartRiverAdviceCreator;
import mallorcatour.core.game.interfaces.IPokerStats;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.core.game.situation.LocalSituationDistance;
import mallorcatour.neural.core.PokerExamples;
import mallorcatour.neural.core.PokerLearningExample;
import mallorcatour.neural.manager.LEManager;
import mallorcatour.util.Log;
import mallorcatour.util.SerializatorUtils;

public class SimilarSituationAdvisor extends NeuralAdvisor {
	private final ISituationData situationData;
	private List<PokerLearningExample> allSituations;
	private List<PokerLearningExample> preflopSituations, flopSituations, turnSituations, riverSituations;
	private int MIN_COUNT_OF_SIMILAR_SITUATIONS = 4;
	private final static double[] DEGREE_OF_SIMILARITY = new double[4];

	public SimilarSituationAdvisor(IPokerNeurals nnStreaming, IPokerStats stats, ISituationData situationData, String name) {
		super(nnStreaming, stats, name);
		this.situationData = situationData;
		DEGREE_OF_SIMILARITY[0] = 0.05;
		DEGREE_OF_SIMILARITY[1] = 0.05;
		DEGREE_OF_SIMILARITY[2] = 0.1;
		DEGREE_OF_SIMILARITY[3] = 0.1;
		initSituations();
	}

	public SimilarSituationAdvisor(IPokerNeurals nnStreaming, IPokerStats stats, ISituationData situationData,
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
			if (street == LocalSituation.PREFLOP) {
				preflopSituations.add(situation);
			} else if (street == LocalSituation.FLOP) {
				flopSituations.add(situation);
			} else if (street == LocalSituation.TURN) {
				turnSituations.add(situation);
			} else if (street == LocalSituation.RIVER) {
				riverSituations.add(situation);
			}
		}
		Log.d("BasePokerNN. Situations inited");
	}

	@Override
	public Advice getAdvice(LocalSituation situation, HoleCards cards) {
		List<PokerLearningExample> examplesForUse = null;
		AdviceCreator adviceCreator = null;
		boolean canRaise = situation.canRaise();
		int street = situation.getStreet();
		switch (street) {
		case 0:
			examplesForUse = preflopSituations;
			adviceCreator = new SmartAdviceCreator(canRaise);
			break;
		case 1:
			examplesForUse = flopSituations;
			adviceCreator = new SmartPostflopAdviceCreator(canRaise);
			break;
		case 2:
			examplesForUse = turnSituations;
			adviceCreator = new SmartPostflopAdviceCreator(canRaise);
			break;
		case 3:
			examplesForUse = riverSituations;
			adviceCreator = new SmartRiverAdviceCreator(situation.getPotOdds() == 0, canRaise);
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
			return Advice.create(creator, LEManager.getAverageOutput(similarSituations));
		}
		return null;
	}
}
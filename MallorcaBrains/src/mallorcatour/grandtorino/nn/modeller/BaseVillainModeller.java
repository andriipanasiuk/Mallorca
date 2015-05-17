/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.nn.modeller;

import mallorcatour.core.bot.LimitType;
import mallorcatour.game.advice.Advice;
import mallorcatour.game.advice.AdviceCreator;
import mallorcatour.game.advice.SmartAdviceCreator;
import mallorcatour.game.advice.SmartPostflopAdviceCreator;
import mallorcatour.game.core.HoleCards;
import mallorcatour.game.situation.LocalSituation;
import mallorcatour.game.situation.LocalSituationInterpreter;
import mallorcatour.grandtorino.nn.danielxn.DanielxnNeurals;
import mallorcatour.grandtorino.nn.brazil.BrazilNeurals;
import mallorcatour.grandtorino.nn.chucky.ChuckyNeurals;
import mallorcatour.grandtorino.nn.core.AbstractNeurals;
import mallorcatour.grandtorino.nn.germany.GermanyNeurals;
import mallorcatour.grandtorino.nn.pokibratFL.PokibratNeurals;
import mallorcatour.grandtorino.nn.sparbotFL.SparbotNeurals;
import mallorcatour.grandtorino.nn.waterhouse.WaterhouseNeurals;
import mallorcatour.grandtorino.villainstat.IVillainListener;
import mallorcatour.grandtorino.villainstat.VillainStatistics;
import mallorcatour.interfaces.IPokerNN;
import mallorcatour.util.Log;
import org.neuroph.core.NeuralNetwork;

/**
 *
 * @author Andrew
 */
public class BaseVillainModeller implements IPokerNN, IVillainListener {

    private static final AbstractNeurals DEFAULT_NL_NEURAL = new DanielxnNeurals();
    private static final AbstractNeurals DEFAULT_FL_NEURAL = new SparbotNeurals();
    private static final AbstractNeurals POKIBRAT_FL = new PokibratNeurals();
    private static final AbstractNeurals SPARBOT_FL = new SparbotNeurals();
    private static final AbstractNeurals BRAZIL_FL = new BrazilNeurals();
    private static final AbstractNeurals WATERHOUSE_FL = new WaterhouseNeurals();
    private static final AbstractNeurals GERMANY_FL = new GermanyNeurals();
    private static final AbstractNeurals CHUCKY_FL = new ChuckyNeurals();
    private static final int REQUIRED_HANDS_FOR_MODELLING = 5;
    private VillainStatistics villainStatistics;
    private AbstractNeurals currentVillainNeural;
    private final PreflopSpectrumModeller preflopVillainModeller;
    private final LimitType limitType;
    private boolean isVillainKnown;
    private final String DEBUG_PATH;

    public BaseVillainModeller(LimitType limitType, String debug) {
        this.limitType = limitType;
        preflopVillainModeller = new PreflopSpectrumModeller();
        currentVillainNeural = limitType == LimitType.NO_LIMIT ? DEFAULT_NL_NEURAL : DEFAULT_FL_NEURAL;
        this.DEBUG_PATH = debug;
    }

    public Advice getAdvice(LocalSituation situation, HoleCards cards) {
        NeuralNetwork nnForUse = null;
        AdviceCreator adviceCreator = null;
        boolean canRaise = situation.canRaise();
        int street = situation.getStreet();
        switch (street) {
            case 0:
                if (isVillainKnown && villainStatistics.isPreflopLearned()) {
                    return preflopVillainModeller.getAdvice(situation, cards,
                            villainStatistics.getPrefloNeuralNetwork());
                }
                nnForUse = currentVillainNeural.getPreflopNN();
                adviceCreator = new SmartAdviceCreator(canRaise);
                break;
            case 1:
                nnForUse = currentVillainNeural.getFlopNN();
                adviceCreator = new SmartPostflopAdviceCreator(canRaise);
                break;
            case 2:
                nnForUse = currentVillainNeural.getTurnNN();
                adviceCreator = new SmartPostflopAdviceCreator(canRaise);
                break;
            case 3:
                nnForUse = currentVillainNeural.getRiverNN();
                adviceCreator = new SmartPostflopAdviceCreator(canRaise);
                break;
            default:
                throw new IllegalArgumentException("Illegal street: " + street);
        }
        nnForUse.setInput(
                new LocalSituationInterpreter().createInput(situation));
        nnForUse.calculate();
        double[] advice = nnForUse.getOutput();
        Advice result = Advice.create(adviceCreator, advice);
        return result;
    }

    public void onVillainChange(VillainStatistics villain) {
        villainStatistics = villain;
        Log.f(DEBUG_PATH, "Villain changed. Name: " + villainStatistics.getName()
                + ". Aggression frequency: " + villainStatistics.getAggressionFrequency()
                + ". Fold frequency: " + villainStatistics.getFoldFrequency()
                + ". AF: " + villainStatistics.getAggressionFactor()
                + ". Wtsd: " + villainStatistics.getWtsd()
                + ". Hands: " + villainStatistics.getHandsCount());
        if (villainStatistics.getHandsCount() >= REQUIRED_HANDS_FOR_MODELLING) {
            chooseModellingNeural();
        }
    }

    private void chooseModellingNeural() {
        double minError = Double.MAX_VALUE;
        double error;
        PokerStatsDistance distance = new PokerStatsDistance();
        if ((error = distance.getDistance(villainStatistics, POKIBRAT_FL)) < minError) {
            minError = error;
            currentVillainNeural = POKIBRAT_FL;
        }
        if ((error = distance.getDistance(villainStatistics, SPARBOT_FL)) < minError) {
            minError = error;
            currentVillainNeural = SPARBOT_FL;
        }
        if ((error = distance.getDistance(villainStatistics, BRAZIL_FL)) < minError) {
            minError = error;
            currentVillainNeural = BRAZIL_FL;
        }
        if ((error = distance.getDistance(villainStatistics, WATERHOUSE_FL)) < minError) {
            minError = error;
            currentVillainNeural = WATERHOUSE_FL;
        }
        if ((error = distance.getDistance(villainStatistics, GERMANY_FL)) < minError) {
            minError = error;
            currentVillainNeural = GERMANY_FL;
        }
        if ((error = distance.getDistance(villainStatistics, CHUCKY_FL)) < minError) {
            minError = error;
            currentVillainNeural = CHUCKY_FL;
        }
        Log.f(DEBUG_PATH, "Modelling by " + currentVillainNeural.getName());
        Log.f(DEBUG_PATH, "Distance to modelling bot: " + minError);
    }

    public void onVillainKnown(boolean known) {
        isVillainKnown = known;
        if (!known) {
            currentVillainNeural = limitType == LimitType.NO_LIMIT ? DEFAULT_NL_NEURAL : DEFAULT_FL_NEURAL;
        }
    }
}

package mallorcatour.neural.modeller;

import mallorcatour.core.game.advice.Advisor;
import mallorcatour.core.stats.PokerStats;

/**
 * Расширение подсказчика, который играет таким образом, что в итоге имеет
 * четкую статистику своих действий.
 */
public interface NeuralAdvisor extends Advisor {
    PokerStats getStats();


}

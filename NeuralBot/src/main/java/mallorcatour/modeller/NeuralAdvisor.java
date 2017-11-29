package mallorcatour.modeller;

import mallorcatour.Advisor;
import mallorcatour.stats.PokerStats;

/**
 * Расширение подсказчика, который играет таким образом, что в итоге имеет
 * четкую статистику своих действий.
 */
public interface NeuralAdvisor extends Advisor {
    PokerStats getStats();


}

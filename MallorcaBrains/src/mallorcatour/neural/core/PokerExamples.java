/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.neural.core;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class PokerExamples implements Serializable, Iterable<PokerLearningExample> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2198075062717636324L;
	private List<PokerLearningExample> examples;

    public PokerExamples(List<PokerLearningExample> examples) {
        this.examples = examples;
    }

    public Iterator<PokerLearningExample> iterator() {
        return examples.iterator();
    }

    public List<PokerLearningExample> getExamples() {
        return examples;
    }
}

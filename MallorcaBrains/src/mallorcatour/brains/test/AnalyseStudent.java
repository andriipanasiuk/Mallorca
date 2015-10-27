package mallorcatour.brains.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.neural.core.PokerLearningExample;
import mallorcatour.neural.manager.SituationIO;
import mallorcatour.tools.Log;

public class AnalyseStudent implements AdvisorListener {

	private List<PokerLearningExample> examples = new ArrayList<>();

	private final String name;
	public AnalyseStudent(String name) {
		this.name = name;
	}

	@Override
	public void onAdvice(LocalSituation situation, IAdvice advice) {
		examples.add(new PokerLearningExample(situation, (Advice) advice));
	}

	public void reset() {
		examples.clear();
	}

	public void save() {
		File directory = new File(name);
		directory.mkdirs();
		SituationIO.writeToFiles(directory, true, examples);
		Log.d("Written " + examples.size() + " examples");
	}
}

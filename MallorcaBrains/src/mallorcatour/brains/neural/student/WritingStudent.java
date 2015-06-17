package mallorcatour.brains.neural.student;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mallorcatour.bot.IStudent;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.hhparser.NNConverter;
import mallorcatour.neural.core.PokerLearningExample;
import mallorcatour.tools.Log;

public class WritingStudent implements IStudent {

	private List<PokerLearningExample> examples = new ArrayList<>();

	private final String name;
	public WritingStudent(String name) {
		this.name = name;
	}

	@Override
	public void learn(LocalSituation situation, IAdvice advice) {
		examples.add(new PokerLearningExample(situation, (Advice) advice));
	}

	public void save() {
		File directory = new File(name);
		directory.mkdirs();
		NNConverter.writeToFiles(directory, true, examples);
		Log.d("Written " + examples.size() + " examples");
	}
}

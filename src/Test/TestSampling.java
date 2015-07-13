package Test;

import java.util.List;

import Corpus.AnnotatedDocument;
import Corpus.Corpus;
import Learning.Model;
import Learning.ObjectiveFunction;
import Learning.Scorer;
import Sampling.DefaultSampler;
import Sampling.ExhaustiveEntitySampler;
import Templates.CheatingTemplate;
import Variables.State;

public class TestSampling {

	public static void main(String[] args) {
		Corpus corpus = TestData.getDummyData();
		List<AnnotatedDocument> documents = corpus.getDocuments();
		AnnotatedDocument doc = documents.get(0);
		State goldState = doc.getGoldState();
		State initialState = new State(doc);
		initialState.goldState = goldState;
		System.out.println("initialState: " + initialState);
		Model model = new Model(new CheatingTemplate());
		Scorer scorer = new Scorer(model);
		// DefaultSampler sampler = new DefaultSampler(10);
		ExhaustiveEntitySampler sampler = new ExhaustiveEntitySampler();
		State nextState = initialState;
		for (int i = 0; i < 4; i++) {
			System.out.println("--------- Step: " + i + " ----------");
			nextState = sampler.getNextStates(nextState, scorer).get(0);
			System.out.println("Next state:");
			System.out.println(nextState);
			System.out.println(nextState.toDetailedString());
		}

		ObjectiveFunction of = new ObjectiveFunction();
		double score = of.score(nextState, goldState);
		System.out.println("Score: " + score);
	}
}

package Test;

import java.util.List;
import java.util.logging.Logger;

import Corpus.AnnotatedDocument;
import Corpus.Corpus;
import Learning.Model;
import Learning.Scorer;
import Learning.objective.DefaultObjectiveFunction;
import Learning.objective.ObjectiveFunction;
import Sampling.ExhaustiveEntitySampler;
import Templates.CheatingTemplate;
import Variables.State;

public class InspectSampling {

	public static void main(String[] args) {
		Corpus<AnnotatedDocument> corpus = DummyData.getDummyData();
		List<AnnotatedDocument> documents = corpus.getDocuments();
		AnnotatedDocument doc = documents.get(0);
		State goldState = doc.getGoldState();
		State initialState = new State(doc);
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

		ObjectiveFunction of = new DefaultObjectiveFunction();
		double score = of.score(nextState, goldState).score;
		System.out.println("Score: " + score);
	}
}

package Test;

import java.util.ArrayList;
import java.util.List;

import Corpus.AnnotatedDocument;
import Corpus.Corpus;
import Corpus.parser.brat.BioNLPLoader;
import Learning.Learner;
import Learning.Model;
import Learning.learner.DefaultLearner;
import Learning.objective.DefaultObjectiveFunction;
import Logging.Log;
import Sampling.ExhaustiveBoundarySampler;
import Sampling.ExhaustiveEntitySampler;
import Sampling.RelationSampler;
import Sampling.Sampler;
import Templates.ContextTemplate;
import Templates.MorphologicalTemplate;
import Templates.RelationTemplate;
import Templates.Template;
import Variables.State;

public class InspectLearning {

	public static void main(String[] args) {
		Corpus<? extends AnnotatedDocument<State>> corpus = null;

		switch (1) {
		case 0:
			corpus = DummyData.getDummyData();
			break;
		case 1:
			corpus = BioNLPLoader.loadBioNLP2013Train(true);
		default:
			break;
		}

		Log.d("Corpus:\n%s", corpus);

		List<Sampler<State>> samplers = new ArrayList<>();
		samplers.add(new ExhaustiveEntitySampler());
		samplers.add(new ExhaustiveBoundarySampler());
		samplers.add(new RelationSampler(20));
		// samplers.add(new DefaultListSampler(20));

		List<Template<State>> templates = new ArrayList<>();
		templates.add(new RelationTemplate());
		templates.add(new MorphologicalTemplate());
		templates.add(new ContextTemplate());
		// templates.add(new CheatingTemplate());

		Model<State> model = new Model<>(templates);
		Learner<State> learner = new DefaultLearner<>(model, samplers, new DefaultObjectiveFunction(), 10, 0.01, 0.001,
				false);
		// learner.train(dataSplit.getTrain());
		learner.train(corpus.getDocuments(), 1);
		// learner.train(corpus.getDocuments().subList(0, 1), 1);
	}
}

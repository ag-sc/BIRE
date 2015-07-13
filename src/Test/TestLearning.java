package Test;

import java.util.ArrayList;
import java.util.List;

import evaluation.DataSplit;
import Corpus.AnnotatedDocument;
import Corpus.Constants;
import Corpus.Corpus;
import Corpus.parser.brat.DatasetLoader;
import Learning.Learner;
import Learning.Model;
import Learning.learner.DefaultLearner;
import Logging.Log;
import Sampling.BoundarySampler;
import Sampling.ExhaustiveEntitySampler;
import Sampling.RelationSampler;
import Sampling.Sampler;
import Templates.ContextTemplate;
import Templates.MorphologicalTemplate;
import Templates.RelationTemplate;
import Templates.Template;

public class TestLearning {

	public static void main(String[] args) {
		Corpus corpus = null;

		switch (2) {
		case 0:
			corpus = TestData.getDummyData();
			break;
		case 1:
			corpus = DatasetLoader
					.convertDatasetToJavaBinaries(Constants.JAVA_BIN_CORPUS_FILEPATH);
			break;
		case 2:
			try {
				corpus = DatasetLoader
						.loadDatasetFromBinaries(Constants.JAVA_BIN_CORPUS_FILEPATH);
			} catch (Exception e) {
				e.printStackTrace();
				Log.w("Preparsed corpus not accessible or corrupted. Parse again:");
				corpus = DatasetLoader
						.convertDatasetToJavaBinaries(Constants.JAVA_BIN_CORPUS_FILEPATH);
			}
			break;
		default:
			break;
		}

		Log.d("Corpus:\n%s", corpus);
		Log.d("Create train/test split");
		DataSplit dataSplit = new DataSplit(corpus, 0.7);
		Log.d("Split: %s => #Train: %s; #Test: %s", dataSplit.getSplit(),
				dataSplit.getTrain().size(), dataSplit.getTest().size());

		List<Sampler> samplers = new ArrayList<Sampler>();
		samplers.add(new ExhaustiveEntitySampler());
		samplers.add(new BoundarySampler(20));
		samplers.add(new RelationSampler(20));
		// samplers.add(new DefaultListSampler(20));

		List<Template> templates = new ArrayList<Template>();
		templates.add(new RelationTemplate());
		templates.add(new MorphologicalTemplate());
		templates.add(new ContextTemplate());
		// templates.add(new CheatingTemplate());

		Model model = new Model(templates);
		Learner learner = new DefaultLearner(model, samplers, 10, 0.01, false);
		// learner.train(dataSplit.getTrain());
		learner.train(dataSplit.getTrain().subList(0, 1));
	}
}

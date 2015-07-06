package Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Corpus.AnnotatedDocument;
import Corpus.Constants;
import Corpus.Corpus;
import Corpus.parser.brat.DatasetLoader;
import Learning.Learner;
import Learning.learner.DefaultLearner;
import Logging.Log;
import Sampling.BoundarySampler;
import Sampling.ExhaustiveEntitySampler;
import Sampling.RelationSampler;
import Sampling.Sampler;

public class TestLearning {

	public static void main(String[] args) {
		// Corpus corpus = TestData.getDummyData();
		Corpus corpus = null;

		// corpus = DatasetLoader
		// .convertDatasetToJavaBinaries(Constants.JAVA_BIN_CORPUS_FILEPATH);

		try {
			corpus = DatasetLoader
					.loadDatasetFromBinaries(Constants.JAVA_BIN_CORPUS_FILEPATH);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d("Corpus:\n%s", corpus);
		// System.out.println(corpus.getCorpusConfig());
		List<AnnotatedDocument> documents = corpus.getDocuments();
		documents = documents.subList(0, 1);
		List<Sampler> samplers = new ArrayList<Sampler>();
		// samplers.add(new DefaultListSampler(20));
		samplers.add(new ExhaustiveEntitySampler(20));
		samplers.add(new BoundarySampler(20));
		samplers.add(new RelationSampler(20));
		Learner learner = new DefaultLearner(10, 0.01, samplers);
		learner.train(documents);
	}
}

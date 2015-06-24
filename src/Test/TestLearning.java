package Test;

import java.util.List;

import Corpus.AnnotatedDocument;
import Corpus.Corpus;
import Learning.Learner;
import Learning.learner.DefaultLearner;

public class TestLearning {

	public static void main(String[] args) {
		Corpus corpus = TestData.getDummyData();
		System.out.println(corpus.getCorpusConfig());
		List<AnnotatedDocument> documents = corpus.getDocuments();
		// Learner learner = new TwoStepLearner(10, 20, 0.1);
		Learner learner = new DefaultLearner(3, 20, 0.1);
		learner.train(documents);
	}

}

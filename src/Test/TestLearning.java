package Test;

import java.util.ArrayList;
import java.util.List;

import Corpus.AnnotatedDocument;
import Corpus.Corpus;
import Learning.Learner;
import Learning.learner.DefaultLearner;
import Sampling.BoundarySampler;
import Sampling.EntitySampler;
import Sampling.RelationSampler;
import Sampling.Sampler;

public class TestLearning {

	public static void main(String[] args) {
		Corpus corpus = TestData.getDummyData();
		System.out.println(corpus.getCorpusConfig());
		List<AnnotatedDocument> documents = corpus.getDocuments();
		List<Sampler> samplers = new ArrayList<Sampler>();
		// samplers.add(new DefaultListSampler(20));
		samplers.add(new EntitySampler(20));
		samplers.add(new BoundarySampler(20));
		samplers.add(new RelationSampler(20));
		Learner learner = new DefaultLearner(3, 0.01, samplers);
		learner.train(documents);
	}

}

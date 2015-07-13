package evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Corpus.AnnotatedDocument;
import Corpus.Corpus;

public class DataSplit {

	private Corpus corpus;
	private double split;
	private List<AnnotatedDocument> all;
	private List<AnnotatedDocument> train;
	private List<AnnotatedDocument> test;

	public DataSplit(Corpus corpus, double split) {
		this.corpus = corpus;
		this.split = split;

		all = new ArrayList<AnnotatedDocument>(corpus.getDocuments());
		Collections.shuffle(all);

		int n = all.size();
		int splitIndex = (int) (split * n);
		train = all.subList(0, splitIndex);
		test = all.subList(splitIndex, n);
	}

	public Corpus getCorpus() {
		return corpus;
	}

	public double getSplit() {
		return split;
	}

	public List<AnnotatedDocument> getAll() {
		return all;
	}

	public List<AnnotatedDocument> getTrain() {
		return train;
	}

	public List<AnnotatedDocument> getTest() {
		return test;
	}

}

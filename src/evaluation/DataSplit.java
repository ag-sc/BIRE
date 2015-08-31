package evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import Corpus.AnnotatedDocument;
import Corpus.Corpus;

public class DataSplit {

	private double split;
	private List<AnnotatedDocument> all;
	private List<AnnotatedDocument> train;
	private List<AnnotatedDocument> test;

	public DataSplit(List<AnnotatedDocument> all, double split, long seed) {
		this.split = split;
		this.all = new ArrayList<AnnotatedDocument>(all);
		shuffleAndSplit(new Random(seed));
	}

	public DataSplit(List<AnnotatedDocument> all, double split) {
		this.split = split;
		this.all = new ArrayList<AnnotatedDocument>(all);
		shuffleAndSplit(new Random());
	}

	private void shuffleAndSplit(Random random) {
		Collections.shuffle(all, random);
		int n = all.size();
		int splitIndex = (int) (split * n);
		train = all.subList(0, splitIndex);
		test = all.subList(splitIndex, n);
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

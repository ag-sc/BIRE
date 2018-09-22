package evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DataSplit<T> {

	private double split;
	private List<T> all;
	private List<T> train;
	private List<T> test;

	public DataSplit(List<T> all, double split, long seed) {
		this.split = split;
		this.all = new ArrayList<T>(all);
		shuffleAndSplit(new Random(seed));
	}

	public DataSplit(List<T> all, double split) {
		this.split = split;
		this.all = new ArrayList<T>(all);
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

	public List<T> getAll() {
		return all;
	}

	public List<T> getTrain() {
		return train;
	}

	public List<T> getTest() {
		return test;
	}

}

package examples.tokenization;

import java.util.TreeSet;

public class Tokenization {
	public TreeSet<Integer> tokenBoundaries;

	public Tokenization() {
		this.tokenBoundaries = new TreeSet<>();
	}

	public Tokenization(Tokenization tokenization) {
		this.tokenBoundaries = new TreeSet<>(tokenization.tokenBoundaries);
	}

	@Override
	public String toString() {
		return "Tokenization [tokenBoundaries=" + tokenBoundaries + "]";
	}
}

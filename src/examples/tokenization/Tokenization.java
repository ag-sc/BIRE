package examples.tokenization;

import java.util.TreeMap;

public class Tokenization {
	public TreeMap<Integer, BoundaryVariable> tokenBoundaries;

	public Tokenization() {
		this.tokenBoundaries = new TreeMap<>();
	}

	public Tokenization(Tokenization tokenization) {
		this.tokenBoundaries = new TreeMap<>(tokenization.tokenBoundaries);
	}

	@Override
	public String toString() {
		return "Tokenization: " + tokenBoundaries.values();
	}
}

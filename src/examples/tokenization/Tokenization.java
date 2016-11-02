package examples.tokenization;

import java.util.Set;
import java.util.TreeSet;

public class Tokenization {
	public Set<Integer> tokenBoundaries;

	public Tokenization() {
		this.tokenBoundaries = new TreeSet<>();
	}

	public Tokenization(Tokenization tokenization) {
		this.tokenBoundaries = new TreeSet<>(tokenization.tokenBoundaries);
	}

	@Override
	public String toString() {
		return "Tokenization: " + tokenBoundaries;
	}
}

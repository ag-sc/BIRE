package examples.tokenization;

import java.util.HashSet;
import java.util.Set;

public class Tokenization {
	public Set<Integer> tokenBoundaries;

	public Tokenization() {
		this.tokenBoundaries = new HashSet<>();
	}

	public Tokenization(Tokenization tokenization) {
		this.tokenBoundaries = new HashSet<>(tokenization.tokenBoundaries);
	}

	@Override
	public String toString() {
		return "Tokenization [tokenBoundaries=" + tokenBoundaries + "]";
	}
}

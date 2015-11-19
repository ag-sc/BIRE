package examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import factors.FactorGraph;
import variables.AbstractState;

public class TokenState extends AbstractState {

	private static final DecimalFormat SCORE_FORMAT = new DecimalFormat("0.00000");
	public Sentence sentence;
	// TODO make this a list and keep in sorted!
	public Set<Integer> tokenBoundaries;

	public TokenState(Sentence sentence) {
		super();
		this.sentence = sentence;
		this.tokenBoundaries = new HashSet<>();
	}

	public TokenState(TokenState state) {
		super();
		this.sentence = state.sentence;
		this.tokenBoundaries = new HashSet<>(state.tokenBoundaries);
		this.modelScore = state.modelScore;
		this.objectiveScore = state.objectiveScore;
		this.factorGraph = new FactorGraph(state.factorGraph);
	}

	@Override
	public String toString() {
		List<Integer> sorted = new ArrayList<>(tokenBoundaries);
		Collections.sort(sorted);
		StringBuilder builder = new StringBuilder();
		int last = 0;
		for (Integer i : sorted) {
			builder.append(sentence.text.subSequence(last, i));
			builder.append(" | ");
			last = i;
		}
		builder.append(sentence.text.subSequence(last, sentence.text.length()));
		return "TokenState [[" + SCORE_FORMAT.format(getModelScore()) + "][" + SCORE_FORMAT.format(getObjectiveScore())
				+ "][" + builder.toString() + "] " + tokenBoundaries + "]";
	}

}

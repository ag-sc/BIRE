package examples.tokenization;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import factors.FactorGraph;
import variables.AbstractState;

public class TokenState extends AbstractState<Sentence> {

	private static final DecimalFormat SCORE_FORMAT = new DecimalFormat("0.00000");
	// TODO make this a list and keep it sorted!
	public Tokenization tokenization;

	/**
	 * This class presents an implementation of an AbstractState for the
	 * tokenization of sentences. The state stores token boundaries as a
	 * collection of integer indices.
	 * 
	 * @param sentence
	 */
	public TokenState(Sentence sentence) {
		super(sentence);
		this.tokenization = new Tokenization();
	}

	public TokenState(TokenState state) {
		super(state);
		this.tokenization = new Tokenization(state.tokenization);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		int last = 0;
		for (BoundaryVariable b : tokenization.tokenBoundaries.values()) {
			builder.append(instance.text.subSequence(last, b.boundaryPosition));
			builder.append("|");
			last = b.boundaryPosition;
		}
		builder.append(instance.text.subSequence(last, instance.text.length()));
		return "TokenState [[" + SCORE_FORMAT.format(getModelScore()) + "][" + SCORE_FORMAT.format(getObjectiveScore())
				+ "][" + builder.toString() + "] " + tokenization.tokenBoundaries.values() + "]";
	}

}

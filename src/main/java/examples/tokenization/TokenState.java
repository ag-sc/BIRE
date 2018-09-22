package examples.tokenization;

import java.io.Serializable;
import java.text.DecimalFormat;

import variables.AbstractState;

public class TokenState extends AbstractState<String>implements Serializable {

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
	public TokenState(String sentence) {
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
		for (int b : tokenization.tokenBoundaries) {
			builder.append(instance.subSequence(last, b));
			builder.append("|");
			last = b;
		}
		builder.append(instance.subSequence(last, instance.length()));
		return "TokenState [[" + SCORE_FORMAT.format(getModelScore()) + "][" + SCORE_FORMAT.format(getObjectiveScore())
				+ "][" + builder.toString() + "] " + tokenization.tokenBoundaries + "]";
	}

}

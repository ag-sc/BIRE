package Corpus;

import java.util.List;

import Variables.State;

public class AnnotatedDocument extends Document {
	/**
	 * Since Entities only hold weak pointer via references to one another,
	 * using a Map is sensible to enable an efficient access to the entities.
	 */
	protected State goldState;

	public AnnotatedDocument(Corpus corpus, String content, List<Token> tokens,
			State goldState) {
		super(corpus, content, tokens);
		this.goldState = goldState;
		this.goldState.setDocument(this);
	}

	public State getGoldState() {
		return goldState;
	}

	@Override
	public String toString() {
		return "AnnotatedDocument [content=" + content + ", tokens=" + tokens
				+ ", goldState=" + goldState + "]";
	}

}

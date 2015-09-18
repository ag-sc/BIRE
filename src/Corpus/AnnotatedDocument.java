package Corpus;

import java.util.List;

import Variables.State;

public class AnnotatedDocument extends Document {
	/**
	 * This State object holds the human labeled, correct annotations, that are
	 * used during training and evaluation.
	 */
	protected State goldState;
	/**
	 * This state contains initial, human labeled annotations for this document,
	 * that are provided during training and testing (!). These annotations
	 * should not be changed since they already match some of the annotations in
	 * the goldState.
	 */
	protected State initialState;

	public AnnotatedDocument(Corpus<? extends AnnotatedDocument> corpus, String name, String content,
			List<Token> tokens) {
		super(corpus, name, content, tokens);
	}

	public State getGoldState() {
		return goldState;
	}

	public State getInitialState() {
		return initialState;
	}

	@Override
	public String toString() {
		return "AnnotatedDocument [content=" + content + ", tokens=" + tokens + ", goldState=" + goldState
				+ ", initialState=" + initialState + "]";
	}

	public void setGoldState(State goldState) {
		this.goldState = goldState;
	}

	public void setInitialState(State initialState) {
		this.initialState = initialState;
	}

}

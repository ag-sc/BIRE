package corpus;

import java.util.List;

import variables.AbstractState;

public class AnnotatedDocument<StateT extends AbstractState> extends Document {
	/**
	 * This State object holds the human labeled, correct annotations, that are
	 * used during training and evaluation.
	 */
	protected StateT goldState;
	/**
	 * This state contains initial, human labeled annotations for this document,
	 * that are provided during training and testing (!). These annotations
	 * should not be changed since they already match some of the annotations in
	 * the goldState.
	 */
	protected StateT initialState;

	public AnnotatedDocument(Corpus<? extends AnnotatedDocument<StateT>> corpus, String name, String content,
			List<Token> tokens) {
		super(corpus, name, content, tokens);
	}

	public StateT getGoldState() {
		return goldState;
	}

	public StateT getInitialState() {
		return initialState;
	}

	@Override
	public String toString() {
		return "AnnotatedDocument [content=" + content + ", tokens=" + tokens + ", goldState=" + goldState
				+ ", initialState=" + initialState + "]";
	}

	public void setGoldState(StateT goldState) {
		this.goldState = goldState;
	}

	public void setInitialState(StateT initialState) {
		this.initialState = initialState;
	}

}

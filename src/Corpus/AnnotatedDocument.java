package Corpus;

import java.util.Collection;
import java.util.List;

import Variables.EntityAnnotation;
import Variables.State;

public class AnnotatedDocument extends Document {
	/**
	 * Since Entities only hold weak pointer via references to one another,
	 * using a Map is sensible to enable an efficient access to the entities.
	 */
	private State goldState;

	public AnnotatedDocument(Corpus corpus, String content, List<Token> tokens,
			State goldState) {
		super(corpus, content, tokens);
		this.goldState = goldState;
	}

	public Collection<EntityAnnotation> getGoldEntities() {
		return goldState.getEntities();
	}

}

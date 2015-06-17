package Corpus;

import java.util.Collection;
import java.util.List;

import Variables.EntityAnnotation;
import Variables.EntityManager;

public class AnnotatedDocument extends Document {
	/**
	 * Since Entities only hold weak pointer via references to one another,
	 * using a Map is sensible to enable an efficient access to the entities.
	 */
	EntityManager goldEntityManager;

	public AnnotatedDocument(Corpus corpus, String content, List<Token> tokens,
			EntityManager goldEntityManager) {
		super(corpus, content, tokens);
		this.goldEntityManager = goldEntityManager;
	}

	public Collection<EntityAnnotation> getGoldEntities() {
		return goldEntityManager.getAllEntities();
	}

}

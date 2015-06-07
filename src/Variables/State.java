package Variables;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Corpus.Document;
import Corpus.Token;

public class State {

	EntityManager manager;
	Document document;

	double score;

	/**
	 * This Copy Constructor creates an exact copy of itself including all
	 * internal annotations.
	 * 
	 * @param state
	 */
	public State(State state) {
		this.manager = new EntityManager(manager);
		this.document = state.document;
	}

	public Collection<EntityAnnotation> getEntities() {
		return manager.getAllEntities();
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public void propagateChange() {
		for (EntityAnnotation entityAnnotation : manager.getAllEntities()) {
			entityAnnotation.propagateChange();
		}
	}

	public Document getDocument() {
		return document;
	}

	public void addEntityAnnotation(EntityAnnotation tokenAnnotation) {
		manager.addEntityAnnotation(tokenAnnotation);
	}

	public void removeEntityAnnotation(EntityAnnotation tokenAnnotation) {
		manager.removeEntityAnnotation(tokenAnnotation);
	}

	public EntityAnnotation getNewEntityInstance() {
		EntityAnnotation e = new EntityAnnotation(manager);
		return e;
	}

	public boolean tokenHasAnnotation(Token token) {
		return manager.tokenHasAnnotation(token);
	}

	public Set<String> getAnnotationForToken(Token token) {
		return manager.getAnnotationForToken(token);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(document.getContent());
		// TODO insert annotations into content
		return builder.toString();
	}

	public EntityAnnotation getEntity(String entityID) {
		return manager.getEntity(entityID);
	}

}

package Variables;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import Corpus.Document;
import Corpus.Token;

public class State {

	EntityManager manager;
	Document document;
	Map<Long, Set<String>> tokenToEntities = new HashMap<Long, Set<String>>();
	
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
		// TODO handle token-to-entity mapping
		manager.addEntityAnnotation(tokenAnnotation);
	}

	public void removeEntityAnnotation(EntityAnnotation tokenAnnotation) {
		// TODO handle token-to-entity mapping
		manager.removeEntityAnnotation(tokenAnnotation.id);
	}

	public EntityAnnotation getNewEntityInstance() {
		EntityAnnotation e = new EntityAnnotation(manager);
		return e;
	}

	public boolean tokenHasAnnotation(Token sampledToken) {
		// TODO Auto-generated method stub
		return false;
	}

	public EntityAnnotation getAnnotationForToken(Token sampledToken) {
		// TODO Auto-generated method stub
		return null;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(document.getContent());
		// TODO insert annotations into content
		return builder.toString();
	}

}

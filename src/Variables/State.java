package Variables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Corpus.Document;
import Corpus.Token;
import Factors.Factor;
import Learning.Model;
import Templates.Template;

public class State {

	public final String id;
	public State goldState;
	private EntityManager manager;
	private Document document;

	private double score;

	private State() {
		this.id = String.valueOf(Math.random()).substring(2, 7);
	}

	/**
	 * This Copy Constructor creates an exact copy of itself including all
	 * internal annotations.
	 * 
	 * @param state
	 */
	public State(State state) {
		this();
		this.manager = new EntityManager(state.manager);
		this.document = state.document;
	}

	public State(Document document) {
		this();
		this.document = document;
		this.manager = new EntityManager();
	}

	public State(Document document, Collection<EntityAnnotation> initialEntities) {
		this();
		this.document = document;
		this.manager = new EntityManager(initialEntities);
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

//	public void propagateChange() {
//		for (EntityAnnotation entityAnnotation : manager.getAllEntities()) {
//			entityAnnotation.propagateChange();
//		}
//	}

	public Document getDocument() {
		return document;
	}

	public void addEntityAnnotation(EntityAnnotation tokenAnnotation) {
		manager.addEntityAnnotation(tokenAnnotation);
	}

	public void removeEntityAnnotation(EntityAnnotation tokenAnnotation) {
		manager.removeEntityAnnotation(tokenAnnotation);
	}

	/**
	 * This method creates a new Entity with the EntityManager of this State as
	 * a parameter.
	 * 
	 * @return
	 */
	public EntityAnnotation getNewEntityInstanceForState() {
		EntityAnnotation e = new EntityAnnotation(manager);
		return e;
	}

	public boolean tokenHasAnnotation(Token token) {
		return manager.tokenHasAnnotation(token);
	}

	public Set<String> getAnnotationForToken(Token token) {
		return manager.getAnnotationForToken(token);
	}

	public EntityAnnotation getEntity(String entityID) {
		return manager.getEntity(entityID);
	}

	// <T1: tumor necrosis <T2: factor :T1>
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("State: ");
		builder.append(id);
		builder.append("| ");
		for (Token t : document.getTokens()) {
			Set<String> entities = manager.getAnnotationForToken(t);
			List<EntityAnnotation> begin = new ArrayList<EntityAnnotation>();
			List<EntityAnnotation> end = new ArrayList<EntityAnnotation>();
			for (String entityID : entities) {
				EntityAnnotation e = manager.getEntity(entityID);
				if (e.getBeginTokenIndex() == t.getIndex())
					begin.add(e);
				if (e.getEndTokenIndex() == t.getIndex())
					end.add(e);
			}
			if (!begin.isEmpty())
				buildPrefix(builder, begin);
			builder.append(t.getText());
			builder.append(" ");
			if (!end.isEmpty())
				buildSuffix(builder, end);
		}
		return builder.toString();
	}

	private void buildPrefix(StringBuilder builder, List<EntityAnnotation> begin) {
		builder.append("[");
		for (EntityAnnotation e : begin) {
			builder.append(e.getID());
			builder.append(":");
		}
		builder.append(" ");
	}

	private void buildSuffix(StringBuilder builder, List<EntityAnnotation> end) {
		for (EntityAnnotation e : end) {
			builder.append(":");
			builder.append(e.getID());
		}
		builder.append("]");
		builder.append(" ");
	}

	public Map<Integer, Set<String>> getTokenToEntityMapping() {
		return manager.getTokenToEntityMapping();
	}

	public void unroll(Model model) {
		for (Template t : model.getTemplates()) {
			t.applyTo(this);
		}
	}

	public double score() {
		double score = 0;
		for (EntityAnnotation e : manager.getAllEntities()) {
			// TODO factors may contribute multiple times to the score
			for (Factor f : e.getFactors()) {
				score *= f.score();
			}
		}
		return score;
	}

	public Collection<Factor> getFactors() {
		Set<Factor> factors = new HashSet<Factor>();
		for (EntityAnnotation e : manager.getAllEntities()) {
			factors.addAll(e.getFactors());
		}

		return factors;
	}
}

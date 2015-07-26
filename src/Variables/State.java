package Variables;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import Corpus.Document;
import Corpus.Token;
import Learning.Score;
import Logging.Log;

public class State implements Serializable {

	{
		Log.off();
	}
	public static final Comparator<State> modelScoreComparator = new Comparator<State>() {

		@Override
		public int compare(State s1, State s2) {
			return (int) -Math.signum(s1.getModelScore() - s2.getModelScore());
		}
	};
	public static final Comparator<State> objectiveScoreComparator = new Comparator<State>() {

		@Override
		public int compare(State s1, State s2) {
			return (int) -Math.signum(s1.getObjectiveFunctionScore().score
					- s2.getObjectiveFunctionScore().score);
		}
	};
	private static final String GENERATED_ENTITY_ID_PREFIX = "G";
	private static final DecimalFormat scoreFormat = new DecimalFormat(
			"0.00000");

	private static final DecimalFormat stateIDFormat = new DecimalFormat(
			"0000000");
	private int entityIdIndex = 0;
	private static int stateIdIndex = 0;
	/**
	 * Since Entities only hold weak pointer via references to one another,
	 * using a Map is sensible to enable an efficient access to the entities.
	 */
	private Map<String, EntityAnnotation> entities = new HashMap<String, EntityAnnotation>();;
	private Map<Integer, Set<String>> tokenToEntities = new HashMap<Integer, Set<String>>();

	private final String id;
	private Document document;
	public State goldState;

	private double modelScore = 1;
	private Score objectiveFunctionScore;

	public State() {
		this.id = generateStateID();
	}

	/**
	 * This Copy Constructor creates an exact copy of itself including all
	 * internal annotations.
	 * 
	 * @param state
	 */
	public State(State state) {
		this();
		this.entityIdIndex = state.entityIdIndex;
		this.document = state.document;
		this.goldState = state.goldState;
		for (EntityAnnotation e : state.entities.values()) {
			this.entities.put(e.getID(), new EntityAnnotation(this, e));
		}
		for (Entry<Integer, Set<String>> e : state.tokenToEntities.entrySet()) {
			this.tokenToEntities.put(e.getKey(),
					new HashSet<String>(e.getValue()));
		}
	}

	public State(Document document) {
		this();
		this.document = document;
	}

	public State(Document document, Collection<EntityAnnotation> initialEntities) {
		this();
		this.document = document;
		for (EntityAnnotation e : initialEntities) {
			addEntityAnnotation(e);
		}
	}

	/**
	 * Returns a previously computed score.
	 * 
	 * @return
	 */
	public double getModelScore() {
		return modelScore;
	}

	public Document getDocument() {
		return document;
	}

	/**
	 * This method creates a new Entity with the EntityManager of this State as
	 * a parameter.
	 * 
	 * @return
	 */
	public EntityAnnotation getNewEntityInstanceForState() {
		EntityAnnotation e = new EntityAnnotation(this);
		return e;
	}

	// /**
	// * Computes the current score by adding the scores of each factor.
	// *
	// * @param templates
	// *
	// * @return
	// */
	// public double recomputeModelScore(Collection<Template> templates) {
	// List<Factor> factors = getFactors();
	// if (factors.isEmpty()) {
	// score = 0;
	// Log.d("No factors: Model score = 0");
	// } else {
	// score = 1;
	// for (Factor f : factors) {
	// double factorScore = f.score();
	// Log.d("Factor score = %s", factorScore);
	// // FIXME Add or multiply factor scores??
	// score *= factorScore;
	// }
	// Log.d("Model score = %s using %s factors", score, getFactors()
	// .size());
	// }
	// return score;
	// }

	// public List<Factor> getFactors() {
	// // TODO Factors should not be tied to entities. Some Factors/Features
	// // may relate to the state as a whole instead of to individual entities.
	// // For example: if a state has no entities at all, it also has no
	// // factors/features that could be used to score it -> impossible to
	// // reward unannotated states.
	// List<Factor> factors = new ArrayList<Factor>();
	// // for (EntityAnnotation e : getEntities()) {
	// // factors.addAll(e.getFactors());
	// // }
	//
	// return factors;
	// }

	public void addEntityAnnotation(EntityAnnotation entity) {
		entities.put(entity.getID(), entity);
		addToTokenToEntityMapping(entity);
	}

	public void removeEntityAnnotation(EntityAnnotation entity) {
		entities.remove(entity.getID());
		removeFromTokenToEntityMapping(entity);
		removeReferencingArguments(entity);
	}

	public Set<String> getEntityIDs() {
		return entities.keySet();
	}

	public Collection<EntityAnnotation> getEntities() {
		return entities.values();
	}

	public EntityAnnotation getEntity(String id) {
		return entities.get(id);
	}

	public boolean tokenHasAnnotation(Token token) {
		Set<String> entities = tokenToEntities.get(token.getIndex());
		return entities != null && !entities.isEmpty();
	}

	public Set<String> getAnnotationsForToken(Token token) {
		return getAnnotationsForToken(token.getIndex());
	}

	public Set<String> getAnnotationsForToken(int tokenIndex) {
		Set<String> entities = tokenToEntities.get(tokenIndex);
		if (entities == null) {
			entities = new HashSet<String>();
			tokenToEntities.put(tokenIndex, entities);
		}
		return entities;
	}

	public void removeFromTokenToEntityMapping(EntityAnnotation entityAnnotation) {
		for (int i = entityAnnotation.getBeginTokenIndex(); i <= entityAnnotation
				.getEndTokenIndex(); i++) {
			Set<String> entities = tokenToEntities.get(i);
			if (entities == null) {
				entities = new HashSet<String>();
				tokenToEntities.put(i, entities);
			} else {
				entities.remove(entityAnnotation.getID());
			}
		}
	}

	public void addToTokenToEntityMapping(EntityAnnotation entityAnnotation) {
		for (int i = entityAnnotation.getBeginTokenIndex(); i <= entityAnnotation
				.getEndTokenIndex(); i++) {
			Set<String> entities = tokenToEntities.get(i);
			if (entities == null) {
				entities = new HashSet<String>();
				tokenToEntities.put(i, entities);
			}
			entities.add(entityAnnotation.getID());
		}
	}

	/**
	 * This function iterates over all entities and all of their arguments to
	 * remove all reference to the given entity.
	 * 
	 * @param removedEntity
	 */
	private void removeReferencingArguments(EntityAnnotation removedEntity) {
		for (EntityAnnotation e : entities.values()) {
			boolean referenceDeleted = true;
			while (referenceDeleted)
				referenceDeleted = e.getArguments().values()
						.remove(removedEntity.getID());
		}
	}

	public String generateStateID() {
		String id = stateIDFormat.format(stateIdIndex);
		stateIdIndex++;
		return id;
	}

	public String generateEntityID() {
		String id = GENERATED_ENTITY_ID_PREFIX + entityIdIndex;
		assert !entities.containsKey(id);
		entityIdIndex++;
		return id;
	}

	public Map<Integer, Set<String>> getTokenToEntityMapping() {
		return tokenToEntities;
	}

	public String getID() {
		return id;
	}

	// <T1-Protein: tumor necrosis <T2-Protein: factor :T1:T2>
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ID:");
		builder.append(id);
		builder.append(" [");
		builder.append(scoreFormat.format(modelScore));
		builder.append("]: ");
		for (Token t : document.getTokens()) {
			Set<String> entities = getAnnotationsForToken(t);
			List<EntityAnnotation> begin = new ArrayList<EntityAnnotation>();
			List<EntityAnnotation> end = new ArrayList<EntityAnnotation>();
			for (String entityID : entities) {
				EntityAnnotation e = getEntity(entityID);
				if (e.getBeginTokenIndex() == t.getIndex())
					begin.add(e);
				if (e.getEndTokenIndex() == t.getIndex())
					end.add(e);
			}
			if (!begin.isEmpty())
				buildTokenPrefix(builder, begin);
			builder.append(t.getText());
			builder.append(" ");
			if (!end.isEmpty())
				buildTokenSuffix(builder, end);
		}
		return builder.toString();
	}

	private void buildTokenPrefix(StringBuilder builder,
			List<EntityAnnotation> begin) {
		builder.append("[");
		for (EntityAnnotation e : begin) {
			builder.append(e.getID());
			builder.append("-");
			builder.append(e.getType().getName());
			builder.append("(");
			builder.append(e.getArguments());
			builder.append("):");
		}
		builder.append(" ");
	}

	private void buildTokenSuffix(StringBuilder builder,
			List<EntityAnnotation> end) {
		for (EntityAnnotation e : end) {
			builder.append(":");
			builder.append(e.getID());
		}
		builder.append("]");
		builder.append(" ");
	}

	public String toDetailedString() {
		StringBuilder builder = new StringBuilder();
		for (EntityAnnotation e : getEntities()) {
			builder.append(e);
			builder.append("\n");
		}
		for (Entry<Integer, Set<String>> e : getTokenToEntityMapping()
				.entrySet()) {
			builder.append(e);
			builder.append("\n");
		}
		return builder.toString().trim();
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public void setModelScore(double modelScore) {
		this.modelScore = modelScore;
	}

	public void setObjectiveFunctionScore(Score objectiveFunctionScore) {
		this.objectiveFunctionScore = objectiveFunctionScore;
	}

	public Score getObjectiveFunctionScore() {
		return objectiveFunctionScore;
	}

}

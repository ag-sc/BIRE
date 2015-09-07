package Variables;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;

import Changes.StateChange;
import Corpus.Document;
import Corpus.Token;
import Factors.FactorGraph;
import Learning.Score;
import Logging.Log;
import utility.EntityID;
import utility.StateID;

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
			return (int) -Math.signum(s1.getObjectiveScore().score
					- s2.getObjectiveScore().score);
		}
	};
	private static final String GENERATED_ENTITY_ID_PREFIX = "G";
	private static final DecimalFormat scoreFormat = new DecimalFormat(
			"0.00000");
	private static final DecimalFormat stateIDFormat = new DecimalFormat(
			"0000000");

	private static int stateIdIndex = 0;
	private int entityIdIndex = 0;

	private FactorGraph factorGraph = new FactorGraph();
	/**
	 * Since Entities only hold weak pointer via references to one another,
	 * using a Map is sensible to enable an efficient access to the entities.
	 */
	private Map<EntityID, EntityAnnotation> entities = new HashMap<EntityID, EntityAnnotation>();
	// TODO Guava might have a more convenient way to handle this mapping
	private Map<Integer, Set<EntityID>> tokenToEntities = new HashMap<Integer, Set<EntityID>>();

	/**
	 * The state needs to keep track of the changes that were made to its
	 * entities in order to allow for efficient computation of factors and their
	 * features. Note: The changes are not stored in the Entity object since it
	 * is more efficient to just clear this map instead of iterating over all
	 * entities and reset a field in order to mark all entities as unchanged.
	 */
	private Multimap<EntityID, StateChange> changedEntities = HashMultimap
			.create();
	private final StateID id;
	private Document document;
	private double modelScore = 1;
	private Score objectiveScore = new Score();

	private State() {
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
		for (EntityAnnotation e : state.entities.values()) {
			this.entities.put(e.getID(), new EntityAnnotation(this, e));
		}
		for (Entry<Integer, Set<EntityID>> e : state.tokenToEntities.entrySet()) {
			this.tokenToEntities.put(e.getKey(),
					new HashSet<EntityID>(e.getValue()));
		}
		this.modelScore = state.modelScore;
		this.objectiveScore = new Score(state.objectiveScore);
		this.changedEntities = HashMultimap.create(state.changedEntities);
		this.factorGraph = new FactorGraph(state.factorGraph);
	}

	public State(Document document) {
		this();
		this.document = document;
	}

	public State(Document document, Collection<EntityAnnotation> initialEntities) {
		this();
		this.document = document;
		for (EntityAnnotation e : initialEntities) {
			addEntity(e);
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

	public Multimap<EntityID, StateChange> getChangedEntities() {
		return changedEntities;
	}

	public void addEntity(EntityAnnotation entity) {
		Log.d("State %s: ADD new annotation: %s", this.getID(), entity);
		entities.put(entity.getID(), entity);
		addToTokenToEntityMapping(entity);
		changedEntities.put(entity.getID(), StateChange.ADD_ANNOTATION);
	}

	public void removeEntity(EntityAnnotation entity) {
		Log.d("State %s: REMOVE annotation: %s", this.getID(), entity);
		entities.remove(entity.getID());
		removeFromTokenToEntityMapping(entity);
		removeReferencingArguments(entity);
		changedEntities.put(entity.getID(), StateChange.REMOVE_ANNOTATION);
	}

	public void removeEntity(EntityID entityID) {
		EntityAnnotation entity = getEntity(entityID);
		if (entity != null) {
			Log.d("State %s: REMOVE annotation: %s", this.getID(), entity);
			entities.remove(entityID);
			removeFromTokenToEntityMapping(entity);
			removeReferencingArguments(entity);
			changedEntities.put(entityID, StateChange.REMOVE_ANNOTATION);
		}
	}

	public Set<EntityID> getEntityIDs() {
		return entities.keySet();
	}

	public Collection<EntityAnnotation> getEntities() {
		return entities.values();
	}

	public EntityAnnotation getEntity(EntityID id) {
		return entities.get(id);
	}

	public boolean tokenHasAnnotation(Token token) {
		Set<EntityID> entities = tokenToEntities.get(token.getIndex());
		return entities != null && !entities.isEmpty();
	}

	public Set<EntityID> getAnnotationsForToken(Token token) {
		return getAnnotationsForToken(token.getIndex());
	}

	public Set<EntityID> getAnnotationsForToken(int tokenIndex) {
		Set<EntityID> entities = tokenToEntities.get(tokenIndex);
		if (entities == null) {
			entities = new HashSet<EntityID>();
			tokenToEntities.put(tokenIndex, entities);
		}
		return entities;
	}

	protected void removeFromTokenToEntityMapping(
			EntityAnnotation entityAnnotation) {
		for (int i = entityAnnotation.getBeginTokenIndex(); i <= entityAnnotation
				.getEndTokenIndex(); i++) {
			Set<EntityID> entities = tokenToEntities.get(i);
			if (entities == null) {
				entities = new HashSet<EntityID>();
				tokenToEntities.put(i, entities);
			} else {
				entities.remove(entityAnnotation.getID());
			}
		}
	}

	protected void addToTokenToEntityMapping(EntityAnnotation entityAnnotation) {
		for (int i = entityAnnotation.getBeginTokenIndex(); i <= entityAnnotation
				.getEndTokenIndex(); i++) {
			Set<EntityID> entities = tokenToEntities.get(i);
			if (entities == null) {
				entities = new HashSet<EntityID>();
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
			Multimap<ArgumentRole, EntityID> arguments = e.getArguments();
			for (Entry<ArgumentRole, EntityID> entry : arguments.entries()) {
				if (entry.getValue().equals(removedEntity.getID())) {
					e.removeArgument(entry.getKey(), entry.getValue());
					/*
					 * Note: no need to mark entity as changed here. This will
					 * happen in the entity's removeArgument-method
					 */
				}
			}
		}
	}

	private StateID generateStateID() {
		String id = stateIDFormat.format(stateIdIndex);
		stateIdIndex++;
		return new StateID(id);
	}

	protected EntityID generateEntityID() {
		String id = GENERATED_ENTITY_ID_PREFIX + entityIdIndex;
		assert (!entities.containsKey(id));
		entityIdIndex++;
		return new EntityID(id);
	}

	public Map<Integer, Set<EntityID>> getTokenToEntityMapping() {
		return tokenToEntities;
	}

	public StateID getID() {
		return id;
	}

	public void setModelScore(double modelScore) {
		this.modelScore = modelScore;
	}

	public void setObjectiveScore(Score objectiveScore) {
		this.objectiveScore = objectiveScore;
	}

	public Score getObjectiveScore() {
		return objectiveScore;
	}

	public FactorGraph getFactorGraph() {
		return factorGraph;
	}

	public void onEntityChanged(EntityAnnotation entity, StateChange change) {
		changedEntities.put(entity.getID(), change);
	}

	public void markAsUnchanged() {
		changedEntities.clear();
	}

	// <T1-Protein: tumor necrosis <T2-Protein: factor :T1:T2>
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ID:");
		builder.append(id);
		builder.append(" [");
		builder.append(scoreFormat.format(modelScore));
		builder.append("]: ");
		builder.append(" [");
		builder.append(scoreFormat
				.format(objectiveScore != null ? objectiveScore.score : 0));
		builder.append("]: ");
		for (Token t : document.getTokens()) {
			Set<EntityID> entities = getAnnotationsForToken(t);
			List<EntityAnnotation> begin = new ArrayList<EntityAnnotation>();
			List<EntityAnnotation> end = new ArrayList<EntityAnnotation>();
			for (EntityID entityID : entities) {
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
		for (Entry<Integer, Set<EntityID>> e : getTokenToEntityMapping()
				.entrySet()) {
			builder.append(e);
			builder.append("\n");
		}
		return builder.toString().trim();
	}

}

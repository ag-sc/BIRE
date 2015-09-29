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

public class State implements IState, Serializable {

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
			return (int) -Math.signum(s1.getObjectiveScore().score - s2.getObjectiveScore().score);
		}
	};
	private static final String GENERATED_ENTITY_ID_PREFIX = "G";
	private static final DecimalFormat scoreFormat = new DecimalFormat("0.00000");
	private static final DecimalFormat stateIDFormat = new DecimalFormat("0000000");

	private static int stateIdIndex = 0;
	private int entityIdIndex = 0;

	private FactorGraph factorGraph = new FactorGraph();
	/**
	 * Since Entities only hold weak pointer via references to one another,
	 * using a Map is sensible to enable an efficient access to the entities.
	 */
	// private Map<EntityID, EntityAnnotation> entities = new HashMap<>();
	private Map<EntityID, ImmutableEntityAnnotation> immutableEntities = new HashMap<>();
	private Map<EntityID, MutableEntityAnnotation> mutableEntities = new HashMap<>();
	// TODO Guava might have a more convenient way to handle this mapping
	private Map<Integer, Set<EntityID>> tokenToEntities = new HashMap<>();

	/**
	 * The state needs to keep track of the changes that were made to its
	 * entities in order to allow for efficient computation of factors and their
	 * features. Note: The changes are not stored in the Entity object since it
	 * is more efficient to just clear this map instead of iterating over all
	 * entities and reset a field in order to mark all entities as unchanged.
	 */
	private Multimap<EntityID, StateChange> changedEntities = HashMultimap.create();
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
		for (ImmutableEntityAnnotation e : state.immutableEntities.values()) {
			this.immutableEntities.put(e.getID(), new ImmutableEntityAnnotation(this, e));
		}
		for (MutableEntityAnnotation e : state.mutableEntities.values()) {
			this.mutableEntities.put(e.getID(), new MutableEntityAnnotation(this, e));
		}
		for (Entry<Integer, Set<EntityID>> e : state.tokenToEntities.entrySet()) {
			this.tokenToEntities.put(e.getKey(), new HashSet<EntityID>(e.getValue()));
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

	public void addImmutableEntity(ImmutableEntityAnnotation entity) {
		Log.d("State %s: ADD new fixed annotation: %s", this.getID(), entity);
		immutableEntities.put(entity.getID(), entity);
		addToTokenToEntityMapping(entity);
		changedEntities.put(entity.getID(), StateChange.ADD_ANNOTATION);
	}

	public void addMutableEntity(MutableEntityAnnotation entity) {
		Log.d("State %s: ADD new annotation: %s", this.getID(), entity);
		mutableEntities.put(entity.getID(), entity);
		addToTokenToEntityMapping(entity);
		changedEntities.put(entity.getID(), StateChange.ADD_ANNOTATION);
	}

	public void removeMutableEntity(MutableEntityAnnotation entity) {
		Log.d("State %s: REMOVE annotation: %s", this.getID(), entity);
		mutableEntities.remove(entity.getID());
		mutableEntities.put(entity.getID(), entity);
		removeFromTokenToEntityMapping(entity);
		removeReferencingArguments(entity);
		changedEntities.put(entity.getID(), StateChange.REMOVE_ANNOTATION);
	}

	public void removeMutableEntity(EntityID entityID) {
		MutableEntityAnnotation entity = getMutableEntity(entityID);
		if (entity != null) {
			Log.d("State %s: REMOVE annotation: %s", this.getID(), entity);
			mutableEntities.remove(entityID);
			removeFromTokenToEntityMapping(entity);
			removeReferencingArguments(entity);
			changedEntities.put(entityID, StateChange.REMOVE_ANNOTATION);
		}
	}

	public Set<EntityID> getEntityIDs() {
		Set<EntityID> entityIDs = new HashSet<>();
		entityIDs.addAll(getImmutableEntityIDs());
		entityIDs.addAll(getMutableEntityIDs());
		return entityIDs;
	}

	public Set<EntityID> getMutableEntityIDs() {
		return mutableEntities.keySet();
	}

	public Set<EntityID> getImmutableEntityIDs() {
		return immutableEntities.keySet();
	}

	public Collection<AEntityAnnotation> getEntities() {
		Collection<AEntityAnnotation> entites = new ArrayList<>();
		entites.addAll(getMutableEntities());
		entites.addAll(getImmutableEntities());
		return entites;
	}

	public Collection<MutableEntityAnnotation> getMutableEntities() {
		return mutableEntities.values();
	}

	public Collection<ImmutableEntityAnnotation> getImmutableEntities() {
		return immutableEntities.values();
	}

	public AEntityAnnotation getEntity(EntityID id) {
		if (mutableEntities.containsKey(id))
			return mutableEntities.get(id);
		else
			return immutableEntities.get(id);
	}

	public MutableEntityAnnotation getMutableEntity(EntityID id) {
		return mutableEntities.get(id);
	}

	public ImmutableEntityAnnotation getImmutableEntity(EntityID id) {
		return immutableEntities.get(id);
	}

	public boolean tokenHasAnnotation(Token token) {
		Set<EntityID> entitiesForToken = tokenToEntities.get(token.getIndex());
		return entitiesForToken != null && !entitiesForToken.isEmpty();
	}

	public Set<EntityID> getAnnotationsForToken(Token token) {
		return getAnnotationsForToken(token.getIndex());
	}

	public Set<EntityID> getAnnotationsForToken(int tokenIndex) {
		Set<EntityID> entitiesForToken = tokenToEntities.get(tokenIndex);
		if (entitiesForToken == null) {
			entitiesForToken = new HashSet<EntityID>();
			tokenToEntities.put(tokenIndex, entitiesForToken);
		}
		return entitiesForToken;
	}

	protected void removeFromTokenToEntityMapping(AEntityAnnotation entityAnnotation) {
		for (int i = entityAnnotation.getBeginTokenIndex(); i < entityAnnotation.getEndTokenIndex(); i++) {
			Set<EntityID> entitiesForToken = tokenToEntities.get(i);
			if (entitiesForToken != null) {
				entitiesForToken.remove(entityAnnotation.getID());
			}
		}
	}

	protected void addToTokenToEntityMapping(AEntityAnnotation entityAnnotation) {
		for (int i = entityAnnotation.getBeginTokenIndex(); i < entityAnnotation.getEndTokenIndex(); i++) {
			Set<EntityID> entitiesForToken = tokenToEntities.get(i);
			if (entitiesForToken == null) {
				entitiesForToken = new HashSet<EntityID>();
				tokenToEntities.put(i, entitiesForToken);
			}
			entitiesForToken.add(entityAnnotation.getID());
		}
	}

	/**
	 * This function iterates over all entities and all of their arguments to
	 * remove all reference to the given entity.
	 * 
	 * @param removedEntity
	 */
	private void removeReferencingArguments(MutableEntityAnnotation removedEntity) {
		for (MutableEntityAnnotation e : mutableEntities.values()) {
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

	public void onEntityChanged(MutableEntityAnnotation entity, StateChange change) {
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
		builder.append(scoreFormat.format(objectiveScore != null ? objectiveScore.score : 0));
		builder.append("]: ");
		for (Token t : document.getTokens()) {
			Set<EntityID> entities = getAnnotationsForToken(t);
			List<AEntityAnnotation> begin = new ArrayList<>();
			List<AEntityAnnotation> end = new ArrayList<>();
			for (EntityID entityID : entities) {
				AEntityAnnotation e = getEntity(entityID);
				if (e.getBeginTokenIndex() == t.getIndex())
					begin.add(e);
				if (e.getEndTokenIndex() == t.getIndex() + 1)
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

	private void buildTokenPrefix(StringBuilder builder, List<AEntityAnnotation> begin) {
		builder.append("[");
		for (AEntityAnnotation e : begin) {
			builder.append(e.getID());
			builder.append("-");
			builder.append(e.getType().getName());
			builder.append("(");
			builder.append(e.getArguments());
			builder.append("):");
		}
		builder.append(" ");
	}

	private void buildTokenSuffix(StringBuilder builder, List<AEntityAnnotation> end) {
		for (AEntityAnnotation e : end) {
			builder.append(":");
			builder.append(e.getID());
		}
		builder.append("]");
		builder.append(" ");
	}

	public String toDetailedString() {
		StringBuilder builder = new StringBuilder();
		for (AEntityAnnotation e : getEntities()) {
			builder.append(e);
			builder.append("\n");
		}
		for (Entry<Integer, Set<EntityID>> e : getTokenToEntityMapping().entrySet()) {
			builder.append(e);
			builder.append("\n");
		}
		return builder.toString().trim();
	}

}

package Variables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Changes.StateChange;
import Corpus.Token;
import utility.EntityID;

public class EntityAnnotation extends AbstractAnnotation {

	private State state;
	private EntityID id;
	/**
	 * This number specifies the token index (!! not character offset) of the
	 * first token that this annotation references.
	 */
	private int beginTokenIndex;
	/**
	 * This number specifies the token index (!! not character offset) of the
	 * last token that this annotation references.
	 */
	private int endTokenIndex;

	private EntityType type;

	/**
	 * We need to keep weak references (IDs only) to other entities in order to
	 * enable an efficient cloning of states and their entities during the
	 * training phase of the model. The state in which this entity lives offers
	 * methods to resolve this weak reference.
	 */
	private Map<ArgumentRole, EntityID> arguments;

	/**
	 * This method should only be used by the managing State during a cloning
	 * process of a state.
	 * 
	 * @param state
	 * @param entityAnnotation
	 */
	protected EntityAnnotation(State state, EntityAnnotation entityAnnotation) {
		this(state, entityAnnotation.id, entityAnnotation.type,
				new HashMap<ArgumentRole, EntityID>(entityAnnotation.arguments), entityAnnotation.beginTokenIndex,
				entityAnnotation.endTokenIndex);
	}

	public EntityAnnotation(State state, EntityID id, EntityType entityType, Map<ArgumentRole, EntityID> arguments,
			int start, int end) {
		this.state = state;
		this.id = id;
		this.type = entityType;
		this.arguments = arguments;
		this.beginTokenIndex = start;
		this.endTokenIndex = end;
	}

	public EntityAnnotation(State state, EntityType entityType, Map<ArgumentRole, EntityID> arguments, int start,
			int end) {
		this(state, state.generateEntityID(), entityType, arguments, start, end);
	}

	public EntityAnnotation(State state, String id, EntityType entityType, Map<ArgumentRole, EntityID> arguments,
			int start, int end) {
		this(state, new EntityID(id), entityType, arguments, start, end);
	}

	public EntityAnnotation(State state, EntityType entityType, int start, int end) {
		this(state, state.generateEntityID(), entityType, new HashMap<ArgumentRole, EntityID>(), start, end);
	}

	public EntityAnnotation(State state, String id, EntityType entityType, int start, int end) {
		this(state, new EntityID(id), entityType, new HashMap<ArgumentRole, EntityID>(), start, end);
	}

	public EntityID getID() {
		return id;
	}

	public void setType(EntityType type) {
		this.type = type;
		state.onEntityChanged(this, StateChange.CHANGE_TYPE);
	}

	public EntityType getType() {
		return type;
	}

	public State getState() {
		return state;
	}

	public int getBeginTokenIndex() {
		return beginTokenIndex;
	}

	public void setBeginTokenIndex(int beginTokenIndex) {
		// TODO this handling of changes is not perfectly efficient and allows
		// errors and inconsistencies if applied wrongly
		state.removeFromTokenToEntityMapping(this);
		this.beginTokenIndex = beginTokenIndex;
		state.addToTokenToEntityMapping(this);
		state.onEntityChanged(this, StateChange.CHANGE_BOUNDARIES);
	}

	public int getEndTokenIndex() {
		return endTokenIndex;
	}

	public void setEndTokenIndex(int endTokenIndex) {
		// TODO this handling of changes is not perfectly efficient and allows
		// errors and inconsistencies if applied wrongly

		state.removeFromTokenToEntityMapping(this);
		this.endTokenIndex = endTokenIndex;
		state.addToTokenToEntityMapping(this);
		state.onEntityChanged(this, StateChange.CHANGE_BOUNDARIES);
	}

	public Map<ArgumentRole, EntityID> getArguments() {
		return new HashMap<>(arguments);
	}

	public void addArgument(ArgumentRole role, EntityID entityID) {
		arguments.put(role, entityID);
		state.onEntityChanged(this, StateChange.ADD_ARGUMENT);
	}

	public void removeArgument(ArgumentRole role) {
		arguments.remove(role);
		state.onEntityChanged(this, StateChange.REMOVE_ARGUMENT);
	}

	/**
	 * Returns the entity that is associated with the specified ID, using the
	 * predefined EntityManager.
	 * 
	 * @param id
	 * @return
	 */
	public EntityAnnotation getEntity(EntityID id) {
		return state.getEntity(id);
	}

	public List<Token> getTokens() {
		List<Token> tokens = new ArrayList<Token>();
		for (int i = beginTokenIndex; i <= endTokenIndex; i++)
			tokens.add(state.getDocument().getTokens().get(i));
		return tokens;
	}

	public String getText() {
		List<Token> tokens = getTokens();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < tokens.size(); i++) {
			Token token = tokens.get(i);
			builder.append(token.getText());
			if (i < tokens.size() - 1) {
				builder.append(" ");
			}
		}
		return builder.toString();
	}

	@Override
	public String toString() {
		return "EntityAnnotation [id=" + id + ", begin=" + beginTokenIndex + ", end=" + endTokenIndex + ", type="
				+ type.getName() + ", arguments=" + arguments + "]";
	}

	public String toDetailedString() {
		return "EntityAnnotation [id=" + id + ", begin=" + beginTokenIndex + ", end=" + endTokenIndex + " => \""
				+ getText() + "\", type=" + type.getName() + ", arguments=" + arguments + "]";
	}
}

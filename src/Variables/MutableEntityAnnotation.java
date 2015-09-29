package Variables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import Changes.StateChange;
import Corpus.Token;
import utility.EntityID;

public class MutableEntityAnnotation extends ImmutableEntityAnnotation {

	public MutableEntityAnnotation(State state, EntityType entityType, Multimap<ArgumentRole, EntityID> arguments,
			int start, int end) {
		super(state, entityType, arguments, start, end);
	}

	public MutableEntityAnnotation(State state, String id, EntityType entityType,
			Multimap<ArgumentRole, EntityID> arguments, int start, int end) {
		super(state, id, entityType, arguments, start, end);
	}

	public MutableEntityAnnotation(State state, EntityType entityType, int start, int end) {
		super(state, entityType, HashMultimap.create(), start, end);
	}

	public MutableEntityAnnotation(State state, String id, EntityType entityType, int start, int end) {
		super(state, id, entityType, HashMultimap.create(), start, end);
	}

	/**
	 * This method should only be used by the managing State during a cloning
	 * process of a state.
	 * 
	 * @param state
	 * @param entityAnnotation
	 */
	protected MutableEntityAnnotation(State state, MutableEntityAnnotation entityAnnotation) {
		super(state, entityAnnotation);
	}

	public void setType(EntityType type) {
		this.type = type;
		state.onEntityChanged(this, StateChange.CHANGE_TYPE);
	}

	public void setBeginTokenIndex(int beginTokenIndex) {
		// TODO this handling of changes is not perfectly efficient and allows
		// errors and inconsistencies if applied wrongly
		state.removeFromTokenToEntityMapping(this);
		this.beginTokenIndex = beginTokenIndex;
		state.addToTokenToEntityMapping(this);
		state.onEntityChanged(this, StateChange.CHANGE_BOUNDARIES);
	}

	public void setEndTokenIndex(int endTokenIndex) {
		// TODO this handling of changes is not perfectly efficient and allows
		// errors and inconsistencies if applied wrongly

		state.removeFromTokenToEntityMapping(this);
		this.endTokenIndex = endTokenIndex;
		state.addToTokenToEntityMapping(this);
		state.onEntityChanged(this, StateChange.CHANGE_BOUNDARIES);
	}

	public void addArgument(ArgumentRole role, EntityID entityID) {
		arguments.put(role, entityID);
		state.onEntityChanged(this, StateChange.ADD_ARGUMENT);
	}

	public void removeArgument(ArgumentRole role, EntityID entity) {
		arguments.remove(role, entity);
		state.onEntityChanged(this, StateChange.REMOVE_ARGUMENT);
	}

	@Override
	public String toString() {
		return "MutableEntityAnnotation [id=" + id + ", begin=" + beginTokenIndex + ", end=" + endTokenIndex + ", type="
				+ type.getName() + ", arguments=" + arguments + "]";
	}

	public String toDetailedString() {
		return "MutableEntityAnnotation [id=" + id + ", begin=" + beginTokenIndex + ", end=" + endTokenIndex + " => \""
				+ getText() + "\", type=" + type.getName() + ", arguments=" + arguments + "]";
	}
}

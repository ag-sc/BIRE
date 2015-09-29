package Variables;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import utility.EntityID;

public class ImmutableEntityAnnotation extends AEntityAnnotation {

	public ImmutableEntityAnnotation(State state, EntityType entityType, Multimap<ArgumentRole, EntityID> arguments,
			int start, int end) {
		super(state, entityType, arguments, start, end);
	}

	public ImmutableEntityAnnotation(State state, String id, EntityType entityType,
			Multimap<ArgumentRole, EntityID> arguments, int start, int end) {
		super(state, id, entityType, arguments, start, end);
	}

	public ImmutableEntityAnnotation(State state, EntityType entityType, int start, int end) {
		super(state, entityType, HashMultimap.create(), start, end);
	}

	public ImmutableEntityAnnotation(State state, String id, EntityType entityType, int start, int end) {
		super(state, id, entityType, HashMultimap.create(), start, end);
	}

	/**
	 * This method should only be used by the managing State during a cloning
	 * process of a state.
	 * 
	 * @param state
	 * @param entityAnnotation
	 */
	protected ImmutableEntityAnnotation(State state, ImmutableEntityAnnotation entityAnnotation) {
		super(state, entityAnnotation.id, entityAnnotation.type, HashMultimap.create(entityAnnotation.arguments),
				entityAnnotation.beginTokenIndex, entityAnnotation.endTokenIndex);
	}
}

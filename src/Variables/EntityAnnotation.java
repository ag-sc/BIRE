package Variables;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Changes.StateChange;
import Factors.Factor;
import Templates.Template;

public class EntityAnnotation extends Annotation {

	Map<Template, List<Factor>> factors = new HashMap<Template, List<Factor>>();

	StateChange change;

	// public void propagateChange() {
	// if (change != null) {
	// for (Factor factor : factors.values()) {
	// factor.propagateChange(this);
	// }
	// }
	// change = null;
	// }

	public Collection<Factor> getFactors() {
		// TODO return all factors from Map
		return null;
	}

	EntityManager manager;
	String id;
	/**
	 * This number specifies the token index (!! not character offset) of the
	 * first token that this annotation references.
	 */
	int beginTokenIndex;
	/**
	 * This number specifies the token index (!! not character offset) of the
	 * last token that this annotation references.
	 */
	int endTokenIndex;

	// String text;
	EntityType type;

	/**
	 * We need to keep weak references (IDs only) to other entities in order to
	 * enable an efficient cloning of states and their entities during the
	 * training phase of the model. The state in which this entity lives offers
	 * methods to resolve this weak reference.
	 */
	Map<String, String> arguments;

	public EntityAnnotation(EntityManager manager) {
		this(manager, manager.generateID());
	}

	public EntityAnnotation(EntityManager manager, String id) {
		this.manager = manager;
		this.id = id;
	}

	/**
	 * This method should only be used by the managing EntityManger during a
	 * clone process of a state. The cloned Entity is added to the given
	 * EnityManagers Collection of entities.
	 * 
	 * @param manager
	 * @param entityAnnotation
	 */
	protected EntityAnnotation(EntityManager manager,
			EntityAnnotation entityAnnotation) {
		this.manager = manager;
		this.id = entityAnnotation.id;
		this.beginTokenIndex = entityAnnotation.beginTokenIndex;
		this.endTokenIndex = entityAnnotation.endTokenIndex;
		// this.text = entityAnnotation.text;
		this.type = entityAnnotation.type;
		this.arguments = new HashMap<String, String>(entityAnnotation.arguments);
	}

	public void setManager(EntityManager manager) {
		this.manager = manager;
	}

	/**
	 * This method is needed in the process of parsing/creating the annotations.
	 * 
	 * @param entityType
	 * @param start
	 * @param end
	 * @param text
	 */
	public void init(EntityType entityType, int start, int end) {
		init(entityType, new HashMap<String, String>(), start, end);
	}

	/**
	 * This method is needed in the process of parsing/creating the annotations.
	 * 
	 * @param entityType
	 * @param start
	 * @param end
	 * @param text
	 */
	public void init(EntityType entityType, Map<String, String> arguments,
			int start, int end) {
		this.type = entityType;
		this.arguments = arguments;
		this.beginTokenIndex = start;
		this.endTokenIndex = end;
		// this.text = text;
	}

	public String getID() {
		return id;
	}

	public void setType(EntityType type) {
		this.type = type;
	}

	public EntityType getType() {
		return type;
	}

	public int getBeginTokenIndex() {
		return beginTokenIndex;
	}

	public void setBeginTokenIndex(int beginTokenIndex) {
		// TODO this handling of changes is not perfectly efficient and allows
		// errors and inconsistencies if applied wrongly
		manager.removeFromTokenToEntityMapping(this);
		this.beginTokenIndex = beginTokenIndex;
		manager.addToTokenToEntityMapping(this);
		change = StateChange.BOUNDARIES_CHANGED;
	}

	public int getEndTokenIndex() {
		return endTokenIndex;
	}

	public void setEndTokenIndex(int endTokenIndex) {
		// TODO this handling of changes is not perfectly efficient and allows
		// errors and inconsistencies if applied wrongly

		manager.removeFromTokenToEntityMapping(this);
		this.endTokenIndex = endTokenIndex;
		manager.addToTokenToEntityMapping(this);
		change = StateChange.BOUNDARIES_CHANGED;
	}

	public Map<String, String> getArguments() {
		return arguments;
	}

	public String addArgument(String role, String entityID) {
		return arguments.put(role, entityID);
	}

	public void removeArgument(String role) {
		arguments.remove(role);
	}

	@Override
	public String toString() {
		return "EntityAnnotation [id=" + id + ", begin=" + beginTokenIndex
				+ ", end=" + endTokenIndex + ", type=" + type.getType()
				+ ", arguments=" + arguments + "]";
	}

	/**
	 * Returns the entity that is associated with the specified ID, using the
	 * predefined EntityManager.
	 * 
	 * @param id
	 * @return
	 */
	public EntityAnnotation getEntity(String id) {
		return manager.getEntity(id);
	}

	public void add(Template template, List<Factor> factors) {
		this.factors.put(template, factors);
	}
}

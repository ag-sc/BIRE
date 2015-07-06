package Variables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import Changes.StateChange;
import Corpus.Token;
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
		Collection<Factor> allFactors = new HashSet<Factor>();
		for (List<Factor> list : factors.values()) {
			allFactors.addAll(list);
		}
		return allFactors;
	}

	private State state;
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

	public EntityAnnotation(State state) {
		this(state, state.generateEntityID());
	}

	public EntityAnnotation(State state, String id) {
		this.state = state;
		this.id = id;
	}

	/**
	 * This method should only be used by the managing EntityManger during a
	 * clone process of a state. The cloned Entity is added to the given
	 * EnityManagers Collection of entities.
	 * 
	 * @param state
	 * @param entityAnnotation
	 */
	protected EntityAnnotation(State state, EntityAnnotation entityAnnotation) {
		this.state = state;
		this.id = entityAnnotation.id;
		this.beginTokenIndex = entityAnnotation.beginTokenIndex;
		this.endTokenIndex = entityAnnotation.endTokenIndex;
		// this.text = entityAnnotation.text;
		this.type = entityAnnotation.type;
		this.arguments = new HashMap<String, String>(entityAnnotation.arguments);
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
		change = StateChange.CHANGE_BOUNDRARIES;
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
		change = StateChange.CHANGE_BOUNDRARIES;
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
				+ ", end=" + endTokenIndex + ", type=" + type.getName()
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
		return state.getEntity(id);
	}

	public void addFactors(Template template, List<Factor> factors) {
		this.factors.put(template, factors);
	}

	public boolean isChanged() {
		return true;
	}

	public List<Token> getTokens() {
		List<Token> tokens = new ArrayList<Token>();
		for (int i = beginTokenIndex; i < endTokenIndex; i++)
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
}

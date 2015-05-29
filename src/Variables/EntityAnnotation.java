package Variables;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Changes.StateChange;

public class EntityAnnotation extends Annotation {

	String id;
	int begin;
	int end;

	String text;

	EntityType type;

	Map<String, EntityAnnotation> arguments = new HashMap<String, EntityAnnotation>();

	public EntityAnnotation(String id) {
		this.id = id;
	}

	/**
	 * This method is needed in the process of parsing/creating the annotations.
	 * 
	 * @param entityType
	 * @param start
	 * @param end
	 * @param text
	 */
	public void init(EntityType entityType, int start, int end, String text) {
		this.begin = start;
		this.end = end;
		this.text = text;
		this.type = entityType;
	}

	public void setBegin(int begin) {
		this.begin = begin;
		change = StateChange.BOUNDARIES_CHANGED;
	}

	public void setEnd(int end) {
		this.end = end;
		change = StateChange.BOUNDARIES_CHANGED;
	}

	public String getID() {
		return id;
	}

	@Override
	public String toString() {
		return "EntityAnnotation [id=" + id + ", type=" + type + ", begin="
				+ begin + ", end=" + end + ", text=" + text + "]";
	}

	public EntityType getType() {
		return type;
	}

	public int getBegin() {
		return begin;
	}

	public int getEnd() {
		return end;
	}

	public Map<String, EntityAnnotation> getArguments() {
		return arguments;
	}
}

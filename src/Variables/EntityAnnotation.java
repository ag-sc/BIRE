package Variables;

import Changes.StateChange;

public class EntityAnnotation extends Annotation {

	String id;
	int begin;
	int end;

	String text;

	EntityType type;

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

}

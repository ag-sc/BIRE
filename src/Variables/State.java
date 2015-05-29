package Variables;

import java.util.ArrayList;
import java.util.List;

import Corpus.Document;

public class State {

	Document document;
	List<EntityAnnotation> entities;
	double score;

	public State(State state) {
		entities = new ArrayList<EntityAnnotation>();

		// TODO clone lists
	}

	public List<EntityAnnotation> getEntities() {
		return entities;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public void propagateChange() {
		for (EntityAnnotation entityAnnotation : entities) {
			entityAnnotation.propagateChange();
		}
	}

	public String toString() {
		// TODO document text + annotations
		return null;
	}
}

package Variables;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import Changes.StateChange;
import Factors.Factor;
import Factors.FactorGraph;

public class State {

	List<EntityAnnotation> entities;
	List<RelationAnnotation> relations;
	double score;

	public State(State state) {
		entities = new ArrayList<EntityAnnotation>();
		relations = new ArrayList<RelationAnnotation>();

		// TODO clone lists
	}

	public List<EntityAnnotation> getEntities() {
		return entities;
	}

	public List<RelationAnnotation> getRelations() {
		return relations;
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
		for (RelationAnnotation relationAnnotation : relations) {
			relationAnnotation.propagateChange();
		}
	}

}

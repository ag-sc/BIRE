package Templates;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Corpus.Token;
import Factors.Factor;
import Learning.Vector;
import Variables.EntityAnnotation;
import Variables.EntityType;
import Variables.State;

public class RelationTemplate extends Template {

	@Override
	public void applyTo(State state) {
		Factor factor;
		for (EntityAnnotation e : state.getEntities()) {
			if (e.isChanged()) {
				EntityType entityType = e.getType();
				String entityAsText = e.getText();
				Map<String, String> arguments = e.getArguments();
				for (Entry<String, String> a : arguments.entrySet()) {

					String argRole = a.getKey();
					EntityAnnotation argEntity = state.getEntity(a.getValue());
					EntityType argType = argEntity.getType();

					factor = new Factor(this);
					Vector featureVector = new Vector();

					/*
					 * The next few features are always present for each
					 * individual token, thus, they always have a value of 1
					 */
					featureVector.set("ENTITY_TYPE=" + entityType.getName()
							+ " & ARG_TYPE=" + argType.getName(), 1.0);
					featureVector.set("ENTITY_TYPE=" + entityType.getName()
							+ " & ARG_ROLE=" + argRole, 1.0);
					featureVector.set("ENTITY_TYPE=" + entityType.getName()
							+ " & ARG_TYPE=" + argType.getName()
							+ " & ARG_ROLE=" + argRole, 1.0);

					featureVector.set("DISTANCE_FROM_ENTITY=" + entityAsText
							+ "_TO_ARGUMENT_ROLE=" + argRole,
							distance(e, argEntity));
					featureVector.set("DISTANCE_FROM_ENTITY_TO_ARGUMENT_ROLE="
							+ argRole, distance(e, argEntity));

					featureVector.set("ENTITY=" + entityAsText
							+ "_BEFORE_ARGUMENT_ROLE=" + argRole,
							isBefore(e, argEntity));
					featureVector.set("ENTITY_BEFORE_ARGUMENT_ROLE=" + argRole,
							isBefore(e, argEntity));

					featureVector.set("ENTITY=" + entityAsText
							+ "_AFTER_ARGUMENT_ROLE=" + argRole,
							isAfter(e, argEntity));
					featureVector.set("ENTITY_AFTER_ARGUMENT_ROLE=" + argRole,
							isAfter(e, argEntity));

					factor.setFeatures(featureVector);
				}

			}
		}
	}

	private double isBefore(EntityAnnotation e1, EntityAnnotation e2) {
		return e1.getEndTokenIndex() < e2.getBeginTokenIndex() ? 1.0 : 0;
	}

	private double isAfter(EntityAnnotation e1, EntityAnnotation e2) {
		return e2.getEndTokenIndex() < e1.getBeginTokenIndex() ? 1.0 : 0;
	}

	private double distance(EntityAnnotation e1, EntityAnnotation e2) {
		// TODO test implementation of entity distance
		return Math.max(e2.getBeginTokenIndex() - e1.getEndTokenIndex(),
				e1.getBeginTokenIndex() - e2.getEndTokenIndex());
	}
}

package Templates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Factors.Factor;
import Learning.Vector;
import Logging.Log;
import Variables.EntityAnnotation;
import Variables.EntityType;
import Variables.State;

public class RelationTemplate extends Template {
	{
		Log.off();
	}

	@Override
	public List<Factor> generateFactors(State state) {
		List<Factor> factors = new ArrayList<Factor>();
		for (EntityAnnotation e : state.getEntities()) {
			// TODO this function has to consider that that some entities need
			// to be updated when other entities change, since the computation
			// of some features does not solely rely on this particualr entity
			if (e.isChanged()) {
				Log.d("Add features to entity %s (\"%s\"):", e.getID(),
						e.getText());
				Factor factor = new Factor(this);
				factors.add(factor);
				Vector featureVector = new Vector();
				factor.setFeatures(featureVector);

				EntityType entityType = e.getType();
				String entityAsText = e.getText();
				Map<String, String> arguments = e.getArguments();
				for (Entry<String, String> a : arguments.entrySet()) {

					String argRole = a.getKey();
					EntityAnnotation argEntity = state.getEntity(a.getValue());
					EntityType argType = argEntity.getType();

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

					for (int i = 0; i < 4; i++) {
						double distanceFeatureValue = Math.abs(distance(e,
								argEntity)) > i ? 1 : 0;
						featureVector.set("DISTANCE_FROM_ENTITY="
								+ entityAsText + "_TO_ARGUMENT_ROLE=" + argRole
								+ ">" + i, distanceFeatureValue);
						// featureVector.set(
						// "DISTANCE_FROM_ENTITY_TO_ARGUMENT_ROLE="
						// + argRole + ">" + i,
						// distanceFeatureValue);
					}

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
				}
				Log.d("Features for entity %s (\"%s\"): %s", e.getID(),
						e.getText(), featureVector);
			}
		}
		return factors;
	}

	private double isBefore(EntityAnnotation e1, EntityAnnotation e2) {
		return e1.getEndTokenIndex() < e2.getBeginTokenIndex() ? 1.0 : 0;
	}

	private double isAfter(EntityAnnotation e1, EntityAnnotation e2) {
		return e2.getEndTokenIndex() < e1.getBeginTokenIndex() ? 1.0 : 0;
	}

	private int distance(EntityAnnotation e1, EntityAnnotation e2) {
		// TODO test implementation of entity distance
		return Math.max(e2.getBeginTokenIndex() - e1.getEndTokenIndex(),
				e1.getBeginTokenIndex() - e2.getEndTokenIndex());
	}
}

package Templates;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import Changes.StateChange;
import Factors.Factor;
import Learning.Vector;
import Logging.Log;
import Templates.variablesets.EntityAndArgumentVariableSet;
import Templates.variablesets.VariableSet;
import Variables.AEntityAnnotation;
import Variables.ArgumentRole;
import Variables.EntityType;
import Variables.State;
import utility.EntityID;

public class RelationTemplate extends Template<State>implements Serializable {
	{
		Log.off();
	}

	public final Set<StateChange> relevantChanges = Sets.newHashSet(StateChange.values());

	@Override
	public Factor generateFactor(State state, VariableSet genericVariables) {
		// TODO features on unannotated tokens (thus, type/name = "null") might
		// be useful
		if (genericVariables instanceof EntityAndArgumentVariableSet) {
			EntityAndArgumentVariableSet variables = (EntityAndArgumentVariableSet) genericVariables;
			AEntityAnnotation mainEntity = state.getEntity(variables.getMainEntityID());
			AEntityAnnotation argEntity = state.getEntity(variables.getArgumentEntityID());
			ArgumentRole argRole = variables.getArgumentRole();

			// find the role for the supposed argument entity
			// /*
			// * TODO only works if an entity cannot serve as argument in
			// * different roles simultaneously for a given "parent" entity
			// */
			// List<ArgumentRole> roles = new ArrayList<>();
			// for (Entry<ArgumentRole, EntityID> e :
			// mainEntity.getArguments().entries()) {
			// if (argEntity.getID().equals(e.getValue())) {
			// roles.add(e.getKey());
			// }
			// }
			// if (roles.size() > 1)
			// Log.w("%s: Generating factor for VariableSet %s and State %s. In
			// entity %s, argument %s serves in multiple roles: %s",
			// this.getClass().getSimpleName(), variables, state.getID(),
			// mainEntity, argEntity.getID(),
			// roles);
			// // TODO what if multiple roles exist? Factor for each single one?
			// if (!roles.isEmpty()) {
			// ArgumentRole argRole = roles.get(0);
			EntityType argType = argEntity.getType();

			Log.d("%s: Add features to entity %s (\"%s\"):", this.getClass().getSimpleName(), mainEntity.getID(),
					mainEntity.getText());

			Vector featureVector = new Vector();

			EntityType entityType = mainEntity.getType();
			String entityAsText = mainEntity.getText();

			// TODO the next few features are (partly) unnecessary due
			// to CorpusConfig specification
			/*
			 * The next few features are always present for each individual
			 * token, thus, they always have a value of 1
			 */
			featureVector.set("ENTITY_TYPE=" + entityType.getName() + " & ARG_TYPE=" + argType.getName(), 1.0);
			featureVector.set("ENTITY_TYPE=" + entityType.getName() + " & ARG_ROLE=" + argRole, 1.0);
			featureVector.set("ENTITY_TYPE=" + entityType.getName() + " & ARG_TYPE=" + argType.getName()
					+ " & ARG_ROLE=" + argRole, 1.0);

			for (int i = 0; i < 4; i++) {
				double distanceFeatureValue = Math.abs(distance(mainEntity, argEntity)) > i ? 1 : 0;
				featureVector.set("DISTANCE_FROM_ENTITY=" + entityAsText + "_TO_ARGUMENT_ROLE=" + argRole + ">" + i,
						distanceFeatureValue);
				// featureVector.set(
				// "DISTANCE_FROM_ENTITY_TO_ARGUMENT_ROLE="
				// + argRole + ">" + i,
				// distanceFeatureValue);
			}

			featureVector.set("ENTITY=" + entityAsText + "_BEFORE_ARGUMENT_ROLE=" + argRole,
					isBefore(mainEntity, argEntity));
			featureVector.set("ENTITY_BEFORE_ARGUMENT_ROLE=" + argRole, isBefore(mainEntity, argEntity));

			featureVector.set("ENTITY=" + entityAsText + "_AFTER_ARGUMENT_ROLE=" + argRole,
					isAfter(mainEntity, argEntity));
			featureVector.set("ENTITY_AFTER_ARGUMENT_ROLE=" + argRole, isAfter(mainEntity, argEntity));

			Log.d("%s: Features for entity %s (\"%s\"): %s", this.getClass().getSimpleName(), mainEntity.getID(),
					mainEntity.getText(), featureVector);
			Factor factor = new Factor(this);
			factor.setFeatures(featureVector);
			return factor;
			// }
		}
		return null;

	}

	private double isBefore(AEntityAnnotation e1, AEntityAnnotation e2) {
		return e1.getEndTokenIndex() <= e2.getBeginTokenIndex() ? 1.0 : 0;
	}

	private double isAfter(AEntityAnnotation e1, AEntityAnnotation e2) {
		return e2.getEndTokenIndex() <= e1.getBeginTokenIndex() ? 1.0 : 0;
	}

	private int distance(AEntityAnnotation e1, AEntityAnnotation e2) {
		// TODO test implementation of entity distance
		return Math.max(e2.getBeginTokenIndex() - e1.getEndTokenIndex() + 1,
				e1.getBeginTokenIndex() - e2.getEndTokenIndex() + 1);
	}

	@Override
	protected boolean isRelevantChange(StateChange value) {
		return relevantChanges.contains(value);
	}

	@Override
	protected Set<VariableSet> getVariableSets(State state) {
		Set<VariableSet> variableSets = new HashSet<>();
		for (AEntityAnnotation entity : state.getEntities()) {
			Multimap<ArgumentRole, EntityID> arguments = entity.getArguments();
			for (Entry<ArgumentRole, EntityID> a : arguments.entries()) {
				/*
				 * TODO if it is possible, that an entity serves as multiple
				 * arguments for a single entity (but with different roles e.g.
				 * as theme and cause simultaneously), then this set of variable
				 * set won't cover all configurations (since it neglects the
				 * role)
				 */
				variableSets.add(new EntityAndArgumentVariableSet(this, entity.getID(), a.getKey(), a.getValue()));
			}
		}
		return variableSets;
	}
}

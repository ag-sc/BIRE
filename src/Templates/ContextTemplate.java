package Templates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Changes.StateChange;
import Corpus.Token;
import Factors.Factor;
import Factors.FactorGraph;
import Learning.Vector;
import Logging.Log;
import Templates.variablesets.SingleEntityVariableSet;
import Templates.variablesets.VariableSet;
import Variables.EntityAnnotation;
import Variables.State;

public class ContextTemplate extends Template implements Serializable {

	{
		Log.off();
	}

	@Override
	public Factor generateFactor(State state, VariableSet genericVariables) {
		if (genericVariables instanceof SingleEntityVariableSet) {

			SingleEntityVariableSet variables = (SingleEntityVariableSet) genericVariables;
			EntityAnnotation entity = state.getEntity(variables.entityID);
			Log.d("Add features to entity %s (\"%s\"):", entity.getID(), entity.getText());

			Vector featureVector = new Vector();

			List<Token> tokens = entity.getTokens();
			Token first = tokens.get(0);
			Token last = tokens.get(tokens.size() - 1);

			String entityType = "ENTITY_TYPE=" + entity.getType().getName() + "_";
			featureVector.set("FIRST_TOKEN_EQUALS=" + first.getText(), 1.0);
			featureVector.set("LAST_TOKEN_EQUALS=" + last.getText(), 1.0);
			featureVector.set(entityType + "FIRST_TOKEN_EQUALS=" + first.getText(), 1.0);
			featureVector.set(entityType + "LAST_TOKEN_EQUALS=" + last.getText(), 1.0);

			int[] tokenOffsets = { -2, -1, 1, 2 };
			for (int i : tokenOffsets) {
				Token tokenAt = Features.getTokenRelativeToEntity(state, entity, i);
				if (tokenAt != null) {
					String at = i > 0 ? "+" + String.valueOf(i) : String.valueOf(i);
					featureVector.set("TOKEN@" + at + "_EQUALS=" + tokenAt.getText(), 1.0);
					featureVector.set(entityType + "TOKEN@" + at + "_EQUALS=" + tokenAt.getText(), 1.0);
				}
			}

			Log.d("Features for entity %s (\"%s\"): %s", entity.getID(), entity.getText(), featureVector);
			Factor factor = new Factor(this);
			factor.setFeatures(featureVector);
			return factor;
		}
		return null;
	}

	@Override
	protected boolean isRelevantChange(StateChange value) {
		return true;
	}

	@Override
	protected Set<VariableSet> getVariableSets(State state) {
		Set<VariableSet> variableSets = new HashSet<>();
		for (EntityAnnotation entity : state.getEntities()) {
			variableSets.add(new SingleEntityVariableSet(this, entity.getID()));
		}
		return variableSets;
	}
}

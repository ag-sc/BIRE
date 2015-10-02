package Templates;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;

import Changes.StateChange;
import Corpus.Token;
import Factors.Factor;
import Learning.Vector;
import Logging.Log;
import Templates.variablesets.SingleEntityVariableSet;
import Templates.variablesets.VariableSet;
import Variables.AEntityAnnotation;
import Variables.State;
import utility.EntityID;

public class MorphologicalTemplate extends Template<State>implements Serializable {

	private Logger log = LogManager.getLogger(getClass());

	{
		Log.off();
	}

	public final Set<StateChange> relevantChanges = Sets.newHashSet(StateChange.ADD_ANNOTATION,
			StateChange.CHANGE_BOUNDARIES, StateChange.CHANGE_TYPE, StateChange.REMOVE_ANNOTATION);

	@Override
	public Factor generateFactor(State state, VariableSet genericVariables) {
		// TODO features on unannotated tokens (thus, type/name = "null") might
		// be useful
		if (genericVariables instanceof SingleEntityVariableSet) {

			SingleEntityVariableSet variables = (SingleEntityVariableSet) genericVariables;
			AEntityAnnotation entity = state.getEntity(variables.entityID);
			log.debug("%s: Add features to entity %s (\"%s\"):", this.getClass().getSimpleName(), entity.getID(),
					entity.getText());
			Log.d("%s: Add features to entity %s (\"%s\"):", this.getClass().getSimpleName(), entity.getID(),
					entity.getText());
			Vector featureVector = new Vector();

			List<Token> tokens = entity.getTokens();
			Token first = tokens.get(0);
			Token last = tokens.get(tokens.size() - 1);
			String entityType = "ENTITY_TYPE=" + entity.getType().getName() + "_";
			featureVector.set(entityType + "ALL_TOKENS_INIT_CAP", Features.StartsWithCapital.all(tokens));
			featureVector.set(entityType + "AT_LEAST_ONE_TOKEN_INIT_CAP", Features.StartsWithCapital.any(tokens));
			featureVector.set(entityType + "FIRST_TOKEN_INIT_CAP", Features.StartsWithCapital.first(tokens));
			featureVector.set(entityType + "LAST_TOKEN_INIT_CAP", Features.StartsWithCapital.last(tokens));

			featureVector.set(entityType + "ALL_TOKENS_ALL_CAP", Features.AllCapital.all(tokens));
			featureVector.set(entityType + "AT_LEAST_ONE_TOKEN_ALL_CAP", Features.AllCapital.any(tokens));
			featureVector.set(entityType + "FIRST_TOKEN_ALL_CAP", Features.AllCapital.first(tokens));
			featureVector.set(entityType + "LAST_TOKEN_ALL_CAP", Features.AllCapital.last(tokens));

			featureVector.set(entityType + "ALL_TOKENS_CONTAIN_DIGIT", Features.ContainsDigit.all(tokens));
			featureVector.set(entityType + "AT_LEAST_ONE_TOKEN_CONTAINS_DIGIT", Features.ContainsDigit.any(tokens));
			featureVector.set(entityType + "FIRST_TOKEN_CONTAINS_DIGIT", Features.ContainsDigit.first(tokens));
			featureVector.set(entityType + "LAST_TOKEN_CONTAINS_DIGIT", Features.ContainsDigit.last(tokens));

			featureVector.set(entityType + "AT_LEAST_ONE_TOKEN_CONTAINS_HYPHEN", Features.ContainsHyphen.any(tokens));
			featureVector.set(entityType + "AT_LEAST_ONE_TOKEN_CONTAINS_PUNCTUATION",
					Features.ContainsPunctuation.any(tokens));
			featureVector.set(entityType + "AT_LEAST_ONE_TOKEN_CONTAINS_GREEK_SYMBOL",
					Features.ContainsGreek.any(tokens));

			/*
			 * The following features are always present for each individual
			 * token, thus, they always have a value of 1
			 */

			int[] suffixLengths = { 2, 3 };
			for (int i : suffixLengths) {
				featureVector.set(entityType + "LAST_TOKEN_SUFFIX_" + i + "=" + suffix(last.getText(), i), 1.0);
				featureVector.set(entityType + "FIRST_TOKEN_SUFFIX_" + i + "=" + suffix(first.getText(), i), 1.0);
			}

			int[] prefixLengths = { 2, 3 };
			for (int i : prefixLengths) {
				featureVector.set(entityType + "LAST_TOKEN_PREFIX_" + i + "=" + prefix(last.getText(), i), 1.0);
				featureVector.set(entityType + "FIRST_TOKEN_PREFIX_" + i + "=" + prefix(first.getText(), i), 1.0);
			}

			Log.d("%s: Features for entity %s (\"%s\"): %s", this.getClass().getSimpleName(), entity.getID(),
					entity.getText(), featureVector);

			Factor factor = new Factor(this);
			factor.setFeatures(featureVector);
			return factor;
		}
		return null;
	}

	private String suffix(String text, int i) {
		if (i > 0)
			return text.substring(Math.max(0, text.length() - i));
		else
			return "";
	}

	private String prefix(String text, int i) {
		if (i > 0) {
			return text.substring(0, Math.min(text.length(), i));
		} else
			return "";
	}

	@Override
	protected boolean isRelevantChange(StateChange value) {
		return relevantChanges.contains(value);
	}

	@Override
	protected Set<VariableSet> getVariableSets(State state) {
		Set<VariableSet> variableSets = new HashSet<>();
		for (EntityID entityID : state.getEntityIDs()) {
			variableSets.add(new SingleEntityVariableSet(this, entityID));
		}
		return variableSets;
	}

}

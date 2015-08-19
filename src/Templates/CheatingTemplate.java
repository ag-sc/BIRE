package Templates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Changes.StateChange;
import Corpus.AnnotatedDocument;
import Factors.Factor;
import Factors.FactorGraph;
import Learning.ObjectiveFunction;
import Learning.Vector;
import Logging.Log;
import Templates.variablesets.UnorderedEntititesVariableSet;
import Templates.variablesets.VariableSet;
import Variables.State;

public class CheatingTemplate extends Template implements Serializable {

	{
		Log.off();
	}

	private static final String GOLD = "GOLD";

	private ObjectiveFunction objective = new ObjectiveFunction();

	public CheatingTemplate() {
		// weights.set(GOLD, 1.0);
	}

	@Override
	public Factor generateFactor(State state, VariableSet variables) {
		Vector featureVector = new Vector();
		if (state.getDocument() instanceof AnnotatedDocument) {
			State goldState = ((AnnotatedDocument) state.getDocument()).getGoldState();
			double score = objective.score(state, goldState).score;
			featureVector.set(GOLD, score);

			Factor factor = new Factor(this);
			factor.setFeatures(featureVector);
			return factor;
		} else {
			Log.w("Template %s: Given state does not have an AnnotatedDocument attached. Cheating template not applicable.",
					this.getClass().getSimpleName());
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
		variableSets.add(new UnorderedEntititesVariableSet(this, state.getEntityIDs()));
		return variableSets;
	}
}

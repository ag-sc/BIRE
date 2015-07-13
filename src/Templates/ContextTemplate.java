package Templates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Corpus.Token;
import Factors.Factor;
import Learning.Vector;
import Logging.Log;
import Variables.EntityAnnotation;
import Variables.State;

public class ContextTemplate extends Template {

	{
		Log.off();
	}

	@Override
	public List<Factor> generateFactors(State state) {
		List<Factor> factors = new ArrayList<Factor>();
		for (EntityAnnotation e : state.getEntities()) {
			if (e.isChanged()) {
				Log.d("Add features to entity %s (\"%s\"):", e.getID(),
						e.getText());
				Factor factor = new Factor(this);
				factors.add(factor);
				Vector featureVector = new Vector();
				factor.setFeatures(featureVector);

				List<Token> tokens = e.getTokens();
				Token first = tokens.get(0);
				Token last = tokens.get(tokens.size() - 1);

				// TODO compare value with entity text, entity type or
				// dictionary entry?
				featureVector.set("FIRST_TOKEN_EQUALS=" + first.getText(), 1.0);
				featureVector.set("LAST_TOKEN_EQUALS=" + last.getText(), 1.0);

				int[] tokenOffsets = { -2, -1, 1, 2 };
				for (int i : tokenOffsets) {
					Token tokenAt = Features.getTokenRelativeToEntity(state, e,
							i);
					if (tokenAt != null) {
						featureVector.set(
								"TOKEN@" + i + "_EQUALS=" + tokenAt.getText(),
								1.0);
					}
				}

				Log.d("Features for entity %s (\"%s\"): %s", e.getID(),
						e.getText(), featureVector);
			}
		}
		return factors;
	}
}

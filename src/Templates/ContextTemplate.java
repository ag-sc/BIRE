package Templates;

import java.util.List;

import Corpus.Token;
import Factors.Factor;
import Learning.Vector;
import Variables.EntityAnnotation;
import Variables.State;

public class ContextTemplate extends Template {


	@Override
	public void applyTo(State state) {
		Factor factor;
		for (EntityAnnotation e : state.getEntities()) {
			if (e.isChanged()) {
				factor = new Factor(this);
				List<Token> tokens = e.getTokens();
				Token first = tokens.get(0);
				Token last = tokens.get(tokens.size() - 1);
				Vector featureVector = new Vector();
				String value = "";
				
				// TODO compare value with entity text or entity type?
				featureVector.set("FIRST_TOKEN_EQUALS=" + value, first
						.getText().equals(value) ? 1.0 : 0);
				featureVector.set("LAST_TOKEN_EQUALS=" + value, last.getText()
						.equals(value) ? 1.0 : 0);

				Token t1 = Features.getTokenRelativeToEntity(state, e, -1);
				featureVector
						.set("TOKEN@-1_EQUALS=" + value, (t1 == null ? "null"
								: t1.getText()).equals(value) ? 1.0 : 0);

				Token t2 = Features.getTokenRelativeToEntity(state, e, 1);
				featureVector
						.set("TOKEN@+1_EQUALS=" + value, (t2 == null ? "null"
								: t2.getText()).equals(value) ? 1.0 : 0);
				factor.setFeatures(featureVector);
			}
		}
	}
}

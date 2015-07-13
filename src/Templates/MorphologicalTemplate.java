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

public class MorphologicalTemplate extends Template {

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

				String entityType = "ENTITY_TYPE=" + e.getType().getName()
						+ "_";
				featureVector.set(entityType + "ALL_TOKENS_INIT_CAP",
						Features.StartsWithCapital.all(tokens));
				featureVector.set(entityType + "AT_LEAST_ONE_TOKEN_INIT_CAP",
						Features.StartsWithCapital.any(tokens));
				featureVector.set(entityType + "FIRST_TOKEN_INIT_CAP",
						Features.StartsWithCapital.first(tokens));
				featureVector.set(entityType + "LAST_TOKEN_INIT_CAP",
						Features.StartsWithCapital.last(tokens));

				featureVector.set(entityType + "ALL_TOKENS_ALL_CAP",
						Features.AllCapital.all(tokens));
				featureVector.set(entityType + "AT_LEAST_ONE_TOKEN_ALL_CAP",
						Features.AllCapital.any(tokens));
				featureVector.set(entityType + "FIRST_TOKEN_ALL_CAP",
						Features.AllCapital.first(tokens));
				featureVector.set(entityType + "LAST_TOKEN_ALL_CAP",
						Features.AllCapital.last(tokens));

				featureVector.set(entityType + "ALL_TOKENS_CONTAIN_DIGIT",
						Features.ContainsDigit.all(tokens));
				featureVector.set(entityType
						+ "AT_LEAST_ONE_TOKEN_CONTAINS_DIGIT",
						Features.ContainsDigit.any(tokens));
				featureVector.set(entityType + "FIRST_TOKEN_CONTAINS_DIGIT",
						Features.ContainsDigit.first(tokens));
				featureVector.set(entityType + "LAST_TOKEN_CONTAINS_DIGIT",
						Features.ContainsDigit.last(tokens));

				featureVector.set(entityType
						+ "AT_LEAST_ONE_TOKEN_CONTAINS_HYPHEN",
						Features.ContainsHyphen.any(tokens));
				featureVector.set(entityType
						+ "AT_LEAST_ONE_TOKEN_CONTAINS_PUNCTUATION",
						Features.ContainsPunctuation.any(tokens));
				featureVector.set(entityType
						+ "AT_LEAST_ONE_TOKEN_CONTAINS_GREEK_SYMBOL",
						Features.ContainsGreek.any(tokens));

				/*
				 * The following features are always present for each individual
				 * token, thus, they always have a value of 1
				 */

				int[] suffixLengths = { 2, 3 };
				for (int i : suffixLengths) {
					featureVector.set(entityType + "LAST_TOKEN_SUFFIX_" + i
							+ "=" + suffix(last.getText(), i), 1.0);
					featureVector.set(entityType + "FIRST_TOKEN_SUFFIX_" + i
							+ "=" + suffix(first.getText(), i), 1.0);
				}

				int[] prefixLengths = { 2, 3 };
				for (int i : prefixLengths) {
					featureVector.set(entityType + "LAST_TOKEN_PREFIX_" + i
							+ "=" + prefix(last.getText(), i), 1.0);
					featureVector.set(entityType + "FIRST_TOKEN_PREFIX_" + i
							+ "=" + prefix(first.getText(), i), 1.0);
				}

				Log.d("Features for entity %s (\"%s\"): %s", e.getID(),
						e.getText(), featureVector);
			}
		}
		return factors;
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
}

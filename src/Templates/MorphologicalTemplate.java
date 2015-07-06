package Templates;

import java.util.List;

import Corpus.Token;
import Factors.Factor;
import Learning.Vector;
import Variables.EntityAnnotation;
import Variables.State;

public class MorphologicalTemplate extends Template {

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
				
				// TODO Entity Type as prefix + null-entity
				String entityType = "ENTITY_TYPE=" + e.getType().getName() + "_";
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

				featureVector.set(
						entityType + "LAST_TOKEN_SUFFIX_3="
								+ suffix(last.getText(), 3), 1.0);
				featureVector.set(entityType + "FIRST_TOKEN_SUFFIX_3="
						+ suffix(first.getText(), 3), 1.0);
				featureVector.set(
						entityType + "LAST_TOKEN_SUFFIX_2="
								+ suffix(last.getText(), 2), 1.0);
				featureVector.set(entityType + "FIRST_TOKEN_SUFFIX_2="
						+ suffix(first.getText(), 2), 1.0);

				featureVector.set(
						entityType + "LAST_TOKEN_PREFIX_3="
								+ prefix(last.getText(), 3), 1.0);
				featureVector.set(entityType + "FIRST_TOKEN_PREFIX_3="
						+ prefix(first.getText(), 3), 1.0);
				featureVector.set(
						entityType + "LAST_TOKEN_PREFIX_2="
								+ prefix(last.getText(), 2), 1.0);
				featureVector.set(entityType + "FIRST_TOKEN_PREFIX_2="
						+ prefix(first.getText(), 2), 1.0);

				factor.setFeatures(featureVector);
			}
		}
	}

	private String suffix(String text, int i) {
		if (i > 0)
			return text.substring(Math.max(0, text.length() - i));
		else
			return "";
	}

	private String prefix(String text, int i) {
		if (i > 0) {
			return text.substring(0, i);
		} else
			return "";
	}
}

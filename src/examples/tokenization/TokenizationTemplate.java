package examples.tokenization;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import factors.AbstractFactor;
import learning.Vector;
import templates.AbstractTemplate;

public class TokenizationTemplate extends AbstractTemplate<TokenState> {

	public int windowSize = 5;

	/**
	 * This template generates factors for each character position and computes
	 * features for the local neighborhood.
	 */
	public TokenizationTemplate() {
	}

	/**
	 * This implementation creates factors for each currently predicted boundary
	 * (except for the edge cases; these are just omitted).
	 */
	@Override
	protected Collection<AbstractFactor> generateFactors(TokenState state) {
		Set<AbstractFactor> factors = new HashSet<>();
		for (int i : state.tokenization.tokenBoundaries) {
			factors.add(new TokenizationFactor(this, i));
		}
		return factors;
	}

	@Override
	protected void computeFactor(TokenState state, AbstractFactor factor) {
		TokenizationFactor tokenizationFactor = (TokenizationFactor) factor;
		int position = tokenizationFactor.position;
		int from = Math.max(position - windowSize / 2, 0);
		int to = Math.min(position + (windowSize + 1) / 2, state.sentence.text.length());

		String window = state.sentence.text.substring(from, to);

		Vector features = new Vector();

		for (int i = 0; i < window.length(); i++) {
			char c = window.charAt(i);
			boolean isPunctuation = c == '.' || c == '!' || c == '?' || c == ',';
			boolean isWhitespace = c == ' ' || c == '\t' || c == '\n';
			boolean isHyphen = c == '-';
			boolean isDigit = Character.isDigit(c);
			boolean isLetter = Character.isLetter(c);
			boolean isLower = Character.isLowerCase(c);
			boolean isUpper = Character.isUpperCase(c);

			int relativePosition = from - position + i;
			features.set("CHAR@" + relativePosition + "_IS_PUNCTUATION", isPunctuation);
			features.set("CHAR@" + relativePosition + "_IS_WHITESPACE", isWhitespace);
			features.set("CHAR@" + relativePosition + "_IS_HYPHEN", isHyphen);
			features.set("CHAR@" + relativePosition + "_IS_DIGIT", isDigit);
			features.set("CHAR@" + relativePosition + "_IS_LETTER", isLetter);
			features.set("CHAR@" + relativePosition + "_IS_LOWER", isLower);
			features.set("CHAR@" + relativePosition + "_IS_UPPER", isUpper);
			features.set("CHAR@" + relativePosition + "_IS_OTHER",
					(!isPunctuation && !isWhitespace && !isHyphen && !isDigit && !isLetter && !isLower && !isUpper));

			// features.set("CHAR@" + relativePosition + "=" + c, 1.0);
		}
		tokenizationFactor.setFeatures(features);
	}

}

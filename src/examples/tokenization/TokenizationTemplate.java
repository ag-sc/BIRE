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

	@Override
	protected void computeFactor(TokenState state, AbstractFactor factor) {
		TokenizationFactor tokenizationFactor = (TokenizationFactor) factor;
		int from = tokenizationFactor.from;
		int to = tokenizationFactor.to;
		// int from = Math.max(0, tokenizationFactor.from);
		// int to = Math.min(state.sentence.text.length(),
		// tokenizationFactor.to);
		String window = state.sentence.text.substring(from, to);

		Vector features = new Vector();

		for (int i = -windowSize / 2; i < windowSize / 2; i++) {
			char c = window.charAt(windowSize / 2 + i);
			boolean isPunctuation = c == '.' || c == '!' || c == '?';
			boolean isWhitespace = c == ' ' || c == '\t' || c == '\n';
			boolean isHyphen = c == '-';
			boolean isDigit = Character.isDigit(c);
			boolean isLetter = Character.isLetter(c);
			boolean isLower = Character.isLowerCase(c);
			boolean isUpper = Character.isUpperCase(c);

			features.set("CHAR@" + i + "_IS_PUNCTUATION", isPunctuation ? 1.0 : 0.0);
			features.set("CHAR@" + i + "_IS_WHITESPACE", isWhitespace ? 1.0 : 0.0);
			features.set("CHAR@" + i + "_IS_HYPHEN", isHyphen ? 1.0 : 0.0);
			features.set("CHAR@" + i + "_IS_DIGIT", isDigit ? 1.0 : 0.0);
			features.set("CHAR@" + i + "_IS_LETTER", isLetter ? 1.0 : 0.0);
			features.set("CHAR@" + i + "_IS_LOWER", isLower ? 1.0 : 0.0);
			features.set("CHAR@" + i + "_IS_UPPER", isUpper ? 1.0 : 0.0);

			features.set("CHAR@" + i + "=" + c, 1.0);
		}
		tokenizationFactor.setFeatures(features);
	}

	@Override
	protected Collection<AbstractFactor> generateFactors(TokenState state) {
		Set<AbstractFactor> factors = new HashSet<>();
		for (int i : state.tokenization.tokenBoundaries) {
			int from = i - windowSize / 2;
			int to = i + windowSize / 2;
			// int from = Math.max(0, i - windowSize / 2);
			// int to = Math.min(state.sentence.text.length(), i + windowSize /
			// 2);
			if (from >= 0 && to <= state.sentence.text.length()) {
				factors.add(new TokenizationFactor(this, from, to));
			}
		}
		return factors;
	}

}

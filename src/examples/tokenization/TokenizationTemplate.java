package examples.tokenization;

import java.util.ArrayList;
import java.util.List;

import factors.Factor;
import factors.patterns.SingleVariablePattern;
import learning.Vector;
import templates.AbstractTemplate;

public class TokenizationTemplate
		extends AbstractTemplate<Sentence, TokenState, SingleVariablePattern<BoundaryVariable>> {

	public int windowSize = 3;

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
	public List<SingleVariablePattern<BoundaryVariable>> generateFactorPatterns(TokenState state) {
		List<SingleVariablePattern<BoundaryVariable>> factors = new ArrayList<>();
		for (BoundaryVariable b : state.tokenization.tokenBoundaries.values()) {
			factors.add(new SingleVariablePattern<>(this, b));
		}
		return factors;
	}

	@Override
	public void computeFactor(Sentence instance, Factor<SingleVariablePattern<BoundaryVariable>> factor) {
		Vector features = factor.getFeatureVector();
		int position = factor.getFactorPattern().getVariable().boundaryPosition;
		int from = Math.max(position - windowSize / 2, 0);
		int to = Math.min(position + (windowSize + 1) / 2, instance.text.length());

		String window = instance.text.substring(from, to);

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

	}

}

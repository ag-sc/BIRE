package examples.tokenization;

import java.util.ArrayList;
import java.util.List;

import examples.tokenization.TokenizationTemplate.TokenizationFactorScope;
import factors.Factor;
import factors.FactorScope;
import learning.Vector;
import templates.AbstractTemplate;

public class TokenizationTemplate extends AbstractTemplate<String, TokenState, TokenizationFactorScope> {

	class TokenizationFactorScope extends FactorScope {
		public int boundaryPosition;
		public int fromOffset;
		public String window;

		public TokenizationFactorScope(AbstractTemplate<?, ?, ?> template, int boundaryPosition, int fromOffset,
				String window) {
			super(template, boundaryPosition, fromOffset, window);
			this.boundaryPosition = boundaryPosition;
			this.fromOffset = fromOffset;
			this.window = window;
		}
	}

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
	public List<TokenizationFactorScope> generateFactorScopes(TokenState state) {
		List<TokenizationFactorScope> factors = new ArrayList<>();
		for (int position : state.tokenization.tokenBoundaries) {
			int from = Math.max(position - windowSize / 2, 0);
			int to = Math.min(position + (windowSize + 1) / 2, state.getInstance().length());

			String window = state.getInstance().substring(from, to);

			factors.add(this.new TokenizationFactorScope(this, position, from, window));
		}
		return factors;
	}

	@Override
	public void computeFactor(Factor<TokenizationFactorScope> factor) {
		Vector features = factor.getFeatureVector();
		int position = factor.getFactorScope().boundaryPosition;
		int from = factor.getFactorScope().fromOffset;
		String window = factor.getFactorScope().window;

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
		}

	}

}

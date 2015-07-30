package Corpus.parser;

import java.util.List;

import Corpus.Token;
import Logging.Log;

public class ParsingUtils {

	/**
	 * Binary search on a list of Tokens. This functions tries to find the token
	 * in who's span of characters the specified character position falls in.
	 * When a token is found, the token's index in the given list is returned.
	 * Note that the upper bound of a tokens character span, which is given by
	 * token.geEnd(), is actually "the position of the last character + 1".
	 * 
	 * 
	 * @param characterPosition
	 * @param tokens
	 * @param inclusive
	 *            if true, the value for upper boundary is included in the span,
	 *            otherwise it is excluded
	 * @return
	 */
	public static int binarySearch(int characterPosition, List<Token> tokens,
			boolean inclusive) {
		int low = 0;
		int high = tokens.size() - 1;
		int middle = 0;
		while (low <= high) {
			// Key is in a[lo..hi] or not present.
			middle = low + (high - low) / 2;
			Token midToken = tokens.get(middle);
			if (characterPosition < midToken.getFrom())
				high = middle - 1;
			else if (inclusive ? characterPosition >= midToken.getTo()
					: characterPosition > midToken.getTo())
				low = middle + 1;
			else
				return middle;
		}
		Log.w("No token for position %s found. Last boundaries %s-%s-%s.",
				characterPosition, low, middle, high);
		return -1;
	}

	public static int binarySearch(int characterPosition, List<Token> tokens) {
		return binarySearch(characterPosition, tokens, false);
	}
}

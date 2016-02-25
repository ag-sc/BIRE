**(Work In Progress)**

We demonstrate the usage of the BIRE framework given a tokenization problem.
Our data consists of natural language sentences that are labeled with an expected tokenization.
**Note:** We define "tokenization" as finding the correct token boundaries, i.e. character offsets.
We create a Sentence class that implements the Instance interface and that contains a String representation of the actual sentence:
'''
public class Sentence implements Instance {

	public String text;

	...
}
'''

Then we define our tokenization.
We create a class Tokenization that consist of a set of Integers:
'''java

public class Tokenization {
	public Set<Integer> tokenBoundaries;
    
    public Tokenization() {
		this.tokenBoundaries = new HashSet<>();
	}
    ...
}
'''

The whole set of Integers describes the complete tokenization of the Sentence while each individual Integer represents a single token boundary.
Example:
Sentence:   "The cat is black."
Boundaries: [0,3,4,7,8,10,11,16,17]

We combine Sentence and Tokenization into a single object that we call TokenizedSentence:
'''java
public class TokenizedSentence extends Sentence implements LabeledInstance<Tokenization> {

	private Tokenization tokenization;

	@Override
	public Tokenization getGoldResult() {
		return tokenization;
	}
	...
}
'''
The TokenizedSentence is our labeled training data and needs to implement LabeledInstance.
The interface requires the implementation of the getGoldResult() method which returns the expected Tokenization of the Sentence.
In our case the type of the "generic" result is fixed to Tokenization.


In this examples, the BIRE framework uses TokenStates to represent the current tokenization of a Sentence.
Each TokenState extends the AbstractState class and references the sentence that it relates to and contains a preliminary tokenization (in the form of character offsets).
'''java
public class TokenState extends AbstractState {

	public Sentence sentence;
	public Tokenization tokenization;
	
	public TokenState(Sentence sentence) {
		super();
		this.sentence = sentence;
		this.tokenization = new Tokenization();
	}
	...
}
'''

Up until this point, we defined all the basic classes to represent our data.
The BIRE framework uses sampling strategies to infer a tokenization from a given sentence.
Before we can implement the exploration of our search space (tokenizations) we need to provide an initial TokenState.
We implement the Initializer interface in the TokenizationInitializer class.
Given a Sentence, this class provides an initial TokenState:
'''java
public class TokenizationInitializer<SentenceT extends Sentence> implements Initializer<SentenceT, TokenState> {

	@Override
	public TokenState getInitialState(SentenceT sentence) {
		return new TokenState(sentence);
	}
}
'''
**Note:** For simplicity, we always initialize the TokenState with no boundaries.
However, it is possible to use heuristics and prior knowledge for token boundaries at this point, e.g. another tokenization tool. 

Next we define our exploration strategy that explores the search space around a given TokenState.
For this, we implement the Explorer interface that requires the getNextStates() method:
'''
public class TokenBoundaryExplorer implements Explorer<TokenState> {

	@Override
	public List<TokenState> getNextStates(TokenState currentState) {
		List<TokenState> nextStates = new ArrayList<>();
		for (int i = 0; i < currentState.sentence.text.length(); i++) {
			if (currentState.tokenization.tokenBoundaries.contains(i)) {
				TokenState generatedState = new TokenState(currentState);
				generatedState.tokenization.tokenBoundaries.remove(i);
				nextStates.add(generatedState);
			} else {
				TokenState generatedState = new TokenState(currentState);
				generatedState.tokenization.tokenBoundaries.add(i);
				nextStates.add(generatedState);
			}
		}
		return nextStates;
	}
}
'''
Given a current TokenState, we generate a set of slightly modified candidate states by applying atomic changes to the current state.
We iterate of all the characters in the sentence and create a modified state with the following rules:
if that specific character position is already marked as a token boundary:
	we remove this boundary.
if not
	we insert a new boundary.
	
This results in exactly one state for each character in the sentence.
This naive exploration strategy allows us to reach every possible tokenization from every state in our sampling procedure.
**Note:** The exploration could easily be optimized, e.g. by only creating boundaries at specific locations such as whitespaces, punctuation, ... . 

At this point, we implemented the necessary components for the sampling procedure.
Following, we set up the components for the learning procedure.
We start with the objective function, that scores a TokenState w.r.t. a preferred Tokenization:
'''
public class TokenizationObjectiveFunction extends ObjectiveFunction<TokenState, Tokenization> {

	@Override
	protected double computeScore(TokenState state, Tokenization goldResult) {
		if (goldResult.tokenBoundaries.size() == 0 && state.tokenization.tokenBoundaries.size() == 0) {
			return 1;
		} else if (goldResult.tokenBoundaries.size() == 0 && state.tokenization.tokenBoundaries.size() != 0) {
			return 0;
		} else if (goldResult.tokenBoundaries.size() != 0 && state.tokenization.tokenBoundaries.size() == 0) {
			return 0;
		}
		Set<Integer> correctBoundaries = Sets.intersection(state.tokenization.tokenBoundaries,
				goldResult.tokenBoundaries);
				
		double precision = correctBoundaries.size();
		double recall = correctBoundaries.size();
	
		precision /= (state.tokenization.tokenBoundaries.size());
		recall /= (goldResult.tokenBoundaries.size());

		double g1 = Math.sqrt(precision * recall);
		return g1;
	}
}
'''
The TokenizationObjectiveFunction computes the overlap between the expected gold tokenization and the tokenization that is proposed by the given TokenState.
The G1-Measure (very similar to F1-measure) is computed to account for missing and  wrongly placed token boundaries.

The objective function gives us a method to score a state at training time.
However, we need to define means to score a state at test time, without the objective function.
For this, we implement a key component of our model: the template.
Our solution for the tokenization problem only uses a single Template:
'''
public class TokenizationTemplate extends AbstractTemplate<TokenState> {

	public int windowSize = 5;

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
			boolean isPunctuation = c == '.' || c == '!' || c == '?';
			boolean isWhitespace = c == ' ' || c == '\t' || c == '\n';
			boolean isHyphen = c == '-';
			boolean isDigit = Character.isDigit(c);
			boolean isLetter = Character.isLetter(c);
			boolean isLower = Character.isLowerCase(c);
			boolean isUpper = Character.isUpperCase(c);

			int relativePosition = from - position + i;
			features.set("CHAR@" + relativePosition + "_IS_PUNCTUATION", isPunctuation ? 1.0 : 0.0);
			features.set("CHAR@" + relativePosition + "_IS_WHITESPACE", isWhitespace ? 1.0 : 0.0);
			features.set("CHAR@" + relativePosition + "_IS_HYPHEN", isHyphen ? 1.0 : 0.0);
			features.set("CHAR@" + relativePosition + "_IS_DIGIT", isDigit ? 1.0 : 0.0);
			features.set("CHAR@" + relativePosition + "_IS_LETTER", isLetter ? 1.0 : 0.0);
			features.set("CHAR@" + relativePosition + "_IS_LOWER", isLower ? 1.0 : 0.0);
			features.set("CHAR@" + relativePosition + "_IS_UPPER", isUpper ? 1.0 : 0.0);

			// features.set("CHAR@" + relativePosition + "=" + c, 1.0);
		}
		tokenizationFactor.setFeatures(features);
	}
}
'''
The functionality of each template is split into two methods that need to be implemented, the generateFactors() and the computeFactor() method.
The generateFactors() introduces a factor for each boundary position that is proposed by the given state. The TokenizationFactor simply extends the AbtractFactor class and, additionally, stores the boundary position that it was assigned to:
'''java
public class TokenizationFactor extends AbstractFactor {

	public int position;
	...
}
'''

Then, the computeFactor() method receives each of the created factors and computes feature values for this specific factor.
In this implementation we decided on computing lexical features on a window of 5 characters around the token boundary in question. By this, we can capture and evaluate a (small) context around the boundary position.



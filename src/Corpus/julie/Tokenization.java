package Corpus.julie;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Corpus.Token;
import Corpus.parser.brat.Utils;
import Logging.Log;
import de.julielab.jtbd.JTBDExtendedAPI;
import de.julielab.jtbd.TokenizedSentence;
import de.julielab.jtbd.Unit;

public class Tokenization {

	private static final String TOKENIZATION_FILE_SUFFIX = ".tok";

	public List<Token> tokens;
	public String originalSentence;
	public int absoluteStartOffset;
	public int absoluteEndOffset;

	public Tokenization(List<Token> tokens, String originalSentence, int absoluteStartOffset) {
		this.tokens = tokens;
		this.originalSentence = originalSentence;
		this.absoluteStartOffset = absoluteStartOffset;
		this.absoluteEndOffset = absoluteStartOffset + originalSentence.length();
	}

	/**
	 * Returns a List of tokens for each sentence in the document. The character
	 * offsets of the tokens match their respective sentences (0 to
	 * sentence.length()). The tokenization is currently performed with the
	 * JTBDv1.6.
	 * 
	 * @param documentName
	 * @param tokenizationDirPath
	 * @param sentencesFilepath
	 * @param julieModelFilepath
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static List<Tokenization> extractAndStoreTokens(String documentName, String tokenizationDirPath,
			String sentencesFilepath, String julieModelFilepath) throws FileNotFoundException, IOException {
		Log.d("tokenize with julie:");
		List<TokenizedSentence> tokenizedSentences = null;
		tokenizedSentences = JTBDExtendedAPI.tokenize(sentencesFilepath, julieModelFilepath);
		List<String> sentences = Utils.readLines(sentencesFilepath);
		saveTokenization(tokenizedSentences, documentName, tokenizationDirPath);
		Log.d("julie-tokenized sentences:");
		List<Tokenization> tokenizations = new ArrayList<Tokenization>();
		int accumulatedSentenceLength = 0;
		for (int i = 0; i < tokenizedSentences.size(); i++) {
			List<Token> tokens = new ArrayList<Token>();
			TokenizedSentence ts = tokenizedSentences.get(i);
			Log.d("%s (#characters: %s)", ts.getOriginalSentence(), ts.getOriginalSentence().length());
			Log.d("\t%s", ts.getTokens());
			int index = 0;
			for (Unit u : ts.getTokens()) {
				String text = u.rep;
				int from = u.begin;
				int to = u.end;
				tokens.add(new Token(index, from, to, text));
				index++;
			}
			tokenizations.add(new Tokenization(tokens, sentences.get(i), accumulatedSentenceLength));
			accumulatedSentenceLength += sentences.get(i).length() + 1;
		}
		return tokenizations;
	}

	// public static List<Token> extractTokens(String documentName,
	// String tokenizationDirPath, String splittedTextFilepath,
	// String julieModelFilepath) throws FileNotFoundException,
	// IOException {
	// Log.d("tokenize with julie:");
	// List<TokenizedSentence> tokenizedSentences = null;
	// tokenizedSentences = JTBDExtendedAPI.tokenize(splittedTextFilepath,
	// julieModelFilepath);
	// List<String> sentences = SentenceSplitter.getSentencesAsList(new File(
	// splittedTextFilepath));
	//
	// saveTokenization(tokenizedSentences, documentName, tokenizationDirPath);
	// Log.d("julie-tokenized sentences:");
	// int index = 0;
	// List<Token> tokens = new ArrayList<Token>();
	// int accumulatedSentenceLength = 0;
	// for (int i = 0; i < tokenizedSentences.size(); i++) {
	// TokenizedSentence ts = tokenizedSentences.get(i);
	// Log.d("%s (#characters: %s)", ts.getOriginalSentence(), ts
	// .getOriginalSentence().length());
	// Log.d("\t%s", ts.getTokens());
	// for (Unit u : ts.getTokens()) {
	// String text = u.rep;
	// int from = accumulatedSentenceLength + u.begin;
	// int to = accumulatedSentenceLength + u.end;
	// tokens.add(new Token(index, from, to, text));
	// index++;
	// }
	// accumulatedSentenceLength += sentences.get(i).length() + 1;
	// }
	// return tokens;
	// }

	private static void saveTokenization(List<TokenizedSentence> tokenizedSentences, String documentName,
			String tokenizationDirPath) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < tokenizedSentences.size(); i++) {
			TokenizedSentence ts = tokenizedSentences.get(i);
			builder.append(ts.getTokenizedSentence());
			if (i < tokenizedSentences.size() - 1)
				builder.append("\n");
		}
		try {
			File tokenizationFile = new File(tokenizationDirPath, documentName + TOKENIZATION_FILE_SUFFIX);
			Utils.writeFile(tokenizationFile, builder.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

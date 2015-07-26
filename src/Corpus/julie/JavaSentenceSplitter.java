package Corpus.julie;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Corpus.parser.brat.BratAnnotatedDocument;
import Corpus.parser.brat.Utils;
import Corpus.parser.brat.annotations.BratAnnotation;
import Corpus.parser.brat.annotations.BratTextBoundAnnotation;
import Logging.Log;

public class JavaSentenceSplitter {

	public static List<String> getSentencesAsList(File srcFile)
			throws FileNotFoundException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(srcFile));
		String line;
		List<String> sentences = new ArrayList<String>();
		while ((line = reader.readLine()) != null) {
			BreakIterator iterator = BreakIterator
					.getSentenceInstance(Locale.US);
			iterator.setText(line);
			int start = iterator.first();
			for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator
					.next()) {
				// end - 1 to remove the trailing whitespace
				String sentence = line.substring(start, end - 1);
				sentences.add(sentence);
				Log.d(sentence);
			}
			/*
			 * This little hack with the trailing whitespaces is necessary so
			 * that the character offsets of the respective annotations stay
			 * consistent with the sentence-splitted text.
			 */
			String sentence = sentences.get(sentences.size() - 1);
			sentence = sentence + " ";
			sentences.set(sentences.size() - 1, sentence);
		}
		reader.close();
		return sentences;
	}

	public static String getSentencesAsString(File srcFile) throws IOException {
		List<String> sentences = getSentencesAsList(srcFile);

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < sentences.size(); i++) {
			builder.append(sentences.get(i));
			if (i < sentences.size() - 1)
				builder.append("\n");
		}

		String splittedText = builder.toString();
		return splittedText;
	}

	public static void extractAndStoreSentences(File srcFile, File sentFile)
			throws IOException {
		String splittedText = getSentencesAsString(srcFile);
		Utils.writeFile(sentFile, splittedText);
	}

	public static boolean isConsistent(String original, String splitted,
			BratAnnotatedDocument doc) {
		for (BratAnnotation ann : doc.getAllAnnotations().values()) {
			if (ann instanceof BratTextBoundAnnotation) {
				BratTextBoundAnnotation t = (BratTextBoundAnnotation) ann;
				int beginIndex = t.getStart();
				int endIndex = t.getEnd();

				String originalAnnText = original.substring(beginIndex,
						endIndex);
				String splittedAnnText = splitted.substring(beginIndex,
						endIndex);
				if (!originalAnnText.equals(splittedAnnText)) {
					Log.w("Splitted file %s does not match (at least) for position %s-%s:\n\torig: %s\n\tsplitted: %s",
							doc.getAnnotationFilename(), beginIndex, endIndex,
							originalAnnText, splittedAnnText);
					return false;
				}
			}
		}
		return true;
	}

}

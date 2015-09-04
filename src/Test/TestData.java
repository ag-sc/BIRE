package Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Corpus.AnnotatedDocument;
import Corpus.AnnotationConfig;
import Corpus.BratCorpus;
import Corpus.Corpus;
import Corpus.Token;
import Corpus.parser.brat.BratConfigReader;
import Logging.Log;
import Variables.EntityAnnotation;
import Variables.State;

public class TestData {

	public static Corpus getDummyData() {
		BratConfigReader configReader = new BratConfigReader();
		AnnotationConfig originalConfig = configReader.readConfig(new File("res/bionlp/annotation.conf"));
		AnnotationConfig simplifiedConfig = new AnnotationConfig();
		simplifiedConfig.addEntityType(originalConfig.getEntityType("Protein"));

		String content = "a critical role for tumor necrosis factor and interleukin-7";
		List<Token> tokens = extractTokens(content);
		Log.d("Tokens for dummy data: %s", tokens);

		BratCorpus corpus = new BratCorpus(simplifiedConfig);
		AnnotatedDocument doc = new AnnotatedDocument(corpus, "DummyDocument", content, tokens, 0);
		State goldState = new State(doc);
		doc.setGoldState(goldState);

		EntityAnnotation e1 = new EntityAnnotation(goldState, "T1", simplifiedConfig.getEntityType("Protein"), 4, 6);
		goldState.addEntityAnnotation(e1);
		EntityAnnotation e2 = new EntityAnnotation(goldState, "T2", simplifiedConfig.getEntityType("Protein"), 8, 8);
		goldState.addEntityAnnotation(e2);

		corpus.addDocument(doc);

		return corpus;
	}

	private static List<Token> extractTokens(String content) {
		List<Token> tokens = new ArrayList<Token>();
		Matcher matcher = Pattern.compile("\\S+").matcher(content);

		int index = 0;
		while (matcher.find()) {
			String text = matcher.group();
			int from = matcher.start();
			int to = matcher.end();

			Token token = new Token(index, from, to, text);
			tokens.add(token);
			index++;
		}
		return tokens;
	}
}

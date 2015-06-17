package Sampling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Corpus.AnnotationConfig;
import Corpus.BratConfigReader;
import Corpus.BratCorpus;
import Corpus.Corpus;
import Corpus.AnnotatedDocument;
import Corpus.Token;
import Learning.Model;
import Learning.ObjectiveFunction;
import Learning.Scorer;
import Variables.EntityAnnotation;
import Variables.EntityManager;
import Variables.State;

public class TestSampling {

	public static void main(String[] args) {
		Corpus corpus = getDummyData();
		List<AnnotatedDocument> documents = corpus.getDocuments();
		AnnotatedDocument doc = documents.get(0);
		State initialState = new State(doc);
		State goldState = new State(doc, doc.getGoldEntities());
		System.out.println("initialState: " + initialState);
		Model model = new Model();
		Scorer scorer = new Scorer(model);
		DefaultSampler sampler = new DefaultSampler(10);
		State nextState = initialState;
		for (int i = 0; i < 2; i++) {
			System.out.println("--------- Step: " + i + " ----------");
			nextState = sampler.getNextState(nextState, scorer);
			System.out.println("Next state:");
			System.out.println(nextState);
			for (EntityAnnotation e : nextState.getEntities()) {
				System.out.println(e);
			}
			for (Entry<Integer, Set<String>> e : nextState
					.getTokenToEntityMapping().entrySet()) {
				System.out.println(e);
			}
		}

		ObjectiveFunction of = new ObjectiveFunction();
		double score = of.score(nextState, goldState);
		System.out.println("Score: " + score);
	}

	public static Corpus getDummyData() {
		BratConfigReader configReader = new BratConfigReader();
		AnnotationConfig config = configReader
				.readConfig("res/annotation.conf");
		String content = "a critical role for tumor necrosis factor and interleukin-7";
		List<Token> tokens = extractTokens(content);
		System.out.println(tokens);
		EntityManager manager = new EntityManager();

		EntityAnnotation e1 = new EntityAnnotation(manager, "T1");
		e1.init(config.getEntityType("Protein"), 4, 6);
		manager.addEntityAnnotation(e1);
		EntityAnnotation e2 = new EntityAnnotation(manager, "T2");
		e2.init(config.getEntityType("Protein"), 8, 8);
		manager.addEntityAnnotation(e2);

		BratCorpus corpus = new BratCorpus(config);
		AnnotatedDocument doc = new AnnotatedDocument(corpus, content, tokens, manager);
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

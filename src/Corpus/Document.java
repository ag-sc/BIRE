package Corpus;

import java.util.List;
import java.util.Map;

import Variables.EntityAnnotation;
import Variables.EntityManager;

public class Document {

	Corpus corpus;
	String content;
	List<Token> tokens;
	/**
	 * Since Entities only hold weak pointer via references to one another,
	 * using a Map is sensible to enable an efficient access to the entities.
	 */
	EntityManager goldEntityManager;

	public Document(Corpus corpus, String content, List<Token> tokens,
			EntityManager goldEntityManager) {
		super();
		this.corpus = corpus;
		this.content = content;
		this.tokens = tokens;
		this.goldEntityManager = goldEntityManager;
	}

	public String getContent() {
		return content;
	}

	public List<Token> getTokens() {
		return tokens;
	}

	public Corpus getCorpus() {
		return corpus;
	}

}

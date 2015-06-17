package Corpus;

import java.util.List;

public class Document {

	Corpus corpus;
	String content;
	List<Token> tokens;

	public Document(Corpus corpus, String content, List<Token> tokens) {
		super();
		this.corpus = corpus;
		this.content = content;
		this.tokens = tokens;
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

package Corpus;

import java.io.Serializable;
import java.util.List;

public class Document implements Serializable {

	protected Corpus corpus;
	protected String content;
	protected List<Token> tokens;

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

package Corpus;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class Document implements Serializable {

	protected String name;
	protected Corpus corpus;
	protected String content;
	protected List<Token> tokens;

	public Document(Corpus corpus, String name, String content, List<Token> tokens) {
		this.corpus = corpus;
		this.name = name;
		this.content = content;
		this.tokens = tokens;
	}

	public String getContent() {
		return content;
	}

	public List<Token> getTokens() {
		return Collections.unmodifiableList(tokens);
	}

	public Corpus getCorpus() {
		return corpus;
	}

	public String getName() {
		return name;
	}

}

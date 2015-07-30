package Corpus;

import java.io.Serializable;
import java.util.List;

public class Document implements Serializable {

	protected String name;
	protected Corpus corpus;
	protected String content;
	protected List<Token> tokens;

	public Document() {
	}

	public Document(String name, String content, List<Token> tokens) {
		super();
		this.name = name;
		this.content = content;
		this.tokens = tokens;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setTokens(List<Token> tokens) {
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

	public void setCorpus(Corpus corpus) {
		this.corpus = corpus;
	}

	public String getName() {
		return name;
	}

}

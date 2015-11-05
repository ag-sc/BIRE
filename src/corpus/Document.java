package corpus;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class Document<PriorT> implements Serializable {

	protected String name;
	protected String content;
	protected List<Token> tokens;
	protected PriorT priorKnowledge;

	public Document(String name, String content, List<Token> tokens) {
		this.name = name;
		this.content = content;
		this.tokens = tokens;
		// this.priorKnowledge = priorKnowledge;
	}

	public String getContent() {
		return content;
	}

	public PriorT getPriorKnowledge() {
		return priorKnowledge;
	}

	public void setPriorKnowledge(PriorT priorKnowledge) {
		this.priorKnowledge = priorKnowledge;
	}

	public List<Token> getTokens() {
		return Collections.unmodifiableList(tokens);
	}

	public String getName() {
		return name;
	}

}

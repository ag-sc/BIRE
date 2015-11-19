package corpus;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class Document<PriotT> implements Serializable, Instance {

	protected String name;
	protected String content;
	protected List<Token> tokens;
	protected PriotT priorKnowledge;

	public Document(String name, String content, List<Token> tokens) {
		this.name = name;
		this.content = content;
		this.tokens = tokens;
	}

	public PriotT getPriorKnowledge() {
		return priorKnowledge;
	}

	public void setPriorKnowledge(PriotT priorKnowledge) {
		this.priorKnowledge = priorKnowledge;
	}

	public String getContent() {
		return content;
	}

	public List<Token> getTokens() {
		return Collections.unmodifiableList(tokens);
	}

	public String getName() {
		return name;
	}

}

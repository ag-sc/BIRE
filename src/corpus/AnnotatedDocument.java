package corpus;

import java.util.List;

public class AnnotatedDocument<PriorT, ResultT> extends Document<PriorT> {
	/**
	 * This State object holds the human labeled, correct annotations, that are
	 * used during training and evaluation.
	 */
	protected ResultT goldQuery;

	public AnnotatedDocument(String name, String content, List<Token> tokens) {
		super(name, content, tokens);
	}

	public ResultT getGoldResult() {
		return goldQuery;
	}

	@Override
	public String toString() {
		return "AnnotatedDocument [content=" + content + ", tokens=" + tokens + ", goldState=" + goldQuery + "]";
	}

	public void setGoldResult(ResultT goldQuery) {
		this.goldQuery = goldQuery;
	}

}

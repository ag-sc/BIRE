package corpus;

import java.util.List;

/**
 * This class implements the LabeledInstance interface for documents by
 * extending from the Document class and extending it by the expected gold
 * result.
 * 
 * @author sjebbara
 *
 * @param <PriorT>
 * @param <ResultT>
 */
public class LabeledDocument<PriorT, ResultT> extends Document<PriorT>implements LabeledInstance<ResultT> {
	/**
	 * This object holds the (human) labeled, correct result, that should be
	 * used during training and evaluation.
	 */
	protected ResultT goldResult;

	public LabeledDocument(String name, String content, List<Token> tokens) {
		super(name, content, tokens);
	}

	public ResultT getGoldResult() {
		return goldResult;
	}

	@Override
	public String toString() {
		return "LabeledDocument [content=" + content + ", tokens=" + tokens + ", goldState=" + goldResult + "]";
	}

	public void setGoldResult(ResultT goldQuery) {
		this.goldResult = goldQuery;
	}

}

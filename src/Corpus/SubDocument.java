package Corpus;

import java.util.List;

import Variables.State;

public class SubDocument extends AnnotatedDocument {
	protected String parentDocumentName;

	protected int offset = 0;

	public SubDocument(Corpus<SubDocument> corpus, String parentDocumentName, String subDocumentName, String content,
			List<Token> tokens, int offset) {
		super(corpus, subDocumentName, content, tokens);
		this.parentDocumentName = parentDocumentName;
		this.offset = offset;
	}

	public String getParentDocumentName() {
		return parentDocumentName;
	}

	public int getOffset() {
		return offset;
	}

	@Override
	public String toString() {
		return "SubDocument [parentDocumentName=" + parentDocumentName + ", name=" + name + ", offset=" + offset
				+ ", content=" + content + ", tokens=" + tokens + ", goldState=" + goldState + "]";
	}
}

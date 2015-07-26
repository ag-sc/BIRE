package Learning;

import java.util.List;

import Corpus.AnnotatedDocument;

public interface Learner {

	public void train(List<AnnotatedDocument> documents, int epochs);
	
}

package Learning;

import java.util.List;

import Corpus.AnnotatedDocument;

public interface Learner {

	public void train(List<? extends AnnotatedDocument> documents, int epochs);

}

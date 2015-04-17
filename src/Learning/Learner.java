package Learning;

import java.util.List;

import Annotation.Annotation;
import Corpus.Document;

public interface Learner {

	public void train(List<Document> documents, List<List<Annotation>> annotations);
	
}

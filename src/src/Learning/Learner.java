package Learning;

import java.util.List;

import Variables.Annotation;
import Variables.State;
import Corpus.Document;

public interface Learner {

	public void train(List<Document> documents, List<State> states);
	
}

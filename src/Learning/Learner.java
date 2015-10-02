package Learning;

import java.util.List;

import Corpus.AnnotatedDocument;
import Variables.IState;

public interface Learner<StateT extends IState> {

	public List<StateT> train(List<? extends AnnotatedDocument<StateT>> documents, int epochs);

}

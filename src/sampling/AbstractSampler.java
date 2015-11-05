package sampling;

import java.util.List;

import corpus.AnnotatedDocument;
import corpus.Document;
import learning.Learner;
import variables.AbstractState;

public abstract class AbstractSampler<PriorT, StateT extends AbstractState, ResultT> {

	public abstract List<StateT> generateChain(AnnotatedDocument<PriorT, ResultT> document, int steps,
			Learner<StateT> learner);

	public abstract List<StateT> generateChain(Document<PriorT> document, int steps);

}

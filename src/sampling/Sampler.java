package sampling;

import java.util.List;

import corpus.AnnotatedDocument;
import learning.Learner;
import variables.AbstractState;

public abstract class Sampler<StateT extends AbstractState> {

	protected boolean useInitialAnnotations = false;

	protected StateT generateInitialState(AnnotatedDocument<StateT> document) {
		// TODO check if this mode (initial knowledge) is requested or if the
		// learner should start from scratch
		return document.getInitialState().duplicate();
	}

	protected boolean isUseInitialAnnotations() {
		return useInitialAnnotations;
	}

	protected void setUseInitialAnnotations(boolean useInitialAnnotations) {
		this.useInitialAnnotations = useInitialAnnotations;
	}

	public abstract List<StateT> generateChain(AnnotatedDocument<StateT> document, Learner<StateT> learner, int steps);

	public abstract List<StateT> generateChain(AnnotatedDocument<StateT> document, int steps);

}

package sampling;

import corpus.Document;
import variables.AbstractState;

/**
 * A class implementing this interface is supposed to provide an initial state/a
 * starting point for the DefaultSampler chain to start sampling from. If
 * applicable, the state should be initialized with (fixed) "prior knowledge" at
 * this point.
 * 
 * @author sjebbara
 *
 * @param <StateT>
 */
public interface Initializer<PriorT, StateT extends AbstractState> {
	public StateT getInitialState(Document<PriorT> document);
}

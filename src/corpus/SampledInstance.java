package corpus;

import variables.AbstractState;

/**
 * The SampledInstance represents a data point for which we can provide a
 * result e.g. a sentence with gold annotations.
 * 
 * @author sjebbara
 *
 * @param <ResultT>
 */
public class SampledInstance<InstanceT, ResultT, StateT extends AbstractState<InstanceT>> {

	/**
	 * The data instance.
	 * 
	 * @return
	 */
	private InstanceT instance;
	/**
	 * The result for this data instance.
	 * 
	 * @return
	 */
	private ResultT result;

	private StateT state;

	public SampledInstance(InstanceT instance, ResultT result, StateT state) {
		super();
		this.instance = instance;
		this.result = result;
		this.state = state;
	}

	public InstanceT getInstance() {
		return instance;
	}

	public ResultT getGoldResult() {
		return result;
	}

	public StateT getState() {
		return state;
	}

}

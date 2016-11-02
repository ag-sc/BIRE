package corpus;

/**
 * The LabeledInstanceImpl represents a data point for which we can provide a
 * result e.g. a sentence with gold annotations.
 * 
 * @author sjebbara
 *
 * @param <ResultT>
 */
public class LabeledInstanceImpl<InstanceT, ResultT> implements LabeledInstance<InstanceT, ResultT>{

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

	public LabeledInstanceImpl(InstanceT instance, ResultT result) {
		super();
		this.instance = instance;
		this.result = result;
	}

	public InstanceT getInstance() {
		return instance;
	}

	public ResultT getResult() {
		return result;
	}

	@Override
	public String toString() {
		return "LabeledInstance [instance=" + instance + ", result=" + result + "]";
	}

}

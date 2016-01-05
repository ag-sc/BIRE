package corpus;

/**
 * The LabeledInstance represents a data point for which we can provide an
 * expected result e.g. a sentence with gold annotations.
 * 
 * @author sjebbara
 *
 * @param <ResultT>
 */
public interface LabeledInstance<ResultT> extends Instance {

	/**
	 * The expected result for this data instance.
	 * 
	 * @return
	 */
	public ResultT getGoldResult();
}

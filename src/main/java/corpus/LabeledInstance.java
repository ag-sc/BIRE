package corpus;

/**
 * The InstanceResultPair represents a data point for which we can provide a
 * result e.g. a sentence with gold annotations.
 * 
 * @author sjebbara
 *
 * @param <ResultT>
 */
public interface LabeledInstance<InstanceT, ResultT> {

	public InstanceT getInstance();

	public ResultT getGoldAnnotation();

}

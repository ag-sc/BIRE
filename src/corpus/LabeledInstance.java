package corpus;

public interface LabeledInstance<ResultT> extends Instance {

	public ResultT getGoldResult();
}

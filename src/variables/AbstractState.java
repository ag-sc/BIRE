package variables;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

import corpus.Document;
import factors.FactorGraph;
import utility.StateID;

public abstract class AbstractState implements Serializable {

	public static final Comparator<AbstractState> modelScoreComparator = new Comparator<AbstractState>() {

		@Override
		public int compare(AbstractState s1, AbstractState s2) {
			// TODO
			return (int) -Math.signum(s1.getModelScore() - s2.getModelScore());
		}
	};
	public static final Comparator<AbstractState> objectiveScoreComparator = new Comparator<AbstractState>() {

		@Override
		public int compare(AbstractState s1, AbstractState s2) {
			// TODO
			return (int) -Math.signum(s1.getObjectiveScore() - s2.getObjectiveScore());
		}
	};
	private static final DecimalFormat STATE_ID_FORMATTER = new DecimalFormat("000000000");

	private static AtomicInteger stateIDIndex = new AtomicInteger();
	protected double modelScore = 1;
	protected double objectiveScore = 0;
	protected FactorGraph factorGraph = new FactorGraph();
	protected final StateID id;

	public AbstractState() {
		this.id = generateStateID();

	}

	public void setModelScore(double modelScore) {
		this.modelScore = modelScore;
	}

	public double getObjectiveScore() {
		return objectiveScore;
	}

	public void setObjectiveScore(double score) {
		this.objectiveScore = score;
	}

	/**
	 * Returns a previously computed score.
	 * 
	 * @return
	 */
	public double getModelScore() {
		return modelScore;
	}

	public FactorGraph getFactorGraph() {
		return factorGraph;
	}

	public StateID getID() {
		return id;
	}

	private StateID generateStateID() {
		int currentID = stateIDIndex.getAndIncrement();
		String id = STATE_ID_FORMATTER.format(currentID);
		return new StateID(id);
	}
	// public abstract Multimap<VariableID, StateChange> getChangedVariables();

	// public abstract void markAsUnchanged();

	// /**
	// * This state should duplicate the whole state. Don't forget to create a
	// * cloned FactorGraph (like newFG = new FactorGraph(oldFG))
	// *
	// * @return
	// */
	// public abstract <StateT extends AbstractState> StateT duplicate();

	public abstract Document getDocument();

}

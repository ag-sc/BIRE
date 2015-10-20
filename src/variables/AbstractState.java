package variables;

import java.io.Serializable;
import java.util.Comparator;

import com.google.common.collect.Multimap;

import changes.StateChange;
import corpus.Document;
import factors.FactorGraph;
import utility.StateID;
import utility.VariableID;

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

	protected double modelScore = 1;
	protected double objectiveScore = 0;
	protected FactorGraph factorGraph = new FactorGraph();

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

	public abstract StateID getID();

	public abstract Multimap<VariableID, StateChange> getChangedVariables();

	public abstract void markAsUnchanged();

	public abstract <StateT extends AbstractState> StateT duplicate();

	public abstract Document getDocument();

}

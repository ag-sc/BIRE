package Variables;

import java.io.Serializable;
import java.util.Comparator;

import com.google.common.collect.Multimap;

import Changes.StateChange;
import Corpus.Document;
import Factors.FactorGraph;
import Learning.Score;
import utility.EntityID;
import utility.StateID;

public interface IState extends Serializable {

	public static final Comparator<IState> modelScoreComparator = new Comparator<IState>() {

		@Override
		public int compare(IState s1, IState s2) {
			return (int) -Math.signum(s1.getModelScore() - s2.getModelScore());
		}
	};
	public static final Comparator<IState> objectiveScoreComparator = new Comparator<IState>() {

		@Override
		public int compare(IState s1, IState s2) {
			return (int) -Math.signum(s1.getObjectiveScore().score - s2.getObjectiveScore().score);
		}
	};

	public StateID getID();

	public FactorGraph getFactorGraph();

	public void setModelScore(double score);

	public double getModelScore();

	public Multimap<EntityID, StateChange> getChangedVariables();

	public Score getObjectiveScore();

	public void markAsUnchanged();

	public <StateT extends IState> StateT duplicate();

	public Document getDocument();

}

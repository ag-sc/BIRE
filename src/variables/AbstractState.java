package variables;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import factors.FactorGraph;
import utility.StateID;

public abstract class AbstractState implements Serializable {

	private static Logger log = LogManager.getFormatterLogger();
	public static final Comparator<AbstractState> modelScoreComparator = new Comparator<AbstractState>() {

		@Override
		public int compare(AbstractState s1, AbstractState s2) {
			// TODO
			return -Double.compare(s1.getModelScore(), s2.getModelScore());
		}
	};
	public static final Comparator<AbstractState> objectiveScoreComparator = new Comparator<AbstractState>() {

		@Override
		public int compare(AbstractState s1, AbstractState s2) {
			// TODO
			return -Double.compare(s1.getObjectiveScore(), s2.getObjectiveScore());
		}
	};
	private static final DecimalFormat STATE_ID_FORMATTER = new DecimalFormat("000000000");

	private static AtomicInteger stateIDIndex = new AtomicInteger();
	protected double modelScore = 1;
	protected double objectiveScore = 0;
	protected FactorGraph factorGraph = new FactorGraph();
	protected final StateID id;

	/**
	 * The AbstractState is the super class of all state implementations. It
	 * provides functions to store and retrieve model and objective scores. It
	 * contains a factor graph that stores its factors. It provides a unique
	 * state ID.
	 */
	public AbstractState() {
		this.id = generateStateID();

	}

	public void setModelScore(double modelScore) {
		if (Double.isNaN(modelScore)) {
			log.error("Model score of state with ID %s is NaN. State: %s", id, this);
		} else if (Double.isInfinite(modelScore)) {
			log.warn("Model score of state with ID %s is infinite. State: %s", id, this);
		}
		this.modelScore = modelScore;
	}

	public double getObjectiveScore() {
		return objectiveScore;
	}

	public void setObjectiveScore(double score) {
		if (Double.isNaN(score)) {
			log.error("Objective score of state with ID %s is NaN. State: %s", id, this);
		} else if (Double.isInfinite(score)) {
			log.warn("Objective score of state with ID %s is infinite. State: %s", id, this);
		}
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

}

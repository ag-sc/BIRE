package variables;

import java.io.Serializable;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import corpus.Instance;
import factors.FactorGraph;
import utility.StateID;

public abstract class AbstractState<InstanceT extends Instance> implements Serializable {

	/**
	 * A comparator implementation that allows to sort states in descending
	 * order with respect to their model score.
	 */
	public static final Comparator<AbstractState<?>> modelScoreComparator = new Comparator<AbstractState<?>>() {

		@Override
		public int compare(AbstractState<?> s1, AbstractState<?> s2) {
			return -Double.compare(s1.getModelScore(), s2.getModelScore());
		}
	};
	/**
	 * A comparator implementation that allows to sort states in descending
	 * order with respect to their objective score.
	 */
	public static final Comparator<AbstractState<?>> objectiveScoreComparator = new Comparator<AbstractState<?>>() {

		@Override
		public int compare(AbstractState<?> s1, AbstractState<?> s2) {
			return -Double.compare(s1.getObjectiveScore(), s2.getObjectiveScore());
		}
	};

	private static Logger log = LogManager.getFormatterLogger();
	private final static AtomicLong stateIDIndex = new AtomicLong();
	protected double modelScore = 1;
	protected double objectiveScore = 0;
	protected FactorGraph factorGraph = new FactorGraph();
	protected final InstanceT instance;
	protected final StateID id;

	/**
	 * The AbstractState is the super class of all state implementations. It
	 * provides functions to store and retrieve model and objective scores. It
	 * contains a factor graph that stores its factors. It provides a unique
	 * state ID.
	 */
	public AbstractState(InstanceT instance) {
		this.id = generateStateID();
		this.instance = instance;

	}

	public AbstractState(AbstractState<InstanceT> state) {
		this(state.instance);
		this.modelScore = state.modelScore;
		this.objectiveScore = state.objectiveScore;
		this.factorGraph = new FactorGraph(state.factorGraph);
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

	public void resetFactorGraph() {
		factorGraph = new FactorGraph();
	}

	public InstanceT getInstance() {
		return instance;
	}

	public StateID getID() {
		return id;
	}

	private StateID generateStateID() {
		long currentID = stateIDIndex.getAndIncrement();
		String id = "S" + String.valueOf(currentID);
		return new StateID(id);
	}

}

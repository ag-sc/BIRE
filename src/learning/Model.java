package learning;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import corpus.Instance;
import factors.Factor;
import factors.FactorGraph;
import factors.FactorPattern;
import factors.FactorPool;
import templates.AbstractTemplate;
import utility.Utils;
import variables.AbstractState;

public class Model<InstanceT extends Instance, StateT extends AbstractState<InstanceT>> implements Serializable {

	private static Logger log = LogManager.getFormatterLogger(Model.class.getName());

	private Collection<AbstractTemplate<InstanceT, StateT, ?>> templates;
	private boolean forceFactorComputation = false;

	private boolean multiThreaded = true;

	/**
	 * The model contains a collection of templates that can generate factors
	 * (and features) for a given state. The model's weights are stored in the
	 * templates and shared across all their respective factors.
	 * 
	 * @param templates
	 */
	public Model(Collection<AbstractTemplate<InstanceT, StateT, ?>> templates) {
		this.templates = templates;
	}

	public boolean isForceFactorComputation() {
		return forceFactorComputation;
	}

	/**
	 * If set to true, all possible factors that can be created for a given
	 * state are always recomputed when the model is applied to a state. This
	 * means that there is no efficient handling of already computed factors.
	 * 
	 * @param forceFactorComputation
	 */
	public void setForceFactorComputation(boolean forceFactorComputation) {
		this.forceFactorComputation = forceFactorComputation;
	}

	public boolean isMultiThreaded() {
		return multiThreaded;
	}

	public void setMultiThreaded(boolean multiThreaded) {
		this.multiThreaded = multiThreaded;
	}

	/**
	 * Loads a collection of templates (each with its respective weight vector)
	 * from the given file.
	 * 
	 * @param file
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void loadModelfromFile(String file) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		templates = (Collection<AbstractTemplate<InstanceT, StateT, ?>>) in.readObject();
		in.close();
	}

	/**
	 * Stores the collection of templates (each with its respective weight
	 * vector) to the specified file.
	 * 
	 * @param file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void saveModelToFile(String file) throws FileNotFoundException, IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
		out.writeObject(templates);
		out.close();
	}

	public Collection<AbstractTemplate<InstanceT, StateT, ?>> getTemplates() {
		return templates;
	}

	public void applyToStates(List<StateT> states, FactorPool factorPool, InstanceT instance) {
		log.info("Apply %s templates to %s states.", templates.size(), states.size());
		for (StateT state : states) {
			state.getFactorGraph().clear();
		}
		Stream<AbstractTemplate<InstanceT, StateT, ?>> stream = Utils.getStream(templates, multiThreaded);
		stream.forEach(t -> applyTemplate(t, states, factorPool, instance));
	}

	private <FactorPatternT extends FactorPattern> Set<FactorPatternT> applyTemplate(
			AbstractTemplate<InstanceT, StateT, FactorPatternT> t, List<StateT> states, FactorPool factorPool,
			InstanceT instance) {
		log.info("Apply template \"%s\" to %s states.", t.getClass().getSimpleName(), states.size());
		/*
		 * Collect all pattern of all states to which this template can be
		 * applied (in parallel).
		 */
		Set<FactorPatternT> allGeneratedPatternsForTemplate = generatePatternsAndAddToStates(t, states);

		log.info("%s possible Factors for template %s", allGeneratedPatternsForTemplate.size(),
				t.getClass().getSimpleName());

		/*
		 * Select only new patterns (or all if forced) for computation.
		 */
		log.info("Compute %s factors ...", forceFactorComputation ? "ALL" : "NEW");
		Set<FactorPatternT> patternsToCompute = null;
		if (forceFactorComputation) {
			patternsToCompute = allGeneratedPatternsForTemplate;
		} else {
			/*
			 * Extract only the ones which are not already associate with a
			 * factor.
			 */
			Set<FactorPatternT> newPatternsForTemplate = factorPool
					.extractNewFactorPatterns(allGeneratedPatternsForTemplate);
			log.info("%s new Factors for template %s", newPatternsForTemplate.size(), t.getClass().getSimpleName());

			patternsToCompute = newPatternsForTemplate;
		}

		/*
		 * Compute all selected factors (in parallel).
		 */
		Set<Factor<FactorPatternT>> newFactors = computeNewFactors(t, patternsToCompute, instance);

		factorPool.addFactors(newFactors);
		return allGeneratedPatternsForTemplate;
	}

	private <FactorPatternT extends FactorPattern> Set<FactorPatternT> generatePatternsAndAddToStates(
			AbstractTemplate<InstanceT, StateT, FactorPatternT> t, List<StateT> states) {
		Stream<StateT> stream = Utils.getStream(states, multiThreaded);
		Set<FactorPatternT> allGeneratedPatternsForTemplate = ConcurrentHashMap.newKeySet();

		stream.forEach(state -> {
			log.debug("Apply template \"%s\" to state %s. Force recomputation: %s", t.getClass().getSimpleName(),
					state.getID(), forceFactorComputation);
			log.debug("%s", state);
			Set<FactorPatternT> generatedPatternsForState = t.generateFactorPatterns(state);
			log.debug("%s possible Factors for state %s", generatedPatternsForState.size(), state.getID());

			FactorGraph factorGraph = state.getFactorGraph();
			factorGraph.addFactorPatterns(generatedPatternsForState);
			allGeneratedPatternsForTemplate.addAll(generatedPatternsForState);
		});
		return allGeneratedPatternsForTemplate;
	}

	private <FactorPatternT extends FactorPattern> Set<Factor<FactorPatternT>> computeNewFactors(
			AbstractTemplate<InstanceT, StateT, FactorPatternT> t, Set<FactorPatternT> patterns, InstanceT instance) {

		Stream<FactorPatternT> stream = Utils.getStream(patterns, multiThreaded);

		Set<Factor<FactorPatternT>> factors = stream.map(p -> {
			Factor<FactorPatternT> f = new Factor<>(p);
			t.computeFactor(instance, f);
			return f;
		}).collect(Collectors.toSet());

		return factors;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (AbstractTemplate<?, StateT, ?> template : templates) {
			builder.append(template.getClass().getSimpleName());
			builder.append("\n");
			builder.append("\t#Features: ");
			builder.append(template.getWeightVector().getFeatureNames().size());
			builder.append("\n");
		}
		return builder.toString();
	}

	public String toDetailedString() {
		StringBuilder builder = new StringBuilder();
		for (AbstractTemplate<?, StateT, ?> template : templates) {
			builder.append(template.getClass().getSimpleName());
			builder.append(" (#Features: ");
			builder.append(template.getWeightVector().getFeatureNames().size());
			builder.append(")");
			builder.append("\n");
			for (String weight : template.getWeightVector().getFeatureNames()) {
				builder.append("\t");
				builder.append(weight);
				builder.append(" : ");
				builder.append(template.getWeightVector().getValueOfFeature(weight));
				builder.append("\n");
			}
		}
		return builder.toString();
	}

}

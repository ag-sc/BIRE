package learning;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import factors.Factor;
import factors.FactorGraph;
import factors.FactorPattern;
import templates.AbstractTemplate;
import variables.AbstractState;

public class Model<StateT extends AbstractState<?>> implements Serializable {

	private static Logger log = LogManager.getFormatterLogger(Model.class.getName());

	private Collection<AbstractTemplate<?, StateT, ?>> templates;
	private boolean forceFactorComputation = false;

	/**
	 * The model contains a collection of templates that can generate factors
	 * (and features) for a given state. The model's weights are stored in the
	 * templates and shared across all their respective factors.
	 * 
	 * @param templates
	 */
	public Model(Collection<AbstractTemplate<?, StateT, ?>> templates) {
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
		templates = (Collection<AbstractTemplate<?, StateT, ?>>) in.readObject();
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

	public Collection<AbstractTemplate<?, StateT, ?>> getTemplates() {
		return templates;
	}

	/**
	 * Computes all necessary factors for the templates of this model. After the
	 * call to this method, all factors for a given state can be retrieved using
	 * the getFactors() method on its respective factor graph.
	 * 
	 * @param state
	 */
	public void applyTo(StateT state) {
		Set<FactorPattern> allGeneratedFactorPatterns = new HashSet<>();
		for (AbstractTemplate<?, StateT, ?> t : templates) {
			Set<? extends FactorPattern> generatedPatternsOfTemplate = t.applyTo(state, forceFactorComputation);
			allGeneratedFactorPatterns.addAll(generatedPatternsOfTemplate);
		}

		FactorGraph factorGraph = state.getFactorGraph();
		factorGraph.setFactorPatterns(allGeneratedFactorPatterns);
	}

	// public void unroll(StateT state) {
	// Set<FactorPattern> allGeneratedFactorPatterns = new HashSet<>();
	// for (AbstractTemplate<?, StateT, ?> t : templates) {
	// Set<? extends FactorPattern> generatedPatternsOfTemplate =
	// t.generateFactorPatterns(state);
	// allGeneratedFactorPatterns.addAll(generatedPatternsOfTemplate);
	// }
	//
	// FactorGraph factorGraph = state.getFactorGraph();
	// factorGraph.setFactorPatterns(allGeneratedFactorPatterns);
	// // factorGraph.removeObsoleteFactors(allGeneratedFactorPatterns);
	// }

	// private <FactorPatternT extends FactorPattern> Set<FactorPatternT>
	// applyTemplate(
	// AbstractTemplate<?, StateT, FactorPatternT> template, StateT state) {
	// log.debug("Apply template \"%s\" to state %s. Force recomputation: %s",
	// this.getClass().getSimpleName(),
	// state.getID(), force);
	// log.debug("%s", state);
	// FactorGraph factorGraph = state.getFactorGraph();
	//
	// Set<FactorPatternT> allPossibleFactors =
	// template.generateFactorPatterns(state);
	// log.debug("%s possible Factors: %s", allPossibleFactors.size(),
	// allPossibleFactors);
	//
	// Set<FactorPatternT> newFactorPatterns =
	// factorGraph.extractNewFactorPatterns(allPossibleFactors);
	// log.debug("%s new Factors: %s", allPossibleFactors.size(),
	// allPossibleFactors);
	//
	// log.debug("Compute new Factors ...");
	// Set<Factor<FactorPatternT>> newFactors = newFactorPatterns.stream().map(p
	// -> new Factor<>(p))
	// .collect(Collectors.toSet());
	// newFactors.forEach(p -> template.computeFactor(state.getInstance(), p));
	// factorGraph.addFactors(newFactors);
	//
	// }

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

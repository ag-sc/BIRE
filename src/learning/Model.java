package learning;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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
import exceptions.UnkownTemplateRequestedException;
import factors.Factor;
import factors.FactorGraph;
import factors.FactorPattern;
import factors.FactorPool;
import learning.scorer.Scorer;
import templates.AbstractTemplate;
import templates.TemplateFactory;
import utility.Utils;
import variables.AbstractState;

public class Model<InstanceT extends Instance, StateT extends AbstractState<InstanceT>> implements Serializable {

	private static final String TEMPLATE_WEIGHTS_FILE_SUFFIX = ".weights.tsv";

	private static Logger log = LogManager.getFormatterLogger(Model.class.getName());

	private Collection<AbstractTemplate<InstanceT, StateT, ?>> templates;
	private boolean forceFactorComputation = false;

	private boolean multiThreaded = true;

	private boolean sequentialScoring = false;

	protected Scorer scorer;

	/**
	 * The model contains a collection of templates that can generate factors
	 * (and features) for a given state. The model's weights are stored in the
	 * templates and shared across all their respective factors.
	 *
	 * @param templates
	 */
	public Model(Scorer scorer, Collection<AbstractTemplate<InstanceT, StateT, ?>> templates) {
		this.scorer = scorer;
		this.templates = templates;
	}

	public Model(Scorer scorer) {
		this(scorer, new ArrayList<>());
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

	public boolean isSequentialScoring() {
		return sequentialScoring;
	}

	public void setSequentialScoring(boolean sequentialScoring) {
		this.sequentialScoring = sequentialScoring;
	}

	public Scorer getScorer() {
		return scorer;
	}

	public void setScorer(Scorer scorer) {
		this.scorer = scorer;
	}

	public void loadModelFromDir(String modelDirPath, TemplateFactory<InstanceT, StateT> factory)
			throws FileNotFoundException, IOException, ClassNotFoundException, UnkownTemplateRequestedException,
			Exception {

		File modelDir = new File(modelDirPath);
		loadModelFromDir(modelDir, factory);
	}

	public void loadModelFromDir(File modelDir, TemplateFactory<InstanceT, StateT> factory)
			throws FileNotFoundException, IOException, UnkownTemplateRequestedException, Exception {
		log.info("Load model from directory %s ...", modelDir);
		File[] templateFiles = modelDir.listFiles(f -> f.getName().endsWith(TEMPLATE_WEIGHTS_FILE_SUFFIX));
		for (File templateFile : templateFiles) {
			AbstractTemplate<InstanceT, StateT, ?> template = loadTemplateWeights(templateFile, factory);
			templates.add(template);
		}
		log.info("Model successfully loaded!");
	}

	private AbstractTemplate<InstanceT, StateT, ?> loadTemplateWeights(File templateFile,
			TemplateFactory<InstanceT, StateT> factory)
					throws IOException, UnkownTemplateRequestedException, Exception {
		System.out.println(templateFile.getName());
		System.out.println(Arrays.toString(templateFile.getName().split("\\.", 2)));
		String templateName = templateFile.getName().split("\\.", 2)[0];

		log.info("Load Template %s from file %s ...", templateName, templateFile);

		AbstractTemplate<InstanceT, StateT, ?> template = factory.newInstance(templateName);

		Vector weights = template.getWeights();

		FileReader fReader = new FileReader(templateFile);
		BufferedReader bReader = new BufferedReader(fReader);

		bReader.lines().forEach(line -> {
			String[] parts = line.split("\t", 2);
			String featureName = parts[0];
			Double featureValue = Double.valueOf(parts[1]);
			weights.set(featureName, featureValue);
		});
		bReader.close();

		log.info("%s feature weights restored.", weights.getFeatureNames().size());
		log.info("Template successfully loaded!");
		return template;
	}

	public void saveModelToFile(String modelsDirPath, String modelDirName) throws FileNotFoundException, IOException {
		File modelsDir = new File(modelsDirPath);
		saveModelToFile(modelsDir, modelDirName);
	}

	public void saveModelToFile(File modelsDir, String modelDirName) throws FileNotFoundException, IOException {
		File modelDir = new File(modelsDir, modelDirName);
		saveModelToFile(modelDir);
	}

	public void saveModelToFile(String modelDirPath) throws FileNotFoundException, IOException {
		File modelDir = new File(modelDirPath);
		saveModelToFile(modelDir);
	}

	public void saveModelToFile(File modelDir) throws FileNotFoundException, IOException {
		if (!modelDir.exists()) {
			modelDir.mkdirs();
		}

		log.info("Save Model to directory %s ...", modelDir);
		for (AbstractTemplate<InstanceT, StateT, ?> template : templates) {
			saveTemplateWeights(modelDir, template);
		}
		log.info("Model successfully saved!");
	}

	private void saveTemplateWeights(File modelDir, AbstractTemplate<InstanceT, StateT, ?> template)
			throws IOException {
		File templateFile = new File(modelDir, template.getClass().getSimpleName() + TEMPLATE_WEIGHTS_FILE_SUFFIX);
		log.info("Save Template %s to file %s ...", template.getClass().getSimpleName(), templateFile);

		FileWriter fWriter = new FileWriter(templateFile);
		BufferedWriter bWriter = new BufferedWriter(fWriter);
		for (Entry<String, Double> feature : template.getWeights().getFeatures().entrySet()) {
			bWriter.write(feature.getKey() + "\t" + feature.getValue());
			bWriter.newLine();
		}
		bWriter.flush();
		bWriter.close();
		log.info("Template successfully saved!");
	}

	public Collection<AbstractTemplate<InstanceT, StateT, ?>> getTemplates() {
		return templates;
	}

	public void score(List<StateT> states, InstanceT instance, FactorPool factorPool) {
		if (sequentialScoring) {
			for (StateT state : states) {
				log.debug("Apply %s templates to %s state.", templates.size(), state.getID());
				state.getFactorGraph().clear();
				factorPool.clear();

				List<StateT> stateAsList = Arrays.asList(state);
				for (AbstractTemplate<InstanceT, StateT, ?> t : templates) {
					applyTemplate(t, stateAsList, factorPool, instance);
				}
				scorer.score(stateAsList, multiThreaded);
			}
		} else {
			applyToStates(states, factorPool, instance);
			scorer.score(states, multiThreaded);
		}
	}

	public void applyToStates(List<StateT> states, FactorPool factorPool, InstanceT instance) {
		log.debug("Apply %s templates to %s states.", templates.size(), states.size());
		for (StateT state : states) {
			state.getFactorGraph().clear();
		}
		if (forceFactorComputation) {
			factorPool.clear();
		}

		Stream<AbstractTemplate<InstanceT, StateT, ?>> stream = Utils.getStream(templates, multiThreaded);
		stream.forEach(t -> {
			applyTemplate(t, states, factorPool, instance);
		});
	}

	private <FactorPatternT extends FactorPattern> Set<FactorPatternT> applyTemplate(
			AbstractTemplate<InstanceT, StateT, FactorPatternT> t, List<StateT> states, FactorPool factorPool,
			InstanceT instance) {
		log.debug("Apply template \"%s\" to %s states.", t.getClass().getSimpleName(), states.size());
		/*
		 * Collect all pattern of all states to which this template can be
		 * applied (in parallel).
		 */
		Set<FactorPatternT> allGeneratedPatternsForTemplate = generatePatternsAndAddToStates(t, states);

		log.debug("%s possible Factors for template %s", allGeneratedPatternsForTemplate.size(),
				t.getClass().getSimpleName());

		/*
		 * Select only new patterns (or all if forced) for computation.
		 */
		log.debug("Compute %s factors ...", forceFactorComputation ? "ALL" : "NEW");
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
			log.debug("%s new Factors for template %s", newPatternsForTemplate.size(), t.getClass().getSimpleName());

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
			log.trace("Apply template \"%s\" to state %s. Force recomputation: %s", t.getClass().getSimpleName(),
					state.getID(), forceFactorComputation);
			log.trace("%s", state);
			List<FactorPatternT> generatedPatternsForState = t.generateFactorPatterns(state);
			log.trace("%s possible Factors for state %s", generatedPatternsForState.size(), state.getID());

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
			builder.append(template.getWeights().getFeatureNames().size());
			builder.append("\n");
		}
		return builder.toString();
	}

	public String toDetailedString() {
		StringBuilder builder = new StringBuilder();
		for (AbstractTemplate<?, StateT, ?> template : templates) {
			builder.append(template.getClass().getSimpleName());
			builder.append(" (#Features: ");
			builder.append(template.getWeights().getFeatureNames().size());
			builder.append(")");
			builder.append("\n");
			for (String weight : template.getWeights().getFeatureNames()) {
				builder.append("\t");
				builder.append(weight);
				builder.append(" : ");
				builder.append(template.getWeights().getValueOfFeature(weight));
				builder.append("\n");
			}
		}
		return builder.toString();
	}

}

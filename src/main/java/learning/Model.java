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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import exceptions.UnkownTemplateRequestedException;
import factors.Factor;
import factors.FactorGraph;
import factors.FactorPool;
import factors.FactorScope;
import learning.scorer.Scorer;
import templates.AbstractTemplate;
import templates.TemplateFactory;
import utility.Utils;
import variables.AbstractState;

public class Model<InstanceT, StateT extends AbstractState<InstanceT>> implements Serializable {

	private static final String TEMPLATE_WEIGHTS_FILE_SUFFIX = ".weights.tsv";

	private static Logger log = LogManager.getFormatterLogger(Model.class.getName());

	private FactorPool sharedFactorPool = FactorPool.getInstance();
	private Collection<AbstractTemplate<InstanceT, StateT, ?>> templates;
	private boolean forceFactorComputation = false;

	private boolean multiThreaded = true;

	@Deprecated
	private boolean sequentialScoring = false;

	protected Scorer scorer;

	/**
	 * The model contains a collection of templates that can generate factors (and
	 * features) for a given state. The model's weights are stored in the templates
	 * and shared across all their respective factors.
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
	 * If set to true, all possible factors that can be created for a given state
	 * are always recomputed when the model is applied to a state. This means that
	 * there is no efficient handling of already computed factors.
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

	@Deprecated
	public boolean isSequentialScoring() {
		return sequentialScoring;
	}

	@Deprecated
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
		List<Entry<String, Double>> sortedWeights = new ArrayList<>(template.getWeights().getFeatures().entrySet());
		Collections.sort(sortedWeights, (o1, o2) -> -Double.compare(o1.getValue(), o2.getValue()));
		for (Entry<String, Double> feature : sortedWeights) {
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

	public void score(List<StateT> states) {
		if (log.getLevel().equals(Level.DEBUG))
			log.debug("Apply %s templates to %s states.", templates.size(), states.size());
		/*
		 * New states: factor graph is empty anyway
		 */
//		for (StateT state : states) {
//			state.getFactorGraph().clear();
//		}

		/**
		 * DO NOT USE PARALLEL HERE: Nested
		 */
		for (AbstractTemplate<InstanceT, StateT, ?> template : templates) {
			applyTemplates(template, states);
		}

		scorer.score(states, multiThreaded);
	}

	private <FactorScopeT extends FactorScope> void applyTemplates(
			AbstractTemplate<InstanceT, StateT, FactorScopeT> template, List<StateT> states) {
		if (log.getLevel().equals(Level.DEBUG))
			log.debug("Apply template \"%s\" to %s states.", template.getClass().getSimpleName(), states.size());
		/*
		 * Collect all Factor scopes of all states to which this template can be applied
		 * (in parallel).
		 */
		Set<FactorScopeT> scopesToCompute = generateScopesAndAddToStates(template, states);
		if (log.getLevel().equals(Level.DEBUG)) {
			log.debug("%s possible Factors for template %s", scopesToCompute.size(),
					template.getClass().getSimpleName());

			/*
			 * Select only new scopes (or all if forced) for computation.
			 */
			log.debug("Compute %s factors ...", forceFactorComputation ? "ALL" : "NEW");
		}
//		Set<FactorScopeT> scopesToCompute = 
		filterScopesByChache(template, scopesToCompute);

		/*
		 * Compute all selected factors (in parallel).
		 */
		Set<Factor<FactorScopeT>> newFactors = computeNewFactors(template, scopesToCompute);

		sharedFactorPool.addFactors(newFactors);
	}

	private <FactorScopeT extends FactorScope> Set<FactorScopeT> filterScopesByChache(
			AbstractTemplate<InstanceT, StateT, FactorScopeT> template,
			Set<FactorScopeT> allGeneratedScopesForTemplate) {

//		Set<FactorScopeT> scopesToCompute = null;

		if (!forceFactorComputation) {
//		} else {
			/*
			 * Extract only the ones which are not already associate with a factor.
			 */
//			Set<FactorScopeT> newFactorScopesForTemplate =
			sharedFactorPool.filterNewFactorScopes(allGeneratedScopesForTemplate);
			if (log.getLevel().equals(Level.DEBUG))
				log.debug("%s new Factors for template %s", allGeneratedScopesForTemplate.size(),
						template.getClass().getSimpleName());
//			log.debug("%s new Factors for template %s", newFactorScopesForTemplate.size(),
//					template.getClass().getSimpleName());

//			scopesToCompute = allGeneratedScopesForTemplate;
		}
//		scopesToCompute = new HashSet<>(allGeneratedScopesForTemplate);

		return allGeneratedScopesForTemplate;
	}

//	static public AtomicLong counter = new AtomicLong(0);

	/**
	 * Parallel
	 * 
	 * @param template
	 * @param states
	 * @return
	 */
	private <FactorScopeT extends FactorScope> Set<FactorScopeT> generateScopesAndAddToStates(
			AbstractTemplate<InstanceT, StateT, FactorScopeT> template, List<StateT> states) {

		/**
		 * Tested performance: 3,3% of runtime for both cases.
		 */

//		long t = System.currentTimeMillis();

		Stream<StateT> stream = Utils.getStream(states, multiThreaded);
		final boolean logTrace = log.getLevel().equals(Level.TRACE);
		Set<FactorScopeT> allGeneratedScopesForTemplate = stream.map(state -> {
			if (logTrace) {
				log.trace("Apply template \"%s\" to state %s. Force recomputation: %s",
						template.getClass().getSimpleName(), state.getID(), forceFactorComputation);
				log.trace("%s", state);
			}
			List<FactorScopeT> generatedScopesForState = template.generateFactorScopes(state);
			if (logTrace) {
				log.trace("%s possible Factors for state %s", generatedScopesForState.size(), state.getID());
			}

			FactorGraph factorGraph = state.getFactorGraph();
			factorGraph.addFactorScopes(generatedScopesForState);
			return generatedScopesForState;
		}).flatMap(l -> l.stream()).collect(Collectors.toSet());

//		Set<FactorScopeT> allGeneratedScopesForTemplate = new HashSet<>();
//
//		for (StateT state : states) {
//
//			log.trace("Apply template \"%s\" to state %s. Force recomputation: %s", template.getClass().getSimpleName(),
//					state.getID(), forceFactorComputation);
//			log.trace("%s", state);
//			List<FactorScopeT> generatedScopesForState = template.generateFactorScopes(state);
//			log.trace("%s possible Factors for state %s", generatedScopesForState.size(), state.getID());
//
//			FactorGraph factorGraph = state.getFactorGraph();
//			factorGraph.addFactorScopes(generatedScopesForState);
//			allGeneratedScopesForTemplate.addAll(generatedScopesForState);
//		}

//		Stream<StateT> stream = Utils.getStream(states, multiThreaded);
//		Set<FactorScopeT> allGeneratedScopesForTemplate = ConcurrentHashMap.newKeySet();
//		stream.forEach(state -> {
//			log.trace("Apply template \"%s\" to state %s. Force recomputation: %s", template.getClass().getSimpleName(),
//					state.getID(), forceFactorComputation);
//			log.trace("%s", state);
//			List<FactorScopeT> generatedScopesForState = template.generateFactorScopes(state);
//			log.trace("%s possible Factors for state %s", generatedScopesForState.size(), state.getID());
//
//			FactorGraph factorGraph = state.getFactorGraph();
//			factorGraph.addFactorScopes(generatedScopesForState);
//			allGeneratedScopesForTemplate.addAll(generatedScopesForState);
//		});

//		long r = (System.currentTimeMillis() - t);
//
//		counter.addAndGet(r);

		return allGeneratedScopesForTemplate;
	}

	private <FactorScopeT extends FactorScope> Set<Factor<FactorScopeT>> computeNewFactors(
			AbstractTemplate<InstanceT, StateT, FactorScopeT> t, Set<FactorScopeT> scopes) {

		Stream<FactorScopeT> stream = Utils.getStream(scopes, multiThreaded);
		Set<Factor<FactorScopeT>> factors = stream.map(p -> {
			Factor<FactorScopeT> f = new Factor<>(p);
			t.computeFactor(f);
			/*
			 * Comment out but still visible through call hierarchy
			 */
			if (false)
				f.getFeatureVector().normalize();
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

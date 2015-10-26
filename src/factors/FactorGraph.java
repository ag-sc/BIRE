package factors;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import templates.AbstractTemplate;

public class FactorGraph implements Serializable {

	// private Map<FactorID, AbstractFactor> factorMap;
	private Multimap<AbstractTemplate<?>, AbstractFactor> factors;
	// private Set<AbstractFactor> factors;
	// private BiMap<AbstractFactor, FactorID> variableSet2Factor;
	// private Multimap<Template<?>, FactorID> template2Factors;
	// private Map<FactorID, Template<?>> factor2Template;
	// private Multimap<VariableID, AbstractFactor> variable2Factors;

	public FactorGraph() {
		init();
	}

	private void init() {
		// variableSet2Factor = HashBiMap.create();
		// template2Factors = HashMultimap.create();
		// factor2Template = new HashMap<>();
		// factorMap = new HashMap<>();
		factors = HashMultimap.create();
		// factors = new HashSet<>();
		// variable2Factors = HashMultimap.create();
	}

	public FactorGraph(FactorGraph factorGraph) {
		// this.variableSet2Factor =
		// HashBiMap.create(factorGraph.variableSet2Factor);
		// this.template2Factors =
		// HashMultimap.create(factorGraph.template2Factors);
		// this.factor2Template = new HashMap<>(factorGraph.factor2Template);
		// this.factorMap = new HashMap<>(factorGraph.factorMap);
		this.factors = HashMultimap.create(factorGraph.factors);
		// this.factors = new HashSet<>(factorGraph.factors);
		// this.variable2Factors =
		// HashMultimap.create(factorGraph.variable2Factors);
	}

	public void updateFactors(AbstractTemplate<?> template, Set<AbstractFactor> factors) {
//		this.factors.clear();
		this.factors.putAll(template, factors);
		// discard all factors that already exist
		// Set<AbstractFactor> newFactors = new HashSet<>(factors);
		// factors.removeAll(this.factors);
		// this.factors.addAll(newFactors);
	}

	public Set<AbstractFactor> getFactors() {
		return new HashSet<>(factors.values());
	}

	public void reset() {
		init();
	}

	// public void removeFactor(Template<?> template, AbstractFactor
	// variableSet) {
	// // FactorID factorID = variableSet2Factor.get(variableSet);
	// template2Factors.remove(template, factorID);
	// factor2Template.remove(factorID);
	// variableSet2Factor.remove(variableSet);
	// for (VariableID entityID : variableSet.getEntityIDs()) {
	// entity2VariableSets.remove(entityID, variableSet);
	// }
	// }
	//
	// public void addFactor(Template<?> template, FactorID factorID,
	// AbstractFactor variableSet) {
	// Log.d("Adding Factor to Graph: %s, %s, %s",
	// template.getClass().getSimpleName(), factorID, variableSet);
	// template2Factors.put(template, factorID);
	// factor2Template.put(factorID, template);
	// variableSet2Factor.put(variableSet, factorID);
	// for (VariableID entityID : variableSet.getEntityIDs()) {
	// entity2VariableSets.put(entityID, variableSet);
	// }
	// }
	//
	// public Set<FactorID> getFactorIDs(Template<?> template) {
	// /*
	// * TODO ensure that there are really no factors of foreign states in
	// * this graph.
	// */
	// return new HashSet<>(template2Factors.get(template));
	// }
	//
	// public AbstractFactor getVariableSetForFactor(FactorID factorID) {
	// return variableSet2Factor.inverse().get(factorID);
	// }
	//
	// public Set<AbstractFactor> getFactorsForVariableWithID(Template<?>
	// template, VariableID entityID) {
	// Set<FactorID> allFactorsForTemplate = new
	// HashSet<>(template2Factors.get(template));
	// Set<AbstractFactor> variablesOfTemplateForEntity = new HashSet<>();
	// for (FactorID factorID : allFactorsForTemplate) {
	// AbstractFactor v = variableSet2Factor.inverse().get(factorID);
	// if (v.getEntityIDs().contains(entityID)) {
	// variablesOfTemplateForEntity.add(v);
	// }
	// }
	//
	// return variablesOfTemplateForEntity;
	// }
	//
	// @Override
	// public String toString() {
	// StringBuilder builder = new StringBuilder();
	// builder.append(String.format("### Template: Factors... ###"));
	// builder.append("\n");
	// for (Template<?> t : template2Factors.keySet()) {
	// builder.append(String.format("\t%s: %s", t.getClass().getSimpleName(),
	// template2Factors.get(t)));
	//
	// builder.append("\n");
	// }
	// // builder.append(String.format("### Factor: Template ###"));
	// // builder.append("\n");
	// // for (FactorID f : factor2Template.keySet()) {
	// // builder.append(String.format("\t%s: %s", f,
	// // factor2Template.get(f).getClass().getSimpleName()));
	// //
	// // builder.append("\n");
	// // }
	// builder.append(String.format("### Factor: VariableSet ###"));
	// builder.append("\n");
	// for (FactorID f : variableSet2Factor.inverse().keySet()) {
	// builder.append(String.format("\t%s: %s", f,
	// variableSet2Factor.inverse().get(f)));
	// builder.append("\n");
	// }
	// builder.append(String.format("### Entity: VariableSet ###"));
	// builder.append("\n");
	// for (VariableID e : entity2VariableSets.keySet()) {
	// builder.append(String.format("\t%s: %s", e, entity2VariableSets.get(e)));
	// builder.append("\n");
	// }
	// return builder.toString();
	// }

}

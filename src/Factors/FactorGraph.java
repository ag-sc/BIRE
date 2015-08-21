package Factors;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import Logging.Log;
import Templates.Template;
import Templates.variablesets.VariableSet;
import utility.EntityID;
import utility.FactorID;

public class FactorGraph implements Serializable {
	{
		Log.off();
	}

	private BiMap<VariableSet, FactorID> variableSet2Factor;
	private Multimap<Template, FactorID> template2Factors;
	private Map<FactorID, Template> factor2Template;
	private Multimap<EntityID, VariableSet> entity2VariableSets;

	public FactorGraph() {
		variableSet2Factor = HashBiMap.create();
		template2Factors = HashMultimap.create();
		factor2Template = new HashMap<>();
		entity2VariableSets = HashMultimap.create();
	}

	public FactorGraph(FactorGraph factorGraph) {
		this.variableSet2Factor = HashBiMap.create(factorGraph.variableSet2Factor);
		this.template2Factors = HashMultimap.create(factorGraph.template2Factors);
		this.factor2Template = new HashMap<>(factorGraph.factor2Template);
		this.entity2VariableSets = HashMultimap.create(factorGraph.entity2VariableSets);
	}

	public void removeFactorForVariableSet(Template template, VariableSet variableSet) {
		FactorID factorID = variableSet2Factor.get(variableSet);
		template2Factors.remove(template, factorID);
		factor2Template.remove(factorID);
		variableSet2Factor.remove(variableSet);
		for (EntityID entityID : variableSet.getEntityIDs()) {
			entity2VariableSets.remove(entityID, variableSet);
		}
	}

	public void addFactor(Template template, FactorID factorID, VariableSet variableSet) {
		Log.d("Adding Factor to Graph: %s, %s, %s", template.getClass().getSimpleName(), factorID, variableSet);
		template2Factors.put(template, factorID);
		factor2Template.put(factorID, template);
		variableSet2Factor.put(variableSet, factorID);
		for (EntityID entityID : variableSet.getEntityIDs()) {
			entity2VariableSets.put(entityID, variableSet);
		}
	}

	public Set<FactorID> getFactorIDs(Template template) {
		/*
		 * TODO ensure that there are really no factors of foreign states in
		 * this graph.
		 */
		return new HashSet<>(template2Factors.get(template));
	}

	public VariableSet getVariableSetForFactor(FactorID factorID) {
		return variableSet2Factor.inverse().get(factorID);
	}

	public Set<VariableSet> getVariableSetsForEntityID(Template template, EntityID entityID) {
		Set<FactorID> allFactorsForTemplate = new HashSet<>(template2Factors.get(template));
		Set<VariableSet> variablesOfTemplateForEntity = new HashSet<>();
		for (FactorID factorID : allFactorsForTemplate) {
			VariableSet v = variableSet2Factor.inverse().get(factorID);
			if (v.getEntityIDs().contains(entityID)) {
				variablesOfTemplateForEntity.add(v);
			}
		}

		return variablesOfTemplateForEntity;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("### Template: Factors... ###"));
		builder.append("\n");
		for (Template t : template2Factors.keySet()) {
			builder.append(String.format("\t%s: %s", t.getClass().getSimpleName(), template2Factors.get(t)));

			builder.append("\n");
		}
		// builder.append(String.format("### Factor: Template ###"));
		// builder.append("\n");
		// for (FactorID f : factor2Template.keySet()) {
		// builder.append(String.format("\t%s: %s", f,
		// factor2Template.get(f).getClass().getSimpleName()));
		//
		// builder.append("\n");
		// }
		builder.append(String.format("### Factor: VariableSet ###"));
		builder.append("\n");
		for (FactorID f : variableSet2Factor.inverse().keySet()) {
			builder.append(String.format("\t%s: %s", f, variableSet2Factor.inverse().get(f)));
			builder.append("\n");
		}
		builder.append(String.format("### Entity: VariableSet ###"));
		builder.append("\n");
		for (EntityID e : entity2VariableSets.keySet()) {
			builder.append(String.format("\t%s: %s", e, entity2VariableSets.get(e)));
			builder.append("\n");
		}
		return builder.toString();
	}

}

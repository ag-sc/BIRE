package examples.weather;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;

import factors.Factor;
import factors.patterns.SingleVariablePattern;
import learning.Vector;
import templates.AbstractTemplate;

public class PlayOutsideTemplate
		extends AbstractTemplate<WeatherInstance, PlayOutsideState, SingleVariablePattern<PlayOutsideVariable>> {
	private static Logger log = LogManager.getFormatterLogger();

	private int maxCombinedAttributes = 2;

	public PlayOutsideTemplate(int maxCombinedAttributes) {
		this.maxCombinedAttributes = maxCombinedAttributes;
	}

	@Override
	public Set<SingleVariablePattern<PlayOutsideVariable>> generateFactorPatterns(PlayOutsideState state) {
		Set<SingleVariablePattern<PlayOutsideVariable>> factors = new HashSet<>();
		factors.add(new SingleVariablePattern<>(this, state.getPlayOutsideVariable()));
		return factors;
	}

	@Override
	protected void computeFactor(WeatherInstance instance, Factor<SingleVariablePattern<PlayOutsideVariable>> factor) {
		Vector features = factor.getFeatureVector();
		boolean isPlayingOutside = factor.getFactorPattern().getVariable().isPlayingOutside();

		Set<String> conds = new HashSet<String>();

		conds.add("Outlook=" + instance.getOutlook().name());
		conds.add("Temperatur=" + instance.getTemperature().name());
		conds.add("Humidity=" + instance.getHumidity().name());
		conds.add("Windy=" + instance.getWindy().name());

		/*
		 * Create all combinations of all attributes to create features.
		 */
		Set<Set<String>> powerSet = Sets.powerSet(conds);
		/*
		 * Keep only attribute combination that maximally combine
		 * "maxCombinedAttributes" attributes.
		 */
		powerSet = powerSet.stream().filter(s -> s.size() <= this.maxCombinedAttributes).collect(Collectors.toSet());

		for (Set<String> set : powerSet) {
			features.set("Is_playing_outside=" + isPlayingOutside + "_" + set.toString(), 1.0);

		}

	}

}

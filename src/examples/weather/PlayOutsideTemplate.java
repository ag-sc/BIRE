package examples.weather;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;

import examples.weather.PlayOutsideTemplate.PlayOutsideFactorVariables;
import examples.weather.WeatherInstance.Humidity;
import examples.weather.WeatherInstance.Outlook;
import examples.weather.WeatherInstance.Temperature;
import examples.weather.WeatherInstance.Windy;
import factors.Factor;
import factors.FactorVariables;
import learning.Vector;
import templates.AbstractTemplate;

public class PlayOutsideTemplate extends AbstractTemplate<WeatherInstance, PlayOutsideState, PlayOutsideFactorVariables> {
	class PlayOutsideFactorVariables extends FactorVariables {
		public boolean playOutside;
		public Outlook outlook;
		public Temperature temperatur;
		public Humidity humidity;
		public Windy windy;

		public PlayOutsideFactorVariables(AbstractTemplate<?, ?, ?> template, boolean playOutside, Outlook outlook,
				Temperature temperatur, Humidity humidity, Windy windy) {
			super(template, playOutside, outlook, temperatur, humidity, windy);
			this.playOutside = playOutside;
			this.outlook = outlook;
			this.temperatur = temperatur;
			this.humidity = humidity;
			this.windy = windy;
		}

		@Override
		public String toString() {
			return "PlayOutsideFactorPattern [playOutside=" + playOutside + ", outlook=" + outlook + ", temperatur="
					+ temperatur + ", humidity=" + humidity + ", windy=" + windy + "]";
		}

	}

	private static Logger log = LogManager.getFormatterLogger();

	private int maxCombinedAttributes = 2;

	public PlayOutsideTemplate(int maxCombinedAttributes) {
		this.maxCombinedAttributes = maxCombinedAttributes;
	}

	@Override
	public List<PlayOutsideFactorVariables> generateFactorVariables(PlayOutsideState state) {
		List<PlayOutsideFactorVariables> factors = new ArrayList<>();
		factors.add(new PlayOutsideFactorVariables(this, state.isPlayingOutside(), state.getInstance().outlook,
				state.getInstance().temperature, state.getInstance().humidity, state.getInstance().windy));
		return factors;
	}

	@Override
	public void computeFactor(Factor<PlayOutsideFactorVariables> factor) {
		Vector features = factor.getFeatureVector();
		Set<String> conds = new HashSet<String>();

		conds.add("Outlook=" + factor.getFactorVariables().outlook.name());
		conds.add("Temperatur=" + factor.getFactorVariables().temperatur.name());
		conds.add("Humidity=" + factor.getFactorVariables().humidity.name());
		conds.add("Windy=" + factor.getFactorVariables().windy.name());

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
			features.set("Is_playing_outside=" + factor.getFactorVariables().playOutside + "_" + set.toString(), 1.0);
		}

	}

}

package examples.weather;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;

import examples.weather.WeatherInstance.Humidity;
import examples.weather.WeatherInstance.Outlook;
import examples.weather.WeatherInstance.Temperature;
import examples.weather.WeatherInstance.Windy;
import factors.AbstractFactor;
import factors.impl.SingleVariableFactor;
import learning.Vector;
import templates.AbstractTemplate;

public class PlayOutsideTemplate extends AbstractTemplate<PlayOutsideState> {
	private static Logger log = LogManager.getFormatterLogger();

	private int maxCombinedAttributes = 2;

	public PlayOutsideTemplate(int maxCombinedAttributes) {
		this.maxCombinedAttributes = maxCombinedAttributes;
	}

	@Override
	protected void computeFactor(PlayOutsideState state, AbstractFactor factor) {
		if (factor instanceof SingleVariableFactor) {
			Vector features = new Vector();
			boolean isPlayingOutside = state.getPlayOutsideVariable().isPlayingOutside();

			Set<String> conds = new HashSet<String>();

			conds.add("Outlook=" + state.getWeatherInstance().getOutlook().name());
			conds.add("Temperatur=" + state.getWeatherInstance().getTemperature().name());
			conds.add("Humidity=" + state.getWeatherInstance().getHumidity().name());
			conds.add("Windy=" + state.getWeatherInstance().getWindy().name());

			/*
			 * Create all combinations of all attributes to create features.
			 */
			Set<Set<String>> powerSet = Sets.powerSet(conds);
			/*
			 * Keep only attribute combination that maximally combine "maxCombinedAttributes" attributes. 
			 */
			powerSet = powerSet.stream().filter(s -> s.size() <= this.maxCombinedAttributes)
					.collect(Collectors.toSet());

			for (Set<String> set : powerSet) {
				features.set("Is_playing_outside=" + isPlayingOutside + "_" + set.toString(), 1.0);

			}

			// features.set("Is_playing_outside=" + isPlayingOutside +
			// "_AND_Outlook_is_"
			// + state.getWeatherInstance().getOutlook(), 1.0);
			// features.set("Is_playing_outside=" + isPlayingOutside +
			// "_AND_Temperature_is_"
			// + state.getWeatherInstance().getTemperature(), 1.0);
			// features.set("Is_playing_outside=" + isPlayingOutside +
			// "_AND_Humidity_is_"
			// + state.getWeatherInstance().getHumidity(), 1.0);
			// features.set(
			// "Is_playing_outside=" + isPlayingOutside + "_AND_Windy_is_" +
			// state.getWeatherInstance().getWindy(),
			// 1.0);

			// for (Outlook w : Outlook.values()) {
			// boolean isWeather = state.getWeatherInstance().getOutlook() == w;
			// // features.set("Is_playing_outside_AND_Outlook_is_" + w,
			// // isWeather && isPlayingOutside);
			// features.set("Is_playing_outside=" + isPlayingOutside +
			// "_AND_Outlook_is_" + w, isWeather);
			// }
			// for (Temperature w : Temperature.values()) {
			// boolean isTemperature =
			// state.getWeatherInstance().getTemperature() == w;
			// // features.set("Is_playing_outside_AND_Temperatur_is_" + w,
			// // isTemperature && isPlayingOutside);
			// features.set("Is_playing_outside=" + isPlayingOutside +
			// "_AND_Temperature_is_" + w, isTemperature);
			// }
			// for (Humidity w : Humidity.values()) {
			// boolean isHumidity = state.getWeatherInstance().getHumidity() ==
			// w;
			// // features.set("Is_playing_outside_AND_Humidity_is_" + w,
			// // isHumidity && isPlayingOutside);
			// features.set("Is_playing_outside=" + isPlayingOutside +
			// "_AND_Humidity_is_" + w, isHumidity);
			// }
			// for (Windy w : Windy.values()) {
			// boolean isWindy = state.getWeatherInstance().getWindy() == w;
			// // features.set("Is_playing_outside_AND_Windy_is_" + w, isWindy
			// // && isPlayingOutside);
			// features.set("Is_playing_outside=" + isPlayingOutside +
			// "_AND_Windy_is_" + w, isWindy);
			// }
			// features.set("Is_playing_outside", isPlayingOutside);
			factor.setFeatures(features);
		} else {
			log.warn("Received Factor is not of the expected type");
		}
	}

	@Override
	protected Collection<AbstractFactor> generateFactors(PlayOutsideState state) {
		List<AbstractFactor> factors = new ArrayList<>();
		factors.add(new SingleVariableFactor(this, state.getPlayOutsideVariable().getID()));
		return factors;
	}

}

package examples.weather;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import factors.AbstractFactor;
import factors.impl.SingleVariableFactor;
import learning.Vector;
import templates.AbstractTemplate;

public class PlayOutsideTemplate extends AbstractTemplate<PlayOutsideState> {
	private static Logger log = LogManager.getFormatterLogger();

	@Override
	protected void computeFactor(PlayOutsideState state, AbstractFactor factor) {
		if (factor instanceof SingleVariableFactor) {
			Vector features = new Vector();
			boolean isPlayingOutside = state.getPlayOutsideVariable().isPlayingOutside();
			for (WeatherCondition w : WeatherCondition.values()) {
				boolean isWeather = state.getWeatherInstance().getWeather().contains(w);
				features.set("Is_playing_outside_AND_Weather_is_" + w, isWeather && isPlayingOutside);
			}
			features.set("Is_playing_outside", isPlayingOutside);
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

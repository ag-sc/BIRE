package examples.weather;

import java.util.Set;

import corpus.LabeledInstance;

public class PlayOutsideInstance extends WeatherInstance implements LabeledInstance<Boolean> {
	public PlayOutsideInstance(Set<WeatherCondition> weather, boolean playOutside) {
		super(weather);
		this.playOutside = playOutside;
	}

	private boolean playOutside;

	@Override
	public Boolean getGoldResult() {
		return playOutside;
	}

	@Override
	public String toString() {
		return "[" + weather + ", playOutside=" + playOutside + "]";
	}
}

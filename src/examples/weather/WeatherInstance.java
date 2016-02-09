package examples.weather;

import java.util.Set;

import corpus.Instance;

public class WeatherInstance implements Instance {
	protected Set<WeatherCondition> weather;

	public WeatherInstance(Set<WeatherCondition> weather) {
		super();
		this.weather = weather;
	}

	public Set<WeatherCondition> getWeather() {
		return weather;
	}

	@Override
	public String toString() {
		return weather.toString();
	}

}

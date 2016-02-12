package examples.weather;

import java.util.Set;

import corpus.LabeledInstance;
import examples.weather.WeatherInstance.Humidity;
import examples.weather.WeatherInstance.Outlook;
import examples.weather.WeatherInstance.Temperature;
import examples.weather.WeatherInstance.Windy;

public class PlayOutsideInstance extends WeatherInstance implements LabeledInstance<Boolean> {
	public PlayOutsideInstance(Outlook outlook, Temperature temperature, Humidity humidity, Windy windy,
			boolean playOutside) {
		super(outlook, temperature, humidity, windy);
		this.playOutside = playOutside;
	}

	private boolean playOutside;

	@Override
	public Boolean getGoldResult() {
		return playOutside;
	}

	@Override
	public String toString() {
		return "[outlook=" + outlook + ", temperature=" + temperature + ", humidity=" + humidity + ", windy=" + windy
				+ ", playOutside=" + playOutside + "]";
	}

}

package examples.weather;

public class WeatherInstance {
	enum Outlook {
		OVERCAST, SUNNY, RAINY;
	}

	enum Temperature {
		HOT, MILD, COOL;
	}

	enum Humidity {
		HIGH, NORMAL;
	}

	enum Windy {
		TRUE, FALSE;
	}

	protected Outlook outlook;
	protected Temperature temperature;
	protected Humidity humidity;
	protected Windy windy;

	public WeatherInstance(Outlook outlook, Temperature temperature, Humidity humidity, Windy windy) {
		this.outlook = outlook;
		this.temperature = temperature;
		this.humidity = humidity;
		this.windy = windy;
	}

	public Outlook getOutlook() {
		return outlook;
	}

	public void setOutlook(Outlook outlook) {
		this.outlook = outlook;
	}

	public Temperature getTemperature() {
		return temperature;
	}

	public void setTemperature(Temperature temperature) {
		this.temperature = temperature;
	}

	public Humidity getHumidity() {
		return humidity;
	}

	public void setHumidity(Humidity humidity) {
		this.humidity = humidity;
	}

	public Windy getWindy() {
		return windy;
	}

	public void setWindy(Windy windy) {
		this.windy = windy;
	}

	@Override
	public String toString() {
		return "[outlook=" + outlook + ", temperature=" + temperature + ", humidity=" + humidity + ", windy=" + windy
				+ "]";
	}

}

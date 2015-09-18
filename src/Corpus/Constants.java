package Corpus;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Constants {

	public static final Properties BIONLP_PROPERTIES = new Properties();

	static {
		try {
			BIONLP_PROPERTIES.load(new FileInputStream("res/dataset.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getBioNLP2013ConfigFilepath() {
		return BIONLP_PROPERTIES.getProperty("BIONLP_2013_CONFIG_FILEPATH");
	}

	public static String getBioNLPSamplesConfigFilepath() {
		return BIONLP_PROPERTIES.getProperty("BIONLP_SAMPLES_CONFIG_FILEPATH");
	}

	public static String getBioNLP2013DevPath() {
		return BIONLP_PROPERTIES.getProperty("BIONLP_2013_DEV_DIRPATH");
	}

	public static String getBioNLP2013TestPath() {
		return BIONLP_PROPERTIES.getProperty("BIONLP_2013_TEST_DIRPATH");
	}

	public static String getBioNLP2013TrainPath() {
		return BIONLP_PROPERTIES.getProperty("BIONLP_2013_TRAIN_DIRPATH");
	}

	public static String getBioNLP2013TrainJavaBinFilepath() {
		return BIONLP_PROPERTIES.getProperty("BIONLP_2013_TRAIN_JAVA_BIN_CORPUS_FILEPATH");
	}

	public static String getBioNLP2013TestJavaBinFilepath() {
		return BIONLP_PROPERTIES.getProperty("BIONLP_2013_TEST_JAVA_BIN_CORPUS_FILEPATH");
	}

	public static String getBioNLP2013DevJavaBinFilepath() {
		return BIONLP_PROPERTIES.getProperty("BIONLP_2013_DEV_JAVA_BIN_CORPUS_FILEPATH");
	}

	public static String getUSAGEJavaBinFilepath() {
		return BIONLP_PROPERTIES.getProperty("USAGE_JAVA_BIN_CORPUS_FILEPATH");
	}
}

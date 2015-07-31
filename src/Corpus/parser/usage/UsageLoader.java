package Corpus.parser.usage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Corpus.AnnotatedDocument;
import Corpus.AnnotationConfig;
import Corpus.BratConfigReader;
import Corpus.BratCorpus;
import Corpus.Constants;
import Corpus.Corpus;
import Corpus.julie.JavaSentenceSplitter;
import Corpus.julie.Tokenization;
import Logging.Log;

public class UsageLoader {

	public static void main(String[] args) {
		convertDatasetToJavaBinaries(Constants.JAVA_BIN_USAGE_CORPUS_FILEPATH);
	}

	public static Corpus loadDatasetFromBinaries(String srcFilepath)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				srcFilepath));
		Corpus corpus = (Corpus) in.readObject();
		in.close();
		return corpus;
	}

	public static Corpus convertDatasetToJavaBinaries(String destFilepath) {
		File annDir = new File("res/usage/de");
		Corpus corpus = UsageParser.parseCorpus(annDir);
		try {
			System.out.println("store");
			saveCorpusToFile(corpus, destFilepath);
			Log.d("Corpus (%s documents) successfully parsed and stored to file \"%s\"",
					corpus.getDocuments().size(), destFilepath);
			System.out.println("done!");
			return corpus;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void saveCorpusToFile(Corpus corpus, String destFilepath)
			throws FileNotFoundException, IOException {
		File destFile = new File(destFilepath);
		File destDir = destFile.getParentFile();

		if (!destDir.exists() && !destDir.mkdirs()) {
			throw new IllegalStateException("Couldn't create dir: " + destDir);
		}
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(
				destFile));
		os.writeObject(corpus);
		os.close();
	}
}
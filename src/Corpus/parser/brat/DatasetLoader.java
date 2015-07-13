package Corpus.parser.brat;

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
import Corpus.julie.SentenceSplitter;
import Corpus.julie.Tokenization;
import Logging.Log;

public class DatasetLoader {

	public static void main(String[] args) {
		convertDatasetToJavaBinaries(Constants.JAVA_BIN_CORPUS_FILEPATH);
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
		File annDir = new File("res/bionlp/ann");
		File textDir = new File("res/bionlp/text");
		File sentencesDir = new File("res/bionlp/julie/sentences");
		File tokensDir = new File("res/bionlp/julie/tokens");
		String modelFilepath = "res/bionlp/julie/models/JULIE_life-science-1.6.mod.gz";
		String configFilepath = "res/bionlp/annotation.conf";

		BratConfigReader confReader = new BratConfigReader();
		Log.d("### Annotation configuration:");
		AnnotationConfig config = confReader.readConfig(configFilepath);
		Log.d("%s", config);

		Map<String, File> annotationFiles = getMapOfFiles(annDir);
		Map<String, File> textFiles = getMapOfFiles(textDir);

		// create a set of documents for which we have both the annotations file
		// and the raw text file.
		Set<String> completeDocumnts = new HashSet<String>(
				annotationFiles.keySet());
		completeDocumnts.retainAll(textFiles.keySet());
		Log.d("%s documents with a given text and annotation file",
				completeDocumnts.size());
		Log.d("filesnames: %s", completeDocumnts);
		Corpus corpus = new BratCorpus(config);
		int current = 1;
		for (String filename : completeDocumnts) {
			Log.d("#############################");
			Log.d("#############################");
			Log.d("parse document \"%s\" (%s/%s)", filename, current,
					completeDocumnts.size());
			File annFile = annotationFiles.get(filename);
			File textFile = textFiles.get(filename);
			try {

				// get the filename without extensions
				Log.d("#####################");
				Log.d("### Brat annotations:");
				BratAnnotationParser parser = new BratAnnotationParser();
				BratAnnotatedDocument bratDoc = parser.parseFile(
						annFile.getPath(), textFile.getPath());
				Log.d("%s", bratDoc);

				Log.d("#####################");
				Log.d("### Text splitted in sentences:");

				File sentFile = new File(sentencesDir, textFile.getName());
				SentenceSplitter.getSentencesAndStore(
						new File(textFile.getPath()), sentFile.getPath());

				Log.d("#####################");
				Log.d("### Tokenization of sentences:");
				List<Tokenization> tokenizations = Tokenization
						.extractAndSaveTokens(filename, tokensDir.getPath(),
								sentFile.getPath(), modelFilepath);

				Log.d("#####################");
				Log.d("### BIRE annotations:");
				List<AnnotatedDocument> documents = Brat2BIREConverter.convert(
						bratDoc, corpus, tokenizations);
				// Log.d("%s", doc.getGoldState().toDetailedString());
				corpus.addDocuments(documents);
				current++;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

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

	private static Map<String, File> getMapOfFiles(File dir) {
		Map<String, File> files = new HashMap<String, File>();
		for (File file : dir.listFiles()) {
			String fileBasename = getFilenameWithoutExtension(file.getName());
			files.put(fileBasename, file);
		}
		return files;
	}

	public static String getFilenameWithoutExtension(String name) {

		return name.replaceFirst("[.][^.]+$", "");
	}
}

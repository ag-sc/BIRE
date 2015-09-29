package Corpus.parser.brat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Corpus.AnnotationConfig;
import Corpus.BioNLPCorpus;
import Corpus.DatasetConfig;
import Corpus.SubDocument;
import Corpus.julie.JavaSentenceSplitter;
import Corpus.julie.Tokenization;
import Corpus.parser.FileUtils;
import Corpus.parser.brat.exceptions.AnnotationFileException;
import Logging.Log;

public class BioNLPLoader {

	public static final File TOKEN_MODEL_FILE = new File("res/bionlp/julie/models/JULIE_life-science-1.6-token.mod.gz");
	public static final File SENTENCE_MODEL_FILE = new File(
			"res/bionlp/julie/models/JULIE_life-science-1.6-sentence.mod.gz");

	public static void main(String[] args) {
		BioNLPCorpus train = loadBioNLP2013Train(false);
		BioNLPCorpus dev = loadBioNLP2013Dev(false);
		Log.d("##### BioNLP 2013 #####");
		Log.d("Train: %s documents.", train.getDocuments().size());
		Log.d("Dev: %s documents.", dev.getDocuments().size());
	}

	public static BioNLPCorpus setupCorpus(File configFile) {
		BratConfigReader confReader = new BratConfigReader();
		Log.d("### Annotation configuration:");
		AnnotationConfig config = confReader.readConfig(configFile);
		Log.d("%s", config);
		return new BioNLPCorpus(config);
	}

	public static BioNLPCorpus defaultCorpus() {
		return setupCorpus(new File(DatasetConfig.getBioNLP2013ConfigFilepath()));
	}

	public static BioNLPCorpus loadBioNLP2013Train(boolean forceParsing) {
		return loadBioNLP2013(DatasetConfig.getBioNLP2013TrainPath(), DatasetConfig.getBioNLP2013TrainJavaBinFilepath(),
				forceParsing);
	}

	public static BioNLPCorpus loadBioNLP2013Dev(boolean forceParsing) {
		return loadBioNLP2013(DatasetConfig.getBioNLP2013DevPath(), DatasetConfig.getBioNLP2013DevJavaBinFilepath(),
				forceParsing);
	}

	// public static Corpus loadBioNLP2013Test() {
	// return loadBioNLP2013(Constants.getBioNLP2013TestPath(),
	// Constants.getBioNLP2013TestJavaBinFilepath());
	// }

	private static BioNLPCorpus loadBioNLP2013(String dirpath, String serializationFilepath, boolean forceParsing) {
		if (!forceParsing) {
			try {
				return loadDatasetFromBinaries(serializationFilepath);
			} catch (Exception e) {
			}
		}
		List<File> texts = FileUtils.getFiles(new File(dirpath), "txt");
		List<File> annotationsA1 = FileUtils.getFiles(new File(dirpath), "a1");
		List<File> annotationsA2 = FileUtils.getFiles(new File(dirpath), "a2");
		return convertDatasetToJavaBinaries(texts, annotationsA1, annotationsA2, serializationFilepath);
	}

	public static BioNLPCorpus loadDatasetFromBinaries(String srcFilepath)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(srcFilepath));
		BioNLPCorpus corpus = (BioNLPCorpus) in.readObject();
		in.close();
		return corpus;
	}

	/**
	 * This method parses the BioNLP documents that are given as parameters.
	 * 
	 * @param destFilepath
	 * @return
	 */
	public static BioNLPCorpus convertDatasetToJavaBinaries(List<File> texts, List<File> annotationsA1,
			List<File> annotationsA2, String destFilepath) {
		BioNLPCorpus corpus = defaultCorpus();

		Map<String, File> annotationA1Files = getMapOfFiles(annotationsA1);
		Map<String, File> annotationA2Files = getMapOfFiles(annotationsA2);
		Map<String, File> textFiles = getMapOfFiles(texts);

		// create a set of documents for which we have both the annotations file
		// and the raw text file.
		Set<String> completeDocuments = new HashSet<String>(textFiles.keySet());
		completeDocuments.retainAll(annotationA1Files.keySet());
		completeDocuments.retainAll(annotationA2Files.keySet());

		Log.d("%s documents with a given text and annotation files", completeDocuments.size());
		Log.d("filesnames: %s", completeDocuments);

		int current = 1;
		for (String filename : completeDocuments) {
			Log.d("#############################");
			Log.d("#############################");
			Log.d("parse document \"%s\" (%s/%s)", filename, current, completeDocuments.size());

			File annA1File = annotationA1Files.get(filename);
			File annA2File = annotationA2Files.get(filename);
			File textFile = textFiles.get(filename);
			try {
				loadDocuments(corpus, textFile, Arrays.asList(annA1File, annA2File));
			} catch (Exception e1) {
				e1.printStackTrace();
				Log.w("Parsing of files for %s not possible. Skip this instance", filename);
			}
			current++;
		}
		if (destFilepath != null) {
			try {
				System.out.println("store");
				saveCorpusToFile(corpus, destFilepath);
				Log.d("Corpus (%s documents) successfully parsed and stored to file \"%s\"",
						corpus.getDocuments().size(), destFilepath);
				System.out.println("done!");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return corpus;
	}

	/**
	 * Parses the provided files and adds the extracted documents to the corpus.
	 * Since each single sentence is considered as a document, this methods
	 * might add several documents to the corpus.
	 * 
	 * @param corpus
	 * @param textFile
	 * @param sentFile
	 * @param annFiles
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static void loadDocuments(BioNLPCorpus corpus, File textFile, List<File> annFiles)
			throws FileNotFoundException, ClassNotFoundException, IOException {

		Log.d("#####################");
		Log.d("### Brat annotations...");
		BratAnnotationParser parser = new BratAnnotationParser();
		BratAnnotatedDocument bratDoc = parser.parseFile(textFile, annFiles);

		Log.d("#####################");
		Log.d("### Text splitting in sentences...");
		List<String> sentences = JavaSentenceSplitter.getSentencesAsList(textFile);
		Log.d("#####################");
		Log.d("### Tokenization of sentences...");
		List<Tokenization> tokenizations = Tokenization.extractTokens(sentences, TOKEN_MODEL_FILE.getPath());

		Log.d("#####################");
		Log.d("### BIRE annotations...");
		List<SubDocument> documents;
		try {
			documents = Brat2BIREConverter.convert(bratDoc, corpus, tokenizations);
			corpus.addDocuments(documents);
		} catch (AnnotationFileException e) {
			e.printStackTrace();
		}
	}

	public static BioNLPCorpus loadDocument(File textFile, List<File> annFiles)
			throws FileNotFoundException, ClassNotFoundException, IOException {
		BioNLPCorpus corpus = defaultCorpus();
		loadDocuments(corpus, textFile, annFiles);
		return corpus;
	}

	private static void saveCorpusToFile(BioNLPCorpus corpus, String destFilepath)
			throws FileNotFoundException, IOException {
		File destFile = new File(destFilepath);
		FileUtils.makeParents(destFile);
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(destFile));
		os.writeObject(corpus);
		os.close();
	}

	private static Map<String, File> getMapOfFiles(List<File> annotationsA1) {
		Map<String, File> files = new HashMap<String, File>();
		for (File file : annotationsA1) {
			String fileBasename = FileUtils.getFilenameWithoutExtension(file.getName());
			files.put(fileBasename, file);
		}
		return files;
	}

}

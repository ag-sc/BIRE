package Corpus.julie;

import java.io.File;
import java.io.IOException;

import de.julielab.jsbd.SentenceSplitterApplication;

public class JulieSentenceSplitter {

	public static void extractAndStoreSentences(File srcFile, File destFile,
			File sentenceModelFile) throws IOException {
		SentenceSplitterApplication.doPrediction(srcFile, destFile,
				sentenceModelFile.getPath());
	}


}

package Corpus;

import java.util.List;

public class BratCorpus implements Corpus {

	List<Document> documents;
	AnnotationConfig corpusConfig;

	@Override
	public AnnotationConfig getCorpusConfig() {
		// TODO Auto-generated method stub
		return corpusConfig;
	}

}

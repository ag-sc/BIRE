package Corpus;

import java.util.ArrayList;
import java.util.List;

public class BratCorpus implements Corpus {

	List<Document> documents = new ArrayList<Document>();
	AnnotationConfig corpusConfig;

	public BratCorpus(AnnotationConfig config) {
		this.corpusConfig = config;
	}

	@Override
	public AnnotationConfig getCorpusConfig() {
		return corpusConfig;
	}

	@Override
	public List<Document> getDocuments() {
		return documents;
	}

	public void addDocument(Document doc) {
		documents.add(doc);
	}

}

package Corpus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BratCorpus implements Corpus {

	List<AnnotatedDocument> documents = new ArrayList<AnnotatedDocument>();
	AnnotationConfig corpusConfig;

	public BratCorpus(AnnotationConfig config) {
		this.corpusConfig = config;
	}

	@Override
	public AnnotationConfig getCorpusConfig() {
		return corpusConfig;
	}

	@Override
	public List<AnnotatedDocument> getDocuments() {
		return documents;
	}

	@Override
	public void addDocument(AnnotatedDocument doc) {
		this.documents.add(doc);
	}

	@Override
	public void addDocuments(Collection<AnnotatedDocument> documents) {
		this.documents.addAll(documents);
	}

	@Override
	public String toString() {
		return "BratCorpus [corpusConfig=" + corpusConfig + ", #documents="
				+ documents.size() + "]";
	}

}

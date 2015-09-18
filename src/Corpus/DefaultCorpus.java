package Corpus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultCorpus<T extends AnnotatedDocument> implements Corpus<T> {

	private List<T> documents = new ArrayList<>();
	private AnnotationConfig corpusConfig;

	public DefaultCorpus(AnnotationConfig config) {
		this.corpusConfig = config;
	}

	@Override
	public AnnotationConfig getCorpusConfig() {
		return corpusConfig;
	}

	@Override
	public List<T> getDocuments() {
		return documents;
	}

	@Override
	public void addDocument(T doc) {
		this.documents.add(doc);
	}

	@Override
	public void addDocuments(Collection<T> documents) {
		this.documents.addAll(documents);
	}

	@Override
	public String toString() {
		return "BratCorpus [corpusConfig=" + corpusConfig + ", #documents=" + documents.size() + "]";
	}

	public String toDetailedString() {
		StringBuilder builder = new StringBuilder();
		for (AnnotatedDocument doc : documents) {
			builder.append(doc.getName());
			builder.append("\n\t");
			builder.append(doc.getContent());
			builder.append("\n\t");
			builder.append(doc.getTokens());
			builder.append("\n\t");
			builder.append(doc.getGoldState());
			builder.append("\n");
		}
		return "BratCorpus [corpusConfig=" + corpusConfig + ", #documents=" + documents.size() + ", documents=\n"
				+ builder.toString() + "]";
	}

}
package Corpus;

import java.util.List;

public interface Corpus {
	public AnnotationConfig getCorpusConfig();
	public List<Document> getDocuments();

}

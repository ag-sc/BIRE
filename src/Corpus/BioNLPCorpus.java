package Corpus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class BioNLPCorpus implements Corpus<SubDocument> {

	private Multimap<String, SubDocument> documents = HashMultimap.create();
	private AnnotationConfig corpusConfig;

	public BioNLPCorpus(AnnotationConfig config) {
		this.corpusConfig = config;
	}

	@Override
	public AnnotationConfig getCorpusConfig() {
		return corpusConfig;
	}

	@Override
	public List<SubDocument> getDocuments() {
		return new ArrayList<>(documents.values());
	}

	public List<SubDocument> getSubDocuments(String parentDocumentName) {
		return new ArrayList<>(documents.get(parentDocumentName));
	}

	public List<SubDocument> getSubDocuments(Collection<String> parentDocumentNames) {
		List<SubDocument> subDocuments = new ArrayList<>();
		for (String p : parentDocumentNames) {
			subDocuments.addAll(documents.get(p));
		}
		return subDocuments;
	}

	public List<String> getParentDocuments() {
		return new ArrayList<>(documents.keySet());
	}

	@Override
	public void addDocument(SubDocument doc) {
		this.documents.put(doc.parentDocumentName, doc);
	}

	@Override
	public void addDocuments(Collection<SubDocument> documents) {
		documents.forEach(d -> this.documents.put(d.getParentDocumentName(), d));
	}

	@Override
	public String toString() {
		return "BratCorpus [corpusConfig=" + corpusConfig + ", #documents=" + documents.size() + "]";
	}

	public String toDetailedString() {
		StringBuilder builder = new StringBuilder();
		for (SubDocument doc : documents.values()) {
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

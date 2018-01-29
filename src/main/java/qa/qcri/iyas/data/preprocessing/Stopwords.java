package qa.qcri.iyas.data.preprocessing;

import java.util.HashSet;
import java.util.Set;

public class Stopwords {
	
	public final static String STOPWORD_EN = "/stopwords/stoplist-en.txt"; 
	
//	public final static String STOPWORD_IT = "/stopwords/stoplist-it.txt"; 
	
//	public final static String STOPWORD_AR = "/stopwords/stoplist-ar.txt"; 
	
	private Set<String> stopwords;
	
	public Stopwords(String path) {
		this.stopwords = new HashSet<>();
		ReadFile in = new ReadFile(this.getClass().getResource(path));
		while(in.hasNextLine()) {
			String word = in.nextLine().trim();
			this.stopwords.add(word);
		}
		in.close();
	}
	
	public Stopwords() {
		this.stopwords = new HashSet<>();
	}
	
	public boolean contains(String word) {
		return this.stopwords.contains(word);
	}
	
	public boolean add(String word) {
		return this.stopwords.add(word);
	}
	
	public boolean isEmpty() {
		return this.stopwords.isEmpty();
	}
}

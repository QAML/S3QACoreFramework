package qa.qcri.iyas.data.preprocessing;

import org.apache.uima.fit.component.Resource_ImplBase;

public abstract class TextPreprocessor extends Resource_ImplBase {

	@Override
	public void afterResourcesInitialized() {
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void init() throws Exception {}
	public void close()  {}
	public abstract String preprocess(String text, String lang);
}

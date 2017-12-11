package qa.qcri.iyas;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.uima.aae.client.UimaAsynchronousEngine;
import org.apache.uima.adapter.jms.client.BaseUIMAAsynchronousEngine_impl;

public class RunStandardFeatures {

	public static void main(String[] args) throws Exception {
		UimaAsynchronousEngine uimaAsEngine = new BaseUIMAAsynchronousEngine_impl();
		
		// create a Map to hold required parameters
		Map<String,Object> appCtx = new HashMap<String,Object>();
		appCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,System.getenv("UIMA_HOME") + "/bin/dd2spring.xsl");
		appCtx.put(UimaAsynchronousEngine.SaxonClasspath,"file:" + System.getenv("UIMA_HOME") + "/saxon/saxon8.jar");
		
		// Deploy service
		List<String> IDs = new LinkedList<String>();
		for (int i=0;i<1;i++) {
			IDs.add(uimaAsEngine.deploy(
					new File("resources/descriptors/qa/qcri/iyas/features/StandardFeatureExtractorAE_DeploymentDescriptor.xml").getAbsolutePath(), appCtx));
		}
		
	}
}

package qa.qcri.iyas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.apache.uima.aae.client.UimaAsynchronousEngine;
import org.apache.uima.adapter.jms.client.BaseUIMAAsynchronousEngine_impl;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.EntityProcessStatus;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceProcessException;

import qa.qcri.iyas.data.readers.InputCollectionDataReader;
import qa.qcri.iyas.features.InputJCasMultiplier;
import qa.qcri.iyas.types.Comment;
import qa.qcri.iyas.types.RelatedQuestionBody;
import qa.qcri.iyas.types.RelatedQuestionSubject;
import qa.qcri.iyas.types.UserQuestionBody;
import qa.qcri.iyas.types.UserQuestionSubject;

public class RunStandardPreprocessor {

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
					new File("resources/descriptors/qa/qcri/iyas/data/preprocessing/StandardPreprocessingAE_DeploymentDescriptor.xml").getAbsolutePath(), appCtx));
		}
	}

}

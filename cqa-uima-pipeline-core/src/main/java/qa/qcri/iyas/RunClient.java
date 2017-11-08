package qa.qcri.iyas;

import java.util.HashMap;

import java.util.Map;

import org.apache.uima.UIMAFramework;
import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.apache.uima.aae.client.UimaAsynchronousEngine;
import org.apache.uima.adapter.jms.client.BaseUIMAAsynchronousEngine_impl;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.collection.EntityProcessStatus;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.tcas.DocumentAnnotation;

import qa.qcri.iyas.types.Comment;
//import qa.qcri.iyas.types.RelatedQuestion;
import qa.qcri.iyas.types.RelatedQuestionBody;
import qa.qcri.iyas.types.RelatedQuestionSubject;
import qa.qcri.iyas.types.UserQuestion;
//import qa.qcri.iyas.types.UserQuestion;
import qa.qcri.iyas.types.UserQuestionBody;
import qa.qcri.iyas.types.UserQuestionSubject;

class ProcessingOutputListener extends UimaAsBaseCallbackListener {
	
	@Override
	public void entityProcessComplete(CAS cas, EntityProcessStatus aStatus) {
		try {
			if (!aStatus.getStatusMessage().equals("success")) {
				System.err.println(aStatus.getStatusMessage());
			} else {
				for (Annotation annotation : cas.getJCas().getAnnotationIndex()) {
					if (annotation instanceof UserQuestion) {
						@SuppressWarnings("unused")
						UserQuestion cqaAnnotation = (UserQuestion)annotation;
						System.out.println(cqaAnnotation.getID()+" complete ");
					} else 
					if (annotation instanceof UserQuestionSubject) {
						UserQuestionSubject cqaAnnotation = (UserQuestionSubject)annotation;
						System.out.println(cqaAnnotation.getID()+" subject ");
					} else if (annotation instanceof UserQuestionBody) {
						UserQuestionBody cqaAnnotation = (UserQuestionBody)annotation;
						System.out.println(cqaAnnotation.getID()+" body "+cqaAnnotation.getNumberOfCandidates());
					} else 
//					if (annotation instanceof RelatedQuestion) {
//						RelatedQuestion cqaAnnotation = (RelatedQuestion)annotation;
//						System.out.println(cqaAnnotation.getID()+" question "+cqaAnnotation.getNumberOfCandidates());
//					} else 
					if (annotation instanceof RelatedQuestionSubject) {
						RelatedQuestionSubject cqaAnnotation = (RelatedQuestionSubject)annotation;
						System.out.println(cqaAnnotation.getID()+" subject ");
					} else if (annotation instanceof RelatedQuestionBody) {
						RelatedQuestionBody cqaAnnotation = (RelatedQuestionBody)annotation;
						System.out.println(cqaAnnotation.getID()+" body "+cqaAnnotation.getNumberOfCandidates());
					} else if (annotation instanceof Comment) {
						Comment cqaAnnotation = (Comment)annotation;
						System.out.println(cqaAnnotation.getID()+" comment ");
					} else if (annotation instanceof DocumentAnnotation) {
						   
					} 
//					else {
//						for (Annotation ann : cas.getJCas().getAnnotationIndex()) {
//							System.err.println(ann.toString());
//						}
//						throw new RuntimeException("The input CAS must have only one annotation!",null);
//					}
				}
			}
		} catch (CASException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

public class RunClient {

	public static void main(String[] args) throws Exception {
		UimaAsynchronousEngine uimaAsEngine = new BaseUIMAAsynchronousEngine_impl();
		
		// create a Map to hold required parameters
		Map<String,Object> appCtx = new HashMap<String,Object>();
		appCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,System.getenv("UIMA_HOME") + "/bin/dd2spring.xsl");
		appCtx.put(UimaAsynchronousEngine.SaxonClasspath,"file:" + System.getenv("UIMA_HOME") + "/saxon/saxon8.jar");
		
		CollectionReaderDescription collectionReaderDescr = CollectionReaderFactory.createReaderDescriptionFromPath("resources/descriptors/qa/qcri/iyas/data/readers/InputCollectionDataReaderAE_Descriptor.xml");
		CollectionReader collectionReader = UIMAFramework.produceCollectionReader(collectionReaderDescr);
		
		uimaAsEngine.setCollectionReader(collectionReader);
		uimaAsEngine.addStatusCallbackListener(new ProcessingOutputListener());
		
		appCtx.put(UimaAsynchronousEngine.ServerUri, "tcp://localhost:61616");
		appCtx.put(UimaAsynchronousEngine.ENDPOINT, "myQueueName");
		appCtx.put(UimaAsynchronousEngine.CasPoolSize, 100);
		
		uimaAsEngine.initialize(appCtx);
		

		double start = System.currentTimeMillis();
		uimaAsEngine.process();
		double end = System.currentTimeMillis();
		double seconds = (end - start)/1000;
		System.out.println(seconds+" seconds");
		
		uimaAsEngine.stop();
		
	}

}

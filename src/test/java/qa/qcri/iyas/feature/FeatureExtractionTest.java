package qa.qcri.iyas.feature;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.activemq.broker.BrokerService;
import org.apache.uima.UIMAFramework;
import org.apache.uima.aae.client.UimaAsynchronousEngine;
import org.apache.uima.adapter.jms.client.BaseUIMAAsynchronousEngine_impl;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.InvalidXMLException;
import org.jdom2.JDOMException;
import org.junit.Test;
import org.xml.sax.SAXException;

import qa.qcri.iyas.DescriptorGenerator;
import qa.qcri.iyas.Starter;
import qa.qcri.iyas.TestDescriptorGenerator;
import qa.qcri.iyas.data.preprocessing.StandardPreprocessor;
import qa.qcri.iyas.data.reader.InputCollectionDataReader;
import qa.qcri.iyas.data.reader.PlainTextDataReader;
import qa.qcri.iyas.data.reader.XmlSemeval2016CqaEn;

public class FeatureExtractionTest {

	private String generateInputTestFile(boolean concatenate) throws IOException {
		StandardPreprocessor sp = new StandardPreprocessor();
		File file = new File("test_input.txt");
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		for (int k=1;k<=10;k++) {
			String qid = "Q"+k;
			String org_subject = "This is the subject of original question "+qid;
			String org_body = "This is the body of original question "+qid;
			
			Map<String,String> mapB = new HashMap<String,String>();
			if (concatenate) {
				mapB.put("body_"+qid, sp.concatenateBodyAndSubject(
						sp.preprocess(org_subject,"en"),
						sp.preprocess(org_body,"en"),false));
			} else {
				mapB.put("subject_"+qid, sp.preprocess(org_subject,"en"));
				mapB.put("body_"+qid, sp.preprocess(org_body,"en"));
			}
			for (int i=1;i<=10;i++) {
				out.write(qid+"	"+org_subject+"	"+org_body);
				String rid = qid+"_R"+i;
				String rel_subject = "This is the subject of related question "+rid;
				String rel_body = "This is the body of related question "+rid;
				out.write("	"+rid+"	"+rel_subject+"	"+rel_body);
				
				if (concatenate) {
					mapB.put("rel_body_"+rid, sp.concatenateBodyAndSubject(
							sp.preprocess(rel_subject,"en"),
							sp.preprocess(rel_body,"en"),false));
				} else {
					mapB.put("rel_subject_"+rid, sp.preprocess(rel_subject,"en"));
					mapB.put("rel_body_"+rid, sp.preprocess(rel_body,"en"));
				}
				
				Map<String,String> mapA = new HashMap<String,String>();
				
				if (concatenate) {
					mapA.put("body_"+rid, sp.concatenateBodyAndSubject(
							sp.preprocess(rel_subject,"en"), 
							sp.preprocess(rel_body,"en"),false));
				} else {
					mapA.put("subject_"+rid, sp.preprocess(rel_subject,"en"));
					mapA.put("body_"+rid, sp.preprocess(rel_body,"en"));
				}
				
				for (int j=1;j<=10;j++) {
					String cid = rid+"_C"+j;
					String comment = "This comment "+cid;
					out.write("	"+cid+"	"+comment);
					mapA.put("comment_"+cid, sp.preprocess(comment,"en"));
				}
				out.newLine();
				
			}
		}
		out.close();
		
		return file.getAbsolutePath();
	}
	
	private CollectionReaderDescription getCollectionReaderDescriptorTaskA(String file) throws ResourceInitializationException, IOException {
		CollectionReaderDescription collectionReaderDescr = CollectionReaderFactory.createReaderDescription(
				InputCollectionDataReader.class);
		ExternalResourceDescription reader = ExternalResourceFactory.createExternalResourceDescription(XmlSemeval2016CqaEn.class,
				XmlSemeval2016CqaEn.FILE_PARAM, file,
				XmlSemeval2016CqaEn.TASK_PARAM, XmlSemeval2016CqaEn.INSTANCE_A_TASK);
		ExternalResourceFactory.bindExternalResource(collectionReaderDescr, 
				InputCollectionDataReader.INPUT_READER_PARAM, reader);
		
		return collectionReaderDescr;
	}
	
	private CollectionReaderDescription getCollectionReaderDescriptorTaskB(String file) throws ResourceInitializationException, IOException {
		CollectionReaderDescription collectionReaderDescr = CollectionReaderFactory.createReaderDescription(
				InputCollectionDataReader.class);
		ExternalResourceDescription reader = ExternalResourceFactory.createExternalResourceDescription(XmlSemeval2016CqaEn.class,
				XmlSemeval2016CqaEn.FILE_PARAM, file,
				XmlSemeval2016CqaEn.TASK_PARAM, XmlSemeval2016CqaEn.INSTANCE_B_TASK);
		ExternalResourceFactory.bindExternalResource(collectionReaderDescr, 
				InputCollectionDataReader.INPUT_READER_PARAM, reader);
		
		return collectionReaderDescr;
	}
	
	private void runTestTaskA(boolean concatenate,String inputFile) throws Exception {
		UimaAsynchronousEngine uimaAsEngine = new BaseUIMAAsynchronousEngine_impl();

		Map<String,Object> appCtx = new HashMap<String,Object>();
		appCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,System.getenv("UIMA_HOME") + "/bin/dd2spring.xsl");
		appCtx.put(UimaAsynchronousEngine.SaxonClasspath,"file:" + System.getenv("UIMA_HOME") + "/saxon/saxon8.jar");
		appCtx.put(UimaAsynchronousEngine.ServerUri, "tcp://localhost:61616");
		appCtx.put(UimaAsynchronousEngine.ENDPOINT, "myQueueName");
		appCtx.put(UimaAsynchronousEngine.CasPoolSize, 100);
		
		CollectionReader collectionReaderA = UIMAFramework.produceCollectionReader(getCollectionReaderDescriptorTaskA(inputFile));
		TestFeatureExtractionStatusCallBackListener listenerA = new TestFeatureExtractionStatusCallBackListener();
		uimaAsEngine.addStatusCallbackListener(listenerA);
		
		uimaAsEngine.initialize(appCtx);
		
		double startA = System.currentTimeMillis();
		while (collectionReaderA.hasNext()) {
			CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
					null, null);
			collectionReaderA.getNext(cas);
			uimaAsEngine.sendCAS(cas);
		}
		uimaAsEngine.collectionProcessingComplete();
		double endA = System.currentTimeMillis();
		double secondsA = (endA - startA)/1000;
		System.out.println(secondsA+" seconds");
		
		uimaAsEngine.stop();
		
	}
	
	private void runTestTaskB(boolean concatenate,String inputFile) throws Exception {
		UimaAsynchronousEngine uimaAsEngine = new BaseUIMAAsynchronousEngine_impl();

		Map<String,Object> appCtx = new HashMap<String,Object>();
		appCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,System.getenv("UIMA_HOME") + "/bin/dd2spring.xsl");
		appCtx.put(UimaAsynchronousEngine.SaxonClasspath,"file:" + System.getenv("UIMA_HOME") + "/saxon/saxon8.jar");
		appCtx.put(UimaAsynchronousEngine.ServerUri, "tcp://localhost:61616");
		appCtx.put(UimaAsynchronousEngine.ENDPOINT, "myQueueName");
		appCtx.put(UimaAsynchronousEngine.CasPoolSize, 110);
		
		CollectionReader collectionReaderB = UIMAFramework.produceCollectionReader(getCollectionReaderDescriptorTaskB(inputFile));
		TestFeatureExtractionStatusCallBackListener listenerB = new TestFeatureExtractionStatusCallBackListener();
		uimaAsEngine.addStatusCallbackListener(listenerB);
		
		uimaAsEngine.initialize(appCtx);
		
		double startA = System.currentTimeMillis();
		while (collectionReaderB.hasNext()) {
			CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
					null, null);
			collectionReaderB.getNext(cas);
			uimaAsEngine.sendCAS(cas);
		}
		uimaAsEngine.collectionProcessingComplete();
		double endA = System.currentTimeMillis();
		double secondsA = (endA - startA)/1000;
		System.out.println(secondsA+" seconds");
		
		uimaAsEngine.stop();
		
	}
	
//	private String deployPipeline(UimaAsynchronousEngine uimaAsEngine) throws Exception {
//		String inputJCasMultiplierAEDescriptor = 
//				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors/test"
//						+ "/FeatureExtractionPipelineAAE_DeploymentDescriptor.xml";
//		
//		// create a Map to hold required parameters
//		Map<String,Object> appCtx = new HashMap<String,Object>();
//		appCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,System.getenv("UIMA_HOME") + "/bin/dd2spring.xsl");
//		appCtx.put(UimaAsynchronousEngine.SaxonClasspath,"file:" + System.getenv("UIMA_HOME") + "/saxon/saxon8.jar");
//		
//		String id = uimaAsEngine.deploy(new File(inputJCasMultiplierAEDescriptor).getAbsolutePath(), appCtx);
//		
//		return id;
//	}
//	
//	private void undeployPipeline(String id,UimaAsynchronousEngine uimaAsEngine) throws Exception {
//		uimaAsEngine.undeploy(id);
//	}
	
	public void multiplierTest() throws Exception {
		
//		BrokerService broker = new BrokerService();
//		broker.addConnector("tcp://localhost:61616");
//		broker.start();
//
//		UimaAsynchronousEngine uimaAsEngine = new BaseUIMAAsynchronousEngine_impl();
//		
////		generateAnalysisEngineDescritors(true);
//		String id = Starter.depoyFeatureExtraction(uimaAsEngine,"tcp://localhost:61616", "myQueueName","en", 10,false,true,false);
//		
//		String file = new File(DescriptorGenerator.class.getResource("/").toURI()).getAbsolutePath()+"/data/XML/SemEval/English/SemEval2016-Task3-CQA-QL-dev.xml";
////		String file = generateInputTestFile(true);
//		runTestTaskB(true, file);
//		file = new File(DescriptorGenerator.class.getResource("/").toURI()).getAbsolutePath()+"/data/XML/SemEval/English/SemEval2016-Task3-CQA-QL-dev-subtaskA.xml";
//		runTestTaskA(true, file);
//		Starter.undeployPipeline(id,uimaAsEngine);
//
//		Thread.sleep(100);
//		uimaAsEngine.stop();
//		broker.stop();
	}
	
	public static void main(String args[]) throws Exception {
		new FeatureExtractionTest().multiplierTest();
	}
}

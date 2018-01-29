/**
 * Copyright 2018 Salvatore Romeo
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 */
 
 
package qa.qcri.iyas.feature;

import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.activemq.broker.BrokerService;
import org.apache.uima.UIMAFramework;
import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.apache.uima.aae.client.UimaAsynchronousEngine;
import org.apache.uima.adapter.jms.client.BaseUIMAAsynchronousEngine_impl;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.collection.EntityProcessStatus;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCreationUtils;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderJDOMFactory;
import org.jdom2.input.sax.XMLReaderXSDFactory;
import org.junit.Test;

import qa.qcri.iyas.DescriptorGenerator;
import qa.qcri.iyas.data.preprocessing.StandardPreprocessor;
import qa.qcri.iyas.data.reader.DataReader;
import qa.qcri.iyas.data.reader.InputCollectionDataReader;
import qa.qcri.iyas.data.reader.PlainTextDataReader;
import qa.qcri.iyas.type.RelatedQuestion;
import qa.qcri.iyas.util.AggregatedJCasManager;

class MyStatusCallbackListenerTaskA extends UimaAsBaseCallbackListener {
	
	private Set<String> inputInstances;
	private SAXBuilder builder;
	private Map<String,Map<String,String>> maps;
	
	public MyStatusCallbackListenerTaskA(Map<String,Map<String,String>> m) throws JDOMException, URISyntaxException {
		inputInstances = new HashSet<>();
		XMLReaderJDOMFactory factory = new XMLReaderXSDFactory(new File(InputJCasMultiplier.class.getResource(DataReader.SCHEMA_INSTANCE_A_PATH).toURI()));
		builder = new SAXBuilder(factory);
		maps = m;
	}
	
	@Override
	public void entityProcessComplete(CAS cas, EntityProcessStatus aStatus) {
		try {
			if (!aStatus.getStatusMessage().equals("success")) {
				fail(aStatus.getStatusMessage());
			} else {
				AnnotationIndex<Annotation> annotations = cas.getJCas().getAnnotationIndex();
				if (annotations.size() > 1) {
					fail("Only an annotation is expected: "+annotations.toString());
				} else {
					Annotation annotation = annotations.iterator().next();
					if (annotation instanceof RelatedQuestion) {
						RelatedQuestion cqaAnnotation = (RelatedQuestion)annotation;
						String subject = cas.getJCas().getView(AggregatedJCasManager.RELATED_QUESTION_SUBJECT_VIEW).getDocumentText();
						String body = cas.getJCas().getView(AggregatedJCasManager.RELATED_QUESTION_BODY_VIEW).getDocumentText();
						
						Map<String,String> map = maps.get(cqaAnnotation.getID());
						
						if (map.get("subject_"+cqaAnnotation.getID()).equals(subject))
							map.remove("subject_"+cqaAnnotation.getID());
						
						if (map.get("body_"+cqaAnnotation.getID()).equals(body))
							map.remove("body_"+cqaAnnotation.getID());
						
						for (String cid : cqaAnnotation.getCandidateViewNames().toArray()) {
							String comment = cas.getJCas().getView(AggregatedJCasManager.COMMENT_VIEW+"-"+cid).getDocumentText();
							if (map.get("comment_"+cid).equals(comment))
								map.remove("comment_"+cid);
						}
						
						if (map.isEmpty())
							System.out.println(cqaAnnotation.getID()+" complete");
						else
							fail("The received aggregated JCas is not complete.");
					} else if (annotation instanceof DocumentAnnotation) {
						boolean b = inputInstances.remove(cas.getDocumentText());
						if (!b)
							fail("Received a not expected CAS.");
						
						Document instance = builder.build(new StringReader(cas.getDocumentText()));
						String id = instance.getRootElement().getChild(DataReader.INSTANCE_A_TAG)
								.getChild(DataReader.RELATED_QUESTION_TAG).getAttributeValue(DataReader.ID_ATTRIBUTE);
						
						System.out.println("Removed "+id);
					} else {
						fail("An unexpected annotation has been received: "+annotation);
					}
				}
			}
		} catch (CASException | JDOMException | IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public Set<String> getInputInstances() {
		return inputInstances;
	}
}

public class JCasMultiplierAndAggregatorTest {
	
	private static Map<String,Map<String,String>> mapsA = new HashMap<String,Map<String,String>>();
	
	private String generateInputTestFile() throws IOException {
		StandardPreprocessor sp = new StandardPreprocessor();
		File file = new File("test_input.txt");
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		for (int k=1;k<=1;k++) {
			String qid = "Q"+k;
			String org_subject = "This is the subject of original question "+qid;
			String org_body = "This is the body of original question "+qid;
			for (int i=1;i<=2;i++) {
				out.write(qid+"	"+org_subject+"	"+org_body);
				String rid = qid+"_R"+i;
				String rel_subject = "This is the subject of related question "+rid;
				String rel_body = "This is the body of related question "+rid;
				out.write("	"+rid+"	"+rel_subject+"	"+rel_body);
				
				Map<String,String> mapA = new HashMap<String,String>();
				mapA.put("subject_"+rid, sp.preprocess(rel_subject,"en"));
				mapA.put("body_"+rid, sp.preprocess(rel_body,"en"));
				
				for (int j=1;j<=10;j++) {
					String cid = rid+"_C"+j;
					String comment = "This comment "+cid;
					out.write("	"+cid+"	"+comment);
					mapA.put("comment_"+cid, sp.preprocess(comment,"en"));
				}
				out.newLine();
				
				mapsA.put(rid, mapA);
			}
		}
		out.close();
		
		return file.getAbsolutePath();
	}
	
	private CollectionReaderDescription getCollectionReaderDescriptor() throws ResourceInitializationException, IOException {
		String file = generateInputTestFile();
		CollectionReaderDescription collectionReaderDescr = CollectionReaderFactory.createReaderDescription(
				InputCollectionDataReader.class);
		ExternalResourceDescription reader = ExternalResourceFactory.createExternalResourceDescription(PlainTextDataReader.class,
				PlainTextDataReader.FILE_PARAM, file,
				PlainTextDataReader.TASK_PARAM, PlainTextDataReader.INSTANCE_A_TASK);
		ExternalResourceFactory.bindExternalResource(collectionReaderDescr, 
				InputCollectionDataReader.INPUT_READER_PARAM, reader);
		
		return collectionReaderDescr;
	}

	@Test
	public void multiplierTest() throws Exception {
		DescriptorGenerator.generateMyAnnotatorAAEDescriptor(
				new File(JCasMultiplierAndAggregatorTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		DescriptorGenerator.generateInputJCasMultiplierAAEDescriptor(
				new File(JCasMultiplierAndAggregatorTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		DescriptorGenerator.generateProcessedJCasAggregatorAAEDescriptor(
				new File(JCasMultiplierAndAggregatorTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		DescriptorGenerator.generatePipelineAAEDescriptor(
				new File(JCasMultiplierAndAggregatorTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		DescriptorGenerator.generatePipelineAAEDeploymentDescriptor(
				new File(JCasMultiplierAndAggregatorTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		
		String inputJCasMultiplierAEDescriptor = 
				new File(JCasMultiplierAndAggregatorTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors/test"
						+ "/PipelineAAE_DeploymentDescriptor.xml";
		
		BrokerService broker = new BrokerService();
		broker.addConnector("tcp://localhost:61616");
		broker.start();
		
		UimaAsynchronousEngine uimaAsEngine1 = new BaseUIMAAsynchronousEngine_impl();
		// create a Map to hold required parameters
		Map<String,Object> appCtx1 = new HashMap<String,Object>();
		appCtx1.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,System.getenv("UIMA_HOME") + "/bin/dd2spring.xsl");
		appCtx1.put(UimaAsynchronousEngine.SaxonClasspath,"file:" + System.getenv("UIMA_HOME") + "/saxon/saxon8.jar");
		
		List<String> IDs = new LinkedList<>();
		IDs.add(uimaAsEngine1.deploy(new File(inputJCasMultiplierAEDescriptor).getAbsolutePath(), appCtx1));
		
		
		
		UimaAsynchronousEngine uimaAsEngine2 = new BaseUIMAAsynchronousEngine_impl();

		Map<String,Object> appCtx2 = new HashMap<String,Object>();
		appCtx2.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,System.getenv("UIMA_HOME") + "/bin/dd2spring.xsl");
		appCtx2.put(UimaAsynchronousEngine.SaxonClasspath,"file:" + System.getenv("UIMA_HOME") + "/saxon/saxon8.jar");
		appCtx2.put(UimaAsynchronousEngine.ServerUri, "tcp://localhost:61616");
		appCtx2.put(UimaAsynchronousEngine.ENDPOINT, "myQueueName");
		appCtx2.put(UimaAsynchronousEngine.CasPoolSize, 100);
		
		CollectionReader collectionReader = UIMAFramework.produceCollectionReader(getCollectionReaderDescriptor());
//		uimaAsEngine2.setCollectionReader(collectionReader);
		
		MyStatusCallbackListenerTaskA listener = new MyStatusCallbackListenerTaskA(mapsA);
		uimaAsEngine2.addStatusCallbackListener(listener);
		
		uimaAsEngine2.initialize(appCtx2);
		
		double start = System.currentTimeMillis();
		while (collectionReader.hasNext()) {
			CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
					null, null);
			collectionReader.getNext(cas);
			listener.getInputInstances().add(cas.getDocumentText());
			uimaAsEngine2.sendCAS(cas);
		}
		uimaAsEngine2.collectionProcessingComplete();
		double end = System.currentTimeMillis();
		double seconds = (end - start)/1000;
		System.out.println(seconds+" seconds");
		
		uimaAsEngine2.stop();
		
		for (String id : IDs)
			uimaAsEngine1.undeploy(id);
		uimaAsEngine1.stop();
		
		broker.stop();
	}
	
	public static void main(String args[]) throws Exception {
		new JCasMultiplierAndAggregatorTest().multiplierTest();
	}
}

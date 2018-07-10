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

package qa.qcri.iyas;

//import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.activemq.broker.BrokerService;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringEscapeUtils;
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
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;

import qa.qcri.iyas.data.reader.DataReader;
import qa.qcri.iyas.data.reader.InputCollectionDataReader;
import qa.qcri.iyas.data.reader.PreprocessedInputCollectionDataReader;
import qa.qcri.iyas.data.reader.VolatileDataReader;
import qa.qcri.iyas.data.reader.XmlSemeval2016CqaAr;
import qa.qcri.iyas.data.reader.XmlSemeval2016CqaEn;
import qa.qcri.iyas.type.AdditionalInfo;
import qa.qcri.iyas.type.Model;



class FeatureExtractionStatusCallBackListener extends UimaAsBaseCallbackListener  {
	
	public String representations[] = new String[0];
	private Integer count = 0;
	
	@Override
	public void entityProcessComplete(CAS cas, EntityProcessStatus aStatus) {
		if (!aStatus.getStatusMessage().equals("success")) {
			System.out.println(StringEscapeUtils.unescapeXml(cas.getDocumentText()));
			System.exit(0);
//			throw new IllegalStateException(aStatus.getStatusMessage());
		} else {
			try {
				if (JCasUtil.exists(cas.getJCas(), AdditionalInfo.class)) {
					AdditionalInfo info = JCasUtil.select(cas.getJCas(), AdditionalInfo.class).iterator().next();
					synchronized (representations) {
						if (representations.length == 0)
							representations = new String[info.getTotalNumberOfExamples()];
					}
					representations[info.getIndex()] = cas.getDocumentText();

					synchronized (count) {
						System.out.println("Processed "+(count++)+" over "+info.getTotalNumberOfExamples()+" examples");
					}
					
//					System.out.println(info.getIndex() + " " + info.getTotalNumberOfExamples()
//					+ " " + info.getInstanceID() + " " + cas.getDocumentText());
				}
			} catch (CASException e) {
				e.printStackTrace();
			}
//			System.out.println(cas.getDocumentText());
		}
	}
}

class ClassificationStatusCallBackListener extends UimaAsBaseCallbackListener  {
	
	public Object predictions[][] = new Object[0][0];
	private Integer count = 0;
	
	@Override
	public void entityProcessComplete(CAS cas, EntityProcessStatus aStatus) {
		if (!aStatus.getStatusMessage().equals("success")) {
			throw new IllegalStateException(aStatus.getStatusMessage());
		} else {
			try {
				if (JCasUtil.exists(cas.getJCas(), AdditionalInfo.class)) {
					AdditionalInfo info = JCasUtil.select(cas.getJCas(), AdditionalInfo.class).iterator().next();
					synchronized (predictions) {
						if (predictions.length == 0)
							predictions = new Object[info.getTotalNumberOfExamples()][2];
					}
					predictions[info.getIndex()][0] = info.getInstanceID();
					predictions[info.getIndex()][1] = Float.parseFloat(info.getPrediction());

					synchronized (count) {
						System.out.println("Classified "+(count++)+" over "+info.getTotalNumberOfExamples()+" examples");
					}
				}
			} catch (CASException e) {
				e.printStackTrace();
			}
		}
	}
}

class LearningStatusCallBackListener extends UimaAsBaseCallbackListener  {
	
	public String modelFile;
	
	@Override
	public void entityProcessComplete(CAS cas, EntityProcessStatus aStatus) {
		if (!aStatus.getStatusMessage().equals("success")) {
			throw new IllegalStateException(aStatus.getStatusMessage());
		} else {
			try {
				if (JCasUtil.exists(cas.getJCas(), Model.class)) {
					Model model = JCasUtil.select(cas.getJCas(), Model.class).iterator().next();
					modelFile = model.getFile();
				}
			} catch (CASException e) {
				e.printStackTrace();
			}
		}
	}
}

class TestStatusCallBackListener extends UimaAsBaseCallbackListener  {
		
	@Override
	public void entityProcessComplete(CAS cas, EntityProcessStatus aStatus) {
		if (!aStatus.getStatusMessage().equals("success")) {
			throw new IllegalStateException(aStatus.getStatusMessage());
		} else {
			System.out.println(cas.getDocumentText());
		}
	}
}

public class Starter {
	
	private static final String QUEUE_NAME_OPT = "qn";
	private static final String TEXT_ANNOTATION_QUEUE_NAME_OPT = "tq";
	private static final String SCALEOUT_OPT = "sc";
	private static final String INPUT_FILE_OPT = "if";
	private static final String OUTPUT_FILE_OPT = "of";
	private static final String IP_ADDRESS_OPT = "ip";
	private static final String TEXT_ANNOTATION_IP_ADDRESS_OPT = "ti";
	private static final String TASK_OPT = "rt";
	
	private static final String QUEUE_NAME_LONG_OPT = "queue-name";
	private static final String TEXT_ANNOTATION_QUEUE_NAME_LONG_OPT = "text-annotation-queue";
	private static final String SCALEOUT_LONG_NAME_OPT = "scaleout";
	private static final String INPUT_FILE_LONG_OPT = "input-file";
	private static final String OUTPUT_FILE_LONG_OPT = "output-file";
	private static final String IP_ADDRESS_LONG_OPT = "ip-address";
	private static final String TEXT_ANNOTATION_IP_ADDRESS_LONG_OPT = "text-annotation-ip";
	private static final String TASK_LONG_OPT = "reranking-task";
	
	
	private static final String DEPLOY_TEXT_ANNOTATION_OPT = "dta";

	private static final String DEPLOY_TEXT_ANNOTATION_LONG_OPT = "deploy-text-annotation";

	
	private static final String DEPLOY_FEATURE_EXTRACTION_OPT = "dfe";
	private static final String USE_SIMS_OPT = "s";
	private static final String USE_RANK_OPT = "r";
	private static final String USE_TREES_OPT = "t";

	private static final String DEPLOY_FEATURE_EXTRACTION_LONG_OPT = "deploy-feature-extraction";
	private static final String USE_SIMS_LONG_OPT = "sims";
	private static final String USE_RANK_LONG_OPT = "rank";
	private static final String USE_TREES_LONG_OPT = "t";
	
	
	
	private static final String UNDEPLOY_OPT = "up";
	private static final String ID_OPT = "si";
	
	private static final String UNDEPLOY_LONG_OPT = "undeploy-pipeline";
	private static final String ID_LONG_OPT = "service-id";
	
	
	
	private static final String ENGLISH_FEATURE_EXTRACTION_OPT = "ef";
	private static final String ENGLISH_FEATURE_EXTRACTION_LONG_OPT = "english-feature-extraction";

	private static final String ARABIC_FEATURE_EXTRACTION_OPT = "af";
	private static final String ARABIC_FEATURE_EXTRACTION_LONG_OPT = "arabic-feature-extraction";
	
	
	private static final String DEPLOY_CLASSIFICATION_OPT = "dc";
	private static final String FE_QUEUE_NAME_OPT = "fqn";
//	private static final String FE_IP_ADRRESS_OPT = "fip";
	private static final String MODEL_FILE_OPT = "mf";

	private static final String DEPLOY_CLASSIFICATION_LONG_OPT = "deploy-classication";
	private static final String FE_QUEUE_NAME_LONG_OPT = "feature-extraction-queue-names";
//	private static final String FE_IP_ADRRESS_LONG_OPT = "feature-extraction-ip-address";
	private static final String MODEL_FILE_LONG_OPT = "model-file";

	
	
	private static final String CLASSIFICATION_OPT = "c";

	private static final String CLASSIFICATION_LONG_OPT = "classify";
	
	
	
	private static final String START_BROKER_OPT = "sb";
	
	private static final String START_BROKER_LONG_OPT = "start-broker";

	
	
	private static final String DEPLOY_LEARNING_OPT = "dl";

	private static final String DEPLOY_LEARNING_LONG_OPT = "deploy-learning";
	
	
	
	private static final String LEARNING_OPT = "l";

	private static final String LEARNING_LONG_OPT = "learning";
	
	
	
	private static final String HELP_OPT = "h";

	private static final String HELP_LONG_OPT = "help";

	
	
	private static final String LANGUAGE_OPT = "L";
	
	private static final String LANGUAGE_LONG_OPT = "lang";
	
	
	public static void startBroker(String ip) throws Exception {
		BrokerService broker = new BrokerService();
		broker.addConnector("http://"+ip+":61616");
		broker.start();
	}
	
	public static String depoyTextAnnotation(UimaAsynchronousEngine uimaAsEngine,String brokerURL,String queueName,
			String lang, int scaleout) throws Exception {
		Map<String,Object> appCtx = new HashMap<String,Object>();
		appCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,System.getenv("UIMA_HOME") + "/bin/dd2spring.xsl");
		appCtx.put(UimaAsynchronousEngine.SaxonClasspath,"file:" + System.getenv("UIMA_HOME") + "/saxon/saxon8.jar");
		appCtx.put(UimaAsynchronousEngine.Timeout, 1000000000);

		String feDescr = DescriptorGenerator.generateStandardTextAnnotatorPipelineDeploymentDescriptor(brokerURL, queueName, lang, scaleout);
		String id = uimaAsEngine.deploy(new File(feDescr).getAbsolutePath(), appCtx);
		
		return id;
	}
	
	public static String depoyFeatureExtraction(UimaAsynchronousEngine uimaAsEngine,String brokerURL,String queueName,
			String standardTextAnnotatorURL, String standardTextAnnotatorQueueName, String lang, int scaleout,
			boolean sims,boolean rank,boolean trees) throws Exception {
		Map<String,Object> appCtx = new HashMap<String,Object>();
		appCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,System.getenv("UIMA_HOME") + "/bin/dd2spring.xsl");
		appCtx.put(UimaAsynchronousEngine.SaxonClasspath,"file:" + System.getenv("UIMA_HOME") + "/saxon/saxon8.jar");
		appCtx.put(UimaAsynchronousEngine.Timeout, 1000000000);
		
		String feDescr = DescriptorGenerator.generateFeatureExtractionPipelineDeploymentDescriptor(brokerURL, queueName, 
				standardTextAnnotatorURL, standardTextAnnotatorQueueName, lang, scaleout, sims,rank,trees);
		String id = uimaAsEngine.deploy(new File(feDescr).getAbsolutePath(), appCtx);
		
		return id;
	}
	
	public static String depoyClassification(UimaAsynchronousEngine uimaAsEngine,String brokerURL,String queueName, String lang,int scaleout,String modelFile,
			String featureExtractionURL,String featureExtractionQueueName) throws Exception {
		
		Map<String,Object> appCtx = new HashMap<String,Object>();
		appCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,System.getenv("UIMA_HOME") + "/bin/dd2spring.xsl");
		appCtx.put(UimaAsynchronousEngine.SaxonClasspath,"file:" + System.getenv("UIMA_HOME") + "/saxon/saxon8.jar");
		appCtx.put(UimaAsynchronousEngine.Timeout, 1000000000);
		
		String descr = DescriptorGenerator.generateClassificationPipelineDeploymentDescriptor(
				brokerURL,queueName,lang,modelFile,featureExtractionURL, featureExtractionQueueName);
		String id = uimaAsEngine.deploy(new File(descr).getAbsolutePath(), appCtx);
		
		return id;
	}
	
	public static String depoyLearning(UimaAsynchronousEngine uimaAsEngine,String brokerURL,String queueName, String lang,int scaleout,
			String featureExtractionURL,String featureExtractionQueueName,boolean sims,boolean rank,boolean trees) throws Exception {
		
		Map<String,Object> appCtx = new HashMap<String,Object>();
		appCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,System.getenv("UIMA_HOME") + "/bin/dd2spring.xsl");
		appCtx.put(UimaAsynchronousEngine.SaxonClasspath,"file:" + System.getenv("UIMA_HOME") + "/saxon/saxon8.jar");
		appCtx.put(UimaAsynchronousEngine.Timeout, 1000000000);
		
		String descr = null;
		if (featureExtractionQueueName != null)
			descr = DescriptorGenerator.generateLearningPipelineDeploymentDescriptor(
					brokerURL,queueName,lang,featureExtractionURL, featureExtractionQueueName, sims, rank, trees);
		else
			descr = DescriptorGenerator.generateLearningPipelineWithoutPreprocessingDeploymentDescriptor(
					brokerURL,queueName, sims, rank, trees);
		
		String id = uimaAsEngine.deploy(new File(descr).getAbsolutePath(), appCtx);
		
		return id;
	}
	
	public static void undeployPipeline(String id,UimaAsynchronousEngine uimaAsEngine) throws Exception {
		uimaAsEngine.undeploy(id);
	}
	
	
	private static CollectionReaderDescription getArabicSemEvalCollectionReaderDescriptor(String file) throws ResourceInitializationException, IOException {
		CollectionReaderDescription collectionReaderDescr = CollectionReaderFactory.createReaderDescription(
				InputCollectionDataReader.class);
		ExternalResourceDescription reader = ExternalResourceFactory.createExternalResourceDescription(XmlSemeval2016CqaAr.class,
				XmlSemeval2016CqaAr.FILE_PARAM, file,
				XmlSemeval2016CqaAr.TASK_PARAM, XmlSemeval2016CqaAr.INSTANCE_B_TASK);
		ExternalResourceFactory.bindExternalResource(collectionReaderDescr, 
				InputCollectionDataReader.INPUT_READER_PARAM, reader);
		
		return collectionReaderDescr;
	}
	
	private static CollectionReaderDescription getEnglishSemEvalCollectionReaderDescriptor(String file,String task) throws ResourceInitializationException, IOException {
		String t = null;
		if (task.equals("cr"))
			t = XmlSemeval2016CqaEn.INSTANCE_A_TASK;
		else if (task.equals("qr"))
			t = XmlSemeval2016CqaEn.INSTANCE_B_TASK;
		else
			throw new IllegalArgumentException();
		
		CollectionReaderDescription collectionReaderDescr = CollectionReaderFactory.createReaderDescription(
				InputCollectionDataReader.class);
		ExternalResourceDescription reader = ExternalResourceFactory.createExternalResourceDescription(XmlSemeval2016CqaEn.class,
				XmlSemeval2016CqaEn.FILE_PARAM, file,
				XmlSemeval2016CqaEn.TASK_PARAM, t);
		ExternalResourceFactory.bindExternalResource(collectionReaderDescr, 
				InputCollectionDataReader.INPUT_READER_PARAM, reader);
		
		return collectionReaderDescr;
	}
	
	private static CollectionReaderDescription getPreprocessedCollectionReaderDescriptor(String file) throws ResourceInitializationException, IOException {
		CollectionReaderDescription collectionReaderDescr = CollectionReaderFactory.createReaderDescription(
				PreprocessedInputCollectionDataReader.class,
				PreprocessedInputCollectionDataReader.INPUT_FILE_PARAM,file);	
		
		return collectionReaderDescr;
	}
	
	public static void extractFeaturesEnglish(String inputFile,String outputFile,String brokerURL,
			String queueName, String task) throws Exception {
		
		UimaAsynchronousEngine uimaAsEngine = new BaseUIMAAsynchronousEngine_impl();

		CollectionReader collectionReader = UIMAFramework.produceCollectionReader(getEnglishSemEvalCollectionReaderDescriptor(inputFile,task));
		FeatureExtractionStatusCallBackListener listener = new FeatureExtractionStatusCallBackListener();
		uimaAsEngine.addStatusCallbackListener(listener);
		uimaAsEngine.setCollectionReader(collectionReader);
		
		Map<String,Object> appCtx = new HashMap<String,Object>();
		appCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,System.getenv("UIMA_HOME") + "/bin/dd2spring.xsl");
		appCtx.put(UimaAsynchronousEngine.SaxonClasspath,"file:" + System.getenv("UIMA_HOME") + "/saxon/saxon8.jar");
		appCtx.put(UimaAsynchronousEngine.ServerUri, brokerURL);
		appCtx.put(UimaAsynchronousEngine.ENDPOINT, queueName);
		appCtx.put(UimaAsynchronousEngine.CasPoolSize, 100);
		appCtx.put(UimaAsynchronousEngine.Timeout, 1000000000);
		
		uimaAsEngine.initialize(appCtx);

		double start = System.currentTimeMillis();
		uimaAsEngine.process();
		double end = System.currentTimeMillis();
		double seconds = (end - start)/1000;
		System.out.println("Feature extraction completed in "+seconds+" seconds");
		
		uimaAsEngine.stop();
		
		File file = new File(outputFile);
		System.out.println("Writing extracted features on "+file.getAbsolutePath());
		
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		for (String ex : listener.representations) {
			out.write(ex);
			out.newLine();
		}
		out.close();
	}
	
	public static void extractFeaturesArabic(String inputFile,String outputFile,String brokerURL,
			String queueName) throws Exception {
		
		UimaAsynchronousEngine uimaAsEngine = new BaseUIMAAsynchronousEngine_impl();

		CollectionReader collectionReader = UIMAFramework.produceCollectionReader(getArabicSemEvalCollectionReaderDescriptor(inputFile));
		FeatureExtractionStatusCallBackListener listener = new FeatureExtractionStatusCallBackListener();
		uimaAsEngine.addStatusCallbackListener(listener);
		uimaAsEngine.setCollectionReader(collectionReader);
		
		Map<String,Object> appCtx = new HashMap<String,Object>();
		appCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,System.getenv("UIMA_HOME") + "/bin/dd2spring.xsl");
		appCtx.put(UimaAsynchronousEngine.SaxonClasspath,"file:" + System.getenv("UIMA_HOME") + "/saxon/saxon8.jar");
		appCtx.put(UimaAsynchronousEngine.ServerUri, brokerURL);
		appCtx.put(UimaAsynchronousEngine.ENDPOINT, queueName);
		appCtx.put(UimaAsynchronousEngine.CasPoolSize, 100);
		appCtx.put(UimaAsynchronousEngine.Timeout, 1000000000);
		
		uimaAsEngine.initialize(appCtx);

		double start = System.currentTimeMillis();
		uimaAsEngine.process();
		double end = System.currentTimeMillis();
		double seconds = (end - start)/1000;
		System.out.println("Feature extraction completed in "+seconds+" seconds");
		
		uimaAsEngine.stop();
		
		File file = new File(outputFile);
		System.out.println("Writing extracted features on "+file.getAbsolutePath());
		
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		for (String ex : listener.representations) {
			out.write(ex);
			out.newLine();
		}
		out.close();
	}
	
	public static void classify(String inputFile,String outputFile,String brokerURL,
			String queueName, String task) throws Exception {
		
		UimaAsynchronousEngine uimaAsEngine = new BaseUIMAAsynchronousEngine_impl();

		CollectionReader collectionReader = UIMAFramework.produceCollectionReader(getEnglishSemEvalCollectionReaderDescriptor(inputFile,task));
		ClassificationStatusCallBackListener listener = new ClassificationStatusCallBackListener();
		uimaAsEngine.addStatusCallbackListener(listener);
		uimaAsEngine.setCollectionReader(collectionReader);
		
		Map<String,Object> appCtx = new HashMap<String,Object>();
		appCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,System.getenv("UIMA_HOME") + "/bin/dd2spring.xsl");
		appCtx.put(UimaAsynchronousEngine.SaxonClasspath,"file:" + System.getenv("UIMA_HOME") + "/saxon/saxon8.jar");
		appCtx.put(UimaAsynchronousEngine.ServerUri, brokerURL);
		appCtx.put(UimaAsynchronousEngine.ENDPOINT, queueName);
		appCtx.put(UimaAsynchronousEngine.CasPoolSize, 100);
		appCtx.put(UimaAsynchronousEngine.Timeout, 1000000000);
		
		uimaAsEngine.initialize(appCtx);

		double start = System.currentTimeMillis();
		uimaAsEngine.process();
		double end = System.currentTimeMillis();
		double seconds = (end - start)/1000;
		System.out.println("Classification completed in "+seconds+" seconds");
		
		uimaAsEngine.stop();
		
		File file = new File(outputFile);
		System.out.println("Writing predictions on "+file.getAbsolutePath());
		
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		for (Object ex[] : listener.predictions) {
			out.write(ex[1].toString());
			out.newLine();
		}
		out.close();
	}
	
	public static void learn(String inputFile,String outputFile,String brokerURL,
			String queueName, String task) throws Exception {
		
		UimaAsynchronousEngine uimaAsEngine = new BaseUIMAAsynchronousEngine_impl();

		CollectionReader collectionReader = null;
		
		if (task != null)
			collectionReader = UIMAFramework.produceCollectionReader(getEnglishSemEvalCollectionReaderDescriptor(inputFile,task));
		else
			collectionReader = UIMAFramework.produceCollectionReader(getPreprocessedCollectionReaderDescriptor(inputFile));
		
		LearningStatusCallBackListener listener = new LearningStatusCallBackListener();
		uimaAsEngine.addStatusCallbackListener(listener);
		uimaAsEngine.setCollectionReader(collectionReader);
		
		Map<String,Object> appCtx = new HashMap<String,Object>();
		appCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,System.getenv("UIMA_HOME") + "/bin/dd2spring.xsl");
		appCtx.put(UimaAsynchronousEngine.SaxonClasspath,"file:" + System.getenv("UIMA_HOME") + "/saxon/saxon8.jar");
		appCtx.put(UimaAsynchronousEngine.ServerUri, brokerURL);
		appCtx.put(UimaAsynchronousEngine.ENDPOINT, queueName);
		appCtx.put(UimaAsynchronousEngine.CasPoolSize, 100);
		appCtx.put(UimaAsynchronousEngine.Timeout, 1000000000);
		
		uimaAsEngine.initialize(appCtx);

		double start = System.currentTimeMillis();
		uimaAsEngine.process();
		double end = System.currentTimeMillis();
		double seconds = (end - start)/1000;
		System.out.println("Learning completed in "+seconds+" seconds");
		
		uimaAsEngine.stop();
		
		File file = new File(outputFile);
		System.out.println("Writing model file path on "+file.getAbsolutePath());
		
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		out.write(listener.modelFile);
		out.newLine();
		out.close();
	}
	
	
	
	public static void main(String args[]) throws Exception {		
		
//		CollectionReaderDescription collectionReaderDescr = CollectionReaderFactory.createReaderDescription(
//				InputCollectionDataReader.class);
//		ExternalResourceDescription reader = ExternalResourceFactory.createExternalResourceDescription(VolatileDataReader.class,
//				VolatileDataReader.TASK_PARAM, VolatileDataReader.INSTANCE_B_TASK);
////		ExternalResourceDescription reader = ExternalResourceFactory.createExternalResourceDescription(XmlSemeval2016CqaEn.class,
////				XmlSemeval2016CqaEn.FILE_PARAM, "/home/sromeo/workspaces/UIMA/workspace/S3QACoreFramework/src/test/resources/data/XML/SemEval/English/SemEval2016-Task3-CQA-QL-dev.xml",
////				XmlSemeval2016CqaEn.TASK_PARAM, XmlSemeval2016CqaEn.INSTANCE_B_TASK);
//		ExternalResourceFactory.bindExternalResource(collectionReaderDescr, 
//				InputCollectionDataReader.INPUT_READER_PARAM, reader);
//		
//		
//		UimaAsynchronousEngine uimaAsEngine1 = new BaseUIMAAsynchronousEngine_impl();
//		
//		CollectionReader collectionReader = UIMAFramework.produceCollectionReader(collectionReaderDescr);
//		FeatureExtractionStatusCallBackListener listener = new FeatureExtractionStatusCallBackListener();
//		uimaAsEngine1.addStatusCallbackListener(listener);
//		uimaAsEngine1.setCollectionReader(collectionReader);
//
//		Map<String,Object> appCtx = new HashMap<String,Object>();
//		appCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,System.getenv("UIMA_HOME") + "/bin/dd2spring.xsl");
//		appCtx.put(UimaAsynchronousEngine.SaxonClasspath,"file:" + System.getenv("UIMA_HOME") + "/saxon/saxon8.jar");
//		appCtx.put(UimaAsynchronousEngine.ServerUri, "http://127.0.0.1:61616");
//		appCtx.put(UimaAsynchronousEngine.ENDPOINT, "arabicFeatureExtractionQueue");
//		appCtx.put(UimaAsynchronousEngine.CasPoolSize, 100);
//		
//		uimaAsEngine1.initialize(appCtx);
//		
//		double start = System.currentTimeMillis();
//		uimaAsEngine1.process();
//		double end = System.currentTimeMillis();
//		double seconds = (end - start)/1000;
//		System.out.println("Feature extraction completed in "+seconds+" seconds");
//		
//		File file = new File("representations.klp");
//		System.out.println("Writing extracted features on "+file.getAbsolutePath());
//		
//		BufferedWriter out = new BufferedWriter(new FileWriter(file));
//		for (String ex : listener.representations) {
//			if (ex == null)
//				continue;
//			out.write(ex);
//			out.newLine();
//		}
//		out.close();
//		
//		System.exit(0);
		
		CommandLineParser parser = new DefaultParser();
		
		Option langOpt = new Option(LANGUAGE_OPT,LANGUAGE_LONG_OPT,true,"Language of the text to be processed.");
		langOpt.setArgName("language");
		langOpt.setRequired(true);
		
		//Broker starter options
		Option startBrokerOpt = new Option(START_BROKER_OPT, START_BROKER_LONG_OPT, false, "Start broker");
		
		Option url6Opt = new Option(IP_ADDRESS_OPT,IP_ADDRESS_LONG_OPT,true,"IP address of the broker will listen");
		url6Opt.setArgName("IP address");
		url6Opt.setRequired(true);
		
		Options startBrokerOpts = new Options();
		startBrokerOpts.addOption(url6Opt);
		
		
		//Text Annotation Deployment options
		//-dta -L ar -ip 127.0.0.1 -qn arabicTextAnnotationQueue -sc 2
		//-dta -L en -ip 127.0.0.1 -qn englishTextAnnotationQueue -sc 10
		Option deployTAOpt = new Option(DEPLOY_TEXT_ANNOTATION_OPT, DEPLOY_TEXT_ANNOTATION_LONG_OPT, false, "Deploy text annotation pipeline");
		
		Option queueName7Opt = new Option(QUEUE_NAME_OPT,QUEUE_NAME_LONG_OPT,true,"Name of the queue where the deploy pipeline will receive the requests");
		queueName7Opt.setArgName("queue name");
		queueName7Opt.setRequired(true);
		
		Option scaleout1Opt = new Option(SCALEOUT_OPT,SCALEOUT_LONG_NAME_OPT,true,"Number of instances to be instantiated for the text annotation pipeline");
		scaleout1Opt.setArgName("scaleout");
		scaleout1Opt.setRequired(true);

		Option url8Opt = new Option(IP_ADDRESS_OPT,IP_ADDRESS_LONG_OPT,true,"IP address of the broker where the text annotation pipeline has to connected");
		url8Opt.setArgName("IP address");
		url8Opt.setRequired(true);

		Options deployTAOpts = new Options();
		deployTAOpts.addOption(queueName7Opt);
		deployTAOpts.addOption(scaleout1Opt);
		deployTAOpts.addOption(url8Opt);
		deployTAOpts.addOption(langOpt);
		
		
		//Feature Extraction Deployment options
		//-dfe -L ar -ip 127.0.0.1 -qn arabicFeatureExtractionQueue -tq arabicTextAnnotationQueue -ti 127.0.0.1 -s -t
		//-dfe -L en -ip 127.0.0.1 -sc 5 -qn englishFeatureExtractionQueue -tq englishTextAnnotationQueue -ti 127.0.0.1 -s -t -r
		Option deployFEOpt = new Option(DEPLOY_FEATURE_EXTRACTION_OPT, DEPLOY_FEATURE_EXTRACTION_LONG_OPT, false, "Deploy feature extraction pipeline");
		
		Option queueName1Opt = new Option(QUEUE_NAME_OPT,QUEUE_NAME_LONG_OPT,true,"Name of the queue where the deploy pipeline will receive the requests");
		queueName1Opt.setArgName("queue name");
		queueName1Opt.setRequired(true);
		
		Option textAnnQueueNameOpt = new Option(TEXT_ANNOTATION_QUEUE_NAME_OPT,TEXT_ANNOTATION_QUEUE_NAME_LONG_OPT,true,"Name of the queue where the text annotation pipeline is listening");
		textAnnQueueNameOpt.setArgName("queue name");
		textAnnQueueNameOpt.setRequired(true);
		
		Option scaleoutOpt = new Option(SCALEOUT_OPT,SCALEOUT_LONG_NAME_OPT,true,"Number of instances to be instantiated for the feature computation sub-pipeline");
		scaleoutOpt.setArgName("scaleout");
		scaleoutOpt.setRequired(true);

		Option url4Opt = new Option(IP_ADDRESS_OPT,IP_ADDRESS_LONG_OPT,true,"IP address of the broker where the feature extraction pipeline has to connected");
		url4Opt.setArgName("IP address");
		url4Opt.setRequired(true);
		
		Option url7Opt = new Option(TEXT_ANNOTATION_IP_ADDRESS_OPT,TEXT_ANNOTATION_IP_ADDRESS_LONG_OPT,true,"IP address of the broker where the text annotation pipeline is registered");
		url7Opt.setArgName("IP address");
		url7Opt.setRequired(true);
		
		Option simsOpt = new Option(USE_SIMS_OPT, USE_SIMS_LONG_OPT, false, "Extract similarity features");
		Option rankOpt = new Option(USE_RANK_OPT, USE_RANK_LONG_OPT, false, "Extract rank feature");
		Option treesOpt = new Option(USE_TREES_OPT, USE_TREES_LONG_OPT, false, "Extract trees");

		Options deployFEOpts = new Options();
		deployFEOpts.addOption(queueName1Opt);
		deployFEOpts.addOption(textAnnQueueNameOpt);
		deployFEOpts.addOption(url4Opt);
		deployFEOpts.addOption(url7Opt);
		deployFEOpts.addOption(scaleoutOpt);
		deployFEOpts.addOption(simsOpt);
		deployFEOpts.addOption(rankOpt);
		deployFEOpts.addOption(treesOpt);
		deployFEOpts.addOption(langOpt);
		
		
		//Classification Deployment options
		//-dc -fqn featureExtractionQueue -qn classificationQueue -ip 127.0.0.1 -mf /home/sromeo/workspaces/UIMA/workspace/S3QACoreFramework/1521745849273.mdl
		Option deployClassificationOpt = new Option(DEPLOY_CLASSIFICATION_OPT, DEPLOY_CLASSIFICATION_LONG_OPT, false, "Deploy classification pipeline");

		Option queueName3Opt = new Option(QUEUE_NAME_OPT,QUEUE_NAME_LONG_OPT,true,"Name of the queue where the classification pipeline will receive the requests");
		queueName3Opt.setArgName("queue name");
		queueName3Opt.setRequired(true);

		Option queueNameFEOpt = new Option(FE_QUEUE_NAME_OPT,FE_QUEUE_NAME_LONG_OPT,true,"Name of the queue where the feature extraction pipeline is listening");
		queueNameFEOpt.setArgName("queue name");
		queueNameFEOpt.setRequired(true);
		
		Option urlFEOpt = new Option(IP_ADDRESS_OPT,IP_ADDRESS_LONG_OPT,true,"IP address of the broker where the classification pipeline has to connected");
		urlFEOpt.setArgName("IP address");
		urlFEOpt.setRequired(true);
		
		Option mfOpt = new Option(MODEL_FILE_OPT,MODEL_FILE_LONG_OPT,true,"Path of  the trained model file to be loaded");
		mfOpt.setArgName("file path");
		mfOpt.setRequired(true);
		
		Options classificationDeploymentOpts = new Options();
		classificationDeploymentOpts.addOption(queueName3Opt);
		classificationDeploymentOpts.addOption(urlFEOpt);
		classificationDeploymentOpts.addOption(queueNameFEOpt);
		classificationDeploymentOpts.addOption(mfOpt);
		classificationDeploymentOpts.addOption(langOpt);

		
		//Learning Deployment options
		//-dl -fqn featureExtractionQueue -qn learningQueue -ip 127.0.0.1 -s -r -t
		Option deployLearningOpt = new Option(DEPLOY_LEARNING_OPT, DEPLOY_LEARNING_LONG_OPT, false, "Deploy learning pipeline");

		Option queueName5Opt = new Option(QUEUE_NAME_OPT,QUEUE_NAME_LONG_OPT,true,"Name of the queue where the learning pipeline will receive the requests");
		queueName5Opt.setArgName("queue name");
		queueName5Opt.setRequired(true);

		Option queue1NameFEOpt = new Option(FE_QUEUE_NAME_OPT,FE_QUEUE_NAME_LONG_OPT,true,"Name of the queue where the feature extraction pipeline is listening");
		queue1NameFEOpt.setArgName("queue name");
		queue1NameFEOpt.setRequired(false);
		
		Option url1FEOpt = new Option(IP_ADDRESS_OPT,IP_ADDRESS_LONG_OPT,true,"IP address of the broker where the learning pipeline has to connected");
		url1FEOpt.setArgName("IP address");
		url1FEOpt.setRequired(true);
		
		Option sims1Opt = new Option(USE_SIMS_OPT, USE_SIMS_LONG_OPT, false, "Use similarity features");
		Option rank1Opt = new Option(USE_RANK_OPT, USE_RANK_LONG_OPT, false, "Use rank feature");
		Option trees1Opt = new Option(USE_TREES_OPT, USE_TREES_LONG_OPT, false, "Use trees");
		
		Options learningDeploymentOpts = new Options();
		learningDeploymentOpts.addOption(queueName5Opt);
		learningDeploymentOpts.addOption(url1FEOpt);
		learningDeploymentOpts.addOption(queue1NameFEOpt);
		learningDeploymentOpts.addOption(sims1Opt);
		learningDeploymentOpts.addOption(rank1Opt);
		learningDeploymentOpts.addOption(trees1Opt);
		learningDeploymentOpts.addOption(langOpt);

		
		//Undeployment options
		Option undeployOpt = new Option(UNDEPLOY_OPT, UNDEPLOY_LONG_OPT, false, "Undeploy a pipeline");

		Option idOpt = new Option(ID_OPT,ID_LONG_OPT,true,"ID of the UIMA-AS service");
		idOpt.setArgName("service ID");
		idOpt.setRequired(true);
		
		Options undeploymentOpts = new Options();
		undeploymentOpts.addOption(idOpt);
		
		
		//English Feature Extraction options
		//-ef -if /home/sromeo/workspaces/UIMA/workspace/S3QACoreFramework/src/test/resources/data/XML/SemEval/English/SemEval2016-Task3-CQA-QL-dev.xml -of representations.klp -ip 127.0.0.1 -qn englishFeatureExtractionQueue -rt qr
		//-af -if /home/sromeo/workspaces/UIMA/workspace/S3QACoreFramework/src/test/resources/data/XML/SemEval/Arabic/SemEval2016-Task3-CQA-MD-dev.xml -of representations.klp -ip 127.0.0.1 -qn arabicFeatureExtractionQueue
		Option englishExtractFeaturesOpt = new Option(ENGLISH_FEATURE_EXTRACTION_OPT, ENGLISH_FEATURE_EXTRACTION_LONG_OPT, false, "Extract features for an input english dataset");
		
		Option queueName2Opt = new Option(QUEUE_NAME_OPT,QUEUE_NAME_LONG_OPT,true,"Name of the queue where the feature extraction pipeline is listening");
		queueName2Opt.setArgName("queue name");
		queueName2Opt.setRequired(true);
		
		Option url2Opt = new Option(IP_ADDRESS_OPT,IP_ADDRESS_LONG_OPT,true,"IP address of the broker where the feature extraction pipeline is connected");
		url2Opt.setArgName("broker IP address");
		url2Opt.setRequired(true);
		
		Option ifOpt = new Option(INPUT_FILE_OPT,INPUT_FILE_LONG_OPT,true,"Input file");
		ifOpt.setArgName("file path");
		ifOpt.setRequired(true);
		
		Option ofOpt = new Option(OUTPUT_FILE_OPT,OUTPUT_FILE_LONG_OPT,true,"Output file where the extracted feature for each instance will be saved");
		ofOpt.setArgName("file path");
		ofOpt.setRequired(true);
		
		Option taskOpt = new Option(TASK_OPT, TASK_LONG_OPT, true, "Which re-ranking task perform: \"cr\" for comment re-ranking, \"qr\" for question re-ranking");
		taskOpt.setArgName("task");
		taskOpt.setRequired(true);
		
		Options processEnOpts = new Options();
		processEnOpts.addOption(url2Opt);
		processEnOpts.addOption(queueName2Opt);
		processEnOpts.addOption(ifOpt);
		processEnOpts.addOption(ofOpt);
		processEnOpts.addOption(taskOpt);
		
		
		//Arabic Feature Extraction options
		Option arabicExtractFeaturesOpt = new Option(ARABIC_FEATURE_EXTRACTION_OPT, ARABIC_FEATURE_EXTRACTION_LONG_OPT, false, "Extract features for an arabic input dataset");
		
		Options processArOpts = new Options();
		processArOpts.addOption(url2Opt);
		processArOpts.addOption(queueName2Opt);
		processArOpts.addOption(ifOpt);
		processArOpts.addOption(ofOpt);
		
		
		//Classification options
		//-c -if /home/sromeo/workspaces/UIMA/workspace/S3QACoreFramework/src/test/resources/data/XML/SemEval/English/SemEval2016-Task3-CQA-QL-dev.xml -of predictions.txt -ip 127.0.0.1 -qn classificationQueue -rt cr
		Option classificationOpt = new Option(CLASSIFICATION_OPT, CLASSIFICATION_LONG_OPT, false, "Classify an input dataset");
		
		Option queueName4Opt = new Option(QUEUE_NAME_OPT,QUEUE_NAME_LONG_OPT,true,"Name of the queue where the classification pipeline is listening");
		queueName4Opt.setArgName("queue name");
		queueName4Opt.setRequired(true);
		
		Option url3Opt = new Option(IP_ADDRESS_OPT,IP_ADDRESS_LONG_OPT,true,"IP address of the broker where the classification pipeline is connected");
		url3Opt.setArgName("broker URL");
		url3Opt.setRequired(true);
		
		Option if2Opt = new Option(INPUT_FILE_OPT,INPUT_FILE_LONG_OPT,true,"Input file");
		if2Opt.setArgName("file path");
		if2Opt.setRequired(true);
		
		Option of2Opt = new Option(OUTPUT_FILE_OPT,OUTPUT_FILE_LONG_OPT,true,"Output file where the predictions will be saved");
		of2Opt.setArgName("file path");
		of2Opt.setRequired(true);
		
		Option task1Opt = new Option(TASK_OPT, TASK_LONG_OPT, true, "Which re-ranking task perform: \"cr\" for comment re-ranking, \"qr\" for question re-ranking");
		task1Opt.setArgName("task");
		task1Opt.setRequired(true);
		
		Options classificationOpts = new Options();
		classificationOpts.addOption(url3Opt);
		classificationOpts.addOption(queueName4Opt);
		classificationOpts.addOption(if2Opt);
		classificationOpts.addOption(of2Opt);
		classificationOpts.addOption(task1Opt);
		
		
		//Learning options
		//-l -if /home/sromeo/workspaces/UIMA/workspace/S3QACoreFramework/src/test/resources/data/XML/SemEval/English/SemEval2016-Task3-CQA-QL-dev.xml -of model.txt -ip 127.0.0.1 -qn learningQueue -rt qr
		Option learningOpt = new Option(LEARNING_OPT, LEARNING_LONG_OPT, false, "Train a model using the input dataset");
		
		Option queueName6Opt = new Option(QUEUE_NAME_OPT,QUEUE_NAME_LONG_OPT,true,"Name of the queue where the learning pipeline is listening");
		queueName6Opt.setArgName("queue name");
		queueName6Opt.setRequired(true);
		
		Option url5Opt = new Option(IP_ADDRESS_OPT,IP_ADDRESS_LONG_OPT,true,"IP address of the broker where the learning pipeline is connected");
		url5Opt.setArgName("broker URL");
		url5Opt.setRequired(true);
		
		Option if3Opt = new Option(INPUT_FILE_OPT,INPUT_FILE_LONG_OPT,true,"Input file");
		if3Opt.setArgName("file path");
		if3Opt.setRequired(true);
		
		Option of3Opt = new Option(OUTPUT_FILE_OPT,OUTPUT_FILE_LONG_OPT,true,"Output file where the trained model file path will be saved");
		of3Opt.setArgName("file path");
		of3Opt.setRequired(true);
		
		Option task2Opt = new Option(TASK_OPT, TASK_LONG_OPT, true, "Which re-ranking task perform: \"cr\" for comment re-ranking, \"qr\" for question re-ranking");
		task2Opt.setArgName("task");
		task2Opt.setRequired(false);
		
		Options learningOpts = new Options();
		learningOpts.addOption(url5Opt);
		learningOpts.addOption(queueName6Opt);
		learningOpts.addOption(if3Opt);
		learningOpts.addOption(of3Opt);
		learningOpts.addOption(task2Opt);
		
		
		Option helpOpt = new Option(HELP_OPT, HELP_LONG_OPT, false, "Help");
		
		OptionGroup optGr = new OptionGroup();
		optGr.addOption(startBrokerOpt);
		optGr.addOption(deployTAOpt);
		optGr.addOption(deployFEOpt);
		optGr.addOption(deployClassificationOpt);
		optGr.addOption(deployLearningOpt);
		optGr.addOption(undeployOpt);
		optGr.addOption(arabicExtractFeaturesOpt);
		optGr.addOption(englishExtractFeaturesOpt);
		optGr.addOption(classificationOpt);
		optGr.addOption(learningOpt);
		optGr.addOption(helpOpt);
		optGr.setRequired(true);
		
		Options commandOptions = new Options();
		commandOptions.addOptionGroup(optGr);
		
		UimaAsynchronousEngine uimaAsEngine = new BaseUIMAAsynchronousEngine_impl();
		try {
			CommandLine line = parser.parse( commandOptions, Arrays.copyOfRange(args,0,Math.min(1, args.length)));
			if (line.hasOption(START_BROKER_OPT)) {
				line = parser.parse(startBrokerOpts, Arrays.copyOfRange(args,1,args.length));
				String ip = line.getOptionValue(IP_ADDRESS_OPT);
				
				startBroker(ip);
			} else if (line.hasOption(DEPLOY_TEXT_ANNOTATION_OPT)) {
				line = parser.parse(deployTAOpts, Arrays.copyOfRange(args,1,args.length));
				String queueName = line.getOptionValue(QUEUE_NAME_OPT);
				String ip = line.getOptionValue(IP_ADDRESS_OPT);
				int scaleout = Integer.parseInt(line.getOptionValue(SCALEOUT_OPT));
				String lang = line.getOptionValue(LANGUAGE_OPT);
				String id = depoyTextAnnotation(uimaAsEngine,"http://"+ip+":61616",queueName,lang,scaleout);
				System.out.println("Feature Extraction pipeline succefully deployed. Service ID: "+id);
			} else if (line.hasOption(DEPLOY_FEATURE_EXTRACTION_OPT)) {
				line = parser.parse( deployFEOpts, Arrays.copyOfRange(args,1,args.length));
				String queueName = line.getOptionValue(QUEUE_NAME_OPT);
				String ip = line.getOptionValue(IP_ADDRESS_OPT);
				String textAnnatationIP = line.getOptionValue(TEXT_ANNOTATION_IP_ADDRESS_OPT);
				String textAnnatationQueue = line.getOptionValue(TEXT_ANNOTATION_QUEUE_NAME_OPT);
				int scaleout = Integer.parseInt(line.getOptionValue(SCALEOUT_OPT));
				boolean useSims = line.hasOption(USE_SIMS_OPT);
				boolean useRank = line.hasOption(USE_RANK_OPT);
				boolean useTrees = line.hasOption(USE_TREES_OPT);
				String lang = line.getOptionValue(LANGUAGE_OPT);
				String id = depoyFeatureExtraction(uimaAsEngine,"http://"+ip+":61616",queueName,"http://"+textAnnatationIP+":61616",
						textAnnatationQueue,lang,scaleout,useSims,useRank,useTrees);
				System.out.println("Feature Extraction pipeline succefully deployed. Service ID: "+id);
			} else if (line.hasOption(ENGLISH_FEATURE_EXTRACTION_OPT)) {
				line = parser.parse(processEnOpts, Arrays.copyOfRange(args,1,args.length));
				String inputFile = line.getOptionValue(INPUT_FILE_OPT);
				String outputFile = line.getOptionValue(OUTPUT_FILE_OPT);
				String ip = line.getOptionValue(IP_ADDRESS_OPT);
				String queueName = line.getOptionValue(QUEUE_NAME_OPT);
				String task = line.getOptionValue(TASK_OPT);
				
				extractFeaturesEnglish(inputFile,outputFile,"http://"+ip+":61616",queueName,task);
			}  else if (line.hasOption(ARABIC_FEATURE_EXTRACTION_OPT)) {
				line = parser.parse(processArOpts, Arrays.copyOfRange(args,1,args.length));
				String inputFile = line.getOptionValue(INPUT_FILE_OPT);
				String outputFile = line.getOptionValue(OUTPUT_FILE_OPT);
				String ip = line.getOptionValue(IP_ADDRESS_OPT);
				String queueName = line.getOptionValue(QUEUE_NAME_OPT);
				
				extractFeaturesArabic(inputFile,outputFile,"http://"+ip+":61616",queueName);
			} else if (line.hasOption(DEPLOY_CLASSIFICATION_OPT)) {
				line = parser.parse(classificationDeploymentOpts, Arrays.copyOfRange(args,1,args.length));
				String queueName = line.getOptionValue(QUEUE_NAME_OPT);
				String featureExtractionQueueName = line.getOptionValue(FE_QUEUE_NAME_OPT);
				String ip = line.getOptionValue(IP_ADDRESS_OPT);
				String modelFile = line.getOptionValue(MODEL_FILE_OPT);
				String lang = line.getOptionValue(LANGUAGE_OPT);

				Starter.depoyClassification(uimaAsEngine, "http://"+ip+":61616", queueName,lang, 1, modelFile, "http://"+ip+":61616", featureExtractionQueueName);
			} else if (line.hasOption(CLASSIFICATION_OPT)) {
				line = parser.parse(classificationOpts, Arrays.copyOfRange(args,1,args.length));
				String inputFile = line.getOptionValue(INPUT_FILE_OPT);
				String outputFile = line.getOptionValue(OUTPUT_FILE_OPT);
				String ip = line.getOptionValue(IP_ADDRESS_OPT);
				String queueName = line.getOptionValue(QUEUE_NAME_OPT);
				String task = line.getOptionValue(TASK_OPT);
				
				classify(inputFile,outputFile,"http://"+ip+":61616",queueName,task);
			} else if (line.hasOption(DEPLOY_LEARNING_OPT)) {
				line = parser.parse(learningDeploymentOpts, Arrays.copyOfRange(args,1,args.length));
				String queueName = line.getOptionValue(QUEUE_NAME_OPT);
				String featureExtractionQueueName = line.getOptionValue(FE_QUEUE_NAME_OPT);
				String ip = line.getOptionValue(IP_ADDRESS_OPT);
				boolean useSims = line.hasOption(USE_SIMS_OPT);
				boolean useRank = line.hasOption(USE_RANK_OPT);
				boolean useTrees = line.hasOption(USE_TREES_OPT);
				String lang = line.getOptionValue(LANGUAGE_OPT);

				Starter.depoyLearning(uimaAsEngine, "http://"+ip+":61616", queueName,lang, 1, "http://"+ip+":61616",featureExtractionQueueName,useSims,useRank,useTrees);
			} else if (line.hasOption(LEARNING_OPT)) {
				line = parser.parse(learningOpts, Arrays.copyOfRange(args,1,args.length));
				String inputFile = line.getOptionValue(INPUT_FILE_OPT);
				String outputFile = line.getOptionValue(OUTPUT_FILE_OPT);
				String ip = line.getOptionValue(IP_ADDRESS_OPT);
				String queueName = line.getOptionValue(QUEUE_NAME_OPT);
				String task = line.getOptionValue(TASK_OPT);
				
				learn(inputFile,outputFile,"http://"+ip+":61616",queueName,task);
			}  else if (line.hasOption(UNDEPLOY_OPT)) {
				line = parser.parse( undeploymentOpts, Arrays.copyOfRange(args,1,args.length));
				String id = line.getOptionValue(ID_OPT);
				undeployPipeline(id,uimaAsEngine);
			} else if (line.hasOption(HELP_OPT)) {
				printHelp(commandOptions,startBrokerOpts, deployTAOpts, deployFEOpts, processArOpts, processEnOpts, 
						classificationDeploymentOpts, classificationOpts,
						learningDeploymentOpts, learningOpts,
						undeploymentOpts);
			}
			
		} catch (ParseException e) {
			e.printStackTrace();
			printHelp(commandOptions,startBrokerOpts, deployTAOpts, deployFEOpts, processArOpts, processEnOpts, 
					classificationDeploymentOpts, classificationOpts,
					learningDeploymentOpts, learningOpts,
					undeploymentOpts);
		} finally {
			uimaAsEngine.stop();
		}
	}
	
	public static void printHelp(Options commandOptions, Options startBrokerOpts, Options deployTAOpts,Options deployFEOpts,Options processArOpts,Options processEnOpts
			,Options classificationDeploymentOpts,Options classificationOpts
			,Options learningDeploymentOpts,Options learningOpts
			,Options undeploymentOpts) throws IOException {
		HelpFormatter formatter = new HelpFormatter();

		StringWriter sw = new StringWriter();
    	PrintWriter pw = new PrintWriter(sw);
    	
    	formatter.printUsage(pw, 1000000000, "java qa.qcri.iyas.Starter ", commandOptions);
    	pw.flush();
    	pw.close();
    	String cmd = sw.toString();
    	sw.close();
    	
    	System.out.println(cmd);
    	
    	sw = new StringWriter();
    	pw = new PrintWriter(sw);
    	pw.println("Commands:");
    	formatter.printOptions(pw, 1000, commandOptions, 2, 5);
    	
    	pw.println("\nStart Broker options:");
    	formatter.printOptions(pw, 1000, startBrokerOpts, 4, 5);
    	pw.println("\nText annotation pipeline deployment options:");
    	formatter.printOptions(pw, 1000, deployTAOpts, 4, 5);
    	pw.println("\nFeature extraction pipeline deployment options:");
    	formatter.printOptions(pw, 1000, deployFEOpts, 4, 5);
    	pw.println("\nClassification pipeline deployment options:");
    	formatter.printOptions(pw, 2000, classificationDeploymentOpts, 4, 5);
    	pw.println("\nLearning pipeline deployment options:");
    	formatter.printOptions(pw, 2000, learningDeploymentOpts, 4, 5);
    	
    	pw.println("\nArabic Feature extraction options:");
    	formatter.printOptions(pw, 2000, processArOpts, 4, 5);
    	pw.println("\nEnglish Feature extraction options:");
    	formatter.printOptions(pw, 2000, processEnOpts, 4, 5);
    	pw.println("\nClassification options:");
    	formatter.printOptions(pw, 2000, classificationOpts, 4, 5);
    	pw.println("\nLearning options:");
    	formatter.printOptions(pw, 2000, learningOpts, 4, 5);
    	
    	pw.flush();pw.println("\nUndeployment options:");
    	formatter.printOptions(pw, 2000, undeploymentOpts, 4, 5);
    	pw.flush();
    	System.out.println(sw.toString());
	}
}

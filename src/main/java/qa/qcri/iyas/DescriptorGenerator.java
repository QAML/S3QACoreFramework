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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.Constants;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.metadata.FixedFlow;
import org.apache.uima.analysis_engine.metadata.impl.FixedFlow_impl;
import org.apache.uima.analysis_engine.metadata.impl.FlowControllerDeclaration_impl;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.flow.impl.FixedFlowController;
import org.apache.uima.resource.ExternalResourceDependency;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.ConfigurationParameterDeclarations;
import org.apache.uima.resource.metadata.ConfigurationParameterSettings;
import org.apache.uima.resource.metadata.Import;
import org.apache.uima.resource.metadata.impl.ConfigurationParameter_impl;
import org.apache.uima.resourceSpecifier.factory.ColocatedDelegateConfiguration;
import org.apache.uima.resourceSpecifier.factory.DelegateConfiguration;
import org.apache.uima.resourceSpecifier.factory.DeploymentDescriptorFactory;
import org.apache.uima.resourceSpecifier.factory.ErrorHandlingSettings;
import org.apache.uima.resourceSpecifier.factory.RemoteDelegateConfiguration;
import org.apache.uima.resourceSpecifier.factory.SerializationStrategy;
import org.apache.uima.resourceSpecifier.factory.ServiceContext;
import org.apache.uima.resourceSpecifier.factory.UimaASAggregateDeploymentDescriptor;
import org.apache.uima.resourceSpecifier.factory.impl.ColocatedDelegateConfigurationImpl;
import org.apache.uima.resourceSpecifier.factory.impl.ServiceContextImpl;
import org.apache.uima.util.InvalidXMLException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.ExternalResourceFactory;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpChunker;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import qa.qcri.iyas.classification.ClassificationAnnotator;
import qa.qcri.iyas.classification.kelp.KeLPClassifier;
import qa.qcri.iyas.data.preprocessing.InputJCasMultiplier;
import qa.qcri.iyas.data.preprocessing.JCasPairGenerator;
import qa.qcri.iyas.data.preprocessing.ProcessedJCASAggregator;
import qa.qcri.iyas.data.preprocessing.StandardPreprocessor;
import qa.qcri.iyas.data.preprocessing.Stopwords;
import qa.qcri.iyas.feature.Feature;
import qa.qcri.iyas.feature.RankFeature;
import qa.qcri.iyas.feature.VectorialFeaturesAnnotator;
import qa.qcri.iyas.feature.similarity.CosineBowSimilarity;
import qa.qcri.iyas.feature.similarity.CosineTokenSimilarity;
import qa.qcri.iyas.feature.similarity.GreedyStringTilingSimilarity;
import qa.qcri.iyas.feature.similarity.LongestCommonSubsequenceComparatorSimilarity;
import qa.qcri.iyas.feature.similarity.LongestCommonSubsequenceNormComparatorSimilarity;
import qa.qcri.iyas.feature.similarity.LongestCommonSubstringComparatorSimilarity;
import qa.qcri.iyas.feature.similarity.SimilarityMeasure;
import qa.qcri.iyas.feature.similarity.TreeKernelSimilarity;
import qa.qcri.iyas.feature.similarity.WordNGramContainmentMeasureSimilarity;
import qa.qcri.iyas.feature.similarity.WordNGramJaccardMeasureSimilarity;
import qa.qcri.iyas.learning.LearningAnnotator;
import qa.qcri.iyas.learning.kelp.KeLPSVMLearner;
import qa.qcri.iyas.representation.RepresentantationExtractor;
import qa.qcri.iyas.representation.decorator.CQAPairIdentifierDecorator;
import qa.qcri.iyas.representation.decorator.DecorationAnnotator;
import qa.qcri.iyas.representation.decorator.JCasDecorator;
import qa.qcri.iyas.representation.decorator.TreePairDecorator;
import qa.qcri.iyas.representation.kelp.KeLPSerializer;

public class DescriptorGenerator {
	
	private static Document loadDescriptor(String file) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		return builder.build(file);
	}
	
	private static void saveDescriptor(Document descriptor, String file) throws IOException {
		XMLOutputter xmlOut = new XMLOutputter();
		BufferedWriter out = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file)));
		out.write(xmlOut.outputString(descriptor));
		out.close();
	}
	
	private static void updateRemoteReplyQueueScaleoutAttribute(Document descriptor,String key,int scaleout) throws JDOMException, IOException {
		Element root = descriptor.getRootElement();
		Element delegatesElement = root.getChild("deployment",root.getNamespace()).getChild("service",root.getNamespace())
				.getChild("analysisEngine",root.getNamespace()).getChild("delegates",root.getNamespace());
		Element remoteAnnotatorAAEElement = null;
		for (Element delegate : delegatesElement.getChildren()) {
			if (delegate.getAttributeValue("key").equals(key)) {
				remoteAnnotatorAAEElement = delegate;
				break;
			}
		}
		remoteAnnotatorAAEElement.setAttribute("remoteReplyQueueScaleout", ""+scaleout);
//		Element myAnnotatorAEElement = myAnnotatorAAEElement.getChild("delegates",root.getNamespace())
//				.getChild("analysisEngine",root.getNamespace());
//		Element scaleoutElement = new Element("scaleout", root.getNamespace());
//		scaleoutElement.setAttribute("numberOfInstances", ""+scaleout);
//		myAnnotatorAEElement.addContent(scaleoutElement);
	}
	
	private static void addScaleoutElementFirstLevel(Document descriptor,String key,int scaleout) throws JDOMException, IOException {
		Element root = descriptor.getRootElement();
		Element delegatesElement = root.getChild("deployment",root.getNamespace()).getChild("service",root.getNamespace())
				.getChild("analysisEngine",root.getNamespace()).getChild("delegates",root.getNamespace());
		Element myAnnotatorAAEElement = null;
		for (Element delegate : delegatesElement.getChildren()) {
			if (delegate.getAttributeValue("key").equals(key)) {
				myAnnotatorAAEElement = delegate;
				break;
			}
		}
		Element myAnnotatorAEElement = myAnnotatorAAEElement.getChild("delegates",root.getNamespace())
				.getChild("analysisEngine",root.getNamespace());
		Element scaleoutElement = new Element("scaleout", root.getNamespace());
		scaleoutElement.setAttribute("numberOfInstances", ""+scaleout);
		myAnnotatorAEElement.addContent(scaleoutElement);
	}
	
	private static void addScaleoutElementSecondLevel(Document descriptor, String firstLevelKey,
			String secondLevelKey,int scaleout) throws JDOMException, IOException {
		Element root = descriptor.getRootElement();
		Element delegatesElement = root.getChild("deployment",root.getNamespace()).getChild("service",root.getNamespace())
				.getChild("analysisEngine",root.getNamespace()).getChild("delegates",root.getNamespace());
		Element annotatorFirstLevelElement = null;
		for (Element delegate : delegatesElement.getChildren()) {
			if (delegate.getAttributeValue("key").equals(firstLevelKey)) {
				annotatorFirstLevelElement = delegate;
				break;
			}
		}
		
		Element annotatorSecondLevelElement = null;
		for (Element delegate : annotatorFirstLevelElement.getChild("delegates",root.getNamespace()).getChildren()) {
			if (delegate.getAttributeValue("key").equals(secondLevelKey)) {
				annotatorSecondLevelElement = delegate;
				break;
			}
		}
		
		Element annotatorElement = annotatorSecondLevelElement.getChild("delegates",root.getNamespace())
				.getChild("analysisEngine",root.getNamespace());
		Element scaleoutElement = new Element("scaleout", root.getNamespace());
		scaleoutElement.setAttribute("numberOfInstances", ""+scaleout);
		annotatorElement.addContent(scaleoutElement);
	}
	
	private static String generateInputJCasMultiplierAAEDescriptor(String root_folder,boolean concatenate) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException {
		new File(root_folder+"/descriptors/preprocessing").mkdirs();
		
		System.out.println("Generating XML description for InputJCasMultiplierAE_Descriptor");
		AnalysisEngineDescription inputJCasMultiplierAEDescriptor = AnalysisEngineFactory.createEngineDescription(
				InputJCasMultiplier.class,
				InputJCasMultiplier.CONCATENATE_PARAM,concatenate);
		ExternalResourceFactory.bindResource(inputJCasMultiplierAEDescriptor,
				InputJCasMultiplier.PREPROCESSOR_EXTERNAL_RESOURCE, StandardPreprocessor.class);
		inputJCasMultiplierAEDescriptor.toXML(
				new FileOutputStream(root_folder+"/descriptors/preprocessing"+"/InputJCasMultiplierAE_Descriptor.xml"));
	
		System.out.println("Generating XML description for InputJCasMultiplierAAE_Descriptor");
		AnalysisEngineDescription inputJCasMultiplierAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		inputJCasMultiplierAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		inputJCasMultiplierAAE.setPrimitive(false);

		inputJCasMultiplierAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(true);
		inputJCasMultiplierAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		inputJCasMultiplierAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
		
		Import inputJCasMultiplierAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		inputJCasMultiplierAEImport.setName("descriptors.preprocessing.InputJCasMultiplierAE_Descriptor");
		inputJCasMultiplierAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("InputJCasMultiplierAE", inputJCasMultiplierAEImport);
		
		List<String> flowNames1 = new ArrayList<String>();
		flowNames1.add("InputJCasMultiplierAE");
		
		FixedFlow fixedFlow1 = new FixedFlow_impl();
	    fixedFlow1.setFixedFlow(flowNames1.toArray(new String[flowNames1.size()]));
	    inputJCasMultiplierAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow1);
		
	    inputJCasMultiplierAAE.toXML(
				new FileOutputStream(root_folder+"/descriptors/preprocessing"+"/InputJCasMultiplierAAE_Descriptor.xml"));		
		
	    return "descriptors.preprocessing.InputJCasMultiplierAAE_Descriptor";
	}
	
//	private static String generateInputJCasMultiplierPipelineAAEDescriptor(String root_folder) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException {
//		new File(root_folder+"/descriptors/preprocessing").mkdirs();
//		
//		String ijmDescr = generateInputJCasMultiplierAAEDescriptor(root_folder,true);
//
//		//Generates a AAE descriptor for the testing pipeline
//		Import flowControllerImport = UIMAFramework.getResourceSpecifierFactory().createImport();
//	    flowControllerImport.setName("org.apache.uima.flow.FixedFlowController");
//	    FlowControllerDeclaration_impl flowControllerDeclaration = new FlowControllerDeclaration_impl();
//		flowControllerDeclaration.setImport(flowControllerImport);
//		flowControllerDeclaration.setKey("FixedFlowController");
//		
//		AnalysisEngineDescription pipelineAAE = AnalysisEngineFactory.createEngineDescription(
//				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
//		pipelineAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
//		pipelineAAE.setPrimitive(false);
//
//		pipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(true);
//		pipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
//		pipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
//		
//		
//		pipelineAAE.setFlowControllerDeclaration(flowControllerDeclaration);
//		ConfigurationParameterDeclarations parameters = pipelineAAE.getAnalysisEngineMetaData().getConfigurationParameterDeclarations();
//		ConfigurationParameter_impl param = new ConfigurationParameter_impl();
//		param.setName(FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER);
//		param.setType("String");
//		param.addOverride("FixedFlowController/"+FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER);
//		parameters.addConfigurationParameter(param);
//		ConfigurationParameterSettings settings = pipelineAAE.getAnalysisEngineMetaData().getConfigurationParameterSettings();
//		settings.setParameterValue(FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER, "drop");
//		
//		
//		Import inputJCasMultiplierAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
//		inputJCasMultiplierAEImport.setName(ijmDescr);
//		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("InputJCasMultiplierAAE", inputJCasMultiplierAEImport);
//		
//		List<String> flowNames = new ArrayList<String>();
//		flowNames.add("InputJCasMultiplierAAE");
//		
//		FixedFlow fixedFlow = new FixedFlow_impl();
//	    fixedFlow.setFixedFlow(flowNames.toArray(new String[flowNames.size()]));
//	    pipelineAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow);
//  
//	    pipelineAAE.toXML(
//				new FileOutputStream(new File(root_folder+"/descriptors/preprocessing/InputJCasMultiplierPipelineAAE_Descriptor.xml").getAbsolutePath()));
//	    
//	    return "descriptors.preprocessing.InputJCasMultiplierPipelineAAE_Descriptor";
//	    
//	}
//	
//	public static String generateInputJCasMultiplierPipelineAAEDeploymentDescriptor(String queueName) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException {
//		try {
//			String rootFolder = new File(System.getProperty("user.dir")).getAbsolutePath();
//			new File(rootFolder+"/descriptors").mkdir();
//			
//			String aaeDescr = generateInputJCasMultiplierPipelineAAEDescriptor(rootFolder);
//			
//			System.out.println("Generating XML description for InputJCasMultiplierPipelineAAE_DeploymentDescriptor");
//			ServiceContext pipelineContext = new ServiceContextImpl("InputJCasMultiplierPipelineAAE", 
//								           "InputJCasMultiplierPipelineAAE_Descriptor",
//								           aaeDescr, 
//								           queueName, "tcp://localhost:61616");
//			pipelineContext.setCasPoolSize(10);
//			
//			
//			ColocatedDelegateConfiguration delegate11 = new ColocatedDelegateConfigurationImpl("InputJCasMultiplierAE", new DelegateConfiguration[0]);
//			delegate11.setCasMultiplier(true);
//			delegate11.setCasPoolSize(10);
//			ColocatedDelegateConfiguration spCldd1 = new ColocatedDelegateConfigurationImpl("InputJCasMultiplierAAE", new DelegateConfiguration[]{delegate11});
//			
//			UimaASAggregateDeploymentDescriptor spdd = DeploymentDescriptorFactory.createAggregateDeploymentDescriptor(
//					pipelineContext,spCldd1);
//			
//			BufferedWriter out = new BufferedWriter(
//					new OutputStreamWriter(
//							new FileOutputStream(rootFolder+"/descriptors/FeatureExtractionPipelineAAE_DeploymentDescriptor.xml")));
//			out.write(spdd.toXML());
//			out.close();
//			
//			return rootFolder+"/descriptors"+"/FeatureExtractionPipelineAAE_DeploymentDescriptor.xml";
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return null;
//		}
//	}
	
	private static String generateStandardTextAnnotatorAAEDescriptor(String root_folder) throws ResourceInitializationException, FileNotFoundException, IOException, SAXException {
		new File(root_folder+"/descriptors/preprocessing").mkdirs();
		//Generates the AE descriptor for StandardPreprocessingAnnotator
		System.out.println("Generating XML description for StandardSimpleFeatureExtractor primitives");
		
		System.out.println("	Generating XML description for SegmenterAE_Descriptor");
		AnalysisEngineDescription segmenter = AnalysisEngineFactory.createEngineDescription(
				OpenNlpSegmenter.class);
		segmenter.toXML(
				new FileOutputStream(root_folder+"/descriptors/preprocessing"+"/SegmenterAE_Descriptor.xml"));
	
		System.out.println("	Generating XML description for POSTaggerAE_Descriptor");
		AnalysisEngineDescription posTagger = AnalysisEngineFactory.createEngineDescription(
				OpenNlpPosTagger.class);
		posTagger.toXML(
				new FileOutputStream(root_folder+"/descriptors/preprocessing"+"/POSTaggerAE_Descriptor.xml"));

		System.out.println("	Generating XML description for LemmatizerAE_Descriptor");
		AnalysisEngineDescription lemmatizer = AnalysisEngineFactory.createEngineDescription(
				StanfordLemmatizer.class);
		lemmatizer.toXML(
				new FileOutputStream(root_folder+"/descriptors/preprocessing"+"/LemmatizerAE_Descriptor.xml"));

		System.out.println("	Generating XML description for ChunkerAE_Descriptor");
		AnalysisEngineDescription chunker = AnalysisEngineFactory.createEngineDescription(
				OpenNlpChunker.class);
		chunker.toXML(
				new FileOutputStream(root_folder+"/descriptors/preprocessing"+"/ChunkerAE_Descriptor.xml"));

		
		System.out.println("Generating XML description for StandardPreprocessingAnnotatorAAE_Descriptor");
		//Generates a AAE descriptor with only the MyAnnotator
		AnalysisEngineDescription standardPreprocessingAnnotatorAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		standardPreprocessingAnnotatorAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		standardPreprocessingAnnotatorAAE.setPrimitive(false);
		
		standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(true);
		standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(false);

		Import segmenterAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		segmenterAEImport.setName("descriptors.preprocessing.SegmenterAE_Descriptor");
		standardPreprocessingAnnotatorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("SegmenterAE", segmenterAEImport);
		
		Import posTaggerAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		posTaggerAEImport.setName("descriptors.preprocessing.POSTaggerAE_Descriptor");
		standardPreprocessingAnnotatorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("POSTaggerAE", posTaggerAEImport);
		
		Import lammatizerAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		lammatizerAEImport.setName("descriptors.preprocessing.LemmatizerAE_Descriptor");
		standardPreprocessingAnnotatorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("LemmatizerAE", lammatizerAEImport);
		
		Import chunkerAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		chunkerAEImport.setName("descriptors.preprocessing.ChunkerAE_Descriptor");
		standardPreprocessingAnnotatorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("ChunkerAE", chunkerAEImport);
		
		List<String> flowNames = new ArrayList<String>();
		flowNames.add("SegmenterAE");
		flowNames.add("POSTaggerAE");
		flowNames.add("LemmatizerAE");
		flowNames.add("ChunkerAE");
		
		FixedFlow fixedFlow = new FixedFlow_impl();
	    fixedFlow.setFixedFlow(flowNames.toArray(new String[flowNames.size()]));
	    standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow);
	    
		standardPreprocessingAnnotatorAAE.toXML(
				new FileOutputStream(new File(root_folder+"/descriptors/preprocessing"+"/StandardTextAnnotatorAAE_Descriptor.xml").getAbsolutePath()));
		
		return "descriptors.preprocessing.StandardTextAnnotatorAAE_Descriptor";
	}
	
	private static String generateStandardTextAnnotatorDescriptor(String root_folder) throws ResourceInitializationException, FileNotFoundException, IOException, SAXException {
		new File(root_folder+"/descriptors/preprocessing").mkdirs();
		
		String aaeDescr = generateStandardTextAnnotatorAAEDescriptor(root_folder);

		
		System.out.println("Generating XML description for StandardPreprocessingAnnotatorAAE_Descriptor");
		//Generates a AAE descriptor with only the MyAnnotator
		AnalysisEngineDescription standardPreprocessingAnnotatorAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		standardPreprocessingAnnotatorAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		standardPreprocessingAnnotatorAAE.setPrimitive(false);
		
		standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(true);
		standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(false);
		
		Import standardTextAnnotatorAAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		standardTextAnnotatorAAEImport.setName(aaeDescr);
		standardPreprocessingAnnotatorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("StandardTextAnnotatorAAE", standardTextAnnotatorAAEImport);
		
		List<String> flowNames = new ArrayList<String>();
		flowNames.add("StandardTextAnnotatorAAE");
		
		FixedFlow fixedFlow = new FixedFlow_impl();
	    fixedFlow.setFixedFlow(flowNames.toArray(new String[flowNames.size()]));
	    standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow);
	    
		standardPreprocessingAnnotatorAAE.toXML(
				new FileOutputStream(new File(root_folder+"/descriptors/preprocessing"+"/StandardTextAnnotator_Descriptor.xml").getAbsolutePath()));
		
		return "descriptors.preprocessing.StandardTextAnnotator_Descriptor";
	}
	
	private static String generateProcessedJCasAggregatorAAEDescriptor(String root_folder) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException {
		new File(root_folder+"/descriptors/preprocessing").mkdirs();
		
		System.out.println("Generating XML description for ProcessedJCASAggregatorAE_Descriptor");
		AnalysisEngineDescription processedJCASAggregatorAEDescriptor = AnalysisEngineFactory.createEngineDescription(
				ProcessedJCASAggregator.class);
//		ExternalResourceFactory.bindResource(processedJCASAggregatorAEDescriptor,
//				ProcessedJCASAggregator.PARAM_PROCESSED_INSTANCES_MANAGER_RESOURCE, ProcessedInstancesManager.class,"",
//				ExternalResourceFactory.PARAM_RESOURCE_NAME,"processedInstancesManager");
		processedJCASAggregatorAEDescriptor.toXML(
				new FileOutputStream(root_folder+"/descriptors/preprocessing"+"/ProcessedJCASAggregatorAE_Descriptor.xml"));
		
		System.out.println("Generating XML description for ProcessedJCASAggregatorAAE_Descriptor");
		AnalysisEngineDescription processedJCasMultiplierAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		processedJCasMultiplierAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		processedJCasMultiplierAAE.setPrimitive(false);

		processedJCasMultiplierAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(false);
		processedJCasMultiplierAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		processedJCasMultiplierAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
		
		Import processedJCasMultiplierAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		processedJCasMultiplierAEImport.setName("descriptors.preprocessing.ProcessedJCASAggregatorAE_Descriptor");
		processedJCasMultiplierAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("ProcessedJCASAggregatorAE", processedJCasMultiplierAEImport);
		
		List<String> flowNames2 = new ArrayList<String>();
		flowNames2.add("ProcessedJCASAggregatorAE");
		
		FixedFlow fixedFlow2 = new FixedFlow_impl();
	    fixedFlow2.setFixedFlow(flowNames2.toArray(new String[flowNames2.size()]));
	    processedJCasMultiplierAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow2);
  
	    processedJCasMultiplierAAE.toXML(
				new FileOutputStream(root_folder+"/descriptors/preprocessing"+"/ProcessedJCASAggregatorAAE_Descriptor.xml"));
	    
	    return "descriptors.preprocessing.ProcessedJCASAggregatorAAE_Descriptor";
		
	}
	
	private static String generateJCasPairGeneratorAAEDescriptor(String root_folder) throws ResourceInitializationException, FileNotFoundException, SAXException, IOException {
		new File(root_folder+"/descriptors/feature").mkdirs();
		System.out.println("Generating XML description for JCasPairGeneratorAE_Descriptor");
		AnalysisEngineDescription jcasPairGeneratorAEDescriptor = AnalysisEngineFactory.createEngineDescription(
				JCasPairGenerator.class);
		jcasPairGeneratorAEDescriptor.toXML(
				new FileOutputStream(root_folder+"/descriptors/feature/JCasPairGeneratorAE_Descriptor.xml"));
		
		System.out.println("Generating XML description for JCasPairGeneratorAAE_Descriptor");
		AnalysisEngineDescription jcasPairGeneratorAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		jcasPairGeneratorAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		jcasPairGeneratorAAE.setPrimitive(false);

		jcasPairGeneratorAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(false);
		jcasPairGeneratorAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		jcasPairGeneratorAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
		
		Import jcasPairGeneratorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		jcasPairGeneratorAEImport.setName("descriptors.feature.JCasPairGeneratorAE_Descriptor");
		jcasPairGeneratorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("JCasPairGeneratorAE", jcasPairGeneratorAEImport);
		
		List<String> flowNames2 = new ArrayList<String>();
		flowNames2.add("JCasPairGeneratorAE");
		
		FixedFlow fixedFlow2 = new FixedFlow_impl();
	    fixedFlow2.setFixedFlow(flowNames2.toArray(new String[flowNames2.size()]));
	    jcasPairGeneratorAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow2);
  
	    jcasPairGeneratorAAE.toXML(
				new FileOutputStream(root_folder+"/descriptors/feature/JCasPairGeneratorAAE_Descriptor.xml"));
	    
	    return "descriptors.feature.JCasPairGeneratorAAE_Descriptor";
	}

	private static List<ExternalResourceDependency> bindSimilarities(AnalysisEngineDescription descr) throws InvalidXMLException {
		List<ExternalResourceDependency> dependencyList = new LinkedList<>();
		
		int[][] lemmaIntervals = new int[][]{
			new int[]{1, 1},
			new int[]{1, 2},
			new int[]{1, 3},
			new int[]{2, 3},
			new int[]{2, 4},
			new int[]{3, 4},
		};
	
		int[][] posIntervals = new int[][]{
			new int[]{1, 3},
			new int[]{1, 4},
			new int[]{2, 4},
		};
		
		String simName = null;
		
//		for(int[] interval : lemmaIntervals) {
//			simName = "CosineBowSimilarity-Lemmas-NoStopwording-"+interval[0]+"-"+interval[1];
//			dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, SimilarityMeasure.class,false));
//			descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
//			
//			ExternalResourceFactory.bindResource(descr,
//					simName, CosineBowSimilarity.class,"",
//					ExternalResourceFactory.PARAM_RESOURCE_NAME,simName,
//					CosineBowSimilarity.PARAM_NAME_STOPWORDS_OBJECT, Stopwords.STOPWORD_EN,
//					CosineBowSimilarity.PARAM_NAME_REMOVE_STOPWORDS, false,
//					CosineBowSimilarity.PARAM_NAME_MIN_N_GRAM_SIZE, interval[0],
//					CosineBowSimilarity.PARAM_NAME_MAX_N_GRAM_SIZE, interval[1],
//					CosineBowSimilarity.PARAM_NAME_REPRESENTATION_TYPE, CosineBowSimilarity.PARAMETER_LIST_LEMMAS);
//			
//			simName = "CosineBowSimilarity-Lemmas-Stopwording-"+interval[0]+"-"+interval[1];
//			dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, SimilarityMeasure.class,false));
//			descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
//			
//			ExternalResourceFactory.bindResource(descr,
//					simName, CosineBowSimilarity.class,"",
//					ExternalResourceFactory.PARAM_RESOURCE_NAME,simName,
//					CosineBowSimilarity.PARAM_NAME_STOPWORDS_OBJECT, Stopwords.STOPWORD_EN,
//					CosineBowSimilarity.PARAM_NAME_REMOVE_STOPWORDS, true,
//					CosineBowSimilarity.PARAM_NAME_MIN_N_GRAM_SIZE, interval[0],
//					CosineBowSimilarity.PARAM_NAME_MAX_N_GRAM_SIZE, interval[1],
//					CosineBowSimilarity.PARAM_NAME_REPRESENTATION_TYPE, CosineBowSimilarity.PARAMETER_LIST_LEMMAS);
//		}
//		
//		
//		for(int[] interval : posIntervals) {
//			simName = "CosineBowSimilarity-POSTags-NoStopwording-"+interval[0]+"-"+interval[1];
//			dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, SimilarityMeasure.class,false));
//			descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
//			
//			ExternalResourceFactory.bindResource(descr,
//					simName, CosineBowSimilarity.class,"",
//					ExternalResourceFactory.PARAM_RESOURCE_NAME,simName,
//					CosineBowSimilarity.PARAM_NAME_STOPWORDS_OBJECT, Stopwords.STOPWORD_EN,
//					CosineBowSimilarity.PARAM_NAME_REMOVE_STOPWORDS, false,
//					CosineBowSimilarity.PARAM_NAME_MIN_N_GRAM_SIZE, interval[0],
//					CosineBowSimilarity.PARAM_NAME_MAX_N_GRAM_SIZE, interval[1],
//					CosineBowSimilarity.PARAM_NAME_REPRESENTATION_TYPE, CosineBowSimilarity.PARAMETER_LIST_POSTAGS);
//		}
		
		
		simName = "GreedyStringTilingSimilarity";
		dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, SimilarityMeasure.class,false));
		descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
		
		ExternalResourceFactory.bindResource(descr,
				simName, GreedyStringTilingSimilarity.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,simName,
				GreedyStringTilingSimilarity.PARAM_NAME_STOPWORDS_OBJECT, Stopwords.STOPWORD_EN,
				GreedyStringTilingSimilarity.PARAM_NAME_REMOVE_STOPWORDS, false,
				GreedyStringTilingSimilarity.PARAM_NAME_TILE_LENGTH, 3);
		
		simName = "LongestCommonSubsequenceComparatorSimilarity";
		dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, SimilarityMeasure.class,false));
		descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
		
		ExternalResourceFactory.bindResource(descr,
				simName, LongestCommonSubsequenceComparatorSimilarity.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,simName,
				LongestCommonSubsequenceComparatorSimilarity.PARAM_NAME_STOPWORDS_OBJECT, Stopwords.STOPWORD_EN,
				LongestCommonSubsequenceComparatorSimilarity.PARAM_NAME_REMOVE_STOPWORDS, false);
		
		
		simName = "LongestCommonSubsequenceNormComparatorSimilarity";
		dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, SimilarityMeasure.class,false));
		descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
		
		ExternalResourceFactory.bindResource(descr,
				simName, LongestCommonSubsequenceNormComparatorSimilarity.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,simName,
				LongestCommonSubsequenceNormComparatorSimilarity.PARAM_NAME_STOPWORDS_OBJECT, Stopwords.STOPWORD_EN,
				LongestCommonSubsequenceNormComparatorSimilarity.PARAM_NAME_REMOVE_STOPWORDS, false);
		
		
		simName = "LongestCommonSubstringComparatorSimilarity";
		dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, SimilarityMeasure.class,false));
		descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
		
		ExternalResourceFactory.bindResource(descr,
				simName, LongestCommonSubstringComparatorSimilarity.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,simName,
				LongestCommonSubstringComparatorSimilarity.PARAM_NAME_STOPWORDS_OBJECT, Stopwords.STOPWORD_EN,
				LongestCommonSubstringComparatorSimilarity.PARAM_NAME_REMOVE_STOPWORDS, false);
		

		for (int i=1;i<=4;i++) {
			simName = "WordNGramJaccardMeasureSimilarity-"+i;
			dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, SimilarityMeasure.class,false));
			descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
			
			ExternalResourceFactory.bindResource(descr,
					simName, WordNGramJaccardMeasureSimilarity.class,"",
					ExternalResourceFactory.PARAM_RESOURCE_NAME,simName,
					WordNGramJaccardMeasureSimilarity.PARAM_NAME_STOPWORDS_OBJECT, Stopwords.STOPWORD_EN,
					WordNGramJaccardMeasureSimilarity.PARAM_NAME_REMOVE_STOPWORDS, false,
					WordNGramJaccardMeasureSimilarity.PARAM_NAME_NGRAM_LENGTH, i);
		}
		
		for (int i=1;i<=2;i++) {
			simName = "WordNGramContainmentMeasureSimilarity-"+i;
			dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, SimilarityMeasure.class,false));
			descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
			
			ExternalResourceFactory.bindResource(descr,
					simName, WordNGramContainmentMeasureSimilarity.class,"",
					ExternalResourceFactory.PARAM_RESOURCE_NAME,simName,
					WordNGramContainmentMeasureSimilarity.PARAM_NAME_STOPWORDS_OBJECT, Stopwords.STOPWORD_EN,
					WordNGramContainmentMeasureSimilarity.PARAM_NAME_REMOVE_STOPWORDS, false,
					WordNGramContainmentMeasureSimilarity.PARAM_NAME_NGRAM_LENGTH, i);
		}
		

		simName = "CosineTokenSimilarity";
		dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, SimilarityMeasure.class,false));
		descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
		
		ExternalResourceFactory.bindResource(descr,
				simName, CosineTokenSimilarity.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,simName,
				CosineTokenSimilarity.PARAM_NAME_STOPWORDS_OBJECT, Stopwords.STOPWORD_EN,
				CosineTokenSimilarity.PARAM_NAME_REMOVE_STOPWORDS, false);
		
		
//		simName = "TreeKernelSimilarity";
//		dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, SimilarityMeasure.class,false));
//		descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
//		
//		ExternalResourceFactory.bindResource(descr,
//				simName, TreeKernelSimilarity.class,"",
//				ExternalResourceFactory.PARAM_RESOURCE_NAME,simName,
//				TreeKernelSimilarity.PARAM_NAME_TREE_TYPE, TreeKernelSimilarity.TREE_TYPE.POS_CHUNK_TREE,
//				TreeKernelSimilarity.PARAM_NAME_TREE_KERNEL, TreeKernelSimilarity.TREE_KERNEL_FUNCTION.PTK,
//				TreeKernelSimilarity.PARAM_NAME_NORMALIZED, true,
//				TreeKernelSimilarity.PARAM_NAME_LAMBDA, 1.0f);
		
		return dependencyList;
	}
	
	private static String generateSimsAEDescriptor(String root_folder) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException {
		new File(root_folder+"/descriptors/feature/similarity").mkdirs();
		AnalysisEngineDescription similarityAnnotatorAE_Descriptor = AnalysisEngineFactory.createEngineDescription(
				VectorialFeaturesAnnotator.class,
				VectorialFeaturesAnnotator.PARAM_NAME_OUT_VECTOR_NAME,"qq-sims");
		
		List<ExternalResourceDependency> dependencyList = bindSimilarities(similarityAnnotatorAE_Descriptor);
		String resouceNames[] = new String[dependencyList.size()];
		for (int i=0;i<resouceNames.length;i++) {
			resouceNames[i] = dependencyList.get(i).getKey();
			System.out.println(resouceNames[i]);
		}
		
		ConfigurationParameterFactory.addConfigurationParameter(similarityAnnotatorAE_Descriptor, 
				VectorialFeaturesAnnotator.PARAM_NAME_SIMILARITIES, resouceNames);
		
		similarityAnnotatorAE_Descriptor.toXML(
				new FileOutputStream(root_folder+"/descriptors/feature/similarity/SimilarityAnnotatorAE_Descriptor.xml"));
		
		return "descriptors.feature.similarity.SimilarityAnnotatorAE_Descriptor";
	}
	
	private static String generateRankAEDescriptor(String root_folder) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException {
		new File(root_folder+"/descriptors/feature").mkdirs();
		AnalysisEngineDescription rankAnnotatorAE_Descriptor = AnalysisEngineFactory.createEngineDescription(
				VectorialFeaturesAnnotator.class,
				VectorialFeaturesAnnotator.PARAM_NAME_OUT_VECTOR_NAME,"rank");
		
		List<ExternalResourceDependency> dependencyList = new LinkedList<>();
		String simName = "RankFeanture";
		dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, Feature.class,false));
		rankAnnotatorAE_Descriptor.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
		
		ExternalResourceFactory.bindResource(rankAnnotatorAE_Descriptor,
				simName, RankFeature.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,simName);
		
		
		String resouceNames[] = new String[dependencyList.size()];
		for (int i=0;i<resouceNames.length;i++) {
			resouceNames[i] = dependencyList.get(i).getKey();
			System.out.println(resouceNames[i]);
		}
		
		ConfigurationParameterFactory.addConfigurationParameter(rankAnnotatorAE_Descriptor, 
				VectorialFeaturesAnnotator.PARAM_NAME_SIMILARITIES, resouceNames);
		
		rankAnnotatorAE_Descriptor.toXML(
				new FileOutputStream(root_folder+"/descriptors/feature/RankFeantureAnnotatorAE_Descriptor.xml"));
		
		return "descriptors.feature.RankFeantureAnnotatorAE_Descriptor";
	}
	
	private static String generateFeatureComputerAAEDescriptor(String root_folder,boolean sims,boolean rank) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException {
		new File(root_folder+"/descriptors/feature").mkdirs();
		//Generates the AE descriptor for StandardPreprocessingAnnotator
		System.out.println("Generating XML description for FeatureComputer primitives");
		
		System.out.println("	Generating XML description for SimilarityAnnotatorAE_Descriptor");
		String simsDescr = generateSimsAEDescriptor(root_folder);
		String rankDescr = generateRankAEDescriptor(root_folder);

		
		System.out.println("Generating XML description for FeatureComputerAAE_Descriptor");
		//Generates a AAE descriptor with only the MyAnnotator
		AnalysisEngineDescription standardPreprocessingAnnotatorAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		standardPreprocessingAnnotatorAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		standardPreprocessingAnnotatorAAE.setPrimitive(false);
		
		standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(true);
		standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(false);
		
		List<String> flowNames = new ArrayList<String>();

		if (sims) {
			Import simsAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
			simsAEImport.setName(simsDescr);
			standardPreprocessingAnnotatorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("SimilarityAnnotatorAE", simsAEImport);
			flowNames.add("SimilarityAnnotatorAE");
		}
		
		if (rank) {
			Import rankAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
			rankAEImport.setName(rankDescr);
			standardPreprocessingAnnotatorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("RankAnnotatorAE", rankAEImport);
			flowNames.add("RankAnnotatorAE");
		}
		
		FixedFlow fixedFlow = new FixedFlow_impl();
	    fixedFlow.setFixedFlow(flowNames.toArray(new String[flowNames.size()]));
	    standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow);
	    
		standardPreprocessingAnnotatorAAE.toXML(
				new FileOutputStream(new File(root_folder+"/descriptors/feature/FeatureComputerAAE_Descriptor.xml").getAbsolutePath()));
		
		return "descriptors.feature.FeatureComputerAAE_Descriptor";
	}
	
	private static String generateFeatureComputerDescriptor(String root_folder,boolean sims,boolean rank) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException {
		new File(root_folder+"/descriptors/feature").mkdirs();
		//Generates the AE descriptor for StandardPreprocessingAnnotator
		
		String aaeDescr = generateFeatureComputerAAEDescriptor(root_folder,sims,rank);

		
		System.out.println("Generating XML description for FeatureComputer_Descriptor");
		//Generates a AAE descriptor with only the MyAnnotator
		AnalysisEngineDescription standardPreprocessingAnnotatorAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		standardPreprocessingAnnotatorAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		standardPreprocessingAnnotatorAAE.setPrimitive(false);
		
		standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(true);
		standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(false);
		
		Import featureComputerAAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		featureComputerAAEImport.setName(aaeDescr);
		standardPreprocessingAnnotatorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("FeatureComputerAAE", featureComputerAAEImport);
		
		List<String> flowNames = new ArrayList<String>();
		flowNames.add("FeatureComputerAAE");
		
		FixedFlow fixedFlow = new FixedFlow_impl();
	    fixedFlow.setFixedFlow(flowNames.toArray(new String[flowNames.size()]));
	    standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow);
	    
		standardPreprocessingAnnotatorAAE.toXML(
				new FileOutputStream(new File(root_folder+"/descriptors/feature/FeatureComputer_Descriptor.xml").getAbsolutePath()));
		
		return "descriptors/feature/FeatureComputer_Descriptor";
	}
	
	private static List<ExternalResourceDependency> bindDecorators(AnalysisEngineDescription descr) throws InvalidXMLException {
		List<ExternalResourceDependency> dependencyList = new LinkedList<>();

		String decoratorName = null;
		
//		CQAPairIdentifierDecorator
		decoratorName = "Tree";
		dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(decoratorName, JCasDecorator.class,false));
		descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
		
		ExternalResourceFactory.bindResource(descr,
				decoratorName, TreePairDecorator.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,decoratorName,
				TreePairDecorator.PARAM_NAME_TREE_TYPE, TreePairDecorator.TreeType.POS_CHUNK_TREE);
		
		decoratorName = "identifier";
		dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(decoratorName, JCasDecorator.class,false));
		descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
		
		ExternalResourceFactory.bindResource(descr,
				decoratorName, CQAPairIdentifierDecorator.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,decoratorName);
		
		return dependencyList;
	}
	
	private static String generateDecorationAEDescriptor(String root_folder) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException {
		new File(root_folder+"/descriptors/representation").mkdirs();
		AnalysisEngineDescription decorationAnnotatorAE_Descriptor = AnalysisEngineFactory.createEngineDescription(
				DecorationAnnotator.class);
		
		List<ExternalResourceDependency> dependencyList = bindDecorators(decorationAnnotatorAE_Descriptor);
		String decoratorNames[] = new String[dependencyList.size()];
		for (int i=0;i<decoratorNames.length;i++) {
			decoratorNames[i] = dependencyList.get(i).getKey();
			System.out.println(decoratorNames[i]);
		}
		
		ConfigurationParameterFactory.addConfigurationParameter(decorationAnnotatorAE_Descriptor, 
				DecorationAnnotator.PARAM_NAME_DECORATORS, decoratorNames);
		
		decorationAnnotatorAE_Descriptor.toXML(
				new FileOutputStream(root_folder+"/descriptors/representation/DecorationAnnotatorAE_Descriptor.xml"));
		
		return "descriptors.representation.DecorationAnnotatorAE_Descriptor";
		
	}
	
	private static String generateDecorationAAEDescriptor(String root_folder,boolean trees) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException {
		new File(root_folder+"/descriptors/representation").mkdirs();
		String aaeDescr = generateDecorationAEDescriptor(root_folder);
		
		System.out.println("Generating XML description for DecorationAnnotatorAAE_Descriptor");
		AnalysisEngineDescription decoratorrAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		decoratorrAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		decoratorrAAE.setPrimitive(false);

		decoratorrAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(true);
		decoratorrAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		decoratorrAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(false);
		
		List<String> flowNames1 = new ArrayList<String>();
		if (trees) {
			Import decoratorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
			decoratorAEImport.setName(aaeDescr);
			decoratorrAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("DecorationAnnotatorAE", decoratorAEImport);
			flowNames1.add("DecorationAnnotatorAE");
		}
		
		FixedFlow fixedFlow1 = new FixedFlow_impl();
	    fixedFlow1.setFixedFlow(flowNames1.toArray(new String[flowNames1.size()]));
	    decoratorrAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow1);
		
	    decoratorrAAE.toXML(
				new FileOutputStream(root_folder+"/descriptors/representation/DecorationAnnotatorAAE_Descriptor.xml"));
	    
		return "descriptors.representation.DecorationAnnotatorAAE_Descriptor";
	}
	
	private static String generateKeLPRepresentationExtractorAAEDescriptor(String root_folder) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException {
		new File(root_folder+"/descriptors/representation").mkdirs();
		System.out.println("Generating XML description for KeLPRepresentationExtractorAE_Descriptor");
		AnalysisEngineDescription representationExtractorAEDescriptor = AnalysisEngineFactory.createEngineDescription(
				RepresentantationExtractor.class);
		
		ExternalResourceFactory.bindResource(representationExtractorAEDescriptor,
				RepresentantationExtractor.SERIALIZER_EXTERNAL_RESOURCE, KeLPSerializer.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,"kelpSerializer");
		representationExtractorAEDescriptor.toXML(
				new FileOutputStream(root_folder+"/descriptors/representation/KeLPRepresentationExtractorAE_Descriptor.xml"));
	
		System.out.println("Generating XML description for KeLPRepresentationExtractorAAE_Descriptor");
		AnalysisEngineDescription representationExtractorAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		representationExtractorAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		representationExtractorAAE.setPrimitive(false);

		representationExtractorAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(false);
		representationExtractorAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		representationExtractorAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
		
		Import representationExtractorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		representationExtractorAEImport.setName("descriptors.representation.KeLPRepresentationExtractorAE_Descriptor");
		representationExtractorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("KeLPRepresentationExtractorAE", representationExtractorAEImport);
		
		List<String> flowNames1 = new ArrayList<String>();
		flowNames1.add("KeLPRepresentationExtractorAE");
		
		FixedFlow fixedFlow1 = new FixedFlow_impl();
	    fixedFlow1.setFixedFlow(flowNames1.toArray(new String[flowNames1.size()]));
	    representationExtractorAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow1);
		
	    representationExtractorAAE.toXML(
				new FileOutputStream(root_folder+"/descriptors/representation/KeLPRepresentationExtractorAAE_Descriptor.xml"));		
		
	    return "descriptors.representation.KeLPRepresentationExtractorAAE_Descriptor";
	}
	
	private static String generateClassificationAnnotatorAEDescriptor(String root_folder,String modelFile) throws InvalidXMLException, ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException {
		new File(root_folder+"/descriptors/classification").mkdirs();
		AnalysisEngineDescription classifiactionAnnotatorAE_Descriptor = AnalysisEngineFactory.createEngineDescription(
				ClassificationAnnotator.class);
		
		System.out.println("Generating XML description for ClassificationAnnotatorAE_Descriptor");
		ExternalResourceFactory.bindResource(classifiactionAnnotatorAE_Descriptor,
				ClassificationAnnotator.PARAM_CLASSIFIER_RESOURCE, KeLPClassifier.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,"kelpSVMClassifer",
				KeLPClassifier.PARAM_NAME_APPLY_REL_TAGS,true,
				KeLPClassifier.PARAM_NAME_POSITIVE_CLASS_LABEL,"Relevant",
				KeLPClassifier.PARAM_NAME_MODEL_FILE,modelFile);

		classifiactionAnnotatorAE_Descriptor.toXML(
				new FileOutputStream(root_folder+"/descriptors/classification/ClassificationAnnotatorAE_Descriptor.xml"));
		
		return "descriptors.classification.ClassificationAnnotatorAE_Descriptor";
	}
	
	private static String generateClassificationAnnotatorAAEDescriptor(String root_folder,String modelFile) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException, URISyntaxException {
		new File(root_folder+"/descriptors/classification").mkdirs();
		
		String aeDescr = generateClassificationAnnotatorAEDescriptor(root_folder,modelFile);
		
		System.out.println("Generating XML description for ClassificationAnnotatorAAE_Descriptor");
		AnalysisEngineDescription classificationAnnotatorAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		classificationAnnotatorAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		classificationAnnotatorAAE.setPrimitive(false);

		classificationAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(true);
		classificationAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		classificationAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(false);
		
		Import classificationAnnotatorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		classificationAnnotatorAEImport.setName(aeDescr);
		classificationAnnotatorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("ClassificationAnnotatorAE", classificationAnnotatorAEImport);
		
		List<String> flowNames1 = new ArrayList<String>();
		flowNames1.add("ClassificationAnnotatorAE");
		
		FixedFlow fixedFlow1 = new FixedFlow_impl();
	    fixedFlow1.setFixedFlow(flowNames1.toArray(new String[flowNames1.size()]));
	    classificationAnnotatorAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow1);
		
	    classificationAnnotatorAAE.toXML(
				new FileOutputStream(root_folder+"/descriptors/classification/ClassificationAnnotatorAAE_Descriptor.xml"));
	    
		return "descriptors.classification.ClassificationAnnotatorAAE_Descriptor";
	}
	
	private static String generateLearningAnnotatorAEDescriptor(String root_folder, boolean useSims, boolean useRank, boolean useTrees) throws InvalidXMLException, ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException {
		new File(root_folder+"/descriptors/learning").mkdirs();
		AnalysisEngineDescription learningAnnotatorAE_Descriptor = AnalysisEngineFactory.createEngineDescription(
				LearningAnnotator.class);
		
		System.out.println("Generating XML description for LearningAnnotatorAE_Descriptor");
		
		String simsRepr = useSims ? "qq-sims" : null;
		String rankRepr = useRank ? "rank" : null;
		String treeRepr = useTrees ? "tree" : null;
		ExternalResourceFactory.bindResource(learningAnnotatorAE_Descriptor,
				LearningAnnotator.PARAM_LEARNER_RESOURCE, KeLPSVMLearner.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,"kelpSVMLearner",
				KeLPSVMLearner.PARAM_NAME_C_SVM_PARAM,1,
				KeLPSVMLearner.PARAM_NAME_APPLY_REL_TAGS,true,
				KeLPSVMLearner.PARAM_NAME_TREE_KERNEL,treeRepr,
				KeLPSVMLearner.PARAM_NAME_SIMS_KERNEL,simsRepr,
				KeLPSVMLearner.PARAM_NAME_RANK_KERNEL,rankRepr,
				KeLPSVMLearner.PARAM_NAME_POSITIVE_CLASS_LABEL,"Relevant");

		learningAnnotatorAE_Descriptor.toXML(
				new FileOutputStream(root_folder+"/descriptors/learning/LearningAnnotatorAE_Descriptor.xml"));
		
		return "descriptors.learning.LearningAnnotatorAE_Descriptor";
	}
	
	private static String generateLearningAnnotatorAAEDescriptor(String root_folder, boolean useSims, boolean useRank, boolean useTrees) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException, URISyntaxException {
		new File(root_folder+"/descriptors/learning").mkdirs();

		String descr = generateLearningAnnotatorAEDescriptor(root_folder, useSims, useRank, useTrees);
		
		System.out.println("Generating XML description for LearningAnnotatorAAE_Descriptor");
		AnalysisEngineDescription learningAnnotatorAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		learningAnnotatorAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		learningAnnotatorAAE.setPrimitive(false);

		learningAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(false);
		learningAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		learningAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
		
		Import learningAnnotatorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		learningAnnotatorAEImport.setName(descr);
		learningAnnotatorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("LearningAnnotatorAE", learningAnnotatorAEImport);
		
		List<String> flowNames1 = new ArrayList<String>();
		flowNames1.add("LearningAnnotatorAE");
		
		FixedFlow fixedFlow1 = new FixedFlow_impl();
	    fixedFlow1.setFixedFlow(flowNames1.toArray(new String[flowNames1.size()]));
	    learningAnnotatorAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow1);
		
	    learningAnnotatorAAE.toXML(
				new FileOutputStream(root_folder+"/descriptors/learning/LearningAnnotatorAAE_Descriptor.xml"));		
		
	    return "descriptors.learning.LearningAnnotatorAAE_Descriptor";
	}
	
	private static String generatePreprocessingPipelineDescriptor(String root_folder) throws ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException, InvalidXMLException {
		new File(root_folder+"/descriptors/preprocessing").mkdirs();
		
		String ijmDescr = DescriptorGenerator.generateInputJCasMultiplierAAEDescriptor(root_folder,true);
		String stDescr = DescriptorGenerator.generateStandardTextAnnotatorDescriptor(root_folder);
		String pjaDescr = DescriptorGenerator.generateProcessedJCasAggregatorAAEDescriptor(root_folder);
		
		//Generates a AAE descriptor for the testing pipeline
		Import flowControllerImport = UIMAFramework.getResourceSpecifierFactory().createImport();
	    flowControllerImport.setName("org.apache.uima.flow.FixedFlowController");
	    FlowControllerDeclaration_impl flowControllerDeclaration = new FlowControllerDeclaration_impl();
		flowControllerDeclaration.setImport(flowControllerImport);
		flowControllerDeclaration.setKey("FixedFlowController");
		
		AnalysisEngineDescription pipelineAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		pipelineAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		pipelineAAE.setPrimitive(false);

		pipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(true);
		pipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		pipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
		
		pipelineAAE.setFlowControllerDeclaration(flowControllerDeclaration);
		ConfigurationParameterDeclarations parameters = pipelineAAE.getAnalysisEngineMetaData().getConfigurationParameterDeclarations();
		ConfigurationParameter_impl param = new ConfigurationParameter_impl();
		param.setName(FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER);
		param.setType("String");
		param.addOverride("FixedFlowController/"+FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER);
		parameters.addConfigurationParameter(param);
		ConfigurationParameterSettings settings = pipelineAAE.getAnalysisEngineMetaData().getConfigurationParameterSettings();
		settings.setParameterValue(FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER, "drop");
		
		
		Import inputJCasMultiplierAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		inputJCasMultiplierAEImport.setName(ijmDescr);
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("InputJCasMultiplierAAE", inputJCasMultiplierAEImport);
		
		Import standardTextAnnotatorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		standardTextAnnotatorAEImport.setName(stDescr);
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("StandardTextAnnotator", standardTextAnnotatorAEImport);
		
		Import processedJCasMultiplierAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		processedJCasMultiplierAEImport.setName(pjaDescr);
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("ProcessedJCASAggregatorAAE", processedJCasMultiplierAEImport);
		
		List<String> flowNames = new ArrayList<String>();
		flowNames.add("InputJCasMultiplierAAE");
		flowNames.add("StandardTextAnnotator");
		flowNames.add("ProcessedJCASAggregatorAAE");
		
		FixedFlow fixedFlow = new FixedFlow_impl();
	    fixedFlow.setFixedFlow(flowNames.toArray(new String[flowNames.size()]));
	    pipelineAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow);
  
	    pipelineAAE.toXML(
				new FileOutputStream(new File(root_folder+"/descriptors/preprocessing/PreprocessingPipelineAAE_Descriptor.xml").getAbsolutePath()));
	    
	    return "descriptors.preprocessing.PreprocessingPipelineAAE_Descriptor";
	}
	
	public static String generatePreprocessingPipelineDeploymentDescritor(String queueName, int scaleout) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException, JDOMException {
		try {
			String rootFolder = new File(System.getProperty("user.dir")).getAbsolutePath();
			new File(rootFolder+"/descriptors").mkdir();
			
			String aaeDescr = generatePreprocessingPipelineDescriptor(rootFolder);
			
			System.out.println("Generating XML description for PreprocessingPipelineAAE_DeploymentDescriptor");
			ServiceContext pipelineContext = new ServiceContextImpl("PreprocessingPipeline", 
								           "PreprocessingPipeline",
								           aaeDescr, 
								           queueName, "tcp://localhost:61616");
			pipelineContext.setCasPoolSize(10);
			
			
			ColocatedDelegateConfiguration delegate11 = new ColocatedDelegateConfigurationImpl("InputJCasMultiplierAE", new DelegateConfiguration[0]);
			delegate11.setCasMultiplier(true);
			delegate11.setCasPoolSize(10);
			ColocatedDelegateConfiguration spCldd1 = new ColocatedDelegateConfigurationImpl("InputJCasMultiplierAAE", new DelegateConfiguration[]{delegate11});


			ColocatedDelegateConfiguration delegate21 = new ColocatedDelegateConfigurationImpl("StandardTextAnnotatorAAE", new DelegateConfiguration[0], new ErrorHandlingSettings[0]);
			ColocatedDelegateConfiguration spCldd2 = new ColocatedDelegateConfigurationImpl("StandardTextAnnotator", new DelegateConfiguration[]{
					delegate21});
			
			ColocatedDelegateConfiguration delegate31 = new ColocatedDelegateConfigurationImpl("ProcessedJCASAggregatorAE", new DelegateConfiguration[0]);
			delegate31.setCasMultiplier(true);
			delegate31.setCasPoolSize(10);
			ColocatedDelegateConfiguration spCldd3 = new ColocatedDelegateConfigurationImpl("ProcessedJCASAggregatorAAE", new DelegateConfiguration[]{delegate31});
			
			UimaASAggregateDeploymentDescriptor spdd = DeploymentDescriptorFactory.createAggregateDeploymentDescriptor(
					pipelineContext,spCldd1,spCldd2,spCldd3);
			
			BufferedWriter out = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream(rootFolder+"/descriptors"+"/PreprocessingPipelineAAE_DeploymentDescriptor.xml")));
			out.write(spdd.toXML());
			out.close();
			
			Document descriptor = loadDescriptor(rootFolder+"/descriptors"+"/PreprocessingPipelineAAE_DeploymentDescriptor.xml");
			addScaleoutElementFirstLevel(descriptor, "StandardTextAnnotator",scaleout);
			saveDescriptor(descriptor, rootFolder+"/descriptors"+"/PreprocessingPipelineAAE_DeploymentDescriptor.xml");
			
			return rootFolder+"/descriptors"+"/PreprocessingPipelineAAE_DeploymentDescriptor.xml";
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	private static String generateFeatureExtractionAAEDescriptor(String root_folder,boolean sims,boolean rank,boolean trees) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException {
		new File(root_folder+"/descriptors/feature").mkdirs();
		
		String ijmDescr = generateInputJCasMultiplierAAEDescriptor(root_folder,true);
		String stDescr = generateStandardTextAnnotatorDescriptor(root_folder);
		String pjaDescr = generateProcessedJCasAggregatorAAEDescriptor(root_folder);
		String jpgDescr = generateJCasPairGeneratorAAEDescriptor(root_folder);
		String fcDescr = generateFeatureComputerDescriptor(root_folder,sims,rank);
		String dDescr = generateDecorationAAEDescriptor(root_folder,trees);
		String krDescr = generateKeLPRepresentationExtractorAAEDescriptor(root_folder);
		
		
		//Generates a AAE descriptor for the testing pipeline
		Import flowControllerImport = UIMAFramework.getResourceSpecifierFactory().createImport();
	    flowControllerImport.setName("org.apache.uima.flow.FixedFlowController");
	    FlowControllerDeclaration_impl flowControllerDeclaration = new FlowControllerDeclaration_impl();
		flowControllerDeclaration.setImport(flowControllerImport);
		flowControllerDeclaration.setKey("FixedFlowController");
		
		AnalysisEngineDescription pipelineAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		pipelineAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		pipelineAAE.setPrimitive(false);

		pipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(true);
		pipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		pipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
		
		
		pipelineAAE.setFlowControllerDeclaration(flowControllerDeclaration);
		ConfigurationParameterDeclarations parameters = pipelineAAE.getAnalysisEngineMetaData().getConfigurationParameterDeclarations();
		ConfigurationParameter_impl param = new ConfigurationParameter_impl();
		param.setName(FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER);
		param.setType("String");
		param.addOverride("FixedFlowController/"+FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER);
		parameters.addConfigurationParameter(param);
		ConfigurationParameterSettings settings = pipelineAAE.getAnalysisEngineMetaData().getConfigurationParameterSettings();
		settings.setParameterValue(FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER, "drop");
		
		
		Import inputJCasMultiplierAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		inputJCasMultiplierAEImport.setName(ijmDescr);
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("InputJCasMultiplierAAE", inputJCasMultiplierAEImport);
		
		Import standardTextAnnotatorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		standardTextAnnotatorAEImport.setName(stDescr);
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("StandardTextAnnotator", standardTextAnnotatorAEImport);
		
		Import processedJCasMultiplierAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		processedJCasMultiplierAEImport.setName(pjaDescr);
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("ProcessedJCASAggregatorAAE", processedJCasMultiplierAEImport);
		
		Import jcasPairGeneratorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		jcasPairGeneratorAEImport.setName(jpgDescr);
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("JCasPairGeneratorAAE", jcasPairGeneratorAEImport);
		
		Import featureComputerAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		featureComputerAEImport.setName(fcDescr);
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("FeatureComputer", featureComputerAEImport);
		
		Import decorationAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		decorationAEImport.setName(dDescr);
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("DecorationAnnotatorAAE", decorationAEImport);
		
		Import representationExtractionAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		representationExtractionAEImport.setName(krDescr);
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("KeLPRepresentationExtractorAAE", representationExtractionAEImport);
		
		List<String> flowNames = new ArrayList<String>();
		flowNames.add("InputJCasMultiplierAAE");
		flowNames.add("StandardTextAnnotator");
		flowNames.add("ProcessedJCASAggregatorAAE");
		flowNames.add("JCasPairGeneratorAAE");
		flowNames.add("FeatureComputer");
		flowNames.add("DecorationAnnotatorAAE");
		flowNames.add("KeLPRepresentationExtractorAAE");
		
		FixedFlow fixedFlow = new FixedFlow_impl();
	    fixedFlow.setFixedFlow(flowNames.toArray(new String[flowNames.size()]));
	    pipelineAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow);
  
	    pipelineAAE.toXML(
				new FileOutputStream(new File(root_folder+"/descriptors/feature/FeatureExtractionAAE_Descriptor.xml").getAbsolutePath()));
	    
	    return "descriptors.feature.FeatureExtractionAAE_Descriptor";
	    
	}
	
	private static String generateFeatureExtractionPipelineAAEDescriptor(String root_folder,boolean sims,boolean rank,boolean trees) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException {
		new File(root_folder+"/descriptors/feature").mkdirs();
		
		String descr = generateFeatureExtractionAAEDescriptor(root_folder,sims,rank,trees);
		
		
		//Generates a AAE descriptor for the testing pipeline
		Import flowControllerImport = UIMAFramework.getResourceSpecifierFactory().createImport();
	    flowControllerImport.setName("org.apache.uima.flow.FixedFlowController");
	    FlowControllerDeclaration_impl flowControllerDeclaration = new FlowControllerDeclaration_impl();
		flowControllerDeclaration.setImport(flowControllerImport);
		flowControllerDeclaration.setKey("FixedFlowController");
		
		AnalysisEngineDescription pipelineAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		pipelineAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		pipelineAAE.setPrimitive(false);

		pipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(true);
		pipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		pipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
		
		
		pipelineAAE.setFlowControllerDeclaration(flowControllerDeclaration);
		ConfigurationParameterDeclarations parameters = pipelineAAE.getAnalysisEngineMetaData().getConfigurationParameterDeclarations();
		ConfigurationParameter_impl param = new ConfigurationParameter_impl();
		param.setName(FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER);
		param.setType("String");
		param.addOverride("FixedFlowController/"+FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER);
		parameters.addConfigurationParameter(param);
		ConfigurationParameterSettings settings = pipelineAAE.getAnalysisEngineMetaData().getConfigurationParameterSettings();
		settings.setParameterValue(FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER, "drop");
		
		
		Import aaeImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		aaeImport.setName(descr);
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("FeatureExtractionPipelineAAE", aaeImport);
		
		List<String> flowNames = new ArrayList<String>();
		flowNames.add("FeatureExtractionPipelineAAE");
		
		FixedFlow fixedFlow = new FixedFlow_impl();
	    fixedFlow.setFixedFlow(flowNames.toArray(new String[flowNames.size()]));
	    pipelineAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow);
  
	    pipelineAAE.toXML(
				new FileOutputStream(new File(root_folder+"/descriptors/feature/FeatureExtractionPipelineAAE_Descriptor.xml").getAbsolutePath()));
	    
	    return "descriptors.feature.FeatureExtractionPipelineAAE_Descriptor";
	    
	}
	
	private static String generateFeatureExtractionPipelineDescriptor(String root_folder,boolean sims,boolean rank,boolean trees) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException {
		new File(root_folder+"/descriptors/feature").mkdirs();
		
		String descr = generateFeatureExtractionPipelineAAEDescriptor(root_folder,sims,rank,trees);
		
		
		//Generates a AAE descriptor for the testing pipeline
		Import flowControllerImport = UIMAFramework.getResourceSpecifierFactory().createImport();
	    flowControllerImport.setName("org.apache.uima.flow.FixedFlowController");
	    FlowControllerDeclaration_impl flowControllerDeclaration = new FlowControllerDeclaration_impl();
		flowControllerDeclaration.setImport(flowControllerImport);
		flowControllerDeclaration.setKey("FixedFlowController");
		
		AnalysisEngineDescription pipelineAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		pipelineAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		pipelineAAE.setPrimitive(false);

		pipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(true);
		pipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		pipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
		
		
		pipelineAAE.setFlowControllerDeclaration(flowControllerDeclaration);
		ConfigurationParameterDeclarations parameters = pipelineAAE.getAnalysisEngineMetaData().getConfigurationParameterDeclarations();
		ConfigurationParameter_impl param = new ConfigurationParameter_impl();
		param.setName(FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER);
		param.setType("String");
		param.addOverride("FixedFlowController/"+FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER);
		parameters.addConfigurationParameter(param);
		ConfigurationParameterSettings settings = pipelineAAE.getAnalysisEngineMetaData().getConfigurationParameterSettings();
		settings.setParameterValue(FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER, "drop");
		
		
		Import aaeImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		aaeImport.setName(descr);
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("FeatureExtractionPipeline", aaeImport);
		
		List<String> flowNames = new ArrayList<String>();
		flowNames.add("FeatureExtractionPipeline");
		
		FixedFlow fixedFlow = new FixedFlow_impl();
	    fixedFlow.setFixedFlow(flowNames.toArray(new String[flowNames.size()]));
	    pipelineAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow);
  
	    pipelineAAE.toXML(
				new FileOutputStream(new File(root_folder+"/descriptors/feature/FeatureExtractionPipeline_Descriptor.xml").getAbsolutePath()));
	    
	    return "descriptors.feature.FeatureExtractionPipeline_Descriptor";
	    
	}
	
	public static String generateFeatureExtractionPipelineDeploymentDescriptor(String brokerURL,String queueName,int scaleout,
			boolean sims,boolean rank,boolean trees) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException {
		try {
			String rootFolder = new File(System.getProperty("user.dir")).getAbsolutePath();
			new File(rootFolder+"/descriptors").mkdir();
			
			String aaeDescr = generateFeatureExtractionPipelineDescriptor(rootFolder,sims,rank,trees);
			
			System.out.println("Generating XML description for FeatureExtractionPipelineAAE_DeploymentDescriptor");
			ServiceContext pipelineContext = new ServiceContextImpl("FeatureExtraction", 
								           "FeatureExtractionPipelineAAE_Descriptor",
								           aaeDescr, 
								           queueName, brokerURL);
			pipelineContext.setCasPoolSize(scaleout);
			

			ColocatedDelegateConfiguration delegate11 = new ColocatedDelegateConfigurationImpl("FeatureExtractionPipelineAAE", new DelegateConfiguration[0]);
			delegate11.setCasMultiplier(true);
			delegate11.setCasPoolSize(scaleout);
			ColocatedDelegateConfiguration spCldd1 = new ColocatedDelegateConfigurationImpl("FeatureExtractionPipeline", new DelegateConfiguration[]{delegate11});
			
//			ColocatedDelegateConfiguration delegate11 = new ColocatedDelegateConfigurationImpl("InputJCasMultiplierAE", new DelegateConfiguration[0]);
//			delegate11.setCasMultiplier(true);
//			delegate11.setCasPoolSize(10);
//			ColocatedDelegateConfiguration spCldd1 = new ColocatedDelegateConfigurationImpl("InputJCasMultiplierAAE", new DelegateConfiguration[]{delegate11});
//			
//			ColocatedDelegateConfiguration delegate21 = new ColocatedDelegateConfigurationImpl("StandardTextAnnotatorAAE", new DelegateConfiguration[0], new ErrorHandlingSettings[0]);
//			ColocatedDelegateConfiguration spCldd2 = new ColocatedDelegateConfigurationImpl("StandardTextAnnotator", new DelegateConfiguration[]{
//					delegate21});
//			
//			ColocatedDelegateConfiguration delegate31 = new ColocatedDelegateConfigurationImpl("ProcessedJCASAggregatorAE", new DelegateConfiguration[0]);
//			delegate31.setCasMultiplier(true);
//			delegate31.setCasPoolSize(10);
//			ColocatedDelegateConfiguration spCldd3 = new ColocatedDelegateConfigurationImpl("ProcessedJCASAggregatorAAE", new DelegateConfiguration[]{delegate31});
//			
//			ColocatedDelegateConfiguration delegate41 = new ColocatedDelegateConfigurationImpl("JCasPairGeneratorAE", new DelegateConfiguration[0]);
//			delegate41.setCasMultiplier(true);
//			delegate41.setCasPoolSize(10);
//			ColocatedDelegateConfiguration spCldd4 = new ColocatedDelegateConfigurationImpl("JCasPairGeneratorAAE", new DelegateConfiguration[]{delegate41});
//			
//			ColocatedDelegateConfiguration delegate51 = new ColocatedDelegateConfigurationImpl("FeatureComputerAAE", new DelegateConfiguration[0], new ErrorHandlingSettings[0]);
//			ColocatedDelegateConfiguration spCldd5 = new ColocatedDelegateConfigurationImpl("FeatureComputer", new DelegateConfiguration[]{delegate51});
//			
//			ColocatedDelegateConfiguration delegate61 = new ColocatedDelegateConfigurationImpl("DecorationAnnotatorAE", new DelegateConfiguration[0], new ErrorHandlingSettings[0]);
//			ColocatedDelegateConfiguration spCldd6 = new ColocatedDelegateConfigurationImpl("DecorationAnnotatorAAE", new DelegateConfiguration[]{delegate61});
//
//			ColocatedDelegateConfiguration delegate71 = new ColocatedDelegateConfigurationImpl("KeLPRepresentationExtractorAE", new DelegateConfiguration[0]);
//			delegate71.setCasMultiplier(true);
//			delegate71.setCasPoolSize(10);
//			ColocatedDelegateConfiguration spCldd7 = new ColocatedDelegateConfigurationImpl("KeLPRepresentationExtractorAAE", new DelegateConfiguration[]{delegate71});
			
			
			UimaASAggregateDeploymentDescriptor spdd = DeploymentDescriptorFactory.createAggregateDeploymentDescriptor(
					pipelineContext,spCldd1);
			
			BufferedWriter out = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream(rootFolder+"/descriptors/FeatureExtractionPipelineAAE_DeploymentDescriptor.xml")));
			out.write(spdd.toXML());
			out.close();
			
			
			Document descriptor = loadDescriptor(rootFolder+"/descriptors/FeatureExtractionPipelineAAE_DeploymentDescriptor.xml");
			addScaleoutElementFirstLevel(descriptor, "FeatureExtractionPipeline",scaleout);
			saveDescriptor(descriptor, rootFolder+"/descriptors/FeatureExtractionPipelineAAE_DeploymentDescriptor.xml");
			
			return rootFolder+"/descriptors"+"/FeatureExtractionPipelineAAE_DeploymentDescriptor.xml";
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private static String generateClassificationPipelineDescriptor(String root_folder,String modelFile) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException, URISyntaxException {
		new File(root_folder+"/descriptors/classification").mkdirs();
		
		String feDescr = generateFeatureExtractionPipelineDescriptor(root_folder,true,true,true);
		String clDescr = generateClassificationAnnotatorAAEDescriptor(root_folder,modelFile);
		
		//Generates a AAE descriptor for the testing pipeline
		Import flowControllerImport = UIMAFramework.getResourceSpecifierFactory().createImport();
	    flowControllerImport.setName("org.apache.uima.flow.FixedFlowController");
	    FlowControllerDeclaration_impl flowControllerDeclaration = new FlowControllerDeclaration_impl();
		flowControllerDeclaration.setImport(flowControllerImport);
		flowControllerDeclaration.setKey("FixedFlowController");
		
		AnalysisEngineDescription pipelineAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		pipelineAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		pipelineAAE.setPrimitive(false);

		pipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(true);
		pipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		pipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
		
		pipelineAAE.setFlowControllerDeclaration(flowControllerDeclaration);
		ConfigurationParameterDeclarations parameters = pipelineAAE.getAnalysisEngineMetaData().getConfigurationParameterDeclarations();
		ConfigurationParameter_impl param = new ConfigurationParameter_impl();
		param.setName(FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER);
		param.setType("String");
		param.addOverride("FixedFlowController/"+FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER);
		parameters.addConfigurationParameter(param);
		ConfigurationParameterSettings settings = pipelineAAE.getAnalysisEngineMetaData().getConfigurationParameterSettings();
		settings.setParameterValue(FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER, "drop");
		
		
		Import featureExtractionAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		featureExtractionAEImport.setName(feDescr);
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("FeatureExtractor", featureExtractionAEImport);
		
		Import classificationAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		classificationAEImport.setName(clDescr);
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("ClassificationAnnotatorAAE", classificationAEImport);
		
		List<String> flowNames = new ArrayList<String>();
		flowNames.add("FeatureExtractor");
		flowNames.add("ClassificationAnnotatorAAE");

		FixedFlow fixedFlow = new FixedFlow_impl();
	    fixedFlow.setFixedFlow(flowNames.toArray(new String[flowNames.size()]));
	    pipelineAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow);
  
	    pipelineAAE.toXML(
				new FileOutputStream(new File(root_folder+"/descriptors/classification/ClassificationPipelineAAE_Descriptor.xml").getAbsolutePath()));	
	    
	    return "descriptors.classification.ClassificationPipelineAAE_Descriptor";
	}
	
	public static String generateClassificationPipelineDeploymentDescriptor(String brokerURL, String queueName,String modelFile,
			String featureExtractionURL,String featureExtractionQueueName) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException, URISyntaxException {
		try {
			String rootFolder = new File(System.getProperty("user.dir")).getAbsolutePath();
			new File(rootFolder+"/descriptors").mkdir();
			
			String aaeDescr = generateClassificationPipelineDescriptor(rootFolder,modelFile);
			
			System.out.println("Generating XML description for ClassificationPipelineAAE_DeploymentDescriptor");
			ServiceContext pipelineContext = new ServiceContextImpl("Classification", 
								           "ClassificationPipelineAAE",
								           aaeDescr, 
								           queueName, brokerURL);
			pipelineContext.setCasPoolSize(10);
			
			RemoteDelegateConfiguration spCldd1 = DeploymentDescriptorFactory.createRemoteDelegateConfiguration(
					"FeatureExtractor",featureExtractionURL,
					featureExtractionQueueName,SerializationStrategy.xmi);
			spCldd1.setCasMultiplier(true);
			spCldd1.setCasPoolSize(100);
			spCldd1.setRemoteReplyQueueScaleout(5);
			spCldd1.setRemote(true);
			
			ColocatedDelegateConfiguration delegate21 = new ColocatedDelegateConfigurationImpl("ClassificationAnnotatorAE", new DelegateConfiguration[0]);
			ColocatedDelegateConfiguration spCldd2 = new ColocatedDelegateConfigurationImpl("ClassificationAnnotatorAAE", new DelegateConfiguration[]{delegate21});
			
			UimaASAggregateDeploymentDescriptor spdd = DeploymentDescriptorFactory.createAggregateDeploymentDescriptor(
					pipelineContext,spCldd1,spCldd2);
			
			BufferedWriter out = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream(rootFolder+"/descriptors/ClassificationPipelineAAE_DeploymentDescriptor.xml")));
			out.write(spdd.toXML());
			out.close();
			
			
//			Document descriptor = loadDescriptor(rootFolder+"/descriptors/ClassificationPipelineAAE_DeploymentDescriptor.xml");
	//		addScaleoutElement(descriptor, "StandardTextAnnotator");
	//		addScaleoutElement(descriptor, "FeatureComputer");
	//		addScaleoutElement(descriptor, "ClassificationAnnotatorAAE");
//			updateRemoteReplyQueueScaleoutAttribute(descriptor,"FeatureExtractor",10);
//			saveDescriptor(descriptor, rootFolder+"/descriptors/ClassificationPipelineAAE_DeploymentDescriptor.xml");
			
			return rootFolder+"/descriptors/ClassificationPipelineAAE_DeploymentDescriptor.xml";
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	private static String generateLearningPipelineDescriptor(String root_folder,boolean sims,boolean rank,boolean trees) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException, URISyntaxException {
		new File(root_folder+"/descriptors/classification").mkdirs();
		
		String feDescr = generateFeatureExtractionPipelineDescriptor(root_folder, sims, rank, trees);
		String lDescr = generateLearningAnnotatorAAEDescriptor(root_folder, sims, rank, trees);
		
		//Generates a AAE descriptor for the testing pipeline
		Import flowControllerImport = UIMAFramework.getResourceSpecifierFactory().createImport();
	    flowControllerImport.setName("org.apache.uima.flow.FixedFlowController");
	    FlowControllerDeclaration_impl flowControllerDeclaration = new FlowControllerDeclaration_impl();
		flowControllerDeclaration.setImport(flowControllerImport);
		flowControllerDeclaration.setKey("FixedFlowController");
		
		AnalysisEngineDescription pipelineAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		pipelineAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		pipelineAAE.setPrimitive(false);

		pipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(true);
		pipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		pipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
		
		pipelineAAE.setFlowControllerDeclaration(flowControllerDeclaration);
		ConfigurationParameterDeclarations parameters = pipelineAAE.getAnalysisEngineMetaData().getConfigurationParameterDeclarations();
		ConfigurationParameter_impl param = new ConfigurationParameter_impl();
		param.setName(FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER);
		param.setType("String");
		param.addOverride("FixedFlowController/"+FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER);
		parameters.addConfigurationParameter(param);
		ConfigurationParameterSettings settings = pipelineAAE.getAnalysisEngineMetaData().getConfigurationParameterSettings();
		settings.setParameterValue(FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER, "drop");
		
		
		Import featureExtractionAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		featureExtractionAEImport.setName(feDescr);
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("FeatureExtractor", featureExtractionAEImport);
		
		Import classificationAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		classificationAEImport.setName(lDescr);
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("LearningAnnotatorAAE", classificationAEImport);
		
		List<String> flowNames = new ArrayList<String>();
		flowNames.add("FeatureExtractor");
		flowNames.add("LearningAnnotatorAAE");

		FixedFlow fixedFlow = new FixedFlow_impl();
	    fixedFlow.setFixedFlow(flowNames.toArray(new String[flowNames.size()]));
	    pipelineAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow);
  
	    pipelineAAE.toXML(
				new FileOutputStream(new File(root_folder+"/descriptors/learning/LearningPipelineAAE_Descriptor.xml").getAbsolutePath()));	
	    
	    return "descriptors.learning.LearningPipelineAAE_Descriptor";
	}
	
	public static String generateLearningPipelineDeploymentDescriptor(String brokerURL, String queueName,
			String featureExtractionURL,String featureExtractionQueueName,
			boolean sims,boolean rank,boolean trees) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException, URISyntaxException {
		try {
			String rootFolder = new File(System.getProperty("user.dir")).getAbsolutePath();
			new File(rootFolder+"/descriptors").mkdir();
			
			String aaeDescr = generateLearningPipelineDescriptor(rootFolder, sims, rank, trees);
			
			System.out.println("Generating XML description for ClassificationPipelineAAE_DeploymentDescriptor");
			ServiceContext pipelineContext = new ServiceContextImpl("Learning", 
								           "LearningPipelineAAE",
								           aaeDescr, 
								           queueName, brokerURL);
			pipelineContext.setCasPoolSize(10);
			
			RemoteDelegateConfiguration spCldd1 = DeploymentDescriptorFactory.createRemoteDelegateConfiguration(
					"FeatureExtractor",featureExtractionURL,
					featureExtractionQueueName,SerializationStrategy.xmi);
			spCldd1.setCasMultiplier(true);
			spCldd1.setCasPoolSize(100);
			spCldd1.setRemoteReplyQueueScaleout(5);
			spCldd1.setRemote(true);
			
			ColocatedDelegateConfiguration delegate21 = new ColocatedDelegateConfigurationImpl("LearningAnnotatorAE", new DelegateConfiguration[0]);
			ColocatedDelegateConfiguration spCldd2 = new ColocatedDelegateConfigurationImpl("LearningAnnotatorAAE", new DelegateConfiguration[]{delegate21});
			
			UimaASAggregateDeploymentDescriptor spdd = DeploymentDescriptorFactory.createAggregateDeploymentDescriptor(
					pipelineContext,spCldd1,spCldd2);
			
			BufferedWriter out = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream(rootFolder+"/descriptors/LearningPipelineAAE_DeploymentDescriptor.xml")));
			out.write(spdd.toXML());
			out.close();
			
			
//			Document descriptor = loadDescriptor(rootFolder+"/descriptors/ClassificationPipelineAAE_DeploymentDescriptor.xml");
	//		addScaleoutElement(descriptor, "StandardTextAnnotator");
	//		addScaleoutElement(descriptor, "FeatureComputer");
	//		addScaleoutElement(descriptor, "ClassificationAnnotatorAAE");
//			updateRemoteReplyQueueScaleoutAttribute(descriptor,"FeatureExtractor",10);
//			saveDescriptor(descriptor, rootFolder+"/descriptors/ClassificationPipelineAAE_DeploymentDescriptor.xml");
			
			return rootFolder+"/descriptors/LearningPipelineAAE_DeploymentDescriptor.xml";
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}

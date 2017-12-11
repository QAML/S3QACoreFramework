/**
 * Copyright 2017 Salvatore Romeo
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.Constants;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.metadata.FixedFlow;
import org.apache.uima.analysis_engine.metadata.impl.FixedFlow_impl;
import org.apache.uima.analysis_engine.metadata.impl.FlowControllerDeclaration_impl;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.flow.impl.FixedFlowController;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.ConfigurationParameterDeclarations;
import org.apache.uima.resource.metadata.ConfigurationParameterSettings;
import org.apache.uima.resource.metadata.Import;
import org.apache.uima.resource.metadata.impl.ConfigurationParameter_impl;
import org.apache.uima.resourceSpecifier.factory.ColocatedDelegateConfiguration;
import org.apache.uima.resourceSpecifier.factory.DelegateConfiguration;
import org.apache.uima.resourceSpecifier.factory.DeploymentDescriptorFactory;
import org.apache.uima.resourceSpecifier.factory.RemoteDelegateConfiguration;
import org.apache.uima.resourceSpecifier.factory.SerializationStrategy;
import org.apache.uima.resourceSpecifier.factory.ServiceContext;
import org.apache.uima.resourceSpecifier.factory.UimaASAggregateDeploymentDescriptor;
import org.apache.uima.resourceSpecifier.factory.impl.ColocatedDelegateConfigurationImpl;
import org.apache.uima.resourceSpecifier.factory.impl.ServiceContextImpl;
import org.apache.uima.util.InvalidXMLException;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpChunker;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import qa.qcri.iyas.data.preprocessing.StandardPreprocessor;
import qa.qcri.iyas.data.reader.InputCollectionDataReader;
import qa.qcri.iyas.data.reader.PlainTextDataReader;
import qa.qcri.iyas.feature.InputJCasMultiplier;
import qa.qcri.iyas.feature.ProcessedInstancesManager;
import qa.qcri.iyas.feature.ProcessedJCASAggregator;

public class Setup {
	
	public static final String ROOT_DESCRIPTORS_FOLDER = "resources/descriptors";
	
	public static void createPrimitiveAnalysisEngine() throws ResourceInitializationException, FileNotFoundException, SAXException, IOException, InvalidXMLException {
		System.out.println("Generating XML description for InputCollectionDataReaderAE_Descriptor");
		CollectionReaderDescription collectionReaderDescritor = CollectionReaderFactory.createReaderDescription(
				InputCollectionDataReader.class);
		ExternalResourceDescription reader = ExternalResourceFactory.createExternalResourceDescription(PlainTextDataReader.class,
				PlainTextDataReader.FILE_PARAM,"/home/shared_files/workspace/Iyas.UIMA.Pipeline/data/dev.txt",
				PlainTextDataReader.TASK_PARAM, PlainTextDataReader.INSTANCE_B_TASK);
		ExternalResourceFactory.bindExternalResource(collectionReaderDescritor, 
				InputCollectionDataReader.INPUT_READER_PARAM, reader);
		collectionReaderDescritor.toXML(new FileOutputStream(ROOT_DESCRIPTORS_FOLDER+"/qa/qcri/iyas/data/readers/InputCollectionDataReaderAE_Descriptor.xml"));

		
		System.out.println("Generating XML description for InputJCasMultiplierAE_Descriptor");
		AnalysisEngineDescription inputJCasMultiplierAEDescriptor = AnalysisEngineFactory.createEngineDescription(
				InputJCasMultiplier.class);
		ExternalResourceFactory.bindResource(inputJCasMultiplierAEDescriptor,
				InputJCasMultiplier.PREPROCESSOR_EXTERNAL_RESOURCE, StandardPreprocessor.class,
				InputJCasMultiplier.CONCATENATE_PARAM,"true");
		inputJCasMultiplierAEDescriptor.toXML(
				new FileOutputStream(ROOT_DESCRIPTORS_FOLDER+"/qa/qcri/iyas/features/InputJCasMultiplierAE_Descriptor.xml"));
	
		
		System.out.println("Generating XML description for ProcessedJCASAggregatorAE_Descriptor");
		AnalysisEngineDescription processedJCASAggregatorAEDescriptor = AnalysisEngineFactory.createEngineDescription(
				ProcessedJCASAggregator.class);
		ExternalResourceFactory.bindResource(processedJCASAggregatorAEDescriptor,
				ProcessedJCASAggregator.PARAM_PROCESSED_INSTANCES_MANAGER_RESOURCE, ProcessedInstancesManager.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,"test");
		processedJCASAggregatorAEDescriptor.toXML(
				new FileOutputStream(ROOT_DESCRIPTORS_FOLDER+"/qa/qcri/iyas/features/ProcessedJCASAggregatorAE_Descriptor.xml"));
		
		System.out.println("Generating XML description for StandardSimpleFeatureExtractor primitives");
		System.out.println("	Generating XML description for SegmenterAE_Descriptor");
		AnalysisEngineDescription segmenter = AnalysisEngineFactory.createEngineDescription(
				StanfordSegmenter.class);
		segmenter.toXML(
				new FileOutputStream(ROOT_DESCRIPTORS_FOLDER+"/qa/qcri/iyas/features/SegmenterAE_Descriptor.xml"));
		
		System.out.println("	Generating XML description for POSTaggerAE_Descriptor");
		AnalysisEngineDescription posTagger = AnalysisEngineFactory.createEngineDescription(
				StanfordPosTagger.class);
		posTagger.toXML(
				new FileOutputStream(ROOT_DESCRIPTORS_FOLDER+"/qa/qcri/iyas/features/POSTaggerAE_Descriptor.xml"));
		
		System.out.println("	Generating XML description for LemmatizerAE_Descriptor");
		AnalysisEngineDescription lemmatizer = AnalysisEngineFactory.createEngineDescription(
				StanfordLemmatizer.class);
		lemmatizer.toXML(
				new FileOutputStream(ROOT_DESCRIPTORS_FOLDER+"/qa/qcri/iyas/features/LemmatizerAE_Descriptor.xml"));
		
		System.out.println("	Generating XML description for ChenkerAE_Descriptor");
		AnalysisEngineDescription chunker = AnalysisEngineFactory.createEngineDescription(
				OpenNlpChunker.class);
		chunker.toXML(
				new FileOutputStream(ROOT_DESCRIPTORS_FOLDER+"/qa/qcri/iyas/features/ChenkerAE_Descriptor.xml"));
		
		System.out.println("	Generating XML description for ParserAE_Descriptor");
		AnalysisEngineDescription parser = AnalysisEngineFactory.createEngineDescription(
				StanfordPosTagger.class);
		parser.toXML(
				new FileOutputStream(ROOT_DESCRIPTORS_FOLDER+"/qa/qcri/iyas/features/ParserAE_Descriptor.xml"));
		
	}
	
	private static void createAggregateAnalysisEngine() throws ResourceInitializationException, FileNotFoundException, SAXException, IOException {
		
		System.out.println("Generating XML description for StandardSimpleFeatureExtractorAAE_Descriptor");		
		AnalysisEngineDescription standardSimpleFeatureExtractorAAEDescriptor = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		standardSimpleFeatureExtractorAAEDescriptor.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		standardSimpleFeatureExtractorAAEDescriptor.setPrimitive(false);
		
		List<String> flowNames4 = new ArrayList<String>();
		
		Import segmenterAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		segmenterAEImport.setName("descriptors.qa.qcri.iyas.features.SegmenterAE_Descriptor");
		standardSimpleFeatureExtractorAAEDescriptor.getDelegateAnalysisEngineSpecifiersWithImports().put("StanfordSegmenter",
				segmenterAEImport);
		flowNames4.add("StanfordSegmenter");
		
		Import posTaggerAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		posTaggerAEImport.setName("descriptors.qa.qcri.iyas.features.POSTaggerAE_Descriptor");
		standardSimpleFeatureExtractorAAEDescriptor.getDelegateAnalysisEngineSpecifiersWithImports().put("StanfordPosTagger",
				posTaggerAEImport);
		flowNames4.add("StanfordPosTagger");

		Import lemmatizerAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		lemmatizerAEImport.setName("descriptors.qa.qcri.iyas.features.LemmatizerAE_Descriptor");
		standardSimpleFeatureExtractorAAEDescriptor.getDelegateAnalysisEngineSpecifiersWithImports().put("StanfordLemmatizer",
				lemmatizerAEImport);
		flowNames4.add("StanfordLemmatizer");

		Import chunkerAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		chunkerAEImport.setName("descriptors.qa.qcri.iyas.features.ChenkerAE_Descriptor");
		standardSimpleFeatureExtractorAAEDescriptor.getDelegateAnalysisEngineSpecifiersWithImports().put("OpenNlpChunker",
				chunkerAEImport);
		flowNames4.add("OpenNlpChunker");

		Import parserAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		parserAEImport.setName("descriptors.qa.qcri.iyas.features.ParserAE_Descriptor");
		standardSimpleFeatureExtractorAAEDescriptor.getDelegateAnalysisEngineSpecifiersWithImports().put("StanfordParser",
				parserAEImport);
		flowNames4.add("StanfordParser");

		FixedFlow fixedFlow4 = new FixedFlow_impl();
	    fixedFlow4.setFixedFlow(flowNames4.toArray(new String[flowNames4.size()]));
	    standardSimpleFeatureExtractorAAEDescriptor.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow4);

	    standardSimpleFeatureExtractorAAEDescriptor.toXML(
				new FileOutputStream(ROOT_DESCRIPTORS_FOLDER+"/qa/qcri/iyas/features/StandardSimpleFeatureExtractorAAE_Descriptor.xml"));
	    
	    
		System.out.println("Generating XML description for InputJCasMultiplierAAE_Descriptor");
		AnalysisEngineDescription inputJCasMultiplierAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		inputJCasMultiplierAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		inputJCasMultiplierAAE.setPrimitive(false);

		inputJCasMultiplierAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(false);
		inputJCasMultiplierAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		inputJCasMultiplierAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
		
		Import inputJCasMultiplierAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		inputJCasMultiplierAEImport.setName("descriptors.qa.qcri.iyas.features.InputJCasMultiplierAE_Descriptor");
		inputJCasMultiplierAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("InputJCasMultiplierAE", inputJCasMultiplierAEImport);
		
		List<String> flowNames1 = new ArrayList<String>();
		flowNames1.add("InputJCasMultiplierAE");
		
		FixedFlow fixedFlow1 = new FixedFlow_impl();
	    fixedFlow1.setFixedFlow(flowNames1.toArray(new String[flowNames1.size()]));
	    inputJCasMultiplierAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow1);
		
	    inputJCasMultiplierAAE.toXML(
				new FileOutputStream("resources/descriptors/qa/qcri/iyas/features/InputJCasMultiplierAAE_Descriptor.xml"));		
		
		

		System.out.println("Generating XML description for ProcessedJCASAggregatorAAE_Descriptor");
		AnalysisEngineDescription processedJCasMultiplierAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		processedJCasMultiplierAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		processedJCasMultiplierAAE.setPrimitive(false);

		processedJCasMultiplierAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(false);
		processedJCasMultiplierAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		processedJCasMultiplierAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
		
		Import processedJCasMultiplierAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		processedJCasMultiplierAEImport.setName("descriptors.qa.qcri.iyas.features.ProcessedJCASAggregatorAE_Descriptor");
		processedJCasMultiplierAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("ProcessedJCASAggregatorAE", processedJCasMultiplierAEImport);
		
		List<String> flowNames2 = new ArrayList<String>();
		flowNames2.add("ProcessedJCASAggregatorAE");
		
		FixedFlow fixedFlow2 = new FixedFlow_impl();
	    fixedFlow2.setFixedFlow(flowNames2.toArray(new String[flowNames2.size()]));
	    processedJCasMultiplierAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow2);
  
	    processedJCasMultiplierAAE.toXML(
				new FileOutputStream("resources/descriptors/qa/qcri/iyas/features/ProcessedJCASAggregatorAAE_Descriptor.xml"));		
		
		
		
	    System.out.println("Generating XML description for StandardPipelineAAE_Descriptor");
	    Import flowControllerImport = UIMAFramework.getResourceSpecifierFactory().createImport();
	    flowControllerImport.setName("org.apache.uima.flow.FixedFlowController");
	    FlowControllerDeclaration_impl flowControllerDeclaration = new FlowControllerDeclaration_impl();
		flowControllerDeclaration.setImport(flowControllerImport);
		flowControllerDeclaration.setKey("FixedFlowController");
		
	    AnalysisEngineDescription standardPipelineAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		standardPipelineAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		standardPipelineAAE.setPrimitive(false);

		standardPipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(true);
		standardPipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		standardPipelineAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
		
		standardPipelineAAE.setFlowControllerDeclaration(flowControllerDeclaration);
//		ConfigurationParameterDeclarations_impl
		ConfigurationParameterDeclarations parameters = standardPipelineAAE.getAnalysisEngineMetaData().getConfigurationParameterDeclarations();
		ConfigurationParameter_impl param = new ConfigurationParameter_impl();
		param.setName(FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER);
		param.setType("String");
		param.addOverride("FixedFlowController/"+FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER);
		parameters.addConfigurationParameter(param);
		ConfigurationParameterSettings settings = standardPipelineAAE.getAnalysisEngineMetaData().getConfigurationParameterSettings();
		settings.setParameterValue(FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER, "drop");
		
		Import inputJCasMultiplierAAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		inputJCasMultiplierAAEImport.setName("descriptors.qa.qcri.iyas.features.InputJCasMultiplierAAE_Descriptor");
		standardPipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("InputJCasMultiplierAAE_Descriptor", inputJCasMultiplierAAEImport);
		
		Import standardSimpleFeatureExtractorAAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		standardSimpleFeatureExtractorAAEImport.setName("descriptors.qa.qcri.iyas.features.StandardSimpleFeatureExtractorAAE_Descriptor");
		standardPipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("StandardSimpleFeatureExtractorAAE_Descriptor", standardSimpleFeatureExtractorAAEImport);
		
		Import processedJCasMultiplierAAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		processedJCasMultiplierAAEImport.setName("descriptors.qa.qcri.iyas.features.ProcessedJCASAggregatorAAE_Descriptor");
		standardPipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("ProcessedJCASAggregatorAAE_Descriptor", processedJCasMultiplierAAEImport);

		List<String> flowNames3 = new ArrayList<String>();
		flowNames3.add("InputJCasMultiplierAAE_Descriptor");
		flowNames3.add("StandardSimpleFeatureExtractorAAE_Descriptor");
		flowNames3.add("ProcessedJCASAggregatorAAE_Descriptor");
		
		FixedFlow fixedFlow3 = new FixedFlow_impl();
	    fixedFlow3.setFixedFlow(flowNames3.toArray(new String[flowNames3.size()]));
	    standardPipelineAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow3);
	    	    
	    standardPipelineAAE.toXML(
				new FileOutputStream("resources/descriptors/qa/qcri/iyas/StandardPipelineAAE_Descriptor.xml"));	
	    
//	    standardPipelineAAE.toXML(System.out);
	
	}
	
	private static void createDeploymentFiles() throws IOException, ResourceInitializationException {
		
		System.out.println("Generating XML description for StandardSimpleFeatureExtractorAAE_DeploymentDescriptor");
		ServiceContext standardSimpleFeaturesContext = new ServiceContextImpl("FeatureExtraction", 
							           "Feature Extraction",
							           "descriptors.qa.qcri.iyas.features.StandardSimpleFeatureExtractorAAE_Descriptor", 
							           "standardSimpleFeatureExtractorQueue", "tcp://localhost:61616");
		
//		ColocatedDelegateConfiguration ssfCldd1 = new ColocatedDelegateConfigurationImpl("de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter-0", new DelegateConfiguration[0]);
//		ColocatedDelegateConfiguration ssfCldd2 = new ColocatedDelegateConfigurationImpl("de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger-1", new DelegateConfiguration[0]);
//		ColocatedDelegateConfiguration ssfCldd3 = new ColocatedDelegateConfigurationImpl("de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer-2", new DelegateConfiguration[0]);
//		ColocatedDelegateConfiguration ssfCldd4 = new ColocatedDelegateConfigurationImpl("de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpChunker-3", new DelegateConfiguration[0]);
//		ColocatedDelegateConfiguration ssfCldd5 = new ColocatedDelegateConfigurationImpl("de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser-4", new DelegateConfiguration[0]);

		ColocatedDelegateConfiguration ssfCldd1 = new ColocatedDelegateConfigurationImpl("StanfordSegmenter", new DelegateConfiguration[0]);
		ColocatedDelegateConfiguration ssfCldd2 = new ColocatedDelegateConfigurationImpl("StanfordPosTagger", new DelegateConfiguration[0]);
		ColocatedDelegateConfiguration ssfCldd3 = new ColocatedDelegateConfigurationImpl("StanfordLemmatizer", new DelegateConfiguration[0]);
		ColocatedDelegateConfiguration ssfCldd4 = new ColocatedDelegateConfigurationImpl("OpenNlpChunker", new DelegateConfiguration[0]);
		ColocatedDelegateConfiguration ssfCldd5 = new ColocatedDelegateConfigurationImpl("StanfordParser", new DelegateConfiguration[0]);

		UimaASAggregateDeploymentDescriptor ssfdd = DeploymentDescriptorFactory.createAggregateDeploymentDescriptor(standardSimpleFeaturesContext,
				ssfCldd1,ssfCldd2,ssfCldd3,ssfCldd4,ssfCldd5);
		
		BufferedWriter out = new BufferedWriter(
				new OutputStreamWriter(
						new FileOutputStream("resources/descriptors/deployment/StandardSimpleFeatureExtractorAAE_DeploymentDescriptor.xml")));
		out.write(ssfdd.toXML());
		out.close();
		

		System.out.println("Generating XML description for StandardPipelineAAE_DeploymentDescriptor");
		ServiceContext standardPipelineContext = new ServiceContextImpl("FeatureExtraction", 
							           "Feature Extraction",
							           "descriptors.qa.qcri.iyas.StandardPipelineAAE_Descriptor", 
							           "myQueueName", "tcp://localhost:61616");
		
		ColocatedDelegateConfiguration spCldd1 = new ColocatedDelegateConfigurationImpl("InputJCasMultiplierAAE_Descriptor", new DelegateConfiguration[0]);//DeploymentDescriptorFactory.createAggregateDelegateConfiguration("InputJCasMultiplierAAE_Descriptor",new DelegateConfiguration[]{});
		spCldd1.setCasMultiplier(true);
		
		RemoteDelegateConfiguration spCldd2 = DeploymentDescriptorFactory.createRemoteDelegateConfiguration(
				"StandardSimpleFeatureExtractorAAE_Descriptor","tcp://localhost:61616",
				"standardSimpleFeatureExtractorQueue",SerializationStrategy.xmi);
//		ColocatedDelegateConfiguration spCldd2 = DeploymentDescriptorFactory.createAggregateDelegateConfiguration("StandardSimpleFeatureExtractorAAE_Descriptor",new DelegateConfiguration[]{});

		ColocatedDelegateConfiguration spCldd3 = DeploymentDescriptorFactory.createAggregateDelegateConfiguration("ProcessedJCASAggregatorAAE_Descriptor",new DelegateConfiguration[]{});
		spCldd3.setCasMultiplier(true);
		
		UimaASAggregateDeploymentDescriptor spdd = DeploymentDescriptorFactory.createAggregateDeploymentDescriptor(standardPipelineContext,spCldd1,spCldd2,spCldd3);
		
		out = new BufferedWriter(
				new OutputStreamWriter(
						new FileOutputStream("resources/descriptors/deployment/StandardPipelineAAE_DeploymentDescriptor.xml")));
		out.write(spdd.toXML());
		out.close();
	}

	public static void main(String[] args) throws ResourceInitializationException, FileNotFoundException, SAXException, IOException, InvalidXMLException {
		
		createPrimitiveAnalysisEngine();
		createAggregateAnalysisEngine();
		createDeploymentFiles();
		
//		AnalysisEngineDescription impl = AnalysisEngineFactory.createEngineDescription();
////		impl.setPrimitive(false);
//		
//		Import imp = UIMAFramework.getResourceSpecifierFactory().createImport();
//		imp.setLocation("qa.qcri.iyas.features.ProcessedJCASAggregatorAE_Descriptor");
//		impl.getDelegateAnalysisEngineSpecifiersWithImports().put("A", imp);
//		
//		impl.toXML(System.out);
		
//		AnalysisEngineDescription inputJCasMultiplierAAEDescriptor = 
//				AnalysisEngineFactory.createEngineDescription(inputJCasMultiplierAEDescriptor);		
//		inputJCasMultiplierAAEDescriptor.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
//		inputJCasMultiplierAAEDescriptor.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(false);
//		inputJCasMultiplierAAEDescriptor.toXML(
//				new FileOutputStream("resources/descriptors/qa/qcri/iyas/features/InputJCasMultiplierAAE_Descriptor.xml"));
//		
//		AnalysisEngineDescription processedJCASAggregatorAAEDescriptor = 
//				AnalysisEngineFactory.createEngineDescription(processedJCASAggregatorAEDescriptor);		
//		processedJCASAggregatorAAEDescriptor.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
//		processedJCASAggregatorAAEDescriptor.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(false);
//		processedJCASAggregatorAAEDescriptor.toXML(
//				new FileOutputStream("resources/descriptors/qa/qcri/iyas/features/ProcessedJCASAggregatorAAE_Descriptor.xml"));
//		
//		
//		AnalysisEngineDescription standardSimpleFeatureExtractorAAEDescriptor = 
//				AnalysisEngineFactory.createEngineDescription(
//						AnalysisEngineFactory.createEngineDescription(StanfordSegmenter.class),
//						AnalysisEngineFactory.createEngineDescription(StanfordPosTagger.class),
//						AnalysisEngineFactory.createEngineDescription(StanfordLemmatizer.class),
//						AnalysisEngineFactory.createEngineDescription(OpenNlpChunker.class),
//						AnalysisEngineFactory.createEngineDescription(StanfordParser.class));
//		standardSimpleFeatureExtractorAAEDescriptor.toXML(
//				new FileOutputStream("resources/descriptors/qa/qcri/iyas/features/StandardSimpleFeatureExtractorAAE_Descriptor.xml"));
//		
//		
//		List<AnalysisEngineDescription> descriptors = new LinkedList<AnalysisEngineDescription>();
//		List<String> names = new LinkedList<String>();
//		
//		descriptors.add(inputJCasMultiplierAAEDescriptor);
//		names.add("InputJCasMultiplierAAE");
//		descriptors.add(standardSimpleFeatureExtractorAAEDescriptor);
//		names.add("StandardSimpleFeatureExtractorAAE");
//		descriptors.add(processedJCASAggregatorAAEDescriptor);
//		names.add("ProcessedJCASAggregatorAAE");
//		
//		FlowControllerDescription flowControllerDescriptor = FlowControllerFactory.createFlowControllerDescription(
//				FixedFlowController.class, FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER,"drop");
//		
//		AnalysisEngineDescription standardPipelineAAEDescriptor = 
//				AnalysisEngineFactory.createEngineDescription(descriptors,names,null,null,flowControllerDescriptor);		
//		
//		standardPipelineAAEDescriptor.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
//		standardPipelineAAEDescriptor.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(true);
//		standardPipelineAAEDescriptor.toXML(
//				new FileOutputStream("resources/descriptors/qa/qcri/iyas/StandardPipelineAAE_Descriptor.xml"));
//		
//		
		
		
		
	    
	}

}

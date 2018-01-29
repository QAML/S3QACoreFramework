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
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.flow.impl.FixedFlowController;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.ConfigurationParameterDeclarations;
import org.apache.uima.resource.metadata.ConfigurationParameterSettings;
import org.apache.uima.resource.metadata.Import;
import org.apache.uima.resource.metadata.impl.ConfigurationParameter_impl;
import org.apache.uima.resourceSpecifier.factory.ColocatedDelegateConfiguration;
import org.apache.uima.resourceSpecifier.factory.DelegateConfiguration;
import org.apache.uima.resourceSpecifier.factory.DeploymentDescriptorFactory;
import org.apache.uima.resourceSpecifier.factory.ErrorHandlingSettings;
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
import org.xml.sax.SAXException;

import qa.qcri.iyas.data.preprocessing.StandardPreprocessor;
import qa.qcri.iyas.feature.InputJCasMultiplier;
import qa.qcri.iyas.feature.MyAnnotator;
import qa.qcri.iyas.feature.ProcessedJCASAggregator;
import qa.qcri.iyas.util.ProcessedInstancesManager;

public class DescriptorGenerator {
//	public static final String ROOT_TEST_DESCRIPTORS_FOLDER = "src/test/resources/descriptors";
	
	public static void generateMyAnnotatorAAEDescriptor(String root_folder) throws InvalidXMLException, ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException {
		new File(root_folder+"/test").mkdirs();
		//Generates the AE descriptor for MyAnnotator
		System.out.println("Generating XML description for MyAnnotatorAE_Descriptor");
		AnalysisEngineDescription myAnnotatorAEDescriptor = AnalysisEngineFactory.createEngineDescription(
				MyAnnotator.class);
		myAnnotatorAEDescriptor.toXML(
				new FileOutputStream(new File(root_folder+"/test/MyAnnotatorAE_Descriptor.xml").getAbsolutePath()));
		
		System.out.println("Generating XML description for MyAnnotatorAAE_Descriptor");
		//Generates a AAE descriptor with only the MyAnnotator
		AnalysisEngineDescription myAnnotatorAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		myAnnotatorAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		myAnnotatorAAE.setPrimitive(false);
		
		myAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(true);
		myAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		myAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(false);
		
		Import myAnnotatorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		myAnnotatorAEImport.setName("descriptors.test.MyAnnotatorAE_Descriptor");
		myAnnotatorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("MyAnnotatorAE", myAnnotatorAEImport);
		
		List<String> flowNames = new ArrayList<String>();
		flowNames.add("MyAnnotatorAE");
		
		FixedFlow fixedFlow = new FixedFlow_impl();
	    fixedFlow.setFixedFlow(flowNames.toArray(new String[flowNames.size()]));
	    myAnnotatorAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow);
	    
		myAnnotatorAAE.toXML(
				new FileOutputStream(new File(root_folder+"/test/MyAnnotatorAAE_Descriptor.xml").getAbsolutePath()));
		
	}
	
	public static void generateInputJCasMultiplierAAEDescriptor(String root_folder) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException {
		new File(root_folder+"/test").mkdirs();
		System.out.println("Generating XML description for InputJCasMultiplierAE_Descriptor");
		AnalysisEngineDescription inputJCasMultiplierAEDescriptor = AnalysisEngineFactory.createEngineDescription(
				InputJCasMultiplier.class);
		ExternalResourceFactory.bindResource(inputJCasMultiplierAEDescriptor,
				InputJCasMultiplier.PREPROCESSOR_EXTERNAL_RESOURCE, StandardPreprocessor.class,
				InputJCasMultiplier.CONCATENATE_PARAM,"true");
		inputJCasMultiplierAEDescriptor.toXML(
				new FileOutputStream(root_folder+"/test/InputJCasMultiplierAE_Descriptor.xml"));
	
		System.out.println("Generating XML description for InputJCasMultiplierAAE_Descriptor");
		AnalysisEngineDescription inputJCasMultiplierAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		inputJCasMultiplierAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		inputJCasMultiplierAAE.setPrimitive(false);

		inputJCasMultiplierAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(false);
		inputJCasMultiplierAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		inputJCasMultiplierAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
		
		Import inputJCasMultiplierAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		inputJCasMultiplierAEImport.setName("descriptors.test.InputJCasMultiplierAE_Descriptor");
		inputJCasMultiplierAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("InputJCasMultiplierAE", inputJCasMultiplierAEImport);
		
		List<String> flowNames1 = new ArrayList<String>();
		flowNames1.add("InputJCasMultiplierAE");
		
		FixedFlow fixedFlow1 = new FixedFlow_impl();
	    fixedFlow1.setFixedFlow(flowNames1.toArray(new String[flowNames1.size()]));
	    inputJCasMultiplierAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow1);
		
	    inputJCasMultiplierAAE.toXML(
				new FileOutputStream(root_folder+"/test/InputJCasMultiplierAAE_Descriptor.xml"));		
		
	}
	
	public static void generateProcessedJCasAggregatorAAEDescriptor(String root_folder) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException {
		new File(root_folder+"/test").mkdirs();
		System.out.println("Generating XML description for ProcessedJCASAggregatorAE_Descriptor");
		AnalysisEngineDescription processedJCASAggregatorAEDescriptor = AnalysisEngineFactory.createEngineDescription(
				ProcessedJCASAggregator.class);
		ExternalResourceFactory.bindResource(processedJCASAggregatorAEDescriptor,
				ProcessedJCASAggregator.PARAM_PROCESSED_INSTANCES_MANAGER_RESOURCE, ProcessedInstancesManager.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,"processedInstancesManager");
		processedJCASAggregatorAEDescriptor.toXML(
				new FileOutputStream(root_folder+"/test/ProcessedJCASAggregatorAE_Descriptor.xml"));
		
		System.out.println("Generating XML description for ProcessedJCASAggregatorAAE_Descriptor");
		AnalysisEngineDescription processedJCasMultiplierAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		processedJCasMultiplierAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		processedJCasMultiplierAAE.setPrimitive(false);

		processedJCasMultiplierAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(false);
		processedJCasMultiplierAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		processedJCasMultiplierAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
		
		Import processedJCasMultiplierAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		processedJCasMultiplierAEImport.setName("descriptors.test.ProcessedJCASAggregatorAE_Descriptor");
		processedJCasMultiplierAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("ProcessedJCASAggregatorAE", processedJCasMultiplierAEImport);
		
		List<String> flowNames2 = new ArrayList<String>();
		flowNames2.add("ProcessedJCASAggregatorAE");
		
		FixedFlow fixedFlow2 = new FixedFlow_impl();
	    fixedFlow2.setFixedFlow(flowNames2.toArray(new String[flowNames2.size()]));
	    processedJCasMultiplierAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow2);
  
	    processedJCasMultiplierAAE.toXML(
				new FileOutputStream(root_folder+"/test/ProcessedJCASAggregatorAAE_Descriptor.xml"));		
		
	}
	
	public static void generatePipelineAAEDescriptor(String root_folder) throws ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException, InvalidXMLException {
		new File(root_folder+"/test").mkdirs();
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
		inputJCasMultiplierAEImport.setName("descriptors.test.InputJCasMultiplierAAE_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("InputJCasMultiplierAAE", inputJCasMultiplierAEImport);
		
		Import myAnnotatorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		myAnnotatorAEImport.setName("descriptors.test.MyAnnotatorAAE_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("MyAnnotatorAAE", myAnnotatorAEImport);
		
		Import processedJCasMultiplierAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		processedJCasMultiplierAEImport.setName("descriptors.test.ProcessedJCASAggregatorAAE_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("ProcessedJCASAggregatorAAE", processedJCasMultiplierAEImport);
		
		List<String> flowNames = new ArrayList<String>();
		flowNames.add("InputJCasMultiplierAAE");
		flowNames.add("MyAnnotatorAAE");
		flowNames.add("ProcessedJCASAggregatorAAE");
		
		FixedFlow fixedFlow = new FixedFlow_impl();
	    fixedFlow.setFixedFlow(flowNames.toArray(new String[flowNames.size()]));
	    pipelineAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow);
  
	    pipelineAAE.toXML(
				new FileOutputStream(new File(root_folder+"/test/PipelineAAE_Descriptor.xml").getAbsolutePath()));	
	    
	}
	
	public static void generatePipelineAAEDeploymentDescriptor(String root_folder) throws InvalidXMLException, ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException, JDOMException {
		new File(root_folder+"/test").mkdirs();
		System.out.println("Generating XML description for PipelineAAE_DeploymentDescriptor");
		ServiceContext pipelineContext = new ServiceContextImpl("FeatureExtraction", 
							           "PipelineAAE_DeploymentDescriptor",
							           "descriptors.test.PipelineAAE_Descriptor", 
							           "myQueueName", "tcp://localhost:61616");
		pipelineContext.setCasPoolSize(10);
		
		
		ColocatedDelegateConfiguration delegate1 = new ColocatedDelegateConfigurationImpl("InputJCasMultiplierAE", new DelegateConfiguration[0]);
		delegate1.setCasMultiplier(true);
		delegate1.setCasPoolSize(10);
		ColocatedDelegateConfiguration spCldd1 = new ColocatedDelegateConfigurationImpl("InputJCasMultiplierAAE", new DelegateConfiguration[]{delegate1});

		ColocatedDelegateConfiguration delegate2 = new ColocatedDelegateConfigurationImpl("MyAnnotatorAE", new DelegateConfiguration[0], new ErrorHandlingSettings[0]);
		ColocatedDelegateConfiguration spCldd2 = new ColocatedDelegateConfigurationImpl("MyAnnotatorAAE", new DelegateConfiguration[]{delegate2});
		
		ColocatedDelegateConfiguration delegate3 = new ColocatedDelegateConfigurationImpl("ProcessedJCASAggregatorAE", new DelegateConfiguration[0]);
		delegate3.setCasMultiplier(true);
		delegate3.setCasPoolSize(10);
		ColocatedDelegateConfiguration spCldd3 = new ColocatedDelegateConfigurationImpl("ProcessedJCASAggregatorAAE", new DelegateConfiguration[]{delegate3});
		
		UimaASAggregateDeploymentDescriptor spdd = DeploymentDescriptorFactory.createAggregateDeploymentDescriptor(
				pipelineContext,spCldd1,spCldd2,spCldd3);
		
		BufferedWriter out = new BufferedWriter(
				new OutputStreamWriter(
						new FileOutputStream(root_folder+"/test/PipelineAAE_DeploymentDescriptor.xml")));
		out.write(spdd.toXML());
		out.close();
		
		
		SAXBuilder builder = new SAXBuilder();
		Document descriptor = builder.build(new File(root_folder+"/test/PipelineAAE_DeploymentDescriptor.xml"));
		Element root = descriptor.getRootElement();
		Element delegatesElement = root.getChild("deployment",root.getNamespace()).getChild("service",root.getNamespace())
				.getChild("analysisEngine",root.getNamespace()).getChild("delegates",root.getNamespace());
		Element myAnnotatorAAEElement = null;
		for (Element delegate : delegatesElement.getChildren()) {
			if (delegate.getAttributeValue("key").equals("MyAnnotatorAAE")) {
				myAnnotatorAAEElement = delegate;
				break;
			}
		}
		Element myAnnotatorAEElement = myAnnotatorAAEElement.getChild("delegates",root.getNamespace())
				.getChild("analysisEngine",root.getNamespace());
		Element scaleout = new Element("scaleout", root.getNamespace());
		scaleout.setAttribute("numberOfInstances", "10");
		myAnnotatorAEElement.addContent(scaleout);
		
		XMLOutputter xmlOut = new XMLOutputter();
		out = new BufferedWriter(
				new OutputStreamWriter(
						new FileOutputStream(root_folder+"/test/PipelineAAE_DeploymentDescriptor.xml")));
		out.write(xmlOut.outputString(descriptor));
		out.close();
	}
}

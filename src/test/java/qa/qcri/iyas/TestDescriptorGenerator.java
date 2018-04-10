/**
 * Copyright 201 Salvatore Romeo
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
import qa.qcri.iyas.feature.MyAnnotator;
import qa.qcri.iyas.feature.PreprocessingPipelineConcatenatedTest;
import qa.qcri.iyas.feature.VectorialFeaturesAnnotator;
import qa.qcri.iyas.feature.similarity.CosineBowSimilarity;
import qa.qcri.iyas.feature.similarity.CosineTokenSimilarity;
import qa.qcri.iyas.feature.similarity.GreedyStringTilingSimilarity;
import qa.qcri.iyas.feature.similarity.LongestCommonSubsequenceComparatorSimilarity;
import qa.qcri.iyas.feature.similarity.LongestCommonSubsequenceNormComparatorSimilarity;
import qa.qcri.iyas.feature.similarity.LongestCommonSubstringComparatorSimilarity;
import qa.qcri.iyas.feature.similarity.ThreadSafeSimilarityMeasure;
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
import qa.qcri.iyas.util.ProcessedInstancesManager;

public class TestDescriptorGenerator {
//	public static final String ROOT_TEST_DESCRIPTORS_FOLDER = "src/test/resources/descriptors";
	
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
	
	private static void addScaleoutElement(Document descriptor,String key) throws JDOMException, IOException {
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
		Element scaleout = new Element("scaleout", root.getNamespace());
		scaleout.setAttribute("numberOfInstances", "10");
		myAnnotatorAEElement.addContent(scaleout);
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
		
		for(int[] interval : lemmaIntervals) {
			simName = "CosineBowSimilarity-Lemmas-NoStopwording-"+interval[0]+"-"+interval[1];
			dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, ThreadSafeSimilarityMeasure.class,false));
			descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
			
			ExternalResourceFactory.bindResource(descr,
					simName, CosineBowSimilarity.class,"",
					ExternalResourceFactory.PARAM_RESOURCE_NAME,simName,
					CosineBowSimilarity.PARAM_NAME_STOPWORDS_OBJECT, Stopwords.STOPWORD_EN,
					CosineBowSimilarity.PARAM_NAME_REMOVE_STOPWORDS, false,
					CosineBowSimilarity.PARAM_NAME_MIN_N_GRAM_SIZE, interval[0],
					CosineBowSimilarity.PARAM_NAME_MAX_N_GRAM_SIZE, interval[1],
					CosineBowSimilarity.PARAM_NAME_REPRESENTATION_TYPE, CosineBowSimilarity.PARAMETER_LIST_LEMMAS);
			
			simName = "CosineBowSimilarity-Lemmas-Stopwording-"+interval[0]+"-"+interval[1];
			dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, ThreadSafeSimilarityMeasure.class,false));
			descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
			
			ExternalResourceFactory.bindResource(descr,
					simName, CosineBowSimilarity.class,"",
					ExternalResourceFactory.PARAM_RESOURCE_NAME,simName,
					CosineBowSimilarity.PARAM_NAME_STOPWORDS_OBJECT, Stopwords.STOPWORD_EN,
					CosineBowSimilarity.PARAM_NAME_REMOVE_STOPWORDS, true,
					CosineBowSimilarity.PARAM_NAME_MIN_N_GRAM_SIZE, interval[0],
					CosineBowSimilarity.PARAM_NAME_MAX_N_GRAM_SIZE, interval[1],
					CosineBowSimilarity.PARAM_NAME_REPRESENTATION_TYPE, CosineBowSimilarity.PARAMETER_LIST_LEMMAS);
		}
		
		
		for(int[] interval : posIntervals) {
			simName = "CosineBowSimilarity-POSTags-NoStopwording-"+interval[0]+"-"+interval[1];
			dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, ThreadSafeSimilarityMeasure.class,false));
			descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
			
			ExternalResourceFactory.bindResource(descr,
					simName, CosineBowSimilarity.class,"",
					ExternalResourceFactory.PARAM_RESOURCE_NAME,simName,
					CosineBowSimilarity.PARAM_NAME_STOPWORDS_OBJECT, Stopwords.STOPWORD_EN,
					CosineBowSimilarity.PARAM_NAME_REMOVE_STOPWORDS, false,
					CosineBowSimilarity.PARAM_NAME_MIN_N_GRAM_SIZE, interval[0],
					CosineBowSimilarity.PARAM_NAME_MAX_N_GRAM_SIZE, interval[1],
					CosineBowSimilarity.PARAM_NAME_REPRESENTATION_TYPE, CosineBowSimilarity.PARAMETER_LIST_POSTAGS);
		}
		
		
		simName = "GreedyStringTilingSimilarity";
		dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, ThreadSafeSimilarityMeasure.class,false));
		descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
		
		ExternalResourceFactory.bindResource(descr,
				simName, GreedyStringTilingSimilarity.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,simName,
				GreedyStringTilingSimilarity.PARAM_NAME_STOPWORDS_OBJECT, Stopwords.STOPWORD_EN,
				GreedyStringTilingSimilarity.PARAM_NAME_REMOVE_STOPWORDS, false,
				GreedyStringTilingSimilarity.PARAM_NAME_TILE_LENGTH, 3);
		
		simName = "LongestCommonSubsequenceComparatorSimilarity";
		dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, ThreadSafeSimilarityMeasure.class,false));
		descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
		
		ExternalResourceFactory.bindResource(descr,
				simName, LongestCommonSubsequenceComparatorSimilarity.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,simName,
				LongestCommonSubsequenceComparatorSimilarity.PARAM_NAME_STOPWORDS_OBJECT, Stopwords.STOPWORD_EN,
				LongestCommonSubsequenceComparatorSimilarity.PARAM_NAME_REMOVE_STOPWORDS, false);
		
		
		simName = "LongestCommonSubsequenceNormComparatorSimilarity";
		dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, ThreadSafeSimilarityMeasure.class,false));
		descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
		
		ExternalResourceFactory.bindResource(descr,
				simName, LongestCommonSubsequenceNormComparatorSimilarity.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,simName,
				LongestCommonSubsequenceNormComparatorSimilarity.PARAM_NAME_STOPWORDS_OBJECT, Stopwords.STOPWORD_EN,
				LongestCommonSubsequenceNormComparatorSimilarity.PARAM_NAME_REMOVE_STOPWORDS, false);
		
		
		simName = "LongestCommonSubstringComparatorSimilarity";
		dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, ThreadSafeSimilarityMeasure.class,false));
		descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
		
		ExternalResourceFactory.bindResource(descr,
				simName, LongestCommonSubstringComparatorSimilarity.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,simName,
				LongestCommonSubstringComparatorSimilarity.PARAM_NAME_STOPWORDS_OBJECT, Stopwords.STOPWORD_EN,
				LongestCommonSubstringComparatorSimilarity.PARAM_NAME_REMOVE_STOPWORDS, false);
		

		for (int i=1;i<=4;i++) {
			simName = "WordNGramJaccardMeasureSimilarity-"+i;
			dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, ThreadSafeSimilarityMeasure.class,false));
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
			dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, ThreadSafeSimilarityMeasure.class,false));
			descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
			
			ExternalResourceFactory.bindResource(descr,
					simName, WordNGramContainmentMeasureSimilarity.class,"",
					ExternalResourceFactory.PARAM_RESOURCE_NAME,simName,
					WordNGramContainmentMeasureSimilarity.PARAM_NAME_STOPWORDS_OBJECT, Stopwords.STOPWORD_EN,
					WordNGramContainmentMeasureSimilarity.PARAM_NAME_REMOVE_STOPWORDS, false,
					WordNGramContainmentMeasureSimilarity.PARAM_NAME_NGRAM_LENGTH, i);
		}
		

		simName = "CosineTokenSimilarity";
		dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, ThreadSafeSimilarityMeasure.class,false));
		descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
		
		ExternalResourceFactory.bindResource(descr,
				simName, CosineTokenSimilarity.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,simName,
				CosineTokenSimilarity.PARAM_NAME_STOPWORDS_OBJECT, Stopwords.STOPWORD_EN,
				CosineTokenSimilarity.PARAM_NAME_REMOVE_STOPWORDS, false);
		
		
		simName = "TreeKernelSimilarity";
		dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, ThreadSafeSimilarityMeasure.class,false));
		descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
		
		ExternalResourceFactory.bindResource(descr,
				simName, TreeKernelSimilarity.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,simName,
				TreeKernelSimilarity.PARAM_NAME_TREE_TYPE, TreeKernelSimilarity.TREE_TYPE.POS_CHUNK_TREE,
				TreeKernelSimilarity.PARAM_NAME_TREE_KERNEL, TreeKernelSimilarity.TREE_KERNEL_FUNCTION.PTK,
				TreeKernelSimilarity.PARAM_NAME_NORMALIZED, true,
				TreeKernelSimilarity.PARAM_NAME_LAMBDA, 1.0f);
		
//		simName = "TreeKernelSimilarity";
//		dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, SimilarityMeasure.class,false));
//		descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
//		
//		ExternalResourceFactory.bindResource(descr,simName,TreeKernelSimilarity.class,
//				TreeKernelSimilarity.PARAM_NAME_TREE_TYPE, TreeKernelSimilarity.TREE_TYPE.POS_CHUNK_TREE.treeType(),
//				TreeKernelSimilarity.PARAM_NAME_TREE_KERNEL, TreeKernelSimilarity.TREE_KERNEL_FUNCTION.PTK.kernelFunctionName(),
//				TreeKernelSimilarity.PARAM_NAME_NORMALIZED, "true",
//				TreeKernelSimilarity.PARAM_NAME_LAMBDA, "1.0f");
		
		return dependencyList;
	}
	
	public static void generateSimsAEDescriptor(String root_folder) throws InvalidXMLException, ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException {
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
				new FileOutputStream(root_folder+"/test/SimilarityAnnotatorAE_Descriptor.xml"));
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
	
	private static void generateDecorationAEDescriptor(String root_folder) throws InvalidXMLException, ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException {
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
				new FileOutputStream(root_folder+"/test/DecorationAnnotatorAE_Descriptor.xml"));
		
		
	}
	
	private static void generateDecorationAAEDescriptor(String root_folder) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException, URISyntaxException {
		new File(root_folder+"/test").mkdirs();
		generateDecorationAEDescriptor(root_folder);
		
		System.out.println("Generating XML description for DecorationAnnotatorAAE_Descriptor");
		AnalysisEngineDescription decoratorrAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		decoratorrAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		decoratorrAAE.setPrimitive(false);

		decoratorrAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(true);
		decoratorrAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		decoratorrAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(false);
		
		Import decoratorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		decoratorAEImport.setName("descriptors.test.DecorationAnnotatorAE_Descriptor");
		decoratorrAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("DecorationAnnotatorAE", decoratorAEImport);
		
		List<String> flowNames1 = new ArrayList<String>();
		flowNames1.add("DecorationAnnotatorAE");
		
		FixedFlow fixedFlow1 = new FixedFlow_impl();
	    fixedFlow1.setFixedFlow(flowNames1.toArray(new String[flowNames1.size()]));
	    decoratorrAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow1);
		
	    decoratorrAAE.toXML(
				new FileOutputStream(root_folder+"/test/DecorationAnnotatorAAE_Descriptor.xml"));		
		
	}
	
	private static void generateLearningAnnotatorAEDescriptor(String root_folder) throws InvalidXMLException, ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException {
		AnalysisEngineDescription learningAnnotatorAE_Descriptor = AnalysisEngineFactory.createEngineDescription(
				LearningAnnotator.class);
		
		System.out.println("Generating XML description for LearningAnnotatorAE_Descriptor");
		ExternalResourceFactory.bindResource(learningAnnotatorAE_Descriptor,
				LearningAnnotator.PARAM_LEARNER_RESOURCE, KeLPSVMLearner.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,"kelpSVMLearner",
				KeLPSVMLearner.PARAM_NAME_C_SVM_PARAM,1,
				KeLPSVMLearner.PARAM_NAME_APPLY_REL_TAGS,true,
				KeLPSVMLearner.PARAM_NAME_TREE_KERNEL,"tree",
				KeLPSVMLearner.PARAM_NAME_SIMS_KERNEL,"qq-sims",
				KeLPSVMLearner.PARAM_NAME_POSITIVE_CLASS_LABEL,"Relevant");

		learningAnnotatorAE_Descriptor.toXML(
				new FileOutputStream(root_folder+"/test/LearningAnnotatorAE_Descriptor.xml"));
		
	}
	
	private static void generateLearningAnnotatorAAEDescriptor(String root_folder) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException, URISyntaxException {
		new File(root_folder+"/test").mkdirs();
		
		generateLearningAnnotatorAEDescriptor(root_folder);
		
		System.out.println("Generating XML description for LearningAnnotatorAAE_Descriptor");
		AnalysisEngineDescription learningAnnotatorAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		learningAnnotatorAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		learningAnnotatorAAE.setPrimitive(false);

		learningAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(false);
		learningAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		learningAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
		
		Import learningAnnotatorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		learningAnnotatorAEImport.setName("descriptors.test.LearningAnnotatorAE_Descriptor");
		learningAnnotatorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("LearningAnnotatorAE", learningAnnotatorAEImport);
		
		List<String> flowNames1 = new ArrayList<String>();
		flowNames1.add("LearningAnnotatorAE");
		
		FixedFlow fixedFlow1 = new FixedFlow_impl();
	    fixedFlow1.setFixedFlow(flowNames1.toArray(new String[flowNames1.size()]));
	    learningAnnotatorAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow1);
		
	    learningAnnotatorAAE.toXML(
				new FileOutputStream(root_folder+"/test/LearningAnnotatorAAE_Descriptor.xml"));		
		
	}
	
	private static void generateClassificationAnnotatorAEDescriptor(String root_folder) throws InvalidXMLException, ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException {
		AnalysisEngineDescription classifiactionAnnotatorAE_Descriptor = AnalysisEngineFactory.createEngineDescription(
				ClassificationAnnotator.class);
		
		System.out.println("Generating XML description for ClassificationAnnotatorAE_Descriptor");
		ExternalResourceFactory.bindResource(classifiactionAnnotatorAE_Descriptor,
				ClassificationAnnotator.PARAM_CLASSIFIER_RESOURCE, KeLPClassifier.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,"kelpSVMClassifer",
				KeLPClassifier.PARAM_NAME_APPLY_REL_TAGS,true,
				KeLPClassifier.PARAM_NAME_POSITIVE_CLASS_LABEL,"Relevant",
				KeLPClassifier.PARAM_NAME_MODEL_FILE,"1521745849273.mdl");

		classifiactionAnnotatorAE_Descriptor.toXML(
				new FileOutputStream(root_folder+"/test/ClassificationAnnotatorAE_Descriptor.xml"));
		
	}
	
	private static void generateClassificationAnnotatorAAEDescriptor(String root_folder) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException, URISyntaxException {
		new File(root_folder+"/test").mkdirs();
		
		generateClassificationAnnotatorAEDescriptor(root_folder);
		
		System.out.println("Generating XML description for ClassificationAnnotatorAAE_Descriptor");
		AnalysisEngineDescription classificationAnnotatorAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		classificationAnnotatorAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		classificationAnnotatorAAE.setPrimitive(false);

		classificationAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(true);
		classificationAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		classificationAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(false);
		
		Import classificationAnnotatorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		classificationAnnotatorAEImport.setName("descriptors.test.ClassificationAnnotatorAE_Descriptor");
		classificationAnnotatorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("ClassificationAnnotatorAE", classificationAnnotatorAEImport);
		
		List<String> flowNames1 = new ArrayList<String>();
		flowNames1.add("ClassificationAnnotatorAE");
		
		FixedFlow fixedFlow1 = new FixedFlow_impl();
	    fixedFlow1.setFixedFlow(flowNames1.toArray(new String[flowNames1.size()]));
	    classificationAnnotatorAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow1);
		
	    classificationAnnotatorAAE.toXML(
				new FileOutputStream(root_folder+"/test/ClassificationAnnotatorAAE_Descriptor.xml"));		
		
	}
	
	private static void generateFeatureComputerAAEDescriptor(String root_folder) throws InvalidXMLException, ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException {
		new File(root_folder+"/test").mkdirs();
		//Generates the AE descriptor for StandardPreprocessingAnnotator
		System.out.println("Generating XML description for FeatureComputer primitives");
		
		System.out.println("	Generating XML description for SimilarityAnnotatorAE_Descriptor");
		generateSimsAEDescriptor(root_folder);

		
		System.out.println("Generating XML description for FeatureComputerAAE_Descriptor");
		//Generates a AAE descriptor with only the MyAnnotator
		AnalysisEngineDescription standardPreprocessingAnnotatorAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		standardPreprocessingAnnotatorAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		standardPreprocessingAnnotatorAAE.setPrimitive(false);
		
		standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(true);
		standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(false);
		
		Import myAnnotatorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		myAnnotatorAEImport.setName("descriptors.test.SimilarityAnnotatorAE_Descriptor");
		standardPreprocessingAnnotatorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("SimilarityAnnotatorAE", myAnnotatorAEImport);
		
		List<String> flowNames = new ArrayList<String>();
		flowNames.add("SimilarityAnnotatorAE");
		
		FixedFlow fixedFlow = new FixedFlow_impl();
	    fixedFlow.setFixedFlow(flowNames.toArray(new String[flowNames.size()]));
	    standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow);
	    
		standardPreprocessingAnnotatorAAE.toXML(
				new FileOutputStream(new File(root_folder+"/test/FeatureComputerAAE_Descriptor.xml").getAbsolutePath()));
		
	}
	
	private static void generateFeatureComputerDescriptor(String root_folder) throws InvalidXMLException, ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException {
		new File(root_folder+"/test").mkdirs();
		//Generates the AE descriptor for StandardPreprocessingAnnotator
		generateFeatureComputerAAEDescriptor(root_folder);

		
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
		featureComputerAAEImport.setName("descriptors.test.FeatureComputerAAE_Descriptor");
		standardPreprocessingAnnotatorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("FeatureComputerAAE", featureComputerAAEImport);
		
		List<String> flowNames = new ArrayList<String>();
		flowNames.add("FeatureComputerAAE");
		
		FixedFlow fixedFlow = new FixedFlow_impl();
	    fixedFlow.setFixedFlow(flowNames.toArray(new String[flowNames.size()]));
	    standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow);
	    
		standardPreprocessingAnnotatorAAE.toXML(
				new FileOutputStream(new File(root_folder+"/test/FeatureComputer_Descriptor.xml").getAbsolutePath()));
		
	}
	
	public static void generateStandardTextAnnotatorAAEDescriptor(String root_folder) throws InvalidXMLException, ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException {
		new File(root_folder+"/test").mkdirs();
		//Generates the AE descriptor for StandardPreprocessingAnnotator
		System.out.println("Generating XML description for StandardSimpleFeatureExtractor primitives");
		
		System.out.println("	Generating XML description for MyAnnotatorAE_Descriptor");
//		AnalysisEngineDescription myAnnotatorAEDescriptor = AnalysisEngineFactory.createEngineDescription(
//				MyAnnotator.class);
//		myAnnotatorAEDescriptor.toXML(
//				new FileOutputStream(new File(root_folder+"/test/MyAnnotatorAE_Descriptor.xml").getAbsolutePath()));
		
		System.out.println("	Generating XML description for SegmenterAE_Descriptor");
		AnalysisEngineDescription segmenter = AnalysisEngineFactory.createEngineDescription(
				OpenNlpSegmenter.class);
		segmenter.toXML(
				new FileOutputStream(root_folder+"/test/SegmenterAE_Descriptor.xml"));
	
		System.out.println("	Generating XML description for POSTaggerAE_Descriptor");
		AnalysisEngineDescription posTagger = AnalysisEngineFactory.createEngineDescription(
				OpenNlpPosTagger.class);
		posTagger.toXML(
				new FileOutputStream(root_folder+"/test/POSTaggerAE_Descriptor.xml"));

		System.out.println("	Generating XML description for LemmatizerAE_Descriptor");
		AnalysisEngineDescription lemmatizer = AnalysisEngineFactory.createEngineDescription(
				StanfordLemmatizer.class);
		lemmatizer.toXML(
				new FileOutputStream(root_folder+"/test/LemmatizerAE_Descriptor.xml"));

		System.out.println("	Generating XML description for ChunkerAE_Descriptor");
		AnalysisEngineDescription chunker = AnalysisEngineFactory.createEngineDescription(
				OpenNlpChunker.class);
		chunker.toXML(
				new FileOutputStream(root_folder+"/test/ChunkerAE_Descriptor.xml"));

		
		System.out.println("Generating XML description for StandardPreprocessingAnnotatorAAE_Descriptor");
		//Generates a AAE descriptor with only the MyAnnotator
		AnalysisEngineDescription standardPreprocessingAnnotatorAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		standardPreprocessingAnnotatorAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		standardPreprocessingAnnotatorAAE.setPrimitive(false);
		
		standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(true);
		standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(false);
		
//		Import myAnnotatorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
//		myAnnotatorAEImport.setName("descriptors.test.MyAnnotatorAE_Descriptor");
//		standardPreprocessingAnnotatorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("MyAnnotatorAE", myAnnotatorAEImport);
//		
		Import segmenterAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		segmenterAEImport.setName("descriptors.test.SegmenterAE_Descriptor");
		standardPreprocessingAnnotatorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("SegmenterAE", segmenterAEImport);
		
		Import posTaggerAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		posTaggerAEImport.setName("descriptors.test.POSTaggerAE_Descriptor");
		standardPreprocessingAnnotatorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("POSTaggerAE", posTaggerAEImport);
		
		Import lammatizerAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		lammatizerAEImport.setName("descriptors.test.LemmatizerAE_Descriptor");
		standardPreprocessingAnnotatorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("LemmatizerAE", lammatizerAEImport);
		
		Import chunkerAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		chunkerAEImport.setName("descriptors.test.ChunkerAE_Descriptor");
		standardPreprocessingAnnotatorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("ChunkerAE", chunkerAEImport);
		
		List<String> flowNames = new ArrayList<String>();
//		flowNames.add("MyAnnotatorAE");
		flowNames.add("SegmenterAE");
		flowNames.add("POSTaggerAE");
		flowNames.add("LemmatizerAE");
		flowNames.add("ChunkerAE");
		
		FixedFlow fixedFlow = new FixedFlow_impl();
	    fixedFlow.setFixedFlow(flowNames.toArray(new String[flowNames.size()]));
	    standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow);
	    
		standardPreprocessingAnnotatorAAE.toXML(
				new FileOutputStream(new File(root_folder+"/test/StandardTextAnnotatorAAE_Descriptor.xml").getAbsolutePath()));
		
	}
	
	public static void generateStandardTextAnnotatorDescriptor(String root_folder) throws InvalidXMLException, ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException {
		new File(root_folder+"/test").mkdirs();
		generateStandardTextAnnotatorAAEDescriptor(root_folder);

		
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
		standardTextAnnotatorAAEImport.setName("descriptors.test.StandardTextAnnotatorAAE_Descriptor");
		standardPreprocessingAnnotatorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("StandardTextAnnotatorAAE", standardTextAnnotatorAAEImport);
		
		List<String> flowNames = new ArrayList<String>();
		flowNames.add("StandardTextAnnotatorAAE");
		
		FixedFlow fixedFlow = new FixedFlow_impl();
	    fixedFlow.setFixedFlow(flowNames.toArray(new String[flowNames.size()]));
	    standardPreprocessingAnnotatorAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow);
	    
		standardPreprocessingAnnotatorAAE.toXML(
				new FileOutputStream(new File(root_folder+"/test/StandardTextAnnotator_Descriptor.xml").getAbsolutePath()));
		
	}
	
	private static void generateMyAnnotatorAAEDescriptor(String root_folder) throws InvalidXMLException, ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException {
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
	
	private static void generateInputJCasMultiplierAAEDescriptor(String root_folder,boolean concatenate) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException {
		new File(root_folder+"/test").mkdirs();
		System.out.println("Generating XML description for InputJCasMultiplierAE_Descriptor");
		AnalysisEngineDescription inputJCasMultiplierAEDescriptor = AnalysisEngineFactory.createEngineDescription(
				InputJCasMultiplier.class,
				InputJCasMultiplier.CONCATENATE_PARAM,concatenate);
		ExternalResourceFactory.bindResource(inputJCasMultiplierAEDescriptor,
				InputJCasMultiplier.PREPROCESSOR_EXTERNAL_RESOURCE, StandardPreprocessor.class);
		inputJCasMultiplierAEDescriptor.toXML(
				new FileOutputStream(root_folder+"/test/InputJCasMultiplierAE_Descriptor.xml"));
	
		System.out.println("Generating XML description for InputJCasMultiplierAAE_Descriptor");
		AnalysisEngineDescription inputJCasMultiplierAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		inputJCasMultiplierAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		inputJCasMultiplierAAE.setPrimitive(false);

		inputJCasMultiplierAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(true);
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
	
	private static void generateProcessedJCasAggregatorAAEDescriptor(String root_folder) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException {
		new File(root_folder+"/test").mkdirs();
		System.out.println("Generating XML description for ProcessedJCASAggregatorAE_Descriptor");
		AnalysisEngineDescription processedJCASAggregatorAEDescriptor = AnalysisEngineFactory.createEngineDescription(
				ProcessedJCASAggregator.class);
//		ExternalResourceFactory.bindResource(processedJCASAggregatorAEDescriptor,
//				ProcessedJCASAggregator.PARAM_PROCESSED_INSTANCES_MANAGER_RESOURCE, ProcessedInstancesManager.class,"",
//				ExternalResourceFactory.PARAM_RESOURCE_NAME,"processedInstancesManager");
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
	
	private static void generateJCasPairGeneratorAAEDescriptor(String root_folder) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException {
		new File(root_folder+"/test").mkdirs();
		System.out.println("Generating XML description for JCasPairGeneratorAE_Descriptor");
		AnalysisEngineDescription jcasPairGeneratorAEDescriptor = AnalysisEngineFactory.createEngineDescription(
				JCasPairGenerator.class);
		jcasPairGeneratorAEDescriptor.toXML(
				new FileOutputStream(root_folder+"/test/JCasPairGeneratorAE_Descriptor.xml"));
		
		System.out.println("Generating XML description for JCasPairGeneratorAAE_Descriptor");
		AnalysisEngineDescription jcasPairGeneratorAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		jcasPairGeneratorAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		jcasPairGeneratorAAE.setPrimitive(false);

		jcasPairGeneratorAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(false);
		jcasPairGeneratorAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		jcasPairGeneratorAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
		
		Import jcasPairGeneratorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		jcasPairGeneratorAEImport.setName("descriptors.test.JCasPairGeneratorAE_Descriptor");
		jcasPairGeneratorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("JCasPairGeneratorAE", jcasPairGeneratorAEImport);
		
		List<String> flowNames2 = new ArrayList<String>();
		flowNames2.add("JCasPairGeneratorAE");
		
		FixedFlow fixedFlow2 = new FixedFlow_impl();
	    fixedFlow2.setFixedFlow(flowNames2.toArray(new String[flowNames2.size()]));
	    jcasPairGeneratorAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow2);
  
	    jcasPairGeneratorAAE.toXML(
				new FileOutputStream(root_folder+"/test/JCasPairGeneratorAAE_Descriptor.xml"));		
		
	}
	
	private static void generateKeLPRepresentationExtractorAAEDescriptor(String root_folder) throws ResourceInitializationException, InvalidXMLException, FileNotFoundException, SAXException, IOException {
		new File(root_folder+"/test").mkdirs();
		System.out.println("Generating XML description for KeLPRepresentationExtractorAE_Descriptor");
		AnalysisEngineDescription representationExtractorAEDescriptor = AnalysisEngineFactory.createEngineDescription(
				RepresentantationExtractor.class);
		
		ExternalResourceFactory.bindResource(representationExtractorAEDescriptor,
				RepresentantationExtractor.SERIALIZER_EXTERNAL_RESOURCE, KeLPSerializer.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,"kelpSerializer");
		representationExtractorAEDescriptor.toXML(
				new FileOutputStream(root_folder+"/test/KeLPRepresentationExtractorAE_Descriptor.xml"));
	
		System.out.println("Generating XML description for KeLPRepresentationExtractorAAE_Descriptor");
		AnalysisEngineDescription representationExtractorAAE = AnalysisEngineFactory.createEngineDescription(
				new LinkedList<AnalysisEngineDescription>(),new LinkedList<String>(),null,null,null);
		representationExtractorAAE.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		representationExtractorAAE.setPrimitive(false);

		representationExtractorAAE.getAnalysisEngineMetaData().getOperationalProperties().setModifiesCas(false);
		representationExtractorAAE.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(true);
		representationExtractorAAE.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
		
		Import representationExtractorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		representationExtractorAEImport.setName("descriptors.test.KeLPRepresentationExtractorAE_Descriptor");
		representationExtractorAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("KeLPRepresentationExtractorAE", representationExtractorAEImport);
		
		List<String> flowNames1 = new ArrayList<String>();
		flowNames1.add("KeLPRepresentationExtractorAE");
		
		FixedFlow fixedFlow1 = new FixedFlow_impl();
	    fixedFlow1.setFixedFlow(flowNames1.toArray(new String[flowNames1.size()]));
	    representationExtractorAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow1);
		
	    representationExtractorAAE.toXML(
				new FileOutputStream(root_folder+"/test/KeLPRepresentationExtractorAAE_Descriptor.xml"));		
		
	}
	
	private static void generatePipelineForPairGeneratorTestDescriptor(String root_folder) throws ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException, InvalidXMLException {
		new File(root_folder+"/test").mkdirs();
		
		TestDescriptorGenerator.generateInputJCasMultiplierAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors",true);
		TestDescriptorGenerator.generateMyAnnotatorAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		TestDescriptorGenerator.generateProcessedJCasAggregatorAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		TestDescriptorGenerator.generateJCasPairGeneratorAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		
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
		
		Import jcasPairGeneratorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		jcasPairGeneratorAEImport.setName("descriptors.test.JCasPairGeneratorAAE_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("JCasPairGeneratorAAE", jcasPairGeneratorAEImport);
		
		List<String> flowNames = new ArrayList<String>();
		flowNames.add("InputJCasMultiplierAAE");
		flowNames.add("MyAnnotatorAAE");
		flowNames.add("ProcessedJCASAggregatorAAE");
		flowNames.add("JCasPairGeneratorAAE");
		
		FixedFlow fixedFlow = new FixedFlow_impl();
	    fixedFlow.setFixedFlow(flowNames.toArray(new String[flowNames.size()]));
	    pipelineAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow);
  
	    pipelineAAE.toXML(
				new FileOutputStream(new File(root_folder+"/test/PipelineForPairGeneratorTestAAE_Descriptor.xml").getAbsolutePath()));	
	    
	}
	
	public static void generatePipelineForPairGeneratorTestDeploymentDescriptor(String root_folder) throws InvalidXMLException, ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException, JDOMException {
		new File(root_folder+"/test").mkdirs();
		
		TestDescriptorGenerator.generatePipelineForPairGeneratorTestDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		
		System.out.println("Generating XML description for PipelineWithPairGeneratorAAE_DeploymentDescriptor");
		ServiceContext pipelineContext = new ServiceContextImpl("FeatureExtraction", 
							           "PipelineWithPairGeneratorAAE_DeploymentDescriptor",
							           "descriptors.test.PipelineForPairGeneratorTestAAE_Descriptor", 
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
		
		ColocatedDelegateConfiguration delegate4 = new ColocatedDelegateConfigurationImpl("JCasPairGeneratorAE", new DelegateConfiguration[0]);
		delegate4.setCasMultiplier(true);
		delegate4.setCasPoolSize(10);
		ColocatedDelegateConfiguration spCldd4 = new ColocatedDelegateConfigurationImpl("JCasPairGeneratorAAE", new DelegateConfiguration[]{delegate4});
		
		UimaASAggregateDeploymentDescriptor spdd = DeploymentDescriptorFactory.createAggregateDeploymentDescriptor(
				pipelineContext,spCldd1,spCldd2,spCldd3,spCldd4);
		
		BufferedWriter out = new BufferedWriter(
				new OutputStreamWriter(
						new FileOutputStream(root_folder+"/test/PipelineForPairGeneratorTestAAE_DeploymentDescriptor.xml")));
		out.write(spdd.toXML());
		out.close();
		
		
		SAXBuilder builder = new SAXBuilder();
		Document descriptor = builder.build(new File(root_folder+"/test/PipelineForPairGeneratorTestAAE_DeploymentDescriptor.xml"));
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
						new FileOutputStream(root_folder+"/test/PipelineForPairGeneratorTestAAE_DeploymentDescriptor.xml")));
		out.write(xmlOut.outputString(descriptor));
		out.close();
	}
	
	private static void generatePreprocessingPipelineDescriptor(String root_folder,boolean concatenate) throws ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException, InvalidXMLException {
		new File(root_folder+"/test").mkdirs();
		
		TestDescriptorGenerator.generateInputJCasMultiplierAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors",concatenate);
		TestDescriptorGenerator.generateStandardTextAnnotatorDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		TestDescriptorGenerator.generateProcessedJCasAggregatorAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		
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
		
		Import standardTextAnnotatorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		standardTextAnnotatorAEImport.setName("descriptors.test.StandardTextAnnotator_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("StandardTextAnnotator", standardTextAnnotatorAEImport);
		
		Import processedJCasMultiplierAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		processedJCasMultiplierAEImport.setName("descriptors.test.ProcessedJCASAggregatorAAE_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("ProcessedJCASAggregatorAAE", processedJCasMultiplierAEImport);
		
		List<String> flowNames = new ArrayList<String>();
		flowNames.add("InputJCasMultiplierAAE");
		flowNames.add("StandardTextAnnotator");
		flowNames.add("ProcessedJCASAggregatorAAE");
		
		FixedFlow fixedFlow = new FixedFlow_impl();
	    fixedFlow.setFixedFlow(flowNames.toArray(new String[flowNames.size()]));
	    pipelineAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow);
  
	    pipelineAAE.toXML(
				new FileOutputStream(new File(root_folder+"/test/PreprocessingPipelineAAE_Descriptor.xml").getAbsolutePath()));	
	    
	}
	
	public static void generatePreprocessingPipelineDeploymentDescriptor(String root_folder,boolean concatenate) throws InvalidXMLException, ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException, JDOMException {
		new File(root_folder+"/test").mkdirs();
		
		TestDescriptorGenerator.generatePreprocessingPipelineDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors",concatenate);
		
		System.out.println("Generating XML description for PreprocessingPipelineAAE_DeploymentDescriptor");
		ServiceContext pipelineContext = new ServiceContextImpl("FeatureExtraction", 
							           "PreprocessingPipelineAAE_Descriptor",
							           "descriptors.test.PreprocessingPipelineAAE_Descriptor", 
							           "myQueueName", "tcp://localhost:61616");
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
						new FileOutputStream(root_folder+"/test/PreprocessingPipelineAAE_DeploymentDescriptor.xml")));
		out.write(spdd.toXML());
		out.close();
		
		
		SAXBuilder builder = new SAXBuilder();
		Document descriptor = builder.build(new File(root_folder+"/test/PreprocessingPipelineAAE_DeploymentDescriptor.xml"));
		Element root = descriptor.getRootElement();
		Element delegatesElement = root.getChild("deployment",root.getNamespace()).getChild("service",root.getNamespace())
				.getChild("analysisEngine",root.getNamespace()).getChild("delegates",root.getNamespace());
		Element myAnnotatorAAEElement = null;
		for (Element delegate : delegatesElement.getChildren()) {
			if (delegate.getAttributeValue("key").equals("StandardTextAnnotator")) {
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
		System.out.print(xmlOut.outputString(descriptor));
		out = new BufferedWriter(
				new OutputStreamWriter(
						new FileOutputStream(root_folder+"/test/PreprocessingPipelineAAE_DeploymentDescriptor.xml")));
		out.write(xmlOut.outputString(descriptor));
		out.close();
	}
	
	private static void generateFeatureExtractionPipelineDescriptor(String root_folder) throws ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException, InvalidXMLException {
		new File(root_folder+"/test").mkdirs();
		
		TestDescriptorGenerator.generateInputJCasMultiplierAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors",true);
		TestDescriptorGenerator.generateStandardTextAnnotatorDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		TestDescriptorGenerator.generateProcessedJCasAggregatorAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		TestDescriptorGenerator.generateJCasPairGeneratorAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		TestDescriptorGenerator.generateFeatureComputerDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		TestDescriptorGenerator.generateDecorationAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		TestDescriptorGenerator.generateKeLPRepresentationExtractorAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		
		
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
		
		Import standardTextAnnotatorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		standardTextAnnotatorAEImport.setName("descriptors.test.StandardTextAnnotator_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("StandardTextAnnotator", standardTextAnnotatorAEImport);
		
		Import processedJCasMultiplierAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		processedJCasMultiplierAEImport.setName("descriptors.test.ProcessedJCASAggregatorAAE_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("ProcessedJCASAggregatorAAE", processedJCasMultiplierAEImport);
		
		Import jcasPairGeneratorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		jcasPairGeneratorAEImport.setName("descriptors.test.JCasPairGeneratorAAE_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("JCasPairGeneratorAAE", jcasPairGeneratorAEImport);
		
		Import featureComputerAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		featureComputerAEImport.setName("descriptors.test.FeatureComputer_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("FeatureComputer", featureComputerAEImport);
		
		Import decorationAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		decorationAEImport.setName("descriptors.test.DecorationAnnotatorAAE_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("DecorationAnnotatorAAE", decorationAEImport);
		
		Import representationExtractionAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		representationExtractionAEImport.setName("descriptors.test.KeLPRepresentationExtractorAAE_Descriptor");
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
				new FileOutputStream(new File(root_folder+"/test/FeatureExtractionPipelineAAE_Descriptor.xml").getAbsolutePath()));	
	    
	}
	
	public static void generateFeatureExtractionPipelineDeploymentDescriptor(String root_folder) throws InvalidXMLException, ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException, JDOMException {
		new File(root_folder+"/test").mkdirs();
		
		TestDescriptorGenerator.generateFeatureExtractionPipelineDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		
		System.out.println("Generating XML description for PreprocessingPipelineAAE_DeploymentDescriptor");
		ServiceContext pipelineContext = new ServiceContextImpl("FeatureExtraction", 
							           "FeatureExtractionPipelineAAE_Descriptor",
							           "descriptors.test.FeatureExtractionPipelineAAE_Descriptor", 
							           "myQueueName", "tcp://localhost:61616");
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
		
		ColocatedDelegateConfiguration delegate41 = new ColocatedDelegateConfigurationImpl("JCasPairGeneratorAE", new DelegateConfiguration[0]);
		delegate41.setCasMultiplier(true);
		delegate41.setCasPoolSize(10);
		ColocatedDelegateConfiguration spCldd4 = new ColocatedDelegateConfigurationImpl("JCasPairGeneratorAAE", new DelegateConfiguration[]{delegate41});
		
		ColocatedDelegateConfiguration delegate51 = new ColocatedDelegateConfigurationImpl("FeatureComputerAAE", new DelegateConfiguration[0], new ErrorHandlingSettings[0]);
		ColocatedDelegateConfiguration spCldd5 = new ColocatedDelegateConfigurationImpl("FeatureComputer", new DelegateConfiguration[]{delegate51});
		
		ColocatedDelegateConfiguration delegate61 = new ColocatedDelegateConfigurationImpl("DecorationAnnotatorAE", new DelegateConfiguration[0], new ErrorHandlingSettings[0]);
		ColocatedDelegateConfiguration spCldd6 = new ColocatedDelegateConfigurationImpl("DecorationAnnotatorAAE", new DelegateConfiguration[]{delegate61});

		ColocatedDelegateConfiguration delegate71 = new ColocatedDelegateConfigurationImpl("KeLPRepresentationExtractorAE", new DelegateConfiguration[0]);
		delegate71.setCasMultiplier(true);
		delegate71.setCasPoolSize(10);
		ColocatedDelegateConfiguration spCldd7 = new ColocatedDelegateConfigurationImpl("KeLPRepresentationExtractorAAE", new DelegateConfiguration[]{delegate71});
		
		UimaASAggregateDeploymentDescriptor spdd = DeploymentDescriptorFactory.createAggregateDeploymentDescriptor(
				pipelineContext,spCldd1,spCldd2,spCldd3,spCldd4,spCldd5,spCldd6,spCldd7);
		
		BufferedWriter out = new BufferedWriter(
				new OutputStreamWriter(
						new FileOutputStream(root_folder+"/test/FeatureExtractionPipelineAAE_DeploymentDescriptor.xml")));
		out.write(spdd.toXML());
		out.close();
		
		
		Document descriptor = loadDescriptor(root_folder+"/test/FeatureExtractionPipelineAAE_DeploymentDescriptor.xml");
		addScaleoutElement(descriptor, "StandardTextAnnotator");
		addScaleoutElement(descriptor, "FeatureComputer");
		saveDescriptor(descriptor, root_folder+"/test/FeatureExtractionPipelineAAE_DeploymentDescriptor.xml");
	}
	
	private static void generateLearningPipelineDescriptor(String root_folder) throws ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException, InvalidXMLException {
		new File(root_folder+"/test").mkdirs();
		
		TestDescriptorGenerator.generateInputJCasMultiplierAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors",true);
		TestDescriptorGenerator.generateStandardTextAnnotatorDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		TestDescriptorGenerator.generateProcessedJCasAggregatorAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		TestDescriptorGenerator.generateJCasPairGeneratorAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		TestDescriptorGenerator.generateFeatureComputerDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		TestDescriptorGenerator.generateDecorationAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		TestDescriptorGenerator.generateKeLPRepresentationExtractorAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		TestDescriptorGenerator.generateLearningAnnotatorAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		
		
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
		
		Import standardTextAnnotatorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		standardTextAnnotatorAEImport.setName("descriptors.test.StandardTextAnnotator_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("StandardTextAnnotator", standardTextAnnotatorAEImport);
		
		Import processedJCasMultiplierAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		processedJCasMultiplierAEImport.setName("descriptors.test.ProcessedJCASAggregatorAAE_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("ProcessedJCASAggregatorAAE", processedJCasMultiplierAEImport);
		
		Import jcasPairGeneratorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		jcasPairGeneratorAEImport.setName("descriptors.test.JCasPairGeneratorAAE_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("JCasPairGeneratorAAE", jcasPairGeneratorAEImport);
		
		Import featureComputerAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		featureComputerAEImport.setName("descriptors.test.FeatureComputer_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("FeatureComputer", featureComputerAEImport);
		
		Import decorationAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		decorationAEImport.setName("descriptors.test.DecorationAnnotatorAAE_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("DecorationAnnotatorAAE", decorationAEImport);
		
		Import representationExtractionAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		representationExtractionAEImport.setName("descriptors.test.KeLPRepresentationExtractorAAE_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("KeLPRepresentationExtractorAAE", representationExtractionAEImport);
		
		Import learningAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		learningAEImport.setName("descriptors.test.LearningAnnotatorAAE_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("LearningAnnotatorAAE", learningAEImport);
		
		List<String> flowNames = new ArrayList<String>();
		flowNames.add("InputJCasMultiplierAAE");
		flowNames.add("StandardTextAnnotator");
		flowNames.add("ProcessedJCASAggregatorAAE");
		flowNames.add("JCasPairGeneratorAAE");
		flowNames.add("FeatureComputer");
		flowNames.add("DecorationAnnotatorAAE");
		flowNames.add("KeLPRepresentationExtractorAAE");
		flowNames.add("LearningAnnotatorAAE");

		FixedFlow fixedFlow = new FixedFlow_impl();
	    fixedFlow.setFixedFlow(flowNames.toArray(new String[flowNames.size()]));
	    pipelineAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow);
  
	    pipelineAAE.toXML(
				new FileOutputStream(new File(root_folder+"/test/LearningPipelineAAE_Descriptor.xml").getAbsolutePath()));	
	    
	}
	
	public static void generateLearningPipelineDeploymentDescriptor(String root_folder) throws InvalidXMLException, ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException, JDOMException {
		new File(root_folder+"/test").mkdirs();
		
		TestDescriptorGenerator.generateLearningPipelineDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		
		System.out.println("Generating XML description for LearningPipelineAAE_DeploymentDescriptor");
		ServiceContext pipelineContext = new ServiceContextImpl("Learning", 
							           "LearningPipelineAAE_Descriptor",
							           "descriptors.test.LearningPipelineAAE_Descriptor", 
							           "myQueueName", "tcp://localhost:61616");
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
		
		ColocatedDelegateConfiguration delegate41 = new ColocatedDelegateConfigurationImpl("JCasPairGeneratorAE", new DelegateConfiguration[0]);
		delegate41.setCasMultiplier(true);
		delegate41.setCasPoolSize(10);
		ColocatedDelegateConfiguration spCldd4 = new ColocatedDelegateConfigurationImpl("JCasPairGeneratorAAE", new DelegateConfiguration[]{delegate41});
		
		ColocatedDelegateConfiguration delegate51 = new ColocatedDelegateConfigurationImpl("FeatureComputerAAE", new DelegateConfiguration[0], new ErrorHandlingSettings[0]);
		ColocatedDelegateConfiguration spCldd5 = new ColocatedDelegateConfigurationImpl("FeatureComputer", new DelegateConfiguration[]{delegate51});
		
		ColocatedDelegateConfiguration delegate61 = new ColocatedDelegateConfigurationImpl("DecorationAnnotatorAE", new DelegateConfiguration[0], new ErrorHandlingSettings[0]);
		ColocatedDelegateConfiguration spCldd6 = new ColocatedDelegateConfigurationImpl("DecorationAnnotatorAAE", new DelegateConfiguration[]{delegate61});

		ColocatedDelegateConfiguration delegate71 = new ColocatedDelegateConfigurationImpl("KeLPRepresentationExtractorAE", new DelegateConfiguration[0]);
		delegate71.setCasMultiplier(true);
		delegate71.setCasPoolSize(10);
		ColocatedDelegateConfiguration spCldd7 = new ColocatedDelegateConfigurationImpl("KeLPRepresentationExtractorAAE", new DelegateConfiguration[]{delegate71});
		
		ColocatedDelegateConfiguration delegate81 = new ColocatedDelegateConfigurationImpl("LearningAnnotatorAE", new DelegateConfiguration[0]);
		delegate81.setCasMultiplier(true);
		delegate81.setCasPoolSize(10);
		ColocatedDelegateConfiguration spCldd8 = new ColocatedDelegateConfigurationImpl("LearningAnnotatorAAE", new DelegateConfiguration[]{delegate81});
		
		UimaASAggregateDeploymentDescriptor spdd = DeploymentDescriptorFactory.createAggregateDeploymentDescriptor(
				pipelineContext,spCldd1,spCldd2,spCldd3,spCldd4,spCldd5,spCldd6,spCldd7,spCldd8);
		
		BufferedWriter out = new BufferedWriter(
				new OutputStreamWriter(
						new FileOutputStream(root_folder+"/test/LearningPipelineAAE_DeploymentDescriptor.xml")));
		out.write(spdd.toXML());
		out.close();
		
		
		Document descriptor = loadDescriptor(root_folder+"/test/LearningPipelineAAE_DeploymentDescriptor.xml");
		addScaleoutElement(descriptor, "StandardTextAnnotator");
		addScaleoutElement(descriptor, "FeatureComputer");
		saveDescriptor(descriptor, root_folder+"/test/LearningPipelineAAE_DeploymentDescriptor.xml");
	}
	
	private static void generateClassificationPipelineDescriptor(String root_folder) throws ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException, InvalidXMLException {
		new File(root_folder+"/test").mkdirs();
		
		TestDescriptorGenerator.generateInputJCasMultiplierAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors",true);
		TestDescriptorGenerator.generateStandardTextAnnotatorDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		TestDescriptorGenerator.generateProcessedJCasAggregatorAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		TestDescriptorGenerator.generateJCasPairGeneratorAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		TestDescriptorGenerator.generateFeatureComputerDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		TestDescriptorGenerator.generateDecorationAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		TestDescriptorGenerator.generateKeLPRepresentationExtractorAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		TestDescriptorGenerator.generateClassificationAnnotatorAAEDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		
		
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
		
		Import standardTextAnnotatorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		standardTextAnnotatorAEImport.setName("descriptors.test.StandardTextAnnotator_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("StandardTextAnnotator", standardTextAnnotatorAEImport);
		
		Import processedJCasMultiplierAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		processedJCasMultiplierAEImport.setName("descriptors.test.ProcessedJCASAggregatorAAE_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("ProcessedJCASAggregatorAAE", processedJCasMultiplierAEImport);
		
		Import jcasPairGeneratorAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		jcasPairGeneratorAEImport.setName("descriptors.test.JCasPairGeneratorAAE_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("JCasPairGeneratorAAE", jcasPairGeneratorAEImport);
		
		Import featureComputerAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		featureComputerAEImport.setName("descriptors.test.FeatureComputer_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("FeatureComputer", featureComputerAEImport);
		
		Import decorationAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		decorationAEImport.setName("descriptors.test.DecorationAnnotatorAAE_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("DecorationAnnotatorAAE", decorationAEImport);
		
		Import representationExtractionAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		representationExtractionAEImport.setName("descriptors.test.KeLPRepresentationExtractorAAE_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("KeLPRepresentationExtractorAAE", representationExtractionAEImport);
		
		Import classificationAEImport = UIMAFramework.getResourceSpecifierFactory().createImport();
		classificationAEImport.setName("descriptors.test.ClassificationAnnotatorAAE_Descriptor");
		pipelineAAE.getDelegateAnalysisEngineSpecifiersWithImports().put("ClassificationAnnotatorAAE", classificationAEImport);
		
		List<String> flowNames = new ArrayList<String>();
		flowNames.add("InputJCasMultiplierAAE");
		flowNames.add("StandardTextAnnotator");
		flowNames.add("ProcessedJCASAggregatorAAE");
		flowNames.add("JCasPairGeneratorAAE");
		flowNames.add("FeatureComputer");
		flowNames.add("DecorationAnnotatorAAE");
		flowNames.add("KeLPRepresentationExtractorAAE");
		flowNames.add("ClassificationAnnotatorAAE");

		FixedFlow fixedFlow = new FixedFlow_impl();
	    fixedFlow.setFixedFlow(flowNames.toArray(new String[flowNames.size()]));
	    pipelineAAE.getAnalysisEngineMetaData().setFlowConstraints(fixedFlow);
  
	    pipelineAAE.toXML(
				new FileOutputStream(new File(root_folder+"/test/ClassificationPipelineAAE_Descriptor.xml").getAbsolutePath()));	
	    
	}
	
	public static void generateClassificationPipelineDeploymentDescriptor(String root_folder) throws InvalidXMLException, ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException, JDOMException {
		new File(root_folder+"/test").mkdirs();
		
		TestDescriptorGenerator.generateClassificationPipelineDescriptor(
				new File(PreprocessingPipelineConcatenatedTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		
		System.out.println("Generating XML description for LearningPipelineAAE_DeploymentDescriptor");
		ServiceContext pipelineContext = new ServiceContextImpl("Learning", 
							           "ClassificationPipelineAAE",
							           "descriptors.test.ClassificationPipelineAAE_Descriptor", 
							           "myQueueName", "tcp://localhost:61616");
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
		
		ColocatedDelegateConfiguration delegate41 = new ColocatedDelegateConfigurationImpl("JCasPairGeneratorAE", new DelegateConfiguration[0]);
		delegate41.setCasMultiplier(true);
		delegate41.setCasPoolSize(10);
		ColocatedDelegateConfiguration spCldd4 = new ColocatedDelegateConfigurationImpl("JCasPairGeneratorAAE", new DelegateConfiguration[]{delegate41});
		
		ColocatedDelegateConfiguration delegate51 = new ColocatedDelegateConfigurationImpl("FeatureComputerAAE", new DelegateConfiguration[0], new ErrorHandlingSettings[0]);
		ColocatedDelegateConfiguration spCldd5 = new ColocatedDelegateConfigurationImpl("FeatureComputer", new DelegateConfiguration[]{delegate51});
		
		ColocatedDelegateConfiguration delegate61 = new ColocatedDelegateConfigurationImpl("DecorationAnnotatorAE", new DelegateConfiguration[0], new ErrorHandlingSettings[0]);
		ColocatedDelegateConfiguration spCldd6 = new ColocatedDelegateConfigurationImpl("DecorationAnnotatorAAE", new DelegateConfiguration[]{delegate61});

		ColocatedDelegateConfiguration delegate71 = new ColocatedDelegateConfigurationImpl("KeLPRepresentationExtractorAE", new DelegateConfiguration[0]);
		delegate71.setCasMultiplier(true);
		delegate71.setCasPoolSize(10);
		ColocatedDelegateConfiguration spCldd7 = new ColocatedDelegateConfigurationImpl("KeLPRepresentationExtractorAAE", new DelegateConfiguration[]{delegate71});
		
		ColocatedDelegateConfiguration delegate81 = new ColocatedDelegateConfigurationImpl("ClassificationAnnotatorAE", new DelegateConfiguration[0]);
		ColocatedDelegateConfiguration spCldd8 = new ColocatedDelegateConfigurationImpl("ClassificationAnnotatorAAE", new DelegateConfiguration[]{delegate81});
		
		UimaASAggregateDeploymentDescriptor spdd = DeploymentDescriptorFactory.createAggregateDeploymentDescriptor(
				pipelineContext,spCldd1,spCldd2,spCldd3,spCldd4,spCldd5,spCldd6,spCldd7,spCldd8);
		
		BufferedWriter out = new BufferedWriter(
				new OutputStreamWriter(
						new FileOutputStream(root_folder+"/test/LearningPipelineAAE_DeploymentDescriptor.xml")));
		out.write(spdd.toXML());
		out.close();
		
		
		Document descriptor = loadDescriptor(root_folder+"/test/LearningPipelineAAE_DeploymentDescriptor.xml");
		addScaleoutElement(descriptor, "StandardTextAnnotator");
		addScaleoutElement(descriptor, "FeatureComputer");
		addScaleoutElement(descriptor, "ClassificationAnnotatorAAE");
		saveDescriptor(descriptor, root_folder+"/test/ClassificationPipelineAAE_DeploymentDescriptor.xml");
	}
	
//	public static void main(String args[]) throws InvalidXMLException, ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException, JDOMException {
//		generateFeatureExtractionPipelineDeploymentDescriptor(
//				new File(DescriptorGenerator.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
//	}
}

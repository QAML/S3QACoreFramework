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
 
 
package qa.qcri.iyas.learning.kelp;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;

import org.apache.ivy.core.resolve.ResolveProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import it.uniroma2.sag.kelp.data.dataset.SimpleDataset;
import it.uniroma2.sag.kelp.data.example.Example;
import it.uniroma2.sag.kelp.data.example.ExampleFactory;
import it.uniroma2.sag.kelp.data.example.ParsingExampleException;
import it.uniroma2.sag.kelp.data.label.Label;
import it.uniroma2.sag.kelp.data.label.StringLabel;
import it.uniroma2.sag.kelp.data.manipulator.TreePairRelTagger;
import it.uniroma2.sag.kelp.data.representation.structure.filter.LexicalStructureElementFilter;
import it.uniroma2.sag.kelp.data.representation.structure.similarity.ExactMatchingStructureElementSimilarity;
import it.uniroma2.sag.kelp.data.representation.tree.node.filter.ContentBasedTreeNodeFilter;
import it.uniroma2.sag.kelp.data.representation.tree.node.filter.TreeNodeFilter;
import it.uniroma2.sag.kelp.data.representation.tree.node.similarity.ContentBasedTreeNodeSimilarity;
import it.uniroma2.sag.kelp.data.representation.tree.node.similarity.TreeNodeSimilarity;
import it.uniroma2.sag.kelp.kernel.Kernel;
import it.uniroma2.sag.kelp.kernel.cache.DynamicIndexSquaredNormCache;
import it.uniroma2.sag.kelp.kernel.cache.FixSizeKernelCache;
import it.uniroma2.sag.kelp.kernel.pairs.UncrossedPairwiseSumKernel;
import it.uniroma2.sag.kelp.kernel.standard.LinearKernelCombination;
import it.uniroma2.sag.kelp.kernel.standard.NormalizationKernel;
import it.uniroma2.sag.kelp.kernel.standard.RbfKernel;
import it.uniroma2.sag.kelp.kernel.tree.PartialTreeKernel;
import it.uniroma2.sag.kelp.kernel.tree.SubSetTreeKernel;
import it.uniroma2.sag.kelp.kernel.vector.LinearKernel;
import it.uniroma2.sag.kelp.learningalgorithm.LearningAlgorithm;
import it.uniroma2.sag.kelp.learningalgorithm.classification.libsvm.BinaryCSvmClassification;
import it.uniroma2.sag.kelp.predictionfunction.PredictionFunction;
import it.uniroma2.sag.kelp.utils.JacksonSerializerWrapper;
import it.uniroma2.sag.kelp.utils.ObjectSerializer;
import qa.qcri.iyas.learning.Learner;

public class KeLPSVMLearner extends Learner  {

	public static final String PARAM_NAME_SIMS_KERNEL = "simsKernel";
	public static final String PARAM_NAME_TREE_KERNEL = "treeKernel";
	public static final String PARAM_NAME_RANK_KERNEL = "rankKernel";
	public static final String PARAM_NAME_APPLY_REL_TAGS = "relTagging";
	public static final String PARAM_NAME_C_SVM_PARAM = "c";
	public static final String PARAM_NAME_POSITIVE_CLASS_LABEL = "posLabel";

	@ConfigurationParameter(name = PARAM_NAME_SIMS_KERNEL, mandatory = false)
	private String simsKernel;
	
	@ConfigurationParameter(name = PARAM_NAME_TREE_KERNEL, mandatory = false)
	private String treeKernel;
	
	@ConfigurationParameter(name = PARAM_NAME_RANK_KERNEL, mandatory = false)
	private String rankKernel;
	
	@ConfigurationParameter(name = PARAM_NAME_APPLY_REL_TAGS, defaultValue = "true")
	private Boolean applyRELTags;
	
	@ConfigurationParameter(name = PARAM_NAME_C_SVM_PARAM, mandatory = true)
	private float c;
	
	@ConfigurationParameter(name = PARAM_NAME_POSITIVE_CLASS_LABEL)
	private String positiveLabelStr;
	
	private Label positiveLabel;
	
	private SimpleDataset addRELtags(SimpleDataset dataset, String representationName) {
		HashSet<String> stopwords = new HashSet<String>();
		stopwords.add("be");
		stopwords.add("have");

		HashSet<String> posOfInterest = new HashSet<String>();
		posOfInterest.add("n");
		posOfInterest.add("v");
		posOfInterest.add("j");
		posOfInterest.add("r");
		LexicalStructureElementFilter elementFilter = new LexicalStructureElementFilter(stopwords, posOfInterest);
		//
		
		TreeNodeFilter nodeFilter = new ContentBasedTreeNodeFilter(elementFilter);


		ExactMatchingStructureElementSimilarity exactMatching = new ExactMatchingStructureElementSimilarity(true);
		//exact matching between lexical nodes 
		TreeNodeSimilarity contentNodeSimilarity = new ContentBasedTreeNodeSimilarity(exactMatching);
		
		TreePairRelTagger newTagger = new TreePairRelTagger(2, 0, 
				representationName, nodeFilter, 
				it.uniroma2.sag.kelp.data.manipulator.TreePairRelTagger.MARKING_POLICY.ON_NODE_LABEL, 
				contentNodeSimilarity, 1);

		dataset.manipulate(newTagger);
		return dataset;
	}

	private LearningAlgorithm getSVMalgorithm(Kernel kernel) {
		BinaryCSvmClassification svm = new BinaryCSvmClassification(kernel, 
				positiveLabel, c, c, false); 

		System.err.println(String.format("Learning algorithm=BinaryCSvm; "
				+ "positive class=%s; c+ = %f c- =%f fairness=%s %n", 
				positiveLabelStr, c, c, ""+false));
		return svm;
	}
	
	private static PredictionFunction train(LearningAlgorithm algorithm, SimpleDataset dataset) {
		for (Label l : dataset.getClassificationLabels()) {
			System.err.println(String.format("Training Label %s=%d examples (%f%%) ", 
					l.toString(), dataset.getNumberOfPositiveExamples(l), 
					((float)dataset.getNumberOfPositiveExamples(l))/((float)dataset.getNumberOfExamples())));
		}
		algorithm.learn(dataset);
		return algorithm.getPredictionFunction();
	}
	
	@Override
	public void afterResourcesInitialized() throws ResourceInitializationException {
		super.afterResourcesInitialized();
		if (simsKernel == null && treeKernel == null && rankKernel == null)
			throw new ResourceInitializationException("No kernel has been set",null);
		
		positiveLabel = new StringLabel(positiveLabelStr);
	}

	@Override
	public String learn(Object examples[]) throws ResolveProcessException {
		try {
			SimpleDataset trainingset = new SimpleDataset();
			for (int i=0;i<examples.length;i++)
				trainingset.addExample((Example)examples[i]);
			
			if (applyRELTags && treeKernel != null) {
				trainingset = addRELtags(trainingset, treeKernel);
			}
			
			LinearKernelCombination kernel = new LinearKernelCombination();
			if (simsKernel != null)
				kernel.addKernel(1,new RbfKernel(1.0f,new LinearKernel(simsKernel)));
			
			if (treeKernel != null) {
				int numberOfExamples = trainingset.getNumberOfExamples();
				SubSetTreeKernel sst = new SubSetTreeKernel(0.4f, "tree");
				sst.setSquaredNormCache(new DynamicIndexSquaredNormCache(numberOfExamples));
				//PartialTreeKernel ptk = new PartialTreeKernel(0.4f, 0.4f, 1, "tree");
				//ptk.setSquaredNormCache(new DynamicIndexSquaredNormCache(numberOfExamples));
				
				//NormalizationKernel norm = new NormalizationKernel(ptk);
				NormalizationKernel norm = new NormalizationKernel(sst);
				norm.setKernelCache(new FixSizeKernelCache(numberOfExamples*2));
				UncrossedPairwiseSumKernel pairwiseKernel = new UncrossedPairwiseSumKernel(norm, true);
				
				kernel.addKernel(1,pairwiseKernel);
			}
			
			if (rankKernel != null)
				kernel.addKernel(1,new RbfKernel(1.0f,new LinearKernel(rankKernel)));
			
			System.err.println("Learning started at " + LocalDateTime.now().toString());
			PredictionFunction learningModel = train(getSVMalgorithm(kernel), trainingset);
			System.err.println("Learning ended at " + LocalDateTime.now().toString());
			
			ObjectSerializer serializer = new JacksonSerializerWrapper();
			return serializer.writeValueAsString(learningModel);
			
		} catch (IOException e) {
			 throw new ResolveProcessException(e);
		}
	}

	@Override
	public Object extractExample(JCas jcas) throws ResolveProcessException {
		try {
			return ExampleFactory.parseExample(jcas.getDocumentText());
		} catch (InstantiationException | ParsingExampleException e) {
			throw new  ResolveProcessException(e);
		}
	}
	
	



}

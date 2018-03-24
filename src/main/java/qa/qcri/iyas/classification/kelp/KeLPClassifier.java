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

package qa.qcri.iyas.classification.kelp;

import java.io.File;
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
import it.uniroma2.sag.kelp.predictionfunction.PredictionFunction;
import it.uniroma2.sag.kelp.utils.JacksonSerializerWrapper;
import it.uniroma2.sag.kelp.utils.ObjectSerializer;
import qa.qcri.iyas.classification.Classifier;

public class KeLPClassifier extends Classifier {
	
	public static final String PARAM_NAME_APPLY_REL_TAGS = "relTagging";
	public static final String PARAM_NAME_POSITIVE_CLASS_LABEL = "Relevant";
	
	private PredictionFunction pf;
	
	@ConfigurationParameter(name = PARAM_NAME_APPLY_REL_TAGS, defaultValue = "true")
	private Boolean applyRELTags;
	
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
	
	@Override
	public void afterResourcesInitialized() throws ResourceInitializationException {
		positiveLabel = new StringLabel(positiveLabelStr);
	}

	@Override
	protected void loadModel(String modelFile) throws ResourceInitializationException {
		ObjectSerializer serializer = new JacksonSerializerWrapper();
		File file = new File(modelFile);
		try {
			pf = (PredictionFunction) serializer.readValue(file, PredictionFunction.class);
			System.out.println("Loaded Kelp model from file "+modelFile);
		}catch (Exception e) {
			throw new ResourceInitializationException("Cannot load Kelp model from file "+modelFile, null);
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

	@Override
	public float getPredictionScore(Object example) throws ResolveProcessException {
		Example kelpEx = (Example)example;
		if (applyRELTags) {
			SimpleDataset sd = new SimpleDataset();
			sd.addExample(kelpEx);
			sd = addRELtags(sd, "tree");
			kelpEx = sd.getExample(0);
		}	
			
		return pf.predict(kelpEx).getScore(positiveLabel);
	}

}

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


package qa.qcri.iyas.feature;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ExternalResourceDependency;
import org.apache.uima.util.CasCopier;
import org.apache.uima.util.InvalidXMLException;
import org.junit.Test;
import org.uimafit.factory.ExternalResourceFactory;
import org.xml.sax.SAXException;

import qa.qcri.iyas.data.preprocessing.JCasPairGenerator;
import qa.qcri.iyas.data.preprocessing.Stopwords;
import qa.qcri.iyas.feature.extractor.SimilarityAnnotator;
import qa.qcri.iyas.feature.similarity.CosineBowSimilarity;
import qa.qcri.iyas.feature.similarity.CosineTokenSimilarity;
import qa.qcri.iyas.feature.similarity.GreedyStringTilingSimilarity;
import qa.qcri.iyas.feature.similarity.GreedyStringTilingSimilarityTest;
import qa.qcri.iyas.feature.similarity.LongestCommonSubsequenceComparatorSimilarity;
import qa.qcri.iyas.feature.similarity.LongestCommonSubsequenceNormComparatorSimilarity;
import qa.qcri.iyas.feature.similarity.LongestCommonSubstringComparatorSimilarity;
import qa.qcri.iyas.feature.similarity.SimilarityMeasure;
import qa.qcri.iyas.feature.similarity.TreeKernelSimilarity;
import qa.qcri.iyas.feature.similarity.WordNGramContainmentMeasureSimilarity;
import qa.qcri.iyas.feature.similarity.WordNGramJaccardMeasureSimilarity;
import qa.qcri.iyas.type.representation.DenseVector;

public class SimilarityAnnotatorTest {
	
	public List<ExternalResourceDependency> bindSimilarities(AnalysisEngineDescription descr) throws InvalidXMLException {
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
			dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, SimilarityMeasure.class,false));
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
			dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, SimilarityMeasure.class,false));
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
			dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, SimilarityMeasure.class,false));
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
		
		
		simName = "TreeKernelSimilarity";
		dependencyList.add(ExternalResourceFactory.createExternalResourceDependency(simName, SimilarityMeasure.class,false));
		descr.setExternalResourceDependencies(dependencyList.toArray(new ExternalResourceDependency[0]));
		
		ExternalResourceFactory.bindResource(descr,
				simName, TreeKernelSimilarity.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,simName,
				TreeKernelSimilarity.PARAM_NAME_TREE_TYPE, TreeKernelSimilarity.TREE_TYPE.POS_CHUNK_TREE,
				TreeKernelSimilarity.PARAM_NAME_TREE_KERNEL, TreeKernelSimilarity.TREE_KERNEL_FUNCTION.PTK,
				TreeKernelSimilarity.PARAM_NAME_NORMALIZED, true,
				TreeKernelSimilarity.PARAM_NAME_LAMBDA, 1.0f);
		
		return dependencyList;
	}

	@Test
	public void test() throws IOException, URISyntaxException, UIMAException, SAXException {
		AnalysisEngineDescription descr = AnalysisEngineFactory.createEngineDescriptionFromPath(
				new File(GreedyStringTilingSimilarityTest.class.getResource(
						"/descriptors/qa/qcri/iyas/feature/StandardSimpleFeatureExtractorAAE_Descriptor.xml" ).
						toURI()).getAbsolutePath());
		AnalysisEngine aae = AnalysisEngineFactory.createEngine(descr);
		
		JCas jcas1 = JCasFactory.createText("The cat is black", "en");
		JCas jcas2 = JCasFactory.createText("The cat is white", "en");
		double refSims[] = new double[] {0.75, 
				0.75, 
				0.7142857142857142, 
				0.7142857142857142, 
				0.6666666666666666, 
				0.6666666666666666, 
				0.5999999999999999, 
				0.5999999999999999, 
				0.5000000000000001, 
				0.5000000000000001, 
				0.33333333333333337, 
				0.33333333333333337, 
				1.0, 
				0.9999999999999998, 
				1.0000000000000002, 
				0.6875, 
				0.6875, 
				0.6875, 
				0.6875, 
				0.6, 
				0.5, 
				0.3333333333333333, 
				0.0, 
				0.75, 
				0.6666666666666666, 
				0.75, 
				0.9999999920769546};
		
		aae.process(jcas1);
		aae.process(jcas2);
		
		AnalysisEngineDescription similarityTestAnnotatorAE_Descriptor = AnalysisEngineFactory.createEngineDescription(
				SimilarityAnnotator.class);
		
		List<ExternalResourceDependency> dependencyList = bindSimilarities(similarityTestAnnotatorAE_Descriptor);
		String resouceNames[] = new String[dependencyList.size()];
		for (int i=0;i<resouceNames.length;i++)
			resouceNames[i] = dependencyList.get(i).getKey();
		
		AnalysisEngine ae = AnalysisEngineFactory.createEngine(similarityTestAnnotatorAE_Descriptor,
				SimilarityAnnotator.PARAM_NAME_SIMILARITIES,resouceNames);
		
		JCas jcas = JCasFactory.createText("text3", "en");
		
		CasCopier copier = new CasCopier(jcas1.getCas(),jcas.getCas());
		copier.copyCasView(jcas1.getCas().getView("_InitialView"), jcas.getCas().createView(JCasPairGenerator.LEFT_CAS_VIEW), true);
		
		copier = new CasCopier(jcas2.getCas(),jcas.getCas());
		copier.copyCasView(jcas2.getCas().getView("_InitialView"), jcas.getCas().createView(JCasPairGenerator.RIGHT_CAS_VIEW), true);
		
		ae.process(jcas);

		for (DenseVector sims : JCasUtil.select(jcas.getView("_InitialView"), DenseVector.class)) {
			int i = 0;
			for (double sim : sims.getFeatures().toArray()) {
				assertEquals(refSims[i++], sim,0.0000001);
			}
		}
	}
	
	public static void main(String args[]) throws UIMAException, IOException, URISyntaxException, SAXException {
//		Result result = JUnitCore.runClasses(SimilarityAnnotatorTest.class);
//		for (Failure failure : result.getFailures()) {
//			System.out.println(failure.toString());
//		}
		
		SimilarityAnnotatorTest sim = new SimilarityAnnotatorTest();
		sim.test();
	}
}

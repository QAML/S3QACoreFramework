/**
 * Copyright 2017 Giovanni Da San Martino, Salvatore Romeo and Alberto Barron-Cedeno
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
 
 
package qa.qcri.iyas.feature.similarity;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.CasCopier;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import qa.qcri.iyas.data.preprocessing.Stopwords;
import qa.qcri.iyas.type.Similarity;

/**
 * A simple class to test class {@link CosineBowSimilarity}.
 * 
 * @author Giovanni Da San Martino
 *
 */
public class CosineBowSimilarityTest {

	@Test
	public void testGetSimilarityValue() throws IOException, URISyntaxException, UIMAException {
		AnalysisEngineDescription descr = AnalysisEngineFactory.createEngineDescriptionFromPath(
				new File(CosineBowSimilarityTest.class.getResource(
						"/descriptors/qa/qcri/iyas/feature/StandardSimpleFeatureExtractorAAE_Descriptor.xml" ).
						toURI()).getAbsolutePath());
		AnalysisEngine aae = AnalysisEngineFactory.createEngine(descr);
		
		JCas jcas1 = JCasFactory.createText("The cat runs fast", "en");
		JCas jcas2 = JCasFactory.createText("The cat runs quick", "en");
		
		aae.process(jcas1);
		aae.process(jcas2);
				
		AnalysisEngineDescription similarityTestAnnotatorAE_Descriptor = AnalysisEngineFactory.createEngineDescription(
				SimilarityMeasureTestAnnotator.class);
		ExternalResourceFactory.bindResource(similarityTestAnnotatorAE_Descriptor,
				SimilarityMeasureTestAnnotator.PARAM_SIMILARITY_RESOURCE, CosineBowSimilarity.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,"cosineBowSimilarity",
				CosineBowSimilarity.PARAM_NAME_STOPWORDS_OBJECT, Stopwords.STOPWORD_EN,
				CosineBowSimilarity.PARAM_NAME_REMOVE_STOPWORDS, false,
				CosineBowSimilarity.PARAM_NAME_MIN_N_GRAM_SIZE, 1,
				CosineBowSimilarity.PARAM_NAME_MAX_N_GRAM_SIZE, 3,
				CosineBowSimilarity.PARAM_NAME_REPRESENTATION_TYPE, CosineBowSimilarity.PARAMETER_LIST_POSTAGS);
		
		AnalysisEngine ae = AnalysisEngineFactory.createEngine(similarityTestAnnotatorAE_Descriptor);
		
		JCas jcas = JCasFactory.createText("text3", "en");
		
		CasCopier copier = new CasCopier(jcas1.getCas(),jcas.getCas());
		copier.copyCasView(jcas1.getCas().getView("_InitialView"), jcas.getCas().createView(SimilarityMeasureTestAnnotator.PRAM_LEFT), true);
		
		copier = new CasCopier(jcas2.getCas(),jcas.getCas());
		copier.copyCasView(jcas2.getCas().getView("_InitialView"), jcas.getCas().createView(SimilarityMeasureTestAnnotator.PRAM_RIGHT), true);
		
		ae.process(jcas);
		
		for (Similarity sim : JCasUtil.select(jcas.getView("_InitialView"), Similarity.class)) {
			System.out.println(sim.getValue());
			assertEquals(0.66666666, sim.getValue(),0.0000001);
		}
	}

	public static void main(String[] args) {
		Result result = JUnitCore.runClasses(CosineBowSimilarityTest.class);
		for (Failure failure : result.getFailures()) {
			System.out.println(failure.toString());
		}
	}
}

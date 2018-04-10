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

import qa.qcri.iyas.type.representation.NumericFeature;

/**
 * A simple class to test a {@link ThreadSafeSimilarityMeasure}.
 * 
 * @author Salvatore Romeo
 *
 */
public class SimilarityMeasureExampleTest {

	@Test
	public void testGetSimilarityValue() throws IOException, URISyntaxException, UIMAException {
		AnalysisEngineDescription descr = AnalysisEngineFactory.createEngineDescriptionFromPath(
				new File(SimilarityMeasureExampleTest.class.getResource(
						"/descriptors/qa/qcri/iyas/feature/StandardSimpleFeatureExtractorAAE_Descriptor.xml" ).
						toURI()).getAbsolutePath());
		AnalysisEngine aae = AnalysisEngineFactory.createEngine(descr);
		
		JCas jcas1 = JCasFactory.createText("The cat is black", "en");
		JCas jcas2 = JCasFactory.createText("The cat is while", "en");
		
		aae.process(jcas1);
		aae.process(jcas2);
			
		AnalysisEngineDescription similarityTestAnnotatorAE_Descriptor = AnalysisEngineFactory.createEngineDescription(
				SimilarityMeasureTestAnnotator.class);
		
		/*
		 * Binds the SimilarityMeasureExample to the SimilarityMeasureTestAnnotator. The paramaters are passed by
		 * means of key-value pairs. In this case there are two parameters: SimilarityMeasureExample.PARAM_NAME_1,
		 * just for the sake of example, and SimilarityMeasureExample.PARAM_NAME_FAKE_SIM_TO_GENERATE that specified the
		 * value returned by SimilarityMeasureExample. These parameters do not any sense, they are used just for
		 * showing how to bind a new implementation of SimilarityMeasure.
		 * IMPORTANT: only this line of code must be changed to test other DataReader implementations.
		 */
		ExternalResourceFactory.bindResource(similarityTestAnnotatorAE_Descriptor,
				SimilarityMeasureTestAnnotator.PARAM_SIMILARITY_RESOURCE, SimilarityMeasureExample.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,"similarityMeasureExample",
				SimilarityMeasureExample.PARAM_NAME_1, "test_param",
				SimilarityMeasureExample.PARAM_NAME_FAKE_SIM_TO_GENERATE, 0.5);
		
		AnalysisEngine ae = AnalysisEngineFactory.createEngine(similarityTestAnnotatorAE_Descriptor);
		
		JCas jcas = JCasFactory.createText("text3", "en");
		
		CasCopier copier = new CasCopier(jcas1.getCas(),jcas.getCas());
		copier.copyCasView(jcas1.getCas().getView("_InitialView"), jcas.getCas().createView(SimilarityMeasureTestAnnotator.LEFT_CAS_VIEW), true);
		
		copier = new CasCopier(jcas2.getCas(),jcas.getCas());
		copier.copyCasView(jcas2.getCas().getView("_InitialView"), jcas.getCas().createView(SimilarityMeasureTestAnnotator.RIGHT_CAS_VIEW), true);
		
		ae.process(jcas);
		
		for (NumericFeature sim : JCasUtil.select(jcas.getView("_InitialView"), NumericFeature.class)) {
			assertEquals(0.5, sim.getValue(),0);
		}
	}

	public static void main(String[] args) {
		Result result = JUnitCore.runClasses(SimilarityMeasureExampleTest.class);
		for (Failure failure : result.getFailures()) {
			System.out.println(failure.toString());
		}
	}
}

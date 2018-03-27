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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCopier;
import org.apache.uima.util.InvalidXMLException;
import org.junit.Test;
import org.xml.sax.SAXException;

import qa.qcri.iyas.TestDescriptorGenerator;
import qa.qcri.iyas.data.preprocessing.JCasPairGenerator;
import qa.qcri.iyas.type.representation.DenseVector;

public class SimilarityAnnotatorTest {
	
	public AnalysisEngine getAnalysisEngine() throws InvalidXMLException, ResourceInitializationException, FileNotFoundException, SAXException, IOException, URISyntaxException {
		
		TestDescriptorGenerator.generateSimsAEDescriptor(
				new File(SimilarityAnnotatorTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		String aeDescriptor = 
				new File(SimilarityAnnotatorTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors/test"
						+ "/SimilarityAnnotatorAE_Descriptor.xml";
		
		AnalysisEngineDescription descr = AnalysisEngineFactory.createEngineDescriptionFromPath(aeDescriptor);

		AnalysisEngine ae = AnalysisEngineFactory.createEngine(descr);
		
		return ae;
	}

	@Test
	public void test() throws IOException, URISyntaxException, UIMAException, SAXException {
		
		TestDescriptorGenerator.generateStandardTextAnnotatorAAEDescriptor(
				new File(SimilarityAnnotatorTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors");
		String aeDescriptor = 
				new File(SimilarityAnnotatorTest.class.getResource("/").toURI()).getAbsolutePath()+"/descriptors/test"
						+ "/StandardTextAnnotatorAAE_Descriptor.xml";
		
		AnalysisEngineDescription descr = AnalysisEngineFactory.createEngineDescriptionFromPath(aeDescriptor);
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
		
		AnalysisEngine ae = getAnalysisEngine();
		
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

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

package qa.qcri.iyas.data.preprocessing;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;

public class ArabicParserAnalysisEngine extends JCasAnnotator_ImplBase {
	
	AnalysisEngine parser = null;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		
		parser = createEngine(createEngineDescription(StanfordParser.class));
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		if (!jcas.getDocumentLanguage().equals("ar"))
			throw new AnalysisEngineProcessException("Unsupported language: "+jcas.getDocumentLanguage(),null);
		try {
			parser.process(jcas);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

}

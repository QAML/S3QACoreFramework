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

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

@TypeCapability(
		inputs = {"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token"},
		
		outputs = {"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma"} 
)

public class ArabicLemmatizerAnalysisEngine extends JCasAnnotator_ImplBase {
	

	public ArabicLemmatizerAnalysisEngine() throws FileNotFoundException, ClassNotFoundException, IOException, InterruptedException {
		super();
	}
	
	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		for (Token token : JCasUtil.select(jcas, Token.class)) {
			Lemma lemma = new Lemma(jcas);
			lemma.setBegin(token.getBegin());
			lemma.setEnd(token.getEnd());
			lemma.setValue(jcas.getDocumentText().substring(token.getBegin(),token.getEnd()));
			lemma.addToIndexes(jcas);
			
			token.setLemma(lemma);
		}
	}

}

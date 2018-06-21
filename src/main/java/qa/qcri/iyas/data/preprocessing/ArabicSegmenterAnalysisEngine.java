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

import java.io.StringReader;
import java.util.Properties;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.stanford.nlp.international.arabic.process.ArabicTokenizer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;

@TypeCapability(
		outputs = {"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
				   "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence"} 
) 
public class ArabicSegmenterAnalysisEngine extends JCasAnnotator_ImplBase {
	
	private static Properties options = new Properties();
	static {
//	    options.setProperty("normArDigits", "true");
//	    options.setProperty("normArPunc", "true");
//	    options.setProperty("normAlif", "true");
//	    options.setProperty("normYa", "true");
//	    
//	    options.setProperty("removeDiacritics", "true");
//	    options.setProperty("removeTatweel", "true");
//	    options.setProperty("removeQuranChars", "true");
//	    options.setProperty("removeLengthening", "true");
	}
	
	private static TokenizerFactory<CoreLabel> factory = ArabicTokenizer.factory();
	static {
	    for (String option : options.stringPropertyNames()) {
	    	factory.setOptions(option);
	  }
	}

	public int getBeginOfSubStringForArabic(int startIdx,char str[],char substr[]) {
		
		for (int i=startIdx;i<str.length-substr.length+1;i++) {
			boolean found = true;
			for (int j=0;j<substr.length;j++) {
				if (str[i+j] != substr[j]) {
					found = false;
					break;
				}
			}
			
			if (found) {
				return i;
			}
		}
		
		return -1;
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		if (!jcas.getDocumentLanguage().equals("ar"))
			new AnalysisEngineProcessException("Unsupported language!",null);
		
		String text = jcas.getDocumentText();
		char[] textArray = text.toCharArray();
		Tokenizer<CoreLabel> tokenizer = factory.getTokenizer(new StringReader(text));
		
//		System.out.println(jcas.getDocumentText());
		
		int startIdx = 0;
		while (tokenizer.hasNext()) {
			String word = tokenizer.next().word();
			int tokenBegin = getBeginOfSubStringForArabic(startIdx,textArray,word.toCharArray());
			
			if (tokenBegin == -1)
				throw new AnalysisEngineProcessException("Cannot find substring "+word+", the corresponding token has been modified",null);
			
			int tokenEnd = tokenBegin+word.length();
			
			Token token = new Token(jcas);
			token.setBegin(tokenBegin);
			token.setEnd(tokenEnd);
			token.addToIndexes(jcas);
			
			startIdx = tokenEnd;
			
//			System.out.println(word);

		}
		
		Token startToken = null;
		Token endToken = null;
		Token lastWordToken = null;
		for (Token token : JCasUtil.select(jcas, Token.class)) {
			if (startToken == null && !jcas.getDocumentText().substring(token.getBegin(), token.getEnd()).matches("\\p{Punct}")) {
				startToken = token;
			} else if(startToken != null && jcas.getDocumentText().substring(token.getBegin(), token.getEnd()).matches("[!\\?\\.]")) {
				endToken = token;
			} else if(startToken != null && endToken != null && !jcas.getDocumentText().substring(token.getBegin(), token.getEnd()).matches("[!\\?\\.]")) {
				Sentence sentence = new Sentence(jcas);
				sentence.setBegin(startToken.getBegin());
				sentence.setEnd(endToken.getEnd());
				sentence.addToIndexes();
				
				if (!jcas.getDocumentText().substring(token.getBegin(), token.getEnd()).matches("\\p{Punct}")) {
					startToken = token;
				} else {
					startToken = null;
				}
				endToken = null;
			}
			
			if (!jcas.getDocumentText().substring(token.getBegin(), token.getEnd()).matches("\\p{Punct}"))
				lastWordToken = token;
		}
		
		if (startToken != null && endToken != null) {
			Sentence sentence = new Sentence(jcas);
			sentence.setBegin(startToken.getBegin());
			sentence.setEnd(endToken.getEnd());
			sentence.addToIndexes();
		} else if (startToken != null && lastWordToken != null) {
			Sentence sentence = new Sentence(jcas);
			sentence.setBegin(startToken.getBegin());
			sentence.setEnd(lastWordToken.getEnd());
			sentence.addToIndexes();
		}
	}

}


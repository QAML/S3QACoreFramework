/**
 * Copyright 2017 Giovanni Da San Martino and Salvatore Romeo
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import com.google.common.base.Joiner;

import cc.mallet.types.Alphabet;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import qa.qcri.iyas.data.preprocessing.BowProvider;
import qa.qcri.iyas.data.preprocessing.Stopwords;
import qa.qcri.iyas.data.tree.nodes.RichNode;
import qa.qcri.iyas.data.tree.nodes.RichTokenNode;

/**
 * The class extends {@link SimilarityMeasure} adding a method to extract the bag-of-word representation from a JCas. 
 *   
 * 
 * @author Giovanni Da San Martino
 *
 */
public abstract class SimilarityMeasureWithBowExtraction extends SimilarityMeasureWithTokenExtraction {

	//(this.alphabet, parameterList, from, to, new Stopwords())
	
	public static final String PARAM_NAME_STOPWORDS_OBJECT = "stopwordsObject";
	
	public static final String PARAM_NAME_REMOVE_STOPWORDS = "removeStopwords";
		
	public static final String PARAM_NAME_MIN_N_GRAM_SIZE = "minNGramSize";
	
	public static final String PARAM_NAME_MAX_N_GRAM_SIZE = "";
	
	public static final String PARAM_NAME_ALPHABET = ""; //

	
	@ConfigurationParameter(name = PARAM_NAME_STOPWORDS_OBJECT)
	protected Stopwords stopwordsObject;
	
	@ConfigurationParameter(name = PARAM_NAME_REMOVE_STOPWORDS, defaultValue="true")
	protected boolean removeStopwords;
	
	@ConfigurationParameter(name = PARAM_NAME_MIN_N_GRAM_SIZE, mandatory=true)
	protected int minNGramSize;
	
	@ConfigurationParameter(name = PARAM_NAME_MAX_N_GRAM_SIZE, mandatory=true)
	protected int maxNGramSize;
	
	protected BowProvider getBowProvider(JCas cas, String parameterList) {
		
		Alphabet alphabet = new Alphabet(); //what is this? Do we ever invoke it with parameters?
		BowProvider bowProvider = null;
		
		if (removeStopwords) {
			bowProvider = new BowProvider(alphabet, parameterList, minNGramSize, maxNGramSize, stopwordsObject);
		} else {
			bowProvider = new BowProvider(alphabet, parameterList, minNGramSize, maxNGramSize, new Stopwords());
		}
		
		return bowProvider;
	}
	
}

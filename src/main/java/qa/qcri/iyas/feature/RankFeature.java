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

package qa.qcri.iyas.feature;

import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.uimafit.util.JCasUtil;

import qa.qcri.iyas.data.preprocessing.JCasPairGenerator;
import qa.qcri.iyas.type.cqa.Comment;
import qa.qcri.iyas.type.cqa.RelatedQuestionBody;

public class RankFeature extends ThreadSafeFeature {

	@Override
	public double getValue(JCas jcas) throws UIMAException {
		JCas rightJCas = jcas.getView(JCasPairGenerator.RIGHT_CAS_VIEW);
		
		int rank;
		if (JCasUtil.exists(rightJCas, Comment.class))
			rank = JCasUtil.select(rightJCas, Comment.class).iterator().next().getRank();
		else
			rank = JCasUtil.select(rightJCas, RelatedQuestionBody.class).iterator().next().getRank();
		
		if (rank == -1)
			throw new UIMAException("Rank attribute not found", null);
		
		return 1.0/rank;
	}

}

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
 
 
package qa.qcri.iyas.util;

import java.util.LinkedList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCreationUtils;

import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.CasCopier;

public class AggregatedJCasManagerTaskA extends AggregatedJCasManager{

	private boolean concatenated;
	private JCas relatedQuestionSubject;
	private JCas relatedQuestionBody;
	private List<JCas> comments = new LinkedList<JCas>();
	
	public AggregatedJCasManagerTaskA(boolean concatenated) {
		this.concatenated = concatenated;
		relatedQuestionSubject = null;
		relatedQuestionBody = null;
	}
	
	/**
	 * Sets the {@link AggregatedJCasManagerTaskA#relatedQuestionBody} instance variable of this {@link AggregatedJCasManagerTaskA}.
	 * All the annotations and SofA are copied from the specified JCas.
	 * @param relatedQuestionBodyJCas
	 * @throws AnalysisEngineProcessException
	 * @throws ResourceInitializationException 
	 * @throws CASException 
	 */
	public void setRelatedQuestionBodyView(JCas relatedQuestionBodyJCas) throws AnalysisEngineProcessException, ResourceInitializationException, CASException {
		if (relatedQuestionBody != null)
			throw new AnalysisEngineProcessException(new IllegalStateException(
					"Related question body has already been set."));
		
		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null);
		CasCopier.copyCas(relatedQuestionBodyJCas.getCas(), cas, true);
		relatedQuestionBody = cas.getJCas();
	}
	
	/**
	 * Sets the {@link AggregatedJCasManagerTaskA#relatedQuestionSubject} instance variable of this {@link AggregatedJCasManagerTaskA}.
	 * All the annotations and SofA are copied from the specified JCas.
	 * @param relatedQuestionBodyJCas
	 * @throws AnalysisEngineProcessException
	 * @throws ResourceInitializationException 
	 * @throws CASException 
	 */
	public void setRelatedQuestionSubjectView(JCas rrelatedQuestionSubjectJCas) throws AnalysisEngineProcessException, ResourceInitializationException, CASException {
		if (relatedQuestionSubject != null)
			throw new AnalysisEngineProcessException(new IllegalStateException(
					"Related question subject has already been set."));
		
		if (concatenated)
			throw new AnalysisEngineProcessException(new IllegalStateException(
					"Related question subject is not allowed to be set since the flag \"concatenated\" is true."));
		
		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null);
		CasCopier.copyCas(rrelatedQuestionSubjectJCas.getCas(), cas, true);
		relatedQuestionSubject = cas.getJCas();
	}
	
	public void addCommentView(JCas comment) throws ResourceInitializationException, CASException {
		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null);
		CasCopier.copyCas(comment.getCas(), cas, true);
		comments.add(cas.getJCas());
	}
}

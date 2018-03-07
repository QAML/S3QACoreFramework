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

import java.util.Collection;
import java.util.Iterator;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import qa.qcri.iyas.type.cqa.QAAnnotation;

public abstract class AggregatedJCasManager {
	public static final String USER_QUESTION_BODY_VIEW = "UserQuestionBodyView";
	public static final String USER_QUESTION_SUBJECT_VIEW = "UserQuestionSubjectView";
	public static final String RELATED_QUESTION_VIEW = "RelatedQuestionView";
	public static final String RELATED_QUESTION_BODY_VIEW = "RelatedQuestionBodyView";
	public static final String RELATED_QUESTION_SUBJECT_VIEW = "RelatedQuestionSubjectView";
	public static final String COMMENT_VIEW = "CommentView";
		
//	protected <T extends TOP> T getAnnotation(JCas jcas,Class<T> clazz) throws UIMAException {
//		FSIterator<T> it = jcas.getAllIndexedFS(clazz);
//		if (!it.hasNext())
//			throw new UIMAException(new IllegalArgumentException(
//					"The specified JCas does not contain any "+clazz.getName()+" annotation"));
//		T annotation = it.next();
//		if (it.hasNext())
//			throw new UIMAException(new IllegalArgumentException(
//					"Only one annotation is expected "+clazz.getName()));
//		
//		return annotation;
//	}
	
	protected <T extends QAAnnotation> T getAnnotation(JCas jcas,Class<T> clazz) throws UIMAException {
		Collection<T> annotations = JCasUtil.select(jcas, clazz);
		Iterator<T> it = annotations.iterator();
		if (!it.hasNext())
			throw new UIMAException(new IllegalArgumentException(
					"The specified JCas does not contain any "+clazz.getName()+" annotation"));
		T annotation = it.next();
		
		if (it.hasNext())
			throw new UIMAException(new IllegalArgumentException(
					"Only one annotation is expected "+clazz.getName()));
		
		return annotation;
	}
	
	protected <T extends QAAnnotation> T checkAnnotationExistence(JCas jcas,Class<T> clazz) throws UIMAException {
		Collection<T> annotations = JCasUtil.select(jcas, clazz);
		Iterator<T> it = annotations.iterator();
				if (!it.hasNext())
			throw new UIMAException(new IllegalArgumentException(
					"The specified JCas does not contain any "+clazz.getName()+" annotation"));
		T annotation = it.next();
		
		if (it.hasNext())
			throw new UIMAException(new IllegalArgumentException(
					"Only one annotation is expected "+clazz.getName()));
		
		return annotation;
	}
	
	public abstract void getAggregatedJCas(JCas jcas) throws UIMAException;
	public abstract boolean isReady() throws UIMAException;
}

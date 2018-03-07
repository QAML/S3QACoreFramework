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
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.UIMAException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.util.CasCopier;
import org.apache.uima.util.CasCreationUtils;
import org.uimafit.util.JCasUtil;

import qa.qcri.iyas.type.cqa.Comment;
import qa.qcri.iyas.type.cqa.InstanceA;
import qa.qcri.iyas.type.cqa.RelatedQuestion;
import qa.qcri.iyas.type.cqa.RelatedQuestionBody;
import qa.qcri.iyas.type.cqa.RelatedQuestionSubject;

public class AggregatedJCasManagerTaskA extends AggregatedJCasManager {

	private boolean concatenated;
	private JCas relatedQuestionSubjectJCas;
	private JCas relatedQuestionBodyJCas;
	private Map<String,JCas> comments;
	private JCas aggregatedJCas;
	
	public AggregatedJCasManagerTaskA() {
		this.concatenated = false;
		this.relatedQuestionSubjectJCas = null;
		this.relatedQuestionBodyJCas = null;
		this.comments = new HashMap<>();
	}
	
	public AggregatedJCasManagerTaskA(JCas jcas) throws UIMAException {
		if (!JCasUtil.exists(jcas, InstanceA.class))
			throw new UIMAException("No InstanceA annotation found.", null);
		this.aggregatedJCas = jcas;
		
		Collection<RelatedQuestion> relatedQuestions = JCasUtil.select(aggregatedJCas, RelatedQuestion.class);
		if (relatedQuestions.size() > 1)
			throw new UIMAException("Only one RelatedQuestion annotation is allowed",null);
		if (relatedQuestions.size() == 0)
			throw new UIMAException("Expected one RelatedQuestion annotation",null);
		
		RelatedQuestion relatedQuestion = relatedQuestions.iterator().next();
		
		this.concatenated = relatedQuestion.getConcatenated();
		if (!concatenated) {
			if (!JCasUtil.exists(aggregatedJCas.getView(RELATED_QUESTION_SUBJECT_VIEW), RelatedQuestionSubject.class))
				throw new UIMAException("RelatedQuestionSubject annotation not found", null);
			this.relatedQuestionSubjectJCas = aggregatedJCas.getView(RELATED_QUESTION_SUBJECT_VIEW);
		}
		if (!JCasUtil.exists(aggregatedJCas.getView(RELATED_QUESTION_BODY_VIEW), RelatedQuestionBody.class))
			throw new UIMAException("RelatedQuestionBody annotation not found", null);
		this.relatedQuestionBodyJCas = aggregatedJCas.getView(RELATED_QUESTION_BODY_VIEW);
		
		this.comments = new HashMap<>();
		
		for (String cID : relatedQuestion.getCandidateIDs().toArray()) {
			if (!JCasUtil.exists(aggregatedJCas.getView(COMMENT_VIEW+"-"+cID), Comment.class))
				throw new UIMAException("Comment annotation not found", null);
			comments.put(cID, aggregatedJCas.getView(COMMENT_VIEW+"-"+cID));
		}
	}
	
	public boolean isConcatenated() {
		return concatenated;
	}
	
	public JCas getRelatedQuestionBodyJCas() {
		return relatedQuestionBodyJCas;
	}
	
	public Collection<JCas> getCandidatesJCases() {
		return this.comments.values();
	}
	
	/**
	 * Sets the {@link AggregatedJCasManagerTaskA#relatedQuestionBodyJCas} instance variable of this {@link AggregatedJCasManagerTaskA}.
	 * All the annotations and SofA are copied from the specified JCas.
	 * @param relatedQuestionBody
	 * @return true if the aggregated JCas is ready
	 * @throws UIMAException
	 */
	public synchronized boolean setRelatedQuestionBodyView(JCas relatedQuestionBody) throws UIMAException {
		if (this.relatedQuestionBodyJCas != null)
			throw new UIMAException(new IllegalStateException(
					"Related question body has already been set."));
		
		if (!JCasUtil.exists(relatedQuestionBody, InstanceA.class))
			throw new UIMAException(new IllegalStateException(
					"The input JCas is supposed to contain an InstanceA annotation."));
		
		//Called also to check that there is only a RelatedQuestionBody as QAAnnotation
		RelatedQuestionBody body = getAnnotation(relatedQuestionBody,RelatedQuestionBody.class);
		validateRelatedQuestionBodyID(body);
		
		if (this.relatedQuestionSubjectJCas != null && body.getConcatenated())
			throw new UIMAException(new IllegalStateException(
					"RelatedQuestionBody is set to be concatenated while a RelatedQuestionSubject has already been received."));
		
		if (this.comments.size() > body.getNumberOfCandidates())
			throw new UIMAException(new IllegalStateException(
					"More comments than expeted have already been received."));
		
//		System.out.println("Received "+body.getID()+"'s body");
		
		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null);
		CasCopier.copyCas(relatedQuestionBody.getCas(), cas, true);
		this.relatedQuestionBodyJCas = cas.getJCas();
		this.concatenated = body.getConcatenated();
		
		JCasUtil.select(cas.getJCas(), InstanceA.class).iterator().next().removeFromIndexes(cas.getJCas());
		
		return isReady();
	}
	
	/**
	 * Sets the {@link AggregatedJCasManagerTaskA#relatedQuestionSubjectJCas} instance variable of this {@link AggregatedJCasManagerTaskA}.
	 * All the annotations and SofA are copied from the specified JCas.
	 * @param relatedQuestionBodyJCas
	 * @return true if the aggregated JCas is ready
	 * @throws UIMAException
	 */
	public synchronized boolean setRelatedQuestionSubjectView(JCas relatedQuestionSubject) throws UIMAException {
		if (this.relatedQuestionSubjectJCas != null)
			throw new UIMAException(new IllegalStateException(
					"Related question subject has already been set."));
		
		if (this.concatenated)
			throw new UIMAException(new IllegalStateException(
					"Related question subject is not allowed to be set since the flag \"concatenated\" is true."));
		
		if (!JCasUtil.exists(relatedQuestionSubject, InstanceA.class))
			throw new UIMAException(new IllegalStateException(
					"The input JCas is supposed to contain an InstanceA annotation."));
		
		//Called also to check that there is only a RelatedQuestionSubject as QAAnnotation
		RelatedQuestionSubject subject = getAnnotation(relatedQuestionSubject,RelatedQuestionSubject.class);
		validateRelatedQuestionSubjectID(subject);
			
//		System.out.println("Received "+subject.getID()+"'s subject");

		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null);
		CasCopier.copyCas(relatedQuestionSubject.getCas(), cas, true);
		this.relatedQuestionSubjectJCas = cas.getJCas();
		
		JCasUtil.select(cas.getJCas(), InstanceA.class).iterator().next().removeFromIndexes(cas.getJCas());
		
		return isReady();
	}
	
	public synchronized boolean addCommentView(JCas comment) throws UIMAException {
		if (this.relatedQuestionBodyJCas != null) {
			RelatedQuestionBody body = getAnnotation(this.relatedQuestionBodyJCas,RelatedQuestionBody.class);
			if (body.getNumberOfCandidates() == this.comments.size()) {
				throw new UIMAException(new IllegalStateException(
					"The number of expected comments has already been reached."));
			}
		}
		
		if (!JCasUtil.exists(comment, InstanceA.class))
			throw new UIMAException(new IllegalStateException(
					"The input JCas is supposed to contain an InstanceA annotation."));
		
		//Called also to check that there is only a Comment as QAAnnotation
		Comment comm = getAnnotation(comment,Comment.class);
		validateCommentID(comm);
		
		if (comments.get(comm.getID()) != null)
			throw new UIMAException(new IllegalStateException(
					"The comment "+comm.getID()+" has already been reached."));
		
//		System.out.println("Received comment "+comm.getID());
		
		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null);
		CasCopier.copyCas(comment.getCas(), cas, true);
		this.comments.put(comm.getID(), cas.getJCas());
		
		JCasUtil.select(cas.getJCas(), InstanceA.class).iterator().next().removeFromIndexes(cas.getJCas());
		
		return isReady();
	}
	
	@Override
	public synchronized void getAggregatedJCas(JCas jcas) throws UIMAException {
		if (!isReady())
			throw new UIMAException("Not ready, some JCas is still missing",null);
		
		RelatedQuestionBody relQuestBody = getAnnotation(this.relatedQuestionBodyJCas, RelatedQuestionBody.class);
		
		RelatedQuestion relatedQuestion = new RelatedQuestion(jcas);
		relatedQuestion.setConcatenated(this.concatenated);
		relatedQuestion.setCandidateIDs(new StringArray(jcas, relQuestBody.getNumberOfCandidates()));
		relatedQuestion.setID(relQuestBody.getID());
		
		
		CasCopier copier = new CasCopier(this.relatedQuestionBodyJCas.getCas(), jcas.getCas());
		copier.copyCasView(this.relatedQuestionBodyJCas.getCas(), jcas.getCas().createView(RELATED_QUESTION_BODY_VIEW), true);
		relatedQuestionBodyJCas.release();
		
		if (!this.concatenated) {
			copier = new CasCopier(this.relatedQuestionSubjectJCas.getCas(), jcas.getCas());
			copier.copyCasView(this.relatedQuestionSubjectJCas.getCas(), jcas.getCas().createView(RELATED_QUESTION_SUBJECT_VIEW), true);
			relatedQuestionSubjectJCas.release();
		}
		
		int j = 0;
		for (String commentID : this.comments.keySet()) {
			copier = new CasCopier(this.comments.get(commentID).getCas(), jcas.getCas());
			copier.copyCasView(this.comments.get(commentID).getCas(), jcas.getCas().createView(COMMENT_VIEW+"-"+commentID), true);
			this.comments.get(commentID).release();
			
			relatedQuestion.getCandidateIDs().set(j++,commentID);
		}
		
		relatedQuestion.addToIndexes();
		InstanceA instanceA = new InstanceA(jcas);
		instanceA.addToIndexes();
	}
	
	@Override
	public synchronized boolean isReady() throws UIMAException {
		if (this.relatedQuestionBodyJCas == null || (!this.concatenated) && this.relatedQuestionSubjectJCas == null)
			return false;
		
		RelatedQuestionBody relQuestBody = getAnnotation(this.relatedQuestionBodyJCas, RelatedQuestionBody.class);
		if (this.comments.size() != relQuestBody.getNumberOfCandidates())
			return false;
		
		return true;
	}
	
	private void validateRelatedQuestionBodyID(RelatedQuestionBody relatedQuestionBody) throws UIMAException {		
		String relatedQuestionID = relatedQuestionBody.getID();
		if (relatedQuestionID.split("_").length != 2)
			throw new UIMAException(new IllegalArgumentException("Malformed ID."));
		
		if (this.relatedQuestionSubjectJCas != null &&
				!relatedQuestionID.equals(getAnnotation(this.relatedQuestionSubjectJCas, RelatedQuestionSubject.class).getID()))
			throw new UIMAException(new IllegalArgumentException("The ID of the input related question body is not "
					+ "consistent with the already received related question subject."));
		else 
			if (!this.comments.isEmpty()) {
			String commentID = getAnnotation(this.comments.values().iterator().next(), Comment.class).getID();
			String split[] = commentID.split("_");
			String id = split[0]+"_"+split[1];
			if (!relatedQuestionID.equals(id))
				throw new UIMAException(new IllegalArgumentException("The ID of the input related question body is not "
						+ "consistent with the already comments."));
		}
	}
	
	private void validateRelatedQuestionSubjectID(RelatedQuestionSubject relatedQuestionSubject) throws UIMAException {		
		String relatedQuestionID = relatedQuestionSubject.getID();
		if (relatedQuestionID.split("_").length != 2)
			throw new UIMAException(new IllegalArgumentException("Malformed ID."));
		
		if (this.relatedQuestionBodyJCas != null &&
				!relatedQuestionID.equals(getAnnotation(relatedQuestionBodyJCas, RelatedQuestionBody.class).getID()))
			throw new UIMAException(new IllegalArgumentException("The ID of the input related question subject is not "
					+ "consistent with the already received related question body."));
		else
			if (!this.comments.isEmpty()) {
				String commentID = getAnnotation(this.comments.values().iterator().next(), Comment.class).getID();
				String split[] = commentID.split("_");
				String id = split[0]+"_"+split[1];
				if (!relatedQuestionID.equals(id))
					throw new UIMAException(new IllegalArgumentException("The ID of the input related question subject is not "
							+ "consistent with the already comments."));
		}
	}
	
	private void validateCommentID(Comment comment) throws UIMAException {		
		String split[] = comment.getID().split("_");
		if (split.length != 3)
			throw new UIMAException(new IllegalArgumentException("Malformed ID."));
		
		String relatedQuestionID = split[0]+"_"+split[1];
		
		if (this.relatedQuestionBodyJCas != null && 
				!relatedQuestionID.equals(getAnnotation(this.relatedQuestionBodyJCas, RelatedQuestionBody.class).getID()))
			throw new UIMAException(new IllegalArgumentException("The ID of the input comment is not "
					+ "consistent with the already received related question body."));
		
		if (this.relatedQuestionSubjectJCas != null &&
				!relatedQuestionID.equals(getAnnotation(this.relatedQuestionSubjectJCas, RelatedQuestionSubject.class).getID()))
			throw new UIMAException(new IllegalArgumentException("The ID of the input comment is not "
					+ "consistent with the already received related question subject."));
		
		if (!this.comments.isEmpty()) {
			String commentID = getAnnotation(this.comments.values().iterator().next(), Comment.class).getID();
			String split2[] = commentID.split("_");
			String id = split2[0]+"_"+split2[1];
			if (!relatedQuestionID.equals(id))
				throw new UIMAException(new IllegalArgumentException("The ID of the input comment is not "
						+ "consistent with the already received comments. Received ID "+relatedQuestionID+", expected ID "+(split2[0]+"_"+split2[1])));
		}
 		
	}
}

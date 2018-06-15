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

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.fit.component.JCasMultiplier_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.descriptor.OperationalProperties;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderJDOMFactory;
import org.jdom2.input.sax.XMLReaderXSDFactory;
import org.uimafit.util.JCasUtil;

import qa.qcri.iyas.data.reader.DataReader;
import qa.qcri.iyas.type.AdditionalInfo;
import qa.qcri.iyas.type.cqa.Comment;
import qa.qcri.iyas.type.cqa.InstanceA;
import qa.qcri.iyas.type.cqa.InstanceB;
import qa.qcri.iyas.type.cqa.InstanceC;
import qa.qcri.iyas.type.cqa.RelatedQuestionBody;
import qa.qcri.iyas.type.cqa.RelatedQuestionSubject;
import qa.qcri.iyas.type.cqa.UserQuestionBody;
import qa.qcri.iyas.type.cqa.UserQuestionSubject;

@OperationalProperties(modifiesCas = true, outputsNewCases = true, multipleDeploymentAllowed = true)
@TypeCapability(
		inputs = {"qa.qcri.iyas.types.UserQuestionSubject",
				   "qa.qcri.iyas.types.UserQuestionBody",
				   "qa.qcri.iyas.types.RelatedQuestionSubject",
				   "qa.qcri.iyas.types.RelatedQuestionBody",
				   "qa.qcri.iyas.types.Comment"},
		
		outputs = {"qa.qcri.iyas.types.UserQuestionSubject",
				   "qa.qcri.iyas.types.UserQuestionBody",
				   "qa.qcri.iyas.types.RelatedQuestionSubject",
				   "qa.qcri.iyas.types.RelatedQuestionBody",
				   "qa.qcri.iyas.types.Comment"}
)
public class InputJCasMultiplier extends JCasMultiplier_ImplBase {
	
	//TODO release the CASes in case of error
	private static enum Status{USER_QUESTION_SUBJECT,USER_QUESTION_BODY,RELATED_QUESTION_SUBJECT,RELATED_QUESTION_BODY,COMMENT};
	private Status status = null;
	private static enum TypeOfInstance{INSTANCE_A,INSTANCE_B,INSTANCE_C};
	
	public static final String PREPROCESSOR_EXTERNAL_RESOURCE = "preprocessor";
	public static final String CONCATENATE_PARAM = "concatenate";
	
	private LinkedList<MyElement> currentElements = null;
	private SAXBuilder saxBuilder = null;
	
	@ExternalResource(key = PREPROCESSOR_EXTERNAL_RESOURCE)
	private TextPreprocessor preprocessor = null;
	
	@ConfigurationParameter(name = CONCATENATE_PARAM, defaultValue = "false", mandatory = false)
	private Boolean concatenateSubjectAndBody;
	
	private class MyElement {
		public TypeOfInstance typeOfInstance;
		public Element element;
		public String requestererID;
		public MyElement(Element element,TypeOfInstance typeOfInstance,String rID) {
			this.element = element;
			this.typeOfInstance = typeOfInstance;
			this.requestererID = rID;
		}
		public String toString() {
			return element.getName()+" "+typeOfInstance;
		}
	}
	
	private void parseInstance(Element instance,String requestererID) throws AnalysisEngineProcessException {
		if (instance.getName().equals(DataReader.INSTANCE_A_TAG)) {
			Element relatedQuestion = instance.getChild(DataReader.RELATED_QUESTION_TAG);
			String relatedQuestionID = relatedQuestion.getAttributeValue(DataReader.ID_ATTRIBUTE);
			if (relatedQuestionID.split("_").length != 2)
				throw new AnalysisEngineProcessException("The related question ID does not satisfy the requirements",null);
			currentElements.addLast(new MyElement(relatedQuestion,TypeOfInstance.INSTANCE_A,requestererID));
			for (Element comment : relatedQuestion.getChildren(DataReader.COMMENT_TAG)) {
				String commentID = comment.getAttributeValue(DataReader.ID_ATTRIBUTE);
				if (commentID.split("_").length != 3 || !commentID.startsWith(relatedQuestionID))
					throw new AnalysisEngineProcessException("The comment question ID does not satisfy the requirements",null);
				currentElements.addLast(new MyElement(comment,TypeOfInstance.INSTANCE_A,requestererID));
			}
			status = Status.RELATED_QUESTION_SUBJECT;
		} else if (instance.getName().equals(DataReader.INSTANCE_B_TAG)) {
			Element userQuestion = instance.getChild(DataReader.USER_QUESTION_TAG);
			currentElements.addLast(new MyElement(userQuestion,TypeOfInstance.INSTANCE_B,requestererID));
			
			String userQuestionID = userQuestion.getAttributeValue(DataReader.ID_ATTRIBUTE);
			if (userQuestionID.split("_").length != 1)
				throw new AnalysisEngineProcessException("The user question ID does not satisfy the requirements",null);
			for (Element relatedQuestion : instance.getChildren(DataReader.RELATED_QUESTION_TAG)) {
				String relatedQuestionID = relatedQuestion.getAttributeValue(DataReader.ID_ATTRIBUTE);
				if (relatedQuestionID.split("_").length != 2 || !relatedQuestionID.startsWith(userQuestionID))
					throw new AnalysisEngineProcessException("The related question ID does not satisfy the requirements",null);
				currentElements.addLast(new MyElement(relatedQuestion,TypeOfInstance.INSTANCE_B,requestererID));
			}
			status = Status.USER_QUESTION_SUBJECT;
		} else if (instance.getName().equals(DataReader.INSTANCE_C_TAG)) {
			Element userQuestion = instance.getChild(DataReader.USER_QUESTION_TAG);
			currentElements.addLast(new MyElement(userQuestion,TypeOfInstance.INSTANCE_C,requestererID));
			
			String userQuestionID = userQuestion.getAttributeValue(DataReader.ID_ATTRIBUTE);
			if (userQuestionID.split("_").length != 1)
				throw new AnalysisEngineProcessException("The user question ID does not satisfy the requirements",null);
			for (Element relatedQuestion : instance.getChildren(DataReader.RELATED_QUESTION_TAG)) {
				String relatedQuestionID = relatedQuestion.getAttributeValue(DataReader.ID_ATTRIBUTE);
				if (relatedQuestionID.split("_").length != 2 || !relatedQuestionID.startsWith(userQuestionID))
					throw new AnalysisEngineProcessException("The related question ID does not satisfy the requirements",null);
				currentElements.addLast(new MyElement(relatedQuestion,TypeOfInstance.INSTANCE_C,requestererID));
				for (Element comment : relatedQuestion.getChildren(DataReader.COMMENT_TAG)) {
					String commentID = comment.getAttributeValue(DataReader.ID_ATTRIBUTE);
					if (commentID.split("_").length != 3 || !commentID.startsWith(relatedQuestionID))
						throw new AnalysisEngineProcessException("The comment question ID does not satisfy the requirements",null);
					currentElements.addLast(new MyElement(comment,TypeOfInstance.INSTANCE_C,requestererID));
				}
			}
			status = Status.USER_QUESTION_SUBJECT;
		}
	}
	
	@Override
	public void initialize(final UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		XMLReaderJDOMFactory factory;
		try {
			factory = new XMLReaderXSDFactory(InputJCasMultiplier.class.getResource(DataReader.SCHEMA_PATH));
			saxBuilder = new SAXBuilder(factory);
		} catch (JDOMException e) {
			e.printStackTrace();
			throw new ResourceInitializationException(e.getMessage(), null);
		}
		currentElements = new LinkedList<MyElement>();
	}
	
	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {	
		try {
			Collection<AdditionalInfo> infos = JCasUtil.select(jcas, AdditionalInfo.class);
			if (infos.size() != 1)
				throw new AnalysisEngineProcessException("Expected an AdditionalInfo annotation, found "+infos.size(),null);
			
			AdditionalInfo info = infos.iterator().next();
			
			if (info.getRequesterID() == null)
				throw new AnalysisEngineProcessException("Requerer Id not set", null);
			
			String requesterID = info.getRequesterID();
			info.removeFromIndexes();
			
			Document document = saxBuilder.build(new StringReader(jcas.getDocumentText()));
			Element root = document.getRootElement();
			Element instance = root.getChildren().get(0);
			parseInstance(instance,requesterID);
			
		} catch (IOException | JDOMException e) {
			e.printStackTrace();
			System.out.println(jcas.getDocumentText());
			throw new AnalysisEngineProcessException(e);
		}
	}

	@Override
	public boolean hasNext() throws AnalysisEngineProcessException {
		return !currentElements.isEmpty();
	}
	
	private JCas getUserQuestion(Element userQuestion,String requesterID) {
		String id = userQuestion.getAttributeValue(DataReader.ID_ATTRIBUTE);
		String lang = userQuestion.getAttributeValue(DataReader.LANG_ATTRIBUTE);
		int numberOfCandidates = Integer.parseInt(userQuestion.getAttributeValue(DataReader.NUMBER_OF_CANDIDATES_ATTRIBUTE));
		
		String subject = preprocessor.preprocess(userQuestion.getChild(DataReader.SUBJECT_TAG).getText(),lang);
		String body = preprocessor.preprocess(userQuestion.getChild(DataReader.BODY_TAG).getText(),lang);
		String question = preprocessor.concatenateBodyAndSubject(subject, body, true);
		
		JCas questionJCas = getEmptyJCas();
		questionJCas.setDocumentLanguage(lang);
		questionJCas.setDocumentText(question);
		UserQuestionBody questionAnnotation = new UserQuestionBody(questionJCas, 0, question.length());
		questionAnnotation.setNumberOfCandidates(numberOfCandidates);
		questionAnnotation.setID(id);
		questionAnnotation.setConcatenated(true);
		questionAnnotation.addToIndexes();
		
		AdditionalInfo info = new AdditionalInfo(questionJCas);
		info.setRequesterID(requesterID);
		info.setIndex(-1);
		info.setTotalNumberOfExamples(-1);
		info.addToIndexes();
		
		return questionJCas;
	}
	
	private JCas getUserQuestionSubject(Element userQuestion,String requesterID) {
		String id = userQuestion.getAttributeValue(DataReader.ID_ATTRIBUTE);
		String lang = userQuestion.getAttributeValue(DataReader.LANG_ATTRIBUTE);
		
		String subject = preprocessor.preprocess(userQuestion.getChild(DataReader.SUBJECT_TAG).getText(),lang);
		JCas subjectJCas = getEmptyJCas();
		subjectJCas.setDocumentLanguage(lang);
		subjectJCas.setDocumentText(subject);
		UserQuestionSubject subjectAnnotation = new UserQuestionSubject(subjectJCas, 0, subject.length());
		subjectAnnotation.setID(id);
		subjectAnnotation.addToIndexes();
		
		AdditionalInfo info = new AdditionalInfo(subjectJCas);
		info.setRequesterID(requesterID);
		info.setIndex(-1);
		info.setTotalNumberOfExamples(-1);
		info.addToIndexes();
		
		return subjectJCas;
	}
	
	private JCas getUserQuestionBody(Element userQuestion,String requesterID) {
		String id = userQuestion.getAttributeValue(DataReader.ID_ATTRIBUTE);
		String lang = userQuestion.getAttributeValue(DataReader.LANG_ATTRIBUTE);
		int numberOfCandidates = Integer.parseInt(userQuestion.getAttributeValue(DataReader.NUMBER_OF_CANDIDATES_ATTRIBUTE));
		
		String body = preprocessor.preprocess(userQuestion.getChild(DataReader.BODY_TAG).getText(),lang);
		JCas bodyJCas = getEmptyJCas();
		bodyJCas.setDocumentLanguage(lang);
		bodyJCas.setDocumentText(body);
		UserQuestionBody bodyAnnotation = new UserQuestionBody(bodyJCas, 0, body.length());
		bodyAnnotation.setNumberOfCandidates(numberOfCandidates);
		bodyAnnotation.setID(id);
		bodyAnnotation.addToIndexes();
		
		AdditionalInfo info = new AdditionalInfo(bodyJCas);
		info.setRequesterID(requesterID);
		info.setIndex(-1);
		info.setTotalNumberOfExamples(-1);
		info.addToIndexes();
		
		return bodyJCas;
	}
	
	private JCas getRelatedQuestion(Element relatedQuestion,String requesterID) {
		String id = relatedQuestion.getAttributeValue(DataReader.ID_ATTRIBUTE);
		String lang = relatedQuestion.getAttributeValue(DataReader.LANG_ATTRIBUTE);
		String relevance = relatedQuestion.getAttributeValue(DataReader.RELEVANCE_ATTRIBUTE);
		int numberOfCandidates = -1;
		if (relatedQuestion.getAttributeValue(DataReader.NUMBER_OF_CANDIDATES_ATTRIBUTE) != null)//In the case of task B related_question does not have this attr.
			numberOfCandidates = Integer.parseInt(relatedQuestion.getAttributeValue(DataReader.NUMBER_OF_CANDIDATES_ATTRIBUTE));
		int index = -1;
		if (relatedQuestion.getAttributeValue(DataReader.INDEX_ATTRIBUTE) != null) 
			index = Integer.parseInt(relatedQuestion.getAttributeValue(DataReader.INDEX_ATTRIBUTE));
		int totalExamples = -1;
		if (relatedQuestion.getAttributeValue(DataReader.TOTAL_NUM_OF_EXAMPLES_ATTRIBUTE) != null) 
			totalExamples = Integer.parseInt(relatedQuestion.getAttributeValue(DataReader.TOTAL_NUM_OF_EXAMPLES_ATTRIBUTE));
		int rank = -1;
		if (relatedQuestion.getAttributeValue(DataReader.RANK_ATTRIBUTE) != null) 
			rank = Integer.parseInt(relatedQuestion.getAttributeValue(DataReader.RANK_ATTRIBUTE));
		
		
		String subject = preprocessor.preprocess(relatedQuestion.getChild(DataReader.SUBJECT_TAG).getText(),lang);
		String body = preprocessor.preprocess(relatedQuestion.getChild(DataReader.BODY_TAG).getText(),lang);
		String question = preprocessor.concatenateBodyAndSubject(subject, body, true);
		
		JCas questionJCas = getEmptyJCas();
		questionJCas.setDocumentLanguage(lang);
		questionJCas.setDocumentText(question);
		
		RelatedQuestionBody questionAnnotation = new RelatedQuestionBody(questionJCas, 0, question.length());
		questionAnnotation.setNumberOfCandidates(numberOfCandidates);
		questionAnnotation.setID(id);
		questionAnnotation.setConcatenated(true);
		questionAnnotation.setRank(rank);
		StringArray labels = new StringArray(questionJCas, 1);
		if (relevance != null)
			labels.set(0, relevance);
		else
			labels.set(0, "?");
		labels.addToIndexes();
		questionAnnotation.setLabels(labels);
		questionAnnotation.addToIndexes();
		
		AdditionalInfo info = new AdditionalInfo(questionJCas);
		info.setIndex(index);
		info.setTotalNumberOfExamples(totalExamples);
		info.setRequesterID(requesterID);
		info.addToIndexes();
		
		return questionJCas;
	}
	
	private JCas getRelatedQuestionSubject(Element relatedQuestion,String requesterID) {
		String id = relatedQuestion.getAttributeValue(DataReader.ID_ATTRIBUTE);
		String lang = relatedQuestion.getAttributeValue(DataReader.LANG_ATTRIBUTE);
		
		String subject = preprocessor.preprocess(relatedQuestion.getChild(DataReader.SUBJECT_TAG).getText(),lang);
		JCas subjectJCas = getEmptyJCas();
		subjectJCas.setDocumentLanguage(lang);
		subjectJCas.setDocumentText(subject);
		RelatedQuestionSubject subjectAnnotation = new RelatedQuestionSubject(subjectJCas, 0, subject.length());
		subjectAnnotation.setID(id);
		subjectAnnotation.addToIndexes();
		
		AdditionalInfo info = new AdditionalInfo(subjectJCas);
		info.setIndex(-1);
		info.setTotalNumberOfExamples(-1);
		info.setRequesterID(requesterID);
		info.addToIndexes();
		
		return subjectJCas;
	}
	
	private JCas getRelatedQuestionBody(Element relatedQuestion,String requesterID) {
		String id = relatedQuestion.getAttributeValue(DataReader.ID_ATTRIBUTE);
		String lang = relatedQuestion.getAttributeValue(DataReader.LANG_ATTRIBUTE);
		int numberOfCandidates = Integer.parseInt(relatedQuestion.getAttributeValue(DataReader.NUMBER_OF_CANDIDATES_ATTRIBUTE));
		int index = Integer.parseInt(relatedQuestion.getAttributeValue(DataReader.INDEX_ATTRIBUTE));
		int totalExamples = Integer.parseInt(relatedQuestion.getAttributeValue(DataReader.TOTAL_NUM_OF_EXAMPLES_ATTRIBUTE));
		int rank = -1;
		if (relatedQuestion.getAttributeValue(DataReader.RANK_ATTRIBUTE) != null) 
			rank = Integer.parseInt(relatedQuestion.getAttributeValue(DataReader.RANK_ATTRIBUTE));
		
		String body = preprocessor.preprocess(relatedQuestion.getChild(DataReader.BODY_TAG).getText(),lang);
		JCas bodyJCas = getEmptyJCas();
		bodyJCas.setDocumentLanguage(lang);
		bodyJCas.setDocumentText(body);
		RelatedQuestionBody bodyAnnotation = new RelatedQuestionBody(bodyJCas, 0, body.length());
		bodyAnnotation.setNumberOfCandidates(numberOfCandidates);
		bodyAnnotation.setID(id);
		bodyAnnotation.setRank(rank);
		bodyAnnotation.addToIndexes();
		
		AdditionalInfo info = new AdditionalInfo(bodyJCas);
		info.setIndex(index);
		info.setTotalNumberOfExamples(totalExamples);
		info.setRequesterID(requesterID);
		info.addToIndexes();
		
		return bodyJCas;
	}
	
	private JCas getComment(Element comment,String requesterID) {
		String id = comment.getAttributeValue(DataReader.ID_ATTRIBUTE);
		String lang = comment.getAttributeValue(DataReader.LANG_ATTRIBUTE);
		String relevance = comment.getAttributeValue(DataReader.RELEVANCE_ATTRIBUTE);
		int index = Integer.parseInt(comment.getAttributeValue(DataReader.INDEX_ATTRIBUTE));
		int totalExamples = Integer.parseInt(comment.getAttributeValue(DataReader.TOTAL_NUM_OF_EXAMPLES_ATTRIBUTE));
		int rank = -1;
		if (comment.getAttributeValue(DataReader.RANK_ATTRIBUTE) != null) 
			rank = Integer.parseInt(comment.getAttributeValue(DataReader.RANK_ATTRIBUTE));
		
		String commentText = preprocessor.preprocess(comment.getText(),lang);
		JCas commentJCas = getEmptyJCas();
		commentJCas.setDocumentLanguage(lang);
		commentJCas.setDocumentText(commentText);
		Comment commentAnnotation = new Comment(commentJCas, 0, commentText.length());
		commentAnnotation.setID(id);
		commentAnnotation.setRank(rank);
		StringArray labels = new StringArray(commentJCas, 1);
		if (relevance != null)
			labels.set(0, relevance);
		else
			labels.set(0, "?");
		labels.addToIndexes();
		commentAnnotation.setLabels(labels);
		commentAnnotation.addToIndexes();
		
		AdditionalInfo info = new AdditionalInfo(commentJCas);
		info.setIndex(index);
		info.setTotalNumberOfExamples(totalExamples);
		info.setRequesterID(requesterID);
		info.addToIndexes();
		
		return commentJCas;
	}
	

	@Override
	public AbstractCas next() throws AnalysisEngineProcessException {
		MyElement next = currentElements.getFirst();
		
		JCas jcas = null;
		if (concatenateSubjectAndBody) {
			if (status == Status.USER_QUESTION_SUBJECT && next.element.getName().equals(DataReader.USER_QUESTION_TAG)) {
				jcas = getUserQuestion(next.element,next.requestererID);
				currentElements.removeFirst();
				status = Status.RELATED_QUESTION_SUBJECT;
			} else if (status == Status.RELATED_QUESTION_SUBJECT && next.element.getName().equals(DataReader.RELATED_QUESTION_TAG)) {
				jcas = getRelatedQuestion(next.element,next.requestererID);
				currentElements.removeFirst();
				if (next.typeOfInstance != TypeOfInstance.INSTANCE_B)
					status = Status.COMMENT;
				else {
					if (currentElements.isEmpty())
						status = null;
					else if (currentElements.getFirst().element.getName().equals(DataReader.RELATED_QUESTION_TAG))
						status = Status.RELATED_QUESTION_SUBJECT;
				}
			} else if (status == Status.COMMENT && next.element.getName().equals(DataReader.COMMENT_TAG)) {
				jcas = getComment(next.element,next.requestererID);
				currentElements.removeFirst();
				
				if (currentElements.isEmpty())
					status = null;
				else if (currentElements.getFirst().element.getName().equals(DataReader.RELATED_QUESTION_TAG))
					status = Status.RELATED_QUESTION_SUBJECT;
			} else {
				throw new AnalysisEngineProcessException(status+" "+next.element.getName(),null);
			}
		} else {
			if (status == Status.USER_QUESTION_SUBJECT && next.element.getName().equals(DataReader.USER_QUESTION_TAG)) {
				jcas = getUserQuestionSubject(next.element,next.requestererID);
				status = Status.USER_QUESTION_BODY;
			} else if (status == Status.USER_QUESTION_BODY && next.element.getName().equals(DataReader.USER_QUESTION_TAG)) {
				jcas = getUserQuestionBody(next.element,next.requestererID);
				currentElements.removeFirst();
				status = Status.RELATED_QUESTION_SUBJECT;
			} else if (status == Status.RELATED_QUESTION_SUBJECT && next.element.getName().equals(DataReader.RELATED_QUESTION_TAG)) {
				jcas = getRelatedQuestionSubject(next.element,next.requestererID);
				status = Status.RELATED_QUESTION_BODY;
			} else if (status == Status.RELATED_QUESTION_BODY && next.element.getName().equals(DataReader.RELATED_QUESTION_TAG)) {
				jcas = getRelatedQuestionBody(next.element,next.requestererID);
				currentElements.removeFirst();
				if (next.typeOfInstance != TypeOfInstance.INSTANCE_B)
					status = Status.COMMENT;
				else {
					if (currentElements.isEmpty())
						status = null;
					else if (currentElements.getFirst().element.getName().equals(DataReader.RELATED_QUESTION_TAG))
						status = Status.RELATED_QUESTION_SUBJECT;
				}
			} else if (status == Status.COMMENT && next.element.getName().equals(DataReader.COMMENT_TAG)) {
				jcas = getComment(next.element,next.requestererID);
				currentElements.removeFirst();
				
				if (currentElements.isEmpty())
					status = null;
				else if (currentElements.getFirst().element.getName().equals(DataReader.RELATED_QUESTION_TAG))
					status = Status.RELATED_QUESTION_SUBJECT;
			} else {
				throw new AnalysisEngineProcessException(status+" "+next.element.getName(),null);
			}
		}
		
		if (next.typeOfInstance == TypeOfInstance.INSTANCE_C) {
			InstanceC instanceAnnotation = new InstanceC(jcas);
			instanceAnnotation.addToIndexes();
		} else if (next.typeOfInstance == TypeOfInstance.INSTANCE_A) {
			InstanceA instanceAnnotation = new InstanceA(jcas);
			instanceAnnotation.addToIndexes();
		} else if (next.typeOfInstance == TypeOfInstance.INSTANCE_B) {
			InstanceB instanceAnnotation = new InstanceB(jcas);
			instanceAnnotation.addToIndexes();
		} 

		return jcas;
	}
}

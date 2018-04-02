/**
* Copyright 2017 Alberto Barron-Cedeno
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/
package qa.qcri.iyas.data.reader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Class to read the English datasets from SemEval 2016 Task 3 on 
 * Community Question Answering [1]. 
 * 
 * [1] http://alt.qcri.org/semeval2016/task3/
 * @author albarron
 *
 */
public class XmlSemeval2016CqaEn extends DataReader{
	
	public static final String FILE_PARAM = "file";
	
	@ConfigurationParameter(name = FILE_PARAM)
	private String file;
	
	private final Charset ENCODING = StandardCharsets.UTF_8;
	
	private final String LINE_START = "line-start-sub";
	
	
	/** The tag representing and original (user) question */
	private final String XML_TAG_ORG_Q = "OrgQuestion";
	private final String XML_ATT_ORG_QID = "ORGQ_ID";
	private final String XML_TAG_ORG_Q_SUBJECT = "OrgQSubject";
	private final String XML_TAG_ORG_Q_BODY = "OrgQBody";
	
	private final String XML_TAG_THREAD = "Thread";
	
	/** The tag representing and related (forum) question */	
	private final String XML_TAG_REL_Q = "RelQuestion";	
	private final String XML_ATT_REL_QID = "RELQ_ID";
	
	private final String XML_TAG_REL_QSUBJECT = "RelQSubject";
	private final String XML_TAG_REL_QBODY = "RelQBody";
	
	private final String XML_ATT_REL_QRELEVANCE = "RELQ_RELEVANCE2ORGQ";
	
	private final String XML_TAG_REL_C = "RelComment";
	private final String XML_TAG_REL_CBODY = "RelCText";
	private final String XML_ATT_REL_CID = "RELC_ID";
//	private final String XML_ATT_REL_CDATE = "RELC_DATE";
//	private final String XML_ATT_REL_CUSERID = "RELC_USERID";
	
	private final String XML_ATT_REL_CREL2FORUM = "RELC_RELEVANCE2RELQ";
	private ListIterator<Element> QUESTION_ITERATOR;

	
//	/**
//	 * String to keep track of the current original question (if necessary)
//	 */
//	private String CURRENT_ID = "NULL";
	
	/** This is specific to this reader because it is for the English dataset.
	 * TODO find out if it is actually necessary 
	 */
	private final String language = "en";
	
	private int totalNumberOfExamples;
	private int index;
	
//	private String DOCUMENT; 

	private int countIntances(Document doc) {
		int count = 0;
		
		if (task.equals(DataReader.INSTANCE_A_TASK)) {
			Elements threads = doc.getElementsByTag(XML_TAG_THREAD);
			for (Element thread : threads)
				count += thread.getElementsByTag(XML_TAG_REL_C).size();
		} else if (task.equals(DataReader.INSTANCE_B_TASK)) {
			count = doc.getElementsByTag(XML_TAG_ORG_Q).size();
		}
		
		return count;
	}
	public void init() throws ResourceInitializationException {
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(file));
			String contents = new String(encoded, ENCODING).replaceAll("(?i)<br[^>]*>", LINE_START).replaceAll("\n", LINE_START);
			Document DOCUMENT = Jsoup.parse(contents);
			totalNumberOfExamples = countIntances(DOCUMENT);
			index = 0;
			
			Elements questions;
			if (task.equals(DataReader.INSTANCE_A_TASK)) {
				//ABC this should allow us to read all the related questions instantaneously
	//			questions = DOCUMENT.getElementsByTag(XML_TAG_REL_Q);
				questions = DOCUMENT.getElementsByTag(XML_TAG_THREAD);
			} else  { // same applies for both B and C
				questions = DOCUMENT.getElementsByTag(XML_TAG_ORG_Q);
			} 
	//		int qNumber = 0;
	//		int totalQuestions = questions.size();
			QUESTION_ITERATOR = questions.listIterator();
	
	//		for (Element question : questions) {
	////			qNumber++;
	//			CQAquestionThreadList tmp = globalElementToObject(question);
	//			if (instances.isEmpty()) {
	//				instances.add(globalElementToObject(question));
	//			} else if (instances.get(instances.size()-1).getOrgQuestion().getId()
	//					.equals(tmp.getOrgQuestion().getId())) {
	//				instances.get(instances.size()-1).addThread(tmp.getThread(0));
	//			} else {
	//				instances.add(globalElementToObject(question));
	//			}
	//		}
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}
	
	@Override
	public boolean hasNext() {
		return QUESTION_ITERATOR.hasNext();
	}

	private String produceAinstance(Element current) {		
		Elements questions = current.getElementsByTag(XML_TAG_REL_Q);
		Elements comments = current.getElementsByTag(XML_TAG_REL_C);
		
		String relatedQuestionID = questions.get(0).attr(XML_ATT_REL_QID);
		String realtedQuestionSubject = StringEscapeUtils.escapeXml(questions.get(0).getElementsByTag(XML_TAG_REL_QSUBJECT).get(0).ownText());
		String realtedQuestionBody = StringEscapeUtils.escapeXml(questions.get(0).getElementsByTag(XML_TAG_REL_QBODY).get(0).ownText());
		
		StringBuffer sb = new StringBuffer();
		sb.append("<"+ROOT_TAG+">")
		  .append(System.getProperty("line.separator"))
		  .append("	<"+INSTANCE_A_TAG+">")
		  .append(System.getProperty("line.separator"));		
		
		sb.append("		<")
		  .append(RELATED_QUESTION_TAG)
		  .append(" ")
		  .append(ID_ATTRIBUTE)
		  .append("=\"")
		  .append(relatedQuestionID)
		  .append("\" ")
		  .append(LANG_ATTRIBUTE)
		  .append("=\"")
		  .append(language)
		  .append("\" ")
		  .append(NUMBER_OF_CANDIDATES_ATTRIBUTE)
		  .append("=\"")
		  .append(comments.size())
		  .append("\">")
		  .append(System.getProperty("line.separator"));
		
		sb.append("			<")
		  .append(SUBJECT_TAG)
		  .append(">")
		  .append(realtedQuestionSubject)
		  .append("</")
		  .append(SUBJECT_TAG)
		  .append(">")
		  .append(System.getProperty("line.separator"));
		
		sb.append("			<")
		  .append(BODY_TAG)
		  .append(">")
		  .append(realtedQuestionBody)
		  .append("</")
		  .append(BODY_TAG)
		  .append(">")
		  .append(System.getProperty("line.separator"));
		
		int rank = 1;
		/////////
		for (Element comment : comments) {
//			System.out.println(comment.attr(XML_ATT_REL_CID));
			String commentID = comment.attr(XML_ATT_REL_CID);
			String commentBody = 
					StringEscapeUtils.escapeXml(comment.getElementsByTag(XML_TAG_REL_CBODY).get(0)
							.ownText().replaceAll(LINE_START, "\n"));
			String commentRelevance = comment.attr(XML_ATT_REL_CREL2FORUM);
					
			sb.append("			<")
			  .append(COMMENT_TAG).append(" ")
			  .append(ID_ATTRIBUTE)
			  .append("=\"")
			  .append(commentID)
			  .append("\" ")
			  .append(LANG_ATTRIBUTE)
			  .append("=\"")
			  .append(language)
			  .append("\" ")
			  .append(INDEX_ATTRIBUTE)
			  .append("=\"")
			  .append(index++)
			  .append("\" ")
			  .append(TOTAL_NUM_OF_EXAMPLES_ATTRIBUTE)
			  .append("=\"")
			  .append(totalNumberOfExamples)
			  .append("\" ")			  
			  .append(RELEVANCE_ATTRIBUTE)
			  .append("=\"")
			  .append(commentRelevance)
			  .append("\" ")
			  .append(RANK_ATTRIBUTE)
			  .append("=\"")
			  .append(rank++)			  
			  .append("\">")
			  .append(commentBody)
			  .append("</").append(COMMENT_TAG).append(">")
			  .append(System.getProperty("line.separator"));
		
//			TODO ALL THIS IS IGNORED IN THE CURRENT SETTING
//			try {
//				date = sdf.parse(comment.attr(XML_ATT_REL_CDATE));
//			} catch (ParseException e) {
//				System.out.format("Instead of the expected date, I get %s%n",  
//						comment.attr("XML_ATT_REL_CDATE"));
//				e.printStackTrace();
//			}
//			String cuserid = comment.attr(XML_ATT_REL_CUSERID);
//			String cusername = comment.attr(XML_ATT_REL_CUSERNAME);
//			String crelevance2org = comment.attr(XML_ATT_REL_CRELEVANCE2ORGQ);

		}		
		sb.append("		</"+RELATED_QUESTION_TAG+">"+System.getProperty("line.separator"));
	
		sb.append("	</")
		  .append(INSTANCE_A_TAG)
		  .append(">")
		  .append(System.getProperty("line.separator"));
		sb.append("</")
		  .append(ROOT_TAG)
		  .append(">")
		  .append(System.getProperty("line.separator"));
		
		return sb.toString();
	}

	
	private String produceBinstance(List<Element> questions) {	
		// GET THE ORIGINAL QUESTION INFO FROM THE FIRST INSTANCE
		Element userQ = questions.get(0).getElementsByTag(XML_TAG_ORG_Q).first();
		String userQuestionID = userQ.attr(XML_ATT_ORG_QID);
		String userQuestionSubject = StringEscapeUtils.escapeXml(userQ.getElementsByTag(XML_TAG_ORG_Q_SUBJECT).get(0).ownText());
		String userQuestionBody = StringEscapeUtils.escapeXml(userQ.getElementsByTag(XML_TAG_ORG_Q_BODY).get(0).ownText());
		
		StringBuffer sb = new StringBuffer();
		sb.append("<")
		  .append(ROOT_TAG)
		  .append(">")
		  .append(System.getProperty("line.separator"));
		sb.append("	<")
		  .append(INSTANCE_B_TAG)
		  .append(">")
		  .append(System.getProperty("line.separator"));		
		sb.append("		<")
		  .append(USER_QUESTION_TAG)
		  .append(" ")
		  .append(ID_ATTRIBUTE)
		  .append("=\""+userQuestionID)
		  .append("\" ")
		  .append(LANG_ATTRIBUTE)
		  .append("=\"")
		  .append(language)
		  .append("\" ")
		  .append(NUMBER_OF_CANDIDATES_ATTRIBUTE)
		  .append("=\"")
		  .append(questions.size())
		  .append("\">")
		  .append(System.getProperty("line.separator"));
		
		sb.append("			<")
		  .append(SUBJECT_TAG)
		  .append(">")
		  .append(userQuestionSubject)
		  .append("</")
		  .append(SUBJECT_TAG)
		  .append(">")
		  .append(System.getProperty("line.separator"));
		
		sb.append("			<"+BODY_TAG+">")
		  .append(userQuestionBody)
		  .append("</")
		  .append(BODY_TAG)
		  .append(">")
		  .append(System.getProperty("line.separator"))
		  .append("		</")
		  .append(USER_QUESTION_TAG)
		  .append(">")
		  .append(System.getProperty("line.separator"));
		
		int rank = 1;
		// GET THE REST OF INFO FOR THE REST OF INSTANCES		
		for (Element myKk : questions) {
			// There's always one single thread and the single thread has one single related question
			Element relatedQ = myKk.getElementsByTag(XML_TAG_THREAD).first()
									.getElementsByTag(XML_TAG_REL_Q).first();
			String relatedQuestionID = relatedQ.attr(XML_ATT_REL_QID);
			// TODO is this preprocesing necessary?
			String realtedQuestionSubject = StringEscapeUtils.escapeXml(relatedQ.getElementsByTag(XML_TAG_REL_QSUBJECT)
													.get(0).ownText().replaceAll(LINE_START, "\n"));
			String realtedQuestionBody = StringEscapeUtils.escapeXml(relatedQ.getElementsByTag(XML_TAG_REL_QBODY)
													.get(0).ownText().replaceAll(LINE_START, "\n"));
			String relatedQuestionRelevance = relatedQ.attr(XML_ATT_REL_QRELEVANCE);
			sb.append("		<")
			  .append(RELATED_QUESTION_TAG)
			  .append(" ")
			  .append(ID_ATTRIBUTE)
			  .append("=\"")
			  .append(relatedQuestionID)
			  .append("\" ")
			  .append(LANG_ATTRIBUTE)
			  .append("=\"")
			  .append(language)
			  .append("\" ")
			  .append(INDEX_ATTRIBUTE)
			  .append("=\"")
			  .append(index++)
			  .append("\" ")
			  .append(RELEVANCE_ATTRIBUTE)
			  .append("=\"")
			  .append(relatedQuestionRelevance)
			  .append("\" ")			  
			  .append(TOTAL_NUM_OF_EXAMPLES_ATTRIBUTE)
			  .append("=\"")
			  .append(totalNumberOfExamples)
			  .append("\" ")
			  .append(RANK_ATTRIBUTE)
			  .append("=\"")
			  .append(rank++)
			  .append("\" >")
			  .append(System.getProperty("line.separator"));
			
			sb.append("			<")
			  .append(SUBJECT_TAG)
			  .append(">")
			  .append(realtedQuestionSubject)
			  .append("</")
			  .append(SUBJECT_TAG)
			  .append(">")
			  .append(System.getProperty("line.separator"));
			
			sb.append("			<")
			  .append(BODY_TAG)
			  .append(">")
			  .append(realtedQuestionBody)
			  .append("</")
			  .append(BODY_TAG)
			  .append(">")
			  .append(System.getProperty("line.separator"))
			  .append("		</")
			  .append(RELATED_QUESTION_TAG)
			  .append(">")
			  .append(System.getProperty("line.separator"));			
		}
		sb.append("	</"+INSTANCE_B_TAG+">"+System.getProperty("line.separator"));
		sb.append("</"+ROOT_TAG+">"+System.getProperty("line.separator"));	
		
		return sb.toString();
	}
	
	private String produceCinstance(List<Element> questions) {
		
		Element userQ = questions.get(0).getElementsByTag(XML_TAG_ORG_Q).first();
		String userQuestionID = userQ.attr(XML_ATT_ORG_QID);
		String userQuestionSubject = userQ.getElementsByTag(XML_TAG_ORG_Q_SUBJECT).get(0).ownText();
		String userQuestionBody = userQ.getElementsByTag(XML_TAG_ORG_Q_BODY).get(0).ownText();
		
		
//		Elements questions = current.getElementsByTag(XML_TAG_REL_Q);
//		String userQuestionID = current.attr(XML_ATT_ORG_QID);
//		String userQuestionSubject = current.getElementsByTag(XML_TAG_ORG_Q_SUBJECT).get(0).ownText();
//		String userQuestionBody = current.getElementsByTag(XML_TAG_ORG_Q_BODY).get(0).ownText();		
		
		StringBuffer sb = new StringBuffer();
		sb.append("<")
		  .append(ROOT_TAG)
		  .append(">")
		  .append(System.getProperty("line.separator"));
		sb.append("	<")
		  .append(INSTANCE_C_TAG)
		  .append(">")
		  .append(System.getProperty("line.separator"));		
		sb.append("		<")
		  .append(USER_QUESTION_TAG)
		  .append(" ")
		  .append(ID_ATTRIBUTE)
		  .append("=\"")
		  .append(userQuestionID)
		  .append("\" ")
		  .append(LANG_ATTRIBUTE)
		  .append("=\"")
		  .append(language)
		  .append("\" ")
		  .append(NUMBER_OF_CANDIDATES_ATTRIBUTE)
		  .append("=\"")
		  .append(questions.size())
		  .append("\">")
		  .append(System.getProperty("line.separator"));
		sb.append("			<")
		  .append(SUBJECT_TAG)
		  .append(">")
		  .append(userQuestionSubject)
		  .append("</")
		  .append(SUBJECT_TAG)
		  .append(">")
		  .append(System.getProperty("line.separator"));
		sb.append("			<")
		  .append(BODY_TAG)
		  .append(">")
		  .append(userQuestionBody)
		  .append("</")
		  .append(BODY_TAG)
		  .append(">")
		  .append(System.getProperty("line.separator"));
		sb.append("		</")
		  .append(USER_QUESTION_TAG)
		  .append(">")
		  .append(System.getProperty("line.separator"));
	
		for  (Element myKk : questions) {
			// Related question
			// There's always one single thread and the single thread has one single related question
			Element relatedQ = myKk.getElementsByTag(XML_TAG_THREAD).first()
									.getElementsByTag(XML_TAG_REL_Q).first();
			String relatedQuestionID = relatedQ.attr(XML_ATT_REL_QID);
			// TODO is this preprocesing necessary?
			String realtedQuestionSubject = relatedQ.getElementsByTag(XML_TAG_REL_QSUBJECT)
													.get(0).ownText().replaceAll(LINE_START, "\n");
			String realtedQuestionBody = relatedQ.getElementsByTag(XML_TAG_REL_QBODY)
													.get(0).ownText().replaceAll(LINE_START, "\n");
			
			Elements comments = myKk.getElementsByTag(XML_TAG_THREAD).get(0).getElementsByTag(XML_TAG_REL_C);
			
			sb.append("		<")
			  .append(RELATED_QUESTION_TAG)
			  .append(" ")
			  .append(ID_ATTRIBUTE)
			  .append("=\"")
			  .append(relatedQuestionID)
			  .append("\" ")
			  .append(LANG_ATTRIBUTE)
			  .append("=\"")
			  .append(language)
			  .append("\" ")
			  .append(NUMBER_OF_CANDIDATES_ATTRIBUTE)
			  .append("=\"")
			  .append(comments.size())
			  .append("\">")
			  .append(System.getProperty("line.separator"));
			sb.append("			<")
			  .append(SUBJECT_TAG)
			  .append(">")
			  .append(realtedQuestionSubject)
			  .append("</")
			  .append(SUBJECT_TAG)
			  .append(">")
			  .append(System.getProperty("line.separator"));
			sb.append("			<")
			  .append(BODY_TAG)
			  .append(">")
			  .append(realtedQuestionBody)
			  .append("</")
			  .append(BODY_TAG)
			  .append(">")
			  .append(System.getProperty("line.separator"));		
		
			for (Element comment : comments) {
				String commentID = comment.attr(XML_ATT_REL_CID);
				String commentBody = 
						comment.getElementsByTag(XML_TAG_REL_CBODY).get(0)
								.ownText().replaceAll(LINE_START, "\n");
				sb.append("			<")
				  .append(COMMENT_TAG)
				  .append(" ")
				  .append(ID_ATTRIBUTE)
				  .append("=\"")
				  .append(commentID)
				  .append("\" ")
				  .append(LANG_ATTRIBUTE)
				  .append("=\"")
				  .append(language)
				  .append("\">")
				  .append(commentBody)
				  .append("</")
				  .append(COMMENT_TAG)
				  .append(">")
				  .append(System.getProperty("line.separator"));
			}
			
			sb.append("		</")
			  .append(RELATED_QUESTION_TAG)
			  .append(">")
			  .append(System.getProperty("line.separator"));
		}

		sb.append("	</")
		  .append(INSTANCE_C_TAG)
		  .append(">")
		  .append(System.getProperty("line.separator"));
		sb.append("</")
		  .append(ROOT_TAG)
		  .append(">")
		  .append(System.getProperty("line.separator"));
		
		return sb.toString();
	}
	
	@Override
	public String next() throws ResourceProcessException {
		
		String sb = null;
		
		if (task.equals(DataReader.INSTANCE_A_TASK)) {			
			sb = produceAinstance(QUESTION_ITERATOR.next());
		} else {
			// In this case it is necessary to iterate over multiple tags to grab all the instances			
			List<Element> originalQuestions = new ArrayList<Element>();
			Element current = QUESTION_ITERATOR.next();
			originalQuestions.add(current);
			String currentId = current.getElementsByTag(XML_TAG_ORG_Q).get(0).attr(XML_ATT_ORG_QID);
			
			while (hasNext()) {				
				current = QUESTION_ITERATOR.next();
				if (current.getElementsByTag(XML_TAG_ORG_Q).get(0).attr(XML_ATT_ORG_QID).equals(currentId)) { 
					originalQuestions.add(current);					
				} else {
					QUESTION_ITERATOR.previous();
					break;
				}			
			}
			
			if (task.equals(DataReader.INSTANCE_B_TASK)) {
				sb = produceBinstance(originalQuestions);
			} else {
				sb = produceCinstance(originalQuestions);
			}
		} 
		
				
		return sb;

		
		///////////
	}

}

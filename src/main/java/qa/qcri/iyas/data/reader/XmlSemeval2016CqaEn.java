package qa.qcri.iyas.data.reader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
//import java.text.ParseException;
//import java.util.ArrayList;
//import java.util.List;
import java.util.ListIterator;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class XmlSemeval2016CqaEn extends DataReader{

	private final Charset ENCODING = StandardCharsets.UTF_8;
	
	private final String FILE_PARAM = "file";
	private final String LINE_START = "line-start-sub";
	@ConfigurationParameter(name = FILE_PARAM)
	private String file;
	
	/** The tag representing and original (user) question */
	private final String XML_TAG_ORG_Q = "OrgQuestion";
	
	/** The tag representing and related (forum) question */
	private final String XML_TAG_REL_Q = "RelQuestion";	
	private final String XML_ATT_REL_QID = "RELQ_ID";
	
	private final String XML_TAG_REL_QSUBJECT = "RelQSubject";
	private final String XML_TAG_REL_QBODY = "RelQBody";
	
	private final String XML_TAG_REL_C = "RelComment";
	private final String XML_TAG_REL_CBODY = "RelCText";
	private final String XML_ATT_REL_CID = "RELC_ID";
//	private final String XML_ATT_REL_CDATE = "RELC_DATE";
//	private final String XML_ATT_REL_CUSERID = "RELC_USERID";
	
	private ListIterator<Element> QUESTION_ITERATOR;
	
//	private String DOCUMENT; 

	public void init() throws IOException, ResourceInitializationException {
		byte[] encoded = Files.readAllBytes(Paths.get(file));
		String contents = new String(encoded, ENCODING).replaceAll("(?i)<br[^>]*>", LINE_START).replaceAll("\n", LINE_START);
		Document DOCUMENT = Jsoup.parse(contents);
		
		Elements questions;
		if (task.equals(DataReader.INSTANCE_A_TASK)) {
			//ABC this should allow us to read all the related questions instantaneously
			questions = DOCUMENT.getElementsByTag(XML_TAG_REL_Q);
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
	}
	
	@Override
	public boolean hasNext() {
		return QUESTION_ITERATOR.hasNext();
	}

	@Override
	public String next() throws Exception {
		
		Element current = QUESTION_ITERATOR.next();
		
		String relatedQuestionID = null;
		String realtedQuestionSubject = null;
		String realtedQuestionBody = null;
//		String userQuestionID = null;
//		String userQuestionSubject = null;
//		String userQuestionBody = null;
		String language = "en";
		
		////////////
		StringBuilder sb = new StringBuilder();
		if (task.equals(DataReader.INSTANCE_C_TASK)) {
//			sb.append("<"+ROOT_TAG+">"+System.getProperty("line.separator"));
//			sb.append("	<"+INSTANCE_C_TAG+">"+System.getProperty("line.separator"));
//			userQuestionID = currentLine[0];
//			userQuestionSubject = currentLine[1];
//			userQuestionBody = currentLine[2];
//			sb.append("		<"+USER_QUESTION_TAG+" "+ID_ATTRIBUTE+"=\""+userQuestionID+"\" "+LANG_ATTRIBUTE+"=\""+language+"\" "+NUMBER_OF_CANDIDATES_ATTRIBUTE+"=\""+10+"\">"+System.getProperty("line.separator"));
//			sb.append("			<"+SUBJECT_TAG+">"+userQuestionSubject+"</"+SUBJECT_TAG+">"+System.getProperty("line.separator"));
//			sb.append("			<"+BODY_TAG+">"+userQuestionBody+"</"+BODY_TAG+">"+System.getProperty("line.separator"));
//			sb.append("		</"+USER_QUESTION_TAG+">"+System.getProperty("line.separator"));
//			
//			do {
//				relatedQuestionID = currentLine[3];
//				realtedQuestionSubject = currentLine[4];
//				realtedQuestionBody = currentLine[5];
//				sb.append("		<"+RELATED_QUESTION_TAG+" "+ID_ATTRIBUTE+"=\""+relatedQuestionID+"\" "+LANG_ATTRIBUTE+"=\""+language+"\" "+NUMBER_OF_CANDIDATES_ATTRIBUTE+"=\""+10+"\">"+System.getProperty("line.separator"));
//				sb.append("			<"+SUBJECT_TAG+">"+realtedQuestionSubject+"</"+SUBJECT_TAG+">"+System.getProperty("line.separator"));
//				sb.append("			<"+BODY_TAG+">"+realtedQuestionBody+"</"+BODY_TAG+">"+System.getProperty("line.separator"));
//				for (int i=6;i<currentLine.length;i+=2) {
//					String commentID = currentLine[i];
//					String comment = currentLine[i+1];
//					sb.append("			<"+COMMENT_TAG+" "+ID_ATTRIBUTE+"=\""+commentID+"\" "+LANG_ATTRIBUTE+"=\""+language+"\">"+comment+"</"+COMMENT_TAG+">"+System.getProperty("line.separator"));
//				}
//				sb.append("		</"+RELATED_QUESTION_TAG+">"+System.getProperty("line.separator"));
//				
//				String line = in.readLine();
//				setNextLine(line);
//			} while (currentLine != null && currentLine[0].equals(userQuestionID));
//			
//			sb.append("	</"+INSTANCE_C_TAG+">"+System.getProperty("line.separator"));
//			sb.append("</"+ROOT_TAG+">"+System.getProperty("line.separator"));
		} else if (task.equals(DataReader.INSTANCE_A_TASK)) {
			sb.append("<"+ROOT_TAG+">")
			  .append(System.getProperty("line.separator"))
			  .append("	<"+INSTANCE_A_TAG+">")
			  .append(System.getProperty("line.separator"));

			relatedQuestionID = current.attr(XML_ATT_REL_QID);
			realtedQuestionSubject = current.getElementsByTag(XML_TAG_REL_QSUBJECT).get(0).ownText();
			realtedQuestionBody = current.getElementsByTag(XML_TAG_REL_QBODY).get(0).ownText();
			
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
			  .append("=\""+10+"\">")	// ABC this is very weird. Ask Salvatore
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
			
			
			/////////
			for (Element comment : current.getElementsByTag(XML_TAG_REL_C)) {
				String commentID = comment.attr(XML_ATT_REL_CID);
				String commentBody = 
						comment.getElementsByTag(XML_TAG_REL_CBODY).get(0).ownText().replaceAll(LINE_START, "\n");
 						
				sb.append("			<")
				  .append(COMMENT_TAG).append(" ")
				  .append(ID_ATTRIBUTE)
				  .append("=\"")
				  .append(commentID)
				  .append("\" ")
				  .append(LANG_ATTRIBUTE)
				  .append("=\"")
				  .append(language)
				  .append("\">")
				  .append(commentBody)
				  .append("</").append(COMMENT_TAG).append(">")
				  .append(System.getProperty("line.separator"));
				
			
				//ALL THIS IS IGNORED IN THE CURRENT SETTING
//				try {
//					date = sdf.parse(comment.attr(XML_ATT_REL_CDATE));
//				} catch (ParseException e) {
//					System.out.format("Instead of the expected date, I get %s%n",  
//							comment.attr("XML_ATT_REL_CDATE"));
//					e.printStackTrace();
//				}
//				String cuserid = comment.attr(XML_ATT_REL_CUSERID);
//				String cusername = comment.attr(XML_ATT_REL_CUSERNAME);
//				String crelevance2org = comment.attr(XML_ATT_REL_CRELEVANCE2ORGQ);
//				String crelevance2rel = comment.attr(XML_ATT_REL_CRELEVANCE2RELQ);

			}			
			////////
			
//			for (int i=6;i<currentLine.length;i+=2) {
//				
//				
//			}
//			sb.append("		</"+RELATED_QUESTION_TAG+">"+System.getProperty("line.separator"));
//			
//			String line = in.readLine();
//			setNextLine(line);
//			
			sb.append("	</")
			  .append(INSTANCE_A_TAG)
			  .append(">")
			  .append(System.getProperty("line.separator"));
			sb.append("</")
			  .append(ROOT_TAG)
			  .append(">")
			  .append(System.getProperty("line.separator"));
		} if (task.equals(DataReader.INSTANCE_B_TASK)) {
//			sb.append("<"+ROOT_TAG+">"+System.getProperty("line.separator"));
//			sb.append("	<"+INSTANCE_B_TAG+">"+System.getProperty("line.separator"));
//			userQuestionID = currentLine[0];
//			userQuestionSubject = currentLine[1];
//			userQuestionBody = currentLine[2];
//			sb.append("		<"+USER_QUESTION_TAG+" "+ID_ATTRIBUTE+"=\""+userQuestionID+"\" "+LANG_ATTRIBUTE+"=\""+language+"\" "+NUMBER_OF_CANDIDATES_ATTRIBUTE+"=\""+10+"\">"+System.getProperty("line.separator"));
//			sb.append("			<"+SUBJECT_TAG+">"+userQuestionSubject+"</"+SUBJECT_TAG+">"+System.getProperty("line.separator"));
//			sb.append("			<"+BODY_TAG+">"+userQuestionBody+"</"+BODY_TAG+">"+System.getProperty("line.separator"));
//			sb.append("		</"+USER_QUESTION_TAG+">"+System.getProperty("line.separator"));
//			
//			do {
//				relatedQuestionID = currentLine[3];
//				realtedQuestionSubject = currentLine[4];
//				realtedQuestionBody = currentLine[5];
//				sb.append("		<"+RELATED_QUESTION_TAG+" "+ID_ATTRIBUTE+"=\""+relatedQuestionID+"\" "+LANG_ATTRIBUTE+"=\""+language+"\" >"+System.getProperty("line.separator"));
//				sb.append("			<"+SUBJECT_TAG+">"+realtedQuestionSubject+"</"+SUBJECT_TAG+">"+System.getProperty("line.separator"));
//				sb.append("			<"+BODY_TAG+">"+realtedQuestionBody+"</"+BODY_TAG+">"+System.getProperty("line.separator"));
//				sb.append("		</"+RELATED_QUESTION_TAG+">"+System.getProperty("line.separator"));
//				
//				String line = in.readLine();
//				setNextLine(line);
//			} while (currentLine != null && currentLine[0].equals(userQuestionID));
//			
//			sb.append("	</"+INSTANCE_B_TAG+">"+System.getProperty("line.separator"));
//			sb.append("</"+ROOT_TAG+">"+System.getProperty("line.separator"));
		} 
				
		return sb.toString();

		
		///////////
	}

}

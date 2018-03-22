/**
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
 
 
package qa.qcri.iyas.data.reader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

public class PlainTextDataReader extends DataReader {
	
	public static final String FILE_PARAM = "file";
	
	@ConfigurationParameter(name = FILE_PARAM)
	private String file;
	
	private BufferedReader in;
	private String currentLine[] = null;
	
	private int index = 0;
	private int totalNumberOfExamples = 0;
	
	private int countExamples() throws IOException {
		in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
		
		int count = 0;
		String line = null;
		if (task.equals(DataReader.INSTANCE_A_TASK)) {
			while ((line = in.readLine()) != null) {
				String split[] = StringEscapeUtils.escapeXml(line).split("\t");
				for (int i=6;i<split.length;i+=2) {
					count++;
				}
			}
		} else if (task.equals(DataReader.INSTANCE_B_TASK)) {
			while ((line = in.readLine()) != null) {
				count++;
			}
		}
		
		in.close();
		
		return count;
	}
	
	private void setNextLine(String line) {
		if (line != null) {
			currentLine = StringEscapeUtils.escapeXml(line).split("\t");
		} else {
			currentLine = null;
		}
	}

	@Override
	protected void init() throws ResourceInitializationException {
		try {
			totalNumberOfExamples = countExamples();
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
			String line = in.readLine();
			setNextLine(line);
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	protected void releaseResources() {
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasNext() {
		return currentLine != null;
	}

	@Override
	public String next() throws ResourceProcessException  {
		
		String relatedQuestionID = null;
		String realtedQuestionSubject = null;
		String realtedQuestionBody = null;
		String userQuestionID = null;
		String userQuestionSubject = null;
		String userQuestionBody = null;
		String language = "en";
		
		StringBuilder sb = new StringBuilder();
		if (task.equals(DataReader.INSTANCE_C_TASK)) {
			sb.append("<"+ROOT_TAG+">"+System.getProperty("line.separator"));
			sb.append("	<"+INSTANCE_C_TAG+">"+System.getProperty("line.separator"));
			userQuestionID = currentLine[0];
			userQuestionSubject = currentLine[1];
			userQuestionBody = currentLine[2];
			sb.append("		<"+USER_QUESTION_TAG+" "+ID_ATTRIBUTE+"=\""+userQuestionID+"\" "+LANG_ATTRIBUTE+"=\""+language+"\" "+NUMBER_OF_CANDIDATES_ATTRIBUTE+"=\""+10+"\">"+System.getProperty("line.separator"));
			sb.append("			<"+SUBJECT_TAG+">"+userQuestionSubject+"</"+SUBJECT_TAG+">"+System.getProperty("line.separator"));
			sb.append("			<"+BODY_TAG+">"+userQuestionBody+"</"+BODY_TAG+">"+System.getProperty("line.separator"));
			sb.append("		</"+USER_QUESTION_TAG+">"+System.getProperty("line.separator"));
			
			do {
				relatedQuestionID = currentLine[3];
				realtedQuestionSubject = currentLine[4];
				realtedQuestionBody = currentLine[5];
				sb.append("		<"+RELATED_QUESTION_TAG+" "+ID_ATTRIBUTE+"=\""+relatedQuestionID+"\" "+LANG_ATTRIBUTE+"=\""+language+"\" "+NUMBER_OF_CANDIDATES_ATTRIBUTE+"=\""+10+"\">"+System.getProperty("line.separator"));
				sb.append("			<"+SUBJECT_TAG+">"+realtedQuestionSubject+"</"+SUBJECT_TAG+">"+System.getProperty("line.separator"));
				sb.append("			<"+BODY_TAG+">"+realtedQuestionBody+"</"+BODY_TAG+">"+System.getProperty("line.separator"));
				for (int i=6;i<currentLine.length;i+=2) {
					String commentID = currentLine[i];
					String comment = currentLine[i+1];
					sb.append("			<"+COMMENT_TAG+" "+ID_ATTRIBUTE+"=\""+commentID+"\" "+LANG_ATTRIBUTE+"=\""+language+"\">"+comment+"</"+COMMENT_TAG+">"+System.getProperty("line.separator"));
				}
				sb.append("		</"+RELATED_QUESTION_TAG+">"+System.getProperty("line.separator"));
				
				try {
					String line = in.readLine();
					setNextLine(line);
				} catch (IOException e) {
					throw new ResourceProcessException(e);
				}
			} while (currentLine != null && currentLine[0].equals(userQuestionID));
			
			sb.append("	</"+INSTANCE_C_TAG+">"+System.getProperty("line.separator"));
			sb.append("</"+ROOT_TAG+">"+System.getProperty("line.separator"));
			
			return sb.toString();
		} else if (task.equals(DataReader.INSTANCE_A_TASK)) {
			sb.append("<"+ROOT_TAG+">"+System.getProperty("line.separator"));
			sb.append("	<"+INSTANCE_A_TAG+">"+System.getProperty("line.separator"));
			
			relatedQuestionID = currentLine[3];
			realtedQuestionSubject = currentLine[4];
			realtedQuestionBody = currentLine[5];
			sb.append("		<"+RELATED_QUESTION_TAG+" "+ID_ATTRIBUTE+"=\""+relatedQuestionID+"\" "+LANG_ATTRIBUTE+"=\""+language+"\" "+NUMBER_OF_CANDIDATES_ATTRIBUTE+"=\""+((currentLine.length-6)/2)+"\">"+System.getProperty("line.separator"));
			sb.append("			<"+SUBJECT_TAG+">"+realtedQuestionSubject+"</"+SUBJECT_TAG+">"+System.getProperty("line.separator"));
			sb.append("			<"+BODY_TAG+">"+realtedQuestionBody+"</"+BODY_TAG+">"+System.getProperty("line.separator"));
			
			for (int i=6;i<currentLine.length;i+=2) {
				String commentID = currentLine[i];
				String comment = currentLine[i+1];
				String label = Math.random() > 0.5 ? "Relevant" : "Irrelevant";
				sb.append("			<"+COMMENT_TAG+" "+ID_ATTRIBUTE+"=\""+commentID+"\" "+LANG_ATTRIBUTE+"=\""+language+"\" "+INDEX_ATTRIBUTE+"=\""+(index++)+"\" "+TOTAL_NUM_OF_EXAMPLES_ATTRIBUTE+"=\""+totalNumberOfExamples+"\" "+RELEVANCE_ATTRIBUTE+"=\""+label+"\">"+comment+"</"+COMMENT_TAG+">"+System.getProperty("line.separator"));
			}
			sb.append("		</"+RELATED_QUESTION_TAG+">"+System.getProperty("line.separator"));
			
			try {
				String line = in.readLine();
				setNextLine(line);
			} catch (IOException e) {
				throw new ResourceProcessException(e);
			}
			
			sb.append("	</"+INSTANCE_A_TAG+">"+System.getProperty("line.separator"));
			sb.append("</"+ROOT_TAG+">"+System.getProperty("line.separator"));
						
			return sb.toString();
		} if (task.equals(DataReader.INSTANCE_B_TASK)) {
			sb.append("<"+ROOT_TAG+">"+System.getProperty("line.separator"));
			sb.append("	<"+INSTANCE_B_TAG+">"+System.getProperty("line.separator"));
			userQuestionID = currentLine[0];
			userQuestionSubject = currentLine[1];
			userQuestionBody = currentLine[2];
			sb.append("		<"+USER_QUESTION_TAG+" "+ID_ATTRIBUTE+"=\""+userQuestionID+"\" "+LANG_ATTRIBUTE+"=\""+language+"\" "+NUMBER_OF_CANDIDATES_ATTRIBUTE+"=\""+10+"\">"+System.getProperty("line.separator"));
			sb.append("			<"+SUBJECT_TAG+">"+userQuestionSubject+"</"+SUBJECT_TAG+">"+System.getProperty("line.separator"));
			sb.append("			<"+BODY_TAG+">"+userQuestionBody+"</"+BODY_TAG+">"+System.getProperty("line.separator"));
			sb.append("		</"+USER_QUESTION_TAG+">"+System.getProperty("line.separator"));
			
			int candidates = 0;
			do {
				relatedQuestionID = currentLine[3];
				realtedQuestionSubject = currentLine[4];
				realtedQuestionBody = currentLine[5];
				String label = Math.random() > 0.5 ? "Relevant" : "Irrelevant";
				sb.append("		<"+RELATED_QUESTION_TAG+" "+ID_ATTRIBUTE+"=\""+relatedQuestionID+"\" "+LANG_ATTRIBUTE+"=\""+language+"\" "+INDEX_ATTRIBUTE+"=\""+(index++)+"\" "+TOTAL_NUM_OF_EXAMPLES_ATTRIBUTE+"=\""+totalNumberOfExamples+"\" "+RELEVANCE_ATTRIBUTE+"=\""+label+"\">"+System.getProperty("line.separator"));
				sb.append("			<"+SUBJECT_TAG+">"+realtedQuestionSubject+"</"+SUBJECT_TAG+">"+System.getProperty("line.separator"));
				sb.append("			<"+BODY_TAG+">"+realtedQuestionBody+"</"+BODY_TAG+">"+System.getProperty("line.separator"));
				sb.append("		</"+RELATED_QUESTION_TAG+">"+System.getProperty("line.separator"));
				
				try {
					String line = in.readLine();
					setNextLine(line);
				} catch (IOException e) {
					throw new ResourceProcessException(e);
				}
				candidates++;
			} while (currentLine != null && currentLine[0].equals(userQuestionID));
			
			sb.append("	</"+INSTANCE_B_TAG+">"+System.getProperty("line.separator"));
			sb.append("</"+ROOT_TAG+">"+System.getProperty("line.separator"));
			
			String xml = sb.toString();
			SAXBuilder builder = new SAXBuilder();
			try {
				Document document = builder.build(new StringReader(xml));
				document.getRootElement().getChild(INSTANCE_B_TAG).getChild(USER_QUESTION_TAG).setAttribute(NUMBER_OF_CANDIDATES_ATTRIBUTE, ""+candidates);
				XMLOutputter xmlOut = new XMLOutputter();
				return xmlOut.outputString(document);
			} catch (JDOMException | IOException e) {
				throw new ResourceProcessException(e);
			}
		}
		
		return null;
	}



}

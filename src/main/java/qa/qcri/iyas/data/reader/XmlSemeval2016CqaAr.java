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
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;


/**
 * This class reads the SemEval 2016 task 3 on cQA XML file for Arabic 
 * 
 * 
 * An example of the input format is the following
 * 
 * ###THIS IS DELIMITER FOR EACH INSTANCE QUERY QUESTION AND FORUM QUESTIONS.  * 
 * <Question QID = "200634">
 *   ###THIS IS THE TEXT OF THE QUERY QUESTION
 *   <Qtext>لدي تاخير لمدة 3 ايام في الدورة (والتي كانت عادة منتظمة وثابتة) انا لا اخذ حبوب منع الحمل او اي  </Qtext>
 *   ###THIS IS ONE OF THE MANY FORUM QUESTION-ANSWER PAIRS ASSOCIATED TO THE QUERY QUESTION
 *   <QApair QAID="312689" QArel="R" QAconf="1.0">
 *      <QAquestion> سنوات. الآن أنا على أبواب الزواج ولا أعرف متى يوم طهري أرجو المساعدة في أسرع وقت على جميع استفساراتي وعذرا على الإطالة.</QAquestion>
 *      <QAanswer>الإجابــة بسم الله الرحمن الرحيم الأخت الفاضلة حفظها الله. </QAanswer>
 *   </QApair>
 *   <QApair QAID ="43211" QArel="R" QAconf="1.0">
 *   ...
 *   </QApair>
 *   ...
 * </Question>  
 *  *  
 * 
 */
public class XmlSemeval2016CqaAr extends DataReader { 

	private final String XML_TAG_ORG_Q = "Question";
	private final String XML_TAG_ORG_Q_BODY = "Qtext";
	private final String XML_TAG_REL_Q = "QApair";
	private final String XML_ATT_REL_QRELEVANCE2ORGQ = "QArel";
	private final String XML_TAG_REL_QBODY = "QAquestion";

	public static final String FILE_PARAM = "file";
	
	@ConfigurationParameter(name = FILE_PARAM)
	private String file;
	
	List<String> formattedInstances;
	
	@Override
	protected void init() throws ResourceInitializationException {
		formattedInstances = new LinkedList<>();
		int totExamples = 0;
		try {
			SAXBuilder saxBuilder = new SAXBuilder();
			Document document = saxBuilder.build(new BufferedReader(new FileReader(file)));
			List<Element> orgQuestions = document.getRootElement().getChildren(XML_TAG_ORG_Q);
			for (Element orgQuestion :orgQuestions) {
				List<Element> relQuestions = orgQuestion.getChildren(XML_TAG_REL_Q);
				totExamples += relQuestions.size();
			}
			
			int qc = 1;
			int idx = 0;
			for (Element orgQuestion :orgQuestions) {
				String orgID = "Q"+(qc++);
				List<Element> relQuestions = orgQuestion.getChildren(XML_TAG_REL_Q);
				StringBuilder sb = new StringBuilder();
				sb.append("<"+ROOT_TAG+">"+System.getProperty("line.separator"));
				sb.append("	<"+INSTANCE_B_TAG+">"+System.getProperty("line.separator"));
				sb.append("		<"+USER_QUESTION_TAG+" "+ID_ATTRIBUTE+"=\""+orgID+"\" "+LANG_ATTRIBUTE+"=\"ar\" "+NUMBER_OF_CANDIDATES_ATTRIBUTE+"=\""+relQuestions.size()+"\""+">"+System.getProperty("line.separator"));
				
				sb.append("			<"+SUBJECT_TAG+">");
				sb.append("</"+SUBJECT_TAG+">"+System.getProperty("line.separator"));
				
				sb.append("			<"+BODY_TAG+">");
				sb.append(StringEscapeUtils.escapeXml(orgQuestion.getChildTextTrim(XML_TAG_ORG_Q_BODY)));
				sb.append("</"+BODY_TAG+">"+System.getProperty("line.separator"));
				
				sb.append("		</"+USER_QUESTION_TAG+">"+System.getProperty("line.separator"));
				
				int rc = 1;
				for (Element relQuestion : relQuestions) {
					String relID = orgID+"_R"+(rc++);
					String relevance;
					if (relQuestion.getAttributeValue(XML_ATT_REL_QRELEVANCE2ORGQ).equals("R"))
						relevance = "Relevant";
					else
						relevance = "Irrelevant";
					sb.append("		<"+RELATED_QUESTION_TAG+" "+ID_ATTRIBUTE+"=\""+relID+"\" "+LANG_ATTRIBUTE+"=\"ar\" "+
						INDEX_ATTRIBUTE+"=\""+(idx++)+"\" "+TOTAL_NUM_OF_EXAMPLES_ATTRIBUTE+"=\""+totExamples+"\" "+RELEVANCE_ATTRIBUTE+"=\""+relevance+"\">"+System.getProperty("line.separator"));
					
					sb.append("			<"+SUBJECT_TAG+">");
					sb.append("</"+SUBJECT_TAG+">"+System.getProperty("line.separator"));
					
					sb.append("			<"+BODY_TAG+">");
					sb.append(StringEscapeUtils.escapeXml(relQuestion.getChildTextTrim(XML_TAG_REL_QBODY)));
					sb.append("</"+BODY_TAG+">"+System.getProperty("line.separator"));
					
					sb.append("		</"+RELATED_QUESTION_TAG+">"+System.getProperty("line.separator"));
				}
				
				sb.append("	</"+INSTANCE_B_TAG+">"+System.getProperty("line.separator"));
				sb.append("</"+ROOT_TAG+">"+System.getProperty("line.separator"));
				formattedInstances.add(sb.toString());
				
			}
		} catch (JDOMException | IOException ex) {
			throw new ResourceInitializationException(ex);
		}
	}
	
//	public XmlSemeval2016CqaAr(String file) throws ResourceInitializationException {
//		this.file = file;
//		init();
//	}
//	
//	public static void main(String args[]) throws ResourceInitializationException {
//		new XmlSemeval2016CqaAr("/home/sromeo/workspaces/workspace-java/Tensor-Models/data/SemEval/Arabic/dev/SemEval2016-Task3-CQA-MD-dev.xml");
//	}

	@Override
	public boolean hasNext() {
		return !formattedInstances.isEmpty();
	}

	@Override
	public String next() throws ResourceProcessException {
		return formattedInstances.remove(0);
	}
}

/**
 * Copyright 201 Salvatore Romeo
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

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.collection.EntityProcessStatus;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderJDOMFactory;
import org.jdom2.input.sax.XMLReaderXSDFactory;
import org.uimafit.util.JCasUtil;

import qa.qcri.iyas.data.preprocessing.InputJCasMultiplier;
import qa.qcri.iyas.data.preprocessing.JCasPairGenerator;
import qa.qcri.iyas.data.reader.DataReader;
import qa.qcri.iyas.type.cqa.InstanceB;
import qa.qcri.iyas.type.cqa.RelatedQuestionBody;
import qa.qcri.iyas.type.cqa.UserQuestionBody;

public class MyStatusCallbackListenerJCasPairTaskB extends UimaAsBaseCallbackListener {
	
	private Set<String> inputInstances;
	private SAXBuilder builder;
	private Map<String,Map<String,String>> maps;
	
	public MyStatusCallbackListenerJCasPairTaskB(Map<String,Map<String,String>> m,boolean concatenate) throws JDOMException, URISyntaxException {
		inputInstances = new HashSet<>();
		XMLReaderJDOMFactory factory = new XMLReaderXSDFactory(new File(InputJCasMultiplier.class.getResource(
				DataReader.SCHEMA_INSTANCE_B_PATH).toURI()));
		builder = new SAXBuilder(factory);
		maps = m;
	}
	
	@Override
	public void entityProcessComplete(CAS cas, EntityProcessStatus aStatus) {
		try {
			if (!aStatus.getStatusMessage().equals("success")) {
				fail(aStatus.getStatusMessage());
			} else {
				AnnotationIndex<Annotation> annotations = cas.getJCas().getAnnotationIndex();
				if (annotations.size() == 1) {
					if (JCasUtil.exists(cas.getJCas(), DocumentAnnotation.class)) {
						String content = cas.getDocumentText();
						boolean b = inputInstances.remove(content);
						if (!b)
							fail("Received a not expected CAS.");
						
						Document instance = builder.build(new StringReader(cas.getDocumentText()));
						String id = instance.getRootElement().getChild(DataReader.INSTANCE_B_TAG)
								.getChild(DataReader.USER_QUESTION_TAG).getAttributeValue(DataReader.ID_ATTRIBUTE);
						
						System.out.println("Removed "+id);
					} else if (JCasUtil.exists(cas.getJCas(), InstanceB.class)) {
						try {
							CAS left =	cas.getView(JCasPairGenerator.LEFT_CAS_VIEW);
							int nl = JCasUtil.select(left.getJCas(), UserQuestionBody.class).size();
							if (nl != 1)
								fail("Expected one UserQuestionBody annotation, found "+nl);
							
							UserQuestionBody userQuestion = JCasUtil.select(left.getJCas(), UserQuestionBody.class).iterator().next();
							
							CAS right =	cas.getView(JCasPairGenerator.RIGHT_CAS_VIEW);
							int nr = JCasUtil.select(right.getJCas(), RelatedQuestionBody.class).size();
							if (nr != 1)
								fail("Expected one RelatedQuestionBody annotation, found "+nr);
							
							RelatedQuestionBody relatedQuestion = JCasUtil.select(right.getJCas(), RelatedQuestionBody.class).iterator().next();
							
							String userQuestionText = left.getDocumentText();
							String relatedQuestionText = right.getDocumentText();
							
							Map<String,String> map = maps.get(userQuestion.getID());
							
							if (map.get("left_"+relatedQuestion.getID()).equals(userQuestionText)) {
								map.remove("left_"+relatedQuestion.getID());
								System.out.println("Removed "+"left_"+relatedQuestion.getID());
							}
							
							if (map.get("right_"+relatedQuestion.getID()).equals(relatedQuestionText)) {
								map.remove("right_"+relatedQuestion.getID());
								System.out.println("Removed "+"right_"+relatedQuestion.getID());
							}
							
							if (map.isEmpty()) {
								maps.remove(userQuestion.getID());
								System.out.println(userQuestion.getID()+" complete");
							}
						} catch (CASRuntimeException e) {
							fail(e.getMessage());
						}
					} else {
						StringBuilder sb = new StringBuilder();
						sb.append("The following unexpected annotations have been received:\n");
						for (Annotation annotation : annotations)
							sb.append(annotation.getClass()+"\n");
						
						fail(sb.toString());
					}
				} else if (annotations.size() == 0) {
					
				} else {
					StringBuilder sb = new StringBuilder();
					sb.append("The following unexpected annotations have been received:\n");
					for (Annotation annotation : annotations)
						sb.append(annotation.getClass()+"\n");
					
					fail(sb.toString());
				}
			}
		} catch (CASException | JDOMException | IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public Set<String> getInputInstances() {
		return inputInstances;
	}
}

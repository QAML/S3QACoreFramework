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
import qa.qcri.iyas.type.cqa.Comment;
import qa.qcri.iyas.type.cqa.InstanceA;
import qa.qcri.iyas.type.cqa.RelatedQuestionBody;

public class MyStatusCallbackListenerJCasPairTaskA extends UimaAsBaseCallbackListener {
	
	private Set<String> inputInstances;
	private SAXBuilder builder;
	private Map<String,Map<String,String>> maps;
	
	public MyStatusCallbackListenerJCasPairTaskA(Map<String,Map<String,String>> m,boolean concatenate) throws JDOMException, URISyntaxException {
		inputInstances = new HashSet<>();
		XMLReaderJDOMFactory factory = new XMLReaderXSDFactory(new File(InputJCasMultiplier.class.getResource(
				DataReader.SCHEMA_INSTANCE_A_PATH).toURI()));
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
						String id = instance.getRootElement().getChild(DataReader.INSTANCE_A_TAG)
								.getChild(DataReader.RELATED_QUESTION_TAG).getAttributeValue(DataReader.ID_ATTRIBUTE);
						
						System.out.println("Removed "+id);
					} else if (JCasUtil.exists(cas.getJCas(), InstanceA.class)) {
						try {
							CAS left =	cas.getView(JCasPairGenerator.LEFT_CAS_VIEW);
							int nl = JCasUtil.select(left.getJCas(), RelatedQuestionBody.class).size();
							if (nl != 1)
								fail("Expected one RelatedQuestionBody annotation, found "+nl);
							
							RelatedQuestionBody question = JCasUtil.select(left.getJCas(), RelatedQuestionBody.class).iterator().next();
							
							CAS right =	cas.getView(JCasPairGenerator.RIGHT_CAS_VIEW);
							int nr = JCasUtil.select(right.getJCas(), Comment.class).size();
							if (nr != 1)
								fail("Expected one Comment annotation, found "+nr);
							
							Comment comment = JCasUtil.select(right.getJCas(), Comment.class).iterator().next();
							
							String questionText = left.getDocumentText();
							String commentText = right.getDocumentText();
							
							Map<String,String> map = maps.get(question.getID());
							
							if (map.get("left_"+comment.getID()).equals(questionText)) {
								map.remove("left_"+comment.getID());
								System.out.println("Removed "+"left_"+comment.getID());
							}
							
							if (map.get("right_"+comment.getID()).equals(commentText)) {
								map.remove("right_"+comment.getID());
								System.out.println("Removed "+"right_"+comment.getID());
							}
							
							if (map.isEmpty()) {
								maps.remove(question.getID());
								System.out.println(question.getID()+" complete");
							}
						} catch (CASRuntimeException e) {
							fail(e.getMessage());
						}
					} else {
						StringBuilder sb = new StringBuilder();
						sb.append("The following unexpexted annotations have been received:\n");
						for (Annotation annotation : annotations)
							sb.append(annotation.getClass()+"\n");
						
						fail(sb.toString());
					}
					
				} else {
					StringBuilder sb = new StringBuilder();
					sb.append("The following unexpexted annotations have been received:\n");
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

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
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.collection.EntityProcessStatus;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderJDOMFactory;
import org.jdom2.input.sax.XMLReaderXSDFactory;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import qa.qcri.iyas.data.preprocessing.InputJCasMultiplier;
import qa.qcri.iyas.data.reader.DataReader;
import qa.qcri.iyas.type.AdditionalInfo;
import qa.qcri.iyas.type.cqa.InstanceA;
import qa.qcri.iyas.type.cqa.InstanceB;
import qa.qcri.iyas.type.cqa.UserQuestion;
import qa.qcri.iyas.type.cqa.UserQuestionBody;
import qa.qcri.iyas.type.cqa.UserQuestionSubject;
import qa.qcri.iyas.util.AggregatedJCasManager;

public class MyStatusCallbackListenerAggregatedJCasTaskB extends UimaAsBaseCallbackListener {
	
	private Set<String> inputInstances;
	private SAXBuilder builder;
	private Map<String,Map<String,String>> maps;
	private boolean concatenate;
	
	public MyStatusCallbackListenerAggregatedJCasTaskB(Map<String,Map<String,String>> m,boolean concatenate) throws JDOMException, URISyntaxException {
		inputInstances = new HashSet<>();
		XMLReaderJDOMFactory factory = new XMLReaderXSDFactory(new File(InputJCasMultiplier.class.getResource(
				DataReader.SCHEMA_INSTANCE_B_PATH).toURI()));
		builder = new SAXBuilder(factory);
		maps = m;
		this.concatenate = concatenate;
	}
	
	private void checkStandardTextAnnotations(JCas jcas) {
		if (!JCasUtil.exists(jcas, Token.class))
			fail("Expected Token annotations");
		if (!JCasUtil.exists(jcas, Sentence.class))
			fail("Expected Sentence annotations");
		if (!JCasUtil.exists(jcas, Lemma.class))
			fail("Expected Lemma annotations");
		if (!JCasUtil.exists(jcas, POS.class))
			fail("Expected POS annotations");
		if (!JCasUtil.exists(jcas, Chunk.class))
			fail("Expected Chunk annotations");
		
	}
	
	@Override
	public void entityProcessComplete(CAS cas, EntityProcessStatus aStatus) {
		try {
			if (!aStatus.getStatusMessage().equals("success")) {
				fail(aStatus.getStatusMessage());
			} else {
				AnnotationIndex<Annotation> annotations = cas.getJCas().getAnnotationIndex();
				if (annotations.size() == 1) {
					if (!JCasUtil.exists(cas.getJCas(), DocumentAnnotation.class)) {
						StringBuilder sb = new StringBuilder();
						sb.append("Only a DocumentAnnotation annotation is expected\n");
						sb.append("The following annotations have been received:\n");
						for (Annotation annotation : annotations)
							sb.append(annotation.getClass()+"\n");
						
						fail(sb.toString());
					} else {
						String content = cas.getDocumentText();
						boolean b = inputInstances.remove(content);
						if (!b)
							fail("Received a not expected CAS.");
						
						Document instance = builder.build(new StringReader(cas.getDocumentText()));
						String id = instance.getRootElement().getChild(DataReader.INSTANCE_B_TAG)
								.getChild(DataReader.USER_QUESTION_TAG).getAttributeValue(DataReader.ID_ATTRIBUTE);
						
						System.out.println("Removed "+id);
					}
				} else if (annotations.size() == 3) {
					Collection<AdditionalInfo> infos = JCasUtil.select(cas.getJCas(), AdditionalInfo.class);
					if (infos.size() != 1)
						fail("Expected an AdditionalInfo annotation, found "+infos.size());
					
					if (!(JCasUtil.exists(cas.getJCas(), UserQuestion.class) && 
							JCasUtil.exists(cas.getJCas(), InstanceB.class))) {
						StringBuilder sb = new StringBuilder();
						sb.append("The expected annotations have not been received\n");
						sb.append("The following annotations have been received:\n");
						for (Annotation annotation : annotations)
							sb.append(annotation.getClass()+"\n");
						
						fail(sb.toString());
					} else {
						UserQuestion cqaAnnotation = JCasUtil.select(cas.getJCas(), UserQuestion.class).iterator().next();
						
						String subject = null;
						if (!concatenate) {
							if (JCasUtil.exists(cas.getJCas().getView(AggregatedJCasManager.USER_QUESTION_SUBJECT_VIEW), InstanceA.class))
								fail("Unexpected InstanceA annotation");
							if (!JCasUtil.exists(cas.getJCas().getView(AggregatedJCasManager.USER_QUESTION_SUBJECT_VIEW), UserQuestionSubject.class))
								fail("Expected UserQuestionSubject annotation");
							subject = cas.getJCas().getView(AggregatedJCasManager.USER_QUESTION_SUBJECT_VIEW).getDocumentText();
							checkStandardTextAnnotations(cas.getJCas().getView(AggregatedJCasManager.USER_QUESTION_SUBJECT_VIEW));
						}
						
						if (JCasUtil.exists(cas.getJCas().getView(AggregatedJCasManager.USER_QUESTION_BODY_VIEW), InstanceA.class))
							fail("Unexpected InstanceA annotation");
						if (!JCasUtil.exists(cas.getJCas().getView(AggregatedJCasManager.USER_QUESTION_BODY_VIEW), UserQuestionBody.class))
							fail("Expected UserQuestionBody annotation");
						String body = cas.getJCas().getView(AggregatedJCasManager.USER_QUESTION_BODY_VIEW).getDocumentText();
						checkStandardTextAnnotations(cas.getJCas().getView(AggregatedJCasManager.USER_QUESTION_BODY_VIEW));
						
						Map<String,String> map = maps.get(cqaAnnotation.getID());
						
						if (!concatenate && map.get("subject_"+cqaAnnotation.getID()).equals(subject)) {
							map.remove("subject_"+cqaAnnotation.getID());
							System.out.println("Removed "+"subject_"+cqaAnnotation.getID());
						}
						
						if (map.get("body_"+cqaAnnotation.getID()).equals(body)) {
							map.remove("body_"+cqaAnnotation.getID());
							System.out.println("Removed "+"body_"+cqaAnnotation.getID());
						}
						
						for (String rid : cqaAnnotation.getCandidateIDs().toArray()) {
							if (!concatenate) {
								String rel_subject = cas.getJCas().getView(AggregatedJCasManager.RELATED_QUESTION_SUBJECT_VIEW+"-"+rid).getDocumentText();
								checkStandardTextAnnotations(cas.getJCas().getView(AggregatedJCasManager.RELATED_QUESTION_SUBJECT_VIEW+"-"+rid));
								if (map.get("rel_subject_"+rid).equals(rel_subject)) {
									map.remove("rel_subject_"+rid);
									System.out.println("Removed "+"rel_subject_"+rid);
								}
							}
							
							String rel_body = cas.getJCas().getView(AggregatedJCasManager.RELATED_QUESTION_BODY_VIEW+"-"+rid).getDocumentText();
							checkStandardTextAnnotations(cas.getJCas().getView(AggregatedJCasManager.RELATED_QUESTION_BODY_VIEW+"-"+rid));
							if (map.get("rel_body_"+rid).equals(rel_body)) {
								map.remove("rel_body_"+rid);
								System.out.println("Removed "+"rel_body_"+rid);
							}
						}
						
						if (map.isEmpty()) {
							maps.remove(cqaAnnotation.getID());
							System.out.println(cqaAnnotation.getID()+" complete");
						} 
						else
							fail("The received aggregated JCas is not complete.");
					}
				} else {
					StringBuilder sb = new StringBuilder();
					sb.append("unexpected annotations have not been received\n");
					sb.append("The following annotations have been received:\n");
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

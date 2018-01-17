/**

 * Copyright 2018 Alberto Barron-Cedeno
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

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderJDOMFactory;
import org.jdom2.input.sax.XMLReaderXSDFactory;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * A JUnit test-case for the class {@link XmlSemeval2016CqaEn} to be used as test example for new implemented
 * {@link DataReader}.
 * 
 * @author albarron (based on sromeo's test)
 *
 */
public class SemevalXmlDataReaderTest {

	@Test
	public void testInstanceA() throws JDOMException, URISyntaxException, CollectionException, UIMAException, IOException {
		CollectionReaderDescription collectionReaderDescr = CollectionReaderFactory.createReaderDescription(
				InputCollectionDataReader.class);
		
		/*
		 * Binds the XmlSemeval2016CqaEn to the InputCollectionDataReader. The parameters are passed by means
		 * of key-value pairs. In this case there are two parameters: XmlSemeval2016CqaEn.FILE_PARAM represents
		 * the input plain text file, and XmlSemeval2016CqaEn.TASK_PARAM defines the type of instances to be
		 * generated (sub-task A, sub-task B or sub-task C).
		 * IMPORTANT: only this line of code must be changed to test other DataReader implementations.
		 */
		ExternalResourceDescription reader = ExternalResourceFactory.createExternalResourceDescription(XmlSemeval2016CqaEn.class,
				XmlSemeval2016CqaEn.FILE_PARAM,"/data/alt/corpora/semeval2016/data/v3.1/SemEval2016-Task3-CQA-QL-dev-subtaskA.oneinstance.xml",
				XmlSemeval2016CqaEn.TASK_PARAM, XmlSemeval2016CqaEn.INSTANCE_A_TASK);
		
		ExternalResourceFactory.bindExternalResource(collectionReaderDescr, 
				InputCollectionDataReader.INPUT_READER_PARAM, reader);
		
		CollectionReader collectionReader = UIMAFramework.produceCollectionReader(collectionReaderDescr);
		
		TypeSystemDescription tsd = 
				  TypeSystemDescriptionFactory.createTypeSystemDescription();
		
		XMLReaderJDOMFactory factory = new XMLReaderXSDFactory(
				new File(InputCollectionDataReader.class.getResource(DataReader.SCHEMA_INSTANCE_A_PATH).toURI()));
		SAXBuilder saxBuilder = new SAXBuilder(factory);
				
		while (collectionReader.hasNext()) {
			JCas jcas = JCasFactory.createJCas(tsd);
			collectionReader.getNext(jcas.getCas());
			
			try {
				saxBuilder.build(new StringReader(jcas.getDocumentText()));
			} catch (Exception e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			
		}
	}
	
	@Test
	public void testInstanceB() throws UIMAException, IOException, JDOMException, URISyntaxException {
		CollectionReaderDescription collectionReaderDescr = CollectionReaderFactory.createReaderDescription(
				InputCollectionDataReader.class);
		
		/*
		 * Binds the XmlSemeval2016CqaEn to the InputCollectionDataReader. The paramaters are passed by means
		 * of key-value pairs. In this case there are two parameters: XmlSemeval2016CqaEn.FILE_PARAM represents
		 * the input plain text file, and XmlSemeval2016CqaEn.TASK_PARAM defines the type of instances to be
		 * generated (sub-task A, sub-task B or sub-task C).
		 * IMPORTANT: only this line of code must be changed to test other DataReader implementations.
		 */
		ExternalResourceDescription reader = ExternalResourceFactory.createExternalResourceDescription(XmlSemeval2016CqaEn.class,
				XmlSemeval2016CqaEn.FILE_PARAM,"/data/alt/corpora/semeval2016/data/v3.1/SemEval2016-Task3-CQA-QL-dev.oneinstance.xml",
				XmlSemeval2016CqaEn.TASK_PARAM, XmlSemeval2016CqaEn.INSTANCE_B_TASK);
		
		ExternalResourceFactory.bindExternalResource(collectionReaderDescr, 
				InputCollectionDataReader.INPUT_READER_PARAM, reader);
		
		CollectionReader collectionReader = UIMAFramework.produceCollectionReader(collectionReaderDescr);
		
		TypeSystemDescription tsd = 
				  TypeSystemDescriptionFactory.createTypeSystemDescription();
		
		XMLReaderJDOMFactory factory = new XMLReaderXSDFactory(
				new File(InputCollectionDataReader.class.getResource(DataReader.SCHEMA_INSTANCE_B_PATH).toURI()));
		SAXBuilder saxBuilder = new SAXBuilder(factory);
				
		while (collectionReader.hasNext()) {
			JCas jcas = JCasFactory.createJCas(tsd);
			collectionReader.getNext(jcas.getCas());
			
			try {
				saxBuilder.build(new StringReader(jcas.getDocumentText()));
			} catch (Exception e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			
		}
	}
	
	@Test
	public void testInstanceC() throws JDOMException, URISyntaxException, CollectionException, UIMAException, IOException {
		CollectionReaderDescription collectionReaderDescr = CollectionReaderFactory.createReaderDescription(
				InputCollectionDataReader.class);
		
		/*
		 * Binds the XmlSemeval2016CqaEn to the InputCollectionDataReader. The paramaters are passed by means
		 * of key-value pairs. In this case there are two parameters: XmlSemeval2016CqaEn.FILE_PARAM represents
		 * the input plain text file, and XmlSemeval2016CqaEn.TASK_PARAM defines the type of instances to be
		 * generated (sub-task A, sub-task B or sub-task C).
		 * IMPORTANT: only this line of code must be changed to test other DataReader implementations.
		 */
		ExternalResourceDescription reader = ExternalResourceFactory.createExternalResourceDescription(XmlSemeval2016CqaEn.class,
				XmlSemeval2016CqaEn.FILE_PARAM,"/data/alt/corpora/semeval2016/data/v3.1/SemEval2016-Task3-CQA-QL-dev.oneinstance.xml",
				XmlSemeval2016CqaEn.TASK_PARAM, XmlSemeval2016CqaEn.INSTANCE_C_TASK);
		
		ExternalResourceFactory.bindExternalResource(collectionReaderDescr, 
				InputCollectionDataReader.INPUT_READER_PARAM, reader);
		
		CollectionReader collectionReader = UIMAFramework.produceCollectionReader(collectionReaderDescr);
		
		TypeSystemDescription tsd = 
				  TypeSystemDescriptionFactory.createTypeSystemDescription();
		
		XMLReaderJDOMFactory factory = new XMLReaderXSDFactory(
				new File(InputCollectionDataReader.class.getResource(DataReader.SCHEMA_INSTANCE_C_PATH).toURI()));
		SAXBuilder saxBuilder = new SAXBuilder(factory);
				
		while (collectionReader.hasNext()) {
			JCas jcas = JCasFactory.createJCas(tsd);
			collectionReader.getNext(jcas.getCas());
			
			try {
				saxBuilder.build(new StringReader(jcas.getDocumentText()));
			} catch (Exception e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			
		}
	}
	
	public static void main(String[] args) {
		Result result = JUnitCore.runClasses(SemevalXmlDataReaderTest.class);
		for (Failure failure : result.getFailures()) {
			System.out.println(failure.toString());
		}
	}

}

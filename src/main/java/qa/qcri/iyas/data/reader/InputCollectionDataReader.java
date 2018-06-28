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

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.descriptor.OperationalProperties;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderJDOMFactory;
import org.jdom2.input.sax.XMLReaderXSDFactory;

import qa.qcri.iyas.type.AdditionalInfo;

/**
 * 
 * @author Salvatore Romeo
 *
 */
@OperationalProperties(outputsNewCases = true,multipleDeploymentAllowed = false)
@TypeCapability(
		outputs = {"qa.qcri.iyas.types.UserQuestionSubject",
				   "qa.qcri.iyas.types.UserQuestionBody",
				   "qa.qcri.iyas.types.RelatedQuestionSubject",
				   "qa.qcri.iyas.types.RelatedQuestionBody",
				   "qa.qcri.iyas.types.Comment",
				   "qa.qcri.iyas.type.AdditionalInfo"}
)
public class InputCollectionDataReader extends JCasCollectionReader_ImplBase {
	
	public static final String INPUT_READER_PARAM = "InputReader";
	
	private static String identifier;

	@ExternalResource(key = INPUT_READER_PARAM)
	private DataReader reader;
	
	private boolean error = false;
	
	@Override
	public void close() {
		try {
			reader.releaseResources();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void getNext(JCas jcas) throws IOException, CollectionException {
		if (identifier == null) {
			identifier = ""+System.currentTimeMillis();
		}
		
		String nextStr = null;
		try {
			URL schemaURL = null;
			if (reader.getTask().equals(DataReader.INSTANCE_A_TASK))
				schemaURL = InputCollectionDataReader.class.getResource(DataReader.SCHEMA_INSTANCE_A_PATH);
			else if (reader.getTask().equals(DataReader.INSTANCE_B_TASK))
				schemaURL = InputCollectionDataReader.class.getResource(DataReader.SCHEMA_INSTANCE_B_PATH);
			else if (reader.getTask().equals(DataReader.INSTANCE_C_TASK))
				schemaURL = InputCollectionDataReader.class.getResource(DataReader.SCHEMA_INSTANCE_C_PATH);
			
			
			XMLReaderJDOMFactory factory = new XMLReaderXSDFactory(
					schemaURL);
			SAXBuilder saxBuilder = new SAXBuilder(factory);
			nextStr = reader.next();	
			saxBuilder.build(new StringReader(nextStr));

			AdditionalInfo info = new AdditionalInfo(jcas);
			info.setRequesterID(identifier);
			info.setIndex(-1);
			info.setTotalNumberOfExamples(-1);
			info.addToIndexes();
			
			jcas.setDocumentText(nextStr);
		} catch (Exception e) {
			error = true;
			throw new CollectionException(e);
		}
	}
	@Override
	public Progress[] getProgress() {
		return new Progress[]{new ProgressImpl(1,1,Progress.ENTITIES)};
	}
	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return reader.hasNext() & !error;
	}

}

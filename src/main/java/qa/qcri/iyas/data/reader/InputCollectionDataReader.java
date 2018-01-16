/**
 * Copyright 2017 Salvatore Romeo
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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

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
				   "qa.qcri.iyas.types.Comment"}
)
public class InputCollectionDataReader extends JCasCollectionReader_ImplBase {
	
	public static final String INPUT_READER_PARAM = "InputReader";

	@ExternalResource(key = INPUT_READER_PARAM)
	private DataReader reader;
	
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
		try {
			File schemaFile = null;
			if (reader.getTask().equals(DataReader.INSTANCE_A_TASK))
				schemaFile = new File(InputCollectionDataReader.class.getResource(DataReader.SCHEMA_INSTANCE_A_PATH).toURI());
			else if (reader.getTask().equals(DataReader.INSTANCE_B_TASK))
				schemaFile = new File(InputCollectionDataReader.class.getResource(DataReader.SCHEMA_INSTANCE_B_PATH).toURI());
			else if (reader.getTask().equals(DataReader.INSTANCE_C_TASK))
				schemaFile = new File(InputCollectionDataReader.class.getResource(DataReader.SCHEMA_INSTANCE_C_PATH).toURI());
			
			
			XMLReaderJDOMFactory factory = new XMLReaderXSDFactory(
					schemaFile);
			SAXBuilder saxBuilder = new SAXBuilder(factory);

			String nextStr = reader.next();
			
			saxBuilder.build(new StringReader(nextStr));
//			System.out.println(nextStr);
//			
//			System.exit(0);
			
			jcas.setDocumentText(nextStr);

		} catch (Exception e) {
			e.printStackTrace();
			throw new CollectionException();
		}
	}
	@Override
	public Progress[] getProgress() {
		return new Progress[]{new ProgressImpl(1,1,Progress.ENTITIES)};
	}
	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return reader.hasNext();
	}

}

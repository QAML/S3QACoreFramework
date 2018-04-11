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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.OperationalProperties;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

import qa.qcri.iyas.type.AdditionalInfo;

/**
 * 
 * @author Salvatore Romeo
 *
 */
@OperationalProperties(outputsNewCases = true,multipleDeploymentAllowed = false)
public class PreprocessedInputCollectionDataReader extends JCasCollectionReader_ImplBase  {
	
	public static final String INPUT_FILE_PARAM = "InputFile";

	private static String identifier;

	private int index;
	private int totalExamples;
	private BufferedReader in;
	private String currentLine;
	
	@ConfigurationParameter(name = INPUT_FILE_PARAM)
	private String file;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		try {
			in = new BufferedReader(new FileReader(file));
			totalExamples = 0;
			while ((currentLine = in.readLine()) != null)
				totalExamples++;
			in.close();
			
			in = new BufferedReader(new FileReader(file));
			currentLine = in.readLine();
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}
	
	@Override
	public void destroy() {
		try {
			in.close();
		} catch (IOException e) {
//			e.printStackTrace();
		}
		super.destroy();
	}
	
	@Override
	public boolean hasNext() throws IOException, CollectionException {

		return currentLine != null;
	}

	@Override
	public Progress[] getProgress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getNext(JCas jcas) throws IOException, CollectionException {
		if (identifier == null) {
			identifier = ""+System.currentTimeMillis();
		}
		
		AdditionalInfo info = new AdditionalInfo(jcas);
		info.setIndex(index++);
		info.setTotalNumberOfExamples(totalExamples);
		info.setRequesterID(identifier);
		info.addToIndexes();
		
		jcas.setDocumentText(currentLine);
		currentLine = in.readLine();
	}

}

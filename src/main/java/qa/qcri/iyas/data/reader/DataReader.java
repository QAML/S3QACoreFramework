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


import org.apache.uima.fit.component.Resource_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;

public abstract class DataReader extends Resource_ImplBase {
	
	public static final String LANGUAGE_PARAM = "language";
	public static final String TASK_PARAM = "task";
	
	public static final String INSTANCE_A_TASK = "instance_a";
	public static final String INSTANCE_B_TASK = "instance_b";
	public static final String INSTANCE_C_TASK = "instance_c";
	
	public static final String ROOT_TAG = "root";
	public static final String INSTANCE_A_TAG = "instance_a";
	public static final String INSTANCE_B_TAG = "instance_b";
	public static final String INSTANCE_C_TAG = "instance_c";
	public static final String USER_QUESTION_TAG = "user_question";
	public static final String RELATED_QUESTION_TAG = "related_question";
	public static final String RELATED_QUESTION_COMMENTS_TAG = "related_question_comments";
	public static final String SUBJECT_TAG = "subject";
	public static final String BODY_TAG = "body";
	public static final String COMMENT_TAG = "comment";
	public static final String ID_ATTRIBUTE = "id";
	public static final String LANG_ATTRIBUTE = "lang";
	public static final String NUMBER_OF_CANDIDATES_ATTRIBUTE = "numberOfCandidates";
	
	public static final String SCHEMA_PATH = "resources/schemas/cqa_instance_schema.xsd";
	
//	@ConfigurationParameter(name = LANGUAGE_PARAM)
//	protected String language;
//	
	@ConfigurationParameter(name = TASK_PARAM)
	protected String task;
	
	@Override
	public void afterResourcesInitialized() {
		try {
			if (!task.matches(INSTANCE_A_TASK+"|"+INSTANCE_B_TASK+"|"+INSTANCE_C_TASK))
				throw new ResourceInitializationException("Unknown task!",null);
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void init() throws Exception {}
	public void close()  {}
	public abstract boolean hasNext();
	public abstract String next() throws Exception;
	
}
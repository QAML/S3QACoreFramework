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

package qa.qcri.iyas.learning;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.fit.component.JCasMultiplier_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.descriptor.OperationalProperties;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import qa.qcri.iyas.type.AdditionalInfo;
import qa.qcri.iyas.type.Model;

@OperationalProperties(modifiesCas = false, outputsNewCases = true, multipleDeploymentAllowed = true)
public class LearningAnnotator extends JCasMultiplier_ImplBase {
	
	public final static String PARAM_LEARNER_RESOURCE = "learner";
	public final static String PARAM_NAME_MODEL_FILE = "modelFile";
	
	@ExternalResource(key = PARAM_LEARNER_RESOURCE)
	private Learner learner;
	
//	@ConfigurationParameter(name = PARAM_NAME_MODEL_FILE)
//	private String modelFile;
	
	private Map<String,Integer> received;
	private Map<String,Object[]> examples;
	private Map<String,JCas> pendingJCases;
	
	@Override
	public void initialize(final UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		received = new HashMap<>();
		examples = new HashMap<>();
		pendingJCases = new HashMap<>();
	}
	
	@Override
	public boolean hasNext() throws AnalysisEngineProcessException {
		return pendingJCases.size() != 0;
	}

	@Override
	public AbstractCas next() throws AnalysisEngineProcessException {
		String requesterID = pendingJCases.keySet().iterator().next();
		JCas jcas = pendingJCases.remove(requesterID);
		examples.remove(requesterID);
		received.remove(requesterID);
		getContext().getLogger().log(Level.INFO, "Sending back CAS "+jcas.getDocumentText()+" to requester "+requesterID);
		return jcas;
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		Collection<AdditionalInfo> infos = JCasUtil.select(jcas, AdditionalInfo.class);
		if (infos.size() != 1)
			throw new AnalysisEngineProcessException("Expected an AdditionalInfo annotation, found "+infos.size(),null);
		
		AdditionalInfo info = infos.iterator().next();
		
		if (info.getRequesterID() == null)
			throw new AnalysisEngineProcessException("Requerer ID not set", null);
		
		if (examples.get(info.getRequesterID()) == null) {
			examples.put(info.getRequesterID(),new Object[info.getTotalNumberOfExamples()]);
			received.put(info.getRequesterID(), 0);
		}
		if (examples.get(info.getRequesterID())[info.getIndex()] != null)
			throw new AnalysisEngineProcessException("Example "+info.getIndex()+" for requester "+info.getRequesterID()+" has already been set",null);
		
		examples.get(info.getRequesterID())[info.getIndex()] = learner.extractExample(jcas);
		received.put(info.getRequesterID(), received.get(info.getRequesterID())+1);
		getContext().getLogger().log(Level.INFO, "received "+received.get(info.getRequesterID())+" over "+examples.get(info.getRequesterID()).length+
				" examples for requester "+info.getRequesterID());
		
		if (examples.get(info.getRequesterID()).length == received.get(info.getRequesterID())) {
			getContext().getLogger().log(Level.INFO, "Starting learning for requester "+info.getRequesterID());
			String output = learner.learn(examples.get(info.getRequesterID()));
			examples.remove(info.getRequesterID());
			received.remove(info.getRequesterID());
			
			long id = System.currentTimeMillis();
			
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(id+".mdl"));
				out.write(output);
				out.newLine();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			JCas newJcas = getEmptyJCas();
			Model model = new Model(newJcas);
			model.setFile(id+".mdl");
			model.addToIndexes();
			
			pendingJCases.put(info.getRequesterID(), newJcas);
		}
			
	}
}

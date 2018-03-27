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

package qa.qcri.iyas.feature;

import static org.junit.Assert.fail;

import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.EntityProcessStatus;
import org.apache.uima.fit.util.JCasUtil;

import qa.qcri.iyas.type.AdditionalInfo;

public class ClassificationStatusCallBackListener extends UimaAsBaseCallbackListener {
	@Override
	public void entityProcessComplete(CAS cas, EntityProcessStatus aStatus) {
		if (!aStatus.getStatusMessage().equals("success")) {
			fail(aStatus.getStatusMessage());
			System.exit(0);
		} else {
			try {
				if (JCasUtil.exists(cas.getJCas(), AdditionalInfo.class)) {
					AdditionalInfo info = JCasUtil.select(cas.getJCas(), AdditionalInfo.class).iterator().next();
					System.out.println(info.getIndex() + " " + info.getTotalNumberOfExamples()
					+ " " + info.getInstanceID() + " " + info.getPrediction());
				} else  {
//					System.out.println(cas.getDocumentText());
				}
			} catch (CASException e) {
				e.printStackTrace();
			}
		}
	}
}

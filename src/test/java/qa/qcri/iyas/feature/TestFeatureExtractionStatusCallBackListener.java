package qa.qcri.iyas.feature;

import static org.junit.Assert.fail;

import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.EntityProcessStatus;
import org.apache.uima.fit.util.JCasUtil;

import qa.qcri.iyas.type.AdditionalInfo;
import qa.qcri.iyas.type.Model;

public class TestFeatureExtractionStatusCallBackListener extends UimaAsBaseCallbackListener  {
	
	public String file;
	
	@Override
	public void entityProcessComplete(CAS cas, EntityProcessStatus aStatus) {
		if (!aStatus.getStatusMessage().equals("success")) {
			fail(aStatus.getStatusMessage());
		} else {
			try {
				if (JCasUtil.exists(cas.getJCas(), AdditionalInfo.class)) {
					AdditionalInfo info = JCasUtil.select(cas.getJCas(), AdditionalInfo.class).iterator().next();
					System.out.println(info.getIndex() + " " + info.getTotalNumberOfExamples()
					+ " " + info.getInstanceID() + " " + cas.getDocumentText());
				} else if (JCasUtil.exists(cas.getJCas(), Model.class)) {
					file = JCasUtil.select(cas.getJCas(), Model.class).iterator().next().getFile();
				}
			} catch (CASException e) {
				e.printStackTrace();
			}
		}
	}
}

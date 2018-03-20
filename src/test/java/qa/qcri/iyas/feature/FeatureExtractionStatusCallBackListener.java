package qa.qcri.iyas.feature;

import static org.junit.Assert.fail;

import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.collection.EntityProcessStatus;
import org.apache.uima.jcas.tcas.Annotation;

public class FeatureExtractionStatusCallBackListener extends UimaAsBaseCallbackListener  {

	@Override
	public void entityProcessComplete(CAS cas, EntityProcessStatus aStatus) {
		if (!aStatus.getStatusMessage().equals("success")) {
			fail(aStatus.getStatusMessage());
		} else {
			System.out.println(cas.getDocumentText());
		}
	}
}

package qa.qcri.iyas.feature.similarity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.xml.sax.SAXException;

public class SimilarityMeasureTestAnnotatorDescriptorGenerator {
	
	public static final String ROOT_DESCRIPTORS_FOLDER = "src/test/resources/descriptors";

	public static void main(String args[]) throws FileNotFoundException, SAXException, IOException, InvalidXMLException, ResourceInitializationException {
		System.out.println("Generating XML description for SimilarityTestAnnotatorAE_Descriptor");
		AnalysisEngineDescription similarityTestAnnotatorAE_Descriptor = AnalysisEngineFactory.createEngineDescription(
				SimilarityMeasureTestAnnotator.class);
		ExternalResourceFactory.bindResource(similarityTestAnnotatorAE_Descriptor,
				SimilarityMeasureTestAnnotator.PARAM_SIMILARITY_RESOURCE, SimilarityMeasureExample.class,"",
				ExternalResourceFactory.PARAM_RESOURCE_NAME,"similarityMeasureExample",
				SimilarityMeasureExample.PARAM_NAME_1, "test_param",
				SimilarityMeasureExample.PARAM_NAME_FAKE_SIM_TO_GENERATE, 1.0);
		similarityTestAnnotatorAE_Descriptor.toXML(
				new FileOutputStream(ROOT_DESCRIPTORS_FOLDER+"/qa/qcri/iyas/feature/SimilarityTestAnnotatorAE_Descriptor.xml"));
	}
}

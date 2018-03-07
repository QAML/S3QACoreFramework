package qa.qcri.iyas.util;

public class FeatureNameArray {
	private String names[];
	
	/**
	 * 
	 * @param names comma-separated names
	 */
	public FeatureNameArray(String names) {
		this.names = names.split(",");
	}
	
	public String[] getName() {
		return names;
	}
}

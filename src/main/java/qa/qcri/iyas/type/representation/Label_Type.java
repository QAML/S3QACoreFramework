
/* First created by JCasGen Tue Mar 20 16:25:50 CET 2018 */
package qa.qcri.iyas.type.representation;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Tue Mar 20 16:27:12 CET 2018
 * @generated */
public class Label_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Label.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("qa.qcri.iyas.type.representation.Label");
 
  /** @generated */
  final Feature casFeat_labels;
  /** @generated */
  final int     casFeatCode_labels;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getLabels(int addr) {
        if (featOkTst && casFeat_labels == null)
      jcas.throwFeatMissing("labels", "qa.qcri.iyas.type.representation.Label");
    return ll_cas.ll_getRefValue(addr, casFeatCode_labels);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setLabels(int addr, int v) {
        if (featOkTst && casFeat_labels == null)
      jcas.throwFeatMissing("labels", "qa.qcri.iyas.type.representation.Label");
    ll_cas.ll_setRefValue(addr, casFeatCode_labels, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public String getLabels(int addr, int i) {
        if (featOkTst && casFeat_labels == null)
      jcas.throwFeatMissing("labels", "qa.qcri.iyas.type.representation.Label");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_labels), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_labels), i);
  return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_labels), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setLabels(int addr, int i, String v) {
        if (featOkTst && casFeat_labels == null)
      jcas.throwFeatMissing("labels", "qa.qcri.iyas.type.representation.Label");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_labels), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_labels), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_labels), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Label_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_labels = jcas.getRequiredFeatureDE(casType, "labels", "uima.cas.StringArray", featOkTst);
    casFeatCode_labels  = (null == casFeat_labels) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_labels).getCode();

  }
}



    

/* First created by JCasGen Tue Mar 20 15:20:49 CET 2018 */
package qa.qcri.iyas.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Thu Mar 22 15:29:39 CET 2018
 * @generated */
public class AdditionalInfo_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = AdditionalInfo.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("qa.qcri.iyas.type.AdditionalInfo");
 
  /** @generated */
  final Feature casFeat_index;
  /** @generated */
  final int     casFeatCode_index;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getIndex(int addr) {
        if (featOkTst && casFeat_index == null)
      jcas.throwFeatMissing("index", "qa.qcri.iyas.type.AdditionalInfo");
    return ll_cas.ll_getIntValue(addr, casFeatCode_index);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setIndex(int addr, int v) {
        if (featOkTst && casFeat_index == null)
      jcas.throwFeatMissing("index", "qa.qcri.iyas.type.AdditionalInfo");
    ll_cas.ll_setIntValue(addr, casFeatCode_index, v);}
    
  
 
  /** @generated */
  final Feature casFeat_totalNumberOfExamples;
  /** @generated */
  final int     casFeatCode_totalNumberOfExamples;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getTotalNumberOfExamples(int addr) {
        if (featOkTst && casFeat_totalNumberOfExamples == null)
      jcas.throwFeatMissing("totalNumberOfExamples", "qa.qcri.iyas.type.AdditionalInfo");
    return ll_cas.ll_getIntValue(addr, casFeatCode_totalNumberOfExamples);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTotalNumberOfExamples(int addr, int v) {
        if (featOkTst && casFeat_totalNumberOfExamples == null)
      jcas.throwFeatMissing("totalNumberOfExamples", "qa.qcri.iyas.type.AdditionalInfo");
    ll_cas.ll_setIntValue(addr, casFeatCode_totalNumberOfExamples, v);}
    
  
 
  /** @generated */
  final Feature casFeat_requesterID;
  /** @generated */
  final int     casFeatCode_requesterID;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getRequesterID(int addr) {
        if (featOkTst && casFeat_requesterID == null)
      jcas.throwFeatMissing("requesterID", "qa.qcri.iyas.type.AdditionalInfo");
    return ll_cas.ll_getStringValue(addr, casFeatCode_requesterID);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setRequesterID(int addr, String v) {
        if (featOkTst && casFeat_requesterID == null)
      jcas.throwFeatMissing("requesterID", "qa.qcri.iyas.type.AdditionalInfo");
    ll_cas.ll_setStringValue(addr, casFeatCode_requesterID, v);}
    
  
 
  /** @generated */
  final Feature casFeat_instanceID;
  /** @generated */
  final int     casFeatCode_instanceID;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getInstanceID(int addr) {
        if (featOkTst && casFeat_instanceID == null)
      jcas.throwFeatMissing("instanceID", "qa.qcri.iyas.type.AdditionalInfo");
    return ll_cas.ll_getStringValue(addr, casFeatCode_instanceID);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setInstanceID(int addr, String v) {
        if (featOkTst && casFeat_instanceID == null)
      jcas.throwFeatMissing("instanceID", "qa.qcri.iyas.type.AdditionalInfo");
    ll_cas.ll_setStringValue(addr, casFeatCode_instanceID, v);}
    
  
 
  /** @generated */
  final Feature casFeat_prediction;
  /** @generated */
  final int     casFeatCode_prediction;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getPrediction(int addr) {
        if (featOkTst && casFeat_prediction == null)
      jcas.throwFeatMissing("prediction", "qa.qcri.iyas.type.AdditionalInfo");
    return ll_cas.ll_getStringValue(addr, casFeatCode_prediction);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setPrediction(int addr, String v) {
        if (featOkTst && casFeat_prediction == null)
      jcas.throwFeatMissing("prediction", "qa.qcri.iyas.type.AdditionalInfo");
    ll_cas.ll_setStringValue(addr, casFeatCode_prediction, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public AdditionalInfo_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_index = jcas.getRequiredFeatureDE(casType, "index", "uima.cas.Integer", featOkTst);
    casFeatCode_index  = (null == casFeat_index) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_index).getCode();

 
    casFeat_totalNumberOfExamples = jcas.getRequiredFeatureDE(casType, "totalNumberOfExamples", "uima.cas.Integer", featOkTst);
    casFeatCode_totalNumberOfExamples  = (null == casFeat_totalNumberOfExamples) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_totalNumberOfExamples).getCode();

 
    casFeat_requesterID = jcas.getRequiredFeatureDE(casType, "requesterID", "uima.cas.String", featOkTst);
    casFeatCode_requesterID  = (null == casFeat_requesterID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_requesterID).getCode();

 
    casFeat_instanceID = jcas.getRequiredFeatureDE(casType, "instanceID", "uima.cas.String", featOkTst);
    casFeatCode_instanceID  = (null == casFeat_instanceID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_instanceID).getCode();

 
    casFeat_prediction = jcas.getRequiredFeatureDE(casType, "prediction", "uima.cas.String", featOkTst);
    casFeatCode_prediction  = (null == casFeat_prediction) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_prediction).getCode();

  }
}



    
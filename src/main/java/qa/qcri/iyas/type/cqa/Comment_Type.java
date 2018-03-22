
/* First created by JCasGen Sun Mar 04 15:52:51 CET 2018 */
package qa.qcri.iyas.type.cqa;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** 
 * Updated by JCasGen Thu Mar 22 09:14:34 CET 2018
 * @generated */
public class Comment_Type extends QAAnnotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Comment.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("qa.qcri.iyas.type.cqa.Comment");
 
  /** @generated */
  final Feature casFeat_questionID;
  /** @generated */
  final int     casFeatCode_questionID;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getQuestionID(int addr) {
        if (featOkTst && casFeat_questionID == null)
      jcas.throwFeatMissing("questionID", "qa.qcri.iyas.type.cqa.Comment");
    return ll_cas.ll_getStringValue(addr, casFeatCode_questionID);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setQuestionID(int addr, String v) {
        if (featOkTst && casFeat_questionID == null)
      jcas.throwFeatMissing("questionID", "qa.qcri.iyas.type.cqa.Comment");
    ll_cas.ll_setStringValue(addr, casFeatCode_questionID, v);}
    
  
 
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
      jcas.throwFeatMissing("labels", "qa.qcri.iyas.type.cqa.Comment");
    return ll_cas.ll_getRefValue(addr, casFeatCode_labels);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setLabels(int addr, int v) {
        if (featOkTst && casFeat_labels == null)
      jcas.throwFeatMissing("labels", "qa.qcri.iyas.type.cqa.Comment");
    ll_cas.ll_setRefValue(addr, casFeatCode_labels, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public String getLabels(int addr, int i) {
        if (featOkTst && casFeat_labels == null)
      jcas.throwFeatMissing("labels", "qa.qcri.iyas.type.cqa.Comment");
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
      jcas.throwFeatMissing("labels", "qa.qcri.iyas.type.cqa.Comment");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_labels), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_labels), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_labels), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_rank;
  /** @generated */
  final int     casFeatCode_rank;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getRank(int addr) {
        if (featOkTst && casFeat_rank == null)
      jcas.throwFeatMissing("rank", "qa.qcri.iyas.type.cqa.Comment");
    return ll_cas.ll_getIntValue(addr, casFeatCode_rank);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setRank(int addr, int v) {
        if (featOkTst && casFeat_rank == null)
      jcas.throwFeatMissing("rank", "qa.qcri.iyas.type.cqa.Comment");
    ll_cas.ll_setIntValue(addr, casFeatCode_rank, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Comment_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_questionID = jcas.getRequiredFeatureDE(casType, "questionID", "uima.cas.String", featOkTst);
    casFeatCode_questionID  = (null == casFeat_questionID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_questionID).getCode();

 
    casFeat_labels = jcas.getRequiredFeatureDE(casType, "labels", "uima.cas.StringArray", featOkTst);
    casFeatCode_labels  = (null == casFeat_labels) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_labels).getCode();

 
    casFeat_rank = jcas.getRequiredFeatureDE(casType, "rank", "uima.cas.Integer", featOkTst);
    casFeatCode_rank  = (null == casFeat_rank) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_rank).getCode();

  }
}



    
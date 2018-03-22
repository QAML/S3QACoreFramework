
/* First created by JCasGen Sun Mar 04 15:52:51 CET 2018 */
package qa.qcri.iyas.type.cqa;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Thu Mar 22 09:14:35 CET 2018
 * @generated */
public class RelatedQuestion_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = RelatedQuestion.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("qa.qcri.iyas.type.cqa.RelatedQuestion");
 
  /** @generated */
  final Feature casFeat_concatenated;
  /** @generated */
  final int     casFeatCode_concatenated;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public boolean getConcatenated(int addr) {
        if (featOkTst && casFeat_concatenated == null)
      jcas.throwFeatMissing("concatenated", "qa.qcri.iyas.type.cqa.RelatedQuestion");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_concatenated);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setConcatenated(int addr, boolean v) {
        if (featOkTst && casFeat_concatenated == null)
      jcas.throwFeatMissing("concatenated", "qa.qcri.iyas.type.cqa.RelatedQuestion");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_concatenated, v);}
    
  
 
  /** @generated */
  final Feature casFeat_candidateIDs;
  /** @generated */
  final int     casFeatCode_candidateIDs;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getCandidateIDs(int addr) {
        if (featOkTst && casFeat_candidateIDs == null)
      jcas.throwFeatMissing("candidateIDs", "qa.qcri.iyas.type.cqa.RelatedQuestion");
    return ll_cas.ll_getRefValue(addr, casFeatCode_candidateIDs);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setCandidateIDs(int addr, int v) {
        if (featOkTst && casFeat_candidateIDs == null)
      jcas.throwFeatMissing("candidateIDs", "qa.qcri.iyas.type.cqa.RelatedQuestion");
    ll_cas.ll_setRefValue(addr, casFeatCode_candidateIDs, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public String getCandidateIDs(int addr, int i) {
        if (featOkTst && casFeat_candidateIDs == null)
      jcas.throwFeatMissing("candidateIDs", "qa.qcri.iyas.type.cqa.RelatedQuestion");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_candidateIDs), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_candidateIDs), i);
  return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_candidateIDs), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setCandidateIDs(int addr, int i, String v) {
        if (featOkTst && casFeat_candidateIDs == null)
      jcas.throwFeatMissing("candidateIDs", "qa.qcri.iyas.type.cqa.RelatedQuestion");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_candidateIDs), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_candidateIDs), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_candidateIDs), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_ID;
  /** @generated */
  final int     casFeatCode_ID;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getID(int addr) {
        if (featOkTst && casFeat_ID == null)
      jcas.throwFeatMissing("ID", "qa.qcri.iyas.type.cqa.RelatedQuestion");
    return ll_cas.ll_getStringValue(addr, casFeatCode_ID);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setID(int addr, String v) {
        if (featOkTst && casFeat_ID == null)
      jcas.throwFeatMissing("ID", "qa.qcri.iyas.type.cqa.RelatedQuestion");
    ll_cas.ll_setStringValue(addr, casFeatCode_ID, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public RelatedQuestion_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_concatenated = jcas.getRequiredFeatureDE(casType, "concatenated", "uima.cas.Boolean", featOkTst);
    casFeatCode_concatenated  = (null == casFeat_concatenated) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_concatenated).getCode();

 
    casFeat_candidateIDs = jcas.getRequiredFeatureDE(casType, "candidateIDs", "uima.cas.StringArray", featOkTst);
    casFeatCode_candidateIDs  = (null == casFeat_candidateIDs) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_candidateIDs).getCode();

 
    casFeat_ID = jcas.getRequiredFeatureDE(casType, "ID", "uima.cas.String", featOkTst);
    casFeatCode_ID  = (null == casFeat_ID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_ID).getCode();

  }
}



    

/* First created by JCasGen Mon Oct 30 14:47:28 AST 2017 */
package qa.qcri.iyas.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Tue Oct 31 16:46:26 AST 2017
 * @generated */
public class RelatedQuestion_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = RelatedQuestion.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("qa.qcri.iyas.types.RelatedQuestion");
 
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
      jcas.throwFeatMissing("concatenated", "qa.qcri.iyas.types.RelatedQuestion");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_concatenated);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setConcatenated(int addr, boolean v) {
        if (featOkTst && casFeat_concatenated == null)
      jcas.throwFeatMissing("concatenated", "qa.qcri.iyas.types.RelatedQuestion");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_concatenated, v);}
    
  
 
  /** @generated */
  final Feature casFeat_candidateViewNames;
  /** @generated */
  final int     casFeatCode_candidateViewNames;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getCandidateViewNames(int addr) {
        if (featOkTst && casFeat_candidateViewNames == null)
      jcas.throwFeatMissing("candidateViewNames", "qa.qcri.iyas.types.RelatedQuestion");
    return ll_cas.ll_getRefValue(addr, casFeatCode_candidateViewNames);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setCandidateViewNames(int addr, int v) {
        if (featOkTst && casFeat_candidateViewNames == null)
      jcas.throwFeatMissing("candidateViewNames", "qa.qcri.iyas.types.RelatedQuestion");
    ll_cas.ll_setRefValue(addr, casFeatCode_candidateViewNames, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public String getCandidateViewNames(int addr, int i) {
        if (featOkTst && casFeat_candidateViewNames == null)
      jcas.throwFeatMissing("candidateViewNames", "qa.qcri.iyas.types.RelatedQuestion");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_candidateViewNames), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_candidateViewNames), i);
  return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_candidateViewNames), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setCandidateViewNames(int addr, int i, String v) {
        if (featOkTst && casFeat_candidateViewNames == null)
      jcas.throwFeatMissing("candidateViewNames", "qa.qcri.iyas.types.RelatedQuestion");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_candidateViewNames), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_candidateViewNames), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_candidateViewNames), i, v);
  }
 



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

 
    casFeat_candidateViewNames = jcas.getRequiredFeatureDE(casType, "candidateViewNames", "uima.cas.StringArray", featOkTst);
    casFeatCode_candidateViewNames  = (null == casFeat_candidateViewNames) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_candidateViewNames).getCode();

  }
}



    
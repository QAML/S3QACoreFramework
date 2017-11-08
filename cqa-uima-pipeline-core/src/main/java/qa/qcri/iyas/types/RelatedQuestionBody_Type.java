
/* First created by JCasGen Mon Oct 30 14:47:28 AST 2017 */
package qa.qcri.iyas.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** 
 * Updated by JCasGen Tue Oct 31 16:46:26 AST 2017
 * @generated */
public class RelatedQuestionBody_Type extends QAAnnotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = RelatedQuestionBody.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("qa.qcri.iyas.types.RelatedQuestionBody");
 
  /** @generated */
  final Feature casFeat_numberOfCandidates;
  /** @generated */
  final int     casFeatCode_numberOfCandidates;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getNumberOfCandidates(int addr) {
        if (featOkTst && casFeat_numberOfCandidates == null)
      jcas.throwFeatMissing("numberOfCandidates", "qa.qcri.iyas.types.RelatedQuestionBody");
    return ll_cas.ll_getIntValue(addr, casFeatCode_numberOfCandidates);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setNumberOfCandidates(int addr, int v) {
        if (featOkTst && casFeat_numberOfCandidates == null)
      jcas.throwFeatMissing("numberOfCandidates", "qa.qcri.iyas.types.RelatedQuestionBody");
    ll_cas.ll_setIntValue(addr, casFeatCode_numberOfCandidates, v);}
    
  
 
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
      jcas.throwFeatMissing("concatenated", "qa.qcri.iyas.types.RelatedQuestionBody");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_concatenated);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setConcatenated(int addr, boolean v) {
        if (featOkTst && casFeat_concatenated == null)
      jcas.throwFeatMissing("concatenated", "qa.qcri.iyas.types.RelatedQuestionBody");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_concatenated, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public RelatedQuestionBody_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_numberOfCandidates = jcas.getRequiredFeatureDE(casType, "numberOfCandidates", "uima.cas.Integer", featOkTst);
    casFeatCode_numberOfCandidates  = (null == casFeat_numberOfCandidates) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_numberOfCandidates).getCode();

 
    casFeat_concatenated = jcas.getRequiredFeatureDE(casType, "concatenated", "uima.cas.Boolean", featOkTst);
    casFeatCode_concatenated  = (null == casFeat_concatenated) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_concatenated).getCode();

  }
}



    
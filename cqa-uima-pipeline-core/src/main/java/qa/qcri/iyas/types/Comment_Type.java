
/* First created by JCasGen Thu Nov 09 13:31:39 AST 2017 */
package qa.qcri.iyas.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** 
 * Updated by JCasGen Thu Nov 09 13:31:39 AST 2017
 * @generated */
public class Comment_Type extends QAAnnotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Comment.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("qa.qcri.iyas.types.Comment");
 
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
      jcas.throwFeatMissing("questionID", "qa.qcri.iyas.types.Comment");
    return ll_cas.ll_getStringValue(addr, casFeatCode_questionID);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setQuestionID(int addr, String v) {
        if (featOkTst && casFeat_questionID == null)
      jcas.throwFeatMissing("questionID", "qa.qcri.iyas.types.Comment");
    ll_cas.ll_setStringValue(addr, casFeatCode_questionID, v);}
    
  



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

  }
}



    
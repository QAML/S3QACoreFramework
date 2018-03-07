
/* First created by JCasGen Wed Mar 07 16:46:36 CET 2018 */
package qa.qcri.iyas.type.representation;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** 
 * Updated by JCasGen Wed Mar 07 16:46:36 CET 2018
 * @generated */
public class Tree_Type extends Represenation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Tree.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("qa.qcri.iyas.type.representation.Tree");
 
  /** @generated */
  final Feature casFeat_stringRepresentaion;
  /** @generated */
  final int     casFeatCode_stringRepresentaion;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getStringRepresentaion(int addr) {
        if (featOkTst && casFeat_stringRepresentaion == null)
      jcas.throwFeatMissing("stringRepresentaion", "qa.qcri.iyas.type.representation.Tree");
    return ll_cas.ll_getStringValue(addr, casFeatCode_stringRepresentaion);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setStringRepresentaion(int addr, String v) {
        if (featOkTst && casFeat_stringRepresentaion == null)
      jcas.throwFeatMissing("stringRepresentaion", "qa.qcri.iyas.type.representation.Tree");
    ll_cas.ll_setStringValue(addr, casFeatCode_stringRepresentaion, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Tree_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_stringRepresentaion = jcas.getRequiredFeatureDE(casType, "stringRepresentaion", "uima.cas.String", featOkTst);
    casFeatCode_stringRepresentaion  = (null == casFeat_stringRepresentaion) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_stringRepresentaion).getCode();

  }
}



    
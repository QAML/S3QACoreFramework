
/* First created by JCasGen Mon Mar 19 07:46:39 CET 2018 */
package qa.qcri.iyas.type.representation;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** 
 * Updated by JCasGen Tue Mar 20 16:27:12 CET 2018
 * @generated */
public class Tree_Type extends Representation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Tree.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("qa.qcri.iyas.type.representation.Tree");
 
  /** @generated */
  final Feature casFeat_casView;
  /** @generated */
  final int     casFeatCode_casView;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getCasView(int addr) {
        if (featOkTst && casFeat_casView == null)
      jcas.throwFeatMissing("casView", "qa.qcri.iyas.type.representation.Tree");
    return ll_cas.ll_getStringValue(addr, casFeatCode_casView);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setCasView(int addr, String v) {
        if (featOkTst && casFeat_casView == null)
      jcas.throwFeatMissing("casView", "qa.qcri.iyas.type.representation.Tree");
    ll_cas.ll_setStringValue(addr, casFeatCode_casView, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Tree_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_casView = jcas.getRequiredFeatureDE(casType, "casView", "uima.cas.String", featOkTst);
    casFeatCode_casView  = (null == casFeat_casView) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_casView).getCode();

  }
}



    
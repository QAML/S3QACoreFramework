
/* First created by JCasGen Sun Jan 14 10:24:26 AST 2018 */
package qa.qcri.iyas.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Sun Feb 25 15:10:21 AST 2018
 * @generated */
public class Similarity_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Similarity.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("qa.qcri.iyas.type.Similarity");
 
  /** @generated */
  final Feature casFeat_name;
  /** @generated */
  final int     casFeatCode_name;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getName(int addr) {
        if (featOkTst && casFeat_name == null)
      jcas.throwFeatMissing("name", "qa.qcri.iyas.type.Similarity");
    return ll_cas.ll_getStringValue(addr, casFeatCode_name);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setName(int addr, String v) {
        if (featOkTst && casFeat_name == null)
      jcas.throwFeatMissing("name", "qa.qcri.iyas.type.Similarity");
    ll_cas.ll_setStringValue(addr, casFeatCode_name, v);}
    
  
 
  /** @generated */
  final Feature casFeat_leftViewName;
  /** @generated */
  final int     casFeatCode_leftViewName;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getLeftViewName(int addr) {
        if (featOkTst && casFeat_leftViewName == null)
      jcas.throwFeatMissing("leftViewName", "qa.qcri.iyas.type.Similarity");
    return ll_cas.ll_getStringValue(addr, casFeatCode_leftViewName);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setLeftViewName(int addr, String v) {
        if (featOkTst && casFeat_leftViewName == null)
      jcas.throwFeatMissing("leftViewName", "qa.qcri.iyas.type.Similarity");
    ll_cas.ll_setStringValue(addr, casFeatCode_leftViewName, v);}
    
  
 
  /** @generated */
  final Feature casFeat_rightViewName;
  /** @generated */
  final int     casFeatCode_rightViewName;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getRightViewName(int addr) {
        if (featOkTst && casFeat_rightViewName == null)
      jcas.throwFeatMissing("rightViewName", "qa.qcri.iyas.type.Similarity");
    return ll_cas.ll_getStringValue(addr, casFeatCode_rightViewName);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setRightViewName(int addr, String v) {
        if (featOkTst && casFeat_rightViewName == null)
      jcas.throwFeatMissing("rightViewName", "qa.qcri.iyas.type.Similarity");
    ll_cas.ll_setStringValue(addr, casFeatCode_rightViewName, v);}
    
  
 
  /** @generated */
  final Feature casFeat_value;
  /** @generated */
  final int     casFeatCode_value;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public double getValue(int addr) {
        if (featOkTst && casFeat_value == null)
      jcas.throwFeatMissing("value", "qa.qcri.iyas.type.Similarity");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_value);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setValue(int addr, double v) {
        if (featOkTst && casFeat_value == null)
      jcas.throwFeatMissing("value", "qa.qcri.iyas.type.Similarity");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_value, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Similarity_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_name = jcas.getRequiredFeatureDE(casType, "name", "uima.cas.String", featOkTst);
    casFeatCode_name  = (null == casFeat_name) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_name).getCode();

 
    casFeat_leftViewName = jcas.getRequiredFeatureDE(casType, "leftViewName", "uima.cas.String", featOkTst);
    casFeatCode_leftViewName  = (null == casFeat_leftViewName) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_leftViewName).getCode();

 
    casFeat_rightViewName = jcas.getRequiredFeatureDE(casType, "rightViewName", "uima.cas.String", featOkTst);
    casFeatCode_rightViewName  = (null == casFeat_rightViewName) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_rightViewName).getCode();

 
    casFeat_value = jcas.getRequiredFeatureDE(casType, "value", "uima.cas.Double", featOkTst);
    casFeatCode_value  = (null == casFeat_value) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_value).getCode();

  }
}



    
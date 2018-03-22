
/* First created by JCasGen Mon Mar 19 07:46:39 CET 2018 */
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
public class NumericFeature_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = NumericFeature.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("qa.qcri.iyas.type.representation.NumericFeature");
 
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
      jcas.throwFeatMissing("index", "qa.qcri.iyas.type.representation.NumericFeature");
    return ll_cas.ll_getIntValue(addr, casFeatCode_index);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setIndex(int addr, int v) {
        if (featOkTst && casFeat_index == null)
      jcas.throwFeatMissing("index", "qa.qcri.iyas.type.representation.NumericFeature");
    ll_cas.ll_setIntValue(addr, casFeatCode_index, v);}
    
  
 
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
      jcas.throwFeatMissing("value", "qa.qcri.iyas.type.representation.NumericFeature");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_value);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setValue(int addr, double v) {
        if (featOkTst && casFeat_value == null)
      jcas.throwFeatMissing("value", "qa.qcri.iyas.type.representation.NumericFeature");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_value, v);}
    
  
 
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
      jcas.throwFeatMissing("name", "qa.qcri.iyas.type.representation.NumericFeature");
    return ll_cas.ll_getStringValue(addr, casFeatCode_name);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setName(int addr, String v) {
        if (featOkTst && casFeat_name == null)
      jcas.throwFeatMissing("name", "qa.qcri.iyas.type.representation.NumericFeature");
    ll_cas.ll_setStringValue(addr, casFeatCode_name, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public NumericFeature_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_index = jcas.getRequiredFeatureDE(casType, "index", "uima.cas.Integer", featOkTst);
    casFeatCode_index  = (null == casFeat_index) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_index).getCode();

 
    casFeat_value = jcas.getRequiredFeatureDE(casType, "value", "uima.cas.Double", featOkTst);
    casFeatCode_value  = (null == casFeat_value) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_value).getCode();

 
    casFeat_name = jcas.getRequiredFeatureDE(casType, "name", "uima.cas.String", featOkTst);
    casFeatCode_name  = (null == casFeat_name) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_name).getCode();

  }
}



    
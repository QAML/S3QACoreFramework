
/* First created by JCasGen Wed Mar 21 14:30:44 CET 2018 */
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
public class Model_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Model.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("qa.qcri.iyas.type.Model");
 
  /** @generated */
  final Feature casFeat_file;
  /** @generated */
  final int     casFeatCode_file;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getFile(int addr) {
        if (featOkTst && casFeat_file == null)
      jcas.throwFeatMissing("file", "qa.qcri.iyas.type.Model");
    return ll_cas.ll_getStringValue(addr, casFeatCode_file);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setFile(int addr, String v) {
        if (featOkTst && casFeat_file == null)
      jcas.throwFeatMissing("file", "qa.qcri.iyas.type.Model");
    ll_cas.ll_setStringValue(addr, casFeatCode_file, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Model_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_file = jcas.getRequiredFeatureDE(casType, "file", "uima.cas.String", featOkTst);
    casFeatCode_file  = (null == casFeat_file) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_file).getCode();

  }
}



    
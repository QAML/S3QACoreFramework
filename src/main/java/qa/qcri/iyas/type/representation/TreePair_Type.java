
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
public class TreePair_Type extends Representation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = TreePair.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("qa.qcri.iyas.type.representation.TreePair");
 
  /** @generated */
  final Feature casFeat_leftTree;
  /** @generated */
  final int     casFeatCode_leftTree;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getLeftTree(int addr) {
        if (featOkTst && casFeat_leftTree == null)
      jcas.throwFeatMissing("leftTree", "qa.qcri.iyas.type.representation.TreePair");
    return ll_cas.ll_getRefValue(addr, casFeatCode_leftTree);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setLeftTree(int addr, int v) {
        if (featOkTst && casFeat_leftTree == null)
      jcas.throwFeatMissing("leftTree", "qa.qcri.iyas.type.representation.TreePair");
    ll_cas.ll_setRefValue(addr, casFeatCode_leftTree, v);}
    
  
 
  /** @generated */
  final Feature casFeat_rightTree;
  /** @generated */
  final int     casFeatCode_rightTree;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getRightTree(int addr) {
        if (featOkTst && casFeat_rightTree == null)
      jcas.throwFeatMissing("rightTree", "qa.qcri.iyas.type.representation.TreePair");
    return ll_cas.ll_getRefValue(addr, casFeatCode_rightTree);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setRightTree(int addr, int v) {
        if (featOkTst && casFeat_rightTree == null)
      jcas.throwFeatMissing("rightTree", "qa.qcri.iyas.type.representation.TreePair");
    ll_cas.ll_setRefValue(addr, casFeatCode_rightTree, v);}
    
  
 
  /** @generated */
  final Feature casFeat_treeName;
  /** @generated */
  final int     casFeatCode_treeName;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getTreeName(int addr) {
        if (featOkTst && casFeat_treeName == null)
      jcas.throwFeatMissing("treeName", "qa.qcri.iyas.type.representation.TreePair");
    return ll_cas.ll_getStringValue(addr, casFeatCode_treeName);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTreeName(int addr, String v) {
        if (featOkTst && casFeat_treeName == null)
      jcas.throwFeatMissing("treeName", "qa.qcri.iyas.type.representation.TreePair");
    ll_cas.ll_setStringValue(addr, casFeatCode_treeName, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public TreePair_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_leftTree = jcas.getRequiredFeatureDE(casType, "leftTree", "qa.qcri.iyas.type.representation.Tree", featOkTst);
    casFeatCode_leftTree  = (null == casFeat_leftTree) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_leftTree).getCode();

 
    casFeat_rightTree = jcas.getRequiredFeatureDE(casType, "rightTree", "qa.qcri.iyas.type.representation.Tree", featOkTst);
    casFeatCode_rightTree  = (null == casFeat_rightTree) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_rightTree).getCode();

 
    casFeat_treeName = jcas.getRequiredFeatureDE(casType, "treeName", "uima.cas.String", featOkTst);
    casFeatCode_treeName  = (null == casFeat_treeName) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_treeName).getCode();

  }
}



    
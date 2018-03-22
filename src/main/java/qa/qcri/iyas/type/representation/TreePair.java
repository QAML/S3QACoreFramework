

/* First created by JCasGen Mon Mar 19 07:46:39 CET 2018 */
package qa.qcri.iyas.type.representation;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Tue Mar 20 16:27:12 CET 2018
 * XML source: /home/sromeo/workspaces/UIMA/workspace/S3QACoreFramework/src/main/resources/descriptors/qa/qcri/iyas/type/RepresentationTypeSystemDescriptor.xml
 * @generated */
public class TreePair extends Representation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(TreePair.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected TreePair() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public TreePair(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public TreePair(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public TreePair(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: leftTree

  /** getter for leftTree - gets 
   * @generated
   * @return value of the feature 
   */
  public Tree getLeftTree() {
    if (TreePair_Type.featOkTst && ((TreePair_Type)jcasType).casFeat_leftTree == null)
      jcasType.jcas.throwFeatMissing("leftTree", "qa.qcri.iyas.type.representation.TreePair");
    return (Tree)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((TreePair_Type)jcasType).casFeatCode_leftTree)));}
    
  /** setter for leftTree - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setLeftTree(Tree v) {
    if (TreePair_Type.featOkTst && ((TreePair_Type)jcasType).casFeat_leftTree == null)
      jcasType.jcas.throwFeatMissing("leftTree", "qa.qcri.iyas.type.representation.TreePair");
    jcasType.ll_cas.ll_setRefValue(addr, ((TreePair_Type)jcasType).casFeatCode_leftTree, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: rightTree

  /** getter for rightTree - gets 
   * @generated
   * @return value of the feature 
   */
  public Tree getRightTree() {
    if (TreePair_Type.featOkTst && ((TreePair_Type)jcasType).casFeat_rightTree == null)
      jcasType.jcas.throwFeatMissing("rightTree", "qa.qcri.iyas.type.representation.TreePair");
    return (Tree)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((TreePair_Type)jcasType).casFeatCode_rightTree)));}
    
  /** setter for rightTree - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setRightTree(Tree v) {
    if (TreePair_Type.featOkTst && ((TreePair_Type)jcasType).casFeat_rightTree == null)
      jcasType.jcas.throwFeatMissing("rightTree", "qa.qcri.iyas.type.representation.TreePair");
    jcasType.ll_cas.ll_setRefValue(addr, ((TreePair_Type)jcasType).casFeatCode_rightTree, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: treeName

  /** getter for treeName - gets 
   * @generated
   * @return value of the feature 
   */
  public String getTreeName() {
    if (TreePair_Type.featOkTst && ((TreePair_Type)jcasType).casFeat_treeName == null)
      jcasType.jcas.throwFeatMissing("treeName", "qa.qcri.iyas.type.representation.TreePair");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TreePair_Type)jcasType).casFeatCode_treeName);}
    
  /** setter for treeName - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setTreeName(String v) {
    if (TreePair_Type.featOkTst && ((TreePair_Type)jcasType).casFeat_treeName == null)
      jcasType.jcas.throwFeatMissing("treeName", "qa.qcri.iyas.type.representation.TreePair");
    jcasType.ll_cas.ll_setStringValue(addr, ((TreePair_Type)jcasType).casFeatCode_treeName, v);}    
  }

    
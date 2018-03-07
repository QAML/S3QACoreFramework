

/* First created by JCasGen Wed Mar 07 16:46:36 CET 2018 */
package qa.qcri.iyas.type.representation;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Wed Mar 07 19:38:12 CET 2018
 * XML source: /home/sromeo/workspaces/UIMA/workspace/S3QACoreFramework/src/main/resources/descriptors/qa/qcri/iyas/type/RepresentationTypeSystemDescriptor.xml
 * @generated */
public class Tree extends Represenation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Tree.class);
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
  protected Tree() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Tree(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Tree(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Tree(JCas jcas, int begin, int end) {
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
  //* Feature: stringRepresentaion

  /** getter for stringRepresentaion - gets 
   * @generated
   * @return value of the feature 
   */
  public String getStringRepresentaion() {
    if (Tree_Type.featOkTst && ((Tree_Type)jcasType).casFeat_stringRepresentaion == null)
      jcasType.jcas.throwFeatMissing("stringRepresentaion", "qa.qcri.iyas.type.representation.Tree");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Tree_Type)jcasType).casFeatCode_stringRepresentaion);}
    
  /** setter for stringRepresentaion - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setStringRepresentaion(String v) {
    if (Tree_Type.featOkTst && ((Tree_Type)jcasType).casFeat_stringRepresentaion == null)
      jcasType.jcas.throwFeatMissing("stringRepresentaion", "qa.qcri.iyas.type.representation.Tree");
    jcasType.ll_cas.ll_setStringValue(addr, ((Tree_Type)jcasType).casFeatCode_stringRepresentaion, v);}    
  }

    
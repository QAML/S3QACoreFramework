

/* First created by JCasGen Tue Mar 20 16:25:50 CET 2018 */
package qa.qcri.iyas.type.representation;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Tue Mar 20 16:27:12 CET 2018
 * XML source: /home/sromeo/workspaces/UIMA/workspace/S3QACoreFramework/src/main/resources/descriptors/qa/qcri/iyas/type/RepresentationTypeSystemDescriptor.xml
 * @generated */
public class Label extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Label.class);
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
  protected Label() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Label(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Label(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Label(JCas jcas, int begin, int end) {
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
  //* Feature: labels

  /** getter for labels - gets 
   * @generated
   * @return value of the feature 
   */
  public StringArray getLabels() {
    if (Label_Type.featOkTst && ((Label_Type)jcasType).casFeat_labels == null)
      jcasType.jcas.throwFeatMissing("labels", "qa.qcri.iyas.type.representation.Label");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Label_Type)jcasType).casFeatCode_labels)));}
    
  /** setter for labels - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setLabels(StringArray v) {
    if (Label_Type.featOkTst && ((Label_Type)jcasType).casFeat_labels == null)
      jcasType.jcas.throwFeatMissing("labels", "qa.qcri.iyas.type.representation.Label");
    jcasType.ll_cas.ll_setRefValue(addr, ((Label_Type)jcasType).casFeatCode_labels, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for labels - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getLabels(int i) {
    if (Label_Type.featOkTst && ((Label_Type)jcasType).casFeat_labels == null)
      jcasType.jcas.throwFeatMissing("labels", "qa.qcri.iyas.type.representation.Label");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Label_Type)jcasType).casFeatCode_labels), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Label_Type)jcasType).casFeatCode_labels), i);}

  /** indexed setter for labels - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setLabels(int i, String v) { 
    if (Label_Type.featOkTst && ((Label_Type)jcasType).casFeat_labels == null)
      jcasType.jcas.throwFeatMissing("labels", "qa.qcri.iyas.type.representation.Label");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Label_Type)jcasType).casFeatCode_labels), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Label_Type)jcasType).casFeatCode_labels), i, v);}
  }

    
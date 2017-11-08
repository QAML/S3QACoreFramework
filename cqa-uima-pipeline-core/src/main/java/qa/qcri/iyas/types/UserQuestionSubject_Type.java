
/* First created by JCasGen Mon Oct 30 14:47:28 AST 2017 */
package qa.qcri.iyas.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;

/** 
 * Updated by JCasGen Tue Oct 31 16:46:26 AST 2017
 * @generated */
public class UserQuestionSubject_Type extends QAAnnotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = UserQuestionSubject.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("qa.qcri.iyas.types.UserQuestionSubject");



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public UserQuestionSubject_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    
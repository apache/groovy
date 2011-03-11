package org.codehaus.groovy.classgen.asm

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.Parameter

/**
 * @author Jochen Theodorou
 */
class DirectMethodCallTest extends AbstractBytecodeTestCase {
    
  void testVirtual() {
      def target = ClassHelper.Integer_TYPE.getMethod("toString", new Parameter[0])
      def makeDirectCall = {su ->
          su. getAST().classes[0].
              getMethod("run", new Parameter[0]).code.
              statements.last().expression.methodTarget = target;
      }
      
      assert compile (method:"run", conversionAction: makeDirectCall, """
          def a = 1; 
          a.toString()
      """).hasSequence([
              "INVOKEVIRTUAL java/lang/Integer.toString ()Ljava/lang/String;"
      ])
  }
}

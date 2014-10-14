package gls.statements

import gls.CompilableTestSupport

public class ReturnTest extends CompilableTestSupport {

  public void testObjectInitializer() {
      shouldNotCompile """
         class A {
            {return}
         }      
      """
  }
  
  public void testStaticInitializer() {
      assertScript """
         class A {
             static foo=2
             static { return; foo=1 }
         }
         assert A.foo==2
      """      
  }

  public void testReturnAdditionInFinally() {
      //GROOVY-7065
      assertScript """
        class CountDown { int counter = 10 }

        CountDown finalCountDown() {
            def countDown = new CountDown()
            try {
                countDown.counter = --countDown.counter
            } catch (ignored) {
                countDown.counter = Integer.MIN_VALUE
            } finally {
                return countDown
            }
        }

        assert finalCountDown().counter == 9
      """
  }
}
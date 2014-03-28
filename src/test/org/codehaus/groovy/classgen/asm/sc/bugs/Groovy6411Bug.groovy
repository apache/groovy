package org.codehaus.groovy.classgen.asm.sc.bugs

import groovy.transform.stc.StaticTypeCheckingTestCase
import org.codehaus.groovy.classgen.asm.sc.StaticCompilationTestSupport

class Groovy6411Bug extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {

    void testShouldNotThrowInvokerInvocationException() {
            assertScript '''
    class Client<T> {
      static boolean caughtIOEx = false
      static boolean caughtInvokerEx = false

      static String method() {
        return ({
          try {
            return doSomething()
          } catch (IOException ioe) {
            caughtIOEx = true
          } catch (Exception e) {
            caughtInvokerEx = true
          }
        })()
      }

      private static <T> T doSomething() throws IOException {
        throw new IOException()
      }
    }

    Client.method()
    assert Client.caughtIOEx
    assert !Client.caughtInvokerEx

    '''
    }
}

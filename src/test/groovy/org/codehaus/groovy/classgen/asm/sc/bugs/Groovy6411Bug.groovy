/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
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

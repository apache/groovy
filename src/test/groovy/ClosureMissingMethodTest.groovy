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
package groovy

class ClosureMissingMethodTest extends GroovyTestCase {

    void testInScript() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate("""
          int count = 0

          foo = {
            count++
            bar()
          }
          baz = {
            foo()
          }

          try {
              baz()
              fail()
          } catch (org.codehaus.groovy.runtime.InvokerInvocationException iie) {
              assert iie.cause.method == 'bar'
              assert count == 1
          } catch (MissingMethodException mme) {
              assert mme.method == 'bar'
              assert count == 1
          }
      """);
    }

    void testInMethod() {
        int count = 0

        def foo = {
            count++
            bar()
        }
        def baz = {
            foo()
        }

        try {
            baz()
            fail()
        } catch (MissingMethodException mme) {
            assert mme.method == 'bar'
            assert count == 1
        }
    }

    void testWithMetaClassInScript() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate("""
          int count = 0

          foo = {
            count++
            bar()
          }
          baz = {
            foo()
          }
          mc = new ExpandoMetaClass(baz.getClass())
          mc.initialize()
          baz.metaClass = mc

          try {
              baz()
              fail()
          } catch (org.codehaus.groovy.runtime.InvokerInvocationException iie) {
              assert iie.cause.method == 'bar'
              assert count == 1
          } catch (MissingMethodException mme) {
              assert mme.method == 'bar'
              assert count == 1
          }
      """);
    }

    void testWithMetaClassInMethod() {
        int count = 0

        def foo = {
            count++
            bar()
        }
        def baz = {
            foo()
        }
        MetaClass mc = new ExpandoMetaClass(baz.getClass())
        mc.initialize()
        baz.metaClass = mc

        try {
            baz()
            fail()
        } catch (MissingMethodException mme) {
            assert mme.method == 'bar'
            assert count == 1
        }
    }
}

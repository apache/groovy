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

import groovy.test.GroovyTestCase
import org.codehaus.groovy.runtime.ReflectionMethodInvoker

final class GroovyInterceptableTest extends GroovyTestCase {

    void testMethodIntercept1() {
        def g = new GI()
        assert g.someInt() == 2806
        assert g.someUnexistingMethod() == 1
        assert g.toString() == "invokeMethodToString"
    }

    void testMethodIntercept2() {
        def g = new GI()
        assert g.foo == 89
        g.foo = 90
        assert g.foo == 90
        // Should this be 1 or 90?
        assert g.getFoo() == 1
    }

    // GROOVY-3015
    void testMethodIntercept3() {
        String shared = '''\
            import org.codehaus.groovy.runtime.InvokerHelper
            import org.codehaus.groovy.runtime.StringBufferWriter
            import static groovy.test.GroovyTestCase.assertEquals

            class Traceable implements GroovyInterceptable {
                private static int indent = 1
                Writer writer = new PrintWriter(System.out)
                Object invokeMethod(String name, Object args) {
                    writer.write('\\n' + ('  ' * indent) + 'Enter ' + name)
                    indent += 1
                    def result = InvokerHelper.getMetaClass(this).invokeMethod(this, name, args)
                    indent -= 1
                    writer.write('\\n' + ('  ' * indent) + 'Leave ' + name)
                    return result
                }
            }

            class Whatever extends Traceable {
                int inner() { return 1 }
                int outer() { return inner() }
                int shouldTraceOuterAndInnerMethod() { return outer() }
                def shouldTraceOuterAndInnerClosure = { -> return outer() }
            }

            def log = new StringBuffer()
            def obj = new Whatever(writer: new StringBufferWriter(log))
        '''

        assertScript shared + '''
            obj.shouldTraceOuterAndInnerMethod()

            assertEquals """
            |  Enter shouldTraceOuterAndInnerMethod
            |    Enter outer
            |      Enter inner
            |      Leave inner
            |    Leave outer
            |  Leave shouldTraceOuterAndInnerMethod""".stripMargin(), log.toString()
        '''

        assertScript shared + '''
            obj.shouldTraceOuterAndInnerClosure()

            assertEquals """
            |  Enter shouldTraceOuterAndInnerClosure
            |    Enter outer
            |      Enter inner
            |      Leave inner
            |    Leave outer
            |  Leave shouldTraceOuterAndInnerClosure""".stripMargin(), log.toString()
        '''
    }

    void testMissingMethod1() {
        def obj = new GI2()
        shouldFail { obj.notAMethod() }
        assert 'missing' == obj.result
    }

    void testMissingMethod2() {
        def obj = new GI2()
        shouldFail { obj.method() }
        assert 'missing' == obj.result
    }
}

//------------------------------------------------------------------------------

class GI implements GroovyInterceptable {
    def foo = 89
    int someInt() { 2806 }
    @Override
    String toString() { "originalToString" }
    @Override
    Object invokeMethod(String name, Object args) {
        if ("toString" == name)
            return "invokeMethodToString"
        else if ("someInt" == name)
            return ReflectionMethodInvoker.invoke(this, name, args)
        else
            return 1
    }
}

class GI2 implements GroovyInterceptable {
    def result = ""
    @Override
    def invokeMethod(String name, args) {
        def metaMethod = Foo.metaClass.getMetaMethod(name, args)
        if (metaMethod != null) return metaMethod.invoke(this, args)
        result += "missing"
        throw new MissingMethodException(name, Foo.class, args)
    }
    def method() {
        notAMethod()
    }
}

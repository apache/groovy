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
package groovy.util

import groovy.xml.MarkupBuilder
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.Test

final class DelegatingScriptTest {

    @Test
    void testDelegatingScript() {
        def sh = new GroovyShell(new Binding(), new CompilerConfiguration().tap{
            scriptBaseClass = DelegatingScript.name
        })
        def script = (DelegatingScript) sh.parse('''
            foo(3, 2) { a, b -> a * b }
            bar = 'test'
            assert 'testsetget' == bar
        ''')
        def dsl = new MyDSL()
        script.setDelegate(dsl)
        script.run()
        assert dsl.foo == 6
        assert dsl.innerBar() == 'testset'
    }

    @Test
    void testUseMarkupBuilderAsDelegate() {
        def sh = new GroovyShell(new Binding(), new CompilerConfiguration().tap{
            scriptBaseClass = DelegatingScript.name
        })
        def script = sh.parse('''
            foo{ bar() }
        ''')
        StringWriter sw = new StringWriter()
        def markupBuilder = new MarkupBuilder(sw)
        script.setDelegate(markupBuilder)
        script.run()

        assert sw.toString() == '''\
<foo>
  <bar />
</foo>'''
    }

    static class MyDSL {
        protected int foo
        protected String bar

        void foo(int x, int y, Closure z) {
            foo = z.call(x, y)
        }

        void setBar(String a) {
            this.bar = a + 'set'
        }

        String getBar() {
            this.bar + 'get'
        }

        String innerBar() {
            this.bar
        }
    }
}

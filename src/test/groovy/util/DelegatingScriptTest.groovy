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

import groovy.test.GroovyTestCase
import org.codehaus.groovy.control.CompilerConfiguration
import groovy.xml.MarkupBuilder

class DelegatingScriptTest extends GroovyTestCase {
    void testDelegatingScript() throws Exception {
        def cc = new CompilerConfiguration()
        cc.scriptBaseClass = DelegatingScript.name
        def sh = new GroovyShell(new Binding(), cc)
        def script = (DelegatingScript) sh.parse('''
            // println DelegatingScript.class
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

    void testUseMarkupBuilderAsDelegate() throws Exception {
        def cc = new CompilerConfiguration()
        cc.scriptBaseClass = DelegatingScript.class.name
        def sh = new GroovyShell(new Binding(), cc)
        def script = sh.parse('''
            foo{ bar() }
        ''')
        StringWriter sw = new StringWriter()
        def markupBuilder = new MarkupBuilder(sw)
        script.setDelegate(markupBuilder)
        script.run()

        assert sw.toString() == '''<foo>
  <bar />
</foo>'''
    }
}

class MyDSL {
    protected int foo
    protected String bar

    void foo(int x, int y, Closure z) { foo = z(x, y) }

    void setBar(String a) {
        this.bar = a + "set"
    }

    String getBar() {
        this.bar + "get"
    }

    String innerBar() {
        this.bar
    }
}

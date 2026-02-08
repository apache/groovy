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
package groovy.text

import groovy.test.GroovyTestCase

class SimpleTemplateTest extends GroovyTestCase {

    void testSimpleCallFromGroovyEmpty() {
        assertEquals('', simpleCall(''))
    }

    void testSimpleCallFromGroovyStatic() {
        def input = 'some static text'
        assertEquals(input, simpleCall(input))
    }

    void testExpressionAssign() {
        assertEquals('1',   simpleCall('<%=1%>'))
        assertEquals(' 1',  simpleCall(' <%=1%>'))
        assertEquals(' 1 ', simpleCall(' <%=1%> '))
        assertEquals(' 1 ', simpleCall(' <%= 1%> '))
        assertEquals(' 1 ', simpleCall(' <%= 1 %> '))
        assertEquals(' 1 ', simpleCall(" <%=\n 1 \n%> "))
        assertEquals(' 1', bindingCall([a:1],' <%=a%>'))
    }

    void testExpressionEval() {
        assertEquals('1', simpleCall('<%print(1)%>'))
        assertEquals('01', simpleCall('<%for(i in 0..1){print(i)}%>'))
    }

    void testWithMarkupBuilder(){
    def text = '''<%
        builder = new groovy.xml.MarkupBuilder(out)
        [1,2,3].each{ count ->
            out.print(1)
        }
    %>'''
    assertEquals('111', simpleCall(text))
    }

    void testWithMarkupBuilderWithSemicolons(){
    def text = '''<%
        builder = new groovy.xml.MarkupBuilder(out);
        [1,2,3].each{ count ->
            out.print(1);
        }
    %>'''
    assertEquals('111', simpleCall(text))
    }

    void testWithQuotesInScriplet(){
    def text = '''<%
        ['1',"2",/3/].each{ x ->
            out.print(x)
        }
    %>'''
    assertEquals('123', simpleCall(text))
    }

    String simpleCall(input){
        bindingCall([:], input)
    }

    String bindingCall(binding, input){
        def template = new SimpleTemplateEngine(true).createTemplate(input)
        return template.make(binding).toString()
    }

}
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
package org.codehaus.groovy.tools.stubgenerator

final class Groovy7113 extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'Foo.groovy': '''
                trait Foo {
                    int foo() { 1 }
                    void bar(int x) { }
                    void baz(int y) { }
                }
            ''',

            'Bar.java': '''
                class Bar extends Baz { }
            ''',

            'Baz.groovy': '''
                class Baz implements Foo { void bar(int x) { } }
            ''',

            'AbstractFoo.groovy':'''
                abstract class AbstractFoo implements Foo { }
            ''',

            'ConcreteBar.java': '''
                public class ConcreteBar extends AbstractFoo { }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        String stub = stubJavaSourceFor('Foo')
        assert stub.contains('interface Foo')
        assert stub.contains('int foo()')
        assert stub.contains('void baz(int y)')
        assert stub.contains('void bar(int x)')

               stub = stubJavaSourceFor('Baz')
        assert stub.contains('class Baz')
        assert stub.contains('int foo()')
        assert stub.contains('void baz(int y)')
        assert stub.contains('void bar(int x)')
    }
}

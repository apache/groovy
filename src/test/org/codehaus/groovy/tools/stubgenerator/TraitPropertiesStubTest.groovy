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

/**
 * GROOVY-8224: Checks that trait properties appear within stubs
 */
final class TraitPropertiesStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'GroovyXTrait.groovy': '''
                trait GroovyXTrait {
                    int bar
                    boolean foo
                    String baz() { 'baz' }
                }
            ''',

            'GroovyXImpl.groovy': '''
                class GroovyXImpl implements GroovyXTrait { }
            ''',

            'Main.java': '''
                public class Main {
                    public static void main(String[] args) {
                        new GroovyXImpl();
                    }
                }
            ''',
        ]
    }

    @Override
    void verifyStubs() {
        verifyMethodAndPropsInStubs(stubJavaSourceFor('GroovyXTrait'))
        verifyMethodAndPropsInStubs(stubJavaSourceFor('GroovyXImpl'))
    }

    private static void verifyMethodAndPropsInStubs(String stubSource) {
        assert stubSource.contains('String baz()')
        assert stubSource.contains('int getBar()')
        assert stubSource.contains('void setBar(int value)')
        assert stubSource.contains('boolean getFoo()')
        assert stubSource.contains('boolean isFoo()')
        assert stubSource.contains('void setFoo(boolean value)')
    }
}

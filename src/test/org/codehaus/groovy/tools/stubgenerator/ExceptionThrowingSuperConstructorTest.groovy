/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.tools.stubgenerator

/**
 * Test stub generation for super constructors with exceptions
 *
 * @author Jochen Theodorou
 */
class ExceptionThrowingSuperConstructorTest extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [   'Dummy.java': '''
                import java.io.*;
                 class Dummy{
                    public Dummy(String s) {}
                    public Dummy(Integer s) {}
                    public Dummy(InputStream i) throws IOException {}
                 }
                 ''',
            'Derived6282.groovy': '''
                class Derived6282 extends jline.SimpleCompletor {
                  Derived6282(foo) {
                    super(foo)
                  }
                }
            '''
        ]
    }

    void verifyStubs() {
        String source = stubJavaSourceFor('Derived6282')
        // it should never select the exception throwing constructor
        assert source.contains("super ((java.lang.String")
    }
}
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
 * Test stub generation for super constructors with exceptions
 */
class ExceptionThrowingSuperConstructorTest extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [   'Dummy.java': '''
                import java.io.*;
                 class Dummy{
                    public Dummy(String s) {}
                    public Dummy(InputStream i) throws IOException {}
                 }
                 ''',
            'Derived6282.groovy': '''
                class Derived6282A extends org.codehaus.groovy.tools.stubgenerator.ExceptionThrowingTestHelper {
                  Derived6282A(foo) {
                    super((String) foo)
                  }
                }
                class Derived6282B extends Dummy {
                  Derived6282B(foo) {
                    super((String) foo)
                  }
                }
            '''
        ]
    }

    void verifyStubs() {
        String source = stubJavaSourceFor('Derived6282A')
        // it should never select the exception throwing constructor
        assert source.contains("super ((java.lang.String")
        source = stubJavaSourceFor('Derived6282B')
        // it should never select the exception throwing constructor
        assert source.contains("super((String")
    }
}

class ExceptionThrowingTestHelper {
    ExceptionThrowingTestHelper(InputStream io) throws IOException {}
    ExceptionThrowingTestHelper(String s) {}
}
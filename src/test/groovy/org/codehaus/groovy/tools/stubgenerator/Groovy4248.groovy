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
 * Test that Parameter annotations are kept in the Java stub.
 */
final class Groovy4248 extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'foo/Main.java': '''
                package foo;
                import bar.GroovyClass;
                public class Main {
                    public static void main(String[] args) throws Exception {
                        new GroovyClass().getClass().getMethod("getView").getAnnotations();
                    }
                }
            ''',

            'bar/GroovyClass.groovy': '''
                package bar
                class GroovyClass {
                    @Deprecated
                    String getView(@Deprecated String pathVariable) {
                        null
                    }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        def stubSource = stubJavaSourceFor('bar.GroovyClass')
        assert stubSource.contains('@java.lang.Deprecated() public  java.lang.String getView')
        assert stubSource.contains('@java.lang.Deprecated() java.lang.String pathVariable')
    }
}

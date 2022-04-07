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

import groovy.test.NotYetImplemented

final class Groovy10571 extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'A.groovy': '''
                public @interface A {
                    Class[] value()
                }
            ''',
            'B.java': '''
                public class B {
                }
            ''',
            'C.groovy': '''
                import groovy.transform.*
                import java.lang.annotation.*

                @stc.POJO
                @A([ B, Object ])
                @AnnotationCollector
                @Target(ElementType.TYPE)
                @Retention(RetentionPolicy.SOURCE)
                public @interface C {
                }
            ''',
            'D.groovy': '''
                @C class D {
                }
            ''',
            'Main.java': '''
                public class Main {
                    public static void main(String[] args) {
                        new D();
                    }
                }
            ''',
        ]
    }

    @Override
    void verifyStubs() {
        String stub = stubJavaSourceFor('C')
        assert stub.contains('Object.class')
        assert stub.contains('B.class')//bug
    }

    @Override @NotYetImplemented
    void testRun() {
        super.testRun()
    }
}

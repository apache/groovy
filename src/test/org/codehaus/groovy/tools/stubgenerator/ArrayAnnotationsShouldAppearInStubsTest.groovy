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
 * Given that an import with an alias always has the fully qualified
 * path we should always be able to substitute the fqn and hence don't
 * need an import. Java code has no visibility to the alias.
 */
class ArrayAnnotationsShouldAppearInStubsTest extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [
            'AnnotatedClass.groovy': '''
                @ArrayAnnotation(["foo", "bar"])
                class AnnotatedClass {
                    @ArrayAnnotation([]) method1() {}
                    @ArrayAnnotation(['baz']) method2() {}
                }
            ''',
            'ArrayAnnotation.java': '''
            public @interface ArrayAnnotation {
                String[] value() default {};
            }
            '''
        ]
    }

    void verifyStubs() {
        classes['AnnotatedClass'].with {
            assert annotations[0].getProperty('value').toString() == '{"foo", "bar"}'
            assert methods['method1'].annotations[0].getProperty('value').toString() == '{}'
            assert methods['method2'].annotations[0].getProperty('value').toString() == '{"baz"}'
        }
    }
}

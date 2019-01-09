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
 * Test that FQN appears fine in generated stub when a enum ClassExpression.<EnumConstant> 
 *  is used as an annotation member value
 */
class AnnotationMemberValuesResolutionV1StubsTest extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [
            'foo/Foo4434V1.java': '''
                package foo;
                
                import java.lang.annotation.ElementType;
                import java.lang.annotation.Retention;
                import java.lang.annotation.RetentionPolicy;
                import java.lang.annotation.Target;
                
                import baz.MyEnum4434V1;
                
                @Retention(RetentionPolicy.RUNTIME)
                @Target( { ElementType.TYPE })
                public @interface Foo4434V1 {
                    MyEnum4434V1 val() default MyEnum4434V1.OTHER_VALUE;
                }
            ''',
            'baz/MyEnum4434V1.java': '''
                package baz;

                public enum MyEnum4434V1 {SOME_VALUE, OTHER_VALUE}
            ''',
            'Bar4434V1.groovy': '''
                import foo.Foo4434V1
                import baz.MyEnum4434V1
                
                @Foo4434V1(val = MyEnum4434V1.SOME_VALUE)
                class Bar4434V1 {}
            '''
        ]
    }

    void verifyStubs() {
        classes['Bar4434V1'].with {
            assert annotations[0].getProperty('val').toString() == 'baz.MyEnum4434V1.SOME_VALUE'
        }
    }
}

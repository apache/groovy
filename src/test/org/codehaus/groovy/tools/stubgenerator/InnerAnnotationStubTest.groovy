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
 * Test for GROOVY-6085
 */
class InnerAnnotationStubTest extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [
                'JacksonAnnotationTest.groovy': '''
                @JsonSubTypes.Type
                @AnnoWithEnum(AnnoWithEnum.Include.NON_NULL)
                class JacksonAnnotationTest { }

            ''',

                'JsonSubTypes.java': '''
                import java.lang.annotation.ElementType;
                import java.lang.annotation.Retention;
                import java.lang.annotation.RetentionPolicy;
                import java.lang.annotation.Target;

                @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
                @Retention(RetentionPolicy.RUNTIME)
                public @interface JsonSubTypes {
                    public Type[] value();

                    public @interface Type {
                        public Class<?> value();
                        public String name() default "";
                    }
                }
            ''',

                'AnnoWithEnum.java': '''
                import java.lang.annotation.ElementType;
                import java.lang.annotation.Retention;
                import java.lang.annotation.RetentionPolicy;
                import java.lang.annotation.Target;

                @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
                @Retention(RetentionPolicy.RUNTIME)
                public @interface AnnoWithEnum {
                    public Include value() default Include.ALWAYS;
                    public enum Include {  ALWAYS,  NON_NULL,  NON_EMPTY; }
                }
            '''
        ]
    }

    void verifyStubs() {
        def stubSource = stubJavaSourceFor('JacksonAnnotationTest')
        assert stubSource.contains('@JsonSubTypes.Type')
        assert stubSource.contains('@AnnoWithEnum(value=AnnoWithEnum.Include.NON_NULL)')
    }
}

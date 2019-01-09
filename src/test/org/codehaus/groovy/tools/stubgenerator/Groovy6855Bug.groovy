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
 * Test that default Closure annotation values compile correctly within stubs.
 */
class Groovy6855Bug extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [
                'foo/Main.java' : '''
                    package foo;

                    import bar.Get;
                    import java.lang.annotation.Annotation;
                    import java.lang.reflect.Constructor;

                    @Get public class Main {
                        public static void main(String[] args) throws Exception {
                            Annotation getAnnotation = Main.class.getAnnotations()[0];
                            Class closureClass = (Class) getAnnotation.annotationType().getMethod("value").getDefaultValue();
                            Constructor closureConstructor = closureClass.getConstructor(Object.class, Object.class);
                            System.out.println(((groovy.lang.Closure) closureConstructor.newInstance(null, null)).call());
                        }
                    }
                ''',
                'bar/Get.groovy' : '''
                    package bar

                    import java.lang.annotation.*

                    @Target(ElementType.TYPE)
                    @Retention(RetentionPolicy.RUNTIME)
                    @interface Get {
                        Class value() default {return 42}
                    }
                '''
        ]
    }

    void verifyStubs() {
        def stubSource = stubJavaSourceFor('bar.Get')
        assert stubSource.contains('java.lang.Class value() default groovy.lang.Closure.class')
    }
}

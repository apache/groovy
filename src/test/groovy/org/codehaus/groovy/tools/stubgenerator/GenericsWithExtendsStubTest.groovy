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
 * Test that Generics information appears correctly in stub return types.
 */
class GenericsWithExtendsStubTest extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [
                'Client.java': '''
                    import java.util.*;

                    public class Client {
                        {
                            new GroovyInterface4650() {
                                public List<? extends CharSequence> getThings1() { return null; }
                                public List<?> getThings2() { return null; }
                                public List<? super CharSequence> getThings3() { return null; }
                                public <T> List<T> getThings4(Collection c, groovy.lang.Closure<Collection<? extends T>> cl) { return null; }
                                public <T> void getThings5(List<? extends List<T>> arg) { }
                            };
                        }
                    }
                ''',
                'GroovyInterface4650.groovy': '''
                    public interface GroovyInterface4650 {
                        List<? extends CharSequence> getThings1()
                        List<?> getThings2()
                        List<? super CharSequence> getThings3()
                        public <T> List<T> getThings4(Collection c, groovy.lang.Closure<Collection<? extends T>> cl)
                        public <T> void getThings5(List<? extends List<T>> arg)
                    }
            '''
        ]
    }

    void verifyStubs() {
        def stubSource = stubJavaSourceFor('GroovyInterface4650')
        assert stubSource.contains('java.util.List<? extends java.lang.CharSequence> getThings1()')
        assert stubSource.contains('java.util.List<?> getThings2()')
        assert stubSource.contains('java.util.List<? super java.lang.CharSequence> getThings3()')
        assert stubSource.contains('<T> java.util.List<T> getThings4(java.util.Collection c, groovy.lang.Closure<java.util.Collection<? extends T>> cl)')
        assert stubSource.contains('<T> void getThings5(java.util.List<? extends java.util.List<T>> arg)')
    }
}

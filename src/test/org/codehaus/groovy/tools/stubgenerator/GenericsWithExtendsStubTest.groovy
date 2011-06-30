/*
 * Copyright 2003-2011 the original author or authors.
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
 * Test that Generics information appears correctly in stub return types.
 *
 * @author Paul King
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
                            };
                        }
                    }
                ''',
                'GroovyInterface4650.groovy': '''
                    public interface GroovyInterface4650 {
                        List<? extends CharSequence> getThings1()
                        List<?> getThings2()
                        List<? super CharSequence> getThings3()
                    }
            '''
        ]
    }

    void verifyStubs() {
        def stubSource = stubJavaSourceFor('GroovyInterface4650')
        assert stubSource.contains('java.util.List<? extends java.lang.CharSequence> getThings1()')
        assert stubSource.contains('java.util.List<?> getThings2()')
        assert stubSource.contains('java.util.List<? super java.lang.CharSequence> getThings3()')
    }
}

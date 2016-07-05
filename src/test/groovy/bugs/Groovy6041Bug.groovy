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


package groovy.bugs

import org.codehaus.groovy.tools.stubgenerator.StringSourcesStubTestCase

class Groovy6041Bug extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
                'Groovy6041Interface.java': '''
                    import java.util.List;

                    public interface Groovy6041Interface {
                        public <T> Promise<T> doExec(Promise<T>... callables);
                    }
                ''',
                'Promise.groovy': 'class Promise<T> {}',
                'Tool.groovy': '''
                    class Tool implements Groovy6041Interface {
                        def <T> Promise<T> doExec(Promise<T>... callables) {
                            return null
                        }
                    }
                '''
        ]
    }

    @Override
    void verifyStubs() {
        def stubSource = stubJavaSourceFor('Tool')
    }
}

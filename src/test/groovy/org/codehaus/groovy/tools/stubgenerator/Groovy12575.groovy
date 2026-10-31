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

final class Groovy12575 extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'p/T.groovy': '''
                package p
                trait T {
                    List<String> log = ['field'];
                    { log << 'block1' }
                    { log << 'block2' }
                }
            ''',
            'p/C.groovy': '''
                package p
                class C implements T {
                }
                @groovy.transform.CompileStatic
                class D implements T {
                }
            ''',
            'p/J.java': '''
                package p;
                import java.util.List;
                public class J {
                    public List<String> c() { return new C().getLog(); }
                    public List<String> d() { return new D().getLog(); }
                }
            ''',
        ]
    }

    @Override
    void verifyStubs() {
        assert stubJavaSourceFor('p.T') != null

        Object pojo = loader.loadClass('p.J').getDeclaredConstructor().newInstance()
        assert pojo.c() == ['field', 'block1', 'block2']
        assert pojo.d() == ['field', 'block1', 'block2']
    }
}

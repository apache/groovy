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

class GROOVY9801 extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [
                'Interface9801.groovy': '''
                    interface Interface9801 {
                        default String method() { return 'test' }
                    }
                ''',
                'Class9801.java': '''
                    public class Class9801 implements Interface9801 {}
                '''
        ]
    }

    void verifyStubs() {
        def stubSource = stubJavaSourceFor('Interface9801')
        assert stubSource.contains('default java.lang.String method')
        def classLoader = new URLClassLoader([targetDir.toURI().toURL()] as URL[], loader)
        classLoader.loadClass('Interface9801')
        def test = classLoader.loadClass('Class9801').getDeclaredConstructor().newInstance()
        assert test.method() == 'test'
    }
}

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

class InterfaceWithDefaultMethodHasDefaultModifierStubTest extends StringSourcesStubTestCase {
    @Override
    Map<String, String> provideSources() {
        [
                'Dummy.java': '''
                    public class Dummy{}
                ''',

                'DefaultInterface.groovy': '''
                    interface DefaultInterface {
                        default String m0() {
                            return "m0"
                        }
                        String m3();
                    }
                '''
        ]
    }

    @Override
    void verifyStubs() {
        def stubContent = stubJavaSourceFor('DefaultInterface');
        assert stubContent.contains('default  java.lang.String m0() { return null; }')
        assert stubContent.contains('java.lang.String m3();')
    }
}

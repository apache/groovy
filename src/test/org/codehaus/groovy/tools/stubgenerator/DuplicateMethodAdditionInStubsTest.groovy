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
 * GROOVY-4453: property methods and methods added indirectly due parameters-with-default-values
 * were resulting in duplicate methods in generated stubs.
 */
class DuplicateMethodAdditionInStubsTest extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [
            'de/me/App4453.java': '''
                package de.me;
                
                import de.app.User4453;
                
                public class App4453 {
                    public void make() {
                        User4453 u = new User4453();
                    }
                }
            ''',
            'de/app/User4453.groovy': '''
                package de.app
                class User4453 {
                    String name
                
                    public void setName(String name, String t = "") {
                          this.name = name
                    }
                }
            '''
        ]
    }

    void verifyStubs() {
        assert classes.size() == 2
        assert classes['de.app.User4453'].methods['setName'].size() == 2
    }
}

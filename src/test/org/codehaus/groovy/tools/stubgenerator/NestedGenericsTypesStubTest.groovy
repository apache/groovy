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
 * Checks that nested generic types correctly appear within stubs.
 */
class NestedGenericsTypesStubTest extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [
            'MyClass.groovy': '''
                class MyClass {
                   List<List<String>> getManyStringLists() { null }
                }
            ''',

            'Main.java': '''
                import java.util.List;
                public class Main {
                   public void main() {
                      List<List<String>> result = new MyClass().getManyStringLists();
                   }
                }
            '''
        ]
    }

    void verifyStubs() {
        def stubSource = stubJavaSourceFor('MyClass')
        assert stubSource.contains('java.util.List<java.util.List<java.lang.String>> getManyStringLists()')
    }
}

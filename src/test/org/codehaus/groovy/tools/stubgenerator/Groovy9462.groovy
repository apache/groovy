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

import groovy.transform.CompileStatic

@CompileStatic
final class Groovy9462 extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        return [
            'Main.java': '''
                public class Main {
                    public static void main(String[] args) {
                        new Neo4jRelationship<Byte, Character>(null, null, "Type");
                    }
                }
            ''',
            'Neo4jRelationship.groovy': '''
                @groovy.transform.CompileStatic
                class Neo4jRelationship<F, T> implements Relationship<F, T> {
                    String type

                    Neo4jRelationship(F from, T to, String type) {
                        this.from = from
                        this.to = to
                        this.type = type
                    }
                }
            ''',
            'Relationship.groovy': '''
                @groovy.transform.CompileStatic
                trait Relationship<F, T> {
                    Long id
                    F from
                    T to
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        def javaStub = stubJavaSourceFor('Neo4jRelationship')
        assert javaStub.contains("public Neo4jRelationship${System.lineSeparator()}(F from, T to,")
    }
}

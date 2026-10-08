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
 * GROOVY-12427: the stub for a class implementing a precompiled trait must not
 * declare the trait's static methods {@code static abstract}.
 */
final class Groovy12427 extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'Person.groovy': """
                import ${this.class.name}Support

                class Person implements ${this.class.simpleName}Support { }
            """,
            'UsesPerson.java': '''
                public class UsesPerson {
                    Person person = new Person();
                    public String described = Person.describe();
                    public String greeted = person.greet();
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        String stub = stubJavaSourceFor('Person')
        assert !stub.contains('abstract')
        assert stub =~ /public static\s+java\.lang\.String describe\(\)/
        assert stub =~ /public\s+java\.lang\.String greet\(\)/

        def usesPerson = loader.loadClass('UsesPerson').getDeclaredConstructor().newInstance()
        assert usesPerson.described == 'named'
        assert usesPerson.greeted == 'hi'
    }
}

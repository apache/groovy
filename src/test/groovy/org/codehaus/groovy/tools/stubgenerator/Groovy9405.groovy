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

final class Groovy9405 extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'test/Requires.java': '''
                package test;
                import java.lang.annotation.*;
                @Retention(RetentionPolicy.RUNTIME)
                @Target({ElementType.PACKAGE})
                public @interface Requires {
                    Class<? extends groovy.lang.Closure> condition();
                }
            ''',
            'test/package-info.groovy': '''
                @test.Requires(condition = { -> true })
                package test
            '''
        ]
    }

    @Override
    void verifyStubs() {
        def piClass = loader.loadClass('test.package-info')
        def inners = piClass.classes
        assert inners.length > 0
        assert inners*.name.contains('test.package-info$_closure1')
    }
}

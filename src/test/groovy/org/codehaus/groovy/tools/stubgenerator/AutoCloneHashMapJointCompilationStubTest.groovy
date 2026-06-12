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
 * GROOVY-11980: when {@code @AutoClone} is applied to a class that
 * extends {@link java.util.LinkedHashMap} (or anything in the
 * {@code HashMap} family), the generated {@code clone()} override
 * must not declare {@code throws CloneNotSupportedException} — the
 * parent's {@code clone()} doesn't declare it, and Java forbids an
 * override that adds a new checked exception.
 *
 * <p>The Java consumer here calls {@code settings.clone()} without a
 * try/catch, which only compiles when the stub omits the throws clause.
 */
final class AutoCloneHashMapJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'foo/Settings.groovy': '''
                package foo

                @groovy.transform.AutoClone
                class Settings extends LinkedHashMap<String, String> {
                }
            ''',
            'foo/JavaUser.java': '''
                package foo;

                public class JavaUser {
                    // No try/catch and no throws — only compiles if the stub
                    // omits "throws CloneNotSupportedException", because
                    // HashMap.clone() does not declare it.
                    public static Settings duplicate(Settings s) {
                        return s.clone();
                    }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        // Stub view: covariant Settings clone() WITHOUT a throws clause.
        // Cloneable comes in transitively via HashMap so the stub's implements
        // clause does not (and need not) repeat it.
        String stub = stubJavaSourceFor('foo.Settings')
        assert stub =~ /public\s+foo\.Settings\s+clone\(\s*\)\s*\{/
        assert !(stub =~ /clone\s*\(\s*\)\s*throws/)

        // Runtime view: Cloneable is inherited via HashMap; clone returns a
        // deep-equivalent Settings.
        Class settingsClass = loader.loadClass('foo.Settings')
        assert Cloneable.isAssignableFrom(settingsClass)
        assert LinkedHashMap.isAssignableFrom(settingsClass)

        def original = settingsClass.newInstance()
        original.put('one', 'first')
        original.put('two', 'second')
        def copy = original.clone()
        assert !copy.is(original)
        assert copy == original
        assert copy.getClass() == settingsClass
    }
}

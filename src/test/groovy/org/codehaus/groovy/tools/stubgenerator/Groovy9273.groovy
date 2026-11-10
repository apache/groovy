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

import java.lang.module.ModuleDescriptor

/**
 * GROOVY-9273: joint compilation of {@code module-info.groovy} with a
 * {@code .java} file must not write {@code interface module-info}, which is
 * not a Java identifier and not a modular compilation unit (JLS §7.7).
 * Sibling {@code .java} files are compiled from the classpath. Groovy still
 * writes {@code module-info.class}.
 */
final class Groovy9273 extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'module-info.groovy': '''
                module demo.joint {
                    exports demo.api;
                }
            ''',
            'demo/api/Greeter.groovy': '''
                package demo.api
                class Greeter {
                    static String hi() { 'hi' }
                }
            ''',
            'demo/api/JavaGreeter.java': '''
                package demo.api;
                public class JavaGreeter {
                    public static String hello() {
                        return Greeter.hi();
                    }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        assert !new File(stubDir, 'module-info.java').exists()
        stubDir.eachFileRecurse(groovy.io.FileType.FILES) { File stub ->
            if (stub.name.endsWith('.java')) {
                assert !stub.text.contains('interface module-info')
            }
        }

        def greeter = stubJavaSourceFor('demo.api.Greeter')
        assert greeter.contains('class Greeter')
        assert !greeter.contains('interface module-info')

        def moduleInfo = new File(targetDir, 'module-info.class')
        assert moduleInfo.isFile()
        def descriptor = ModuleDescriptor.read(new ByteArrayInputStream(moduleInfo.bytes))
        assert descriptor.name() == 'demo.joint'
        assert descriptor.exports()*.source() == ['demo.api']

        assert loader.loadClass('demo.api.JavaGreeter').getMethod('hello').invoke(null) == 'hi'
    }
}

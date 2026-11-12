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
 * The annotations of a script's own no-arg {@code run()} are copied to the
 * script class; those that may go on a method but not on a type must not
 * appear on the class in its Java stub, or javac rejects the stub.
 */
final class ScriptRunAnnotationsStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'foo/Runner.groovy': '''
                package foo

                @Override
                @java.beans.Transient
                @groovy.transform.CompileStatic
                Object run() {
                    'done'
                }
            ''',
            'foo/AliasRunner.groovy': '''
                package foo

                import java.beans.Transient as Tr

                @Tr
                Object run() {
                    'aliased'
                }
            ''',
            'foo/Starter.groovy': '''
                package foo

                @java.beans.Transient
                void main() {
                }
            ''',
            'foo/JavaUser.java': '''
                package foo;

                public class JavaUser {
                    public static Object go() {
                        return new Runner().run();
                    }
                    public static Object goAliased() {
                        return new AliasRunner().run();
                    }
                    public static Starter start() {
                        return new Starter();
                    }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        String stub = stubJavaSourceFor('foo.Runner')
        String classHeader = stub.substring(0, stub.indexOf('class Runner'))
        assert !classHeader.contains('Override')
        assert !classHeader.contains('Transient')
        assert classHeader.contains('CompileStatic')

        stub = stubJavaSourceFor('foo.AliasRunner')
        assert !stub.substring(0, stub.indexOf('class AliasRunner')).contains('Transient')

        stub = stubJavaSourceFor('foo.Starter')
        assert !stub.substring(0, stub.indexOf('class Starter')).contains('Transient')

        def javaUser = loader.loadClass('foo.JavaUser')
        assert javaUser.go() == 'done'
        assert javaUser.goAliased() == 'aliased'
        assert javaUser.start() != null
    }
}

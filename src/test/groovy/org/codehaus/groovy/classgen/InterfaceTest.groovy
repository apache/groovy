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
package org.codehaus.groovy.classgen

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.Test

final class InterfaceTest {

    @Test
    void testJavaImplementsGroovyInterface() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )
        def parentDir = File.createTempDir()
        try {
            new File(parentDir, 'test').mkdir()

            def a = new File(parentDir, 'test/GClass.groovy')
            a.write '''package test
                class GClass {
                }
            '''
            def b = new File(parentDir, 'test/GInterface.groovy')
            b.write '''package test
                interface GInterface {
                    GClass[] getGC()
                    default String foo() { 'foo' + GInterface.this.bar() }
                    private String bar() { 'bar' }
                    static  String baz() { 'baz' }
                }
            '''
            def c = new File(parentDir, 'test/JClass.java')
            c.write '''package test;
                public class JClass implements GInterface {
                    public GClass[] getGC() {
                        return new GClass[0];
                    }
                    public String toString() {
                        return this.foo();
                    }
                }
            '''
            def d = new File(parentDir, 'Main.groovy')
            d.write '''
                def jc = new test.JClass()
                assert jc.getGC().length == 0
                assert jc.toString() == 'foobar'
                assert test.GInterface.baz() == 'baz'
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b, c, d)
            cu.compile()

            loader.loadClass('Main').main()
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }
}

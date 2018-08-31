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
package org.codehaus.groovy.transform.classloading

import junit.framework.TestCase
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.transform.GlobalLegacyTestTransformClassLoader
import org.objectweb.asm.ClassVisitor

/**
 * Tests whether global transforms in legacy META-INF location are successfully detected, loaded,
 * and run if separate class loaders are used for loading compile dependencies and AST transforms.
 */
class LegacyTransformsTest extends GroovyTestCase {
    URL[] urls = collectUrls(getClass().classLoader) + addGroovyUrls()
    GroovyClassLoader dependencyLoader = new GroovyClassLoader(new URLClassLoader(urls, (ClassLoader) null))
    GroovyClassLoader transformLoader = new GroovyClassLoader(new URLClassLoader(urls, new GroovyOnlyClassLoader()))

    private static addGroovyUrls() {
        [
                GroovyObject.class.protectionDomain.codeSource.location.toURI().toURL(),    // load Groovy runtime
                ClassVisitor.class.protectionDomain.codeSource.location.toURI().toURL(),    // load asm
                GroovyTestCase.class.protectionDomain.codeSource.location.toURI().toURL(),  // load Groovy test module
                TestCase.class.protectionDomain.codeSource.location.toURI().toURL(),        // -"-
                this.protectionDomain.codeSource.location.toURI().toURL(),                  // load test as well
        ]
    }

    private Set<URL> collectUrls(ClassLoader classLoader) {
        if (classLoader == null) return []
        if (classLoader instanceof URLClassLoader) {
            return collectUrls(classLoader.parent) + Arrays.asList(classLoader.URLs)
        }
        collectUrls(classLoader.parent)
    }

    private compileAndLoadClass(String source, GroovyClassLoader dependencyLoader, GroovyClassLoader transformLoader) {
        def unit = new CompilationUnit(null, null, dependencyLoader, transformLoader)
        unit.addSource("Foo.groovy", source)
        unit.compile()

        assert unit.classes.size() == 1
        def classInfo = unit.classes[0]

        def loader = new GroovyClassLoader(getClass().classLoader)
        return loader.defineClass(classInfo.name, classInfo.bytes)
    }

    void setUp() {
        assert dependencyLoader.loadClass(CompilationUnit.class.name) != CompilationUnit
        assert dependencyLoader.loadClass(getClass().name) != getClass()

        assert transformLoader.loadClass(CompilationUnit.class.name) == CompilationUnit
        // TODO: reversing arguments of != results in VerifyError
        assert getClass() != transformLoader.loadClass(getClass().name)
    }

    void testGlobalTransform() {
        transformLoader = new GlobalLegacyTestTransformClassLoader(transformLoader, ToUpperCaseGlobalTransform)
        def clazz = compileAndLoadClass("class Foo {}", dependencyLoader, transformLoader)
        assert clazz
        assert clazz.name == "FOO"
    }

    static class GroovyOnlyClassLoader extends ClassLoader {
        synchronized Class<?> loadClass(String name, boolean resolve) {
            // treat this package as not belonging to Groovy
            if (name.startsWith(getClass().getPackage().name)) {
                throw new ClassNotFoundException(name)
            }
            if (name.startsWith("java.") || name.startsWith("groovy.") || name.startsWith("org.codehaus.groovy.")) {
                return getClass().classLoader.loadClass(name, resolve)
            }
            throw new ClassNotFoundException(name)
        }
    }

}

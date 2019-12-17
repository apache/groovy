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

import groovy.test.GroovyTestCase
import junit.framework.TestCase
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.transform.GroovyASTTransformationClass
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.GlobalTestTransformClassLoader
import org.codehaus.groovy.vmplugin.VMPluginFactory
import org.objectweb.asm.ClassVisitor

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.lang.annotation.ElementType

/**
 * Tests whether local and global transforms are successfully detected, loaded,
 * and run if separate class loaders are used for loading compile dependencies
 * and AST transforms.
 */
class TransformsAndCustomClassLoadersTest extends GroovyTestCase {
    URL[] urls = collectUrls(getClass().classLoader) + addGroovyUrls()
    GroovyClassLoader dependencyLoader = new GroovyClassLoader(new URLClassLoader(urls, (ClassLoader) ( VMPluginFactory.getPlugin().getVersion() >= 9 ? ClassLoader.getPlatformClassLoader() : null )))
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

    void setUp() {
        assert dependencyLoader.loadClass(CompilationUnit.class.name) != CompilationUnit
        assert dependencyLoader.loadClass(getClass().name) != getClass()

        assert transformLoader.loadClass(CompilationUnit.class.name) == CompilationUnit
        // TODO: reversing arguments of != results in VerifyError
        assert getClass() != transformLoader.loadClass(getClass().name)
    }

    void testBuiltInLocalTransform() {
        def clazz = compileAndLoadClass("@groovy.transform.TupleConstructor class Foo { String bar }", dependencyLoader, transformLoader)
        checkHasTupleConstructor(clazz)
    }

    void testThirdPartyLocalTransform() {
        def clazz = compileAndLoadClass("@org.codehaus.groovy.transform.classloading.ToUpperCase class Foo {}", dependencyLoader, transformLoader)
        assert clazz.name == "FOO"
    }

    void testLocalTransformWhoseAnnotationUsesClassesAttribute() {
        def clazz = compileAndLoadClass("@org.codehaus.groovy.transform.classloading.ToUpperCase2 class Foo {}", dependencyLoader, transformLoader)
        assert clazz.name == "FOO"
    }

    void testGlobalTransform() {
        transformLoader = new GlobalTestTransformClassLoader(transformLoader, ToUpperCaseGlobalTransform)

        def clazz = compileAndLoadClass("class Foo {}", dependencyLoader, transformLoader)
        assert clazz
        assert clazz.name == "FOO"
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

    private checkHasTupleConstructor(Class clazz) {
        def foo = clazz.newInstance(["some property"] as Object[])
        assert foo.bar == 'some property'
    }

    private Set<URL> collectUrls(ClassLoader classLoader) {
        if (classLoader == null) return []
        if (classLoader instanceof URLClassLoader) {
            return collectUrls(classLoader.parent) + Arrays.asList(classLoader.URLs)
        }
        collectUrls(classLoader.parent)
    }

    static class GroovyOnlyClassLoader extends ClassLoader {
        synchronized Class<?> loadClass(String name, boolean resolve) {
            // treat this package as not belonging to Groovy
            if (name.startsWith(getClass().getPackage().name)) {
                throw new ClassNotFoundException(name)
            }
            if (name.startsWith("java.") || name.startsWith("groovy.")
                    || name.startsWith("org.codehaus.groovy.") || name.startsWith("org.apache.groovy.")) {
                return getClass().classLoader.loadClass(name, resolve)
            }
            throw new ClassNotFoundException(name)
        }
    }
}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@GroovyASTTransformationClass("org.codehaus.groovy.transform.classloading.ToUpperCaseLocalTransform")
@interface ToUpperCase {}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@GroovyASTTransformationClass(classes = [ToUpperCaseLocalTransform])
@interface ToUpperCase2 {}

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class ToUpperCaseLocalTransform implements ASTTransformation {
    void visit(ASTNode[] nodes, SourceUnit source) {
        def clazz = nodes[1]
        assert clazz instanceof ClassNode

        clazz.name = clazz.name.toUpperCase()
    }
}

@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
class ToUpperCaseGlobalTransform implements ASTTransformation {
    void visit(ASTNode[] nodes, SourceUnit source) {
        def module = nodes[0]
        assert module instanceof ModuleNode

        for (clazz in module.classes) {
            clazz.name = clazz.name.toUpperCase()
        }
    }
}

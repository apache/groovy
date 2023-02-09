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

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GlobalTestTransformClassLoader
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformationClass
import org.codehaus.groovy.vmplugin.VMPluginFactory
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Tests whether local and global transforms are successfully detected, loaded,
 * and run if separate class loaders are used for loading compile dependencies
 * and AST transforms.
 */
final class TransformsAndCustomClassLoadersTest {

    private final URL[] urls = collectUrls(this.class.classLoader) + [GroovyObject.location, this.class.location]

    private final GroovyClassLoader dependencyLoader = new GroovyClassLoader(new URLClassLoader(urls, (ClassLoader) (VMPluginFactory.plugin.version >= 9 ? ClassLoader.platformClassLoader : null)))
    private final GroovyClassLoader transformLoader = new GroovyClassLoader(new URLClassLoader(urls, new GroovyOnlyClassLoader()))

    @Before
    void setUp() throws Exception {
        assert dependencyLoader.loadClass(CompilationUnit.class.name) !== CompilationUnit
        assert dependencyLoader.loadClass(this.class.name) !== this.class

        assert transformLoader.loadClass(CompilationUnit.class.name) === CompilationUnit
        assert transformLoader.loadClass(this.class.name) !== this.class
    }

    @After
    void tearDown() throws Exception {
        dependencyLoader.close()
        transformLoader.close()
    }

    @Test
    void testBuiltInLocalTransform() {
        def clazz = compileAndLoadClass('@groovy.transform.TupleConstructor class Foo { String bar }', dependencyLoader, transformLoader)
        checkHasTupleConstructor(clazz)
    }

    @Test
    void testThirdPartyLocalTransform() {
        def clazz = compileAndLoadClass('@org.codehaus.groovy.transform.classloading.ToUpperCase class Foo {}', dependencyLoader, transformLoader)
        assert clazz.name == 'FOO'
    }

    @Test
    void testLocalTransformWhoseAnnotationUsesClassesAttribute() {
        def clazz = compileAndLoadClass('@org.codehaus.groovy.transform.classloading.ToUpperCase2 class Foo {}', dependencyLoader, transformLoader)
        assert clazz.name == 'FOO'
    }

    @Test
    void testGlobalTransform() {
        try (def transformLoader = new GlobalTestTransformClassLoader(transformLoader, ToUpperCaseGlobalTransform)) {
            def clazz = compileAndLoadClass('class Foo {}', dependencyLoader, transformLoader)
            assert clazz
            assert clazz.name == 'FOO'
        }
    }

    @Test
    void testShouldKeepOriginalExceptionDuringGlobalTransformApplyingGroovy9469Bug() {
        try (def transformLoader = new GlobalTestTransformClassLoader(transformLoader, FailingWithMeaningfulMessageTransformation)) {
            compileAndLoadClass('class Foo {}', dependencyLoader, transformLoader)
            Assert.fail('Excepted MultipleCompilationErrorsException not thrown')
        } catch(MultipleCompilationErrorsException e) {
            assert e.message.contains('FailingWithMeaningfulMessageTransformation')
            assert e.message.contains('FileSystemNotFoundException')
            assert e.message.contains('meaningful error message')
        }
    }

    //--------------------------------------------------------------------------

    private Class compileAndLoadClass(String source, GroovyClassLoader dependencyLoader, GroovyClassLoader transformLoader) {
        try (def loader = new GroovyClassLoader(this.class.classLoader)) {
            def unit = new CompilationUnit(null, null, dependencyLoader, transformLoader)
            unit.addSource('Foo.groovy', source)
            unit.compile(CompilePhase.CLASS_GENERATION.phaseNumber)

            assert unit.classes.size() == 1
            def classInfo = unit.classes[0]
            loader.defineClass(classInfo.name, classInfo.bytes)
        }
    }

    private void checkHasTupleConstructor(Class clazz) {
        def foo = clazz.newInstance(['some property'] as Object[])
        assert foo.bar == 'some property'
    }

    private Set<URL> collectUrls(ClassLoader classLoader) {
        if (classLoader == null)
            return Collections.emptySet()
        if (classLoader instanceof URLClassLoader)
            return collectUrls(classLoader.parent) + Arrays.asList(classLoader.URLs)

        collectUrls(classLoader.parent)
    }

    static final class GroovyOnlyClassLoader extends ClassLoader {
        synchronized Class<?> loadClass(String name, boolean resolve) {
            // treat this package as not belonging to Groovy
            if (name.startsWith(this.class.package.name)) {
                throw new ClassNotFoundException(name)
            }
            if (name.startsWith('java.')
                    || name.startsWith('groovy.')
                    || name.startsWith('org.apache.groovy.')
                    || name.startsWith('org.codehaus.groovy.')) {
                def loader = this.class.classLoader
                def result = loader.loadClass(name)
                if (resolve) loader.resolveClass(result)

                return result
            }
            throw new ClassNotFoundException(name)
        }
    }
}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@GroovyASTTransformationClass('org.codehaus.groovy.transform.classloading.ToUpperCaseLocalTransform')
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

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class FailingWithMeaningfulMessageTransformation implements ASTTransformation {
    FailingWithMeaningfulMessageTransformation() {
        throw new java.nio.file.FileSystemNotFoundException('Custom exception with meaningful error message')
    }
    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
    }
}

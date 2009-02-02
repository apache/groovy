/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform.vm5

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.control.CompilePhase

/**
 * Tests whether local and global transforms are successfully detected, loaded,
 * and run if a custom class loader is used for resolving compile dependencies
 * that does not delegate to the compiler's defining class loader (e.g. to
 * avoid pollution of the compile classpath).
 *
 * @author Peter Niederwieser
 */
class TransformsAndCustomClassLoadersTest extends GroovyTestCase {
    void testLocalTransform() {
        def resolvingLoader = new GroovyProjectClassesLoader()
        def transformLoader = new GroovyClassLoader(TransformsAndCustomClassLoadersTest.classLoader)
        checkIsIsolated(resolvingLoader)

        def clazz = compileAndLoadClass("@Immutable final class Foo { String bar }", resolvingLoader, transformLoader)
        checkIsImmutable(clazz)
    }

    void testGlobalTransform() {
        def resolvingLoader = new GroovyProjectClassesLoader()
        def transformLoader = new ToUpperCaseTransformLoader()
        checkIsIsolated(resolvingLoader)

        def clazz = compileAndLoadClass("class Foo {}", resolvingLoader, transformLoader)
        assert clazz
        assert clazz.name == "FOO"
    }

    private compileAndLoadClass(String source, GroovyClassLoader dependencyLoader, GroovyClassLoader transformLoader) {
        def unit = new CompilationUnit(null, null, dependencyLoader, transformLoader)
        unit.addSource(new SourceUnit("", source, new CompilerConfiguration(), null, null))
        unit.compile()

        def classInfo = unit.classes[0]
        assert classInfo
        return transformLoader.defineClass(classInfo.name, classInfo.bytes)
    }

    private checkIsIsolated(ClassLoader loader) {
        def clazz = loader.loadClass(CompilationUnit.name)
        assert clazz
        assert clazz != CompilationUnit
    }

    private checkIsImmutable(Class clazz) {
        try {
            def foo = clazz.newInstance(["setting property"] as Object[])
            foo.bar = "updating property"
            fail()
        } catch (ReadOnlyPropertyException expected) {}
    }
}

/**
 * A class loader that can load classes in the Groovy project,
 * but does so without delegating to another class loader.
 */
class GroovyProjectClassesLoader extends GroovyClassLoader {
    private bootstrapClassLoader = new URLClassLoader([] as URL[], null)

    GroovyProjectClassesLoader() {
        super(null, null, false)
        for (url in getGroovyLoaderURLs())
            addURL(url)
        checkCanLoadGroovyClasses()
        checkCanLoadOrgCodehausGroovyClasses()
    }

    private URL[] getGroovyLoaderURLs() {
        def groovyLoader = Closure.classLoader
        if (groovyLoader instanceof URLClassLoader)
            return groovyLoader.URLs
        else
            assert false, "sorry, GroovyProjectClassesLoader doesn't work in this class loader environment"
    }

    private checkCanLoadGroovyClasses() {
        assert loadClass(GroovyShell.name)
    }

    private checkCanLoadOrgCodehausGroovyClasses() {
        assert loadClass(CompilationUnit.name)
    }

    @Override
    synchronized Class loadClass(String name, boolean resolve) {
        def clazz = doLoadClass(name)
        if (resolve) resolveClass(clazz)
        return clazz
    }

    private Class doLoadClass(String name) {
        def clazz = findLoadedClass(name)
        if (clazz != null) return clazz

        if (name.startsWith("java."))
            return bootstrapClassLoader.loadClass(name)

        return findClass(name)
    }
}

class ToUpperCaseTransformLoader extends GroovyClassLoader {
    ToUpperCaseTransformLoader() {
        super(CompilationUnit.classLoader)
    }

    Enumeration getResources(String name) {
        if (name.equals("META-INF/services/org.codehaus.groovy.transform.ASTTransformation"))
            return Collections.enumeration([getURL()])
        else
          return super.getResources(name)
    }

    def getURL() {
        return new FakeURLFactory().createURL("org.codehaus.groovy.transform.vm5.ToUpperCaseTransform")
    }
}

@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
class ToUpperCaseTransform implements ASTTransformation {
    void visit(ASTNode[] nodes, SourceUnit source) {
        assert nodes[0] instanceof ModuleNode

        for (clazz in nodes[0].classes)
            clazz.name = clazz.name.toUpperCase()
    }
}


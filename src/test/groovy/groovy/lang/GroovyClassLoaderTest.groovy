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
package groovy.lang

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CompileUnit
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.junit.Test

import java.security.CodeSource
import java.util.concurrent.atomic.AtomicInteger

import static org.objectweb.asm.Opcodes.ACC_PUBLIC

final class GroovyClassLoaderTest {

    private final GroovyClassLoader classLoader = new GroovyClassLoader()

    private static boolean contains(String[] paths, String eval) {
        try {
            eval = new File(eval).toURI().toURL().getFile()
            for (it in paths) {
                if (eval == it) return true
            }
        } catch (MalformedURLException ignore) {
        }
        return false
    }

    static List<URL> getPaths(URLClassLoader ucl) {
        ucl.getURLs().findAll { !it.file.endsWith('.jar') }
    }

    static List<URL> getPaths(String path) {
        int start = 0, end = 0
        String sep = File.pathSeparator
        def list = []
        while (end < path.length()) {
            start = end
            end = path.indexOf(sep, end)
            if (end == -1) break
            def sub = path.substring(start, end)
            if (!sub.endsWith('.jar')) {
                list.add(new File(sub).toURI().toURL())
            }
            end += 1
        }
        list
    }

    static void verifyPackageDetails(Class clazz, String expectedPkgName) {
        assert clazz.package instanceof Package
        assert clazz.package.name == expectedPkgName
    }

    //--------------------------------------------------------------------------

    @Test
    void testAddsClasspathEntryOnlyIfItHasNotAlreadyBeenAdded() {
        String newClasspathEntry = '/tmp'
        int initialClasspathEntryCount = classLoader.classPath.length

        classLoader.addClasspath(newClasspathEntry)
        assert classLoader.classPath.length == initialClasspathEntryCount + 1
        assert contains(classLoader.classPath, newClasspathEntry)

        classLoader.addClasspath(newClasspathEntry)
        assert classLoader.classPath.length == initialClasspathEntryCount + 1
        assert contains(classLoader.classPath, newClasspathEntry)
    }

    @Test
    void testParseThenLoadByName() {
        def clazz = classLoader.parseClass('println "howdy"')
        assert classLoader.loadClass(clazz.name) == clazz
    }

    @Test
    void testParseThenLoadByNameWeak() {
        def clazz = classLoader.parseClass('println "howdy"')
        assert classLoader.loadClass(clazz.name) != null
    }

    @Test
    void testClassNotFoundIsNotHidden() {
        def paths = []
        def loader = this.class.classLoader
        while (loader != null) {
            if (loader instanceof URLClassLoader) {
                paths += getPaths(loader)
            }
            loader = loader.parent
        }
        paths += getPaths(System.getProperty('java.class.path'))
        paths = paths.unique()

        def file, tempFolder
        try {
            // On the latest update of the Mac JDK (as of 2009/06/24), the default placement of temp files seems problematic
            // specifying explicitly a custom target directory seems to solve the build issue.
            tempFolder = new File('./build/generated')
            tempFolder.mkdir()
            file = File.createTempFile('Foo', '.groovy', tempFolder)

            def name = file.name - '.groovy'
            def script = "class $name extends groovy.test.GroovyTestCase{}"
            file << script
            paths << file.parentFile.toURI().toURL()
            def cl = new URLClassLoader(paths as URL[], (ClassLoader) null)
            def gcl = new GroovyClassLoader(cl)
            try {
                gcl.loadClass(name)
                assert false
            } catch (NoClassDefFoundError ncdfe) {
                // TODO: hack for running when under coverage; find a better way
                assert ncdfe.message.indexOf('TestCase') > 0
            } catch (MultipleCompilationErrorsException mce) {
                mce.errorCollector.errors.each { err ->
                    assert err instanceof SyntaxErrorMessage
                    assert err.cause.message.indexOf('TestCase') > 0
                }
            }
        } finally {
            try {
                if (file != null) {
                    file.delete()
                }
            } catch (Throwable ignore) {
                ;
            } finally {
                if (tempFolder != null) {
                    tempFolder.delete()
                }
            }
        }
    }

    @Test
    void testClassPathNotDerived() {
        def config = new CompilerConfiguration()
        def loader1 = new GroovyClassLoader(null, config)
        config = new CompilerConfiguration()
        config.setClasspath('foo')
        def loader2 = new GroovyClassLoader(loader1, config)
        config = new CompilerConfiguration()
        def loader3 = new GroovyClassLoader(loader2, config)
        def urls = loader1.URLs
        assert urls.length == 0
        urls = loader2.URLs
        assert urls.length == 1
        assert urls[0].toString().endsWith('foo')
        urls = loader3.URLs
        assert urls.length == 0
    }

    @Test
    void testMultiThreading() {
        def config = new CompilerConfiguration()
        config.recompileGroovySource = true

        def loader = new GroovyClassLoaderTestCustomGCL(config)

        def ts = new Thread[100]
        for (i in 0..<100) {
            ts[i] = Thread.start {
                if (i % 2 == 1) sleep(100)
                assert GroovyClassLoaderTestFoo1 == loader.loadClass('Foox')
            }
        }
        sleep(100)
        for (i in 0..<100) {ts[i].join()}

        assert GroovyClassLoaderTestFoo2 == loader.loadClass('Foox')
    }

    @Test
    void testAdditionalPhaseOperation() {
        def loader = new GroovyClassLoaderTestCustomPhaseOperation()
        def clazz = loader.parseClass('class Foo {}')
        def field = clazz.declaredFields.find { it.name == 'id' && it.type == Long.TYPE }
        assert field != null
    }

    @Test
    void testSourceEncoding() {
        String oldEncoding = System.getProperty('file.encoding')
        System.setProperty('file.encoding', 'US-ASCII')
        try {
            def gcl = new GroovyClassLoader(this.class.classLoader, new CompilerConfiguration().tap{sourceEncoding = 'UTF-8'})
            def clazz = gcl.parseClass('return "\u20AC"') // EURO currency symbol
            def result = clazz.getDeclaredConstructor().newInstance().run()
            int i = result[0]
            // 0xFFFD is used if the original character was not found,
            // it is the famous ? that can often be seen. So if this here
            // fails, then the String conversion failed at one point
            assert i != 0xFFFD
        } finally {
            System.setProperty('file.encoding', oldEncoding)
        }
    }

    // GROOVY-3537
    @Test
    void testPackageDefinitionForGroovyClassesInParseClass() {
        def loader = new GroovyClassLoader(this.class.classLoader)
        def script = '''
            package pkg1
            def x = 1
        '''
        verifyPackageDetails(loader.parseClass(script, 'Pkg1Groovy3537Script.groovy'), 'pkg1')

        script = '''
            package pkg1.pkg2
            class Groovy3537A{}
        '''
        verifyPackageDetails(loader.parseClass(script, 'Pkg1Pkg2Groovy3537A.groovy'), 'pkg1.pkg2')
    }

    @Test
    void testPackageDefinitionForGroovyClassesInDefineClass() {
        def loader = new GroovyClassLoader(this.class.classLoader)
        def classNode = new ClassNode('pkg3.Groovy3537B', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        classNode.addConstructor(new ConstructorNode(ACC_PUBLIC, null))
        def unit = new CompileUnit(loader, new CompilerConfiguration())
        def module = new ModuleNode(unit)
        classNode.setModule(module)
        def clazz = loader.defineClass(classNode, classNode.getName() + '.groovy', '')
        verifyPackageDetails(clazz, 'pkg3')
    }

    // GROOVY-11117
    @Test
    void testParseClassReturn() {
        def c = classLoader.parseClass '''
            interface A {
                interface B { }
                interface C { }
            }
        '''
        assert c.name == 'A'

        c = classLoader.parseClass '''
            interface A {
                trait B { }
                class C { }
            }
        '''
        assert c.name == 'A'

        c = classLoader.parseClass '''
            interface A {
                trait B {
                    static class C { }
                }
            }
        '''
        assert c.name == 'A'

        c = classLoader.parseClass '''
            def m() {
                def aic = new Object() {}
            }
            interface A { }
            trait B { }
            class C { }
        '''
        assert c.name.startsWith('Script_')

        c = classLoader.parseClass '''
            print "hello"
        '''
        assert c.name.startsWith('Script_')
    }
}

//------------------------------------------------------------------------------

class GroovyClassLoaderTestFoo1 {}
class GroovyClassLoaderTestFoo2 {}

class GroovyClassLoaderTestCustomGCL extends GroovyClassLoader {
    GroovyClassLoaderTestCustomGCL(config) {
        super(null, config)
    }
    def counter = new AtomicInteger(0)
    @Override
    protected Class recompile(URL source, String name, Class oldClass) {
        if (name == 'Foox') {
            if (counter.getAndIncrement() < 100) {
                return GroovyClassLoaderTestFoo1
            } else {
                return GroovyClassLoaderTestFoo2
            }
        }
        return super.recompile(source, name, oldClass)
    }
    @Override
    protected boolean isSourceNewer(URL source, Class cls) {
        return true
    }
}

class GroovyClassLoaderTestPropertyAdder implements CompilationUnit.IPrimaryClassNodeOperation {
    @Override
    void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
        classNode.addProperty('id', ACC_PUBLIC, ClassHelper.long_TYPE, null, null, null)
    }
}

class GroovyClassLoaderTestCustomPhaseOperation extends GroovyClassLoader {
    @Override
    CompilationUnit createCompilationUnit(CompilerConfiguration config, CodeSource source) {
        super.createCompilationUnit(config, source).tap {
            addPhaseOperation(new GroovyClassLoaderTestPropertyAdder(), Phases.CONVERSION)
        }
    }
}

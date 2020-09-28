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

import groovy.test.GroovyTestCase
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
import org.objectweb.asm.Opcodes

import java.security.CodeSource
import java.util.concurrent.atomic.AtomicInteger

class GroovyClassLoaderTest extends GroovyTestCase implements Opcodes {

    private final GroovyClassLoader classLoader = new GroovyClassLoader()

    private static boolean contains(String[] paths, String eval) {
        try {
            eval = (new File(eval)).toURI().toURL().getFile()
        } catch (MalformedURLException e) {
            return false
        }
        for (it in paths) {
            if (eval.equals(it)) return true
        }
        return false
    }

    void testAddsAClasspathEntryOnlyIfItHasNotAlreadyBeenAdded() {
        String newClasspathEntry = "/tmp"
        int initialNumberOfClasspathEntries = classLoader.getClassPath().length

        classLoader.addClasspath(newClasspathEntry)
        assert initialNumberOfClasspathEntries + 1 == classLoader.getClassPath().length
        assert contains(classLoader.getClassPath(), newClasspathEntry)

        classLoader.addClasspath(newClasspathEntry)
        assert initialNumberOfClasspathEntries + 1 == classLoader.getClassPath().length
        assert contains(classLoader.getClassPath(), newClasspathEntry)
    }

    static getPaths(URLClassLoader ucl) {
        def urls = ucl.getURLs()
        return urls.findAll {!it.file.endsWith(".jar")}
    }

    static getPaths(String path) {
        int start = 0, end = 0
        String sep = File.pathSeparator
        def ret = []
        while (end < path.length()) {
            start = end
            end = path.indexOf(sep, end)
            if (end == -1) break
            def sub = path.substring(start, end)
            if (!sub.endsWith(".jar")) {
                ret << ((new File(sub)).toURL())
            }
            end++
        }
        return ret
    }

    void testParseThenLoadByName() {
        def loader = new GroovyClassLoader()
        def clazz = loader.parseClass("println 'howdy'")
        assert clazz == loader.loadClass(clazz.name)
    }

    void testParseThenLoadByNameWeak() {
        def loader = new GroovyClassLoader()
        assert null != loader.loadClass(loader.parseClass("println 'howdy'").name)
    }

    void testClassNotFoundIsNotHidden() {
        def paths = []
        def loader = this.class.classLoader
        while (loader != null) {
            if (loader instanceof URLClassLoader) {
                paths += getPaths(loader)
            }
            loader = loader.parent
        }
        paths += getPaths(System.getProperty("java.class.path"))
        paths = paths.unique()

        def file, tempFolder
        try {
            // On the latest update of the Mac JDK (as of 2009/06/24), the default placement of temp files seems problematic
            // specifying explicitly a custom target directory seems to solve the build issue.
            tempFolder = new File('./build/generated')
            tempFolder.mkdir()
            file = File.createTempFile("Foo", ".groovy", tempFolder)

            def name = file.name - ".groovy"
            def script = """
            class $name extends groovy.test.GroovyTestCase{}
          """
            file << script
            paths << file.parentFile.toURL()
            def cl = new URLClassLoader(paths as URL[], (ClassLoader) null)
            def gcl = new GroovyClassLoader(cl)
            try {
                gcl.loadClass(name)
                assert false
            } catch (NoClassDefFoundError ncdfe) {
                // TODO: hack for running when under coverage - find a better way
                assert ncdfe.message.indexOf("TestCase") > 0 || ncdfe.message.indexOf("cobertura") > 0
            } catch (MultipleCompilationErrorsException mce) {
                mce.errorCollector.errors.each { err ->
                    assert err instanceof SyntaxErrorMessage
                    assert err.cause.message.indexOf("TestCase") > 0 || err.cause.message.indexOf("cobertura") > 0
                }
            }
        } finally {
            try {
                if (file != null) {
                    file.delete()
                    if (tempFolder != null) {
                        tempFolder.delete()
                    }
                }
            } catch (Throwable ignore) { /*drop it*/ }
        }
    }

    void testClassPathNotDerived() {
        def config = new CompilerConfiguration()
        def loader1 = new GroovyClassLoader(null, config)
        config = new CompilerConfiguration()
        config.setClasspath("foo")
        def loader2 = new GroovyClassLoader(loader1, config)
        config = new CompilerConfiguration()
        def loader3 = new GroovyClassLoader(loader2, config)
        def urls = loader1.URLs
        assert urls.length == 0
        urls = loader2.URLs
        assert urls.length == 1
        assert urls[0].toString().endsWith("foo")
        urls = loader3.URLs
        assert urls.length == 0
    }

    void testMultithreading() {
        def config = new CompilerConfiguration()
        config.recompileGroovySource = true

        def loader = new GroovyClassLoaderTestCustomGCL(config)

        def ts = new Thread[100]
        for (i in 0..<100) {
            ts[i] = Thread.start {
                if (i % 2 == 1) sleep(100)
                assert GroovyClassLoaderTestFoo1 == loader.loadClass("Foox")
            }
        }
        sleep(100)
        for (i in 0..<100) {ts[i].join()}

        assert GroovyClassLoaderTestFoo2 == loader.loadClass("Foox")
    }

    void testAdditionalPhaseOperation() {
        def loader = new GroovyClassLoaderTestCustomPhaseOperation()
        def ret = loader.parseClass("""class Foo{}""")
        def field = ret.declaredFields.find {it.name == "id" && it.type == Long.TYPE}
        assert field != null
    }

    void testEncoding() {
        def config = new CompilerConfiguration()
        config.sourceEncoding = "UTF-8"
        def encoding = System.getProperty("file.encoding")
        System.setProperty("file.encoding", "US-ASCII")
        def gcl = new GroovyClassLoader(this.class.classLoader, config)
        // 20AC should be the currency symbol for EURO
        def clazz = gcl.parseClass('return "\u20AC"')
        def result = clazz.newInstance().run()
        int i = result[0]

        try {
            // 0xFFFD is used if the original character was not found,
            // it is the famous ? that can often be seen. So if this here
            // fails, then the String conversion failed at one point
            assert i != 0xFFFD
        } finally {
            System.setProperty("file.encoding", encoding)
        }
    }

    void testPackageDefinitionForGroovyClassesInParseClass() {
        def loader = new GroovyClassLoader(this.class.classLoader)
        def script = """
            package pkg1
            def x = 1
        """
        verifyPackageDetails(loader.parseClass(script, 'Pkg1Groovy3537Script.groovy'), "pkg1")
        
        script = """
            package pkg1.pkg2
            class Groovy3537A{}
        """
        verifyPackageDetails(loader.parseClass(script, 'Pkg1Pkg2Groovy3537A.groovy'), "pkg1.pkg2")
    }
    
    void testPackageDefinitionForGroovyClassesInDefineClass() {
        def loader = new GroovyClassLoader(this.class.classLoader)
        def classNode = new ClassNode("pkg3.Groovy3537B", ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        classNode.addConstructor(new ConstructorNode(ACC_PUBLIC, null))
        def unit = new CompileUnit(loader, new CompilerConfiguration())
        def module = new ModuleNode(unit)
        classNode.setModule(module)
        def clazz = loader.defineClass(classNode, classNode.getName() + ".groovy", "")
        verifyPackageDetails(clazz, "pkg3")
    }
    
    static void verifyPackageDetails(clazz, expectedPkgName) {
        assert clazz.package instanceof Package
        assert clazz.package.name == expectedPkgName
    }
}

class GroovyClassLoaderTestFoo1 {}
class GroovyClassLoaderTestFoo2 {}

class GroovyClassLoaderTestCustomGCL extends GroovyClassLoader {
    GroovyClassLoaderTestCustomGCL(config) {
        super(null, config)
    }
    def counter = new AtomicInteger(0)
    protected Class recompile(URL source, String name, Class oldClass) {
        if (name == "Foox") {
            if (counter.getAndIncrement() < 100) {
                return GroovyClassLoaderTestFoo1
            } else {
                return GroovyClassLoaderTestFoo2
            }
        }
        return super.recompile(source, name, oldClass)
    }

    protected boolean isSourceNewer(URL source, Class cls) {
        return true
    }
}

import static org.objectweb.asm.Opcodes.ACC_PUBLIC

class GroovyClassLoaderTestPropertyAdder implements CompilationUnit.IPrimaryClassNodeOperation {
    @Override
    void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
        classNode.addProperty("id", ACC_PUBLIC, ClassHelper.long_TYPE, null, null, null)
    }
}

class GroovyClassLoaderTestCustomPhaseOperation extends GroovyClassLoader {
    @Override
    CompilationUnit createCompilationUnit(CompilerConfiguration config, CodeSource source) {
        def cu = super.createCompilationUnit(config, source)
        cu.addPhaseOperation(new GroovyClassLoaderTestPropertyAdder(), Phases.CONVERSION)
        return cu
    }
}

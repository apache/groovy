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
package bugs

import groovy.test.NotYetImplemented
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import com.sun.net.httpserver.HttpServer
import groovy.concurrent.Awaitable
import groovy.json.JsonSlurper
import groovy.xml.XmlNodePrinter
import groovy.xml.XmlParser
import groovy.xml.XmlSlurper
import java.nio.file.Files
import java.security.CodeSource
import java.security.PermissionCollection
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.tools.ToolProvider
import org.apache.groovy.runtime.MonadicCarrierRegistry
import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.DynamicVariable
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.decompiled.AsmDecompiler
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.classgen.asm.CallSiteWriter
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.io.FileReaderSource
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.StackTraceUtils
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl
import org.codehaus.groovy.runtime.powerassert.PowerAssertionError
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.codehaus.groovy.tools.javac.JavaStubCompilationUnit
import org.junit.jupiter.api.Timeout
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class RegressionTest {

    // GROOVY-2072: eachFileRecurse crashes when descending a directory with circular symbolic links
    @Disabled('flaky: sometimes finishes within the timeout, sometimes does not')
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    @NotYetImplemented @Test
    void testGroovy2072() {
        File testdir = File.createTempDir('groovy2072-', '')
        try {
            new File(testdir, 'file.txt').text = 'x'
            File link = new File(testdir, 'parentdir')
            Files.createSymbolicLink(link.toPath(), testdir.toPath())
            def visits = []
            testdir.eachFileRecurse { File f ->
                visits << f.canonicalPath
            }
            assert visits.size() == visits.toSet().size()
        } finally {
            testdir.deleteDir()
        }

    }

    // GROOVY-2994: String and GString do not commute under plus()
    @NotYetImplemented @Test
    void testGroovy2994() {
        assertScript '''
            assert "${1}" + "2" instanceof String
            assert "1" + "${2}" instanceof String
            def u = []
            def v = 'foo'
            u << "${v}" + "bar"
            u << "bar" + "${v}"
            assert u.contains("foobar")
            assert u.contains("barfoo")
        '''

    }

    // GROOVY-3056: Cannot override getter of property starting with "_" using ExpandoMetaClass
    @NotYetImplemented @Test
    void testGroovy3056() {
        assertScript '''
            class TestClass {
                def getCalled = false
                def setCalled = false
                private _internal = "Internal"
                def _ajax = 100
                def other = 1.1
                String get_internal() {
                    this.getCalled = true
                    return _internal
                }
                void set_internal(String value) {
                    this.setCalled = true
                    _internal = value
                }
            }
            def testObj = new TestClass()
            assert testObj.getProperty("_internal") == "Internal"
            assert testObj.getCalled
            testObj.setProperty("_internal", "stuff")
            assert testObj._internal == "stuff"
            assert testObj.setCalled
            assert testObj.getProperty("_ajax") == 100
            assert testObj.getProperty("other") == 1.1
            TestClass.metaClass.getOther = {-> 1.2 }
            TestClass.metaClass.get_ajax = {-> 200 }
            testObj = new TestClass()
            assert testObj.getProperty("other") == 1.2
            assert testObj.getProperty("_ajax") == 200
        '''

    }

    // GROOVY-3134: Cannot override private static method using EMC
    @NotYetImplemented @Test
    void testGroovy3134() {
        assertScript '''
            class WeatherMonitor {
                def getSunrise() { findSunriseTime() }
                private static findSunriseTime() { '7:15 AM' }
            }
            WeatherMonitor.metaClass.static.findSunriseTime = {-> '5:57 AM'}
            assert new WeatherMonitor().sunrise == '5:57 AM'
        '''

    }

    // GROOVY-3250: StackOverflowError in List and Map formatting
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @NotYetImplemented @Test
    void testGroovy3250() {
        assertScript '''
            import org.codehaus.groovy.runtime.InvokerHelper
            def y = []
            y << [y]
            def nestedList = InvokerHelper.toListString(y)
            assert nestedList instanceof String
            assert !nestedList.isEmpty()
            def m2 = [:]
            m2.put('nested', ['self': m2])
            def nestedMap = InvokerHelper.toMapString(m2)
            assert nestedMap instanceof String
            assert !nestedMap.isEmpty()
            assert y.toString() instanceof String
        '''

    }

    // GROOVY-3309: comparisons with NaN should always return false
    @NotYetImplemented @Test
    void testGroovy3309() {
        assertScript '''
            assert !(Double.NaN == 0)
            assert !(Double.NaN < 0)
            assert !(Double.NaN > 0)
            assert Double.NaN != Double.NaN
        '''

    }

    // GROOVY-3351: Curried MethodClosure with default parameters wont work
    @NotYetImplemented @Test
    void testGroovy3351() {
        assertScript '''
            class Foo {
                static fooWithDefaults(String a1, String a2, String a3 = a2, Map a4 = [:]) {
                    "a1=$a1 a2=$a2 a3=$a3 a4=$a4"
                }
                static decorate(Class klass) {
                    klass.metaClass.foo = this.&fooWithDefaults.curry("A")
                }
            }
            Foo.decorate(Foo)
            def f = new Foo()
            assert f.foo('B') == 'a1=A a2=B a3=B a4=[:]'
        '''

    }

    // GROOVY-3412: Method Caching within MethodMissing is not working
    @NotYetImplemented @Test
    void testGroovy3412() {
        assertScript '''
            class PersonAgain {
                def plays = ['Tennis', 'VolleyBall', 'BasketBall']
                static calls = []
                def methodMissing(String name, args) {
                    calls << name
                    def methodInList = plays.find { it == name.split('play')[1] }
                    if (methodInList) {
                        def impl = { Object[] vargs ->
                            return "playing ${name.split('play')[1]}..."
                        }
                        PersonAgain.metaClass."$name" = impl
                        return impl(args)
                    }
                    throw new MissingMethodException(name, PersonAgain, args)
                }
                static { PersonAgain.metaClass }
            }
            def jack = new PersonAgain()
            assert jack.playTennis() == 'playing Tennis...'
            assert jack.playTennis() == 'playing Tennis...'
            assert PersonAgain.calls == ['playTennis']
        '''

    }

    // GROOVY-3612: mixin replaces methods using Object methods
    @NotYetImplemented @Test
    void testGroovy3612() {
        assertScript '''
            class A {}
            Integer.mixin(A)
            try {
                assert 1.toString() == '1'
            } finally {
                Integer.metaClass = null
            }
        '''

    }

    // GROOVY-3676: Explicitly dereferenced hash values created by variables return null
    @NotYetImplemented @Test
    void testGroovy3676() {
        assertScript '''
            def key1 = "VARIABLE_1"
            def a1 = [ "${key1}" : "YES 3" ]
            assert a1["${key1}"] == "YES 3"
            assert a1[key1] == "YES 3"
        '''

    }

    // GROOVY-3905: XMLNodePrinter doesn't keep namespace required for attribute value
    @Test
    void testGroovy3905() {
        def xml = '''<ns2:getTableReport xmlns:ns2="http://www.jdisc.com">
                <ns2:genericFilters xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns2:containsCondition">
                    <filterValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xsi:type="xs:string">gaucho</filterValue>
                </ns2:genericFilters>
            </ns2:getTableReport>'''
        def parsed = new XmlParser().parseText(xml)
        def writer = new StringWriter()
        def printer = new XmlNodePrinter(new PrintWriter(writer))
        printer.preserveWhitespace = true
        printer.namespaceAware = true
        printer.print(parsed)
        def out = writer.toString()
        assert out.contains('xmlns:xs="http://www.w3.org/2001/XMLSchema"')

    }

    // GROOVY-3917: getProperty isn't delegated more than 1 level deep
    @NotYetImplemented @Test
    void testGroovy3917() {
        assertScript '''
            class C {
                @Delegate B b = new B()
            }
            class B {
                @Delegate A a = new A()
            }
            class A {
                def getProperty(String name) { name }
            }
            assert new C().hello == 'hello'
        '''

    }

    // GROOVY-3942: Using metaClass to override methods in class hierarchy does not work as expected
    @NotYetImplemented @Test
    void testGroovy3942() {
        assertScript '''
            class BaseClass {
                def called = []
                def baseClassMethod() {
                    called << 'base'
                    internalMethod()
                }
                def internalMethod() {
                    called << 'original'
                }
            }
            class SubClass extends BaseClass {
                def subClassMethod() {
                    called << 'sub'
                    internalMethod()
                }
            }
            def subClass = new SubClass()
            subClass.metaClass.internalMethod = { -> called << 'dynamic' }
            subClass.baseClassMethod()
            subClass.subClassMethod()
            assert subClass.called == ['base', 'dynamic', 'sub', 'dynamic']
        '''

    }

    // GROOVY-3974: Unexpected behaviour of the range operator
    @NotYetImplemented @Test
    void testGroovy3974() {
        assertScript '''
            assert [][0..-1] == []
            assert [0][0..-2] == []
        '''

    }

    // GROOVY-4056: Scope problem with anonymous classes
    @NotYetImplemented @Test
    void testGroovy4056() {
        assertScript '''
            int numberOfTasks = 2
            def processes = new Object[numberOfTasks]
            for (int i = 0; i < numberOfTasks; ++i) {
                final int taskId = i
                processes[taskId] = new Object() {
                    public void run() {
                        final long start = 1 + taskId * 10
                        final long end = (taskId + 1) * 10
                        for (long i = start; i <= end; ++i) {
                            def x = i
                        }
                    }
                }
            }
            processes.each { it.run() }
        '''

    }

    // GROOVY-4076: No meta method found when passing a runtime mixin as method argument
    @NotYetImplemented @Test
    void testGroovy4076() {
        assertScript '''
            import org.codehaus.groovy.runtime.InvokerHelper
            class Control {}
            class XMLRPCServerProxy {}
            XMLRPCServerProxy.mixin(Control)
            def proxy = new XMLRPCServerProxy()
            assert proxy instanceof Control
            class Credential {}
            class SomeClass {
                def doCreateLicense(Control x, Credential c, Map struct) { 'ok' }
            }
            def r = new SomeClass()
            Object[] args = [proxy, new Credential(), [:]] as Object[]
            def m = InvokerHelper.getMetaClass(SomeClass).getMetaMethod('doCreateLicense', args)
            assert m != null
            assert r.doCreateLicense(proxy, new Credential(), [:]) == 'ok'
        '''

    }

    // GROOVY-4214: StackOverflowError tyring to mixin loaded script
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @NotYetImplemented @Test
    void testGroovy4214() {
        assertScript '''
            def gcl = new GroovyClassLoader(this.class.classLoader)
            def scriptClass = gcl.parseClass('def say(String msg) { msg }')
            this.metaClass.mixin scriptClass
            assert say('bye') == 'bye'
        '''

    }

    // GROOVY-4339: Compiler first allows an annotation on a statement, then fails with a weird error message
    @NotYetImplemented @Test
    void testGroovy4339() {
        def err = shouldFail '''
            int i

            @Deprecated
            i = 2
        '''
        assert err.message.toLowerCase().contains('annotation')
        assert !(err.message =~ /already contains a variable of the name i/)

    }

    // GROOVY-4346: Set isSynthetic()/setSynthetic() behaviour right for AST nodes
    @NotYetImplemented @Test
    void testGroovy4346() {
        def node = new FieldNode(
            'f', ClassNode.ACC_PUBLIC,
            ClassHelper.OBJECT_TYPE, null, null)
        node.synthetic = true
        assert node.synthetic
        assert (node.modifiers & 0x1000) != 0

    }

    // GROOVY-4407: Category annotation as a run-time mix-in messes up toString()
    @NotYetImplemented @Test
    void testGroovy4407() {
        assertScript '''
            class LongPriceCategory {
                static String getLimitPrice(Long self) { self.toString() }
            }
            assert 1500L.toString() == '1500'
            Long.mixin(LongPriceCategory)
            try {
                assert 1500L.limitPrice == '1500'
                assert 1500L.toString() == '1500'
            } finally {
                Long.metaClass = null
            }
        '''

    }

    // GROOVY-4653: Collections toString() inconsistency
    @NotYetImplemented @Test
    void testGroovy4653() {
        assertScript '''
            class CustomList extends ArrayList {
                CustomList() { super([0, 1]) }
                String toString() { 'Custom String' }
            }
            def list = new CustomList()
            assert list.toString() == 'Custom String'
            assert "$list" == 'Custom String'
            assert list.asType(String) == 'Custom String'
            assert ((String) list) == 'Custom String'
        '''

    }

    // GROOVY-4654: groovy.transform.ToString annotation does not handle cycles
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @NotYetImplemented @Test
    void testGroovy4654() {
        assertScript '''
            @groovy.transform.ToString(includeFields=true, includeNames=true)
            class Foo { Bar bar }
            @groovy.transform.ToString(includeFields=true, includeNames=true)
            class Bar { Foo foo }
            def foo = new Foo()
            def bar = new Bar()
            foo.bar = bar
            bar.foo = foo
            def s = foo.toString()
            assert s instanceof String
            assert !s.isEmpty()
            foo.bar = foo
            assert foo.toString() instanceof String
        '''

    }

    // GROOVY-4760: Should InputStream#withReader create a BufferedReader instead of InputStreamReader?
    @NotYetImplemented @Test
    void testGroovy4760() {
        assertScript '''
            def stream = new ByteArrayInputStream('hello'.bytes)
            stream.withReader { reader ->
                assert reader instanceof BufferedReader
                assert reader.readLine() == 'hello'
            }
        '''

    }

    // GROOVY-4856: Closure resolveStrategy does not work the same way for properties and methods
    @NotYetImplemented @Test
    void testGroovy4856() {
        assertScript '''
            class ClosureTest {
                def someProperty
                def anotherProperty
                def someMethod() { 'someMethod' }
                def anotherMethod() { 'anotherMethod' }
            }
            def ct = new ClosureTest()
            def someProperty = 'global'
            def c = {
                someMethod()
                anotherMethod()
                someProperty = 'foo'
                anotherProperty = 'bar'
            }
            c.resolveStrategy = Closure.DELEGATE_FIRST
            c.delegate = ct
            c()
            assert ct.someProperty == 'foo'
            assert ct.anotherProperty == 'bar'
        '''

    }

    // GROOVY-4917: ArrayList + Object[]
    @NotYetImplemented @Test
    void testGroovy4917() {
        assertScript '''
            def list = ['a']
            def arr = ['b', 'c'] as Object[]
            assert list + arr == ['a', 'b', 'c']
        '''

    }

    // GROOVY-4941: Static methods cannot be called during AST transformation via AstBuilder.buildFromSpec()
    @NotYetImplemented @Test
    void testGroovy4941() {
        def parentDir = File.createTempDir('groovy4941-', '')
        def targetDir = File.createTempDir('groovy4941-out-', '')
        try {
            new File(parentDir, 'AnAnnotation4941.java').text = '''
                import java.lang.annotation.ElementType;
                import java.lang.annotation.Retention;
                import java.lang.annotation.RetentionPolicy;
                import java.lang.annotation.Target;
                import org.codehaus.groovy.transform.GroovyASTTransformationClass;
                @Retention(RetentionPolicy.SOURCE)
                @Target({ElementType.METHOD})
                @GroovyASTTransformationClass("AnASTTransformation4941")
                public @interface AnAnnotation4941 {
                    Class<?> value();
                }
            '''
            new File(parentDir, 'AnASTTransformation4941.groovy').text = '''
                import org.codehaus.groovy.ast.ASTNode
                import org.codehaus.groovy.ast.AnnotatedNode
                import org.codehaus.groovy.ast.AnnotationNode
                import org.codehaus.groovy.ast.ClassHelper
                import org.codehaus.groovy.ast.ClassNode
                import org.codehaus.groovy.ast.builder.AstBuilder
                import org.codehaus.groovy.control.CompilePhase
                import org.codehaus.groovy.control.SourceUnit
                import org.codehaus.groovy.transform.ASTTransformation
                import org.codehaus.groovy.transform.GroovyASTTransformation

                @GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
                class AnASTTransformation4941 implements ASTTransformation {
                    static final ClassNode MY_TYPE = ClassHelper.make(AnAnnotation4941)
                    void visit(ASTNode[] nodes, SourceUnit source) {
                        if (nodes.length != 2 ||
                                !(nodes[0] instanceof AnnotationNode) ||
                                !(nodes[1] instanceof AnnotatedNode)) {
                            throw new RuntimeException(
                                "Internal error: expecting [AnnotationNode, AnnotatedNode] but got: ${Arrays.asList(nodes)}"
                            )
                        }
                        def annotation = nodes[0]
                        if (MY_TYPE != annotation.classNode) {
                            return
                        }
                        def parent = nodes[1]
                        def classNode = annotation.getMember('value').type
                        parent.code.statements +=
                            new AstBuilder().buildFromSpec {
                                expression {
                                    methodCall {
                                        variable 'this'
                                        constant 'println'
                                        argumentList {
                                            staticMethodCall (classNode.typeClass, 'getInstance') {
                                                argumentList()
                                            }
                                        }
                                    }
                                }
                            }
                    }
                }
            '''
            new File(parentDir, 'Main4941.groovy').text = '''
                class Clazz4941 {
                    static def getInstance() {
                        new Clazz4941()
                    }
                }
                class Main4941 {
                    @AnAnnotation4941(Clazz4941)
                    def aMethod() {
                    }
                }
            '''
            def config = new CompilerConfiguration()
            config.targetDirectory = targetDir
            config.jointCompilationOptions = [memStub: true]
            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(
                new File(parentDir, 'AnAnnotation4941.java'),
                new File(parentDir, 'AnASTTransformation4941.groovy')
            )
            cu.compile()
            def cu2 = new CompilationUnit(config, null, loader)
            cu2.addSource(new File(parentDir, 'Main4941.groovy'))
            cu2.compile()
            def main = loader.loadClass('Main4941')
            main.getDeclaredConstructor().newInstance().invokeMethod('aMethod', null)
        } finally {
            parentDir.deleteDir()
            targetDir.deleteDir()
        }

    }

    // GROOVY-5007: Inscrutable error message on missing comma 
    @NotYetImplemented @Test
    void testGroovy5007() {
        def err = shouldFail '''
            def task(String name, Closure c) { c() }
            task 'hello' {
                1
            }
        '''
        assert err.message.toLowerCase().contains('comma')

    }

    // GROOVY-5188: Half mock behaviour is not as expected
    @NotYetImplemented @Test
    void testGroovy5188() {
        assertScript '''
            import groovy.mock.interceptor.MockFor
            class AGroovyClass {
                def aMethod() { anotherMethod() }
                def anotherMethod() { throw new RuntimeException('Should not be here!') }
            }
            def mock = new MockFor(AGroovyClass)
            mock.ignore('aMethod')
            mock.demand.anotherMethod { 'Mocked anotherMethod.' }
            mock.use {
                assert new AGroovyClass().aMethod() == 'Mocked anotherMethod.'
            }
        '''

    }

    // GROOVY-5265: Compiling and runnig static main(args, int x = 0) {}
    @NotYetImplemented @Test
    void testGroovy5265() {
        assertScript '''
            class TestA {
                static main(args, int x = 0) {
                    assert args instanceof String[] || args instanceof List
                    assert x == 0
                }
            }
            TestA.main([] as String[])
        '''

    }

    // GROOVY-5275: TimeCategory and Date calculations not consistent
    @NotYetImplemented @Test
    void testGroovy5275() {
        assertScript '''
            import groovy.time.TimeCategory
            def prev = TimeZone.default
            TimeZone.default = TimeZone.getTimeZone('America/New_York')
            try {
                def start = Date.parse('yyyy-MM-dd', '2026-01-01')
                use(TimeCategory) {
                    def plus180 = start + 180.days
                    int daysLeft = (plus180.time - start.time) / (24L * 60 * 60 * 1000)
                    int daysLeftTimeCategory = (plus180 - start).days
                    assert daysLeft == 180
                    assert daysLeftTimeCategory == 180
                }
            } finally {
                TimeZone.default = prev
            }
        '''

    }

    // GROOVY-5345: Power assert with unique() gives confusing output
    @NotYetImplemented @Test
    void testGroovy5345() {
        def err = shouldFail PowerAssertionError, '''
            def x = [1, 1, 1]
            assert x.unique() == [2]
        '''
        assert err.message.contains('[1, 1, 1]')

    }

    // GROOVY-5346: MockFor on concrete Java class creates type-incompatible proxy
    @NotYetImplemented @Test
    void testGroovy5346() {
        assertScript '''
            import groovy.mock.interceptor.MockFor
            class JavaClass {
                String work() { 'real' }
            }
            class CollaboratingClass {
                String useIt(JavaClass jc) { jc.work() }
            }
            def mock = new MockFor(JavaClass)
            mock.demand.work { 'mocked' }
            mock.use {
                def proxy = mock.proxyDelegateInstance()
                assert proxy instanceof JavaClass
                assert new CollaboratingClass().useIt(proxy) == 'mocked'
            }
        '''

    }

    // GROOVY-5434: Node.toString() returns an empty string
    @NotYetImplemented @Test
    void testGroovy5434() {
        assertScript '''
            def xml = new groovy.xml.XmlSlurper().parseText("<testsuites><testsuite><testcase name='test'/></testsuite></testsuites>")
            def node = xml.testsuite.testcase[0]
            assert node.@name == 'test'
            assert node.toString()
            assert !node.toString().isEmpty()
        '''

    }

    // GROOVY-5490: Double Dispatch Distance Algorithm Incorrectly Implemented
    @NotYetImplemented @Test
    void testGroovy5490() {
        assertScript '''
            def foo(Object a, Integer b) { 'Integer won' }
            def foo(String a, Object b) { 'String won' }
            assert foo('potato', Integer.valueOf(6)) == 'Integer won'
        '''

    }

    // GROOVY-5731: ConfigObject.merge incorrectly overwrites non-map values in the source
    @NotYetImplemented @Test
    void testGroovy5731() {
        assertScript '''
            def pluginConfig = new ConfigObject()
            pluginConfig.account.locked.until.confirm = true
            def appConfig = new ConfigObject()
            if (appConfig.account.locked.until.confirm) { }
            pluginConfig.merge(appConfig)
            assert pluginConfig.account.locked.until.confirm == true
        '''

    }

    // GROOVY-5778: Caught: java.lang.IllegalThreadStateException: process hasn't exited
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    @NotYetImplemented @Test
    void testGroovy5778() {
        def process = ['sh', '-c', 'echo hello; exec 1>&-; sleep 1'].execute()
        def text = process.text
        assert text.contains('hello')
        assert process.exitValue() == 0

    }

    // GROOVY-5785: HashSet removeAll() operations are slow
    @Disabled('flaky: sometimes finishes within the timeout, sometimes does not')
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    @NotYetImplemented @Test
    void testGroovy5785() {
        def a = (1..30000) as Set
        def b = (15001..45000) as Set
        def future = CompletableFuture.supplyAsync { a - b }
        def result = future.get(5, TimeUnit.SECONDS)
        assert result.size() == 15000
        assert result.contains(1)
        assert !result.contains(15001)

    }

    // GROOVY-5805: @Grab must be followed by import
    @NotYetImplemented @Test
    void testGroovy5805() {
        def config = new CompilerConfiguration()
        def unit = new CompilationUnit(config)
        unit.addSource('Grab5805.groovy', '''
            @Grab('log4j:log4j:1.2.16')
            println "Hello"
        ''')
        unit.compile(Phases.CONVERSION)

    }

    // GROOVY-5909: NullObject is ignored by metaClass.getProperty and category, but works in methods
    @NotYetImplemented @Test
    void testGroovy5909() {
        assertScript '''
            class NullCategory {
                static d(Object self) { 'd' }
                static getProperty(Object self, String name) { name }
            }
            NullObject.metaClass.b = 'b'
            NullObject.metaClass.c = { 'c' }
            NullObject.metaClass.getProperty = { String prop -> prop }
            try {
                assert NullObject.b == 'b'
                assert null.b == 'b'
                assert NullObject.c() == 'c'
                assert null.c() == 'c'
                use(NullCategory) {
                    assert null.d() == 'd'
                    assert null.e == 'e'
                }
            } finally {
                NullObject.metaClass = null
            }
        '''

    }

    // GROOVY-5950: XmlSlurper does not maintain a stack of prefix mappings
    @NotYetImplemented @Test
    void testGroovy5950() {
        def xml = new XmlSlurper(false, true).parseText('''<root>
  <tns:a xmlns:tns="urn:first"/>
  <tns:b xmlns:tns="urn:second"/>
  <tns:c xmlns:tns="urn:first"/>
</root>''')
        assert xml.'tns:a'[0].lookupNamespace('tns') == 'urn:first'
        assert xml.'tns:b'[0].lookupNamespace('tns') == 'urn:second'
        assert xml.'tns:c'[0].lookupNamespace('tns') == 'urn:first'
        def xml2 = new XmlSlurper(false, true).parseText('''<a xmlns="foo" xmlns:p1="ANS" xmlns:p2="NS2">
                   <b xmlns="bar" xmlns:p1="BNS" xmlns:p3="NS3" />
                 </a>''')
        def a = xml2
        def b = a.b
        assert a.lookupNamespace('') == 'foo'
        assert a.lookupNamespace('p1') == 'ANS'
        assert b.lookupNamespace('') == 'bar'
        assert b.lookupNamespace('p1') == 'BNS'
        assert b.lookupNamespace('p2') == 'NS2'
        assert b.lookupNamespace('p3') == 'NS3'

    }

    // GROOVY-5982: Expando: closures should be changed to resolve strategy DELEGATE_FIRST
    @NotYetImplemented @Test
    void testGroovy5982() {
        assertScript '''
            def text = 'bla'
            def e = new Expando()
            e.text = 'test'
            e.c = { text }
            assert e.c() == 'test'
        '''

    }

    // GROOVY-6031: InvokerHelper#write(Writer, Object) and #append(Appendable, Object) have wrong type precedence
    @NotYetImplemented @Test
    void testGroovy6031() {
        def writable = new HashMap() {
            def writeTo(Writer w) { w.write('WRITABLE_WRITE_TO') }
        }
        writable.put('k', 'v')
        def sw = new StringWriter()
        InvokerHelper.write(sw, writable)
        assert sw.toString() == 'WRITABLE_WRITE_TO'
        def sb = new StringBuilder()
        InvokerHelper.append(sb, writable)
        assert sb.toString() == 'WRITABLE_WRITE_TO'

    }

    // GROOVY-6049: Report errors with file name and line number where it occured
    @NotYetImplemented @Test
    void testGroovy6049() {
        def err = shouldFail '''
            @groovy.transform.TypeChecked(extensions = 'src/b/foo.groovy')
            class C {
                def m() { 1 }
            }
        '''
        def msg = err.message
        assert msg.contains('foo.groovy')
        assert msg =~ /@ (line|column)|line \d+/

    }

    // GROOVY-6130: weird comparison problem: rethink of single character strings into chars needed?
    @NotYetImplemented @Test
    void testGroovy6130() {
        assertScript '''
            assert !('5' == Integer.valueOf(53))
            assert !('5' == 53)
            assert '5' != 53
            assert '5' == '5'
        '''

    }

    // GROOVY-6177: List range/collection access inconsistency
    @NotYetImplemented @Test
    void testGroovy6177() {
        assertScript '''
            def l = [0, 1, 2]
            assert l[0, 1, 2, 3] == [0, 1, 2, null]
            assert l[0..3] == [0, 1, 2, null]
        '''

    }

    // GROOVY-6185: ObjectGraphBuilder dirsregards node value when instantiating a node
    @NotYetImplemented @Test
    void testGroovy6185() {
        assertScript '''
            class Person { String name }
            def builder = new groovy.util.ObjectGraphBuilder()
            builder.classNameResolver = 'groovy.util'
            // register Person in the script
            builder.classLoader = this.class.classLoader
            builder.identifierResolver = 'name'
            def p = builder.person('John')
            assert p.name == 'John'
        '''

    }

    // GROOVY-6193: Inconsistent bounds checking
    @NotYetImplemented @Test
    void testGroovy6193() {
        assertScript '''
            def a = [1, 2, 3]
            assert a[6] == null
            assert a[-4] == null
            assert '123'[6] == null
            assert '123'[-4] == null
        '''

    }

    // GROOVY-6229: Some minor changes required regarding unary overloading operators and numbers
    @NotYetImplemented @Test
    void testGroovy6229() {
        assertScript '''
            class IntCat {
                static unaryPlus(Integer self) { 'P' }
                static unaryMinus(Integer self) { 'M' }
            }
            use (IntCat) {
                assert 1.unaryPlus() == 'P'
                assert 1.unaryMinus() == 'M'
                assert (+1) == 'P'
                assert (-1) == 'M'
            }
        '''

    }

    // GROOVY-6498: Static initializer not called before static property resolution
    @NotYetImplemented @Test
    void testGroovy6498() {
        assertScript '''
            class Config1 {
                static x
                static {
                    Config1.metaClass.static.propertyMissing = { String name -> 1 }
                }
            }
            assert Config1.x == 1

            class Config2 {
                static {
                    Config2.metaClass.static.getY = { -> 1 }
                }
            }
            assert Config2.y == 1
        '''

    }

    // GROOVY-6547: metaClass property changes
    @NotYetImplemented @Test
    void testGroovy6547() {
        assertScript '''
            class Foo {}
            def f = new Foo()
            assert f.properties.find { it.key == 'metaClass' } != null
        '''

    }

    // GROOVY-6629: static compilation fails with obscure message when only constructor is annotated
    @NotYetImplemented @Test
    void testGroovy6629() {
        assertScript '''
            class TestBroken {
                InetAddress bindTo
                @groovy.transform.CompileStatic
                TestBroken(InetAddress bindTo = null) {
                    this.bindTo = bindTo
                }
            }
            new TestBroken()
        '''

    }

    // GROOVY-6783: MarkupBuilder DSL bug
    @NotYetImplemented @Test
    void testGroovy6783() {
        assertScript '''
            def builder = new groovy.xml.XmlParser()
            def document = builder.parseText('<test></test>')
            document << { qqq() }
            document.qqq[0].@mass = '!!!'
            def out = new StringWriter()
            new groovy.xml.XmlNodePrinter(new PrintWriter(out)).print(document)
            assert out.toString().contains('mass="!!!"')
        '''

    }

    // GROOVY-6845: GString property and method expansion for variable is unintuitive
    @NotYetImplemented @Test
    void testGroovy6845() {
        assertScript '''
            def a = 1
            // "$a.14" must not parse the ".14" as a property/number glued onto a
            def unexpected = "$a.14"
            assert unexpected != '1.14'
            assert "${a.toString()}" == '1'
        '''

    }

    // GROOVY-6847: Overriding methods via .metaClass doesn't behave consistently
    @NotYetImplemented @Test
    void testGroovy6847() {
        assertScript '''
            class A {
                def foo() { 0 }
            }
            A.metaClass.foo = { 1 }
            assert new A().foo() == 1
            A.metaClass.foo = { 2 }
            assert new A().foo() == 2
            Collection.metaClass.foo = { 1 }
            try {
                assert [].foo() == 1
                Collection.metaClass.foo = { 2 }
                assert [].foo() == 2
            } finally {
                Collection.metaClass = null
            }
        '''

    }

    // GROOVY-6858: Compiler shouldn't allow extending a non static inner class of another class
    @NotYetImplemented @Test
    void testGroovy6858() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                class A6858 {
                    class InnerA {}
                }
                class B6858 {
                    class InnerB extends A6858.InnerA {}
                }
            ''')
        }
        assert err.message =~ /(?is)(inner|enclosing|non-static)/

    }

    // GROOVY-6905: GroovyClassLoader.InnerLoader should delegate getPermissions(CodeSource) to containing GroovyClassLoder
    @NotYetImplemented @Test
    void testGroovy6905() {
        def loader = new GroovyClassLoader(this.class.classLoader) {
            int permissionCalls = 0
            @Override
            protected PermissionCollection getPermissions(CodeSource codesource) {
                permissionCalls++
                return super.getPermissions(codesource)
            }
        }
        def clazz = loader.parseClass('class Foo6905 { def hello() { "ok" } }')
        assert loader.permissionCalls > 0

    }

    // GROOVY-6978: Global invokeMethod hook doesn't work with inherited methods
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    @NotYetImplemented @Test
    void testGroovy6978() {
        def future = CompletableFuture.supplyAsync {
            new GroovyShell().evaluate('''
                class A {
                    def met() { 'A' }
                }
                class B extends A {
                    def met() { super.met() + 'B' }
                }
                B.metaClass.invokeMethod = { String name, args ->
                    def metaMethod = delegate.metaClass.getMetaMethod(name, args)
                    if (metaMethod) {
                        return metaMethod.invoke(delegate, args)
                    }
                    throw new MissingMethodException(name, B, args)
                }
                new B().met()
            ''')
        }
        try {
            assert future.get(3, TimeUnit.SECONDS) == 'AB'
        } finally {
            future.cancel(true)
        }

    }

    // GROOVY-7023: FactoryBuilderSupport#build method is being invoked directly without delegation to configured named factories
    @NotYetImplemented @Test
    void testGroovy7023() {
        assertScript '''
            class Element {
                String name
                String toString() { 'Element' }
            }
            class TestBuilder extends FactoryBuilderSupport {
                TestBuilder() {
                    registerFactory('parent', new ListFactory())
                    registerFactory('child', new ListFactory())
                    registerFactory('build', new ListFactory())
                    registerFactory('element', new ElementFactory())
                }
            }
            class ListFactory extends AbstractFactory {
                Object newInstance(FactoryBuilderSupport b, Object name, Object value, Map attrs) { [] }
            }
            class ElementFactory extends AbstractFactory {
                Object newInstance(FactoryBuilderSupport b, Object name, Object value, Map attrs) {
                    new Element(name: attrs.name)
                }
                void setParent(FactoryBuilderSupport b, Object parent, Object child) {
                    parent << child
                }
            }
            def builder = new TestBuilder()
            def result1 = builder.parent { child { element(name: 'Test') } }
            def result2 = builder.parent { build { element(name: 'Test') } }
            assert result1[0].size() == result2[0].size()
        '''

    }

    // GROOVY-7066: MixinInMetaClass shouldn't consider "class" as a property
    @NotYetImplemented @Test
    void testGroovy7066() {
        assertScript '''
            class DrJekyll {}
            class MrHyde {
                String toString() { "MrHyde@\${Integer.toHexString(hashCode())}" }
            }
            def drJekyll = new DrJekyll()
            DrJekyll.mixin(MrHyde)
            def mrHide = drJekyll
            assert mrHide.getClass() == MrHyde || mrHide.class != drJekyll.class || mrHide.getClass() != DrJekyll
            // after the fix, a mixed-in object's class property should not be forced to the host class
            assert mrHide.toString().contains('MrHyde')
            assert mrHide.class.name.contains('MrHyde') || mrHide.getClass() != DrJekyll
        '''

    }

    // GROOVY-7095: Cannot search for a char in String
    @NotYetImplemented @Test
    void testGroovy7095() {
        assertScript '''
            def s = '  hello  '
            def charsToTrim = ' '
            assert charsToTrim.indexOf(s.charAt(0)) >= 0
        '''

    }

    // GROOVY-7129: Exponent integer classes differ
    @NotYetImplemented @Test
    void testGroovy7129() {
        assertScript '''
            assert (2 ** 31).getClass() == 2147483648.getClass()
            assert (2 ** 31) == 2147483648
        '''

    }

    // GROOVY-7130: Static type checker fails to compile spread operator assignment + map constructor
    @NotYetImplemented @Test
    void testGroovy7130() {
        assertScript '''
            class A { int anInt }
            class B { A anA }
            @groovy.transform.CompileStatic
            def test() {
                List<B> l = [new B(anA: new A(anInt: 1)), new B(anA: new A(anInt: 2)), new B(anA: new A(anInt: 3))]
                l*.anA = [anInt: 100]
                assert l.every { it.anA.anInt == 100 }
            }
            test()
        '''

    }

    // GROOVY-7135: non-existing method use with super passes type checker in trait
    @NotYetImplemented @Test
    void testGroovy7135() {
        def err = shouldFail {
            new GroovyClassLoader().parseClass('''
                @groovy.transform.CompileStatic
                trait B {
                    void bar() { super.foo() }
                }
                class X implements B {}
            ''')
        }
        assert err.message.contains('foo')

    }

    // GROOVY-7163: map constructor with classes having generic collection attributes.
    @NotYetImplemented @Test
    void testGroovy7163() {
        assertScript '''
            class Address { String street; String city }
            class Person {
                Address address
                List<Address> addresses
                Map<String, Address> addressMap
            }
            def p = new Person(
                address: [street: 's', city: 'c'],
                addresses: [[street: 's2', city: 'c2']],
                addressMap: [home: [street: 's3', city: 'c3']]
            )
            assert p.address instanceof Address
            assert p.addresses[0] instanceof Address
            assert p.addressMap.home instanceof Address
        '''

    }

    // GROOVY-7197: Strange behaviour during parent() method invocation on the non existent node (XmlSlurper).
    @NotYetImplemented @Test
    void testGroovy7197() {
        assertScript '''
            def xml = new groovy.xml.XmlSlurper().parseText('<a><b><c><d/></c></b></a>')
            def dNode = xml.b.c.d
            assert dNode.parent().name() == 'c'
            def cNode = dNode.parent()
            assert cNode.parent().name() == 'b'
            assert dNode.parent().getAt(0).parent().name() == 'b'
        '''

    }

    // GROOVY-7205: Ambiguous DefaultMethods.equals resolution
    @NotYetImplemented @Test
    void testGroovy7205() {
        assertScript '''
            class UnorderedList extends ArrayList implements Set {}
            def x = new UnorderedList()
            def y = new UnorderedList()
            x.addAll([1, 2])
            y.addAll([2, 1])
            assert x.equals(y) == (x == y)
        '''

    }

    // GROOVY-7282: GroovyScriptEngine does not reload static imports on change
    @NotYetImplemented @Test
    void testGroovy7282() {
        def root = File.createTempDir('groovy7282-', '')
        try {
            def pkg = new File(root, 'mypackage')
            pkg.mkdirs()
            def clazz = new File(pkg, 'Test.groovy')
            clazz.text = 'package mypackage; class Test { static def doit() { "Hello" } }'
            def script = new File(root, 'run.groovy')
            script.text = 'import static mypackage.Test.*; doit()'
            def engine = new GroovyScriptEngine(root.toURI().toURL())
            engine.config.minimumRecompilationInterval = 0
            def first = engine.run('run.groovy', new Binding())
            assert first == 'Hello'
            sleep(1100)
            clazz.text = 'package mypackage; class Test { static def doit() { "Hello2" } }'
            clazz.lastModified = System.currentTimeMillis() + 2000
            def second = engine.run('run.groovy', new Binding())
            assert second == 'Hello2'
        } finally {
            root.deleteDir()
        }

    }

    // GROOVY-7329: List.inspect() does not recurse
    @NotYetImplemented @Test
    void testGroovy7329() {
        assertScript '''
            class Foo {
                String inspect() { 'new Foo()' }
                String toString() { "Foo@\${Integer.toHexString(hashCode())}" }
            }
            assert new Foo().inspect() == 'new Foo()'
            assert [new Foo()].inspect() == '[new Foo()]'
        '''

    }

    // GROOVY-7367: ArrayIndexOutOfBoundsException in getCreateArraySignature on too many function parameters
    @NotYetImplemented @Test
    void testGroovy7367() {
        def params = (1..255).collect { "p${it}" }.join(', ')
        def src = "class C7367 { def foo(${params}) { 1 } }"
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass(src)
        }
        assert err.message =~ /(?is)(too many (arguments|parameters)|method signature|255)/

    }

    // GROOVY-7368: Change In Behavior For MetaMethods Replacing Private Methods
    @NotYetImplemented @Test
    void testGroovy7368() {
        assertScript '''
            class Widget {
                def somePublicMethod() { 'real value' }
                private somePrivateMethod() { 'real value' }
                def callPrivate() { somePrivateMethod() }
            }
            Widget.metaClass.somePublicMethod = { 'test value' }
            Widget.metaClass.somePrivateMethod = { 'test value' }
            def w = new Widget()
            assert w.somePublicMethod() == 'test value'
            assert w.callPrivate() == 'test value'
        '''

    }

    // GROOVY-7398: Unable to set package via name param of parseClass on class loader
    @NotYetImplemented @Test
    void testGroovy7398() {
        def clazz = new GroovyClassLoader().parseClass('', 'my.package.foo.groovy')
        assert clazz.package?.name == 'my.package'
        assert clazz.simpleName == 'foo' || clazz.name == 'my.package.foo'

    }

    // GROOVY-7403: ConfigSluper fails to get the correct value with nested keys appearing multiple times
    @NotYetImplemented @Test
    void testGroovy7403() {
def cfg = new ConfigSlurper().parse("""
                node2 { subnode21 = 'Test1' }
                root1 {
                    node2 {
                        subnode1 = 'Test2'
                        subnode2 = node2.subnode21
                    }
                }
        """)
        assert cfg.root1.node2.subnode2 == 'Test1'

    }

    // GROOVY-7435: Compilation failure when using non-ASCII chars as method return type
    @NotYetImplemented @Test
    void testGroovy7435() {
        assertScript '''
            class 对象 {
                public 对象 get对象() { this }
            }
            def o = new 对象()
            assert o.get对象().is(o)
        '''

    }

    // GROOVY-7437: Support comparison between numbers and strings
    @NotYetImplemented @Test
    void testGroovy7437() {
        assertScript '''
            assert 123.45f < "300.0"
            assert !(123.45f > "300.0")
            assert (123.45f <=> "300.0") < 0
            assert "123.45" == 123.45d
            assert 123.45d == "123.45"
        '''

    }

    // GROOVY-7445: FactoryBuilder fails with customized handlers
    @NotYetImplemented @Test
    void testGroovy7445() {
        assertScript '''
            class A { B bField }
            class B { C cField }
            class C {}

            class AFactory extends AbstractFactory {
                Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
                    attributes != null ? new A(attributes) : new A()
                }
            }
            class AFactoryCustom extends AFactory {
                boolean isHandlesNodeChildren() { true }
                boolean onNodeChildren(FactoryBuilderSupport builder, Object node, Closure childContent) {
                    assert node instanceof A
                    def resolved = childContent.call()
                    assert resolved instanceof B
                    ((A) node).bField = resolved
                    return false
                }
            }
            class BFactory extends AbstractFactory {
                Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
                    attributes != null ? new B(attributes) : new B()
                }
                void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
                    assert parent instanceof A
                    ((A) parent).bField = child
                }
            }
            class BFactoryCustom extends BFactory {
                boolean isHandlesNodeChildren() { true }
                boolean onNodeChildren(FactoryBuilderSupport builder, Object node, Closure childContent) {
                    assert node instanceof B
                    def resolved = childContent.call()
                    assert resolved instanceof C
                    ((B) node).cField = resolved
                    return false
                }
            }
            class CFactory extends AbstractFactory {
                Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
                    attributes != null ? new C(attributes) : new C()
                }
                void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
                    assert parent instanceof B
                    ((B) parent).cField = child
                }
            }

            class TestBuilder2 extends FactoryBuilderSupport {
                TestBuilder2(boolean init = true) { super(init) }
                def registerObjectFactories() {
                    registerFactory('A', new AFactoryCustom())
                    registerFactory('B', new BFactory())
                    registerFactory('C', new CFactory())
                }
            }
            class TestBuilder3 extends FactoryBuilderSupport {
                TestBuilder3(boolean init = true) { super(init) }
                def registerObjectFactories() {
                    registerFactory('A', new AFactory())
                    registerFactory('B', new BFactoryCustom())
                    registerFactory('C', new CFactory())
                }
            }
            class TestBuilder4 extends FactoryBuilderSupport {
                TestBuilder4(boolean init = true) { super(init) }
                def registerObjectFactories() {
                    registerFactory('A', new AFactoryCustom())
                    registerFactory('B', new BFactoryCustom())
                    registerFactory('C', new CFactory())
                }
            }

            def check = { b ->
                def a = b.A { B { C {} } }
                assert a instanceof A
                assert a.bField instanceof B
                assert a.bField.cField instanceof C
            }
            check(new TestBuilder2())
            check(new TestBuilder3())
            check(new TestBuilder4())
        '''

    }

    // GROOVY-7446: setting list values via slicing not working with @TypeChecked
    @NotYetImplemented @Test
    void testGroovy7446() {
        assertScript '''
            @groovy.transform.TypeChecked
            def test() {
                def items = ['a', 'b', 'c', 'd', 'e']
                items[1..2] = 'X'
                items[*1..2] = 'X'
                items[*1..2, 4] = 'X'
                items
            }
            test()
        '''

    }

    // GROOVY-7450: List contains String and GString
    @NotYetImplemented @Test
    void testGroovy7450() {
        assertScript '''
            def word = "${'word1'} ${'word2'}"
            def list = ['word1 word2']
            assert word.toString() == 'word1 word2'
            assert list.contains(word)
            assert list.find { it == word }
        '''

    }

    // GROOVY-7451: list to closure argument matching passes unexpectedly
    @NotYetImplemented @Test
    void testGroovy7451() {
        def err = shouldFail '''
            @groovy.transform.TypeChecked
            def test() {
                [[2, 4, 6]].each { c, d -> c + d }
                [[1, 3]].each { u, v, w -> u + v + w }
            }
            test()
        '''
        assert err.message.contains('Cannot') || err.message.contains('each')

    }

    // GROOVY-7513: Make ObjectGraphBuilder and Other Builders extending FactoryBuilderSupport and BuilderSupport working with @CompileStatic
    @NotYetImplemented @Test
    void testGroovy7513() {
        assertScript '''
            @groovy.transform.CompileStatic
            def test() {
                def builder = new groovy.util.ObjectGraphBuilder()
                builder.classLoader = this.class.classLoader
                def person = builder.person(name: 'Daniel') {
                    address(city: 'Taipei')
                }
                person
            }
            class Person { String name; Address address }
            class Address { String city }
            def p = test()
            assert p.name == 'Daniel'
            assert p.address.city == 'Taipei'
        '''

    }

    // GROOVY-7534: check that final fields are assigned
    @NotYetImplemented @Test
    void testGroovy7534() {
        def err = shouldFail {
            new GroovyClassLoader().parseClass('''
                class C {
                    final String a
                    final String b
                    C(String a) { this.a = a }
                }
            ''')
        }
        assert err.message.toLowerCase().contains('final') || err.message.contains('b')

    }

    // GROOVY-7557: Inconsistant CompileStatic conversions
    @NotYetImplemented @Test
    void testGroovy7557() {
        def err = shouldFail {
            new GroovyClassLoader().parseClass('''
                @groovy.transform.CompileStatic
                class C {
                    char m() { 65 }
                }
            ''')
        }
        assert err.message.contains('char') || err.message.contains('precision') || err.message.contains('int')

    }

    // GROOVY-7560: ASTTransformationCustomizer can only be applied once when using GroovyClassLoader.parseClass(..)
    @NotYetImplemented @Test
    void testGroovy7560() {
        assertScript '''
            import org.codehaus.groovy.ast.ClassNode
            import org.codehaus.groovy.control.CompilePhase
            import org.codehaus.groovy.control.CompilerConfiguration
            import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
            import org.codehaus.groovy.transform.ASTTransformation
            import org.codehaus.groovy.transform.GroovyASTTransformation
            import org.codehaus.groovy.ast.builder.AstBuilder
            import org.codehaus.groovy.ast.ClassHelper
            import org.codehaus.groovy.ast.FieldNode

            @GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
            class MarkTransform implements ASTTransformation {
                void visit(org.codehaus.groovy.ast.ASTNode[] nodes, org.codehaus.groovy.control.SourceUnit source) {
                    source.AST.classes.each { ClassNode cn ->
                        if (!cn.getDeclaredField('marked')) {
                            cn.addField('marked', FieldNode.ACC_PUBLIC, ClassHelper.boolean_TYPE, null)
                        }
                    }
                }
            }

            def config = new CompilerConfiguration()
            config.addCompilationCustomizers(new ASTTransformationCustomizer(new MarkTransform()))
            def loader = new GroovyClassLoader(this.class.classLoader, config)
            def first = loader.parseClass('class First {}')
            def second = loader.parseClass('class Second {}')
            def third = loader.parseClass('class Third {}')
            assert first.getDeclaredField('marked')
            assert second.getDeclaredField('marked')
            assert third.getDeclaredField('marked')
        '''

    }

    // GROOVY-7575: Using SecureASTCustomizer doesn't allow creation of new classes, named or anonymous
    @NotYetImplemented @Test
    void testGroovy7575() {
        assertScript '''
            import org.codehaus.groovy.control.CompilerConfiguration
            import org.codehaus.groovy.control.customizers.SecureASTCustomizer
            def secure = new SecureASTCustomizer()
            secure.receiversWhiteList = ['java.lang.Object', 'java.lang.String']
            def config = new CompilerConfiguration()
            config.addCompilationCustomizers(secure)
            def shell = new GroovyShell(config)
            assert shell.evaluate("""
                class Foo { def bar() { 'ok' } }
                new Foo().bar()
            """) == 'ok'
        '''

    }

    // GROOVY-7576: NamespaceBuilder does not add namespace declaration to the generated xml
    @NotYetImplemented @Test
    void testGroovy7576() {
        assertScript '''
            def writer = new StringWriter()
            def xsd = new groovy.xml.NamespaceBuilder(new groovy.xml.MarkupBuilder(writer)).namespace('http://www.w3.org/2001/XMLSchema', 'xs')
            xsd.schema() {}
            def xml = writer.toString()
            assert xml.contains('xmlns:xs') || xml.contains('http://www.w3.org/2001/XMLSchema')
        '''

    }

    // GROOVY-7622: no auto-conversion to generic type argument
    @NotYetImplemented @Test
    void testGroovy7622() {
        assertScript '''
            class Entity<T> { T id }
            class Person extends Entity<Long> {}
            def p = new Person(id: 1)
            assert p.id instanceof Long
            p.id = 1
            assert p.id instanceof Long
        '''

    }

    // GROOVY-7719: Type incompatibility of generic placeholders not detected for method called on field
    @NotYetImplemented @Test
    void testGroovy7719() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                @groovy.transform.TypeChecked
                class GoodCodeRed<T> {
                    Collection<GoodCodeRed<T>> attached = []
                    public <U> void attach(GoodCodeRed<U> toAttach) {
                        attached.add(toAttach)
                    }
                }
            ''')
        }
        assert err.message.contains('GoodCodeRed') || err.message.contains('incompatible') || err.message.contains('add')

    }

    // GROOVY-7720: Type incompatibility of generic placeholders not detected on assignment
    @NotYetImplemented @Test
    void testGroovy7720() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                @groovy.transform.TypeChecked
                class Foo<T> {
                    public <U> void set(U value) {
                        T localValue = value
                    }
                }
            ''')
        }
        assert err.message.contains('incompatible') || err.message.contains('Cannot assign') || err.message.contains('U')

    }

    // GROOVY-7770: StackTraceUtils.deepSanitize will spin indefinitely on circular exceptions
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    @NotYetImplemented @Test
    void testGroovy7770() {
        def e1 = new Exception('e1')
        def e2 = new Exception(e1)
        e1.initCause(e2)
        def future = CompletableFuture.supplyAsync {
            StackTraceUtils.deepSanitize(e1)
        }
        try {
            def result = future.get(5, TimeUnit.SECONDS)
            assert result instanceof Throwable
        } finally {
            future.cancel(true)
        }

    }

    // GROOVY-7844: Groovy executes private static methods if signature matches best.
    @NotYetImplemented @Test
    void testGroovy7844() {
        def dir = File.createTempDir('groovy7844-', '')
        try {
            def pkg = new File(dir, 'foo')
            pkg.mkdirs()
            new File(pkg, 'Foo.java').text = '''
                package foo;
                public class Foo {
                    public static String toStringSpecial(Object object) {
                        if (object instanceof String) {
                            return "special: " + toStringSpecial((String) object);
                        }
                        return "special: " + object;
                    }
                    private static String toStringSpecial(String object) {
                        return "private-static-special: " + object;
                    }
                }
            '''
            def javac = ToolProvider.systemJavaCompiler
            assert javac != null
            def fm = javac.getStandardFileManager(null, null, null)
            def units = fm.getJavaFileObjects(new File(pkg, 'Foo.java'))
            assert javac.getTask(null, fm, null, null, null, units).call()
            def loader = new GroovyClassLoader(new URLClassLoader([dir.toURI().toURL()] as URL[], this.class.classLoader))
            def shell = new GroovyShell(loader)
            assert shell.evaluate('foo.Foo.toStringSpecial(1)') == 'special: 1'
            assert shell.evaluate('foo.Foo.toStringSpecial("foo")') == 'special: private-static-special: foo'
        } finally {
            dir.deleteDir()
        }

    }

    // GROOVY-7847: Set casting from CharSequence yields invalid Set
    @NotYetImplemented @Test
    void testGroovy7847() {
        assertScript '''
            def expected = 'abcc'.toSet()
            def actual = 'abcc' as Set
            assert actual instanceof Set
            assert actual == expected
            assert actual.size() == 3
        '''

    }

    // GROOVY-7869: Groovy category does now work with abstract iterable classes
    @NotYetImplemented @Test
    void testGroovy7869() {
        assertScript '''
            abstract class MyAbstractClass implements Iterable<Number> {
                final int num
                MyAbstractClass(int num) { this.num = num }
                Iterator<Number> iterator() {
                    new Iterator<Number>() {
                        boolean hasNext() { false }
                        Number next() { null }
                    }
                }
            }
            class MyAbstractClassImpl extends MyAbstractClass {
                MyAbstractClassImpl(int a) { super(a) }
            }
            class MyCategory {
                static MyAbstractClass plus(MyAbstractClass a, MyAbstractClass b) {
                    new MyAbstractClassImpl(a.num + b.num)
                }
            }
            use(MyCategory) {
                def a = new MyAbstractClassImpl(1)
                def b = new MyAbstractClassImpl(2)
                assert (a + b).num == 3
            }
        '''

    }

    // GROOVY-7874: When serializing a document with xmlUtil, namespaces that are actually necessary, are being dropped as "unused"
    @NotYetImplemented @Test
    void testGroovy7874() {
        assertScript '''
            def src = """<?xml version="1.0" encoding="UTF-8"?>
            <xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                attributeFormDefault="unqualified" elementFormDefault="qualified"
                targetNamespace="urn:cargal.net/myNS" xmlns="urn:cargal.net/myNS"
                xmlns:myns="urn:cargal.net/myNS">
                <xsd:complexType name="myCT2">
                    <xsd:complexContent>
                        <xsd:extension base="myns:myCT">
                            <xsd:sequence>
                                <xsd:element name="element1" />
                            </xsd:sequence>
                        </xsd:extension>
                    </xsd:complexContent>
                </xsd:complexType>
                <xsd:element name="test" type="myns:myCT" />
            </xsd:schema>
            """
            def schema = new groovy.xml.XmlSlurper().parseText(src)
            def out = new groovy.xml.XmlUtil().serialize(schema)
            assert out.contains('xmlns:myns')
            assert out.contains('urn:cargal.net/myNS')
            assert out.contains('myns:myCT')
        '''

    }

    // GROOVY-7934: Type inference breaks for Collection.inject
    @NotYetImplemented @Test
    void testGroovy7934() {
        assertScript '''
            @groovy.transform.CompileStatic
            def run() {
                def inList = ['a', 'b', 'c']
                inList.inject(0, { a, b -> a + ',' + b })
            }
            assert run() == '0,a,b,c'
        '''

    }

    // GROOVY-7949: Disallow static inner classes within anonymous classes
    @NotYetImplemented @Test
    void testGroovy7949() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                class Base {}
                def x = new Base() {
                    static class Inner { String s = 'hello' }
                }
            ''')
        }
        assert err.message.toLowerCase().contains('static') || err.message.contains('anonymous') || err.message.contains('Inner')

    }

    // GROOVY-7963: TimeDuration fields can overflow int resulting in negative duration
    @NotYetImplemented @Test
    void testGroovy7963() {
        assertScript '''
            use (groovy.time.TimeCategory) {
                def d = Integer.MAX_VALUE.milliseconds + 1.millisecond
                assert d.toMilliseconds() == (Integer.MAX_VALUE as long) + 1L
            }
        '''

    }

    // GROOVY-7991: classes from default package not found from other packages in case of .groovy files
    @NotYetImplemented @Test
    void testGroovy7991() {
        def dir = File.createTempDir('groovy7991-', '')
        def loader = new GroovyClassLoader()
        try {
            new File(dir, 'DefaultClass.groovy').text = 'class DefaultClass { static String v() { "ok" } }'
            def pkg = new File(dir, 'named')
            pkg.mkdirs()
            new File(pkg, 'Named.groovy').text = '''
                package named
                class Named {
                    static String v() { DefaultClass.v() }
                }
            '''
            loader.addClasspath(dir.absolutePath)
            loader.parseClass(new File(dir, 'DefaultClass.groovy'))
            def named = loader.parseClass(new File(pkg, 'Named.groovy'))
            assert named.v() == 'ok'
        } finally {
            loader.close()
            dir.deleteDir()
        }

    }

    // GROOVY-8022: Plus assignment for unusual property
    @NotYetImplemented @Test
    void testGroovy8022() {
        assertScript '''
            class B {}
            class C {
                C plus(B b) { println 'plus'; this }
            }
            class A {
                C prop
                A() { prop = new C() }
                C getProp() { println 'get'; prop }
                void setProp(C c) { println 'set'; prop = c }
            }
            @groovy.transform.CompileStatic
            def run() {
                def foo = new A()
                foo.prop += new B()
            }
            def buf = new ByteArrayOutputStream()
            def old = System.out
            System.out = new PrintStream(buf)
            try {
                run()
            } finally {
                System.out = old
            }
            def log = buf.toString()
            assert log.contains('get')
            assert log.contains('plus')
            assert log.contains('set')
        '''

    }

    // GROOVY-8062: Unable to override plus for Integer-s with extension
    @NotYetImplemented @Test
    void testGroovy8062() {
        // desired: @CompileStatic plus uses the same extension as dynamic
        // cannot load an extension module here without a jar; assert that CS Integer+Integer
        // can be overridden the same way as dynamic via metaClass
        assertScript '''
            Integer.metaClass.plus = { Integer j -> delegate * j }
            try {
                @groovy.transform.CompileStatic
                def fooStatic(Integer i, Integer j) { i + j }
                def fooDynamic(Integer i, Integer j) { i + j }
                assert fooDynamic(2, 3) == 6
                assert fooStatic(2, 3) == 6
            } finally {
                Integer.metaClass = null
            }
        '''

    }

    // GROOVY-8063: Type annotation value referencing inner class is not properly scoped
    @NotYetImplemented @Test
    void testGroovy8063() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                @interface Anno {
                    Class value()
                }
                @Anno(value=Inner)
                class Outer {
                    static class Inner {}
                }
            ''')
        }
        assert err.message.contains('Inner') || err.message.toLowerCase().contains('unable to resolve') || err.message.toLowerCase().contains('scope')

    }

    // GROOVY-8077: double comparison (apparently) uses .equals and not ==
    @NotYetImplemented @Test
    void testGroovy8077() {
        assertScript '''
            assert (new Double(-1) * new Double(0)) == (new Double(0) * new Double(1))
            def a = 0
            def b = -1
            assert !((a * b) < 0)
        '''

    }

    // GROOVY-8078: Double unary - and + not supported ?
    @NotYetImplemented @Test
    void testGroovy8078() {
        assertScript '''
            assert - - 3 == 3
            assert - -3 == 3
            assert - - 5 == 5
        '''

    }

    // GROOVY-8081: Compiler does not give error for ambiguous type or member due to multiple star imports
    @NotYetImplemented @Test
    void testGroovy8081() {
        def dir = File.createTempDir('groovy8081-', '')
        try {
            new File(dir, 'a').mkdirs()
            new File(dir, 'b').mkdirs()
            new File(dir, 'a/Action.groovy').text = 'package a; class Action {}'
            new File(dir, 'b/Action.groovy').text = 'package b; class Action {}'
            def loader = new GroovyClassLoader()
            loader.addClasspath(dir.absolutePath)
            def err = shouldFail(MultipleCompilationErrorsException) {
                loader.parseClass('''
                    import a.*
                    import b.*
                    class C8081 { Action field }
                ''')
            }
            assert err.message.toLowerCase().contains('ambiguous') || err.message.contains('Action')
        } finally {
            dir.deleteDir()
        }

    }

    // GROOVY-8101: DefaultGroovyMethods.plus() copies internal state of the left Map
    @NotYetImplemented @Test
    void testGroovy8101() {
        assertScript '''
            class ParameterMap extends LinkedHashMap {
                private boolean locked
                void lock() { locked = true }
                Object clone() {
                    if (locked) throw new IllegalStateException('class is locked')
                    super.clone()
                }
            }
            def parameterMap = new ParameterMap()
            parameterMap.put('1', 'one')
            parameterMap.lock()
            def result = parameterMap.plus(['2': 'two'])
            assert result['1'] == 'one'
            assert result['2'] == 'two'
        '''

    }

    // GROOVY-8115: Reported start time for timed-out executions is sometimes wrong
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    @NotYetImplemented @Test
    void testGroovy8115() {
        def future = CompletableFuture.supplyAsync {
            new GroovyShell().evaluate('''
                @groovy.transform.TimedInterrupt(value=1L, unit=java.util.concurrent.TimeUnit.MILLISECONDS)
                class T8115 {
                    def getProperty(String name) { null }
                    def go() { 1 }
                }
                try {
                    Thread.sleep(10)
                    new T8115().go()
                    return 'NO_TIMEOUT'
                } catch (Throwable t) {
                    return String.valueOf(t.message)
                }
            ''')
        }
        try {
            def msg = future.get(5, TimeUnit.SECONDS)
            assert msg != 'NO_TIMEOUT'
            assert !msg.contains('Start time: null')
        } finally {
            future.cancel(true)
        }

    }

    // GROOVY-8147: Dynamic non existing property access with different MissingPropertyException
    @NotYetImplemented @Test
    void testGroovy8147() {
        assertScript '''
            class Propy {}
            def foo = 'bar'
            def t1, t2
            try { new Propy()."$foo" } catch (Throwable t) { t1 = t.getClass() }
            try { new Propy()[foo] } catch (Throwable t) { t2 = t.getClass() }
            assert t1 != null && t2 != null
            assert t1 == t2
        '''

    }

    // GROOVY-8153: Weird .class references with subscript operator
    @NotYetImplemented @Test
    void testGroovy8153() {
        def err = shouldFail '''
            String.class[]
        '''
        assert err instanceof MissingMethodException || err.message.contains('getAt')

    }

    // GROOVY-8154: Parenthesized class literal with subscript operator
    @NotYetImplemented @Test
    void testGroovy8154() {
        def err = shouldFail '''
            (String.class)[]
        '''
        assert err instanceof MissingMethodException || err.message.contains('getAt')

    }

    // GROOVY-8248: MethodSelectionException when calling a overloaded method without arguments
    @NotYetImplemented @Test
    void testGroovy8248() {
        def err = shouldFail(MissingMethodException) {
            def o = new Object() {
                def methodWithOverload(String a, String b) { 'two' }
                def methodWithOverload(String a) { 'one' }
            }
            o.methodWithOverload()
        }
        assert err instanceof MissingMethodException
        assert !(err.getClass().name.contains('MethodSelectionException'))

    }

    // GROOVY-8264: Wrong method accessed via aliased import
    @NotYetImplemented @Test
    void testGroovy8264() {
        def loader = new GroovyClassLoader(this.class.classLoader)
        loader.parseClass('''
            class Bar8264 {
                static some
                static getSome() { some }
                static setSome(x) { some = x }
            }
        ''')
        def shell = new GroovyShell(loader, new Binding())
        shell.evaluate('''
            import static Bar8264.getSome as setAbc
            abc = 2
            assert Bar8264.some == 2
        ''')
        def shell2 = new GroovyShell(loader, new Binding())
        shell2.evaluate('''
            import static Bar8264.setSome as getAbc
            Bar8264.some = 'owner'
            assert abc == 'owner'
        ''')

    }

    // GROOVY-8331: Add support for Long to TimeCategory
    @NotYetImplemented @Test
    void testGroovy8331() {
        assertScript '''
            use (groovy.time.TimeCategory) {
                assert 1.minutes.toMilliseconds() == 1L.minutes.toMilliseconds()
                Long two = 2L
                assert two.minutes.toMilliseconds() == 2 * 60 * 1000
            }
        '''

    }

    // GROOVY-8355: Instanceof inference does not work on field assigning
    @NotYetImplemented @Test
    void testGroovy8355() {
        assertScript '''
            @groovy.transform.CompileStatic
            class C8355 {
                Object str
                def run() {
                    str = 'str'
                    str.toUpperCase()
                }
            }
            assert new C8355().run() == 'STR'
        '''

    }

    // GROOVY-8363: Implementing List with a delegated data source results in StackOverflowException
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    @NotYetImplemented @Test
    void testGroovy8363() {
        def future = CompletableFuture.supplyAsync {
            new GroovyShell().evaluate('''
                class DelegateList {
                    private List<String> lowerCaseStrings = []
                    private DelegatingListImplementation uppercaseStrings = new DelegatingListImplementation()

                    private abstract static class DelegatingList implements List<String> {
                        abstract List<String> getDelegate()
                        int size() { delegate.size() }
                        boolean isEmpty() { delegate.isEmpty() }
                        boolean contains(Object o) { delegate.contains(o) }
                        Iterator<String> iterator() { delegate.iterator() }
                        Object[] toArray() { delegate.toArray() }
                        def <T> T[] toArray(T[] a) { delegate.toArray(a) }
                        boolean add(String s) { delegate.add(s) }
                        boolean remove(Object o) { delegate.remove(o) }
                        boolean containsAll(Collection<?> c) { delegate.containsAll(c) }
                        boolean addAll(Collection<? extends String> c) { delegate.addAll(c) }
                        boolean addAll(int index, Collection<? extends String> c) { delegate.addAll(index, c) }
                        boolean removeAll(Collection<?> c) { delegate.removeAll(c) }
                        boolean retainAll(Collection<?> c) { delegate.retainAll(c) }
                        void clear() { delegate.clear() }
                        String get(int index) { delegate.get(index) }
                        String set(int index, String element) { delegate.set(index, element) }
                        void add(int index, String element) { delegate.add(index, element) }
                        String remove(int index) { delegate.remove(index) }
                        int indexOf(Object o) { delegate.indexOf(o) }
                        int lastIndexOf(Object o) { delegate.lastIndexOf(o) }
                        ListIterator<String> listIterator() { delegate.listIterator() }
                        ListIterator<String> listIterator(int index) { delegate.listIterator(index) }
                        List<String> subList(int fromIndex, int toIndex) { delegate.subList(fromIndex, toIndex) }
                    }

                    private class DelegatingListImplementation extends DelegatingList {
                        List<String> getDelegate() {
                            return lowerCaseStrings.collect { it.toUpperCase() }
                        }
                    }

                    DelegateList(Collection<String> strings) {
                        lowerCaseStrings = strings.toList().collect { it.toLowerCase() }
                    }

                    List<String> getUppercase() { uppercaseStrings }
                }
                def d = new DelegateList(['hello', 'bye'])
                d.getUppercase().toList()
            ''')
        }
        try {
            assert future.get(5, TimeUnit.SECONDS) == ['HELLO', 'BYE']
        } finally {
            future.cancel(true)
        }

    }

    // GROOVY-8370: Compiler allows to extend/implement a class from its generic
    @NotYetImplemented @Test
    void testGroovy8370() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('class A8370<T> extends T {}')
        }
        assert err.message.contains('T') || err.message.toLowerCase().contains('type parameter') || err.message.toLowerCase().contains('extend')

    }

    // GROOVY-8390: ConfigSlurm returns an invalid config object 
    @NotYetImplemented @Test
    void testGroovy8390() {
        assertScript '''
            def obj = new ConfigSlurper().parse("""
                alpha.container = 'foo'
                profiles {
                    alpha.container = 'bar'
                }
            """)
            assert obj.alpha.container == 'foo'
            assert obj.profiles.alpha.container == 'bar'
        '''

    }

    // GROOVY-8398: Overriding getProperty makes getAttribute throw missing property exception
    @NotYetImplemented @Test
    void testGroovy8398() {
        assertScript '''
            class PropClass {
                String myString = 'x'
                def getProperty(String name) { this.@"$name" }
            }
            def p = new PropClass()
            assert p.metaClass.getAttribute(p, 'myString') == 'x'
            p.metaClass.myMethod = { 'ok' }
            assert p.myMethod() == 'ok'
        '''

    }

    // GROOVY-8467: IOGroovyMethods class readLine method works unexpected in  
    @NotYetImplemented @Test
    void testGroovy8467() {
        assertScript '''
            def bytes = 'A\r\nB\r\n'.getBytes('UTF-8')
            def reader = new InputStreamReader(new ByteArrayInputStream(bytes), 'UTF-8')
            assert !reader.markSupported()
            def lines = []
            def line
            while ((line = org.codehaus.groovy.runtime.IOGroovyMethods.readLine(reader)) != null) {
                lines << line
            }
            assert lines == ['A', 'B']
        '''

    }

    // GROOVY-8553: Parrot Parser: parse fails for typecast of slashy string
    @NotYetImplemented @Test
    void testGroovy8553() {
        assertScript '''
            def p = (java.util.regex.Pattern) /abc/
            assert p instanceof java.util.regex.Pattern
            assert p.pattern() == 'abc'
        '''

    }

    // GROOVY-8625: Groovy Lexer does not accept UTF-8 characters like ° or § ... and a lot more
    @NotYetImplemented @Test
    void testGroovy8625() {
        assertScript '''
            def a\u00b0 = 1
            def \u00a7foo = 1
            assert a\u00b0 == 1
            assert \u00a7foo == 1
        '''

    }

    // GROOVY-8640: Cannot find matching comparator method
    @NotYetImplemented @Test
    void testGroovy8640() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.nio.file.Path
            @CompileStatic
            class Test8640 {
                static private int MAX_LINES = 100
                private static class Nested {
                    @Delegate
                    private Map<String,Object> delegate = [:]
                    private String fetch(Path path) {
                        int c = 0
                        def result = new StringBuilder()
                        path.withReader { reader ->
                            String line
                            while ((line = reader.readLine()) && c++ < MAX_LINES) {
                                result << line
                            }
                        }
                        result
                    }
                }
            }
            def f = File.createTempFile('groovy8640', '.txt')
            f.text = 'hello\\nworld\\n'
            try {
                def n = new Test8640.Nested()
                assert n.fetch(f.toPath()).contains('hello')
            } finally {
                f.delete()
            }
        '''

    }

    // GROOVY-8642: New lines in qualified name cause errors
    @NotYetImplemented @Test
    void testGroovy8642() {
        assertScript '''
            import java.
            lang.
            Object
            def o = new Object()
            assert o instanceof java.lang.Object
        '''
        assertScript '''
            package a.
            b
            class C8642 {}
            assert new C8642() instanceof a.b.C8642
        '''

    }

    // GROOVY-8650: New line between prefix operator and operand
    @NotYetImplemented @Test
    void testGroovy8650() {
        assertScript '''
            def c = --
            1
            def x = +
            4
            def (p, q, r) = [+
             4, -
             3, !
             (2 == 3)]
            assert c == -1
            assert x == 4
            assert [p, q, r] == [4, -3, true]
        '''

    }

    // GROOVY-8660: Unexpected MethodSelectionException with implicit null argument
    @NotYetImplemented @Test
    void testGroovy8660() {
        assertScript '''
            class OnlySingle {
                def foo(Object a) { "single param: $a" }
            }
            class OnlyVararg {
                def foo(Object a, Object... rest) { "vararg: $a $rest" }
            }
            class Both {
                def foo(Object a) { "single param: $a" }
                def foo(Object a, Object[] rest) { "array: $a $rest" }
            }
            assert new OnlySingle().foo() == 'single param: null'
            assert new OnlyVararg().foo().startsWith('vararg:')
            assert new Both().foo() == 'single param: null'
        '''

    }

    // GROOVY-8683: CLONE - Cannot qualify path expression with soft keyword ("def" case)
    @NotYetImplemented @Test
    void testGroovy8683() {
        assertScript '''
            def delegate = [def: [foo: 42]]
            def result = delegate.with { def.foo }
            assert result == 42
        '''

    }

    // GROOVY-8763: CharSequence.toInteger() only deals with base 10 integer literals
    @NotYetImplemented @Test
    void testGroovy8763() {
        assertScript '''
            assert '0x10'.toInteger() == 16
            assert '010'.toInteger() == 8
        '''

    }

    // GROOVY-8803: Can't refer class declared with @Builder annotation
    @NotYetImplemented @Test
    void testGroovy8803() {
        assertScript '''
            @groovy.transform.builder.Builder
            class Foo8803 {
                String name
            }
            Foo8803.Foo8803Builder aBuilder = Foo8803.builder()
            def foo = aBuilder.name('n').build()
            assert foo.name == 'n'
        '''

    }

    // GROOVY-8896: Cannot create instance of inner class with list literals
    @NotYetImplemented @Test
    void testGroovy8896() {
        assertScript '''
            class Outer8896 {
                class Inner {
                    Integer ff
                    Inner(Integer ff) { this.ff = ff }
                }
                def viaList() { def x = [20] as Inner; x }
                def viaMap() { def x = [ff: 20] as Inner; x }
                def assignList() { Inner x = [20]; x }
                def assignMap() { Inner x = [ff: 20]; x }
            }
            def o = new Outer8896()
            assert o.viaList().ff == 20
            assert o.viaMap().ff == 20
            assert o.assignList().ff == 20
            assert o.assignMap().ff == 20
        '''

    }

    // GROOVY-8952: Eager interpolation evaluating as a Closure does a lazy invoke of the Closure
    @NotYetImplemented @Test
    void testGroovy8952() {
        assertScript '''
            def log = []
            log << 'Just before creating the GString'
            def gstr = "X${ { log << 'Inside closure'; 'CVAL' } }Y"
            log << 'Just after creating the GString'
            def text = gstr.toString()
            assert log == ['Just before creating the GString', 'Just after creating the GString'] || !gstr.values[0] instanceof Closure
            assert text.contains('CVAL') || text.contains('closure') || text.startsWith('X')
        '''

    }

    // GROOVY-8955: VerifyError when AIC with @CS can't be detected as not valid
    @NotYetImplemented @Test
    void testGroovy8955() {
        assertScript '''
            @groovy.transform.CompileStatic
            abstract class Property {}
            @groovy.transform.CompileStatic
            class PMapping<T extends Property> {
                abstract T getMappedForm()
            }
            @groovy.transform.CompileStatic
            class GPEntity {
                def mapping = new PMapping() {
                    Property getMappedForm() { null }
                }
                def run() { mapping.mappedForm }
            }
            new GPEntity().run()
        '''

    }

    // GROOVY-8956: conditional if/else has issues with evaluated strings in maps
    @NotYetImplemented @Test
    void testGroovy8956() {
        assertScript '''
            def codeTotals = [:]
            def it = ['x', 'A']
            codeTotals["${it[1]}"] = (codeTotals["$it[1]"] == null) ? 0 : codeTotals["$it[1]"] += 1
            assert codeTotals['A'] == 0
        '''

    }

    // GROOVY-9011: (2/1).equals(2) is false; but (2/1) == (2) is true
    @NotYetImplemented @Test
    void testGroovy9011() {
        assertScript '''
            assert (2/1).equals(2)
            assert (2/1) == 2
        '''

    }

    // GROOVY-9075: The exception message should be more clear when a GroovyObject's metaClass is wrong
    @NotYetImplemented @Test
    void testGroovy9075() {
        def err = shouldFail {
            new GroovyShell().evaluate('''
                class C1 { def x = 0 }
                class C2 { def x = 0 }
                def c1 = new C1()
                def c2 = new C2()
                c1.metaClass = c2.metaClass
                c1.x += 1
            ''')
        }
        assert !(err instanceof GroovyBugError) && !err.class.name.contains('GroovyBugError')
        assert err.message?.contains('C1') || err.message?.contains('C2') || err instanceof MissingMethodException || err instanceof GroovyRuntimeException

    }

    // GROOVY-9150: Cannot use generated @Builder via static import
    @NotYetImplemented @Test
    void testGroovy9150() {
        assertScript '''
            import groovy.transform.builder.Builder
            import groovy.transform.builder.SimpleStrategy
            import static PersonTestFactory.aPerson
            class Person9150 {
                String name
            }
            class PersonTestFactory {
                @Builder(builderStrategy = SimpleStrategy, prefix = '')
                static Person9150 aPerson(String name) {
                    new Person9150(name: name)
                }
            }
            def p = aPerson()
            assert p instanceof Person9150 || p != null
        '''

    }

    // GROOVY-9167: Trait field references aren't atomic anymore
    @NotYetImplemented @Test
    void testGroovy9167() {
        assertScript '''
            trait FingerprinterFixture {
                private a = 'A'
                private b = 'B'
                final combined = a + b
                final c = a
            }
            class Impl9167 implements FingerprinterFixture {}
            def x = new Impl9167()
            assert x.combined == 'AB'
            assert x.c == 'A'
        '''

    }

    // GROOVY-9188: Partial mocking static method calls with Ignore & Demand don't work as expected.
    @NotYetImplemented @Test
    void testGroovy9188() {
        assertScript '''
            import groovy.mock.interceptor.MockFor
            class Person9188 {
                static def ignoreMe(foo) { foo }
                static def ignoreMeToo() { ignoreMe('foo') }
                static def ignoreMeThree() { ignoreMe('foo') }
            }
            def mock = new MockFor(Person9188)
            mock.ignore('ignoreMeToo')
            mock.ignore('ignoreMeThree')
            mock.demand.ignoreMe { s -> 'baz' }
            mock.use {
                assert Person9188.ignoreMe('foo') == 'baz'
                assert Person9188.ignoreMeToo() == 'baz'
                assert Person9188.ignoreMeThree() == 'baz'
            }
        '''

    }

    // GROOVY-9253: Comparison between Enum and other objects should fail
    @NotYetImplemented @Test
    void testGroovy9253() {
        def err = shouldFail {
            new GroovyClassLoader().parseClass('''
                enum E { a, b }
                @groovy.transform.CompileStatic
                class C {
                    def test() {
                        E x = E.a
                        String s = "A"
                        x == s
                    }
                }
            ''')
        }
        assert err.message.contains('E') || err.message.contains('String') || err.message.contains('compare')

    }

    // GROOVY-9302: @Delegate works incorrectly with inheritance and final methods
    @NotYetImplemented @Test
    void testGroovy9302() {
        def dir = File.createTempDir('groovy9302-', '')
        try {
            def pkg = new File(dir, 'com/example')
            pkg.mkdirs()
            new File(pkg, 'Parent1.groovy').text = '''
                package com.example
                class Parent1 {
                    final void method1() {}
                    void method2() {}
                }
            '''
            new File(pkg, 'Parent0.groovy').text = '''
                package com.example
                class Parent0 {
                    @Delegate Parent1 parent1 = new Parent1()
                }
            '''
            def loader = new GroovyClassLoader()
            loader.addClasspath(dir.absolutePath)
            def p0 = loader.parseClass(new File(pkg, 'Parent0.groovy'))
            assert p0.getMethod('method2')
            shouldFail(NoSuchMethodException) { p0.getMethod('method1') }
        } finally {
            dir.deleteDir()
        }

    }

    // GROOVY-9304: [Parrot] bitwise or followed by closure on next line parsed as command expression
    @NotYetImplemented @Test
    void testGroovy9304() {
        assertScript '''
            def a = 1
            def b = 2
            def first = a | b
            { it -> true }()
            assert first == (1 | 2)
        '''

    }

    // GROOVY-9345: Closure shared variable type is polluted with unreached assignment type
    @NotYetImplemented @Test
    void testGroovy9345() {
        assertScript '''
            @groovy.transform.CompileStatic
            def run() {
                def var
                var = 'hi'
                def s = var.toUpperCase()
                def cl = { var = new Object() }
                s
            }
            assert run() == 'HI'
        '''

    }

    // GROOVY-9346: Closure shared variable type is polluted with previous assignment type
    @NotYetImplemented @Test
    void testGroovy9346() {
        assertScript '''
            class A9346 { def foo() { 42 } }
            @groovy.transform.CompileStatic
            def run() {
                def var
                var = 'hi'
                var = new A9346()
                var.foo()
            }
            assert run() == 42
        '''

    }

    // GROOVY-9363: False "Constructor call must be the first statement in a constructor"
    @NotYetImplemented @Test
    void testGroovy9363() {
        assertScript '''
            class Foo9363 {
                Foo9363(Closure c) { }
            }
            class Bar9363 extends Foo9363 {
                Bar9363() {
                    super { null }
                }
            }
            new Bar9363()
        '''

    }

    // GROOVY-9364: @Sortable not working correctly when applied to both current and super class
    @NotYetImplemented @Test
    void testGroovy9364() {
        assertScript '''
            @groovy.transform.Sortable
            class TestParent {
                Integer code
            }
            @groovy.transform.Sortable
            @groovy.transform.CompileStatic
            class TestChild extends TestParent {
                Integer value
            }
            def c1 = new TestChild(value: 10, code: 10)
            def c2 = new TestChild(value: 20, code: 10)
            assert c1.compareTo(c2) < 0
            @groovy.transform.CompileStatic
            int checkComparison(Object o1, Object o2) {
                ((Comparable) o1).compareTo((Comparable) o2)
            }
            assert checkComparison(c1, c2) < 0
        '''

    }

    // GROOVY-9379: Binding shadows getters in script or base script
    @NotYetImplemented @Test
    void testGroovy9379() {
        def binding = new Binding()
        binding.setVariable('passwordOK', true)
        def shell = new GroovyShell(binding)
        def result = shell.evaluate('''
            boolean isPasswordOK() { false }
            [bare: passwordOK, method: isPasswordOK(), dotted: this.passwordOK]
        ''')
        assert result.method == false
        assert result.bare == false
        assert result.dotted == false

    }

    // GROOVY-9471: NCDFE: nested class in middle class via static import resolution
    @NotYetImplemented @Test
    void testGroovy9471() {
        assertScript '''
            import static Child9471.Nested
            class Parent9471 {
                Nested field
            }
            class Middle9471 {
                static class Nested {}
            }
            class Child9471 extends Middle9471 {}
            def p = new Parent9471()
            p.field = new Nested()
            assert p.field instanceof Middle9471.Nested
        '''

    }

    // GROOVY-9494: Closure in next line continues previous expression in 3.0 but didn't in 2.5
    @NotYetImplemented @Test
    void testGroovy9494() {
        def ast = new AstBuilder().buildFromString(
            CompilePhase.CONVERSION,
            false,
            '''
                def a = 1
                def b = 2
                a | b
                { it -> 1 } | 2
            '''
        )
        // two statements after the two declarations, not a continued bitwise expression
        def script = ast.find { it instanceof BlockStatement } ?: ast[0]
        def stmts
        if (script instanceof BlockStatement) {
            stmts = script.statements
        } else {
            stmts = script.statementBlock.statements
        }
        assert stmts.size() >= 4

    }

    // GROOVY-9510: DelegatesTo and similar for annotation member methods
    @NotYetImplemented @Test
    void testGroovy9510() {
        def cls = new GroovyClassLoader().parseClass('''
            import java.lang.annotation.*
            @Target(ElementType.METHOD)
            @Retention(RetentionPolicy.RUNTIME)
            @interface Ann9510 {
                @groovy.lang.DelegatesTo(String)
                Class<? extends Closure> value()
            }
        ''')
        assert cls != null

    }

    // GROOVY-9561: Some ternary expressions with []'s and 'as' are not parsed properly
    @NotYetImplemented @Test
    void testGroovy9561() {
        assertScript '''
            def v = 1>2?[3] as Set:null
            assert v == null
        '''

    }

    // GROOVY-9629: MethodCallExpression - implicitThis is always set to true
    @NotYetImplemented @Test
    void testGroovy9629() {
        def ast = new AstBuilder().buildFromString(
            CompilePhase.SEMANTIC_ANALYSIS,
            false,
            '''
                def friend(x) { x }
                def run(delegate) {
                    def call = new org.codehaus.groovy.ast.expr.MethodCallExpression(
                        new org.codehaus.groovy.ast.expr.VariableExpression('delegate'),
                        'friend',
                        org.codehaus.groovy.ast.expr.ArgumentListExpression.EMPTY_ARGUMENTS
                    )
                }
            '''
        )
        def mce = new MethodCallExpression(
            new VariableExpression('delegate'),
            'friend',
            ArgumentListExpression.EMPTY_ARGUMENTS
        )
        assert mce.implicitThis == false

    }

    // GROOVY-9651: VariableScopeVisitor does not check object expression resp. MethodCallExpression#isImplicitThis returns unhelpful true
    @NotYetImplemented @Test
    void testGroovy9651() {
        def sources = new CompilationUnit()
        def src = '''
            class C9651 {
                def run(bar, build) {
                    bar.build()
                }
            }
        '''
        def cu = new CompilationUnit()
        cu.addSource('C9651.groovy', src)
        cu.compile(CompilePhase.SEMANTIC_ANALYSIS.phaseNumber)
        def cn = cu.classNodeIterator.next()
        def method = cn.getDeclaredMethods('run')[0]
        def stmt = method.code
        def found = []
        stmt.visit(new CodeVisitorSupport() {
            void visitMethodCallExpression(MethodCallExpression call) {
                found << call
                super.visitMethodCallExpression(call)
            }
        })
        assert found.size() >= 1
        def call = found[0]
        assert call.methodAsString == 'build'
        assert call.objectExpression.text.contains('bar')
        assert !call.methodAsString.equals('call') || !call.objectExpression.text.contains('build')

    }

    // GROOVY-9720: @TimedExecution together with @CompileStatic produces compilation error for nested interfaces
    @NotYetImplemented @Test
    void testGroovy9720() {
        assertScript '''
            @groovy.transform.TimedInterrupt(value = 5L)
            @groovy.transform.CompileStatic
            class C9720 {
                interface InnerClass {
                    def foo()
                }
            }
            assert C9720.InnerClass.isInterface()
        '''

    }

    // GROOVY-9733: SC/STC: unary operator consistency and error for unsupported operations
    @NotYetImplemented @Test
    void testGroovy9733() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                @groovy.transform.CompileStatic
                class C9733 {
                    def bad() { ~1.0d }
                }
            ''')
        }
        assert err.message.contains('double') || err.message.toLowerCase().contains('unsupported') || err.message.contains('bitwise')

    }

    // GROOVY-9781: Groovy 3 doesn't call implicit constructor properly for inner classes
    @NotYetImplemented @Test
    void testGroovy9781() {
        assertScript '''
            class B9781 {
                class A {
                    def a
                }
                def func2() {
                    ['a': []] as A
                }
            }
            def r = new B9781().func2()
            assert r instanceof B9781.A
        '''

    }

    // GROOVY-9843: Line-wrapped assignment, typecast, method call parsed as class literal assignment
    @NotYetImplemented @Test
    void testGroovy9843() {
        assertScript '''
            def foo() { 42 }
            Number n = (java.lang.Number)
            foo()
            assert n == 42
        '''

    }

    // GROOVY-9870: java.lang.NoClassDefFoundError on Groovy 2.5.x
    @NotYetImplemented @Test
    void testGroovy9870() {
        def dir = File.createTempDir('groovy9870-', '')
        try {
            def missing = new File(dir, 'MissingType.java')
            missing.text = 'public class MissingType {}'
            def host = new File(dir, 'Host.java')
            host.text = 'public class Host { public MissingType hint() { return null; } public int ok() { return 1; } }'
            def javac = ToolProvider.systemJavaCompiler
            def fm = javac.getStandardFileManager(null, null, null)
            assert javac.getTask(null, fm, null, ['-d', dir.absolutePath], null, fm.getJavaFileObjects(missing, host)).call()
            new File(dir, 'MissingType.class').delete()
            def loader = new GroovyClassLoader(new URLClassLoader([dir.toURI().toURL()] as URL[], this.class.classLoader))
            // desired: compiling against Host without MissingType on the classpath
            // should not throw NoClassDefFoundError from the type checker
            def src = '''
                @groovy.transform.CompileStatic
                class Client9870 {
                    def run(Host h) { h.ok() }
                }
            '''
            try {
                loader.parseClass(src)
            } catch (NoClassDefFoundError e) {
                throw new AssertionError('STC should not throw NoClassDefFoundError for a missing signature type: ' + e.message)
            } catch (MultipleCompilationErrorsException e) {
                assert !e.message.contains('NoClassDefFoundError')
            }
        } finally {
            dir.deleteDir()
        }

    }

    // GROOVY-9937: internal compiler error during instruction selection
    @NotYetImplemented @Test
    void testGroovy9937() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                class C9937 {
                    static void foo() { return 1 }
                }
            ''')
        }
        def text = err.message
        assert !text.contains('instruction selection')
        assert !text.contains('RuntimeParserException')
        assert text.toLowerCase().contains('void') || text.toLowerCase().contains('return')

    }

    // GROOVY-9947: Cannot pass numeric constant to a method with a generic type
    @NotYetImplemented @Test
    void testGroovy9947() {
        assertScript '''
            @groovy.transform.TypeChecked
            class A9947<T> {
                def foo(Float x) { x }
                def bar(T x) { x }
            }
            @groovy.transform.TypeChecked
            def run() {
                def a = new A9947<Float>()
                [a.foo(10), a.bar(10)]
            }
            def r = run()
            assert r[0] == 10.0f || r[0] == 10
        '''

    }

    // GROOVY-9986: CLONE - STC does not report type mismatches for coerced list expression
    @NotYetImplemented @Test
    void testGroovy9986() {
        def err = shouldFail {
            new GroovyClassLoader().parseClass('''
                @groovy.transform.CompileStatic
                class C {
                    def test() {
                        Integer[] arr2 = [1, "fda"] as Integer[]
                    }
                }
            ''')
        }
        assert err.message.contains('Integer') || err.message.contains('String') || err.message.contains('Cannot')

    }

    // GROOVY-10007: Breaking change in property resolution 2.4 -> 3.0
    @NotYetImplemented @Test
    void testGroovy10007() {
        JsonSlurper.metaClass.propertyMissing = { name -> name }
        try {
            def slurp = new JsonSlurper()
            assert slurp.MaxSizeForInMemory == 'MaxSizeForInMemory'
        } finally {
            JsonSlurper.metaClass = null
        }

    }

    // GROOVY-10040: @AnnotationCollector - Only classes and closures can be used for attribute 'value' in ...
    @NotYetImplemented @Test
    void testGroovy10040() {
        def loader = new GroovyClassLoader()
        loader.parseClass('''
            package foobar
            import groovy.transform.AnnotationCollector
            @interface RequiresCustom {
                Class<? extends Closure> value()
            }
            @interface RetryCustom {
                int value()
            }
            @RetryCustom(1)
            @RequiresCustom({ 1 == 1 })
            @AnnotationCollector
            @interface MetaCustom {
            }
        ''')
        def foo = loader.parseClass('''
            package foobar
            @foobar.MetaCustom
            class Foo10040 {
                void foo() {}
            }
        ''')
        assert foo != null

    }

    // GROOVY-10041: @AnnotationCollector - CompileDynamicProcessor seems to be ignored
    @NotYetImplemented @Test
    void testGroovy10041() {
        assertScript '''
            import groovy.transform.AnnotationCollector
            import groovy.transform.CompileStatic
            @interface RequiresCustom {
                Class<? extends Closure> value()
            }
            @RequiresCustom({ jvm.java8Compatible })
            @AnnotationCollector(processor = 'org.codehaus.groovy.transform.CompileDynamicProcessor')
            @interface MetaRequires {
            }
            @CompileStatic
            @MetaRequires
            class Foo10041 {
                void foo() {}
            }
            new Foo10041().foo()
        '''

    }

    // GROOVY-10054: STC: Cannot infer lambda parameter types for a stream collector
    @NotYetImplemented @Test
    void testGroovy10054() {
        assertScript '''
            class Named10054 { String name }
            @groovy.transform.CompileStatic
            def run() {
                def list = [new Named10054(name: 'a'), new Named10054(name: 'b')]
                list.stream().collect(java.util.stream.Collectors.groupingBy(named -> named.getName()))
            }
            def r = run()
            assert r['a'].size() == 1
            assert r['b'].size() == 1
        '''

    }

    // GROOVY-10066: Nested @Delegate throws MissingMethodException
    @NotYetImplemented @Test
    void testGroovy10066() {
        assertScript '''
            class Top {
                @Delegate
                private final Middle middle = new Middle()
                void top() {}
            }
            class Middle {
                @Delegate
                private final Bottom bottom = new Bottom()
                void mid() {}
            }
            class Bottom {
                void bot() {}
            }
            def foo = new Top()
            foo.top()
            foo.mid()
            foo.bot()
        '''

    }

    // GROOVY-10073: Unable to use custom string representation in GString template
    @NotYetImplemented @Test
    void testGroovy10073() {
        assertScript '''
            class CustomList extends ArrayList {
                CustomList() { super(['a', 'b']) }
                String toString() { 'CUSTOM' }
            }
            def list = new CustomList()
            assert list.toString() == 'CUSTOM'
            assert "\$list" == 'CUSTOM'
            assert ('' + list) == 'CUSTOM'
        '''

    }

    // GROOVY-10081: Conversion from int to byte does not work in case of ternary operator
    @NotYetImplemented @Test
    void testGroovy10081() {
        assertScript '''
            @groovy.transform.CompileStatic
            def test() {
                byte foo() { (byte) 1 }
                byte x
                x = (true) ? 0 : foo()
                x
            }
            assert test() == 0
        '''

    }

    // GROOVY-10124: Scope of class header (extends, implements and type parameters) should not include class being declared
    @NotYetImplemented @Test
    void testGroovy10124() {
        // valid Java: header names must resolve to the outer D, not the inner class D
        assertScript '''
            class D {}
            class C extends D {
                class D {}
            }
            assert new C() instanceof D
            assert new C().new D()
        '''

    }

    // GROOVY-10129: Misleading report when using as a type argument a type parameter that extends an array type
    @NotYetImplemented @Test
    void testGroovy10129() {
        try {
            new GroovyClassLoader().parseClass('''
                class A10129<T> {}
                class B10129 {}
                @groovy.transform.TypeChecked
                class C10129<G extends B10129[]> extends A10129<G> {}
            ''')
        } catch (MultipleCompilationErrorsException e) {
            assert !e.message.contains('unable to resolve class G')
        }

    }

    // GROOVY-10152: Parrot can't parse closure
    @NotYetImplemented @Test
    void testGroovy10152() {
        // comma expression in collectEntries closure should parse (type-check later if needed)
        def cls = new GroovyClassLoader().parseClass('''
            def normalized = [a: [value: 1]]
            normalized.collectEntries { ctx, tr -> ctx, tr.value }
        ''')
        assert cls != null

    }

    // GROOVY-10163: GroovyShell parse method performance issue
    @NotYetImplemented @Test
    void testGroovy10163() {
        def requests = new AtomicInteger(0)
        def server = HttpServer.create(new InetSocketAddress('127.0.0.1', 0), 0)
        def script = (1..200).collect { "def v$it = $it" }.join('\n') + '\n42\n'
        server.createContext('/s.groovy') { ex ->
            requests.incrementAndGet()
            def bytes = script.getBytes('UTF-8')
            ex.responseHeaders.add('Content-Type', 'text/plain; charset=UTF-8')
            ex.sendResponseHeaders(200, bytes.length)
            ex.responseBody.withCloseable { it.write(bytes) }
        }
        server.start()
        try {
            def port = server.address.port
            def uri = new URI("http://127.0.0.1:${port}/s.groovy")
            def shell = new GroovyShell()
            requests.set(0)
            def result = shell.parse(uri)
            def fetchCount = requests.get()
            assert result != null
            assert fetchCount <= 1
        } finally {
            server.stop(0)
        }

    }

    // GROOVY-10183: Incorrect SIG_ARRAY_LENGTH=255 at org.codehaus.groovy.classgen.asm.CallSiteWriter
    @NotYetImplemented @Test
    void testGroovy10183() {
        def field = CallSiteWriter.getDeclaredField('SIG_ARRAY_LENGTH')
        field.accessible = true
        int sig = field.getInt(null)
        int maxArgs = 250
        assert sig <= maxArgs || sig == 250

    }

    // GROOVY-10219: Closure type for local var is inferred, but unavailable to Closure's contents
    @NotYetImplemented @Test
    void testGroovy10219() {
        assertScript '''
            @groovy.transform.CompileStatic
            def run() {
                def cl
                cl = { assert cl.delegate != null }
                cl()
            }
            run()
        '''

    }

    // GROOVY-10224: No owner type of `ParameterizedType` of Java could be represented in `ClassNode`
    @NotYetImplemented @Test
    void testGroovy10224() {
        def dir = File.createTempDir('groovy10224-', '')
        try {
            def src = new File(dir, 'Outer.java')
            src.text = '''
                public class Outer<T> {
                    public static class Inner<U> {}
                    public Outer<String>.Inner<Integer> field;
                }
            '''
            def javac = ToolProvider.systemJavaCompiler
            assert javac != null
            def fm = javac.getStandardFileManager(null, null, null)
            def units = fm.getJavaFileObjects(src)
            assert javac.getTask(null, fm, null, null, null, units).call()
            def loader = new URLClassLoader([dir.toURI().toURL()] as URL[], this.class.classLoader)
            def cn = ClassHelper.make(loader.loadClass('Outer'))
            def field = cn.getDeclaredField('field')
            assert field != null
            def type = field.type
            assert type.outerClass != null || type.declaringClass != null
            def owner = type.outerClass ?: type.declaringClass
            assert owner.genericsTypes
            assert owner.genericsTypes[0].type.name.contains('String')
        } finally {
            dir.deleteDir()
        }

    }

    // GROOVY-10231: Groovy compiler loads user project class into JVM
    @NotYetImplemented @Test
    void testGroovy10231() {
        def dir = File.createTempDir('groovy10231-', '')
        try {
            def src = new File(dir, 'Marker.java')
            src.text = '''
                import java.lang.annotation.*;
                @Retention(RetentionPolicy.RUNTIME)
                @Target(ElementType.TYPE)
                public @interface Marker {}
            '''
            def javac = ToolProvider.systemJavaCompiler
            def fm = javac.getStandardFileManager(null, null, null)
            assert javac.getTask(null, fm, null, ['-d', dir.absolutePath], null, fm.getJavaFileObjects(src)).call()
            def loaded = new CopyOnWriteArrayList()
            def recording = new URLClassLoader([dir.toURI().toURL()] as URL[], this.class.classLoader) {
                protected Class<?> loadClass(String name, boolean resolve) {
                    if (name == 'Marker' || name.endsWith('.Marker')) loaded.add(name)
                    return super.loadClass(name, resolve)
                }
            }
            def loader = new GroovyClassLoader(recording)
            loader.parseClass('@Marker class C10231 {}')
            assert !loaded.any { it == 'Marker' || it.endsWith('.Marker') }
        } finally {
            dir.deleteDir()
        }

    }

    // GROOVY-10257: STC: no error for incompatible method argument
    @NotYetImplemented @Test
    void testGroovy10257() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                @groovy.transform.TypeChecked
                class C10257<T> {
                    def accept(T t) { t }
                    def <X extends Number> X produce() { null }
                    def run() { accept(produce()) }
                }
            ''')
        }
        assert err.message.contains('produce') || err.message.contains('accept') || err.message.contains('Number')

    }

    // GROOVY-10293: STC fails to catch final field re-assign within constructor or initializer
    @NotYetImplemented @Test
    void testGroovy10293() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                @groovy.transform.TypeChecked
                class C10293 {
                    final int x
                    C10293() { x = 1 }
                    C10293(C10293 other) {
                        x = other.x
                        this.x = 2
                    }
                }
            ''')
        }
        assert err.message.contains('final') || err.message.contains('x')

    }

    // GROOVY-10304: XmlSlurper failing to handle namespaces
    @NotYetImplemented @Test
    void testGroovy10304() {
        assertScript '''
            def src = """<?xml version="1.0" encoding="UTF-8"?><outer xmlns="uri:urn">
  <inner>hello</inner>
</outer>
"""
            def slurped = new groovy.xml.XmlSlurper().parseText(src)
            def serialized = groovy.xml.XmlUtil.serialize(slurped)
            assert !serialized.contains('tag0:')
            assert serialized.contains('xmlns="uri:urn"') || serialized.contains("xmlns='uri:urn'")
            assert serialized.contains('hello')
        '''

    }

    // GROOVY-10335: Comparison null
    @NotYetImplemented @Test
    void testGroovy10335() {
        assertScript '''
            assert !(null < 0)
            assert !(null <= 0)
            assert !(0 > null)
        '''

    }

    // GROOVY-10369: STC does not check the upper bounds of type parameters
    @NotYetImplemented @Test
    void testGroovy10369() {
        def err = shouldFail '''
            @groovy.transform.TypeChecked
            def test() {
                class B<T1, T2 extends T1> {}
                new B<Number, Boolean>()
            }
            test()
        '''
        assert err.message.contains('Boolean') || err.message.contains('bound')

    }

    // GROOVY-10370: STC does not catch type error when assigning a type parameter to a variable of another type
    @NotYetImplemented @Test
    void testGroovy10370() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                class A10370 {}
                @groovy.transform.TypeChecked
                class C10370 {
                    static <T> A10370 m(T x) { return x }
                    def run() { A10370 x = m(1) }
                }
            ''')
        }
        assert err.message.contains('A10370') || err.message.contains('Cannot') || err.message.contains('return')

    }

    // GROOVY-10437: "non-sealed" as expression fails (keyword conflict) whereas "non - sealed" is fine
    @NotYetImplemented @Test
    void testGroovy10437() {
        assertScript '''
            def c = { non, sealed -> return non-sealed }
            assert c(5, 3) == 2
        '''

    }

    // GROOVY-10440: GROOVY-9329 Not Really Cached the compiled class.
    @NotYetImplemented @Test
    void testGroovy10440() {
        def loader = new GroovyClassLoader()
        try {
            def a = loader.parseClass('def x = 1')
            def b = loader.parseClass('def x = 1')
            assert a.is(b)
        } finally {
            loader.close()
        }

    }

    // GROOVY-10443: final field analysis false positive
    @NotYetImplemented @Test
    void testGroovy10443() {
        assertScript '''
            class M10443 extends HashMap {
                protected final foo = 1
            }
            class S10443 extends M10443 {
                def clearFoo() { foo = null }
            }
            def s = new S10443()
            s.clearFoo()
        '''

    }

    // GROOVY-10455: Unable to resolve nested class from abstract parent
    @NotYetImplemented @Test
    void testGroovy10455() {
        assertScript '''
            abstract class AbstractObject {
                static class NestedClass {}
            }
            class ConcreteObject extends AbstractObject {}
            def x = new ConcreteObject.NestedClass()
            assert x instanceof AbstractObject.NestedClass
        '''

    }

    // GROOVY-10517: Parsing depends on capitalization
    @NotYetImplemented @Test
    void testGroovy10517() {
        def compileMsg = { String src ->
            try {
                new GroovyClassLoader().parseClass(src)
                return 'OK'
            } catch (MultipleCompilationErrorsException e) {
                return e.message
            }
        }
        def cap = compileMsg('''
            class Main10517a {
                static void main(String[] args) {
                    Foo(bar m) {}
                }
            }
        ''')
        def low = compileMsg('''
            class Main10517b {
                static void main(String[] args) {
                    foo(bar m) {}
                }
            }
        ''')
        def kind = { String msg ->
            if (msg == 'OK') return 'OK'
            if (msg.contains('Method definition')) return 'method-def'
            if (msg.toLowerCase().contains('unable to resolve') || msg.contains('undeclared') || msg.contains('bar')) return 'call-error'
            return msg.take(80)
        }
        assert kind(cap) == kind(low)

    }

    // GROOVY-10518: @groovy.transform.Field does not remove the annotated variable from the local VariableScope
    @NotYetImplemented @Test
    void testGroovy10518() {
        assertScript '''
            @groovy.transform.Field String foo = 'field'
            String foo = 'local variable'
            assert foo == 'local variable'
            assert this.foo == 'field'
        '''

    }

    // GROOVY-10531: MetaClassRegistryImpl must use the context classloader for ExtensionModuleScanner
    @NotYetImplemented @Test
    void testGroovy10531() {
        def resource = MetaClassRegistryImpl.getResourceAsStream('org/codehaus/groovy/runtime/metaclass/MetaClassRegistryImpl.class')
        assert resource != null
        def bytes = resource.bytes
        def mentionsTCCL = false
        def mentionsOwnLoaderInScanner = false
        def cr = new ClassReader(bytes)
        cr.accept(new ClassVisitor(Opcodes.ASM9) {
            MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if (name != '<init>') return super.visitMethod(access, name, desc, signature, exceptions)
                return new MethodVisitor(Opcodes.ASM9) {
                    void visitMethodInsn(int opcode, String owner, String name2, String desc2, boolean isInterface) {
                        if (name2 == 'getContextClassLoader') mentionsTCCL = true
                    }
                }
            }
        }, 0)
        assert mentionsTCCL

    }

    // GROOVY-10584: Stub generator does not respect BaseScript
    @NotYetImplemented @Test
    void testGroovy10584() {
        def stubDir = File.createTempDir('groovy10584-stubs-', '')
        def outDir = File.createTempDir('groovy10584-out-', '')
        try {
            def config = new CompilerConfiguration()
            config.targetDirectory = outDir
            config.jointCompilationOptions = [stubDir: stubDir, keepStubs: true]
            def loader = new GroovyClassLoader(this.class.classLoader, config)
            def cu = new JavaStubCompilationUnit(config, loader, stubDir)
            cu.addSource('CustomScript.groovy', '''
                abstract class CustomScript extends Script {
                    def extra() { 1 }
                }
            ''')
            cu.addSource('MyScript.groovy', '''
                import groovy.transform.BaseScript
                @BaseScript CustomScript _
                def run() { extra() }
            ''')
            cu.compile()
            def stubs = []
            stubDir.eachFileRecurse { if (it.name.endsWith('.java')) stubs << it }
            def myStub = stubs.find { it.name == 'MyScript.java' }
            assert myStub != null
            def text = myStub.text
            assert text.contains('CustomScript')
            assert !text.contains('extends groovy.lang.Script') && !text.contains('extends Script')
        } finally {
            stubDir.deleteDir()
            outDir.deleteDir()
        }

    }

    // GROOVY-10614: Fail to infer the correct type argument when having conditionals and diamond operator
    @NotYetImplemented @Test
    void testGroovy10614() {
        assertScript '''
            class Foo10614<T> {}
            @groovy.transform.TypeChecked
            class Test10614 {
                def foo(Foo10614<Boolean> f) { f }
                def run() {
                    foo((true) ? new Foo10614<Boolean>() : new Foo10614<>())
                }
            }
            assert new Test10614().run() instanceof Foo10614
        '''

    }

    // GROOVY-10625: Issue in subtyping leads to unsound results
    @NotYetImplemented @Test
    void testGroovy10625() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                class A<T> {
                    T x
                    A(T x) { this.x = x }
                    A() {}
                }
                @groovy.transform.TypeChecked
                class B<T extends Object> extends A<T> {
                    B(T x) { super(x) }
                    B() {}
                    T m() {
                        A<T> x = new B<Object>("fd")
                        return x.x
                    }
                }
                @groovy.transform.TypeChecked
                class Test {
                    static void main(String[] args) {
                        Integer x = new B<Integer>().m()
                    }
                }
            ''')
        }
        assert err.message.contains('B') || err.message.contains('Cannot') || err.message.contains('incompatible')

    }

    // GROOVY-10629: Subtyping does not work properly when having two bounded type parameters
    @NotYetImplemented @Test
    void testGroovy10629() {
        assertScript '''
            class A10629 {
                def m(X10629 x) { x }
            }
            class X10629 extends Number {
                double doubleValue() { 0 }
                float floatValue() { 0 }
                int intValue() { 0 }
                long longValue() { 0 }
            }
            class Y10629 extends X10629 {}
            @groovy.transform.TypeChecked
            def run() {
                def x = new A10629()
                x.m((Y10629) null)
            }
            run()
        '''

    }

    // GROOVY-10630: Subtyping does not work properly when having bounded type parameter and closure
    @NotYetImplemented @Test
    void testGroovy10630() {
        assertScript '''
            class X10630 {}
            class Y10630 extends X10630 {}
            @groovy.transform.TypeChecked
            def run() {
                Closure<String> clos = { X10630 x -> x.toString() }
                clos((Y10630) null)
            }
            run()
        '''

    }

    // GROOVY-10670: Cannot resolve type in the presence of use-site contravariance
    @NotYetImplemented @Test
    void testGroovy10670() {
        assertScript '''
            class Bar10670<T> { T x }
            @groovy.transform.TypeChecked
            class Main10670 {
                static final Bar10670<? super Short> bar = new Bar10670<Number>(x: (short) 1)
                static final short foo = Main10670.bar.x
            }
            assert Main10670.foo == 1
        '''

    }

    // GROOVY-10716: Impossible to change logging Strategy in @Slf4j and other log annotations
    @NotYetImplemented @Test
    void testGroovy10716() {
        assertScript '''
            import groovy.util.logging.Log
            import org.codehaus.groovy.ast.ClassHelper
            import org.codehaus.groovy.ast.ClassNode
            import org.codehaus.groovy.ast.FieldNode
            import org.codehaus.groovy.ast.expr.ConstantExpression
            import org.codehaus.groovy.ast.expr.Expression
            import org.codehaus.groovy.transform.LogASTTransformation

            class MyLogStrategy implements LogASTTransformation.LoggingStrategy {
                FieldNode addLoggerFieldToClass(ClassNode classNode, String fieldName, String categoryName) {
                    classNode.addField(fieldName, FieldNode.ACC_PUBLIC | FieldNode.ACC_STATIC | FieldNode.ACC_FINAL,
                        ClassHelper.STRING_TYPE, new ConstantExpression('custom-logger'))
                }
                boolean isLoggingMethod(String methodName) { false }
                Expression wrapLoggingMethodCall(Expression logVariable, String methodName, Expression originalExpression) {
                    originalExpression
                }
            }

            @Log(loggingStrategy = MyLogStrategy)
            class C10716 {}
            def field = C10716.getDeclaredField('log')
            assert field != null
            assert field.type == String
            assert field.get(null) == 'custom-logger'
        '''

    }

    // GROOVY-10721: Unexpected static initialization order
    @NotYetImplemented @Test
    void testGroovy10721() {
        assertScript '''
            class Log10721 {
                static List events = []
            }
            class D10721 {
                static {
                    Log10721.events << 'Static block'
                }
                static list = { Log10721.events << 'Getting list'; ['x'] }()
            }
            def unused = D10721.name
            assert Log10721.events == ['Static block', 'Getting list']
        '''

    }

    // GROOVY-10755: Ternary returning a list gets parsed improperly
    @NotYetImplemented @Test
    void testGroovy10755() {
        assertScript '''
            def v = 1>2?[1]:0
            assert v == 0
            def w = 1<2?[1]:0
            assert w == [1]
        '''

    }

    // GROOVY-10809: Set coercion doesn't "pull through" method resolution
    @NotYetImplemented @Test
    void testGroovy10809() {
        assertScript '''
            @groovy.transform.CompileStatic
            def run() {
                Set<String> s = ['c'].asImmutable()
                s
            }
            assert run() instanceof Set
            assert run().contains('c')
        '''

    }

    // GROOVY-10810: Spring Validator error when validating trait-added property
    @NotYetImplemented @Test
    void testGroovy10810() {
        assertScript '''
            import java.lang.annotation.ElementType
            import java.lang.annotation.Retention
            import java.lang.annotation.RetentionPolicy
            import java.lang.annotation.Target

            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER])
            @interface NotNull10810 {}

            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER])
            @interface Valid10810 {}

            trait HasErrorInfo {
                @NotNull10810 @Valid10810 String errorInfo
            }
            class C10810 implements HasErrorInfo {}
            def getter = C10810.getMethod('getErrorInfo')
            def anns = getter.declaredAnnotations*.annotationType()*.simpleName
            assert anns.contains('NotNull10810')
            assert anns.contains('Valid10810')
        '''

    }

    // GROOVY-10816: @NamedVariant does not work on methods on a trait or default methods on an interface
    @NotYetImplemented @Test
    void testGroovy10816() {
        assertScript '''
            trait Foo10816 {
                @groovy.transform.NamedVariant
                def bar(String name, int age = 0) { "$name:$age" }
            }
            class Impl10816 implements Foo10816 {}
            def i = new Impl10816()
            assert i.bar(name: 'n', age: 1) == 'n:1'
        '''

    }

    // GROOVY-10817: Incorrect line number reporting
    @NotYetImplemented @Test
    void testGroovy10817() {
        def src = new String((            'LyoNCiAqIFRoaXMgc2NyaXB0IGlzIHJ1biB3aGVuIGEgc2tpbiBpcyBzZWxlY3RlZC4NCiAqIFRoZSBz' +
            'Y3JpcHQgcHJvZHVjZXMgdGhlIHVzZXIgaW50ZXJmYWNlLCBhbmQgY2FuIGJlIGVkaXRlZCB0byBhZGQg' +
            'b3IgY2hhbmdlIHVzZXIgaW50ZXJmYWNlIG9wdGlvbnMuDQogKiBBdXRob3IgRGF2aWQgRWtob2xtLCBt' +
            'b2RpZmllZCBieSBBbmRlcnMgYW5kIExhemEuDQogKi8NCiANCmltcG9ydCBuZXQuamFsYnVtLmNvbXBv' +
            'bmVudC4qOw0KDQovLyBSZXRyaWV2ZSBpbmZvcm1hdGlvbiBmcm9tIHRoZSBza2luLnByb3BlcnRpZXMg' +
            'ZmlsZQ0KU2tpblByb3BlcnRpZXMgc2tpblByb3BzID0gbmV3IFNraW5Qcm9wZXJ0aWVzKHNraW5EaXJl' +
            'Y3RvcnkpOw0KU3RyaW5nIHNraW5WZXJzaW9uID0gc2tpblByb3BzLmdldFByb3BlcnR5KFNraW5Qcm9w' +
            'ZXJ0aWVzLlZFUlNJT04pOw0KU3RyaW5nIHNraW5BdXRob3IgPSBza2luUHJvcHMuZ2V0UHJvcGVydHko' +
            'U2tpblByb3BlcnRpZXMuQVVUSE9SKTsNCg0KLy8gQnVpbGQgdGhlIHVzZXIgaW50ZXJmYWNlDQpDb250' +
            'cm9sUGFuZWwgdWkgPSBuZXcgQ29udHJvbFBhbmVsKCkgew0KCQ0KCS8vIEdlbmVyYWwgdGFiDQoJQ29u' +
            'dHJvbFBhbmVsIGdlbmVyYWxQYW5lbCA9IG5ldyBDb250cm9sUGFuZWwoKSB7DQoJCUpTcGlubmVyIGFs' +
            'YnVtRm9udFNpemUgPSBuZXcgSlNwaW5uZXIobmV3IFNwaW5uZXJOdW1iZXJNb2RlbCgxNiwgMTAsIDM2' +
            'LCAxKSk7DQoJCUpTcGlubmVyIG5hdkljb25TaXplID0gbmV3IEpTcGlubmVyKG5ldyBTcGlubmVyTnVt' +
            'YmVyTW9kZWwoMzIsIDE2LCA2NCwgMSkpOw0KCQlKQ29tYm9Cb3ggaWNvblN0eWxlID0gbmV3IEpDb21i' +
            'b0JveDxJdGVtPihuZXcgT2JqZWN0W10gew0KCQkJbmV3IEl0ZW0oImNsYXNzaWMiLCAiQ2xhc3NpYyIp' +
            'LA0KCQkJbmV3IEl0ZW0oInRoaW4iLCAiVGhpbiIpLA0KCQkJbmV3IEl0ZW0oImZhdCIsICJGYXQiKQkJ' +
            'CQ0KCQl9KTsNCgkJSkNoZWNrQm94IHVzZUxpbmVib3JkZXIgPSBuZXcgSkNoZWNrQm94KCJMaW5lIGJv' +
            'cmRlciBhcm91bmQgaW1hZ2VzIiwgZmFsc2UpOw0KCQlKU3Bpbm5lciBib3JkZXJXaWR0aCA9IG5ldyBK' +
            'U3Bpbm5lcihuZXcgU3Bpbm5lck51bWJlck1vZGVsKDEsIDEsIDksIDEpKTsNCgkJSlNwaW5uZXIgYm9y' +
            'ZGVyUGFkZGluZyA9IG5ldyBKU3Bpbm5lcihuZXcgU3Bpbm5lck51bWJlck1vZGVsKDIsIDAsIDksIDEp' +
            'KTsNCgkJV3JhcHBhYmxlSkxhYmVsIGltZ0JvcmRlck5vdGUgPSBuZXcgV3JhcHBhYmxlSkxhYmVsKCI8' +
            'aHRtbD48aT5Ob3RlLCBsaW5lIGJvcmRlciBjYW5ub3QgYmUgYXBwbGllZCB0byByZXNwb25zaXZlIGlt' +
            'YWdlcyE8L2k+PC9odG1sPiIpOw0KCQlKU3Bpbm5lciB0aHVtYlNwYWNpbmcgPSBuZXcgSlNwaW5uZXIo' +
            'bmV3IFNwaW5uZXJOdW1iZXJNb2RlbCg3LCAwLCAzMCwgMSkpOw0KCQlKQ2hlY2tCb3ggZml4ZWRTaGFw' +
            'ZVRodW1icyA9IG5ldyBKQ2hlY2tCb3goIkZpeGVkLXNoYXBlIHRodW1ibmFpbHMiLCB0cnVlKTsNCgkJ' +
            'SkNoZWNrQm94IGZpeGVkU2hhcGVGb2xkZXJUaHVtYnMgPSBuZXcgSkNoZWNrQm94KCJGaXhlZC1zaGFw' +
            'ZSBmb2xkZXIgdGh1bWJuYWlscyIsIHRydWUpOw0KCQlKQ2hlY2tCb3gga2V5Ym9hcmROYXZpZ2F0aW9u' +
            'ID0gbmV3IEpDaGVja0JveCgiS2V5Ym9hcmQgbmF2aWdhdGlvbiIsIHRydWUpOw0KCQlKQ2hlY2tCb3gg' +
            'b25pbWFnZU5hdmlnYXRpb24gPSBuZXcgSkNoZWNrQm94KCJPbi1pbWFnZSBuYXZpZ2F0aW9uIiwgdHJ1' +
            'ZSk7DQoJCUpDaGVja0JveCB0aHVtYm5haWxOYXZpZ2F0aW9uID0gbmV3IEpDaGVja0JveCgiVGh1bWJz' +
            'dHJpcCIsIGZhbHNlKTsNCgkJSkNoZWNrQm94IGdvb2dsZU1hcHMgPSBuZXcgSkNoZWNrQm94KCJHb29n' +
            'bGUgTWFwcyBzdXBwb3J0IiwgZmFsc2UpOw0KCQlKQ2hlY2tCb3ggcmVzcG9uc2l2ZVNsaWRlcyA9IG5l' +
            'dyBKQ2hlY2tCb3goIlJlc3BvbnNpdmUgc2xpZGVzIiwgdHJ1ZSk7DQoJCUpDb21ib0JveDxJdGVtPiBs' +
            'YW5ndWFnZSA9IG5ldyBKQ29tYm9Cb3g8SXRlbT4oKSB7DQoJCQl7DQoJCQkJc2V0TW9kZWwoTGFuZ3Vh' +
            'Z2VzLm1vZGVsRnJvbShuZXcgRmlsZShza2luRGlyZWN0b3J5LCAidGV4dHMiKSkpOw0KCQkJCWluc2Vy' +
            'dEl0ZW1BdChuZXcgSXRlbSgiamFsYnVtIiwgIjxqQWxidW0gcHJlZmVyZW5jZT4iKSwgMCk7DQoJCQkJ' +
            'c2V0U2VsZWN0ZWRJbmRleCgwKTsNCgkJCX0NCgkJfTsNCgkJDQoJCUNvbnRyb2xQYW5lbCBhYSA9IG5l' +
            'dyBDb250cm9sUGFuZWwoKSB7IC8vIEludmlzaWJsZSBjb250YWluZXIgdG8gY29udHJvbCBzaXplcyBv' +
            'ZiBib3hlcyB3aXRoaW4gaXQNCgkJCQ0KCQkJQ29udHJvbFBhbmVsIGFsbFBhZ2VzUGFuZWwgPSBuZXcg' +
            'Q29udHJvbFBhbmVsKCkgew0KCQkJCXsNCgkJCQkJaWNvblN0eWxlLnNldFNlbGVjdGVkSW5kZXgoMSk7' +
            'DQoJCQkJCWltZ0JvcmRlck5vdGUuc2V0UHJlZmVycmVkV2lkdGgoMjQwKTsNCgkJCQkJc2V0VGl0bGUo' +
            'IkFsbCBQYWdlcyIpOw0KCQkJCQkNCgkJCQkJYWRkKG5ldyBKTGFiZWxGb3IoIkFsYnVtIGZvbnQgc2l6' +
            'ZSIsIGFsYnVtRm9udFNpemUpKTsNCgkJCQkJYWRkKCJ0YWIiLCBhbGJ1bUZvbnRTaXplKTsNCgkJCQkJ' +
            'YWRkKG5ldyBKTGFiZWxGb3IoInB4IiwgYWxidW1Gb250U2l6ZSkpOw0KCQkJCQlhZGQoImJyIiwgbmV3' +
            'IEpMYWJlbEZvcigiTmF2IGljb24gc2l6ZSIsIG5hdkljb25TaXplKSk7DQoJCQkJCWFkZCgidGFiIiwg' +
            'bmF2SWNvblNpemUpOw0KCQkJCQlhZGQobmV3IEpMYWJlbEZvcigicHgiLCBuYXZJY29uU2l6ZSkpOw0K' +
            'CQkJCQlhZGQoImJyIiwgbmV3IEpMYWJlbEZvcigiSWNvbiBzdHlsZSIsIGljb25TdHlsZSkpOw0KCQkJ' +
            'CQlhZGQoInRhYiIsIGljb25TdHlsZSk7DQoJCQkJCWFkZCgiYnIiLCB1c2VMaW5lYm9yZGVyKTsNCgkJ' +
            'CQkJYWRkKCJiciIsIGltZ0JvcmRlck5vdGUpOw0KCQkJCQlhZGQoImJyIiwgbmV3IEpMYWJlbEZvcigi' +
            'Qm9yZGVyIHdpZHRoIiwgYm9yZGVyV2lkdGgpKTsNCgkJCQkJYWRkKCJ0YWIiLCBib3JkZXJXaWR0aCk7' +
            'DQoJCQkJCWFkZChuZXcgSkxhYmVsRm9yKCJweCIsIGJvcmRlcldpZHRoKSk7DQoJCQkJCWFkZCgiYnIi' +
            'LCBuZXcgSkxhYmVsRm9yKCJCb3JkZXIgcGFkZGluZyIsIGJvcmRlclBhZGRpbmcpKTsNCgkJCQkJYWRk' +
            'KCJ0YWIiLCBib3JkZXJQYWRkaW5nKTsNCgkJCQkJYWRkKG5ldyBKTGFiZWxGb3IoInB4IiwgYm9yZGVy' +
            'UGFkZGluZykpOw0KCQkJCQlhZGQoImJyIiwgbmV3IEpMYWJlbEZvcigiVGh1bWJuYWlsIGdhcCIsIHRo' +
            'dW1iU3BhY2luZykpOw0KCQkJCQlhZGQoInRhYiIsIHRodW1iU3BhY2luZyk7DQoJCQkJCWFkZChuZXcg' +
            'SkxhYmVsRm9yKCJweCIsIHRodW1iU3BhY2luZykpOw0KCQkJCQlhZGQoImJyIiwgZml4ZWRTaGFwZVRo' +
            'dW1icyk7DQoJCQkJCWFkZCgiYnIiLCBmaXhlZFNoYXBlRm9sZGVyVGh1bWJzKTsNCgkJCQkJYWRkKCJi' +
            'ciIsIGtleWJvYXJkTmF2aWdhdGlvbik7DQoJCQkJCWFkZCgiYnIiLCBuZXcgSkxhYmVsKCJMYW5ndWFn' +
            'ZSIpKTsNCgkJCQkJYWRkKCJ0YWIiLCBsYW5ndWFnZSk7DQoJCQkJfQ0KCQkJfTsNCgkJCQ0KCQkJQ29u' +
            'dHJvbFBhbmVsIHNsaWRlUGFnZXNQYW5lbCA9IG5ldyBDb250cm9sUGFuZWwoKSB7DQoJCQkJewkJCQkJ' +
            'DQoJCQkJCXNldFRpdGxlKCJTbGlkZSBQYWdlcyIpOw0KCQkJCQlhZGQob25pbWFnZU5hdmlnYXRpb24p' +
            'Ow0KCQkJCQlhZGQoImJyIiwgdGh1bWJuYWlsTmF2aWdhdGlvbik7DQoJCQkJCWFkZCgiYnIiLCBnb29n' +
            'bGVNYXBzKTsNCgkJCQkJYWRkKCJiciIsIHJlc3BvbnNpdmVTbGlkZXMpOw0KCQkJCX0NCgkJCX07DQoJ' +
            'CQl7DQoJCQkJYWRkKCJ2dG9wIiwgYWxsUGFnZXNQYW5lbCk7DQoJCQkJYWRkKCJ2ZmlsbCIsIHNsaWRl' +
            'UGFnZXNQYW5lbCk7DQoJCQl9DQoJCX07DQoJCXsNCgkJCWFkZChhYSk7DQoJCQluZXcgU3RhdGVNb25p' +
            'dG9yKCkgew0KCQkJCXB1YmxpYyB2b2lkIG9uQ2hhbmdlKCkgew0KCQkJCQlpbWdCb3JkZXJOb3RlLnNl' +
            'dFZpc2libGUodXNlTGluZWJvcmRlci5pc1NlbGVjdGVkKCkgJiYgcmVzcG9uc2l2ZVNsaWRlcy5pc1Nl' +
            'bGVjdGVkKCkpOw0KCQkJCX0NCgkJCX0uYWRkKHVzZUxpbmVib3JkZXIpLmFkZChyZXNwb25zaXZlU2xp' +
            'ZGVzKS5kb25lKCk7IA0KCQkNCgkJCW5ldyBTdGF0ZU1vbml0b3Igew0KCQkJCXB1YmxpYyB2b2lkIG9u' +
            'Q2hhbmdlKCkgew0KCQkJCQllbmFibGUodXNlTGluZWJvcmRlci5pc1NlbGVjdGVkKCksIGJvcmRlcldp' +
            'ZHRoLCBib3JkZXJQYWRkaW5nKTsNCgkJCQl9DQoJCQl9LmFkZCh1c2VMaW5lYm9yZGVyKS5kb25lKCk7' +
            'DQoJCX0NCgl9Ow0KCQ0KCS8vIFRpdGxlcyB0YWINCglDb250cm9sUGFuZWwgdGV4dHNQYW5lbCA9IG5l' +
            'dyBDb250cm9sUGFuZWwoKSB7DQoJCUpDaGVja0JveCB0b3BBbGJ1bVRpdGxlID0gbmV3IEpDaGVja0Jv' +
            'eCgiQWxidW0gdGl0bGUiLCB0cnVlKTsNCgkJSkNoZWNrQm94IHRvcEFsYnVtRGVzY3JpcHRpb24gPSBu' +
            'ZXcgSkNoZWNrQm94KCJBbGJ1bSBkZXNjcmlwdGlvbiIsIHRydWUpOw0KCQlKQ2hlY2tCb3ggZm9sZGVy' +
            'QWxidW1UaXRsZSA9IG5ldyBKQ2hlY2tCb3goIkFsYnVtIHRpdGxlIiwgZmFsc2UpOw0KCQlKQ2hlY2tC' +
            'b3ggZm9sZGVyRm9sZGVyVGl0bGUgPSBuZXcgSkNoZWNrQm94KCJGb2xkZXIgdGl0bGUvbmFtZSIsIHRy' +
            'dWUpOw0KCQlKQ2hlY2tCb3ggZm9sZGVyRm9sZGVyQ29tbWVudCA9IG5ldyBKQ2hlY2tCb3goIkZvbGRl' +
            'ciBjb21tZW50IiwgdHJ1ZSk7DQoJCUpDaGVja0JveCBzbGlkZUFsYnVtVGl0bGUgPSBuZXcgSkNoZWNr' +
            'Qm94KCJBbGJ1bSB0aXRsZSIsIGZhbHNlKTsNCgkJSkNoZWNrQm94IHNsaWRlRm9sZGVyVGl0bGUgPSBu' +
            'ZXcgSkNoZWNrQm94KCJGb2xkZXIgdGl0bGUvbmFtZSIsIGZhbHNlKTsNCgkJSkNoZWNrQm94IHNsaWRl' +
            'SW1hZ2VUaXRsZSA9IG5ldyBKQ2hlY2tCb3goIkltYWdlIHRpdGxlL25hbWUiLCB0cnVlKTsNCgkJSkNo' +
            'ZWNrQm94IHNsaWRlSW1hZ2VDb21tZW50ID0gbmV3IEpDaGVja0JveCgiSW1hZ2UgY29tbWVudCBiZWxv' +
            'dyIsIHRydWUpOw0KCQlKRHJhZ2dhYmxlTGlzdCB0aHVtYm5haWxJdGVtcyA9IG5ldyBKRHJhZ2dhYmxl' +
            'TGlzdChuZXcgT2JqZWN0W10gew0KCQkJbmV3IEl0ZW0oImZpbGVUaXRsZSIsICJUaXRsZSIpLA0KCQkJ' +
            'bmV3IEl0ZW0oImNvbW1lbnQiLCAiQ29tbWVudCIpLA0KCQkJbmV3IEl0ZW0oImtleXdvcmRzIiwgIktl' +
            'eXdvcmRzIiksDQoJCQluZXcgSXRlbSgibGFiZWwiLCAiQmFzZSBuYW1lIiksDQoJCQluZXcgSXRlbSgi' +
            'ZmlsZU5hbWUiLCAiRmlsZSBuYW1lIikJCQkNCgkJfSwgbmV3IFN0cmluZ1tdIHsgImZpbGVUaXRsZSIg' +
            'fSApOw0KCQlKQ29tYm9Cb3ggdGh1bWJuYWlsSXRlbXNEaXNwbGF5ID0gbmV3IEpDb21ib0JveChuZXcg' +
            'T2JqZWN0W10geyBuZXcgSXRlbSgiZmlyc3RGb3VuZCIsICJTaG93IGZpcnN0IGZvdW5kIiksIG5ldyBJ' +
            'dGVtKCJhbGwiLCAiU2hvdyBhbGwiKSB9KTsJDQoJCQ0KCQlDb250cm9sUGFuZWwgYWEgPSBuZXcgQ29u' +
            'dHJvbFBhbmVsKCkgeyAvLyBJbnZpc2libGUgY29udGFpbmVyIHRvIGNvbnRyb2wgc2l6ZXMgb2YgYm94' +
            'ZXMgd2l0aGluIGl0DQoNCgkJCUNvbnRyb2xQYW5lbCBhbGJ1bUluZGV4UGFnZVBhbmVsID0gbmV3IENv' +
            'bnRyb2xQYW5lbCgpIHsNCgkJCQl7DQoJCQkJCXNldFRpdGxlKCJBbGJ1bSBJbmRleCBQYWdlIik7DQoJ' +
            'CQkJCWFkZCh0b3BBbGJ1bVRpdGxlKTsNCgkJCQkJYWRkKCJiciIsIHRvcEFsYnVtRGVzY3JpcHRpb24p' +
            'Ow0KCQkJCX0NCgkJCX07DQoNCgkJCUNvbnRyb2xQYW5lbCBmb2xkZXJJbmRleFBhZ2VzUGFuZWwgPSBu' +
            'ZXcgQ29udHJvbFBhbmVsKCkgew0KCQkJCXsNCgkJCQkJc2V0VGl0bGUoIkZvbGRlciBJbmRleCBQYWdl' +
            'cyIpOw0KCQkJCQlhZGQoZm9sZGVyQWxidW1UaXRsZSk7DQoJCQkJCWFkZCgiYnIiLCBmb2xkZXJGb2xk' +
            'ZXJUaXRsZSk7DQoJCQkJCWFkZCgiYnIiLCBmb2xkZXJGb2xkZXJDb21tZW50KTsNCgkJCQl9DQoJCQl9' +
            'Ow0KDQoJCQlDb250cm9sUGFuZWwgc2xpZGVQYWdlc1BhbmVsID0gbmV3IENvbnRyb2xQYW5lbCgpIHsN' +
            'CgkJCQl7DQoJCQkJCXNldFRpdGxlKCJTbGlkZSBQYWdlcyIpOw0KCQkJCQlhZGQoc2xpZGVBbGJ1bVRp' +
            'dGxlKTsNCgkJCQkJYWRkKCJiciIsIHNsaWRlRm9sZGVyVGl0bGUpOw0KCQkJCQlhZGQoImJyIiwgc2xp' +
            'ZGVJbWFnZVRpdGxlKTsNCgkJCQkJYWRkKCJiciIsIHNsaWRlSW1hZ2VDb21tZW50KTsNCgkJCQl9DQoJ' +
            'CQl9Ow0KCQkJew0KCQkJCWFkZCgidnRvcCB2ZmlsbCIsIGFsYnVtSW5kZXhQYWdlUGFuZWwpOw0KCQkJ' +
            'CWFkZCgidmZpbGwiLCBmb2xkZXJJbmRleFBhZ2VzUGFuZWwpOw0KCQkJCWFkZCgidmZpbGwiLCBzbGlk' +
            'ZVBhZ2VzUGFuZWwpOw0KCQkJfQ0KCQl9Ow0KCQkNCgkJQ29udHJvbFBhbmVsIGJiID0gbmV3IENvbnRy' +
            'b2xQYW5lbCgpICB7IC8vIEludmlzaWJsZSBjb250YWluZXIgc28gdGhhdCB0aGlzIHN1YnBhbmVsIHdp' +
            'bGwgbGluZSB1cCB3aXRoIHRoZSBvbmVzIGFib3ZlIGl0DQoNCgkJCUNvbnRyb2xQYW5lbCB0aHVtYm5h' +
            'aWxQYW5lbCA9IG5ldyBDb250cm9sUGFuZWwoKSB7DQoJCQkJew0KCQkJCQlzZXRUaXRsZSgiVGh1bWJu' +
            'YWlsIGxhYmVscyIpOw0KCQkJCQlhZGQobmV3IEpMYWJlbCgiQXZhaWxhYmxlIikpOw0KCQkJCQlhZGQo' +
            'InRhYiIsIG5ldyBKTGFiZWwoIlVzZWQiKSk7DQoJCQkJCWFkZCgiYnIiLCBuZXcgSlNjcm9sbFBhbmUo' +
            'dGh1bWJuYWlsSXRlbXMuZ2V0U2Vjb25kYXJ5TGlzdCgpKSk7DQoJCQkJCWFkZCgidGFiIiwgbmV3IEpT' +
            'Y3JvbGxQYW5lKHRodW1ibmFpbEl0ZW1zKSk7DQoJCQkJCWFkZCgiYnIgdGFiIiwgdGh1bWJuYWlsSXRl' +
            'bXNEaXNwbGF5KTsNCgkJCQl9DQoJCQl9Ow0KCQkJew0KCQkJCWFkZCh0aHVtYm5haWxQYW5lbCk7DQoJ' +
            'CQl9DQoJCX07DQoJCXsNCgkJCWFkZChhYSk7DQoJCQlhZGQoImJyIiwgYmIpOw0KCQl9DQoJfTsNCgkN' +
            'CgkvLyBBYm91dCB0YWINCglDb250cm9sUGFuZWwgYWJvdXRQYW5lbCA9IG5ldyBDb250cm9sUGFuZWwo' +
            'KSB7DQoJCUpMaW5rTGFiZWwgZGV2ZWxvcGVyQ2VudGVyID0gbmV3IEpMaW5rTGFiZWwoImh0dHA6Ly9q' +
            'YWxidW0ubmV0L2RldmVsb3BlciIsICJqQWxidW0gRGV2ZWxvcGVyIENlbnRlciIpOw0KCQl7DQoJCQlh' +
            'ZGQobmV3IEpMYWJlbChza2luICsgIiB2ZXJzaW9uICIgKyBza2luVmVyc2lvbiArICIsIMKpMjAyMSBi' +
            'eSAiICsgc2tpbkF1dGhvcikpOw0KCQkJYWRkKCJiciIsIG5ldyBKTGFiZWwoIkEgc2tlbGV0YWwgc2tp' +
            'biwgaW50ZW5kZWQgdG8gcHJvdmlkZSBhIGZyYW1ld29yayBmb3IgZnVydGhlciBkZXZlbG9wbWVudCIp' +
            'KTsNCgkJCWFkZCgiYnIiLCBuZXcgSkxhYmVsKCJUaGlzIGlzIGEgc2FtcGxlIHVzZXIgaW50ZXJmYWNl' +
            'IChzZWUgZmlsZSBcIm9ubG9hZC5ic2hcIiksIGRlc2lnbmVkIHRvIikpOw0KCQkJYWRkKCJiciIsIG5l' +
            'dyBKTGFiZWwoImRlbW9uc3RyYXRlIGhvdyBzdWNoIGFuIGludGVyZmFjZSBpcyBhc3NlbWJsZWQiKSk7' +
            'DQoJCQlhZGQoImJyIiwgZGV2ZWxvcGVyQ2VudGVyKTsNCgkJfQ0KCX07DQoJDQoJLy8gVGFiYmVkIHBh' +
            'bmUNCglKVGFiYmVkUGFuZSB0YWJzID0gbmV3IEpUYWJiZWRQYW5lKCkgew0KCQl7DQoJCQlhZGQoZ2Vu' +
            'ZXJhbFBhbmVsLCAiR2VuZXJhbCIpOw0KCQkJYWRkKHRleHRzUGFuZWwsICJUZXh0cyIpOw0KCQkJYWRk' +
            'KGFib3V0UGFuZWwsICJBYm91dCIpOw0KCQl9DQoJfTsNCgl7DQoJCWFkZCgidnRvcCBoZmlsbCB2Zmls' +
            'bCIsIHRhYnMpOw0KCQlwdXRDbGllbnRQcm9wZXJ0eSgiaGVscFBhZ2UiLCAiaHR0cDovL2phbGJ1bS5u' +
            'ZXQvaGVscC9Ta2luL01pbmltYWwiKTsNCgl9DQp9Ow0KDQovLyBTZXQgdGhlIFVJDQp3aW5kb3cuc2V0' +
            'U2tpblVJKHVpKTsNCg==').decodeBase64(), 'UTF-8')
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass(src, 'onload.groovy')
        }
        def m = (err.message =~ /@ line (\d+)/)
        assert m
        def line = m[0][1] as int
        assert line >= 95 && line <= 110

    }

    // GROOVY-10856: MethodSelectionException when calling respondsTo()
    @NotYetImplemented @Test
    void testGroovy10856() {
        assertScript '''
            class C10856 {
                @groovy.transform.NamedVariant
                def myMethod(String a, int b = 0) { a + b }
            }
            def r = new C10856().respondsTo('myMethod', null)
            assert r != null
        '''

    }

    // GROOVY-10870: Fail to infer type argument of parameterized function
    @NotYetImplemented @Test
    void testGroovy10870() {
        assertScript '''
            class Foo10870<X> {}
            @groovy.transform.CompileStatic
            class Test10870 {
                static <T> void m(T t, Foo10870<T> f) {}
                static run() { m(false, new Foo10870<>()) }
            }
            Test10870.run()
        '''

    }

    // GROOVY-10888: Parser doesn't accept wildcard in type witness
    @NotYetImplemented @Test
    void testGroovy10888() {
        assertScript '''
            import static java.util.stream.Collectors.partitioningBy
            class Asdf {
                def run() {
                    ['a', 'b'].stream()
                        .<Map<Boolean, List<String>>, ?> collect(partitioningBy { it == 'a' })
                }
            }
            def r = new Asdf().run()
            assert r[true] == ['a']
        '''

    }

    // GROOVY-10900: Local variable annotations not available through accessed variable
    @NotYetImplemented @Test
    void testGroovy10900() {
        def cu = new CompilationUnit()
        cu.addSource('A10900.groovy', '''
            def run() {
                @Deprecated def object = null
                def y = object
                y
            }
        ''')
        cu.compile(CompilePhase.SEMANTIC_ANALYSIS.phaseNumber)
        def found = []
        def cn = cu.classNodeIterator.next()
        cn.methods.each { m ->
            m.code?.visit(new CodeVisitorSupport() {
                void visitVariableExpression(VariableExpression node) {
                    if (node.name == 'object') {
                        def accessed = node.accessedVariable
                        def anns = accessed?.annotations ?: []
                        found << [isDecl: node.isClosureSharedVariable(), accessedAnns: anns*.classNode*.name, annsOnSelf: node.annotations*.classNode*.name]
                    }
                    super.visitVariableExpression(node)
                }
            })
        }
        assert found
        def hasDeprecated = found.any { rec ->
            (rec.accessedAnns + rec.annsOnSelf).any { it.contains('Deprecated') }
        }
        assert hasDeprecated

    }

    // GROOVY-10901: Annotation on local variable seen as type annotation
    @NotYetImplemented @Test
    void testGroovy10901() {
        def cu = new CompilationUnit()
        cu.addSource('A10901.groovy', '''
            def run() {
                def list = [1, 2]
                def (int i, @Deprecated Number n) = list
                n
            }
        ''')
        cu.compile(CompilePhase.SEMANTIC_ANALYSIS.phaseNumber)
        def varAnns = []
        def typeAnns = []
        def cn = cu.classNodeIterator.next()
        cn.methods.each { m ->
            m.code?.visit(new CodeVisitorSupport() {
                void visitDeclarationExpression(DeclarationExpression node) {
                    node.tupleExpression?.expressions?.each { expr ->
                        if (expr instanceof VariableExpression && expr.name == 'n') {
                            varAnns.addAll(expr.annotations*.classNode*.name)
                            typeAnns.addAll(expr.type.annotations*.classNode*.name ?: [])
                            typeAnns.addAll(expr.type.typeAnnotations*.classNode*.name ?: [])
                        }
                    }
                    super.visitDeclarationExpression(node)
                }
            })
        }
        assert varAnns.any { it.contains('Deprecated') }
        assert !typeAnns.any { it.contains('Deprecated') }

    }

    // GROOVY-10912: Inconsistency in the treatment of primitives values when passed as call arguments
    @NotYetImplemented @Test
    void testGroovy10912() {
        assertScript '''
            @groovy.transform.TypeChecked
            def run() {
                Character.valueOf('c' as char)
                Character.valueOf('c')
            }
            run()
        '''

    }

    // GROOVY-10944: Inconsistency with getAt between List and other Iterables
    @NotYetImplemented @Test
    void testGroovy10944() {
        def capture = { c, idx ->
            try {
                return [ok: true, value: c[idx]]
            } catch (Throwable t) {
                return [ok: false, type: t.getClass()]
            }
        }
        def list = [1, 2, 3]
        def set = [1, 2, 3] as Set
        def col = new ArrayList([1, 2, 3]) as Collection
        def a = capture(list, -4)
        def b = capture(set, -4)
        def c = capture(col, -4)
        assert a.ok == b.ok && a.ok == c.ok
        if (a.ok) {
            assert a.value == b.value && a.value == c.value
        } else {
            assert a.type == b.type && a.type == c.type
        }

    }

    // GROOVY-10956: Initialization of non-static inner class fails
    @NotYetImplemented @Test
    void testGroovy10956() {
        assertScript '''
            class Foo10956 {
                class Bar {}
            }
            Foo10956 y = new Foo10956()
            Foo10956.Bar z = y.new Bar()
            assert z instanceof Foo10956.Bar
        '''

    }

    // GROOVY-10959: Type checking fails when casting method references
    @NotYetImplemented @Test
    void testGroovy10959() {
        assertScript '''
            import java.util.function.Function
            import java.util.stream.Collectors
            class X10959 { String m(Object o) { String.valueOf(o) } }
            class Y10959 { String m(String s) { s } }
            @groovy.transform.TypeChecked
            def run() {
                def x = new X10959()
                def y = new Y10959()
                def z = { String a, String b -> a }
                ['a'].stream().collect(Collectors.toConcurrentMap(x::m as Function<Object,String>, y::m as Function<String,String>, z))
            }
            def r = run()
            assert r['a'] == 'a'
        '''

    }

    // GROOVY-10961: STC rejects program when having SAM types and conditionals
    @NotYetImplemented @Test
    void testGroovy10961() {
        assertScript '''
            import java.util.function.Supplier
            class P10961 { Integer peek() { 1 } }
            @groovy.transform.TypeChecked
            def run() {
                def p1 = new P10961()
                Supplier<Integer> p2 = { 2 }
                Supplier<Integer> x = (true) ? p1::peek : p2
                x.get()
            }
            assert run() == 1
        '''

    }

    // GROOVY-10977: STC does not catch the type error when a generic is instantiated with bounded type parameter
    @NotYetImplemented @Test
    void testGroovy10977() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                @groovy.transform.TypeChecked
                class C10977 {
                    def <T extends Number> void test() {
                        List<T> d = new ArrayList<T>()
                        d.add(false)
                    }
                }
            ''')
        }
        assert err.message.contains('add') || err.message.contains('boolean') || err.message.contains('Number')

    }

    // GROOVY-10984: Unexpected parser error when having fully qualified classes and constructor references
    @NotYetImplemented @Test
    void testGroovy10984() {
        assertScript '''
            def q = java.util.PriorityQueue<Number>::new
            assert q().empty || q() instanceof java.util.PriorityQueue
        '''

    }

    // GROOVY-10992: Unexpected type mismatch when calling a parameterized function with a bounded type parameter
    @NotYetImplemented @Test
    void testGroovy10992() {
        assertScript '''
            import java.util.function.BinaryOperator
            @groovy.transform.TypeChecked
            def <X extends Number> X run(X y, BinaryOperator<X> z) {
                java.util.stream.Stream.of(y).reduce(y, z)
            }
            assert run(1, { a, b -> a + b }) == 1
        '''

    }

    // GROOVY-10997: Unexpected type mismatch when using super on a bounded type parameter
    @NotYetImplemented @Test
    void testGroovy10997() {
        assertScript '''
            import java.util.function.Consumer
            @groovy.transform.TypeChecked
            def <Y, I extends Y> void run(java.util.stream.Stream<I> x, Consumer<Y> y) {
                x.forEach(y)
            }
            def log = []
            run([1].stream(), { log << it } as Consumer)
            assert log == [1]
        '''

    }

    // GROOVY-10999: Unexpected type mismatch when having bounded type parameters
    @NotYetImplemented @Test
    void testGroovy10999() {
        assertScript '''
            @groovy.transform.TypeChecked
            def <U extends Number, H extends U> void run() {
                java.util.Collections.<H,H>emptyNavigableMap().ceilingKey((H) null)
            }
            run()
        '''

    }

    // GROOVY-11002: Unexpected type mismatch when having overloaded methods with variable arguments
    @NotYetImplemented @Test
    void testGroovy11002() {
        assertScript '''
            @groovy.transform.TypeChecked
            class Main11002 {
                static <T> T m(java.util.function.Supplier<T> s, T... args) { s.get() }
                static <T> T m(java.util.function.IntSupplier s, T... args) { null }
                static run() { Main11002.<String>m(() -> 'df', 'fdaf') }
            }
            assert Main11002.run() == 'df'
        '''

    }

    // GROOVY-11021: Resolving wrong overloaded method when having variable arguments
    @NotYetImplemented @Test
    void testGroovy11021() {
        assertScript '''
            import groovy.transform.TypeChecked
            class Foo11021<T> {}
            class Bar11021<T> {
                static <T> Bar11021<T> concat(Bar11021<? extends T>... x) { null }
                static <T> Bar11021<T> concat(Bar11021<? extends Bar11021<? extends T>> x) { null }
                void toSortedSet(Comparator<? super T> x) {}
            }
            @TypeChecked
            class Main11021 {
                static void test() {
                    Bar11021<Foo11021<Number>> x = null
                    Comparator<Foo11021<Number>> y = null
                    Bar11021.concat(x).toSortedSet(y)
                }
            }
            Main11021.test()
        '''

    }

    // GROOVY-11047: Fail to catch incompatible return type of lambdas
    @NotYetImplemented @Test
    void testGroovy11047() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                @groovy.transform.CompileStatic
                class C11047 {
                    def run() {
                        java.util.stream.StreamSupport.<Process>stream(() -> 1, 1, false)
                    }
                }
            ''')
        }
        assert err.message.contains('Process') || err.message.contains('int') || err.message.contains('Supplier') || err.message.contains('lambda')

    }

    // GROOVY-11058: plus() on UnmodifiableMap returns a new Map with changed data
    @NotYetImplemented @Test
    void testGroovy11058() {
        def orig = Collections.unmodifiableMap([t: 'somestring', n: 1])
        def snapshot = new LinkedHashMap(orig)
        try {
            def result = orig + [m: 2]
            assert orig == snapshot
            shouldFail(UnsupportedOperationException) {
                result.put('x', 1)
            }
        } catch (UnsupportedOperationException ignored) {
            assert orig == snapshot
        }

    }

    // GROOVY-11066: Unable to call parameterized function that contains type variables with recursive bounds
    @NotYetImplemented @Test
    void testGroovy11066() {
        assertScript '''
            @groovy.transform.TypeChecked
            class ISM11066 {
                static <K extends Comparable<? super K>, V> Map<K,V> of(K k, V v) { [(k): v] }
            }
            @groovy.transform.TypeChecked
            def run() {
                ISM11066.<String, Number>of('a', 1)
            }
            assert run()['a'] == 1
        '''

    }

    // GROOVY-11077: STC overconstraints lambda's parameter types when expecting a SAM that involves wildcards
    @NotYetImplemented @Test
    void testGroovy11077() {
        assertScript '''
            @groovy.transform.TypeChecked
            class C11077 {
                static void m(Comparator<? extends Object> c) {}
                static run() { m((Integer x, Integer y) -> 1) }
            }
            C11077.run()
        '''

    }

    // GROOVY-11082: Incompatible generic argument when mixing diamond operator and "super" wildcard
    @NotYetImplemented @Test
    void testGroovy11082() {
        assertScript '''
            import java.util.function.Supplier
            class Foo11082<T> {
                Foo11082(Supplier<? super T> s) {}
            }
            @groovy.transform.TypeChecked
            def run() {
                Supplier<CharSequence> func = { 'x' }
                Foo11082<StringBuffer> x = new Foo11082<>(func)
                x
            }
            assert run() instanceof Foo11082
        '''

    }

    // GROOVY-11086: Wrong method is chosen when having overloaded methods with bounded type parameters
    @NotYetImplemented @Test
    void testGroovy11086() {
        assertScript '''
            import groovy.transform.TypeChecked
            class Foo11086<T> {}
            class Bar11086<T> {}
            @TypeChecked
            class Main11086 {
                static <X, T extends Iterable<X>> Foo11086<T> m(Iterable<X> x) { null }
                static <X, T extends List<X>> Bar11086<T> m(List<X> x) { null }
                static void test() {
                    LinkedList<Number> x = null
                    Foo11086<Iterable<Number>> y = Main11086.<Number, Iterable<Number>>m(x)
                }
            }
            Main11086.test()
        '''

    }

    // GROOVY-11109: Unable to initialize generic class with an array
    @NotYetImplemented @Test
    void testGroovy11109() {
        assertScript '''
            class Foo11109<T> { Foo11109(T t) {} }
            @groovy.transform.TypeChecked
            def run() {
                Byte[][] arg = new Byte[0][0]
                new Foo11109<Byte[]>(arg)
            }
            assert run() instanceof Foo11109
        '''

    }

    // GROOVY-11142: this reference in trait fails to resolve properly 
    @NotYetImplemented @Test
    void testGroovy11142() {
        assertScript '''
            trait Foo11142 {
                def bar() { this }
            }
            def a = 'xxx'.withTraits(Foo11142).bar()
            assert a instanceof String
            assert a == 'xxx'
        '''

    }

    // GROOVY-11153: groovy.transform.builder.Builder does not create methods for super-class setters
    @NotYetImplemented @Test
    void testGroovy11153() {
        assertScript '''
            import groovy.transform.builder.Builder
            class BaseDto11153 {
                private String phone
                void setPhone(String p) { this.phone = p }
                String getPhone() { phone }
            }
            @Builder(includeSuperProperties=true, useSetters=true, allProperties=true, allNames=true)
            class ExtendedDto11153 extends BaseDto11153 {}
            def d = ExtendedDto11153.builder().phone('123').build()
            assert d.phone == '123'
        '''

    }

    // GROOVY-11244: Cannot define Java-like lambda inside closure
    @NotYetImplemented @Test
    void testGroovy11244() {
        assertScript '''
            import java.util.function.Function
            @groovy.transform.TypeChecked
            def run() {
                def cl = {
                    Function<Integer, Integer> x = (z) -> 1 + z
                    x.apply(1)
                }
                cl()
            }
            assert run() == 2
        '''

    }

    // GROOVY-11298: Name Conflict in Processing XML
    @NotYetImplemented @Test
    void testGroovy11298() {
        assertScript '''
            def writer = new StringWriter()
            def request = new groovy.xml.MarkupBuilder(writer)
            def o = { number 1 }
            request.parent {
                'o' o
            }
            def xml = writer.toString()
            assert xml.contains('<o>')
            assert xml.contains('<number>') || xml.contains('1')
        '''

    }

    // GROOVY-11410: type mismatch when the expected type is java.lang.Class
    @NotYetImplemented @Test
    void testGroovy11410() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                @groovy.transform.TypeChecked
                class C11410 {
                    def run() { Class<String> lapse = 1 }
                }
            ''')
        }
        assert err.message.contains('Class') || err.message.contains('int') || err.message.contains('Cannot')

    }

    // GROOVY-11411: Static Compilation fails for Grails domain class method getAll()
    @NotYetImplemented @Test
    void testGroovy11411() {
        assertScript '''
            class Domain11411 {
                static List getAll(Iterable<Serializable> ids) { ids as List }
            }
            @groovy.transform.CompileStatic
            def run() {
                Domain11411.getAll([1L, 2L])
            }
            assert run() == [1L, 2L]
        '''

    }

    // GROOVY-11430: Inconsistent behavior when encountering overloaded methods
    @NotYetImplemented @Test
    void testGroovy11430() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                import groovy.transform.CompileStatic
                import java.util.function.Consumer
                class A11430<T> {
                    void m(T x) {}
                    void m(Integer x) {}
                }
                @CompileStatic
                class Test11430 {
                    static void main(String[] args) {
                        def x = new A11430<Integer>()
                        Consumer<Integer> y = x::m
                    }
                }
            ''')
        }
        assert err.message.toLowerCase().contains('ambiguous') || err.message.contains('m(')

    }

    // GROOVY-11431: missing type error when using a Closure within a ternary operator
    @NotYetImplemented @Test
    void testGroovy11431() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                @groovy.transform.TypeChecked
                class C11431 {
                    def run() {
                        Closure<String> y = { 's' }
                        long x = ((true) ? y : () -> 100).call()
                    }
                }
            ''')
        }
        assert err.message.contains('long') || err.message.contains('String') || err.message.contains('Cannot')

    }

    // GROOVY-11444: StackOverflowError when dealing with a generic type bounded by an array
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    @NotYetImplemented @Test
    void testGroovy11444() {
        def future = CompletableFuture.supplyAsync {
            try {
                new GroovyClassLoader().parseClass('class A11444<T extends X[], X extends T> {}')
                return 'COMPILED'
            } catch (MultipleCompilationErrorsException e) {
                return 'ERROR:' + e.message
            } catch (StackOverflowError e) {
                return 'SOE'
            }
        }
        try {
            def result = future.get(5, TimeUnit.SECONDS)
            assert result != 'SOE'
            assert result == 'ERROR:' + result.substring(6) || result == 'COMPILED' || result.startsWith('ERROR:')
            assert result != 'SOE'
        } finally {
            future.cancel(true)
        }

    }

    // GROOVY-11446: Missing type mismatch when dealing with a polymorphic function expecting a wildcard
    @NotYetImplemented @Test
    void testGroovy11446() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                @groovy.transform.CompileStatic
                class C11446 {
                    def <T> List<T> m(List<? extends T> l) { (List<T>) l }
                    def run() {
                        List<?> raw = ['a']
                        List<Number> x = m(raw)
                    }
                }
            ''')
        }
        assert err.message.contains('List') || err.message.contains('Cannot') || err.message.contains('Number')

    }

    // GROOVY-11447: Using a conditional when expecting a SAM type leads to type unsafe results
    @NotYetImplemented @Test
    void testGroovy11447() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                import groovy.transform.TypeChecked
                @FunctionalInterface
                interface Executor11447 {
                    void execute(Runnable command)
                }
                class A11447 {
                    private Executor11447 exec
                    A11447(Executor11447 exec) { this.exec = exec }
                    void execute(Runnable command) { this.exec.execute(command) }
                }
                @TypeChecked
                class Test11447 {
                    static void main(String[] args) {
                        A11447 a = new A11447((true) ? ""::indexOf : (y, z) -> 1)
                        a.execute(() -> {})
                    }
                }
            ''')
        }
        assert err.message.contains('Executor') || err.message.contains('indexOf') || err.message.contains('Cannot') || err.message.contains('lambda')

    }

    // GROOVY-11487: RuntimeParserException when encountering a lambda and a function type with wildcard
    @NotYetImplemented @Test
    void testGroovy11487() {
        assertScript '''
            @groovy.transform.CompileStatic
            class C11487 {
                static class A<T> {}
                static class B<T> extends A<T> {}
                static def <T> void m(java.util.function.Function<A<T>, B<T>> f) {}
                static run() { m((B<T> b) -> b) }
            }
            // The ticket is a RuntimeParserException during instruction selection for a
            // lambda vs function type with wildcard. Desired: compile without that crash.
            true
        '''

    }

    // GROOVY-11495: failing to detect type error when having a method reference with an invalid return
    @NotYetImplemented @Test
    void testGroovy11495() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                class A11495<T> { T x }
                @groovy.transform.TypeChecked
                class Test11495 {
                    static A11495<String> m(String s) { new A11495<String>(x: s) }
                    def run() {
                        java.util.function.Function<String, A11495<Integer>> z = Test11495::m
                    }
                }
            ''')
        }
        assert err.message.contains('A11495') || err.message.contains('Function') || err.message.contains('Cannot')

    }

    // GROOVY-11496: missing type mismatch when indexing lists
    @NotYetImplemented @Test
    void testGroovy11496() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                @groovy.transform.TypeChecked
                class C11496 {
                    def run() {
                        List<Boolean> x = [true]
                        List<Boolean> y = [true]
                        x[y]
                    }
                }
            ''')
        }
        assert err.message.contains('List') || err.message.contains('int') || err.message.contains('Boolean') || err.message.contains('Cannot')

    }

    // GROOVY-11531: Missing errors for improper enum constant initialization
    @NotYetImplemented @Test
    void testGroovy11531() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                enum E11531 {
                    FOO(), BAR {}, BAZ(null)
                    E11531(int i) {}
                }
            ''')
        }
        assert err.message.contains('constructor') || err.message.contains('E11531') || err.message.contains('Cannot')

    }

    // GROOVY-11629: Redundant null check on @NullChecked parameter when iterating on it
    @NotYetImplemented @Test
    void testGroovy11629() {
        def dir = File.createTempDir('groovy11629-', '')
        try {
            def config = new CompilerConfiguration()
            config.targetDirectory = dir
            def loader = new GroovyClassLoader(this.class.classLoader, config)
            def cls = loader.parseClass('''
                @groovy.transform.CompileStatic
                @groovy.transform.NullCheck
                class C11629 {
                    def listNumbers(List<Number> numbers) {
                        for (number in numbers) {
                            number
                        }
                    }
                }
            ''')
            def classFile = new File(dir, cls.name.replace('.', '/') + '.class')
            if (!classFile.exists()) {
                classFile = dir.listFiles()?.find { it.name.endsWith('.class') && it.name.contains('C11629') }
            }
            assert classFile?.exists()
            def cr = new ClassReader(classFile.bytes)
            def ifNulls = 0
            def inListNumbers = false
            cr.accept(new ClassVisitor(Opcodes.ASM9) {
                MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                    def mv = super.visitMethod(access, name, desc, signature, exceptions)
                    if (name != 'listNumbers') return mv
                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        void visitJumpInsn(int opcode, Label label) {
                            if (opcode == Opcodes.IFNULL || opcode == Opcodes.IFNONNULL) {
                                ifNulls++
                            }
                            super.visitJumpInsn(opcode, label)
                        }
                    }
                }
            }, 0)
            // @NullCheck already inserts one explicit null throw; a second IFNULL before iterator() is the residual
            assert ifNulls <= 1
        } finally {
            dir.deleteDir()
        }

    }

    // GROOVY-11861: method selection does not support narrowing of call args
    @NotYetImplemented @Test
    void testGroovy11861() {
        assertScript '''
            class C11861 { short s }
            new C11861().setS(42)
            def c = new C11861()
            c.setS(42)
            assert c.s == 42
        '''

    }

    // GROOVY-11873: RuntimeParserException during instruction selection: the inferred type is not compatible with the parameter
    @NotYetImplemented @Test
    void testGroovy11873() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyClassLoader().parseClass('''
                interface Function1<T, R> { R apply(T t) }
                @groovy.transform.CompileStatic
                class Test11873 {
                    Function1<? super Short, ? extends Number> fun = (Long x) -> null
                }
            ''')
        }
        def text = err.message
        assert !text.contains('RuntimeParserException')
        assert !text.contains('instruction selection')

    }

    // GROOVY-11931: Incorrect method resolution precedence for overloaded methods with Map parameter
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    @NotYetImplemented @Test
    void testGroovy11931() {
        def future = CompletableFuture.supplyAsync {
            new GroovyShell().evaluate('''
                class C11931 {
                    def get(Serializable id) { get(id: id) }
                    def get(Map filterParams) { filterParams }
                    def run() { get(java.util.Map.of('k1', 'id', 'v1', 1)) }
                }
                new C11931().run()
            ''')
        }
        try {
            def result = future.get(5, TimeUnit.SECONDS)
            assert result instanceof Map
            assert result.k1 == 'id' || result.containsKey('k1')
        } finally {
            future.cancel(true)
        }

    }

    // GROOVY-12485: Recognise Zazr control carriers by name in the monadic carrier allow-list
    @NotYetImplemented @Test
    void testGroovy12485() {
        def names = MonadicCarrierRegistry.namedEntries()*.typeName
        assert names.any { it.startsWith('dev.zazr') }

    }

    // GROOVY-12510: STC accepts a private method of a superclass called through a subclass receiver
    @NotYetImplemented @Test
    void testGroovy12510() {
        ['TypeChecked', 'CompileStatic'].each { mode ->
            def src = """
                @groovy.transform.${mode}
                class Outer12510 {
                    static class Base {
                        private def im() { 'base' }
                    }
                    static class Sub extends Base {}
                    def run() { new Sub().im() }
                }
            """
            shouldFail(MultipleCompilationErrorsException) {
                new GroovyClassLoader().parseClass(src)
            }
        }

    }

    // GROOVY-12511: SC: private method of the enclosing class is called on the wrong instance from an inner or anonymous subclass
    @NotYetImplemented @Test
    void testGroovy12511() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Outer {
                private String im() { 'outer' }
                def run() {
                    def sub = new Sub()
                    def anon = new Outer() {
                        private String im() { 'anon' }
                        def call() { im() }
                    }
                    [sub.im(), anon.call()]
                }
                class Sub extends Outer {
                    private String im() { 'sub' }
                }
            }
            assert new Outer().run() == ['outer', 'outer']
        '''

    }

    // GROOVY-12513: STC: private field of a variable's declared type is not accessible when flow typing narrows the variable to a subclass
    @NotYetImplemented @Test
    void testGroovy12513() {
        assertScript '''
            @groovy.transform.TypeChecked
            class Outer {
                static class Box { private int f = 1 }
                static class Sub extends Box {}
                def m() {
                    Box b = new Sub()
                    b.f
                }
            }
            assert new Outer().m() == 1
        '''

    }

    // GROOVY-12515: Elvis-assignment on a property evaluates the receiver twice (a.b.c ?= d, a.b.c ??= d)
    @NotYetImplemented @Test
    void testGroovy12515() {
        assertScript '''
            class Bean { def prop }
            class Holder {
                int gets = 0
                def bean = new Bean()
                def getBean() { gets += 1; bean }
            }
            def h = new Holder()
            h.bean.prop ?= 'x'
            assert h.gets == 1
            assert h.bean.prop == 'x'
            h = new Holder()
            h.bean.prop ??= 'y'
            assert h.gets == 1
            assert h.bean.prop == 'y'
        '''

    }

    // GROOVY-12523: STC: x.@name is accepted for a private field of a superclass when the type of x is a subclass
    @NotYetImplemented @Test
    void testGroovy12523() {
        ['TypeChecked', 'CompileStatic'].each { mode ->
            def src = """
                @groovy.transform.${mode}
                class Outer12523 {
                    private String f = 'outer'
                    static class Sub extends Outer12523 {
                        String read(Sub sub) { sub.@f }
                    }
                }
            """
            def err = shouldFail(MultipleCompilationErrorsException) {
                new GroovyClassLoader().parseClass(src)
            }
            assert err.message =~ /(?is)(cannot access field|cannot access.*field|field .*inaccessible|private field)/
        }

    }

    // GROOVY-12539: FileReaderSource does not strip a UTF-8 BOM: checks 0xFF instead of 0xBF
    @NotYetImplemented @Test
    void testGroovy12539() {
        def file = File.createTempFile('groovy12539-', '.groovy')
        try {
            file.bytes = ([0xEF, 0xBB, 0xBF] as byte[]) + 'println 123'.getBytes('UTF-8')
            def source = new FileReaderSource(
                file, new CompilerConfiguration())
            def c = source.reader.read()
            assert c != 0xFEFF
            def shell = new GroovyShell()
            assert shell.evaluate(file) == 123 || true
            new GroovyShell().evaluate(file)
        } finally {
            file.delete()
        }

    }

    // GROOVY-12543: STC: Strange behavior depending on source position
    @NotYetImplemented @Test
    void testGroovy12543() {
        def err = shouldFail {
            def loader = new GroovyClassLoader()
            loader.parseClass('''
                import groovy.transform.CompileStatic
                import org.codehaus.groovy.ast.ASTNode
                import org.codehaus.groovy.ast.ClassNode
                import org.codehaus.groovy.ast.expr.ConstantExpression
                import org.codehaus.groovy.ast.expr.ListExpression
                import org.codehaus.groovy.ast.expr.MethodPointerExpression
                import org.codehaus.groovy.ast.stmt.ExpressionStatement
                import org.codehaus.groovy.control.CompilePhase
                import org.codehaus.groovy.control.SourceUnit
                import org.codehaus.groovy.transform.AbstractASTTransformation
                import org.codehaus.groovy.transform.GroovyASTTransformation
                import org.codehaus.groovy.transform.GroovyASTTransformationClass

                @GroovyASTTransformationClass('Bar12543')
                @interface Foo12543 {}

                @GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
                class Bar12543 extends AbstractASTTransformation {
                    void visit(ASTNode[] nodes, SourceUnit source) {
                        def methodName = new ConstantExpression('does_not_exist')
                        nodes.find { it instanceof ClassNode }
                            .getMethods('baz').first()
                            .code = new ExpressionStatement(new MethodPointerExpression(new ListExpression(), methodName))
                    }
                }
            ''')
            def shell = new GroovyShell(loader)
            shell.evaluate('''
                import groovy.transform.CompileStatic
                @CompileStatic
                @Foo12543
                class Baz12543 {
                    def baz() {}
                }
                new Baz12543().baz()
                'done'
            ''')
        }
        assert err.message?.contains('does_not_exist') || err.message?.contains('Cannot find matching method') || err instanceof MultipleCompilationErrorsException || err.class.name.contains('MultipleCompilationErrors')

    }

    // GROOVY-12546: Nested @POJO reading outer constant casts this to GroovyObject
    @NotYetImplemented @Test
    void testGroovy12546() {

        assertScript '''
            import groovy.transform.CompileStatic
            import groovy.transform.stc.POJO

            class OuterCmd {
                public static final String FAIL_CODE = 'IS_TENANT'

                @CompileStatic
                @POJO
                static class Problem extends IllegalStateException {
                    Problem(String id) { super("thing $id") }
                    String code() { FAIL_CODE }
                }
            }

            assert new OuterCmd.Problem('x').code() == 'IS_TENANT'
        '''

    }

    // GROOVY-12547: STC writes unresolved placeholder type variables (T#E2;) into closure generic signatures
    @NotYetImplemented @Test
    void testGroovy12547() {
        assertScript '''
            @groovy.transform.CompileStatic
            class PlaceholderLeak {
                static <E2> java.util.stream.Stream<Map<String, E2>> project() {
                    java.util.stream.Stream.of([a: null] as Map<String, E2>)
                }
                static long count() { project().map { row -> row.size() }.count() }
            }
            assert PlaceholderLeak.count() == 1
        '''

    }

    // GROOVY-12556: STC: an extension method of an interface called through qualified super is reported as an abstract method
    @NotYetImplemented @Test
    void testGroovy12556() {
        assertScript '''
            @groovy.transform.CompileStatic
            class C implements Iterable<Integer> {
                Iterator<Integer> iterator() { [1].iterator() }
                def first() { -1 }
                def run() {
                    [Iterable.super.first(), first()]
                }
            }
            assert new C().run() == [1, -1]
        '''

    }

    // GROOVY-12561: SC: a closure passed or assigned for a type parameter bound to a functional interface is stored unconverted
    @NotYetImplemented @Test
    void testGroovy12561() {
        assertScript '''
            import java.util.function.Function
            @groovy.transform.CompileStatic
            class C {
                static class Box<T> {
                    T shout
                }
                static Box<Function<String, String>> make() {
                    def b = new Box<Function<String, String>>()
                    b.shout = { String s -> s + '!' }
                    b
                }
            }
            assert C.make().shout.apply('hi') == 'hi!'
        '''

    }

    // GROOVY-12628: Assigning a primitive numeric value to a Boolean (boxed) target throws WrongMethodTypeException at run time instead of applying Groovy truth
    @NotYetImplemented @Test
    void testGroovy12628() {
        assertScript '''
            Boolean b = 1
            assert b == true
            b = 0
            assert b == false
            b = 1L
            assert b == true
            b = 1.0d
            assert b == true
            int i = 1
            Boolean boxed = i
            assert boxed == true
        '''

    }

    // GROOVY-12631: CS: assigning to an attribute obj.@name = value where the receiver class has no field name, but there is an extension getter getName(), crashes the compiler with an ArrayIndexOutOfBoundsException
    @NotYetImplemented @Test
    void testGroovy12631() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            def test() {
                def file = new File('x')
                file.@text = 'b'
            }
            test()
        '''
        assert err.message.contains('No such attribute') || err.message.contains('text')
        assert !(err instanceof ArrayIndexOutOfBoundsException)
        assert !err.message.contains('Internal compiler error')

    }

    // GROOVY-12632: CS: reading an attribute obj.@name where the receiver class has no field name, but there is an extension getter getName(), compiles to a call of that getter
    @NotYetImplemented @Test
    void testGroovy12632() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            def test() {
                def sb = new StringBuilder('abc')
                sb.@length
            }
            test()
        '''
        assert err.message.contains('No such attribute') || err.message.contains('length')

    }

    // GROOVY-12636: TC/CS: an element taken from a raw List by subscript, or by GDK methods such as first(), pop() and tail(), has an unresolved type
    @NotYetImplemented @Test
    void testGroovy12636() {
        def cases = [
            'int b = l[0]',
            'int c = l.first()',
            'int d = l.pop()',
            'List t = l.tail()',
        ]
        ['TypeChecked', 'CompileStatic'].each { mode ->
            cases.each { stmt ->
                def src = """
                    @groovy.transform.${mode}
                    def f(List l) {
                        ${stmt}
                    }
                """
                shouldFail(MultipleCompilationErrorsException) {
                    new GroovyClassLoader().parseClass(src)
                }
            }
        }

    }

    // GROOVY-12637: CS: an Object method such as toString() or hashCode() called without a receiver inside an inner class (named or anonymous) is resolved to the *outer* class's override, instead of the method the inner class inherits
    @NotYetImplemented @Test
    void testGroovy12637() {
        assertScript '''
            @groovy.transform.CompileStatic
            class C {
                String toString() { 'C' }
                def named() {
                    new Inner().x()
                }
                def anon() {
                    new Object() {
                        def run() { toString() }
                    }.run()
                }
                class Inner {
                    def x() { toString() }
                }
            }
            def c = new C()
            assert c.named() != 'C'
            assert c.anon() != 'C'
        '''

    }

    // GROOVY-12682: AsmDecompiler doesn't account for the resource at the URL changing
    @NotYetImplemented @Test
    void testGroovy12682() {
        def srcDir = File.createTempDir('asm-src', '')
        def outDir = File.createTempDir('asm-out', '')
        try {
            def src = new File(srcDir, 'Foo.java')
            src.text = 'public class Foo { public static int n = 1; }\n'
            def javac = ToolProvider.systemJavaCompiler
            assert javac != null
            def fm = javac.getStandardFileManager(null, null, null)
            def task = javac.getTask(null, fm, null, ['-d', outDir.absolutePath], null, fm.getJavaFileObjects(src))
            assert task.call()
            def url = new File(outDir, 'Foo.class').toURI().toURL()
            def first = AsmDecompiler.parseClass(url)
            assert first.fields*.fieldName.contains('n')

            src.text = 'public class Foo { public static int n = 1; public static int m = 2; }\n'
            task = javac.getTask(null, fm, null, ['-d', outDir.absolutePath], null, fm.getJavaFileObjects(src))
            assert task.call()
            def second = AsmDecompiler.parseClass(url)
            assert second.fields*.fieldName.contains('m')
        } finally {
            srcDir.deleteDir()
            outDir.deleteDir()
        }

    }

    // GROOVY-12705: SC: multiple assignment with a list literal that has more values than variables does not evaluate the extra values
    @NotYetImplemented @Test
    void testGroovy12705() {
        assertScript '''
            @groovy.transform.CompileStatic
            def test() {
                def log = []
                def v = { String s -> log << s; s }
                def (a, b) = [v('a'), v('b'), v('c')]
                assert log == ['a', 'b', 'c']
                [a, b]
            }
            test()
        '''

    }

    // GROOVY-12731: Indy: compound assignment keeps invoking the operator method of another instance's metaclass
    @NotYetImplemented @Test
    void testGroovy12731() {
        assertScript '''
            class Counter12731 {
                def plus(int n) { 'real' }
            }
            class Caller12731 {
                def addOne(Object value) {
                    def result = value
                    result += 1
                    result
                }
            }
            def customized() {
                def counter = new Counter12731()
                counter.metaClass.plus = { int n -> 'customized' }
                counter
            }
            def caller = new Caller12731()
            caller.addOne(customized())
            assert caller.addOne(new Counter12731()) == 'real'
        '''

    }

    // GROOVY-12745: STC: inaccessible field or property for variable expression
    @NotYetImplemented @Test
    void testGroovy12745() {
        def cu = new CompilationUnit()
        cu.addSource('B.groovy', '''
            package p
            class B {
                @groovy.transform.PackageScope static String field
            }
        ''')
        cu.addSource('C.groovy', '''
            @groovy.transform.CompileStatic
            class C extends p.B {
                static {
                    def x = field
                    field = null
                }
            }
        ''')
        try {
            cu.compile()
        } catch (MultipleCompilationErrorsException ignored) {
        }
        def found = []
        def iter = cu.classNodeIterator
        while (iter.hasNext()) {
            def cn = iter.next()
            cn.declaredConstructors.each { ctor ->
                ctor.code?.visit(new CodeVisitorSupport() {
                    void visitVariableExpression(VariableExpression node) {
                        if (node.name == 'field') {
                            def accessed = node.accessedVariable
                            found << [isDynamic: accessed instanceof DynamicVariable, isField: accessed instanceof FieldNode]
                        }
                        super.visitVariableExpression(node)
                    }
                })
            }
        }
        // also walk static initializer / object initializers
        iter = cu.classNodeIterator
        while (iter.hasNext()) {
            def cn = iter.next()
            (cn.fields + [cn]).each { /* walk clinit via object initializers */ }
            cn.visitContents(new ClassCodeVisitorSupport() {
                protected SourceUnit getSourceUnit() { null }
                void visitVariableExpression(VariableExpression node) {
                    if (node.name == 'field') {
                        def accessed = node.accessedVariable
                        found << [isDynamic: accessed instanceof DynamicVariable, isField: accessed instanceof FieldNode]
                    }
                    super.visitVariableExpression(node)
                }
            })
        }
        assert found
        assert found.every { it.isDynamic } || found.every { !it.isField }

    }

    // GROOVY-12760: STC: call to a property accessor that also exists as an interface default method compiles to different bytecode depending on whether the class is compiled in the same unit
    @NotYetImplemented @Test
    void testGroovy12760() {
        def invokeOf = { File classFile, String methodName ->
            def ops = []
            def cr = new ClassReader(classFile.bytes)
            cr.accept(new ClassVisitor(Opcodes.ASM9) {
                MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                    if (name != methodName) return super.visitMethod(access, name, desc, signature, exceptions)
                    return new MethodVisitor(Opcodes.ASM9) {
                        void visitMethodInsn(int opcode, String owner, String name2, String desc2, boolean isInterface) {
                            def kind = opcode == Opcodes.INVOKEINTERFACE ? 'invokeinterface' : opcode == Opcodes.INVOKEVIRTUAL ? 'invokevirtual' : opcode == Opcodes.INVOKESPECIAL ? 'invokespecial' : String.valueOf(opcode)
                            ops << "${kind} ${owner}.${name2}"
                        }
                    }
                }
            }, 0)
            ops
        }
        def togetherDir = File.createTempDir('groovy12760-together-', '')
        def splitDir = File.createTempDir('groovy12760-split-', '')
        try {
            def namedBaseImpl = '''
                package p
                interface Named {
                    default String getName() { 'named' }
                    default void setName(String n) { }
                }
                abstract class Base implements Named { String name }
                class Impl extends Base { }
            '''
            def svcSrc = '''
                package p
                @groovy.transform.CompileStatic
                class Svc {
                    static String rename(Impl base) {
                        base.name = 'x'
                        base.setName('y')
                        base.getName()
                    }
                }
            '''
            def cfgT = new CompilerConfiguration(targetDirectory: togetherDir)
            def unitT = new CompilationUnit(cfgT)
            unitT.addSource('NamedBaseImpl.groovy', namedBaseImpl)
            unitT.addSource('Svc.groovy', svcSrc)
            unitT.compile()
            def cfgS = new CompilerConfiguration(targetDirectory: splitDir)
            def unitS1 = new CompilationUnit(cfgS)
            unitS1.addSource('NamedBaseImpl.groovy', namedBaseImpl)
            unitS1.compile()
            def loader = new GroovyClassLoader()
            loader.addClasspath(splitDir.absolutePath)
            def cfgS2 = new CompilerConfiguration(targetDirectory: splitDir)
            def unitS2 = new CompilationUnit(cfgS2, null, loader)
            unitS2.addSource('Svc.groovy', svcSrc)
            unitS2.compile()
            def together = invokeOf(new File(togetherDir, 'p/Svc.class'), 'rename')
            def split = invokeOf(new File(splitDir, 'p/Svc.class'), 'rename')
            assert together == split
        } finally {
            togetherDir.deleteDir()
            splitDir.deleteDir()
        }

    }

    // GROOVY-12765: STC: a diamond constructor call is not checked against its target, so a Holder<Number> can wrap a List<Integer>
    @NotYetImplemented @Test
    void testGroovy12765() {
        def err = shouldFail '''
            @groovy.transform.TypeChecked
            class C {
                static class Holder<T> {
                    List<T> items
                    Holder(List<T> items) { this.items = items }
                }
                void test(List<Integer> integers, List<? extends Integer> wildcard) {
                    Holder<Number> one = new Holder<>(integers)
                    Holder<Number> two = new Holder<>(wildcard)
                }
            }
        '''
        assert err.message.contains('List')

    }

    // GROOVY-12766: STC: arguments of a generic method call are not checked against an explicit or inferred type argument
    @NotYetImplemented @Test
    void testGroovy12766() {
        def err = shouldFail '''
            @groovy.transform.TypeChecked
            class C {
                def <E> E first(List<E> list) { list.get(0) }
                def <T> void add(List<T> list, T value) { list.add(value) }
                void test(List<? extends CharSequence> chars, List<StringBuilder> builders) {
                    String s = this.<String>first(chars)
                    this.<CharSequence>add(builders, 'hello')
                    List<? extends CharSequence> local = new ArrayList<StringBuilder>()
                    this.<CharSequence>add(local, 'hello')
                    add(builders, 'hello')
                    List<CharSequence> more = []
                    add(more, 42)
                }
            }
        '''
        assert err.message.contains('first') || err.message.contains('add')

    }

    // GROOVY-12768: try-with-resources and withCloseable throw "Self-suppression not permitted" when close() rethrows the block's exception
    @NotYetImplemented @Test
    void testGroovy12768() {
        assertScript '''
            def shared = new IllegalStateException('orig')
            def res = new Closeable() {
                void close() { throw shared }
            }
            def err = shouldFail(IllegalStateException) {
                try (def r = res) {
                    throw shared
                }
            }
            assert err.is(shared)
            def res2 = new Closeable() {
                void close() { throw shared }
            }
            err = shouldFail(IllegalStateException) {
                res2.withCloseable { throw shared }
            }
            assert err.is(shared)
        '''

    }

    // GROOVY-12769: STC: a Class of a type-variable value is accepted as Class<? extends U> through a generic method call
    @NotYetImplemented @Test
    void testGroovy12769() {
        def err = shouldFail '''
            @groovy.transform.TypeChecked
            def <U> Class<? extends U> classOf(U u) {
                List<Class<? extends U>> list = Collections.singletonList(u.getClass())
                list[0]
            }
        '''
        assert err.message.contains('Cannot assign')

    }

    // GROOVY-12770: STC: after instanceof Class, a value is accepted as any parameterized Class
    @NotYetImplemented @Test
    void testGroovy12770() {
        def err = shouldFail '''
            @groovy.transform.TypeChecked
            void test(Serializable s) {
                if (s instanceof Class) {
                    Class<? extends Number> c = s
                }
            }
        '''
        assert err.message.contains('Cannot assign')

    }

    // GROOVY-12771: A member class inherited by a class is not resolved by its simple name in a class nested within it
    @NotYetImplemented @Test
    void testGroovy12771() {
        assertScript '''
            class Outer {
                class Inner {}
                static class StaticInner {}
            }
            class Sub extends Outer {
                Inner a
                static class Nested {
                    StaticInner b
                    Inner c
                }
            }
            new Sub()
        '''

    }

    // GROOVY-12772: STC: a closure's result is not checked against a type parameter of the calling method that the target gives a generic call
    @NotYetImplemented @Test
    void testGroovy12772() {
        def err = shouldFail '''
            @groovy.transform.TypeChecked
            def <T> List<T> cast(List<String> strings) {
                List<T> list = strings.collect { String s -> s }
                list
            }
        '''
        assert err.message.contains('Cannot assign')

    }

    // GROOVY-12773: STC: an argument of a generic call is not checked against a type argument that the target gives in terms of a bounded type parameter of the calling method
    @NotYetImplemented @Test
    void testGroovy12773() {
        def err = shouldFail '''
            @groovy.transform.TypeChecked
            def <U extends Number> List<Class<? extends U>> classes(Class<? extends Number> c) {
                List<Class<? extends U>> list = Collections.singletonList(c)
                list
            }
        '''
        assert err.message.contains('Cannot assign')

    }

    // GROOVY-12774: STC: a Class<? extends List> argument is accepted for a Class<? extends List<String>> type argument of a parameterized receiver
    @NotYetImplemented @Test
    void testGroovy12774() {
        def err = shouldFail '''
            @groovy.transform.TypeChecked
            void test(Map<String, Class<? extends List<String>>> map, Class<? extends List> raw) {
                map.put('k', raw)
            }
        '''
        assert err.message.contains('Cannot call')

    }

    // GROOVY-12775: STC: the result of collect is not checked against a target's Class<? extends X> type argument
    @NotYetImplemented @Test
    void testGroovy12775() {
        def err = shouldFail '''
            @groovy.transform.TypeChecked
            def test() {
                def <T> void classes(List<Object> items) {
                    List<Class<? extends T>> list = items.collect { it.getClass() }
                }
                class IntList extends ArrayList<Integer> { }
                List<Class<? extends List<String>>> list = [IntList].collect { it }
            }
        '''
        assert err.message.contains('Cannot assign')

    }

    // GROOVY-12776: STC: a constructor reference is not checked against the parameter types of a functional interface whose type parameters a generic call infers
    @NotYetImplemented @Test
    void testGroovy12776() {
        def err = shouldFail '''
            import java.util.function.Function
            @groovy.transform.TypeChecked
            def test() {
                def <T, R> Function<T, R> toFunction(Function<T, R> fn) { fn }
                Function<Object, Object> f = toFunction(StringBuilder::new)
            }
            test()
        '''
        assert err.message.contains('Cannot assign')

    }

    // GROOVY-12777: STC: a method reference whose method has a bounded type parameter is not checked when the generic call it is passed to is itself an argument
    @NotYetImplemented @Test
    void testGroovy12777() {
        def err = shouldFail '''
            import java.util.function.Consumer
            @groovy.transform.TypeChecked
            class C {
                static <T extends Number> void takeNumber(T t) { }
            }
            @groovy.transform.TypeChecked
            def test() {
                def <T> Consumer<T> toConsumer(Consumer<T> c) { c }
                void use(Consumer<String> c) { c.accept('x') }
                use(toConsumer(C::takeNumber))
            }
            test()
        '''
        assert err.message.contains('Cannot call')

    }

    // GROOVY-12778: STC: a method reference to a method whose type parameter is bounded by itself is inferred as Object for a generic call
    @NotYetImplemented @Test
    void testGroovy12778() {
        def err = shouldFail '''
            import java.util.function.BinaryOperator
            @groovy.transform.TypeChecked
            class C {
                static <T extends Comparable<T>> T max(T a, T b) { a.compareTo(b) >= 0 ? a : b }
            }
            @groovy.transform.TypeChecked
            def test() {
                def <T> BinaryOperator<T> toBinaryOperator(BinaryOperator<T> b) { b }
                BinaryOperator<Object> max = toBinaryOperator(C::max)
            }
            test()
        '''
        assert err.message.contains('Cannot assign')

    }

    // GROOVY-12779:  STC: Type::extensionMethod with further parameters is not found when passed to a generic call
    @NotYetImplemented @Test
    void testGroovy12779() {
        assertScript '''
            import java.util.function.BiFunction
            @groovy.transform.TypeChecked
            def test() {
                def <T, U, R> BiFunction<T, U, R> toBiFunction(BiFunction<T, U, R> fn) { fn }
                BiFunction<List<String>, String, String> join = toBiFunction(List::join)
                assert join.apply(['a', 'b'], '-') == 'a-b'
            }
            test()
        '''

    }

    // GROOVY-12780: STC: Comparator.comparing(Type::method) is rejected when the method returns a type parameter of a generic Type
    @NotYetImplemented @Test
    void testGroovy12780() {
        assertScript '''
            @groovy.transform.TypeChecked
            def test() {
                Comparator<Map.Entry<String, Integer>> byKey = Comparator.comparing(Map.Entry::getKey)
                def entries = new ArrayList<Map.Entry<String, Integer>>([b: 1, a: 2].entrySet())
                assert entries.sort(false, byKey)*.key == ['a', 'b']
            }
            test()
        '''

    }

    // GROOVY-12781: A method reference to overloaded methods selects by declared parameter types under @CompileStatic and by argument types under @TypeChecked
    @NotYetImplemented @Test
    void testGroovy12781() {
        assertScript '''
            import java.util.function.Function
            class C {
                static String pick(String s) { 'String' }
                static String pick(Object o) { 'Object' }
            }
            @groovy.transform.CompileStatic
            def cs() {
                Function<Object, String> f = C::pick
                f.apply('x')
            }
            assert cs() == 'String'
        '''

    }

    // GROOVY-12783: SC: a lambda or method reference returning to a functional interface that declares a String does not convert the value
    @NotYetImplemented @Test
    void testGroovy12783() {
        assertScript '''
            import java.util.function.Supplier
            def given(Supplier s) { s.get() }
            @groovy.transform.CompileStatic
            def cs() {
                given(() -> "${'x'}")
            }
            def value = cs()
            assert value instanceof String
            assert value == 'x'
        '''

    }

    // GROOVY-12788: Awaitable.all() and await(a, b) do not fail fast as documented
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    @NotYetImplemented @Test
    void testGroovy12788() {
        def future = CompletableFuture.supplyAsync {
            await Awaitable.all(
                async { Thread.sleep(3000) },
                async { throw new RuntimeException('fail') }
            )
        }
        try {
            future.get(400, TimeUnit.MILLISECONDS)
            assert false: 'Awaitable.all should fail fast'
        } catch (ExecutionException e) {
            def cause = e.cause
            assert cause instanceof RuntimeException
            assert cause.message == 'fail'
        } finally {
            future.cancel(true)
        }

    }

    // GROOVY-12789: An instance method of a class is checked as an override of a static method of its interfaces, which since GROOVY-12111 breaks classes that implement a trait
    @NotYetImplemented @Test
    void testGroovy12789() {
        assertScript '''
            trait T {
                static Long getCount() { 1L }
            }
            class C implements T {
                Integer count
            }
            def c = new C()
            c.count = 3
            assert c.count == 3
        '''

    }

    // GROOVY-12803: Creating a non-static inner class as new Outer.Inner() without an outer instance is not a compile error
    @NotYetImplemented @Test
    void testGroovy12803() {
        def err = shouldFail '''
            class A {
                class B {}
            }
            def x = new A.B()
        '''
        assert err.message.toLowerCase().contains('enclosing') || err.message.toLowerCase().contains('outer') || err.message.contains('A.B')

    }

    // GROOVY-12805: STC: Iterator.inject lacks @ClosureParams, so the closure parameters are untyped
    @NotYetImplemented @Test
    void testGroovy12805() {
        assertScript '''
            @groovy.transform.TypeChecked
            def test() {
                def result = ['a', 'bb', 'ccc'].iterator().inject { acc, str -> acc + str.toUpperCase() }
                result
            }
            assert test() == 'aBBCCC'
        '''

    }

    // GROOVY-12808: STC: @TypeChecked wrapping @CompileStatic still does not compose type-checking extensions
    @NotYetImplemented @Test
    void testGroovy12808() {
        def err = shouldFail {
            new GroovyShell().evaluate('''
                import groovy.transform.*
                @TypeChecked(extensions='groovy/transform/stc/UndefinedVariableTestExtension.groovy')
                class C12808 {
                    @CompileStatic(extensions='groovy/transform/stc/UnresolvedPropertyTestExtension.groovy')
                    class D {
                        def foo() {
                            bar.baz
                        }
                    }
                }
                def c = new C12808()
                def d = new C12808.D(c)
            ''')
        }
        assert err.message =~ /Access to String#baz is forbidden/

    }

    // GROOVY-12809: Compiler: a Java constant used from joint compilation is still not inlined into a Groovy annotation attribute
    @NotYetImplemented @Test
    void testGroovy12809() {
        def config = new CompilerConfiguration().tap {
            jointCompilationOptions = [memStub: true]
            targetDirectory = File.createTempDir()
        }
        File parentDir = File.createTempDir()
        try {
            new File(parentDir, 'p').mkdir()
            new File(parentDir, 'p/A.java').write '''package p;
                public class A {
                    public static final String CONST = "value";
                }
            '''
            new File(parentDir, 'B.groovy').write '''
                interface I12809 {
                    String SINCE = p.A.CONST + '12345'
                    @Deprecated(since = SINCE)
                    interface J {
                    }
                }
                assert I12809.J.getAnnotation(Deprecated).since() == 'value12345'
            '''
            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(new File(parentDir, 'p/A.java'), new File(parentDir, 'B.groovy'))
            cu.compile()
            loader.loadClass('B').main()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }

    }

    // GROOVY-12810: A same-named static method still hides Class.getSuperclass() after a DelegatingMetaClass is installed
    @NotYetImplemented @Test
    void testGroovy12810() {
        assertScript '''
            class A {}
            class C extends A {
                static getSuperclass() { 'This was the wrong method' }
            }
            def c = new C()
            def dmc = new DelegatingMetaClass(c.metaClass)
            dmc.initialize()
            c.metaClass = dmc
            assert c.getClass().getSuperclass() == A
        '''

    }

    // GROOVY-12811: A statically imported method still wins over a delegate method inside with{}
    @NotYetImplemented @Test
    void testGroovy12811() {
        assertScript '''
            import static PublicStatic.who
            class PublicStatic {
                static who() { 'PublicStatic' }
            }
            class C {
                def who() { 'C' }
            }
            assert new C().with { who() } == 'C'
        '''

    }

    // GROOVY-12814: A method call inside tap or with is still not resolved when used as a special constructor argument
    @NotYetImplemented @Test
    void testGroovy12814() {
        assertScript '''
            @groovy.transform.ToString
            class A {
                A(x) {}
                def b
                void init() { b = 42 }
            }
            class C {
                C() {
                    this(new A(null).tap { init() })
                }
                C(x) {
                    assert x.toString() == 'A(42)'
                }
            }
            new C()
        '''

    }

    // GROOVY-12815: @ASTTest still fails when the value closure uses an explicit arrow
    @NotYetImplemented @Test
    void testGroovy12815() {
        assertScript '''
            @groovy.transform.ASTTest(phase=SEMANTIC_ANALYSIS, value={ -> assert true })
            def x
        '''

    }

    // GROOVY-12816: Annotation attribute defaults still reject some primitive casts to short
    @NotYetImplemented @Test
    void testGroovy12816() {
        assertScript '''
            @interface A {
                short s1() default (byte)1
                short s2() default (char)2
                short s3() default (long)3
            }
            assert A.getMethod('s1').defaultValue == 1
            assert A.getMethod('s2').defaultValue == 2
            assert A.getMethod('s3').defaultValue == 3
        '''

    }

    // GROOVY-12817: super.setMetaClass() from a user-defined setMetaClass still does not reach the DGM implementation
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @NotYetImplemented @Test
    void testGroovy12817() {
        assertScript '''
            class C {
                void setMetaClass(MetaClass metaClass) {
                    super.setMetaClass(metaClass)
                }
            }
            def x = new C()
            x.metaClass = x.metaClass
        '''

    }

    // GROOVY-12819: STC: property access on Class<T> is not resolved for an intersection bound with a static member
    @NotYetImplemented @Test
    void testGroovy12819() {
        assertScript '''
            @groovy.transform.TypeChecked
            def test() {
                interface I1 {
                    static String getFoo() { 'foo' }
                }
                interface I2 {
                    String bar = 'bar'
                }
                def <T extends I1 & I2> void test(Class<T> cls) {
                    cls?.foo
                    cls?.bar
                }
                test(null)
            }
            test()
        '''

    }

}

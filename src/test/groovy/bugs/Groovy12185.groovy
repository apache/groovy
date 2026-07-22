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


import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.runtime.BooleanClosureWrapper
import org.codehaus.groovy.runtime.dgmimpl.NumberNumberPlus
import org.codehaus.groovy.runtime.dgmimpl.arrays.IntegerArrayGetAtMetaMethod
import org.junit.jupiter.api.Test

/**
 * GROOVY-12185: classic call-site runtime lives in optional groovy-callsite;
 * core uses invokedynamic by default and keeps DGM invoke() paths.
 */
final class Groovy12185 {

    @Test
    void testIndyIsDefaultAndDoesNotReferenceCallSiteArray() {
        def config = new CompilerConfiguration()
        assert config.isIndyEnabled()
        def gcl = new GroovyClassLoader(Thread.currentThread().contextClassLoader, config)
        def clazz = gcl.parseClass('class C { def m(x) { x.toString() } }')
        assert !clazz.declaredMethods*.name.contains('$getCallSiteArray')
        assert clazz.newInstance().m(123) == '123'
    }

    @Test
    void testClassicCompilationUsesCallSiteArrayWhenIndyDisabled() {
        def config = new CompilerConfiguration(optimizationOptions: [indy: false])
        assert !config.isIndyEnabled()
        def gcl = new GroovyClassLoader(Thread.currentThread().contextClassLoader, config)
        def clazz = gcl.parseClass('class ClassicC { def m(x) { x.toString() } }')
        assert clazz.declaredMethods*.name.contains('$getCallSiteArray')
        // Requires groovy-callsite on test runtime classpath
        assert clazz.newInstance().m(99) == '99'
    }

    /**
     * Primary purpose of groovy-callsite: classes compiled in classic mode
     * (the shape emitted by Groovy 4/5 with indy disabled, and by pre-indy
     * releases) must load and execute against Groovy 6 core + groovy-callsite.
     * Covers POGO / POJO / static / ctor / property / safe-nav / numbers /
     * arrays / closures, including a second pass that uses warmed call sites.
     */
    @Test
    void testClassicBytecodeSurfaceCompatibleWithGroovy4And5Shape() {
        def config = new CompilerConfiguration(optimizationOptions: [indy: false])
        def gcl = new GroovyClassLoader(Thread.currentThread().contextClassLoader, config)
        def clazz = gcl.parseClass('''
            class ClassicCompat {
                String name
                int n
                ClassicCompat(String name, int n) { this.name = name; this.n = n }
                def greet(String who) { "hello $who from $name" }
                def selfCall() { greet('self') }
                static String tag(String s) { "TAG:$s" }
                def runAll() {
                    def list = []
                    list << greet('world')
                    list << selfCall()
                    list << tag('x')
                    def other = new ClassicCompat('other', 7)
                    list << other.name
                    list << this.name
                    list << 42.toString()
                    list << (1 + 2)
                    int[] arr = [10, 20, 30] as int[]
                    list << arr[1]
                    ClassicCompat nil = null
                    list << nil?.greet('z')
                    def c = { a, b -> a + b }
                    list << c(3, 4)
                    list << ('ab' * 2)
                    list << [9, 8, 7][0]
                    return list
                }
            }
        ''')
        assert clazz.declaredMethods*.name.contains('$getCallSiteArray')
        def instance = clazz.getConstructor(String, int).newInstance('main', 1)
        2.times {
            def result = instance.runAll()
            assert result[0] == 'hello world from main'
            assert result[1] == 'hello self from main'
            assert result[2] == 'TAG:x'
            assert result[3] == 'other'
            assert result[4] == 'main'
            assert result[5] == '42'
            assert result[6] == 3
            assert result[7] == 20
            assert result[8] == null
            assert result[9] == 7
            assert result[10] == 'abab'
            assert result[11] == 9
        }
    }

    @Test
    void testClassicCompilationFailsFastWithoutCallSiteRuntime() {
        def config = new CompilerConfiguration(optimizationOptions: [indy: false])
        // Hide classic call-site types from the compilation class loader so
        // WriterController's classpath check fails before bytecode is emitted.
        def gcl = new GroovyClassLoader(Thread.currentThread().contextClassLoader, config) {
            @Override
            Class loadClass(String name, boolean lookupScriptFiles, boolean preferClassOverScript, boolean resolve)
                    throws ClassNotFoundException, CompilationFailedException {
                if (name.startsWith('org.codehaus.groovy.runtime.callsite.')) {
                    throw new ClassNotFoundException(name)
                }
                return super.loadClass(name, lookupScriptFiles, preferClassOverScript, resolve)
            }
        }
        try {
            gcl.parseClass('class X { def m() { 1 } }')
            assert false: 'expected failure when classic call-site runtime is missing'
        } catch (Throwable t) {
            def msg = rootMessage(t)
            assert msg.contains('groovy-callsite') || msg.contains('CallSiteArray') || msg.contains('Classic call-site'):
                    "unexpected failure: $msg"
        }
    }

    @Test
    void testNumberAndArrayMetaMethodsInvokePathsRemainInCore() {
        def plus = new NumberNumberPlus()
        assert plus.invoke(1, [2] as Object[]) == 3

        def getAt = new IntegerArrayGetAtMetaMethod()
        int[] arr = [10, 20, 30] as int[]
        assert getAt.invoke(arr, [1] as Object[]) == 20
    }

    @Test
    void testBooleanClosureWrapperRemainsInCore() {
        def bcw = new BooleanClosureWrapper({ it > 0 })
        assert bcw.call(1)
        assert !bcw.call(-1)
    }

    @Test
    void testMetaClassImplPublicSelectionApi() {
        def mc = (MetaClassImpl) GroovySystem.metaClassRegistry.getMetaClass(String)
        def method = mc.getMethodWithCaching(String, 'length', [] as Class[])
        assert method != null
        assert method.name == 'length'

        def ctor = mc.chooseConstructor([String] as Class[])
        assert ctor != null
        assert ctor.doConstructorInvoke(['hi'] as Object[]).toString() == 'hi'
    }

    private static String rootMessage(Throwable t) {
        def cur = t
        while (cur.cause != null && !cur.cause.is(cur)) {
            cur = cur.cause
        }
        return (t.message ?: '') + ' / ' + (cur.message ?: '') + ' / ' + t.toString()
    }
}

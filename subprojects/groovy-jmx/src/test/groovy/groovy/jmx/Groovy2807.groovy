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
package groovy.jmx

import java.nio.file.Files
import java.util.concurrent.atomic.AtomicInteger

import javax.management.Attribute
import javax.management.AttributeList
import javax.management.DynamicMBean
import javax.management.MBeanException
import javax.management.MBeanInfo
import javax.management.MBeanOperationInfo
import javax.management.MBeanServer
import javax.management.MBeanServerConnection
import javax.management.MBeanServerFactory
import javax.management.ObjectName
import javax.management.RuntimeMBeanException
import javax.tools.Diagnostic
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import javax.tools.ToolProvider

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertSame

class Groovy2807 {

    private final MBeanServer server = MBeanServerFactory.createMBeanServer()

    @Test
    // GROOVY-2807
    void sameArityDifferentTypes() {
        def bean = overloaded()
        assert bean.describeOperation('redeploy').size() == 3
        assert bean.redeploy('http://example.com/app') == 'string'
        assert bean.redeploy(URI.create('http://example.com/app')) == 'uri'
        assert bean.redeploy(new OverloadedMBean.BaseNode()) == 'base'
        assert bean.redeploy(new OverloadedMBean.ChildNode()) == 'base'
    }

    @Test
    // GROOVY-2807
    void swappedParameterTypes() {
        def bean = overloaded()
        assert bean.pair('v', Short.valueOf((short) 2)) == 'string-int:v:2'
        assert bean.pair(Byte.valueOf((byte) 3), 'v') == 'int-string:3:v'
        assert bean.pair('v', Integer.valueOf(4)) == 'string-int:v:4'
        assert bean.pair(Integer.valueOf(5), 'v') == 'int-string:5:v'
    }

    @Test
    // GROOVY-2807
    void wideningSelectsTheMostSpecificOverload() {
        def bean = overloaded()
        assert bean.num(Byte.valueOf((byte) 1)) == 'short'
        assert bean.num(Short.valueOf((short) 1)) == 'short'
        assert bean.num(Character.valueOf((char) 'A')) == 'int'
        assert bean.num(Integer.valueOf(1)) == 'int'
        assert bean.num(Long.valueOf(1L)) == 'long'
        assert bean.num(Float.valueOf(1.5f)) == 'float'
        assert bean.num(Double.valueOf(1.5d)) == 'double:1.5'
        assert bean.num('text') == 'string'
        assert bean.num(new BigDecimal('1.5')) == 'double:1.5'

        assert bean.tiny(Byte.valueOf((byte) 1)) == 'byte'
        assert bean.tiny(Short.valueOf((short) 1)) == 'short'
        assert bean.tiny(Integer.valueOf(1)) == 'int'

        assert bean.code(Byte.valueOf((byte) 1)) == 'short'
        assert bean.code(Character.valueOf((char) 'A')) == 'char'
        assert bean.code(Integer.valueOf(1)) == 'int'

        assert bean.bit(Boolean.TRUE) == 'boolean'
        assert bean.bit(Integer.valueOf(1)) == 'int'

        assert bean.id(Integer.valueOf(7)) == 'long'
        assert bean.id(Long.valueOf(7L)) == 'long'
    }

    @Test
    // GROOVY-2807
    void arrayOverloadsAreDistinctFromScalars() {
        def bean = overloaded()
        assert bean.id(new long[] {1L, 2L}) == 'long[]:2'
        assert bean.id(Long.valueOf(5L), Integer.valueOf(3)) == 'long-int'
        assert bean.id(new long[] {9L}, Integer.valueOf(4)) == 'long[]-int:1'
        assert bean.refs(new String[] {'a', 'b'}) == 'String[]'
        assert bean.refs(new Integer[] {1}) == 'Object[]'
        assert bean.refs(new Object[] {new Object()}) == 'Object[]'
        assert bean.bytes(new byte[] {1}) == 'byte[]'
        assert bean.bytes(new int[] {1}) == 'int[]'
    }

    @Test
    // GROOVY-2807
    void subtypesAndExactWrappersBeatWiderOverloads() {
        def bean = overloaded()
        assert bean.child(new OverloadedMBean.ChildNode()) == 'child'
        assert bean.child(new OverloadedMBean.BaseNode()) == 'base'
        assert bean.shape('x') == 'string'
        assert bean.shape(Integer.valueOf(1)) == 'object'
        assert bean.shape(null) == 'string'
        assert bean.wrap(Integer.valueOf(1)) == 'int'
        assert bean.wrap(new Object()) == 'object'
        assert bean.box(Integer.valueOf(3)) == 'Integer'
        assert bean.flag(Boolean.FALSE) == 'Boolean'
        assert bean.seq('hello') == 'string'
    }

    @Test
    // GROOVY-2807
    void groovyCoercionsAreUsedWhenJavaConversionDoesNotApply() {
        def bean = overloaded()
        def text = "${'hello'}"
        assert text instanceof GString
        assert bean.seq(text) == 'charsequence'
        assert bean.label(text) == 'string:hello'
        assert bean.label('hello') == 'string:hello'
        assert bean.label(URI.create('http://example.com')) == 'uri'
        assert bean.real(new BigDecimal('1.5')) == 'double:1.5'
        assert bean.real(Double.valueOf(1.25d)) == 'double:1.25'
        assert bean.real('x') == 'string'
        def mixed = "${'n'}"
        assert bean.mix(mixed, Integer.valueOf(2)) == 'string-double:n:2.0'
        assert bean.mix(URI.create('http://example.com'), 'ok') == 'uri-string:ok'
    }

    @Test
    // GROOVY-2807
    void singleCandidateKeepsThePreviousDispatch() {
        def bean = overloaded()
        assert bean.only(Integer.valueOf(4)) == 'only:4'
        assert bean.only(Short.valueOf((short) 4)) == 'only:4'
        assert bean.ping() == 'pong'
        assert bean.echo('hi') == 'echo:hi'
        assertThrows(MissingMethodException) {
            bean.missing()
        }
        assertThrows(MissingMethodException) {
            bean.invokeMethod('missing', new Object[0])
        }
    }

    @Test
    // GROOVY-2807
    void exactWrapperBeatsPrimitiveWhenBothAreApplicable() {
        def rig = recording('boxes',
                RecordingMBean.op('box', 'java.lang.Integer'),
                RecordingMBean.op('box', 'int'),
                RecordingMBean.op('flag', 'java.lang.Boolean'),
                RecordingMBean.op('flag', 'boolean'))
        rig.bean.box(Integer.valueOf(1))
        assert rig.recording.signature.toList() == ['java.lang.Integer']
        rig.bean.flag(Boolean.TRUE)
        assert rig.recording.signature.toList() == ['java.lang.Boolean']
    }

    @Test
    // GROOVY-2807
    void nullPrefersTheMostSpecificReference() {
        def rig = recording('nulls',
                RecordingMBean.op('shape', 'java.lang.String'),
                RecordingMBean.op('shape', 'java.lang.Object'))
        rig.bean.shape(null)
        assert rig.recording.signature.toList() == ['java.lang.String']
        rig.bean.shape('x')
        assert rig.recording.signature.toList() == ['java.lang.String']
        rig.bean.shape(Integer.valueOf(1))
        assert rig.recording.signature.toList() == ['java.lang.Object']
    }

    @Test
    // GROOVY-2807
    void tiedOverloadsUseTheLastRegisteredSignature() {
        def rig = recording('ties',
                RecordingMBean.op('mark', RecordingMBean.MarkerA.name),
                RecordingMBean.op('mark', RecordingMBean.MarkerB.name),
                RecordingMBean.op('odd', 'java.lang.String'),
                RecordingMBean.op('odd', 'java.net.URI'))
        rig.bean.mark(new RecordingMBean.BothMarkers())
        assert rig.recording.signature.toList() == [RecordingMBean.MarkerB.name]
        rig.bean.odd(Integer.valueOf(1))
        assert rig.recording.signature.toList() == ['java.net.URI']
        rig.bean.odd(null)
        assert rig.recording.signature.toList() == ['java.net.URI']
    }

    @Test
    // GROOVY-2807
    void primitiveOverloadBeatsObject() {
        def types = ['boolean', 'byte', 'char', 'short', 'int', 'long', 'float', 'double']
        def ops = []
        for (String type : types) {
            ops << RecordingMBean.op(type, type)
            ops << RecordingMBean.op(type, 'java.lang.Object')
        }
        def rig = recording('prims', ops.toArray(new MBeanOperationInfo[0]))
        def values = [
                boolean: Boolean.TRUE,
                byte   : Byte.valueOf((byte) 1),
                char   : Character.valueOf((char) 'A'),
                short  : Short.valueOf((short) 1),
                int    : Integer.valueOf(1),
                long   : Long.valueOf(1L),
                float  : Float.valueOf(1.5f),
                double : Double.valueOf(1.5d),
        ]
        values.each { String type, Object value ->
            rig.bean.invokeMethod(type, value)
            assert rig.recording.action == type
            assert rig.recording.signature.toList() == [type]
        }
    }

    @Test
    // GROOVY-2807
    void typeNamesResolveForArraysLoadersAndInstances() {
        def rig = recording('names',
                RecordingMBean.op('arr', 'java.lang.String[]'),
                RecordingMBean.op('arr', 'no.such.Type[]'),
                RecordingMBean.op('jvm', 'java.lang.Integer'),
                RecordingMBean.op('jvm', '[Ljava.lang.String;'),
                RecordingMBean.op('nums', 'int'),
                RecordingMBean.op('nums', '[J'),
                RecordingMBean.op('pick', 'java.lang.String'),
                RecordingMBean.op('pick', 'no.such.Type'),
                RecordingMBean.op('gone', 'com.missing.Type'))
        rig.bean.arr(new String[] {'a'})
        assert rig.recording.signature.toList() == ['java.lang.String[]']
        rig.bean.jvm(new String[] {'a'})
        assert rig.recording.signature.toList() == ['[Ljava.lang.String;']
        rig.bean.nums(new long[] {1L})
        assert rig.recording.signature.toList() == ['[J']
        rig.bean.nums(Integer.valueOf(1))
        assert rig.recording.signature.toList() == ['int']
        rig.bean.pick('hi')
        assert rig.recording.signature.toList() == ['java.lang.String']
        rig.bean.pick(null)
        assert rig.recording.signature.toList() == ['java.lang.String']
        rig.bean.gone('x')
        assert rig.recording.signature.toList() == ['com.missing.Type']

        def probe = new Probe()
        def previous = Thread.currentThread().contextClassLoader
        try {
            Thread.currentThread().contextClassLoader = null
            rig.bean.pick(probe)
        } finally {
            Thread.currentThread().contextClassLoader = previous
        }
        // pick(Probe) matches neither String nor the absent type, so the last
        // registered pick overload is the historical fallback
        assert rig.recording.signature.toList() == ['no.such.Type']

        def visible = recording('visible',
                RecordingMBean.op('vis', 'java.lang.String'),
                RecordingMBean.op('vis', Probe.name))
        previous = Thread.currentThread().contextClassLoader
        try {
            Thread.currentThread().contextClassLoader = null
            visible.bean.vis(probe)
        } finally {
            Thread.currentThread().contextClassLoader = previous
        }
        assert visible.recording.signature.toList() == [Probe.name]

        def phantom = phantomClass()
        def hidden = recording('hidden',
                RecordingMBean.op('hide', phantom.name),
                RecordingMBean.op('hide', 'java.lang.String'))
        previous = Thread.currentThread().contextClassLoader
        try {
            Thread.currentThread().contextClassLoader = ClassLoader.platformClassLoader
            hidden.bean.hide(phantom.getDeclaredConstructor().newInstance())
        } finally {
            Thread.currentThread().contextClassLoader = previous
        }
        assert hidden.recording.signature.toList() == [phantom.name]
    }

    @Test
    // GROOVY-2807
    void duplicateSignaturesStayCallable() {
        def rig = recording('dups',
                RecordingMBean.op('dup', 'java.lang.String'),
                RecordingMBean.op('dup', 'java.lang.String'),
                RecordingMBean.op('ping'),
                RecordingMBean.op('ping'))
        assert rig.bean.dup('a') == 'dup'
        assert rig.recording.signature.toList() == ['java.lang.String']
        assert rig.bean.ping() == 'ping'
        assert rig.recording.signature.toList() == []
    }

    @Test
    // GROOVY-2807
    void groovyOnlyConversionsPreferTheNarrowerTarget() {
        def rig = recording('groovyOnly',
                RecordingMBean.op('wide', 'double'),
                RecordingMBean.op('wide', 'java.lang.Integer'),
                RecordingMBean.op('edge', 'java.lang.Integer'),
                RecordingMBean.op('edge', 'int'))
        rig.bean.wide(new BigInteger('9'))
        assert rig.recording.signature.toList() == ['java.lang.Integer']
        assert rig.recording.params[0] instanceof Integer
        rig.bean.edge(new BigInteger('9'))
        assert rig.recording.signature.toList() == ['int']
    }

    @Test
    // GROOVY-2807
    void nullArgumentsDoNotSelectPrimitiveParameters() {
        def rig = recording('nullPrim',
                RecordingMBean.op('pair', 'java.lang.String', 'int'),
                RecordingMBean.op('pair', 'int', 'java.lang.String'),
                RecordingMBean.op('solo', 'long'),
                RecordingMBean.op('solo', 'int'))
        rig.bean.pair(null, null)
        assert rig.recording.signature.toList() == ['int', 'java.lang.String']
        rig.bean.solo(null)
        assert rig.recording.signature.toList() == ['int']
        def bean = overloaded()
        assert bean.mix(null, new BigDecimal('1.5')) == 'string-double:null:1.5'
    }

    @Test
    // GROOVY-2807
    void serverFailuresUseTheExistingErrorPolicy() {
        def name = new ObjectName('groovy.test:type=Throwing')
        def throwing = new ThrowingMBean()
        server.registerMBean(throwing, name)
        def bean = new GroovyMBean(server, name.canonicalName)
        assertSame(server, bean.server())
        // Groovy call sites unwrap GroovyRuntimeException, so callers see the
        // server exception rather than the wrapper this class throws internally.
        throwing.checked = false
        def runtime = assertThrows(RuntimeMBeanException) {
            bean.boom()
        }
        assert runtime.cause instanceof IllegalStateException
        throwing.checked = true
        def checked = assertThrows(IllegalStateException) {
            bean.boom()
        }
        assert checked.message == 'checked'

        def quiet = new GroovyMBean(server, name.canonicalName, true)
        throwing.checked = true
        assert quiet.boom() == null
        throwing.checked = false
        assert quiet.boom() == null
    }

    @Test
    // GROOVY-2807
    void operationKeyOverrideStillGroupsAndFilters() {
        CountingKeyMBean.CALLS.set(0)
        def name = new ObjectName('groovy.test:type=Counted')
        server.registerMBean(new Overloaded(), name)
        def counted = new CountingKeyMBean(server, name)
        int constructed = CountingKeyMBean.CALLS.get()
        assert constructed == counted.info().operations.length
        assert counted.ping() == 'pong'
        assert CountingKeyMBean.CALLS.get() == constructed + 1

        def recording = new RecordingMBean(
                RecordingMBean.op('beta', 'java.lang.Object'),
                RecordingMBean.op('beta', 'java.lang.String', 'int'),
                RecordingMBean.op('alpha', 'java.lang.String'))
        def mixedName = new ObjectName('groovy.test:type=MixedKey')
        server.registerMBean(recording, mixedName)
        def mixed = new ConstantKeyMBean(server, mixedName)
        assert mixed.beta('x') == 'beta'
        assert recording.signature.toList() == ['java.lang.Object']
        assert mixed.alpha('x') == 'alpha'
        assert recording.signature.toList() == ['java.lang.String']
        assertThrows(MissingMethodException) {
            mixed.noSuch()
        }

        def only = new RecordingMBean(RecordingMBean.op('alpha', 'java.lang.String'))
        def onlyName = new ObjectName('groovy.test:type=OnlyKey')
        server.registerMBean(only, onlyName)
        def onlyBean = new ConstantKeyMBean(server, onlyName)
        assert onlyBean.alpha('x') == 'alpha'
        assertThrows(MissingMethodException) {
            onlyBean.beta('x')
        }

        def shaped = new RecordingMBean(RecordingMBean.op('alpha', 'java.lang.String', 'int'))
        def shapedName = new ObjectName('groovy.test:type=ShapedKey')
        server.registerMBean(shaped, shapedName)
        def shapedBean = new ConstantKeyMBean(server, shapedName)
        assertThrows(MissingMethodException) {
            shapedBean.alpha('x')
        }
    }

    private GroovyMBean overloaded() {
        def name = new ObjectName('groovy.test:type=Overloaded')
        server.registerMBean(new Overloaded(), name)
        new GroovyMBean(server, name)
    }

    private Map recording(String type, MBeanOperationInfo... ops) {
        def name = new ObjectName("groovy.test:type=${type}")
        def recording = new RecordingMBean(ops)
        server.registerMBean(recording, name)
        [recording: recording, bean: new GroovyMBean(server, name)]
    }

    private static Class<?> phantomClass() {
        def dir = Files.createTempDirectory('groovy-mbean-phantom')
        def source = dir.resolve('jmxfix')
        Files.createDirectories(source)
        def javaFile = source.resolve('Phantom.java').toFile()
        javaFile.text = 'package jmxfix; public class Phantom {}'
        def compiler = ToolProvider.systemJavaCompiler
        assert compiler != null
        def diagnostics = new DiagnosticCollector<Diagnostic<? extends JavaFileObject>>()
        def fileManager = compiler.getStandardFileManager(diagnostics, null, null)
        def units = fileManager.getJavaFileObjects(javaFile)
        def compiled = compiler.getTask(null, fileManager, diagnostics, ['-d', dir.toString(), '--release', '17'], null, units).call()
        assert compiled
        def loader = new URLClassLoader([dir.toUri().toURL()] as URL[], ClassLoader.platformClassLoader)
        loader.loadClass('jmxfix.Phantom')
    }
}

class Probe {
}

class ConstantKeyMBean extends GroovyMBean {

    ConstantKeyMBean(MBeanServerConnection server, ObjectName name) {
        super(server, name)
    }

    @Override
    protected String createOperationKey(String operation, int params) {
        'same'
    }
}

class ThrowingMBean implements DynamicMBean {

    boolean checked

    @Override
    Object getAttribute(String attribute) {
        null
    }

    @Override
    void setAttribute(Attribute attribute) {
    }

    @Override
    AttributeList getAttributes(String[] attributes) {
        new AttributeList()
    }

    @Override
    AttributeList setAttributes(AttributeList attributes) {
        new AttributeList()
    }

    @Override
    Object invoke(String actionName, Object[] params, String[] signature) {
        if (checked) {
            throw new MBeanException(new IllegalStateException('checked'))
        }
        throw new IllegalStateException('runtime')
    }

    @Override
    MBeanInfo getMBeanInfo() {
        new MBeanInfo(ThrowingMBean.name, 'throwing', null, null,
                [RecordingMBean.op('boom')] as MBeanOperationInfo[], null)
    }
}

class CountingKeyMBean extends GroovyMBean {

    static final AtomicInteger CALLS = new AtomicInteger()

    CountingKeyMBean(MBeanServerConnection server, ObjectName name) {
        super(server, name)
    }

    @Override
    protected String createOperationKey(String operation, int params) {
        CALLS.incrementAndGet()
        super.createOperationKey(operation, params)
    }
}

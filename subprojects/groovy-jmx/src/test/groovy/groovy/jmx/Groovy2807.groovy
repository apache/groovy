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

import groovy.transform.CompileStatic
import org.codehaus.groovy.runtime.wrappers.PojoWrapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

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
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import javax.tools.ToolProvider
import java.lang.reflect.Array
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Date as SqlDate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import java.util.function.Supplier

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertInstanceOf
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

class Groovy2807 {

    private final MBeanServer server = MBeanServerFactory.createMBeanServer()

    @AfterEach
    void tearDown() {
        MBeanServerFactory.releaseMBeanServer(server)
    }

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
        assert bean.num(Integer.valueOf(1)) == 'int'
        assert bean.num(Long.valueOf(1L)) == 'long'
        assert bean.num(Float.valueOf(1.5f)) == 'float'
        assert bean.num(Double.valueOf(1.5d)) == 'double:1.5'
        assert bean.num('text') == 'string'
        assert bean.num(new BigDecimal('1.5')) == 'double:1.5'
        assert bean.num(1.5) == 'double:1.5'

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

        // Dynamic Groovy does not widen Character to numeric primitives
        def charRig = recording('chars',
                RecordingMBean.op('ch', 'int'),
                RecordingMBean.op('ch', 'java.lang.Object'),
                RecordingMBean.op('chFallback', 'int'),
                RecordingMBean.op('chFallback', 'java.lang.String'))
        charRig.bean.ch(Character.valueOf((char) 'A'))
        assert charRig.recording.signature.toList() == ['java.lang.Object']
        charRig.bean.chFallback(Character.valueOf((char) 'A'))
        assert charRig.recording.signature.toList() == ['java.lang.String']
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

        // Multi-dimensional and covariant 2D arrays in source and JVM descriptor syntax
        def matrixRig = recording('matrix',
                RecordingMBean.op('mat', 'java.lang.String[][]'),
                RecordingMBean.op('mat', 'int[][]'),
                RecordingMBean.op('jvmMat', '[[Ljava.lang.String;'),
                RecordingMBean.op('jvmMat', '[[I'),
                RecordingMBean.op('cov2d', 'java.lang.Object[]'),
                RecordingMBean.op('cov2d', 'java.lang.Object[][]'),
                RecordingMBean.op('cov2d', 'java.lang.CharSequence[][]'))
        String[][] strMatrix = [['a'] as String[]] as String[][]
        int[][] intMatrix = [[1] as int[]] as int[][]
        matrixRig.bean.mat(strMatrix)
        assert matrixRig.recording.signature.toList() == ['java.lang.String[][]']
        matrixRig.bean.mat(intMatrix)
        assert matrixRig.recording.signature.toList() == ['int[][]']
        matrixRig.bean.jvmMat(strMatrix)
        assert matrixRig.recording.signature.toList() == ['[[Ljava.lang.String;']
        matrixRig.bean.jvmMat(intMatrix)
        assert matrixRig.recording.signature.toList() == ['[[I']
        matrixRig.bean.cov2d(strMatrix)
        assert matrixRig.recording.signature.toList() == ['java.lang.CharSequence[][]']
    }

    @Test
    // GROOVY-2807
    void subtypesAndExactWrappersBeatWiderOverloads() {
        def bean = overloaded()
        def direct = new Overloaded()
        assert bean.child(new OverloadedMBean.ChildNode()) == 'child'
        assert bean.child(new OverloadedMBean.BaseNode()) == 'base'
        assert bean.shape('x') == 'string'
        assert bean.shape(Integer.valueOf(1)) == 'object'
        // Dynamic Groovy prefers the more general reference overload when superclass depths differ
        assert bean.shape(null) == 'object'
        assert bean.shape(null) == direct.shape(null)
        assert bean.child(null) == 'base'
        assert bean.child(null) == direct.child(null)
        assert bean.seq(null) == 'charsequence'
        assert bean.seq(null) == direct.seq(null)
        // Explicit server().invoke targets a specific overload for null
        assert bean.server().invoke(bean.name(), 'shape', [null] as Object[], ['java.lang.String'] as String[]) == 'string'
        assert bean.wrap(Integer.valueOf(1)) == 'int'
        assert bean.wrap(new Object()) == 'object'
        assert bean.box(3) == 'Integer'
        assert bean.box(Integer.valueOf(3)) == 'Integer'
        assert bean.box(null) == 'Integer'
        assert bean.flag(Boolean.FALSE) == 'Boolean'
        assert bean.seq('hello') == 'string'
    }

    @Test
    // GROOVY-2807
    void groovyCoercionsAndNumericConversionsMatchDynamicDispatch() {
        def bean = overloaded()
        def text = "${'hello'}"
        assertInstanceOf(GString, text)
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

        // Coercing inside covariant Object[] arrays must not throw ArrayStoreException
        assert bean.invokeMethod('num', [new BigDecimal('1.5')] as BigDecimal[]) == 'double:1.5'
        assert bean.invokeMethod('label', [text] as GString[]) == 'string:hello'
        assert bean.invokeMethod('id', [Integer.valueOf(7)] as Integer[]) == 'long'

        // BigDecimal, BigInteger, Number, and SAM/Closure coercions
        def numRig = recording('nums',
                RecordingMBean.op('dec', 'float'),
                RecordingMBean.op('dec', 'double'),
                RecordingMBean.op('bigDec', 'java.math.BigDecimal'),
                RecordingMBean.op('bigDec', 'java.lang.String'),
                RecordingMBean.op('bigInt', 'java.math.BigInteger'),
                RecordingMBean.op('bigInt', 'java.lang.String'),
                RecordingMBean.op('number', 'int'),
                RecordingMBean.op('number', 'java.lang.Number'),
                RecordingMBean.op('sam', Supplier.name),
                RecordingMBean.op('sam', Consumer.name),
                RecordingMBean.op('sam', 'java.lang.String'),
                RecordingMBean.op('runOrConsume', Runnable.name),
                RecordingMBean.op('runOrConsume', Consumer.name))
        numRig.bean.dec(new BigDecimal('1.5'))
        assert numRig.recording.signature.toList() == ['double']
        assertInstanceOf(Double, numRig.recording.params[0])

        numRig.bean.bigDec(Double.valueOf(2.5d))
        assert numRig.recording.signature.toList() == ['java.math.BigDecimal']
        assertInstanceOf(BigDecimal, numRig.recording.params[0])

        numRig.bean.bigInt(Integer.valueOf(42))
        assert numRig.recording.signature.toList() == ['java.math.BigInteger']
        assertInstanceOf(BigInteger, numRig.recording.params[0])

        numRig.bean.number(new AtomicInteger(5))
        assert numRig.recording.signature.toList() == ['java.lang.Number']
        assertInstanceOf(AtomicInteger, numRig.recording.params[0])

        numRig.bean.sam({ -> 'ok' })
        assert numRig.recording.signature.toList() == [Supplier.name]
        assertInstanceOf(Supplier, numRig.recording.params[0])
        numRig.bean.sam({ Object x -> })
        assert numRig.recording.signature.toList() == [Consumer.name]
        assertInstanceOf(Consumer, numRig.recording.params[0])
        // Closure directly implements Runnable, so Runnable beats SAM Consumer in dynamic Groovy
        numRig.bean.runOrConsume({ Object x -> })
        assert numRig.recording.signature.toList() == [Runnable.name]
        assertInstanceOf(Runnable, numRig.recording.params[0])

        // Coercion failures respect ignoreErrors policy
        assertThrows(IllegalArgumentException) {
            bean.real(new BigDecimal('1e400'))
        }
        assertThrows(NumberFormatException) {
            numRig.bean.bigDec(Double.NaN)
        }
        def quiet = new GroovyMBean(server, bean.name(), true)
        assertNull(quiet.real(new BigDecimal('1e400')))
    }

    @Test
    // GROOVY-2807
    void directWrapperInvocationSelectsAndUnwraps() {
        def bean = overloaded()
        assert invokeWrapped(bean, 'shape', null, String) == 'string'
        assert invokeWrapped(bean, 'child', null, OverloadedMBean.ChildNode) == 'child'
        assert invokeWrapped(bean, 'seq', null, String) == 'string'
        assert invokeWrapped(bean, 'box', null, Integer) == 'Integer'
        assert invokeWrapped(bean, 'echo', null, String) == 'echo:null'

        def rig = recording('wrapped',
                RecordingMBean.op('mark', RecordingMBean.MarkerA.name),
                RecordingMBean.op('mark', RecordingMBean.MarkerB.name),
                RecordingMBean.op('solo', 'long'),
                RecordingMBean.op('solo', 'int'),
                RecordingMBean.op('odd', 'java.lang.String'),
                RecordingMBean.op('odd', 'java.net.URI'))
        def both = new RecordingMBean.BothMarkers()
        invokeWrapped(rig.bean as GroovyMBean, 'mark', both, RecordingMBean.MarkerA)
        assert rig.recording.signature.toList() == [RecordingMBean.MarkerA.name]
        assertSame(both, rig.recording.params[0])

        // Covariant PojoWrapper[] array unwraps cleanly without ArrayStoreException
        invokeWrappedArray(rig.bean as GroovyMBean, 'mark', both, RecordingMBean.MarkerA)
        assert rig.recording.signature.toList() == [RecordingMBean.MarkerA.name]
        assertSame(both, rig.recording.params[0])

        // A Wrapper holding null must not select a primitive parameter
        invokeWrapped(rig.bean as GroovyMBean, 'solo', null, Integer)
        assert rig.recording.signature.toList() == ['int']
        assertNull(rig.recording.params[0])

        // Fallback path also unwraps Wrappers before server.invoke
        invokeWrapped(rig.bean as GroovyMBean, 'odd', Integer.valueOf(2), Integer)
        assert rig.recording.signature.toList() == ['java.net.URI']
        assert rig.recording.params[0] == Integer.valueOf(2)
    }

    @Test
    // GROOVY-2807
    void singleCandidateKeepsThePreviousDispatch() {
        def bean = overloaded()
        assert bean.only(Integer.valueOf(4)) == 'only:4'
        assert bean.only(Short.valueOf((short) 4)) == 'only:4'
        assert bean.ping() == 'pong'
        assert bean.echo('hi') == 'echo:hi'
        assert bean.echo(null) == 'echo:null'
        // Methods defined on GroovyMBean itself delegate through super.invokeMethod
        assertSame(bean.name(), bean.invokeMethod('name', new Object[0]))
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
    void nullFollowsDynamicGroovyDistanceAndTieRules() {
        def rig = recording('nulls',
                RecordingMBean.op('shape', 'java.lang.String'),
                RecordingMBean.op('shape', 'java.lang.Object'),
                RecordingMBean.op('revShape', 'java.lang.Object'),
                RecordingMBean.op('revShape', 'java.lang.String'),
                RecordingMBean.op('colAsc', Collection.name),
                RecordingMBean.op('colAsc', List.name),
                RecordingMBean.op('colDesc', List.name),
                RecordingMBean.op('colDesc', Collection.name),
                RecordingMBean.op('arrAsc', 'java.lang.Object[]'),
                RecordingMBean.op('arrAsc', 'java.lang.String[]'),
                RecordingMBean.op('arrDesc', 'java.lang.String[]'),
                RecordingMBean.op('arrDesc', 'java.lang.Object[]'))

        // Different superclass distances: Object wins regardless of order
        rig.bean.shape(null)
        assert rig.recording.signature.toList() == ['java.lang.Object']
        assert rig.recording.params.length == 1 && rig.recording.params[0] == null
        rig.bean.revShape(null)
        assert rig.recording.signature.toList() == ['java.lang.Object']
        // Direct invokeMethod(name, null) wraps null as a 1-element array
        rig.bean.invokeMethod('shape', null)
        assert rig.recording.signature.toList() == ['java.lang.Object']

        // Equal-distance sub-interfaces and covariant arrays: strictly more specific wins
        rig.bean.colAsc(null)
        assert rig.recording.signature.toList() == [List.name]
        rig.bean.colDesc(null)
        assert rig.recording.signature.toList() == [List.name]
        rig.bean.arrAsc(null)
        assert rig.recording.signature.toList() == ['java.lang.String[]']
        rig.bean.arrDesc(null)
        assert rig.recording.signature.toList() == ['java.lang.String[]']

        rig.bean.shape('x')
        assert rig.recording.signature.toList() == ['java.lang.String']
        rig.bean.shape(Integer.valueOf(1))
        assert rig.recording.signature.toList() == ['java.lang.Object']
    }

    @Test
    // GROOVY-2807
    void tiedOverloadsPreferStrictlyMoreSpecificThenLastRegistered() {
        def rig = recording('ties',
                RecordingMBean.op('mark', RecordingMBean.MarkerA.name),
                RecordingMBean.op('mark', RecordingMBean.MarkerB.name),
                RecordingMBean.op('tri', RecordingMBean.MarkerA.name),
                RecordingMBean.op('tri', RecordingMBean.MarkerB.name),
                RecordingMBean.op('tri', RecordingMBean.BothMarkers.name),
                RecordingMBean.op('threeWay', 'java.lang.String'),
                RecordingMBean.op('threeWay', 'java.net.URI'),
                RecordingMBean.op('threeWay', 'java.io.File'),
                RecordingMBean.op('odd', 'java.lang.String'),
                RecordingMBean.op('odd', 'java.net.URI'),
                RecordingMBean.op('arrAsc', 'java.lang.CharSequence[]'),
                RecordingMBean.op('arrAsc', 'java.lang.Object[]'),
                RecordingMBean.op('arrDesc', 'java.lang.Object[]'),
                RecordingMBean.op('arrDesc', 'java.lang.CharSequence[]'),
                RecordingMBean.op('pairTie', 'java.lang.CharSequence[]', 'java.lang.Object[]'),
                RecordingMBean.op('pairTie', 'java.lang.Object[]', 'java.lang.CharSequence[]'),
                RecordingMBean.op('pairDom', 'java.lang.CharSequence[]', 'java.lang.Object[]'),
                RecordingMBean.op('pairDom', 'java.lang.CharSequence[]', 'java.lang.CharSequence[]'))

        // True tie keeps the last registered operation
        def both = new RecordingMBean.BothMarkers()
        rig.bean.mark(both)
        assert rig.recording.signature.toList() == [RecordingMBean.MarkerB.name]

        // A later strictly closer candidate clears an earlier tie
        rig.bean.tri(both)
        assert rig.recording.signature.toList() == [RecordingMBean.BothMarkers.name]

        // Three-way equal-distance tie keeps the last registered operation
        rig.bean.threeWay(null)
        assert rig.recording.signature.toList() == ['java.io.File']

        // Unmatched calls fall back to the last registered operation
        rig.bean.odd(Integer.valueOf(1))
        assert rig.recording.signature.toList() == ['java.net.URI']
        rig.bean.odd(null)
        assert rig.recording.signature.toList() == ['java.net.URI']

        // Equal-distance covariant array overloads pick the strictly more specific
        // signature regardless of registration order
        String[] strings = ['a'] as String[]
        rig.bean.arrAsc(strings)
        assert rig.recording.signature.toList() == ['java.lang.CharSequence[]']
        rig.bean.arrDesc(strings)
        assert rig.recording.signature.toList() == ['java.lang.CharSequence[]']

        // Two-parameter equal-distance tie: neither dominates -> last registered
        rig.bean.pairTie(strings, strings)
        assert rig.recording.signature.toList() == ['java.lang.Object[]', 'java.lang.CharSequence[]']

        // Two-parameter equal-distance tie: second strictly dominates first
        rig.bean.pairDom(strings, strings)
        assert rig.recording.signature.toList() == ['java.lang.CharSequence[]', 'java.lang.CharSequence[]']
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
    void arrayAndMalformedTypeNamesResolveOrSkipSafely() {
        String maxDepth = 'int' + ('[]' * 255)
        String tooDeep = 'int' + ('[]' * 256)
        def rig = recording('names',
                RecordingMBean.op('arr', 'java.lang.String[]'),
                RecordingMBean.op('arr', 'no.such.Type[]'),
                RecordingMBean.op('jvm', 'java.lang.Integer'),
                RecordingMBean.op('jvm', '[Ljava.lang.String;'),
                RecordingMBean.op('nums', 'int'),
                RecordingMBean.op('nums', '[J'),
                RecordingMBean.op('pick', 'java.lang.String'),
                RecordingMBean.op('pick', 'no.such.Type'),
                RecordingMBean.op('bad', (String) null),
                RecordingMBean.op('bad', ''),
                RecordingMBean.op('bad', '[]'),
                RecordingMBean.op('bad', '[I[]'),
                RecordingMBean.op('bad', 'void'),
                RecordingMBean.op('bad', 'void[]'),
                RecordingMBean.op('bad', '[V'),
                RecordingMBean.op('bad', '['),
                RecordingMBean.op('bad', '[Ljava.lang.String'),
                RecordingMBean.op('bad', maxDepth),
                RecordingMBean.op('bad', tooDeep),
                RecordingMBean.op('bad', 'java.lang.String'),
                RecordingMBean.op('platform', SqlDate.name),
                RecordingMBean.op('platform', 'java.lang.String'),
                RecordingMBean.op('gone', 'com.missing.Type'))

        rig.bean.arr(new String[] {'a'})
        assert rig.recording.signature.toList() == ['java.lang.String[]']
        // Second call exercises both positive and negative standard-loader caches
        rig.bean.arr(new String[] {'b'})
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
        rig.bean.bad('ok')
        assert rig.recording.signature.toList() == ['java.lang.String']
        rig.bean.platform(new SqlDate(0L))
        assert rig.recording.signature.toList() == [SqlDate.name]
        rig.bean.gone('x')
        assert rig.recording.signature.toList() == ['com.missing.Type']

        def probe = new Probe('p')
        rig.bean.pick(probe)
        assert rig.recording.signature.toList() == ['no.such.Type']
    }

    @Test
    // GROOVY-2807
    void classLoaderResolutionHandlesTcclFallbackShadowingAndIsolation(@TempDir Path tempDir) {
        def probe = new Probe('p')
        def visible = recording('visible',
                RecordingMBean.op('vis', 'java.lang.String'),
                RecordingMBean.op('vis', Probe.name),
                RecordingMBean.op('miss', 'no.such.Type'),
                RecordingMBean.op('miss', 'java.lang.String'))

        def previous = Thread.currentThread().contextClassLoader
        try {
            Thread.currentThread().contextClassLoader = null
            visible.bean.vis(probe)
            assert visible.recording.signature.toList() == [Probe.name]
            visible.bean.miss('ok')
            assert visible.recording.signature.toList() == ['java.lang.String']
            visible.bean.miss(probe)
            assert visible.recording.signature.toList() == ['java.lang.String']
        } finally {
            Thread.currentThread().contextClassLoader = previous
        }

        // Isolated custom ClassLoaders: exact type, subclass, array, multi-arg with null slot,
        // sibling loader shadowing, negative-miss bypass, LinkageError, and throwing loader
        Path classesDir1 = compilePhantomClasses(tempDir.resolve('cl1'))
        Path classesDir2 = compilePhantomClasses(tempDir.resolve('cl2'))
        Path brokenDir = compileBrokenClass(tempDir.resolve('broken'))

        try (def loader1 = new URLClassLoader([classesDir1.toUri().toURL()] as URL[], ClassLoader.platformClassLoader);
             def loader2 = new URLClassLoader([classesDir2.toUri().toURL()] as URL[], ClassLoader.platformClassLoader);
             def brokenLoader = new URLClassLoader([brokenDir.toUri().toURL()] as URL[], ClassLoader.platformClassLoader)) {
            Class<?> phantom1 = loader1.loadClass('jmxfix.Phantom')
            Class<?> subPhantom1 = loader1.loadClass('jmxfix.SubPhantom')
            Class<?> phantom2 = loader2.loadClass('jmxfix.Phantom')
            ClassLoader failingLoader = new FailingClassLoader()

            def hidden = recording('hidden',
                    RecordingMBean.op('hide', phantom1.name),
                    RecordingMBean.op('hide', 'java.lang.String'),
                    RecordingMBean.op('hideArr', "${phantom1.name}[]"),
                    RecordingMBean.op('hideArr', 'java.lang.String'),
                    RecordingMBean.op('link', phantom1.name, phantom1.name),
                    RecordingMBean.op('link', 'java.lang.Object', 'java.lang.Object'),
                    RecordingMBean.op('pairMiss', 'no.such.Type', 'java.lang.String'),
                    RecordingMBean.op('pairMiss', 'java.lang.Object', 'java.lang.Object'),
                    RecordingMBean.op('broken', 'jmxfix.Broken'),
                    RecordingMBean.op('broken', 'java.lang.String'))

            previous = Thread.currentThread().contextClassLoader
            try {
                // Prime negative standard-loader cache before a foreign-loader argument arrives
                hidden.bean.hide('prime')
                assert hidden.recording.signature.toList() == ['java.lang.String']

                Thread.currentThread().contextClassLoader = ClassLoader.platformClassLoader
                Object p1 = phantom1.getDeclaredConstructor().newInstance()
                Object p2 = phantom2.getDeclaredConstructor().newInstance()

                hidden.bean.hide(p1)
                assert hidden.recording.signature.toList() == [phantom1.name]

                // Subclass loaded by the same custom ClassLoader resolves its superclass parameter
                hidden.bean.hide(subPhantom1.getDeclaredConstructor().newInstance())
                assert hidden.recording.signature.toList() == [phantom1.name]

                // Array of custom ClassLoader type
                hidden.bean.hideArr(Array.newInstance(phantom1, 1))
                assert hidden.recording.signature.toList() == ["${phantom1.name}[]"]

                // Multi-parameter custom-ClassLoader operation where one slot is null
                hidden.bean.link(p1, null)
                assert hidden.recording.signature.toList() == [phantom1.name, phantom1.name]
                hidden.bean.link(null, p1)
                assert hidden.recording.signature.toList() == [phantom1.name, phantom1.name]
                // Other-arg fallback also handles missing types cleanly with mixed loaders
                hidden.bean.pairMiss(null, p1)
                assert hidden.recording.signature.toList() == ['java.lang.Object', 'java.lang.Object']
                hidden.bean.pairMiss('a', p1)
                assert hidden.recording.signature.toList() == ['java.lang.Object', 'java.lang.Object']
                hidden.bean.pairMiss(probe, p1)
                assert hidden.recording.signature.toList() == ['java.lang.Object', 'java.lang.Object']
                hidden.bean.pairMiss(null, probe)
                assert hidden.recording.signature.toList() == ['java.lang.Object', 'java.lang.Object']
                hidden.bean.pairMiss(p1, p1)
                assert hidden.recording.signature.toList() == ['java.lang.Object', 'java.lang.Object']
                hidden.bean.pairMiss(p1, 'a')
                assert hidden.recording.signature.toList() == ['java.lang.Object', 'java.lang.Object']
                hidden.bean.pairMiss(p1, null)
                assert hidden.recording.signature.toList() == ['java.lang.Object', 'java.lang.Object']
                hidden.bean.pairMiss(null, null)
                assert hidden.recording.signature.toList() == ['java.lang.Object', 'java.lang.Object']

                // Sibling ClassLoader shadowing: tccl=loader1 defines Phantom v1, arg is Phantom v2
                Thread.currentThread().contextClassLoader = loader1
                hidden.bean.hide(p2)
                assert hidden.recording.signature.toList() == [phantom2.name]
                hidden.bean.pairMiss(null, p1)
                assert hidden.recording.signature.toList() == ['java.lang.Object', 'java.lang.Object']
                hidden.bean.pairMiss(p1, null)
                assert hidden.recording.signature.toList() == ['java.lang.Object', 'java.lang.Object']

                // TCCL loading also avoids caching child-loader types
                hidden.bean.hide(p1)
                assert hidden.recording.signature.toList() == [phantom1.name]
                Thread.currentThread().contextClassLoader = loader2
                hidden.bean.hide('via-tccl')
                assert hidden.recording.signature.toList() == ['java.lang.String']
                hidden.bean.hide(p2)
                assert hidden.recording.signature.toList() == [phantom2.name]

                // LinkageError (missing superclass) is skipped cleanly
                Thread.currentThread().contextClassLoader = brokenLoader
                hidden.bean.broken('still-works')
                assert hidden.recording.signature.toList() == ['java.lang.String']

                // Throwing ClassLoader (e.g. stopped webapp loader throwing IllegalStateException)
                Thread.currentThread().contextClassLoader = failingLoader
                hidden.bean.hide('still-works')
                assert hidden.recording.signature.toList() == ['java.lang.String']
            } finally {
                Thread.currentThread().contextClassLoader = previous
            }
        }
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
        assertInstanceOf(Integer, rig.recording.params[0])
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
        assertNull(rig.recording.params[0])
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
        assertInstanceOf(IllegalStateException, runtime.cause)
        throwing.checked = true
        def checked = assertThrows(IllegalStateException) {
            bean.boom()
        }
        assert checked.message == 'checked'

        def quiet = new GroovyMBean(server, name.canonicalName, true)
        throwing.checked = true
        assertNull(quiet.boom())
        throwing.checked = false
        assertNull(quiet.boom())
    }

    @Test
    // GROOVY-2807
    void operationKeyOverrideStillGroupsAndFilters() {
        def counter = new AtomicInteger()
        CountingKeyMBean.COUNTER.set(counter)
        try {
            def name = new ObjectName('groovy.test:type=Counted')
            server.registerMBean(new Overloaded(), name)
            def counted = new CountingKeyMBean(server, name)
            int constructed = counter.get()
            assert constructed == counted.info().operations.length
            assert counted.ping() == 'pong'
            assert counter.get() == constructed + 1
        } finally {
            CountingKeyMBean.COUNTER.remove()
        }

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

        // Subclass returning null key and MBean operation with null name stay safe
        def nullKeyBean = new NullKeyMBean(server, mixedName)
        assert nullKeyBean.alpha('x') == 'alpha'

        def nullNameRec = new RecordingMBean(
                RecordingMBean.op(null, 'java.lang.String'),
                RecordingMBean.op('ok', 'java.lang.String'))
        def nullNameObj = new ObjectName('groovy.test:type=NullNameOp')
        server.registerMBean(nullNameRec, nullNameObj)
        def nullNameBean = new ConstantKeyMBean(server, nullNameObj)
        assert nullNameBean.ok('x') == 'ok'

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

    @Test
    // GROOVY-2807
    void concurrentOverloadInvocationsAreThreadSafe(@TempDir Path tempDir) {
        def bean = overloaded()
        Path classesDir = compilePhantomClasses(tempDir.resolve('concurrent'))
        try (def loader = new URLClassLoader([classesDir.toUri().toURL()] as URL[], ClassLoader.platformClassLoader)) {
            Class<?> phantom = loader.loadClass('jmxfix.Phantom')
            Object phantomInstance = phantom.getDeclaredConstructor().newInstance()
            def mixedName = new ObjectName('groovy.test:type=concurrentHidden')
            server.registerMBean(new SignatureReturningMBean(
                    RecordingMBean.op('op', phantom.name),
                    RecordingMBean.op('op', 'no.such.Type'),
                    RecordingMBean.op('op', 'java.lang.String')), mixedName)
            def mixedBean = new GroovyMBean(server, mixedName)

            int threads = 8
            int iterations = 100
            def start = new CountDownLatch(1)
            def executor = Executors.newFixedThreadPool(threads)
            try {
                def futures = (0..<threads).collect { int t ->
                    executor.submit({ ->
                        assertTrue(start.await(10, TimeUnit.SECONDS))
                        for (int i = 0; i < iterations; i++) {
                            assertEquals('string', bean.redeploy('http://example.com/' + t))
                            assertEquals('uri', bean.redeploy(URI.create('http://example.com/' + i)))
                            assertEquals('string-int:k:7', bean.pair('k', Short.valueOf((short) 7)))
                            assertEquals('int-string:3:v', bean.pair(Byte.valueOf((byte) 3), 'v'))
                            assertEquals('double:1.5', bean.num(new BigDecimal('1.5')))
                            assertEquals('java.lang.String', mixedBean.op('s'))
                            assertEquals(phantom.name, mixedBean.op(phantomInstance))
                            assertEquals('java.lang.String', mixedBean.op(Integer.valueOf(i)))
                        }
                    } as Runnable)
                }
                start.countDown()
                futures.each { it.get(30, TimeUnit.SECONDS) }
            } finally {
                executor.shutdownNow()
                assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS))
            }
        }
    }

    @CompileStatic
    private static Object invokeWrapped(GroovyMBean bean, String method, Object value, Class<?> type) {
        bean.invokeMethod(method, new PojoWrapper(value, type))
    }

    @CompileStatic
    private static Object invokeWrappedArray(GroovyMBean bean, String method, Object value, Class<?> type) {
        bean.invokeMethod(method, new PojoWrapper[]{new PojoWrapper(value, type)})
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

    private static Path compilePhantomClasses(Path dir) {
        Path source = dir.resolve('jmxfix')
        Files.createDirectories(source)
        File phantomFile = source.resolve('Phantom.java').toFile()
        phantomFile.text = 'package jmxfix; public class Phantom {}'
        File subFile = source.resolve('SubPhantom.java').toFile()
        subFile.text = 'package jmxfix; public class SubPhantom extends Phantom {}'
        compileJava(dir, [phantomFile, subFile])
        dir
    }

    private static Path compileBrokenClass(Path dir) {
        Path source = dir.resolve('jmxfix')
        Files.createDirectories(source)
        File baseFile = source.resolve('MissingBase.java').toFile()
        baseFile.text = 'package jmxfix; public class MissingBase {}'
        File brokenFile = source.resolve('Broken.java').toFile()
        brokenFile.text = 'package jmxfix; public class Broken extends MissingBase {}'
        compileJava(dir, [baseFile, brokenFile])
        // Delete superclass bytecode so loading jmxfix.Broken triggers NoClassDefFoundError (LinkageError)
        Files.delete(source.resolve('MissingBase.class'))
        dir
    }

    private static void compileJava(Path outDir, List<File> files) {
        def compiler = ToolProvider.systemJavaCompiler
        assert compiler != null
        def diagnostics = new DiagnosticCollector<JavaFileObject>()
        try (def fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
            def units = fileManager.getJavaFileObjectsFromFiles(files)
            def compiled = compiler.getTask(null, fileManager, diagnostics, ['-d', outDir.toString()], null, units).call()
            assert compiled : diagnostics.diagnostics.toString()
        }
    }

    private static final class Probe {
        final String label

        Probe(String label) {
            this.label = label
        }
    }

    private static final class FailingClassLoader extends ClassLoader {
        FailingClassLoader() {
            super(null)
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) {
            throw new IllegalStateException('loader closed')
        }
    }

    private static class ConstantKeyMBean extends GroovyMBean {

        ConstantKeyMBean(MBeanServerConnection server, ObjectName name) {
            super(server, name)
        }

        @Override
        protected String createOperationKey(String operation, int params) {
            'same'
        }
    }

    private static class NullKeyMBean extends GroovyMBean {

        NullKeyMBean(MBeanServerConnection server, ObjectName name) {
            super(server, name)
        }

        @Override
        protected String createOperationKey(String operation, int params) {
            null
        }
    }

    private static class ThrowingMBean implements DynamicMBean {

        boolean checked

        @Override
        Object getAttribute(String attribute) {
            null
        }

        @Override
        void setAttribute(Attribute attribute) {
            // Attributes are not used by this failure-injection MBean.
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

    private static class CountingKeyMBean extends GroovyMBean {

        static final ThreadLocal<AtomicInteger> COUNTER = new ThreadLocal<>()

        CountingKeyMBean(MBeanServerConnection server, ObjectName name) {
            super(server, name)
        }

        @Override
        protected String createOperationKey(String operation, int params) {
            COUNTER.get()?.incrementAndGet()
            super.createOperationKey(operation, params)
        }
    }

    private static class SignatureReturningMBean implements DynamicMBean {

        private final MBeanInfo info

        SignatureReturningMBean(MBeanOperationInfo... operations) {
            this.info = new MBeanInfo(SignatureReturningMBean.name, 'signature-returning', null, null, operations, null)
        }

        @Override
        Object getAttribute(String attribute) {
            null
        }

        @Override
        void setAttribute(Attribute attribute) {
            // Attributes are not used by this stateless concurrent test MBean.
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
            String.join(',', signature)
        }

        @Override
        MBeanInfo getMBeanInfo() {
            info
        }
    }
}

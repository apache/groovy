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

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.runtime.PackedClosure
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * A packed closure takes a single {@code List} argument as the closure class compiled from the
 * same literal does: the list itself where the signature accepts it, and otherwise its elements,
 * whatever the number of parameters. Each literal is compiled both ways and called with each
 * argument on each route; the two must give the same result or the same exception.
 */
final class Groovy12839 {

    /** Name to literal; the name starts with the kind of signature the cases below key on. */
    private static final Map<String, String> CLOSURES = [
        'one String'               : "{ String s -> 'S:' + s }",
        'one String, a capture'    : "{ String s -> cap + ':' + s }",
        'one String, two captures' : "{ String s -> cap + n + ':' + s }",
        'one String, written'      : "{ String s -> n += 1; cap + n + ':' + s }",
        'one Integer'              : "{ Integer i -> 'I:' + i }",
        'one Map'                  : "{ Map m -> 'M:' + m }",
        'one Runnable'             : "{ Runnable r -> 'R:' + (r != null) }",
        'one LinkedList'           : "{ LinkedList l -> 'LL:' + l }",
        'one Request'              : "{ Request request -> 'Q:' + request?.path }",
        'primitive int'            : "{ int i -> 'i:' + i }",
        'primitive long'           : "{ long l -> 'l:' + l }",
        'primitive double'         : "{ double d -> 'd:' + d }",
        'primitive char'           : "{ char c -> 'c:' + c }",
        'primitive boolean'        : "{ boolean z -> 'z:' + z }",
        'one Object'               : "{ Object o -> 'O:' + o }",
        'one untyped'              : "{ o -> 'U:' + o }",
        'one implicit'             : "{ 'it:' + it }",
        'one List'                 : "{ List l -> 'L:' + l }",
        'one Collection'           : "{ Collection c -> 'C:' + c }",
        'one Iterable'             : "{ Iterable i -> 'It:' + i }",
        'one ArrayList'            : "{ ArrayList l -> 'AL:' + l }",
        'one Serializable'         : "{ Serializable s -> 'Ser:' + s }",
        'array String[]'           : "{ String[] a -> 'SA:' + (a == null ? null : a.toList()) }",
        'array String...'          : "{ String... a -> 'SV:' + (a == null ? null : a.toList()) }",
        'array Object[]'           : "{ Object[] a -> 'OA:' + (a == null ? null : a.toList()) }",
        'array Object...'          : "{ Object... a -> 'OV:' + (a == null ? null : a.toList()) }",
        'array int[]'              : "{ int[] a -> 'IA:' + (a == null ? null : a.toList()) }",
        'array List[]'             : "{ List[] a -> 'LA:' + (a == null ? null : a.toList()) }",
        'none'                     : "{ -> 'none' }",
        'two untyped'              : "{ a, b -> 'AB:' + a + '|' + b }",
        'two String'               : "{ String a, String b -> 'SS:' + a + '|' + b }",
        'two int'                  : "{ int a, int b -> 'ii:' + a + '|' + b }",
        'three untyped'            : "{ a, b, c -> 'ABC:' + a + '|' + b + '|' + c }",
        'rest untyped, Object...'  : "{ a, Object... b -> 'AV:' + a + '|' + (b == null ? null : b.toList()) }",
        'rest List, Object...'     : "{ List a, Object... b -> 'LV:' + a + '|' + (b == null ? null : b.toList()) }",
        'rest String, Object...'   : "{ String a, Object... b -> 'StV:' + a + '|' + (b == null ? null : b.toList()) }",
        'rest String, String...'   : "{ String a, String... b -> 'SSV:' + a + '|' + (b == null ? null : b.toList()) }",
    ]

    /** A single {@code List} argument, by name; made anew for each call. */
    private static final Map<String, Closure<List>> LISTS = [
        "['four']"           : { ['four'] },
        '[]'                 : { [] },
        "['a', 'b']"         : { ['a', 'b'] },
        "['a', 'b', 'c']"    : { ['a', 'b', 'c'] },
        '[1]'                : { [1] },
        '[1, 2]'             : { [1, 2] },
        '[1L]'               : { [1L] },
        '[1.5G]'             : { [1.5G] },
        '[1.5d]'             : { [1.5d] },
        "[(char) 'a']"       : { [(char) 'a'] },
        '[true]'             : { [true] },
        '[null]'             : { [null] },
        '[1, null]'          : { [1, null] },
        "[['a']]"            : { [['a']] },
        "[['a'], 'b']"       : { [['a'], 'b'] },
        '[[k: 1]]'           : { [[k: 1]] },
        '[GString]'          : { ["g${1}"] },
        "['a', GString]"     : { ['a', "g${1}"] },
        "[String[]]"         : { [['a'] as String[]] },
        'LinkedList'         : { new LinkedList(['a']) },
        'unmodifiable'       : { Collections.unmodifiableList(['a']) },
        'Tuple0'             : { new Tuple0() },
        'Tuple1'             : { new Tuple1('a') },
        'Tuple2'             : { new Tuple2('a', 'b') },
    ]

    private static final List<String> ROUTES = ['callObject', 'callArray', 'dynamicCall', 'dynamicParentheses', 'dynamicDoCall']

    private static String source(String annotations) {
        int k = 0
        String literals = CLOSURES.collect { name, literal ->
            "Closure k${++k} = $literal; all.put('$name', k$k)"
        }.join('\n                    ')
        """
            class Request { String path }
            $annotations
            class Literals {
                Map<String, Closure> make() {
                    String cap = 'cap'
                    int n = 0
                    Map<String, Closure> all = [:]
                    $literals
                    all
                }
            }
            @groovy.transform.CompileStatic
            class Static {
                static Object callObject(Closure c, Object a) { c.call(a) }
                static Object callArray(Closure c, Object a) {
                    Object[] one = new Object[1]
                    one[0] = a
                    Object[] varargs = new Object[1]
                    varargs[0] = one
                    try {
                        Closure.getMethod('call', Object[].class).invoke(c, varargs)
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        throw e.cause
                    }
                }
            }
            class Dynamic {
                static Object dynamicCall(Closure c, Object a) { c.call(a) }
                static Object dynamicParentheses(Closure c, Object a) { c(a) }
                static Object dynamicDoCall(Closure c, Object a) { c.doCall(a) }
            }
            [new Literals().make(), Static, Dynamic, { String path -> new Request(path: path) }]
        """
    }

    private static List evaluate(String annotations, boolean flag) {
        String name = CompilerConfiguration.CLOSURE_PACKING
        String previous = System.getProperty(name)
        if (flag) System.setProperty(name, 'true') else System.clearProperty(name)
        try {
            (List) new GroovyShell().evaluate(source(annotations))
        } finally {
            if (previous != null) System.setProperty(name, previous) else System.clearProperty(name)
        }
    }

    /** The outcome of every case: the value returned, or the class of the exception thrown. */
    private static Map<String, String> outcomes(List compiled, Map<String, Closure> arguments, Closure<Boolean> applies) {
        def (Map<String, Closure> closures, Class statics, Class dynamics, Closure request) = compiled
        Map<String, String> outcomes = [:]
        closures.each { name, closure ->
            arguments.each { label, make ->
                if (!applies(name, label)) return
                ROUTES.each { route ->
                    String outcome
                    try {
                        Object argument = make.maximumNumberOfParameters == 1 ? make(request) : make()
                        outcome = String.valueOf((route.startsWith('call') ? statics : dynamics)."$route"(closure, argument))
                    } catch (Throwable t) {
                        outcome = t.class.name
                    }
                    outcomes["$name <- $label via $route".toString()] = outcome.replaceAll(/@[0-9a-f]+/, '@')
                }
            }
        }
        outcomes
    }

    private static void assertSameOutcomes(String mode, String how, Map<String, Closure> arguments, Closure<Boolean> applies = { name, label -> true }) {
        List unpacked = evaluate(mode, false)
        List packed = how == 'annotation' ? evaluate("$mode @groovy.transform.PackedClosures", false) : evaluate(mode, true)
        unpacked[0].each { name, closure -> assertFalse(closure instanceof PackedClosure, name) }
        packed[0].each { name, closure -> assertTrue(closure instanceof PackedClosure, name) }

        Map<String, String> expected = outcomes(unpacked, arguments, applies)
        Map<String, String> actual = outcomes(packed, arguments, applies)
        assertFalse(expected.isEmpty())
        List<String> differences = expected.findResults { key, value ->
            actual[key] == value ? null : "$key: closure class gives $value, packed closure gives ${actual[key]}".toString()
        }
        assertEquals([], differences)
    }

    @ParameterizedTest
    @CsvSource(["'',annotation", "'',property", '@groovy.transform.CompileStatic,annotation', '@groovy.transform.CompileStatic,property'])
    void testSingleListArgument(String mode, String how) {
        assertSameOutcomes(mode, how, LISTS)
    }

    // what a mock framework does with a response closure: the invocation's arguments as one List
    @ParameterizedTest
    @CsvSource(["'',annotation", "'',property", '@groovy.transform.CompileStatic,annotation', '@groovy.transform.CompileStatic,property'])
    void testOneElementListForOneTypedParameter(String mode, String how) {
        List packed = how == 'annotation' ? evaluate("$mode @groovy.transform.PackedClosures", false) : evaluate(mode, true)
        def (Map<String, Closure> closures, Class statics, Class dynamics, Closure request) = packed
        Closure response = closures['one Request']
        assertTrue(response instanceof PackedClosure)
        assertEquals('Q:/orders', response.call([request('/orders')]))
        assertEquals('Q:/orders', response.call(request('/orders')))
        assertEquals('S:four', closures['one String'].call(['four']))
        assertEquals('cap:four', closures['one String, a capture'].call(['four']))
        assertEquals('i:4', closures['primitive int'].call([4]))
        assertEquals('L:[four]', closures['one List'].call(['four']))
        assertEquals('O:[four]', closures['one Object'].call(['four']))
        assertEquals('it:[four]', closures['one implicit'].call(['four']))
    }

    // arguments that are not a List still bind as they did
    @ParameterizedTest
    @CsvSource(["'',annotation", "'',property", '@groovy.transform.CompileStatic,annotation', '@groovy.transform.CompileStatic,property'])
    void testSingleArgumentThatIsNoList(String mode, String how) {
        Map<String, Closure> scalars = [
            "'x'"     : { 'x' },
            '1'       : { 1 },
            'GString' : { "g${1}" },
            '[k: 1]'  : { [k: 1] },
            'Request' : { Closure request -> request('/x') },
        ]
        assertSameOutcomes(mode, how, scalars) { String name, String label -> !name.startsWith('array') }
    }

    // null binds to a parameter of a reference type and to a trailing array as the array; where
    // no signature accepts it, the two fail alike
    @ParameterizedTest
    @CsvSource(["'',annotation", "'',property", '@groovy.transform.CompileStatic,annotation', '@groovy.transform.CompileStatic,property'])
    void testSingleNullArgument(String mode, String how) {
        assertSameOutcomes(mode, how, [null: { null }])
    }

    // null among several arguments, where the lone-argument rules play no part
    @ParameterizedTest
    @CsvSource(["'',annotation", "'',property", '@groovy.transform.CompileStatic,annotation', '@groovy.transform.CompileStatic,property'])
    void testNullAmongSeveralArguments(String mode, String how) {
        List<Map<String, Closure>> both = [evaluate(mode, false)[0],
            (how == 'annotation' ? evaluate("$mode @groovy.transform.PackedClosures", false) : evaluate(mode, true))[0]]
        List<List<String>> outcomes = both.collect { Map<String, Closure> closures ->
            [{ closures['two int'].call(null, 1) }, { closures['two int'].call(1, null) }, { closures['two int'].call(1, 2) },
             { closures['two String'].call(null, null) }, { closures['two String'].call("g${1}", null) },
             { closures['rest String, Object...'].call(null, null) }, { closures['three untyped'].call(null, null, null) }].collect { Closure call ->
                try {
                    String.valueOf(call())
                } catch (Throwable t) {
                    t.class.name
                }
            }
        }
        assertEquals([MissingMethodException.name, MissingMethodException.name, 'ii:1|2', 'SS:null|null', 'SS:g1|null',
                      'StV:null|null', 'ABC:null|null|null'], outcomes[0])
        assertEquals(outcomes[0], outcomes[1])
    }
}

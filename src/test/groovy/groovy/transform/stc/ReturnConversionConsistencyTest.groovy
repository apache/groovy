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
package groovy.transform.stc

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * Checks that every way of returning a value to a primitive return type accepts and converts the
 * same values: a method's {@code return}, and a lambda, closure, method reference, method reference
 * to a generic method, method pointer and method pointer to a generic method implementing a functional interface,
 * and a {@code Closure} value passed or returned as a functional interface. The return of a
 * method is the reference; any other result must be listed in {@link #EXCEPTIONS} with its reason.
 * <p>
 * A difference found here usually means that one of these places has its own rule instead of
 * using the shared return check of {@link StaticTypeCheckingVisitor}.
 */
final class ReturnConversionConsistencyTest {

    /** source type, the expression of that type, and the boxed type for a type argument */
    private static final List<List<String>> SOURCES = [
        ['byte', '(byte) 65', 'Byte'],
        ['short', '(short) 65', 'Short'],
        ['int', '65', 'Integer'],
        ['long', '65L', 'Long'],
        ['float', '65.0f', 'Float'],
        ['double', '65.0d', 'Double'],
        ['char', "(char) 'A'", 'Character'],
        ['boolean', 'true', 'Boolean'],
        ['Integer', 'Integer.valueOf(65)', 'Integer'],
        ['Long', 'Long.valueOf(65L)', 'Long'],
        ['BigDecimal', '65.0G', 'BigDecimal'],
        ['BigInteger', '65G', 'BigInteger'],
        ['String', "'A'", 'String'],
    ]

    private static final List<String> TARGETS = ['int', 'long', 'double', 'boolean', 'char']

    private static final Map<String, Closure<List<String>>> FORMS = [
        'method'  : { String target, String boxed -> ["static $target m() { H.v() }", 'm()'] },
        'lambda'  : { String target, String boxed -> ['', 'S s = () -> H.v(); s.get()'] },
        'closure' : { String target, String boxed -> ['', 'S s = { -> H.v() }; s.get()'] },
        'mref'    : { String target, String boxed -> ['', 'S s = H::v; s.get()'] },
        'genmref' : { String target, String boxed -> ['', "G<$boxed> g = H::id; g.apply(H.v())"] },
        'mpointer': { String target, String boxed -> ['', 'S s = H.&v; s.get()'] },
        'genmpointer': { String target, String boxed -> ['', "G<$boxed> g = H.&id; g.apply(H.v())"] },
        'closurearg': { String target, String boxed -> ['static r(S s) { s.get() }', "Closure<$boxed> c = { -> H.v() }; r(c)"] },
        'closureret': { String target, String boxed -> ["static S r(Closure<$boxed> c) { c }", 'r({ -> H.v() }).get()'] },
    ]

    /**
     * Known, deliberate differences from a method's return, keyed by "form source->target", with
     * the reason. An entry that no longer differs is reported too, so this list stays current.
     */
    private static final Map<String, String> EXCEPTIONS = [
        'genmref int->char': 'the type argument is Integer, which a method cannot return as char either',
        'genmpointer int->char': 'the type argument is Integer, which a method cannot return as char either',
        'closurearg int->char': 'the type argument is Integer, which a method cannot return as char either',
        'closureret int->char': 'the type argument is Integer, which a method cannot return as char either',
    ]

    @ParameterizedTest
    @ValueSource(strings = ['TypeChecked', 'CompileStatic'])
    void testEveryReturnConvertsLikeAMethodReturn(String mode) {
        def table = new StringBuilder()
        def problems = []
        for (source in SOURCES) {
            for (target in TARGETS) {
                Map<String, String> outcomes = FORMS.collectEntries { form, code ->
                    [form, outcome(mode, source, target, *code(target, source[2]))]
                }
                String expected = outcomes.method
                table << String.format('%-10s -> %-7s', source[0], target)
                outcomes.each { form, result ->
                    table << String.format(' | %-8s %-14s', form, result)
                    String key = "$form ${source[0]}->$target"
                    boolean differs = result != expected
                    if (differs && !EXCEPTIONS.containsKey(key)) {
                        problems << "$key: $result, but a method's return gives $expected"
                    } else if (!differs && EXCEPTIONS.containsKey(key)) {
                        problems << "$key: listed as an exception (${EXCEPTIONS[key]}) but now agrees"
                    }
                }
                table << '\n'
            }
        }
        assert problems.isEmpty() : "@$mode:\n${problems.join('\n')}\n\n$table"
    }

    private static String outcome(String mode, List<String> source, String target, String member, String body) {
        String script = """
            import java.util.function.*
            interface S { $target get() }
            interface G<T> { $target apply(T t) }
            @groovy.transform.$mode
            class H {
                static ${source[0]} v() { ${source[1]} }
                static <T> T id(T t) { t }
            }
            @groovy.transform.$mode
            class Test {
                $member
                static run() { $body }
            }
            Test.run()
        """
        try {
            def result = new GroovyShell().evaluate(script)
            return "$result:${result.getClass().simpleName}"
        } catch (MultipleCompilationErrorsException ignore) {
            return 'error'
        } catch (Throwable t) {
            return t.getClass().simpleName
        }
    }
}

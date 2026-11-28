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

import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCall
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.messages.WarningMessage
import org.codehaus.groovy.transform.stc.AbstractTypeCheckingExtension
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * A closure, lambda or method reference that is typed by its context: the type that the call it is passed to is
 * assigned, returned, cast or passed to, the element type of a collection literal, or the other branch of a
 * conditional expression. Statically compiled code compiles a lambda or method reference as a native lambda
 * there, and a closure for a type parameter as one whose class implements the functional interface.
 *
 * @see FunctionalLiteralSiteTest
 */
final class FunctionalContextInferenceTest {

    private static final String IMPORTS = '''
        import groovy.transform.*
        import java.util.function.*
    '''

    private static final String HELPER = '''
        class K {
            static String kind(Object o) {
                if (o instanceof org.codehaus.groovy.runtime.MethodClosure) return o instanceof Function ? 'MC+I' : 'MC'
                if (o instanceof Closure) return o instanceof Function ? 'C+I' : 'C'
                if (java.lang.reflect.Proxy.isProxyClass(o.getClass())) return 'P'
                o instanceof Function ? 'L' : String.valueOf(o)
            }
            static <T> T id(T t) { t }
            static <T> T pick(T a, T b) { b }
            static Object take(Function<String, String> f) { f }
            static String trim(String s) { s.trim() }
        }
    '''

    private final GroovyShell shell = new GroovyShell()

    //--------------------------------------------------------------------------
    // the target type of a call

    @Test
    void testCallIsTypedByItsAssignmentReturnOrCast() {
        assertScript shell, IMPORTS + HELPER + '''
            @CompileStatic
            class T {
                static List<Object> assigned() {
                    Optional<Function<String, String>> lambda = Optional.of(s -> s.trim())
                    Optional<Function<String, String>> reference = Optional.of(String::trim)
                    Optional<Function<String, String>> closure = Optional.of { it.trim() }
                    List<Function<String, String>> array = Arrays.asList(s -> s.trim(), String::strip)
                    List<Function<String, String>> list = List.of((String s) -> s.trim())
                    Map<String, Function<String, String>> map = Map.of('k', String::trim)
                    assert lambda.get().apply(' a ') == 'a' && reference.get().apply(' b ') == 'b' && closure.get().apply(' c ') == 'c'
                    assert array[1].apply(' d ') == 'd' && list[0].apply(' e ') == 'e' && map.k.apply(' f ') == 'f'
                    [lambda.get(), reference.get(), closure.get(), array[0], array[1], list[0], map.k]
                }
                static Optional<Function<String, String>> returned() {
                    return Optional.of(s -> s.trim())
                }
                static Object cast() {
                    ((Optional<Function<String, String>>) Optional.of(String::trim)).get()
                }
            }

            assert T.assigned().collect { K.kind(it) } == ['L', 'L', 'C+I', 'L', 'L', 'L', 'L']
            assert K.kind(T.returned().get()) == 'L' && T.returned().get().apply(' a ') == 'a'
            assert K.kind(T.cast()) == 'L'
        '''
    }

    @Test
    void testCallIsTypedByTheParameterItIsPassedFor() {
        assertScript shell, IMPORTS + HELPER + '''
            @CompileStatic
            class T {
                static <T> T self(T t) { t } // an unqualified call of a static method
                static List<Object> test() {
                    Function<String, String> assigned = K.id(K.id(s -> s.trim()))
                    def nested = K.take(K.id(K.id(K.id(s -> s.trim()))))
                    def reference = K.take(K.id(String::trim))
                    def closure = K.take(K.id { it.trim() })
                    def unqualified = K.take(self(self((String s) -> s.trim())))
                    Optional<Function<String, String>> optional = Optional.of(K.id(String::strip))
                    List<Function<String, String>> list = []
                    list.add(K.id(s -> s.strip()))
                    def box = new Box<Function<String, String>>(K.id(s -> s.trim()))
                    [assigned, nested, reference, closure, unqualified, optional.get(), list[0], box.value]
                }
            }
            class Box<T> { T value; Box(T value) { this.value = value } }

            def values = T.test()
            assert values.collect { K.kind(it) } == ['L', 'L', 'L', 'C+I', 'L', 'L', 'L', 'L']
            assert values*.apply(' a ') == ['a'] * 8
        '''
    }

    @Test
    void testArgumentIsTypedByAnotherArgument() {
        assertScript shell, IMPORTS + HELPER + '''
            @CompileStatic
            class T {
                static List<String> test() {
                    Function<String, String> function = String::trim
                    def lambda = K.pick(function, s -> s.strip())
                    def reference = K.pick(function, String::strip)
                    def closure = K.pick(function) { it.strip() }
                    assert [lambda, reference, closure]*.apply(' a ') == ['a'] * 3 // each is a Function<String, String>
                    [K.kind(lambda), K.kind(reference), K.kind(closure)]
                }
            }

            assert T.test() == ['L', 'L', 'C+I']
        '''
    }

    @Test
    void testCallWithATargetTypeThatIsNotAFunctionalInterfaceIsCheckedAsBefore() {
        assertScript shell, IMPORTS + HELPER + '''
            @CompileStatic
            class T {
                static String closure(Closure<String> c) { c.call(' a ') }
                static String object(Object o) { K.kind(o) }
                static List<Object> test() {
                    def self = K.id { String s -> s.trim() }
                    [closure(K.id { String s -> s.trim() }), object(K.id((String s) -> s.trim())), object(Optional.of { 1 }.get()), K.kind(self)]
                }
            }

            assert T.test() == ['a', 'C', 'C', 'C']
        '''
    }

    //--------------------------------------------------------------------------
    // collection literals

    @Test
    void testElementOfACollectionLiteral() {
        assertScript shell, IMPORTS + HELPER + '''
            @CompileStatic
            class T {
                // a lambda with one parameter and no type is written in parentheses in a list
                static List<Function<String, String>> field = [(s -> s.trim()), String::strip]
                static List<Object> test(boolean flag) {
                    List<Function<String, String>> list = [(s -> s.trim()), String::strip, { it.trim() }, flag ? (s -> s) : String::trim]
                    Map<String, Function<String, String>> map = [lambda: (String s) -> s.trim(), reference: String::trim, closure: { String s -> s.trim() }]
                    List<List<Function<String, String>>> nested = [[(s -> s.trim())], [String::trim]]
                    List<? extends Function<String, String>> wildcard = [(s -> s.trim())]
                    Set<Function<String, String>> set = [String::trim]
                    Collection<Function<String, String>> call = [K.id((s -> s.trim()))]
                    assert list[0].apply(' a ') == 'a' && map.reference.apply(' b ') == 'b' && nested[1][0].apply(' c ') == 'c'
                    [list[0], list[1], list[2], list[3], map.lambda, map.reference, map.closure, nested[0][0], nested[1][0],
                     wildcard[0], set.first(), call.first(), field[0], field[1]]
                }
            }

            assert T.test(true).collect { K.kind(it) } == ['L', 'L', 'C+I', 'L', 'L', 'L', 'C+I', 'L', 'L', 'L', 'L', 'L', 'L', 'L']
        '''
    }

    @Test
    void testValueOfTypeClosureInACollectionLiteralIsRejectedAsBefore() {
        for (value in ['closure', 'K.&trim']) {
            def err = shouldFail shell, IMPORTS + HELPER + """
                @CompileStatic
                void test(Closure<String> closure) {
                    List<Function<String, String>> list = [$value]
                }
            """
            assert err.message.contains('Incompatible generic argument types')
        }
    }

    //--------------------------------------------------------------------------
    // conditional expressions

    @Test
    void testConditionalArgumentWithABranchThatIsAValue() {
        assertScript shell, IMPORTS + HELPER + '''
            @CompileStatic
            class T {
                static List<Object> test(boolean flag, Function<String, String> function) {
                    List<Function<String, String>> list = []
                    list.add(function ?: (s -> s.trim()))
                    list.add(flag ? function : String::strip)
                    list.add(flag ? { it.trim() } : function)
                    list << (flag ? function : (s -> s.trim()))
                    list.add(flag ? null : (s -> s.trim()))
                    def taken = K.take(flag ? function : (s -> s.trim()))
                    list.findAll().each { assert it.apply(' a ') == 'a' }
                    List<Object> values = []
                    values.addAll(list)
                    values.add(taken)
                    values
                }
            }

            Function<String, String> function = { String s -> s.trim() }
            assert T.test(true, function).collect { it == null ? null : it.is(function) ? 'F' : K.kind(it) } == ['F', 'F', 'C+I', 'F', null, 'F']
            assert T.test(false, function).collect { it.is(function) ? 'F' : K.kind(it) } == ['F', 'L', 'F', 'L', 'L', 'L']
            assert T.test(false, null).collect { it == null ? null : K.kind(it) } == ['L', 'L', null, 'L', 'L', 'L']
        '''
    }

    @Test
    void testConditionalArgumentChoosesTheMethodByItsValueBranch() {
        assertScript shell, IMPORTS + '''
            @CompileStatic
            class T {
                static String m(Function<String, String> f) { 'function' }
                static String m(Supplier<String> s) { 'supplier' }
                static String n(Closure<String> c) { 'closure' }
                static String n(Function<String, String> f) { 'function' }
                static List<String> test(boolean flag, Function<String, String> function, Supplier<String> supplier, Closure<String> closure) {
                    [m(flag ? function : (s -> s)), m(flag ? supplier : (() -> 'x')), n(closure ?: { String s -> s }), n(function ?: (s -> s))]
                }
            }

            assert T.test(true, null, null, null) == ['function', 'supplier', 'closure', 'function']
        '''
    }

    @Test
    void testInstanceofRestrictionInADeferredBranch() {
        assertScript shell, IMPORTS + '''
            @CompileStatic
            class T {
                static Supplier<Integer> test(Object o) {
                    List<Supplier<Integer>> list = []
                    list.add(o instanceof String ? (() -> o.length()) : (() -> -1))
                    list.add(o instanceof String ? { -> o.length() * 2 } : null)
                    list[0]
                }
            }

            assert T.test('abc').get() == 3
            assert T.test(42).get() == -1
        '''
    }

    //--------------------------------------------------------------------------
    // the choice of the method

    @Test
    void testCallThatAwaitsItsTargetTypeSelectsTheMethodAsItsArgumentWould() {
        assertScript shell, IMPORTS + HELPER + '''
            @CompileStatic
            class T {
                static String m(Function<String, String> f) { 'function ' + f.apply(' a ') }
                static String m(Integer i) { 'integer' }
                static String o(Optional<Function<String, String>> f) { 'optional ' + f.get().apply(' b ') }
                static String o(String s) { 'string' }
                static String r(Runnable r) { 'runnable' }
                static String r(Supplier<String> s) { 'supplier' }
                static String q(Runnable r) { 'runnable' }
                static String q(Function<String, String> f) { 'function' }
                static List<Object> test() {
                    [m(K.id(s -> s.trim())), o(Optional.of(String::trim)),
                     // the method that the lambda or method reference selects as an argument itself, as before
                     r(K.id(() -> 'x')), r(() -> 'x'), q(K.id((String s) -> s)), q((String s) -> s), q(K.id(K.id(String::trim))), q(String::trim),
                     // no target type: a closure, as before
                     println(K.id { 1 }) == null]
                }
            }

            assert T.test() == ['function a', 'optional b', 'runnable', 'runnable', 'function', 'function', 'function', 'function', true]
        '''
    }

    @Test
    void testValueOfTypeClosureIsAWitness() {
        // T is not Supplier<String> for the closure: the common supertype of the arguments, as before
        for (mode in ['@CompileStatic', '@TypeChecked']) {
            def err = shouldFail shell, IMPORTS + """
                $mode
                class T {
                    static <T> T first(T a, T b) { a }
                    static String test(Closure<String> closure, Supplier<String> supplier) {
                        first(closure, supplier).get()
                    }
                }
            """
            assert err.message.contains('Cannot find matching method java.lang.Object#get()')
        }

        def err = shouldFail shell, IMPORTS + '''
            @CompileStatic
            void test(Closure<String> closure) {
                Optional<Function<String, String>> optional = Optional.of(closure)
            }
        '''
        assert err.message.contains('Incompatible generic argument types')
    }

    //--------------------------------------------------------------------------
    // what a lambda or method reference is compiled for

    @Test
    void testOtherBoundsOfATypeParameter() {
        assertScript shell, IMPORTS + '''
            interface Marker { }
            @CompileStatic
            class T {
                static <T extends Function<String, String> & Serializable> T first(T t) { t }
                static <T extends Serializable & Function<String, String>> Object second(T t) { t }
                static <T extends Function<String, String> & Marker & Serializable> T both(T t) { t }
                static List<Object> test() {
                    [first(s -> s.trim()), first(String::trim), second(s -> s.trim()), second(String::trim), both((String s) -> s.trim())]
                }
            }

            def values = T.test()
            assert values.every { it instanceof Function && it instanceof Serializable && !(it instanceof Closure) }
            assert values*.apply(' a ') == ['a'] * 5
            assert values[4] instanceof Marker
            // serializable, as a lambda cast to an intersection type is
            def bytes = new ByteArrayOutputStream()
            new ObjectOutputStream(bytes).withCloseable { it.writeObject(values[0]) }
        '''

        // a closure is converted to the first bound, the erasure of the type parameter, as before
        assertScript shell, IMPORTS + '''
            interface Marker { }
            @CompileStatic
            class T {
                static <T extends Function<String, String> & Marker> Object marked(T t) { t }
                static <T extends Function<String, String>> Object bound(T t) { t }
                static List<Object> test() {
                    [marked { String s -> s.trim() }, marked { it.trim() }, bound { it.trim() }]
                }
            }

            def values = T.test()
            assert values.every { it instanceof Function && !(it instanceof Closure) && !(it instanceof Marker) }
            assert values*.apply(' a ') == ['a'] * 3
        '''

        def err = shouldFail shell, IMPORTS + '''
            @CompileStatic
            class T {
                static <T extends Function<String, String> & Runnable> T m(T t) { t }
                static void test() { m(s -> s.trim()) }
            }
        '''
        assert err.message.contains('java.lang.Runnable is a functional interface too')
    }

    @Test
    void testMethodWithTypeParametersTakesAMethodReferenceOnly() {
        String types = '''
            interface Generic { def <T> T apply(T t) }
            class U { static <T> T id(T t) { t } }
        '''
        assertScript shell, IMPORTS + types + '''
            @CompileStatic
            List<Generic> test() {
                List<Generic> list = []
                list.add(U::id)
                list << U::id
                list
            }

            assert test().every { !(it instanceof Closure) }
            assert test()*.apply('x') == ['x', 'x']
        '''

        for (statement in ['list.add((Object o) -> o)', 'list << (o -> o)', 'List<Generic> other = [(o -> o)]']) {
            def err = shouldFail shell, IMPORTS + types + """
                @CompileStatic
                void test() {
                    List<Generic> list = []
                    $statement
                }
            """
            assert err.message.contains('A lambda cannot implement the method apply of Generic, which declares type parameters') : statement
        }
    }

    @Test
    void testLambdaOrMethodReferenceThatCannotBeCompiledForItsTargetIsAnError() {
        String types = '''
            abstract class Task { abstract String exec(String s) }
            sealed interface Shout permits Loud { String shout(String s) }
            final class Loud implements Shout { String shout(String s) { s.toUpperCase() } }
        '''
        Map<String, String> errors = [
            'List<Task> list = []; list.add((String s) -> s)'    : 'The lambda cannot be compiled for Task, which is not an interface',
            'List<Task> list = []; list << String::trim'         : 'The method reference cannot be compiled for Task, which is not an interface',
            'List<Shout> list = []; list.add((String s) -> s)'   : 'The lambda cannot be compiled for Shout, which is a sealed interface',
            'List<Shout> list = [(String s) -> s]'               : 'The lambda cannot be compiled for Shout, which is a sealed interface',
            'List<Function<String, String>> list = []; list.add((a, b) -> a)'         : 'Wrong number of parameters for method target: apply(java.lang.String)',
            'List<Function<String, String>> list = []; list << (() -> "x")'           : 'Wrong number of parameters for method target: apply(java.lang.String)',
            'List<Function<Object, String>> list = []; list.add((String s) -> s)'     : 'Expected type java.lang.Object for lambda parameter: s',
            'List<Function<Object, String>> list = [(String s) -> s]'                 : 'Expected type java.lang.Object for lambda parameter: s',
            'List<Function> list = []; list.add((String s) -> s.trim())'              : 'Expected type java.lang.Object for lambda parameter: s', // a raw type
            'List<Function<String, String>> list = []; list += ((String s) -> s)'     : 'was not given that type'
        ]
        errors.each { statement, message ->
            def unit = new CompilationUnit(new CompilerConfiguration(warningLevel: WarningMessage.LIKELY_ERRORS))
            unit.addSource('E.groovy', IMPORTS + types + """
                @CompileStatic
                void test() {
                    $statement
                }
            """)
            def err = shouldFail { unit.compile(Phases.CLASS_GENERATION) }
            assert err.message.contains(message) : statement
            assert !unit.errorCollector.warnings : statement // an error, not a warning that it is stored as a closure
        }
    }

    @Test
    void testRawTarget() {
        assertScript shell, IMPORTS + HELPER + '''
            @CompileStatic
            List<Function> test() {
                List<Function> list = []
                list.add(o -> o.toString().trim())
                list << String::valueOf
                list.add { String s -> s.trim() } // an unchecked conversion of the argument, as before
                list
            }

            assert test().collect { K.kind(it) } == ['L', 'L', 'C+I']
            assert test()*.apply(' a ') == ['a', ' a ', 'a']
        '''
    }

    //--------------------------------------------------------------------------
    // regressions

    @Test
    void testCallThatAwaitsItsTargetTypeInAMapPassedToAConstructor() {
        String source = IMPORTS + '''
            class Foo { Supplier<Integer> name }
            class Bar { Map map; Bar(Map map) { this.map = map } }
            @CompileStatic
            class T {
                static List<Object> test(String s) {
                    def foo = new Foo(Map.of('name', { -> s.length() + 1 }))
                    def bar = new Bar(Map.of('name', () -> s.length() + 2))
                    def conditional = new Bar(s ? Map.of('name', { -> s.length() + 3 }) : null)
                    [foo.name.get(), bar.map.name, conditional.map.name]
                }
            }
        '''
        assertScript shell, source + '''
            def (foo, bar, conditional) = T.test('abc')
            assert foo == 4 && bar() == 5 && conditional() == 6
        '''

        def err = shouldFail shell, IMPORTS + '''
            class Foo { Supplier<Integer> name }
            @CompileStatic
            void test(String s) {
                new Foo(Map.of('name', { -> s.nonexistent() }))
            }
        '''
        assert err.message.contains('Cannot find matching method java.lang.String#nonexistent()')
        assert err.message.contains('1 error')
    }

    @Test
    void testErrorInAnArgumentTypedByItsContextIsReportedOnce() {
        Map<String, String> statements = [
            'nested call'       : 'K.take(K.id(K.id(s -> s.nonexistent())))',
            'assigned call'     : 'Optional<Function<String, String>> o = Optional.of(s -> s.nonexistent())',
            'list literal'      : 'List<Function<String, String>> list = [(s -> s.nonexistent())]',
            'conditional'       : 'List<Function<String, String>> list = []; list.add(function ?: (s -> s.nonexistent()))',
            'receiver of a call': 'K.take(K.id(s -> s.trim())).nonexistent()'
        ]
        statements.each { name, statement ->
            def err = shouldFail shell, IMPORTS + HELPER + """
                @CompileStatic
                void test(Function<String, String> function) {
                    $statement
                }
            """
            assert err.message.contains(name == 'receiver of a call' ? 'Cannot find matching method java.lang.Object#nonexistent()'
                    : 'Cannot find matching method java.lang.String#nonexistent()') : name
            assert err.message.contains('1 error') : name
        }
    }

    static class CallbackLog extends AbstractTypeCheckingExtension {
        static final List<String> LOG = []
        CallbackLog(StaticTypeCheckingVisitor visitor) { super(visitor) }
        @Override boolean beforeMethodCall(MethodCall call) { log('before', call.methodAsString); false }
        @Override void afterMethodCall(MethodCall call) { log('after', call.methodAsString) }
        @Override void onMethodSelection(Expression expression, MethodNode target) {
            // an argument that awaits its target type has no type yet; no other type is a placeholder
            if (expression instanceof MethodCall && target.name == 'take') {
                assert expression.arguments.expressions.every { typeCheckingVisitor.getType(it) != null }
            }
            log('select', target.name)
        }
        private static void log(String event, String name) { if (name in ['id', 'take', 'of']) LOG << "$event $name".toString() }
    }

    @Test
    void testExtensionCallbacksFireOnceForACallThatAwaitsItsTargetType() {
        CallbackLog.LOG.clear()
        assertScript shell, IMPORTS + HELPER + """
            @CompileStatic(extensions = '${CallbackLog.name}')
            class T {
                static Object test() {
                    Optional<Function<String, String>> optional = Optional.of(K.id(String::trim))
                    K.take(K.id(s -> s.trim()))
                }
            }

            assert T.test().apply(' a ') == 'a'
        """
        assert CallbackLog.LOG == [
            'before of', 'before id', 'select id', 'select of', 'after id', 'after of',
            'before take', 'before id', 'select id', 'select take', 'after id', 'after take'
        ]

        // a method reference typed through each call it is passed to
        CallbackLog.LOG.clear()
        assertScript shell, IMPORTS + HELPER + """
            @CompileStatic(extensions = '${CallbackLog.name}')
            class T {
                static List<Object> test() {
                    Function<String, String> assigned = K.id(K.id(String::trim))
                    [assigned, K.take(K.id(K.id(String::trim)))]
                }
            }

            assert T.test()*.apply(' a ') == ['a', 'a']
        """
        assert CallbackLog.LOG == [
            'before id', 'before id', 'select id', 'select id', 'after id', 'after id',
            'before take', 'before id', 'before id', 'select id', 'select id', 'select take', 'after id', 'after id', 'after take'
        ]
    }

    @Test
    void testDelegationMetadataOfAClosureInAPendingCallOrConditional() {
        assertScript shell, IMPORTS + HELPER + '''
            class Builder { String name() { 'builder' } }
            @CompileStatic
            class T {
                static String with(@DelegatesTo(value = Builder, strategy = Closure.DELEGATE_FIRST) Closure<String> c) {
                    c.delegate = new Builder(); c.resolveStrategy = Closure.DELEGATE_FIRST; c.call()
                }
                static String name() { 'owner' }
                static List<String> test(boolean flag) {
                    List<Supplier<String>> list = []
                    list.add(flag ? { -> name() } : { -> name() + '!' }) // not the delegate of the call below
                    list.add(K.id { -> name() })
                    [with { name() }, list[0].get(), list[1].get(), with { def s = K.id { -> name() }; s.call() }]
                }
            }

            assert T.test(true) == ['builder', 'owner', 'owner', 'builder']
        '''
    }

    @Test
    void testLambdaCannotReachTheDelegateOfAnEnclosingClosure() {
        // a closure reaches it through its owner; a native lambda has none, and failed at run time with a ClassCastException
        for (statement in ['add(() -> size())', 'it << (() -> size())', 'add(K.id(() -> size()))', 'take(() -> size())']) {
            def err = shouldFail shell, IMPORTS + HELPER + """
                @CompileStatic
                class T {
                    static Object take(Supplier<Integer> s) { s.get() }
                    static void test(List<Supplier<Integer>> list) {
                        list.with { $statement }
                    }
                }
            """
            assert err.message.contains('The method size is called on the owner.delegate of an enclosing closure, which a lambda compiled for '
                    + 'java.util.function.Supplier<java.lang.Integer> cannot reach') : statement
        }

        assertScript shell, IMPORTS + '''
            @CompileStatic
            List<Integer> test(List<Supplier<Integer>> list) {
                list.with {
                    add(() -> it.size())    // on its object
                    add { -> size() + 10 }  // a closure
                }
                list*.get()
            }

            assert test([]) == [2, 12]
        '''
    }

    //--------------------------------------------------------------------------
    // type-checked code

    @Test
    void testTypeCheckedCodeStoresClosuresAndLambdasThatImplementTheInterface() {
        assertScript shell, IMPORTS + HELPER + '''
            @TypeChecked
            class T {
                static List<Object> test(boolean flag) {
                    List<Function<String, String>> list = []
                    list.add { it.trim() }
                    list.add(s -> s.trim())
                    list << ((String s) -> s.trim())
                    list.add(flag ? (s -> s.trim()) : { String s -> s.strip() })
                    list[0] = (s -> s.trim())
                    Optional<Function<String, String>> optional = Optional.of(s -> s.trim())
                    List<Function<String, String>> literal = [(s -> s.trim())]
                    List<Object> values = [optional.get(), literal[0]]
                    values.addAll(0, list)
                    values
                }
            }

            def values = T.test(true)
            assert values.collect { K.kind(it) } == ['C+I'] * 6
            assert values*.apply(' a ') == ['a'] * 6 // not a MissingMethodException
            assert values.every { it(' a ') == 'a' }
        '''
    }

    @Test
    void testTypeCheckedCodeStoresAMethodReferenceForATypeParameterAsAMethodClosure() {
        Map<String, String> rejected = [
            'list.add(String::trim)'                            : "Argument is a method reference, but parameter type 'E' is not a functional interface",
            'Optional.of(String::trim).get().apply(" a ")'      : "Argument is a method reference, but parameter type 'T' is not a functional interface",
            'Optional<Function<String, String>> o = Optional.of(String::trim)': "Argument is a method reference, but parameter type 'T' is not a functional interface",
            'List<Function<String, String>> other = [String::trim]': 'Incompatible generic argument types'
        ]
        rejected.each { statement, message ->
            def err = shouldFail shell, IMPORTS + """
                @TypeChecked
                void test(boolean flag) {
                    List<Function<String, String>> list = [null]
                    $statement
                }
            """
            assert err.message.contains(message) : statement
        }

        // an operand, a subscript and a branch of a conditional argument are method closures, as before, with a warning
        String source = IMPORTS + '''
            @TypeChecked
            class T {
                static List<Function<String, String>> test(boolean flag) {
                    List<Function<String, String>> list = [null]
                    list << String::trim
                    list[0] = String::strip
                    list.add(flag ? String::trim : String::strip)
                    list
                }
            }
        '''
        def unit = new CompilationUnit(new CompilerConfiguration(warningLevel: WarningMessage.LIKELY_ERRORS))
        unit.addSource('T.groovy', source)
        unit.compile(Phases.CLASS_GENERATION)
        String function = 'java.util.function.Function<java.lang.String, java.lang.String>'
        assert unit.errorCollector.warnings*.message == ["the closure is stored as a Closure, not as $function: " +
                "a statically compiled read through $function will fail; write 'as $function' to convert it".toString()] * 4
        int first = source.readLines().findIndexOf { it.contains('list << String::trim') } + 1
        assert unit.errorCollector.warnings*.context*.startLine == [first, first + 1, first + 2, first + 2]

        assertScript shell, source + HELPER + '''
            def values = T.test(true)
            assert values.collect { K.kind(it) } == ['MC'] * 3
            assert values.collect { it(' a ') } == ['a'] * 3
        '''

        // a declared target converts the method closure, as before
        assertScript shell, IMPORTS + '''
            @TypeChecked
            String test() {
                Function<String, String> function = String::trim
                function.apply(' a ')
            }

            assert test() == 'a'
        '''
    }

    //--------------------------------------------------------------------------
    // closure literals with a declared target

    @Test
    void testClosureWithADeclaredTargetIsConvertedAsBefore() {
        for (mode in ['@CompileStatic', '@TypeChecked']) {
            assertScript shell, IMPORTS + HELPER + """
                $mode
                class T {
                    String name = 'field'
                    static Supplier<String> take(Supplier<String> s) { s }
                    Supplier<String> returned() { return { -> name } }
                    List<Object> test() {
                        Supplier<String> assigned = { 'assigned' }
                        Supplier<String> cast = (Supplier<String>) { 'cast' }
                        def coerced = { 'coerced' } as Supplier<String>
                        Comparator<String> comparator = { String a, String b -> a.length() <=> b.length() }
                        assert ['ccc', 'a', 'bb'].toSorted(comparator) == ['a', 'bb', 'ccc']
                        [assigned, take { 'passed' }, returned(), cast, coerced, comparator]
                    }
                }

                def values = new T().test()
                // a proxy for the closure, not the closure
                assert values.every { !(it instanceof Closure) && java.lang.reflect.Proxy.isProxyClass(it.getClass()) }
                assert values[0..4]*.get() == ['assigned', 'passed', 'field', 'cast', 'coerced']
                assert values[5] instanceof Comparator && values[5].compare('a', 'bb') == -1
            """
        }
    }

    //--------------------------------------------------------------------------
    // the report

    private static final String REPORTED = IMPORTS + HELPER + '''
        class Named<T> { T value }
        @CompileStatic
        class T {
            static Object test(boolean flag) {
                List<Function<String, String>> list = []
                list.add(s -> s.trim())
                list << String::trim
                list.add(flag ? (s -> s) : String::strip)
                def named = new Named<Function<String, String>>(value: s -> s)
                Optional<Function<String, String>> optional = Optional.of(s -> s.trim())
                K.take(K.id(s -> s))
                // compiled as in Groovy 6
                list.add(String::trim)
                list[0] = (s -> s.trim())
                Function<String, String> declared = (s -> s.trim())
                K.take(String::trim)
                Function<String, String> closure = { it.trim() }
                list.add { it.trim() }
            }
        }
    '''

    private static List<String> report(String value, String source = REPORTED, List<byte[]> classes = null) {
        String previous = System.getProperty(CompilerConfiguration.FUNCTIONAL_REPORT)
        if (value != null) System.setProperty(CompilerConfiguration.FUNCTIONAL_REPORT, value) else System.clearProperty(CompilerConfiguration.FUNCTIONAL_REPORT)
        try {
            def unit = new CompilationUnit(new CompilerConfiguration(warningLevel: WarningMessage.LIKELY_ERRORS))
            unit.addSource('R.groovy', source)
            unit.compile(Phases.CLASS_GENERATION)
            classes?.addAll(unit.classes.sort { it.name }*.bytes)
            (unit.errorCollector.warnings ?: []).collect { "${it.context.startLine}: ${it.message}".toString() }
        } finally {
            if (previous != null) System.setProperty(CompilerConfiguration.FUNCTIONAL_REPORT, previous) else System.clearProperty(CompilerConfiguration.FUNCTIONAL_REPORT)
        }
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testReportOfWhatIsCompiledDifferentlyThanInGroovy6() {
        int first = REPORTED.readLines().findIndexOf { it.contains('list.add(s -> s.trim())') } + 1
        String function = 'java.util.function.Function<java.lang.String, java.lang.String>'
        def lines = report('true')
        assert lines.every { it.contains('[functional report] ') }
        assert lines.collect { it.replace(function, 'F').replaceAll(/;.*/, '').replace('[functional report] ', '') } == [
            "$first: lambda as an argument for a type parameter: a native lambda for F, was a closure in Groovy 6",
            "${first + 1}: method reference as an operand for a type parameter: a native lambda for F, was a method closure in Groovy 6",
            "${first + 2}: lambda as a branch of a conditional argument: a native lambda for F, was a closure in Groovy 6",
            "${first + 2}: method reference as a branch of a conditional argument: a native lambda for F, was a method closure in Groovy 6",
            "${first + 3}: lambda as a named argument: a native lambda for F, was a closure in Groovy 6",
            "${first + 4}: call: the type parameter T of of is F, from the type the call's value is assigned, passed, returned or cast to",
            "${first + 4}: lambda as an argument for a type parameter: a native lambda for F, was a closure in Groovy 6",
            "${first + 5}: call: its type arguments are inferred with F, the type of the parameter it is passed for",
            "${first + 5}: lambda as an argument for a type parameter: a native lambda for F, was a closure in Groovy 6"
        ].collect { it.toString() }
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testNoReportWithoutTheOption() {
        assert report(null).isEmpty()
        assert report('false').isEmpty()
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void testReportDoesNotChangeTheGeneratedCode() {
        List<byte[]> without = [], with = []
        report(null, REPORTED, without)
        report('true', REPORTED, with)
        assert without.size() == with.size() && !without.isEmpty()
        without.eachWithIndex { byte[] bytes, int i -> assert Arrays.equals(bytes, with[i]) }
    }
}

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

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.messages.WarningMessage
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * What statically compiled code stores for a closure, lambda or method reference, or for a value of type
 * {@code Closure}, at each kind of slot declared with a type parameter that the call or assignment binds to a
 * functional interface, and whether a statically compiled read through the interface works:
 * <ul>
 * <li>{@code C+I}: a closure whose class implements the interface, which dynamic code still calls as a closure;
 * <li>{@code L}: a native lambda, an instance of the interface only;
 * <li>{@code MC}: a method closure, stored as it is, with a warning;
 * <li>{@code C}: a closure, stored as it is, with a warning.
 * </ul>
 *
 * @see FunctionalTypeArgumentTest
 */
final class FunctionalLiteralSiteTest {

    private static final String FUNCTION = 'java.util.function.Function<java.lang.String, java.lang.String>'

    private static final String IMPORTS = '''
        import groovy.transform.*
        import java.util.function.*
    '''

    private static final String HELPER = '''
        class Box<T> { T value; Box() { }; Box(T value) { this.value = value } }
        class Named<T> { T value }
        class K {
            static String kind(Object o) {
                if (o instanceof org.codehaus.groovy.runtime.MethodClosure) return o instanceof Function ? 'MC+I' : 'MC'
                if (o instanceof Closure) return o instanceof Function ? 'C+I' : 'C'
                o instanceof Function ? 'L' : String.valueOf(o)
            }
            // a statically compiled read through the interface, with no conversion of the element
            @CompileStatic static String read(List<Function<String, String>> list) { list[0].apply(' a ') }
            static String trim(String s) { s.trim() }
            static Closure<String> make() { return { String s -> s.trim() } }
        }
    '''

    /** The statements that store {@code X}, or {@code X} and {@code Y}, and the expression that reads it back. */
    private static final Map<String, String> ARGUMENT_SITES = [
        'list.add(X)'       : 'List<Function<String, String>> list = []; list.add(X); list[0]',
        'list.add(0, X)'    : 'List<Function<String, String>> list = []; list.add(0, X); list[0]',
        'list.set(0, X)'    : 'List<Function<String, String>> list = [null]; list.set(0, X); list[0]',
        'map.put(k, X)'     : 'Map<String, Function<String, String>> map = [:]; map.put("k", X); map.get("k")',
        'varargs'           : 'List<Function<String, String>> list = []; Collections.addAll(list, X); list[0]',
        'constructor'       : 'new Box<Function<String, String>>(X).value'
    ]

    private static final Map<String, String> OPERAND_SITES = [
        'list << X'         : 'List<Function<String, String>> list = []; list << X; list[0]',
        'named argument'    : 'new Named<Function<String, String>>(value: X).value'
    ]

    private static final Map<String, String> ASSIGNMENT_SITES = [
        'list[0] = X'       : 'List<Function<String, String>> list = [null]; list[0] = X; list[0]',
        'map[k] = X'        : 'Map<String, Function<String, String>> map = [:]; map["k"] = X; map.get("k")',
        'map?[k] = X'       : 'Map<String, Function<String, String>> map = [:]; map?["k"] = X; map.get("k")',
        'map.k = X'         : 'Map<String, Function<String, String>> map = [:]; map.k = X; map.get("k")',
        'box.value = X'     : 'def box = new Box<Function<String, String>>(); box.value = X; box.value'
    ]

    private static final Map<String, String> CONDITIONAL_ARGUMENT_SITES = [
        'list.add(c ? X : Y)' : 'List<Function<String, String>> list = []; list.add(flag ? X : Y); list[0]',
        'list << (c ? X : Y)' : 'List<Function<String, String>> list = []; list << (flag ? X : Y); list[0]'
    ]

    private static final Map<String, String> CONDITIONAL_ASSIGNMENT_SITES = [
        'list[0] = c ? X : Y'   : 'List<Function<String, String>> list = [null]; list[0] = flag ? X : Y; list[0]',
        'box.value = c ? X : Y' : 'def box = new Box<Function<String, String>>(); box.value = flag ? X : Y; box.value'
    ]

    private static final Map<String, List<String>> CLOSURES = [
        'closure'        : ['{ String s -> s.trim() }', '{ String s -> s.strip() }'],
        'untyped closure': ['{ it.trim() }', '{ it.strip() }']
    ]

    private static final Map<String, List<String>> LAMBDAS = [
        'lambda'         : ['(String s) -> s.trim()', '(String s) -> s.strip()'],
        'untyped lambda' : ['(s -> s.trim())', '(s -> s.strip())']
    ]

    private static final Map<String, List<String>> REFERENCES = [
        'method reference': ['String::trim', 'String::strip']
    ]

    private static final Map<String, List<String>> VALUES = [
        'closure variable': ['variable', 'variable'],
        'method pointer'  : ['K.&trim', 'K.&trim'],
        'call result'     : ['K.make()', 'K.make()']
    ]

    private final GroovyShell shell = new GroovyShell()

    /**
     * Compiles and runs the site with the literal, and checks the representation of the stored value, the number of
     * warnings, a statically compiled read through the interface and a dynamic call of the value as a closure.
     */
    private void check(String site, String code, String name, List<String> literal, String kind, int expected) {
        String source = IMPORTS + HELPER + """
            @CompileStatic
            class T {
                static Object test(boolean flag) {
                    Closure<String> variable = { String s -> s.trim() }
                    ${code.replace('X', literal[0]).replace('Y', literal[1])}
                }
            }
        """
        String where = "$name at $site"
        try {
            List<String> reported = warnings(source)
            assert reported.size() == expected : where
            assert reported.every { it == warning(FUNCTION) } : where

            for (flag in [true, false]) {
                shell.setVariable('flag', flag)
                shell.setVariable('kind', kind)
                shell.setVariable('where', where)
                shell.evaluate source + '''
                    def value = T.test(flag)
                    assert K.kind(value) == kind : where
                    if (kind == 'C' || kind == 'MC') { // stored as it is: not an instance of the interface
                        try {
                            K.read([value])
                            assert false : "$where: expected a ClassCastException"
                        } catch (ClassCastException expected) {
                        }
                    } else {
                        assert K.read([value]) == 'a' : where
                    }
                    if (kind == 'L') { // not a closure
                        try {
                            value(' a ')
                            assert false : "$where: expected a MissingMethodException"
                        } catch (MissingMethodException expected) {
                        }
                    } else {
                        assert value(' a ') == 'a' && value.call(' a ') == 'a' : where
                    }
                '''
            }
        } catch (MultipleCompilationErrorsException e) {
            throw new AssertionError("$where: $e.message", e)
        }
    }

    private void checkAll(Map<String, String> sites, Map<String, List<String>> literals, String kind, int expected = 0) {
        sites.each { site, code ->
            literals.each { name, literal ->
                check(site, code, name, literal, kind, expected)
            }
        }
    }

    //--------------------------------------------------------------------------
    // site x literal kind

    @Test
    void testClosureLiteralImplementsTheInterfaceAtEverySite() {
        def sites = ARGUMENT_SITES + OPERAND_SITES + ASSIGNMENT_SITES + CONDITIONAL_ARGUMENT_SITES + CONDITIONAL_ASSIGNMENT_SITES
        checkAll(sites, CLOSURES, 'C+I')
    }

    @Test
    void testLambdaImplementsTheInterfaceWhereItWasAClosure() {
        checkAll(ARGUMENT_SITES + OPERAND_SITES + CONDITIONAL_ARGUMENT_SITES, LAMBDAS, 'C+I')
    }

    @Test
    void testLambdaAssignedToASubscriptOrPropertyStaysANativeLambda() {
        checkAll(ASSIGNMENT_SITES + CONDITIONAL_ASSIGNMENT_SITES, LAMBDAS, 'L')
    }

    @Test
    void testMethodReferenceArgumentOrAssignedIsCompiledForTheInterface() {
        checkAll(ARGUMENT_SITES + ASSIGNMENT_SITES + CONDITIONAL_ASSIGNMENT_SITES, REFERENCES, 'L')
    }

    @Test
    void testMethodReferenceOperandOrConditionalArgumentStaysAMethodClosure() {
        checkAll(OPERAND_SITES, REFERENCES, 'MC', 1)
        checkAll(CONDITIONAL_ARGUMENT_SITES, REFERENCES, 'MC', 2) // each branch
    }

    @Test
    void testClosureValueIsStoredAsItIsAtEverySite() {
        VALUES.each { name, value ->
            String kind = name == 'method pointer' ? 'MC' : 'C'
            checkAll(ARGUMENT_SITES + OPERAND_SITES + ASSIGNMENT_SITES, [(name): value], kind, 1)
            checkAll(CONDITIONAL_ARGUMENT_SITES + CONDITIONAL_ASSIGNMENT_SITES, [(name): value], kind, 2) // each branch
        }
    }

    @Test
    void testMixedBranchesOfAConditionalArgument() {
        assertScript shell, IMPORTS + HELPER + '''
            @CompileStatic
            class T {
                static List<Function<String, String>> test(boolean flag) {
                    List<Function<String, String>> list = []
                    list.add(flag ? { it.trim() } : (s -> s.strip()))
                    list << (flag ? ((String s) -> s.trim()) : { String s -> s.strip() })
                    list
                }
            }

            for (flag in [true, false]) {
                def list = T.test(flag)
                assert list.collect { K.kind(it) } == ['C+I', 'C+I']
                assert list*.apply(' a ') == ['a', 'a']
            }
        '''
    }

    @Test
    void testPlusAssignIsRejected() {
        // DefaultGroovyMethods#plus(List<T>, T) is called with T as Object, so the result is no List<Function>
        for (literal in ['{ String s -> s.trim() }', '(String s) -> s.trim()', 'String::trim']) {
            shouldFail shell, IMPORTS + """
                @CompileStatic
                void test() {
                    List<Function<String, String>> list = []
                    list += $literal
                }
            """
        }
    }

    //--------------------------------------------------------------------------
    // lambdas

    @Test
    void testUntypedLambdaParametersTakeTheTypesOfTheInterface() {
        assertScript shell, IMPORTS + HELPER + '''
            @CompileStatic
            class T {
                static String m(String s) { 'string' }
                static String m(Object o) { 'object' }
                static List test() {
                    List<BiFunction<String, Integer, String>> pairs = []
                    pairs.add((s, n) -> s * n)
                    pairs << ((s, n) -> s.substring(n))
                    List<Function<String, String>> overloads = []
                    overloads.add(s -> m(s))
                    List<Consumer<List<String>>> consumers = []
                    consumers.add(strings -> strings.add(strings[0].toUpperCase()))
                    List<Comparator<String>> comparators = []
                    comparators.add((a, b) -> a.length() <=> b.length())
                    List<IntBinaryOperator> operators = []
                    operators.add((a, b) -> a * b)
                    def strings = ['a']
                    consumers[0].accept(strings)
                    [pairs[0].apply('ab', 2), pairs[1].apply('abc', 1), overloads[0].apply('x'), strings,
                     comparators[0].compare('a', 'bb'), operators[0].applyAsInt(3, 4),
                     [pairs[0], overloads[0], consumers[0], comparators[0], operators[0]].every { it instanceof Closure }]
                }
            }

            assert T.test() == ['abab', 'bc', 'string', ['a', 'A'], -1, 12, true]
        '''
    }

    @Test
    void testLambdaThatImplementsTheInterfaceIsCheckedAsAClosure() {
        assertScript shell, IMPORTS + '''
            @CompileStatic
            class T {
                String name = 'field'
                String hello() { 'hello' }
                List<Supplier<String>> make() {
                    List<Supplier<String>> list = []
                    String local = 'local'
                    list.add(() -> name + ' ' + local + ' ' + hello() + ' ' + this.name)
                    list.add(() -> owner.getClass().simpleName + delegate.getClass().simpleName) // a closure has an owner
                    list << (() -> { Supplier<String> nested = () -> name + ' ' + local; nested.get() })
                    list.add(() -> { List<Supplier<String>> inner = []; inner.add(() -> hello() + ' ' + local); inner[0].get() })
                    list
                }
                static List<Integer> count() {
                    List<Supplier<Integer>> list = []
                    int n = 0
                    list.add(() -> ++n)
                    [list[0].get(), list[0].get(), n]
                }
            }

            def list = new T().make()
            assert list*.get() == ['field local hello field', 'TT', 'field local', 'hello local']
            assert list.every { it instanceof Closure && it instanceof Supplier }
            assert list[0].owner instanceof T && list[0].thisObject instanceof T
            assert T.count() == [1, 2, 2]
        '''
    }

    @Test
    void testLambdaInATrait() {
        assertScript shell, IMPORTS + '''
            @CompileStatic
            trait Named {
                String name() { 'trait' }
                List<Supplier<String>> make() {
                    List<Supplier<String>> list = []
                    list.add(() -> name() + '!')
                    list << (() -> name() + '?')
                    list
                }
            }
            @CompileStatic
            class T implements Named { }

            def list = new T().make()
            assert list*.get() == ['trait!', 'trait?']
            assert list.every { it instanceof Closure && it instanceof Supplier }
        '''
    }

    @Test
    void testLambdaThatImplementsTheInterfaceKeepsWhatAClosureDoes() {
        assertScript shell, IMPORTS + '''
            class D {
                static String m(Closure c) { 'closure' }
                static String m(Function f) { 'function' }
            }
            @CompileStatic
            class T {
                static List<Function<String, String>> functions() {
                    List<Function<String, String>> list = []
                    list.add((String s) -> s.trim())
                    list
                }
                static List<Predicate<String>> predicates() {
                    List<Predicate<String>> list = []
                    list.add(s -> s.startsWith('a'))
                    list
                }
            }

            def list = T.functions()
            def function = list[0]
            def predicate = T.predicates()[0]
            assert D.m(function) == 'closure' // dynamic code selects the Closure overload, as before
            assert function.curry(' x ')() == 'x'
            assert function.memoize()(' y ') == 'y'
            assert [' a ', ' b '].collect(function) == ['a', 'b']
            assert ['abc', 'b'].grep(predicate) == ['abc']
            assert (switch ('abc') { case predicate -> 'case'; default -> 'default' }) == 'case'
            assert function.andThen { it + '!' }(' z ') == 'z!' // Closure#andThen(Closure)
            assert predicate.test('abc') && function.apply(' w ') == 'w'
            assert list.contains(function) && list.indexOf(function) == 0 && list.remove(function)
            assert function instanceof Serializable && function.maximumNumberOfParameters == 1
        '''
    }

    @Test
    void testTypeCheckedAndDynamicCodePassALambdaAsBefore() {
        for (mode in ['@TypeChecked', '']) {
            assertScript shell, IMPORTS + HELPER + """
                $mode
                class T {
                    static List<Object> test() {
                        List<Function<String, String>> list = []
                        list.add((String s) -> s.trim())
                        list << ((String s) -> s.trim())
                        Collections.addAll(list, (String s) -> s.trim())
                        list.add(true ? ((String s) -> s.trim()) : ((String s) -> s.strip()))
                        [list[0], list[1], list[2], list[3], new Named<Function<String, String>>(value: (String s) -> s.trim()).value]
                    }
                }

                assert T.test().collect { K.kind(it) } == ['C'] * 5
            """
        }
    }

    //--------------------------------------------------------------------------
    // raw and mismatched targets

    @Test
    void testTypedLiteralImplementsARawInterface() {
        assertScript shell, IMPORTS + HELPER + '''
            @CompileStatic
            class T {
                static List<Function> functions() {
                    List<Function> list = []
                    list.add { String s -> s.trim() }
                    list.add((String s) -> s.trim())
                    list << { String s -> s.trim() }
                    list << ((String s) -> s.trim())
                    list.add { it.toString().trim() }
                    list.add(s -> s.toString().trim())
                    list.add(String::valueOf)
                    list
                }
                static List<Object> read(List<Function> list) {
                    List<Object> result = []
                    for (Function f : list) result << f.apply(' a ')
                    result
                }
                static Object wrongArgument() {
                    List<Function> list = []
                    list.add { Integer i -> i + 1 }
                    list[0].apply('one')
                }
            }

            def list = T.functions()
            assert list.collect { K.kind(it) } == ['C+I'] * 6 + ['L']
            assert T.read(list) == ['a'] * 6 + [' a ']
            // the closure is called as a coercion proxy for it would call it, so a wrong argument fails as it does there
            groovy.test.GroovyAssert.shouldFail(MissingMethodException) { T.wrongArgument() }
        '''
    }

    @Test
    void testLiteralWhoseParametersDoNotFitIsStoredAsItIs() {
        String source = IMPORTS + '''
            @CompileStatic
            class W {
                static List<Object> make() {
                    List<Function<Object, String>> list = []
                    list.add { String s -> s.trim() }               // a String for an Object
                    list.add((String s) -> s.trim())
                    list << { String s -> s.trim() }
                    List<BiFunction<Object, Object, String>> pairs = []
                    pairs << { String a, b -> a + b }
                    List<Function<String, String>> functions = []
                    functions.add { String a, String b -> a + b }   // two parameters for one
                    functions.add((String a, String b) -> a + b)
                    functions.add { String s = 'x' -> s }           // a default value
                    List<Object> values = []
                    values.addAll(list)
                    values.addAll(pairs)
                    values.addAll(functions)
                    values
                }
                static List<Object> fits() {
                    List<Function<String, String>> list = []
                    list.add { Object o -> o.toString() }           // an Object for a String
                    list.add((CharSequence s) -> s.toString())
                    list << { def s -> s.toString() }
                    List<Object> values = []
                    values.addAll(list)
                    values
                }
            }
        '''
        def reported = warnings(source)
        assert reported.size() == 7
        assert reported.every { it.startsWith('the closure is stored as a Closure, not as java.util.function.') }

        assertScript shell, source + '''
            assert W.make().every { it instanceof Closure && !(it instanceof Function) && !(it instanceof BiFunction) }
            assert W.make()[0](' a ') == 'a'
            assert W.fits().every { it instanceof Closure && it instanceof Function }
        '''
    }

    //--------------------------------------------------------------------------
    // warnings

    @Test
    void testWarningForALiteralThatCannotImplementTheInterface() {
        String types = '''
            interface Caller { String call(String s) }
            interface Runner { String run(String s) }
            interface Caser { boolean isCase(String s) }
            interface Owned { String apply(String s); default Object getOwner() { 'default owner' } }
            interface Generic { def <T> T apply(T t) }
            abstract class Task { abstract String exec(String s) }
            sealed interface Shout permits Loud { String shout(String s) }
            final class Loud implements Shout { String shout(String s) { s.toUpperCase() } }
            trait Greeter { abstract String greet(String s) }
        '''
        Map<String, String> literals = [
            Caller : '{ String s -> s }',       // the name of a method of Closure
            Runner : '(String s) -> s',
            Caser  : '{ String s -> true }',
            Owned  : '(String s) -> s',         // a default method that Closure would replace
            Generic: '{ Object o -> o }',       // a method with type parameters
            Task   : '{ String s -> s }',       // an abstract class
            Shout  : '(String s) -> s',         // a sealed interface
            Greeter: '{ String s -> s }'        // a trait
        ]
        literals.each { type, literal ->
            List<String> statements = ["list.add($literal)", "list << ($literal)", "list.add(flag ? ($literal) : null)"]
            // an assignment checks a closure against the method of the interface, as before
            if (type != 'Generic') statements << "list[0] = { String s -> ${type == 'Caser' ? 'true' : 's'} }".toString()
            for (statement in statements) {
                String source = IMPORTS + types + """
                    @CompileStatic
                    class W {
                        static List<$type> make(boolean flag) {
                            List<$type> list = [null]
                            $statement
                            list
                        }
                    }
                """
                assert warnings(source) == [warning(type)] : "$type: $statement"

                assertScript shell, source + """
                    def value = W.make(true).find()
                    assert value instanceof Closure && !(value instanceof $type) // stored as before
                """
            }
        }
    }

    @Test
    void testWarningForALiteralOfAnInterfaceTheClosureClassCannotAccess() {
        def parent = new GroovyClassLoader()
        parent.parseClass '''
            package p
            @groovy.transform.PackageScope
            interface Callback { String f(String s) }
        '''
        // another class loader defines W, so W is in another run-time package than Callback
        def unit = new CompilationUnit(new CompilerConfiguration(), null, new GroovyClassLoader(parent))
        unit.addSource 'W.groovy', '''
            package p
            @groovy.transform.CompileStatic
            class W {
                static List make() {
                    List<Callback> list = []
                    list.add { String s -> s + '!' }
                    list.add((String s) -> s + '?')
                    list
                }
            }
        '''
        unit.compile(Phases.CLASS_GENERATION)
        assert unit.errorCollector.warnings*.message == [warning('p.Callback')] * 2
    }

    @Test
    void testNoWarningWhereAClosureAlreadyIsTheInterface() {
        String source = IMPORTS + '''
            import java.util.concurrent.Callable
            @CompileStatic
            class W {
                static List<Object> make(Closure<String> closure) {
                    List<Runnable> runnables = []
                    runnables.add { -> }
                    runnables.add(() -> { })
                    runnables << closure
                    List<Callable<String>> callables = []
                    callables.add { 'closure' }
                    callables << (() -> 'lambda')
                    callables.add(closure)
                    callables[0] = closure
                    runnables[0].run()                      // a statically compiled read through the interface
                    [callables[0].call(), callables[1].call(), callables[2].call()]
                }
            }
        '''
        assert warnings(source).isEmpty()

        assertScript shell, source + '''
            assert W.make { 'value' } == ['value', 'lambda', 'value']
        '''
    }

    @Test
    void testNoWarningForAValueThatIsConvertedOrAlreadyTheInterface() {
        assert warnings(IMPORTS + HELPER + '''
            @CompileStatic
            class W {
                static void test(Closure<String> closure, Function<String, String> function, boolean flag) {
                    List<Function<String, String>> list = []
                    list.add(closure as Function<String, String>)
                    list << (closure as Function<String, String>)
                    list.add(function)
                    list << function
                    list[0] = function
                    list.add(String::trim)
                    list[0] = String::trim
                    list[0] = (s -> s.trim())
                    new Box<Function<String, String>>(function)
                    new Named<Function<String, String>>(value: function)
                    List<Object> objects = []
                    objects.add(closure)                    // no functional interface
                    objects << K.&trim
                    List<Closure<String>> closures = []
                    closures.add(closure)
                    closures << (s -> s.toString())
                }
            }
        ''').isEmpty()
    }

    @Test
    void testInferenceForAClosureValueIsUnchanged() {
        // T is not Function<String, String> for the closure: it is stored as it is, with a warning, and the
        // call's value has the common supertype of the arguments, as before
        String helper = IMPORTS + '''
            @CompileStatic
            class H {
                static <T> T pick(T a, T b) { b }
            }
        '''
        String source = helper + '''
            @CompileStatic
            class W {
                static Object test(Closure<String> closure) {
                    Function<String, String> function = String::trim
                    def picked = H.pick(function, closure)
                    picked
                }
            }
        '''
        assert warnings(source) == [warning(FUNCTION)]

        assertScript shell, source + '''
            def closure = { String s -> s }
            assert W.test(closure).is(closure)
        '''

        def err = shouldFail shell, helper + '''
            @CompileStatic
            void test(Closure<String> closure) {
                Function<String, String> function = String::trim
                def picked = H.pick(function, closure)
                picked.apply(' a ')
            }
        '''
        assert err.message.contains('Cannot find matching method java.lang.Object#apply(java.lang.String)')

        err = shouldFail shell, helper + '''
            @CompileStatic
            void test(Closure<String> closure) {
                Function<String, String> function = String::trim
                Function<String, String> picked = H.pick(function, closure)
            }
        '''
        assert err.message.contains('Cannot assign value of type java.lang.Object to variable of type java.util.function.Function<java.lang.String, java.lang.String>')
    }

    //--------------------------------------------------------------------------
    // regressions

    @Test
    void testClosureOrLambdaInAMapPassedToAConstructor() {
        assertScript shell, IMPORTS + '''
            class Foo { Supplier<Integer> name }
            class Bar { Map map; Bar(Map map) { this.map = map } }
            @CompileStatic
            class T {
                static List<Object> test(String s) {
                    def foo = new Foo(Map.of('name', { -> s.length() + 1 }))
                    def bar = new Bar(Map.of('name', { -> s.length() + 2 }))
                    def lambda = new Bar(Map.of('name', () -> s.length() + 3))
                    def named = new Named<Supplier<Integer>>(Map.of('value', () -> s.length() + 4))
                    def literal = new Named<Supplier<Integer>>(value: () -> s.length() + 5)
                    [foo.name.get(), bar.map.name, lambda.map.name, named.value, literal.value]
                }
            }
            class Named<T> { T value }

            def (foo, bar, lambda, named, literal) = T.test('abc')
            assert foo == 4
            assert bar() == 5 && lambda() == 6 && named() == 7
            assert [bar, lambda, named].every { it instanceof Closure && !(it instanceof Supplier) } // a value of the map
            assert literal instanceof Closure && literal instanceof Supplier && literal.get() == 8
        '''
    }

    @Test
    void testErrorInALiteralIsReportedOnceWithTheTypesOfTheInterface() {
        Map<String, String> statements = [
            'argument'            : 'list.add(s -> s.nonexistent())',
            'closure argument'    : 'list.add { it.nonexistent() }',
            'operand'             : 'list << (s -> s.nonexistent())',
            'conditional argument': 'list.add(flag ? (s -> s.nonexistent()) : (s -> s))',
            'named argument'      : 'new Named<Function<String, String>>(value: s -> s.nonexistent())',
            'subscript'           : 'list[0] = (s -> s.nonexistent())'
        ]
        statements.each { name, statement ->
            def err = shouldFail shell, IMPORTS + HELPER + """
                @CompileStatic
                void test(boolean flag) {
                    List<Function<String, String>> list = [null]
                    $statement
                }
            """
            assert err.message.contains('Cannot find matching method java.lang.String#nonexistent()') : name
            assert err.message.contains('1 error') : name
        }

        def err = shouldFail shell, IMPORTS + '''
            @CompileStatic
            void test() {
                List<Function<String, String>> list = []
                list.add((String s) -> 42)
            }
        '''
        assert err.message.contains('Cannot return value of type int for lambda expecting java.lang.String')
        assert err.message.contains('1 error')

        // a later statement is checked as if the lambda before it had no error
        err = shouldFail shell, IMPORTS + '''
            @CompileStatic
            void test() {
                List<Function<String, String>> list = []
                list.add(s -> s.trim())
                list.add(s -> s.nonexistent())
                list.add(s -> s.strip())
                String s = list[0].apply(' a ').missing()
            }
        '''
        assert err.message.contains('Cannot find matching method java.lang.String#nonexistent()')
        assert err.message.contains('Cannot find matching method java.lang.String#missing()')
        assert err.message.contains('2 errors')
    }

    private static String warning(String type) {
        "the closure is stored as a Closure, not as $type: a statically compiled read through $type will fail; write 'as $type' to convert it"
    }

    private static List<String> warnings(String source) {
        def unit = new CompilationUnit(new CompilerConfiguration(warningLevel: WarningMessage.LIKELY_ERRORS))
        unit.addSource('W.groovy', source)
        unit.compile(Phases.CLASS_GENERATION)
        (unit.errorCollector.warnings ?: [])*.message
    }
}

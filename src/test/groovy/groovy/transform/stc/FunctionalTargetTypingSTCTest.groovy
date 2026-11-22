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
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * A grid of programs in which a generic method, or a method reference, method pointer,
 * lambda or closure that calls it, takes a type argument from its target.
 * <p>
 * Each cell of the grid is one program, made from:
 * <ul>
 * <li>a <em>kind</em>: where the type parameter of the method is constrained, see {@link #KINDS};</li>
 * <li>a <em>functor</em>: the method called directly, or a reference, pointer, lambda or closure;</li>
 * <li>a <em>context</em>: where the target comes from, see {@link #script};</li>
 * <li>whether the type that the target gives is within the bound of the type parameter.</li>
 * </ul>
 * A cell is <em>legal</em> if that type is within the bound, and <em>illegal</em> if it is not:
 * accepting an illegal program gives an expression a static type that its value does not have.
 * A legal program should compile and run, and an illegal one should be a compile error.
 * <p>
 * Running a program does not decide which it is, but it is the evidence for each. An illegal
 * program goes on to use the value at the type it was given, and so fails: with a cast error, with
 * no method found, or on reading back a list that now holds a value of the wrong type.
 * {@link #testCellAsDynamicCode} checks that every legal program runs as dynamic code, so none
 * is working code that the grid would have rejected, and that every illegal one fails, so none is
 * only a rule of Java that Groovy does not share. An illegal program that compiles is run as
 * compiled as well, to show the failure where the types were checked.
 * <p>
 * A third kind of cell is <em>coerced</em>. Groovy converts a value where it moves into a declared
 * type: where it is assigned or returned, and where a lambda, closure, method reference or method
 * pointer returns it to a functional interface. Any value is converted to a {@code String}. Such a
 * program is to be read as if the conversion were written out, as a call of {@code toString()}
 * would be, so the type parameter is not given the type of the target: for
 * {@code String s = C.zero()} it is a {@code Number} and the result is then converted. A coerced
 * program should compile and run, and the value must then have the type it was converted to, so
 * each one reads the value back with no static type for it. A type argument is not such a place:
 * a value that is already in a list is not converted.
 * <p>
 * The cells that do not yet behave as they should are listed in {@link #ILLEGAL_ACCEPTED},
 * {@link #LEGAL_REJECTED}, {@link #COERCION_NOT_PERFORMED} and {@link #COERCION_REJECTED}. A listed
 * cell is checked to behave as listed, so the lists show what is known not to work, and a change
 * that moves a cell either way fails here.
 */
class FunctionalTargetTypingSTCTest extends StaticTypeCheckingTestCase {

    private static final String DECLARATIONS = '''
        static List<Integer> store = new ArrayList<Integer>()
        static void check() { for (Number n : store) n.intValue() }
        static <T extends Number> void takeNumber(T t) { t.intValue() }
        static <T extends Number> List<T> numbers() { (List<T>) store }
        static <T extends Number> T zero() { (T) Integer.valueOf(0) }
        static <T extends Number> T same(T t) { t.intValue(); t }
        static <T extends Comparable<T>> T max(T a, T b) { a.compareTo(b) >= 0 ? a : b }
        static <T> Consumer<T> toConsumer(Consumer<T> c) { c }
        static <T> Supplier<T> toSupplier(Supplier<T> s) { s }
        static <T, R> Function<T, R> toFunction(Function<T, R> f) { f }
        static <T> BinaryOperator<T> toBinary(BinaryOperator<T> b) { b }
        // the class of what a supplier gives, read with no static type for it
        static String given(Supplier s) { Object o = s.get(); o.getClass().simpleName }
    '''

    /**
     * Where the type parameter is constrained. {@code type} is the target, {@code use} runs it,
     * and {@code X}, {@code A}, {@code V} and {@code W} are replaced by the legal or illegal values.
     */
    private static final Map<String, Map<String, Object>> KINDS = [
        // the parameter of the functional interface: <T extends Number> void takeNumber(T)
        parameter: [
            type: 'Consumer<X>', wrap: 'toConsumer', use: ['v.accept(V)'],
            reference: 'C::takeNumber', pointer: 'C.&takeNumber', lambda: 'x -> C.takeNumber(x)', closure: '{ x -> C.takeNumber(x) }',
            legal: [X: 'Integer', V: '1'], illegal: [X: 'String', V: "'x'"]
        ],
        // within what the functional interface returns: <T extends Number> List<T> numbers()
        result: [
            type: 'Supplier<List<X>>', wrap: 'toSupplier', use: ['v.get().add(V)', 'C.check()'],
            call: ['List<X> d = C.numbers()', 'd.add(V)', 'C.check()'],
            reference: 'C::numbers', pointer: 'C.&numbers', lambda: '() -> C.numbers()', closure: '{ -> C.numbers() }',
            legal: [X: 'Integer', V: '1'], illegal: [X: 'String', V: "'x'"]
        ],
        // what the functional interface returns: <T extends Number> T zero()
        bareResult: [
            type: 'Supplier<X>', wrap: 'toSupplier', use: ['X r = v.get()'],
            call: ['X d = C.zero()'],
            reference: 'C::zero', pointer: 'C.&zero', lambda: '() -> C.zero()', closure: '{ -> C.zero() }',
            legal: [X: 'Integer'], illegal: [X: 'UUID'],
            // a Number is converted to a String where it is assigned or returned
            coerced: [X: 'String'], coercedUse: ["assert given(v) == 'String'"],
            coercedCall: ['String d = C.zero()', 'Object o = d', 'assert o instanceof String']
        ],
        // both: <T extends Number> T same(T)
        parameterAndResult: [
            type: 'Function<X, X>', wrap: 'toFunction', use: ['X r = v.apply(V)'],
            call: ['X d = C.same(V)'],
            reference: 'C::same', pointer: 'C.&same', lambda: 'x -> C.same(x)', closure: '{ x -> C.same(x) }',
            legal: [X: 'Integer', V: '1'], illegal: [X: 'String', V: "'x'"]
        ],
        // a bound that names the type parameter: <T extends Comparable<T>> T max(T, T)
        selfBound: [
            type: 'BinaryOperator<X>', wrap: 'toBinary', use: ['X r = v.apply(V, W)'],
            call: ['X d = C.max(V, W)'],
            reference: 'C::max', pointer: 'C.&max', lambda: '(a, b) -> C.max(a, b)', closure: '{ a, b -> C.max(a, b) }',
            legal: [X: 'String', V: "'a'", W: "'b'"], illegal: [X: 'Object', V: '1', W: "'x'"]
        ],
        // a type parameter of the class of the receiver: T get() of Optional<T>
        receiver: [
            type: 'Function<Optional<A>, X>', wrap: 'toFunction', use: ['X r = v.apply(Optional.of(V))'],
            reference: 'Optional::get', lambda: 'o -> o.get()', closure: '{ o -> o.get() }',
            legal: [X: 'String', A: 'String', V: "'x'"], illegal: [X: 'UUID', A: 'Integer', V: '1']
        ]
    ]

    private static final List<String> FUNCTORS = ['reference', 'pointer', 'lambda', 'closure']

    /**
     * Illegal programs that compile, as "functor context" to the kinds. Each fails when it
     * is run, or leaves a value of the wrong type in a list.
     */
    private static final Map<String, List<String>> ILLEGAL_ACCEPTED = [
        // The bound of a type parameter is checked for a method reference or pointer that
        // is passed to a generic call with a target. Elsewhere a type argument that the
        // target gives is not checked against the bound: a direct call, ...
        'call assign'              : ['result', 'selfBound'],
        // ... a functor that is assigned or passed to a functional interface itself, ...
        'reference assign'         : ['result', 'bareResult', 'selfBound'],
        'reference argument'       : ['result', 'bareResult', 'selfBound'],
        'pointer assign'           : ['parameter', 'result', 'bareResult', 'parameterAndResult', 'selfBound'],
        'pointer argument'         : ['parameter', 'result', 'bareResult', 'parameterAndResult', 'selfBound'],
        'lambda assign'            : ['result', 'bareResult'],
        'lambda argument'          : ['result', 'bareResult'],
        'closure assign'           : ['result', 'bareResult'],
        'closure argument'         : ['result', 'bareResult'],
        // ... and the direct call in the body of a lambda or closure
        'lambda wrapped'           : ['result'],
        'lambda nested'            : ['result'],
        'lambda wrappedReturn'     : ['result'],
        'closure wrapped'          : ['result'],
        // GROOVY-12778: a bound that names the type parameter is not checked
        'reference wrappedReturn'  : ['selfBound'],
        // GROOVY-12777: a generic call that is itself an argument
        'reference wrappedArgument': ['parameter']
    ]

    /**
     * Illegal programs that are accepted with type checking only: static compilation
     * finds no method for the method reference.
     */
    private static final Map<String, List<String>> ILLEGAL_ACCEPTED_TYPE_CHECKED_ONLY = [
        'reference assign'  : ['parameter', 'parameterAndResult'],
        'reference argument': ['parameter', 'parameterAndResult']
    ]

    /**
     * Legal programs that are compile errors, as "functor context" to the kinds.
     */
    private static final Map<String, List<String>> LEGAL_REJECTED = [
        // a lambda or closure parameter without a type does not take its type from the
        // target through a generic call, so it is an Object in the body
        'lambda wrapped'           : ['parameter', 'bareResult', 'parameterAndResult', 'selfBound', 'receiver'],
        'lambda nested'            : ['parameter', 'bareResult', 'parameterAndResult', 'selfBound', 'receiver'],
        'lambda wrappedArgument'   : ['parameter', 'result', 'bareResult', 'parameterAndResult', 'selfBound', 'receiver'],
        'lambda wrappedReturn'     : ['parameter', 'bareResult', 'parameterAndResult', 'selfBound', 'receiver'],
        'closure wrapped'          : ['parameter', 'bareResult', 'parameterAndResult', 'selfBound', 'receiver'],
    ]

    /**
     * Coerced programs that compile but do not convert the value, as "functor context" to the
     * kinds: the functional interface declares a {@code String} and gives a {@code Number}.
     * Whether a method reference or pointer is to convert or to be a compile error is not decided.
     */
    // GROOVY-12783
    private static final Map<String, List<String>> COERCION_NOT_PERFORMED = [
        'reference assign'  : ['bareResult'],
        'reference argument': ['bareResult'],
        'pointer assign'    : ['bareResult'],
        'pointer argument'  : ['bareResult']
    ]

    /**
     * Coerced programs that do not convert the value with static compilation only: with type
     * checking, the lambda is a closure, which converts what it returns.
     */
    // GROOVY-12783
    private static final Map<String, List<String>> COERCION_NOT_PERFORMED_STATIC_COMPILE_ONLY = [
        'lambda assign'  : ['bareResult'],
        'lambda argument': ['bareResult']
    ]

    /**
     * Coerced programs that are compile errors, as "functor context" to the kinds: through a
     * generic call, the target gives the type parameter of the call a {@code String}, and what
     * the functor returns is then checked against it as a type argument.
     */
    // GROOVY-12783
    private static final Map<String, List<String>> COERCION_REJECTED = [
        'reference wrapped'        : ['bareResult'],
        'reference nested'         : ['bareResult'],
        'reference wrappedArgument': ['bareResult'],
        'reference wrappedReturn'  : ['bareResult'],
        'pointer wrapped'          : ['bareResult'],
        'lambda wrapped'           : ['bareResult'],
        'lambda nested'            : ['bareResult'],
        'lambda wrappedArgument'   : ['bareResult'],
        'lambda wrappedReturn'     : ['bareResult'],
        'closure wrapped'          : ['bareResult']
    ]

    static List<String> cells() {
        List<String> cells = []
        KINDS.each { kind, spec ->
            for (legality in ['legal', 'illegal', 'coerced']) {
                if (!spec[legality]) continue
                if (spec.call) cells << "$kind call assign $legality".toString()
                for (functor in FUNCTORS) {
                    if (!spec[functor]) continue
                    List<String> contexts = ['assign', 'wrapped', 'argument']
                    if (functor in ['reference', 'lambda']) contexts += ['nested', 'wrappedArgument', 'wrappedReturn']
                    for (context in contexts) cells << "$kind $functor $context $legality".toString()
                }
            }
        }
        cells
    }

    /**
     * The program of a cell. The contexts are: {@code assign}, the call or functor is
     * assigned to the target; {@code argument}, the functor is passed to a parameter of
     * the target type; {@code wrapped}, the functor is passed to a generic call that is
     * assigned to the target; {@code nested}, to two such calls; {@code wrappedArgument},
     * the generic call is passed to a parameter; {@code wrappedReturn}, it is returned.
     */
    private static String script(String kind, String functor, String context, String legality) {
        Map<String, Object> spec = KINDS[kind]
        Map<String, String> values = (Map<String, String>) spec[legality]
        Closure<String> fill = { String text ->
            values.inject(text) { String result, String name, String value -> result.replaceAll("\\b$name\\b", value) }
        }
        String type = fill((String) spec.type), wrap = spec.wrap, f = spec[functor]
        boolean coerced = legality == 'coerced'
        List<String> use = ((List<String>) spec[coerced ? 'coercedUse' : 'use']).collect(fill)
        List<String> main
        String member = ''
        switch (context) {
            case 'assign':
                main = functor == 'call' ? ((List<String>) spec[coerced ? 'coercedCall' : 'call']).collect(fill) : ["$type v = $f".toString()] + use
                break
            case 'wrapped':
                main = ["$type v = $wrap($f)".toString()] + use
                break
            case 'nested':
                main = ["$type v = $wrap($wrap($f))".toString()] + use
                break
            case 'argument':
                main = ["useIt($f)".toString()]
                member = "static void useIt($type v) { ${use.join('; ')} }"
                break
            case 'wrappedArgument':
                main = ["useIt($wrap($f))".toString()]
                member = "static void useIt($type v) { ${use.join('; ')} }"
                break
            case 'wrappedReturn':
                main = ["$type v = make()".toString()] + use
                member = "static $type make() { return $wrap($f) }"
                break
            default:
                throw new IllegalArgumentException(context)
        }
        """
            import java.util.function.*
            class C {
                $DECLARATIONS
                $member
                static main(args) {
                    ${main.join('\n                    ')}
                }
            }
        """
    }

    protected boolean isStaticCompile() {
        false
    }

    @ParameterizedTest(name = '{0}')
    @MethodSource('cells')
    void testCell(String cell) {
        def (String kind, String functor, String context, String legality) = cell.tokenize(' ')
        String script = script(kind, functor, context, legality)
        String key = "$functor $context"

        if (legality == 'coerced') {
            boolean notPerformed = kind in COERCION_NOT_PERFORMED[key]
                    || (staticCompile && kind in COERCION_NOT_PERFORMED_STATIC_COMPILE_ONLY[key])
            if (kind in COERCION_REJECTED[key]) {
                assert !compiles(script) : "$cell now compiles: remove it from COERCION_REJECTED\n$script"
            } else if (notPerformed) {
                assert compiles(script) : "$cell is now a compile error: move it to COERCION_REJECTED\n$script"
                // the value does not have the type that the functional interface declares
                Throwable failure = null
                try {
                    shell.evaluate(script)
                } catch (Exception | AssertionError e) {
                    failure = e
                }
                assert failure != null : "$cell now converts the value: remove it from COERCION_NOT_PERFORMED\n$script"
            } else {
                assertScript script
            }
        } else if (legality == 'legal') {
            if (kind in LEGAL_REJECTED[key]) {
                assert !compiles(script) : "$cell now compiles: remove it from LEGAL_REJECTED\n$script"
            } else {
                assertScript script
            }
        } else {
            boolean accepted = kind in ILLEGAL_ACCEPTED[key]
                    || (!staticCompile && kind in ILLEGAL_ACCEPTED_TYPE_CHECKED_ONLY[key])
            if (accepted) {
                assert compiles(script) : "$cell is now a compile error: remove it from ILLEGAL_ACCEPTED\n$script"
                // it uses a value at a type that the value does not have
                Throwable failure = null
                try {
                    shell.evaluate(script)
                } catch (Exception | AssertionError e) {
                    failure = e
                }
                assert failure != null : "$cell is illegal, compiles and runs\n$script"
            } else {
                assert !compiles(script) : "$cell is illegal and compiles\n$script"
            }
        }
    }

    // the legal programs run and the illegal ones fail; a coerced program is not run, as
    // dynamic code does not convert a value to a type argument of a functional interface
    @ParameterizedTest(name = '{0}')
    @MethodSource('cells')
    void testCellAsDynamicCode(String cell) {
        Assumptions.assumeFalse(staticCompile)
        def (String kind, String functor, String context, String legality) = cell.tokenize(' ')
        Assumptions.assumeFalse(legality == 'coerced')
        String script = script(kind, functor, context, legality)

        Throwable failure = null
        try {
            new GroovyShell().evaluate(script)
        } catch (Exception | AssertionError e) {
            failure = e
        }
        if (legality == 'legal') {
            assert failure == null : "$cell fails as dynamic code\n$script"
        } else {
            assert failure != null && !(failure instanceof MultipleCompilationErrorsException) : "$cell runs as dynamic code\n$script"
        }
    }

    private boolean compiles(String script) {
        try {
            shell.parse(script)
            true
        } catch (MultipleCompilationErrorsException ignore) {
            false
        }
    }
}

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
 * A cell is <em>legal</em> if the program runs as dynamic Groovy and <em>illegal</em> if it then
 * fails; {@link #testCellAsDynamicCode} checks that, so that a conversion Groovy makes on assignment,
 * such as any value to a {@code String}, is not taken for an error. A legal program should compile
 * and run, and an illegal one should be a compile error.
 * <p>
 * The cells that do not yet behave so are listed in {@link #ILLEGAL_ACCEPTED} and
 * {@link #LEGAL_REJECTED}. A listed cell is checked to behave as listed, so the lists show what
 * is known not to work, and a change that moves a cell either way fails here.
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
            legal: [X: 'Integer'], illegal: [X: 'UUID']
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
        'reference wrapped'        : ['selfBound'],
        'reference nested'         : ['selfBound'],
        'reference wrappedReturn'  : ['selfBound'],
        // GROOVY-12777: a generic call that is itself an argument
        'reference wrappedArgument': ['parameter', 'selfBound']
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
        // a generic call that is itself an argument, as for GROOVY-12777
        'reference wrappedArgument': ['result', 'parameterAndResult']
    ]

    static List<String> cells() {
        List<String> cells = []
        KINDS.each { kind, spec ->
            for (legality in ['legal', 'illegal']) {
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
    private static String script(String kind, String functor, String context, boolean legal) {
        Map<String, Object> spec = KINDS[kind]
        Map<String, String> values = (Map<String, String>) spec[legal ? 'legal' : 'illegal']
        Closure<String> fill = { String text ->
            values.inject(text) { String result, String name, String value -> result.replaceAll("\\b$name\\b", value) }
        }
        String type = fill((String) spec.type), wrap = spec.wrap, f = spec[functor]
        List<String> use = ((List<String>) spec.use).collect(fill)
        List<String> main
        String member = ''
        switch (context) {
            case 'assign':
                main = functor == 'call' ? ((List<String>) spec.call).collect(fill) : ["$type v = $f".toString()] + use
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
        boolean legal = legality == 'legal'
        String script = script(kind, functor, context, legal)
        String key = "$functor $context"

        if (legal) {
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
            } else {
                assert !compiles(script) : "$cell is illegal and compiles\n$script"
            }
        }
    }

    // the legal programs run and the illegal ones fail, so the grid expects what Groovy does
    @ParameterizedTest(name = '{0}')
    @MethodSource('cells')
    void testCellAsDynamicCode(String cell) {
        Assumptions.assumeFalse(staticCompile)
        def (String kind, String functor, String context, String legality) = cell.tokenize(' ')
        boolean legal = legality == 'legal'
        String script = script(kind, functor, context, legal)

        Throwable failure = null
        try {
            new GroovyShell().evaluate(script)
        } catch (Exception | AssertionError e) {
            failure = e
        }
        if (legal) {
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

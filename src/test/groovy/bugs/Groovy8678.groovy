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
package groovy.bugs

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class Groovy8678 {

    @Test
    void testFloatingPointLiteralWithoutLeadingZero() {
        assertScript '''
            def aFloat = .42
            assert (aFloat as String) == '0.42'
            assert aFloat / .21 == 2
            assert aFloat / 2 == .21
        '''
    }

    private class WithMethods {
        def '42'() {
            'FortyTwo'
        }
        def '42d'() {
            'FortyTwoDee'
        }
        def get84() {
            'EightyFour'
        }
        def get84f() {
            'EightyFourEff'
        }
        def call(arg) {
            "Arg: ${arg}"
        }
    }

    @Test
    void testMethodCallQuoted() {
        def x = new WithMethods()
        assert x.'42'() == 'FortyTwo'
        assert x
        // with new line in between
                .'42'() == 'FortyTwo'
        assert x.'42d'() == 'FortyTwoDee'
        assert x.'84' == 'EightyFour'
        assert x.'84f' == 'EightyFourEff'
    }

    @Test
    void testCallWithNumberArgument() {
        def x = new WithMethods()
        assert (x .42 == 'Arg: 0.42')
        assert (x.42 == 'Arg: 0.42')
        assert (x .42f == 'Arg: 0.42')
        assert (x.42f == 'Arg: 0.42')
        assert (x .84d == 'Arg: 0.84')
        assert (x.84f == 'Arg: 0.84')
    }

    @Test
    void testMissingPropertyOrMethod() {
        def x = new WithMethods()
        def throwable = shouldFail () -> x.'42'
        assert throwable.class == MissingPropertyException.class
        assert throwable.message == 'No such property: 42 for class: groovy.bugs.Groovy8678'
        throwable = shouldFail () -> x.'42d'
        assert throwable.class == MissingPropertyException.class
        assert throwable.message == 'No such property: 42d for class: groovy.bugs.Groovy8678'
        throwable = shouldFail () -> x.'84'()
        assert throwable.class == MissingMethodException.class
        assert throwable.message.startsWith('No signature of method: groovy.bugs.Groovy8678.84() is applicable for argument types: () values: []')
        throwable = shouldFail () -> x.'84f'()
        assert throwable.class == MissingMethodException.class
        assert throwable.message.startsWith('No signature of method: groovy.bugs.Groovy8678.84f() is applicable for argument types: () values: []')
    }

    @Test
    @SuppressWarnings("all")
    void testCompilationFailure() {
        shouldNotCompile('\'42\'', '\'FortyTwo\'', '42',
                // after GROOVY-8678
                cls -> cls == MissingMethodException.class,
                msg -> msg.startsWith('No signature of method: WithMethods.call() is applicable for argument types: (BigDecimal) values: [0.42]'))
                // before GROOVY-8678
                // cls -> cls == MultipleCompilationErrorsException.class,
                // msg -> msg.contains('Unexpected input: \'x.42\''))
        shouldNotCompile('\'42\'', '\'FortyTwo\'', '42()',
                // after GROOVY-8678
                cls -> cls == MissingMethodException.class,
                msg -> msg.startsWith('No signature of method: java.math.BigDecimal.call() is applicable for argument types: () values: []'))
                // before GROOVY-8678
                // cls -> cls == MultipleCompilationErrorsException.class,
                // msg -> msg.contains('Unexpected input: \'x.42\''))
        shouldNotCompile('\'42d\'', '\'FortyTwo\'', '42d',
                // after GROOVY-8678
                cls -> cls == MissingMethodException.class,
                msg -> msg.startsWith('No signature of method: WithMethods.call() is applicable for argument types: (Double) values: [0.42]'))
                // before GROOVY-8678
                // cls -> cls == MultipleCompilationErrorsException.class,
                // msg -> msg.contains('Unexpected input: \'x.42d\''))
        shouldNotCompile('\'42d\'', '\'FortyTwo\'', '42d()',
                // after GROOVY-8678
                cls -> cls == MissingMethodException.class,
                msg -> msg.startsWith('No signature of method: java.lang.Double.call() is applicable for argument types: () values: []'))
                // before GROOVY-8678
                // cls -> cls == MultipleCompilationErrorsException.class,
                // msg -> msg.contains('Unexpected input: \'x.42d\''))
        shouldNotCompile('get84', '\'EightyFour\'', '84',
                // after GROOVY-8678
                cls -> cls == MissingMethodException.class,
                msg -> msg.startsWith('No signature of method: WithMethods.call() is applicable for argument types: (BigDecimal) values: [0.84]'))
                // before GROOVY-8678
                // cls -> cls == MultipleCompilationErrorsException.class,
                // msg -> msg.contains('Unexpected input: \'x.84\''))
        shouldNotCompile('get84', '\'EightyFour\'', '84()',
                // after GROOVY-8678
                cls -> cls == MissingMethodException.class,
                msg -> msg.startsWith('No signature of method: java.math.BigDecimal.call() is applicable for argument types: () values: []'))
                // before GROOVY-8678
                // cls -> cls == MultipleCompilationErrorsException.class,
                // msg -> msg.contains('Unexpected input: \'x.84\''))
        shouldNotCompile('get84f', '\'EightyFourEff\'', '84f',
                // after GROOVY-8678
                cls -> cls == MissingMethodException.class,
                msg -> msg.startsWith('No signature of method: WithMethods.call() is applicable for argument types: (Float) values: [0.84]'))
                // before GROOVY-8678
                // cls -> cls == MultipleCompilationErrorsException.class,
                // msg -> msg.contains('Unexpected input: \'x.84f\''))
        shouldNotCompile('get84f', '\'EightyFourEff\'', '84f()',
                // after GROOVY-8678
                cls -> cls == MissingMethodException.class,
                msg -> msg.startsWith('No signature of method: java.lang.Float.call() is applicable for argument types: () values: []'))
                // before GROOVY-8678
                // cls -> cls == MultipleCompilationErrorsException.class,
                // msg -> msg.contains('Unexpected input: \'x.84f\''))
    }

    private static void shouldNotCompile(String methodName,
                                 String returnValue,
                                 String expression,
                                 Closure<Class<? extends Exception>> exceptionAssertion,
                                 Closure<String> messageAssertion) {
        def throwable = shouldFail """
            class WithMethods {
                def ${methodName}() {
                    ${returnValue}
                }
            }
            def x = new WithMethods()
            x.${expression}
        """.stripIndent()
        assert exceptionAssertion(throwable.class)
        assert messageAssertion(throwable.message)
    }

    @Category(BigDecimal)
    private class BigDecimalCategory {
        // using @Category here has a similar effect like
        //     BigDecimal.metaClass.call << { delegate * 2 }
        // but without the side effects on the state of BigDecimal's meta class after the test
        def call() {
            return this * 2
        }
    }

    @Category(Float)
    private class FloatCategory {
        // using @Category here has a similar effect like
        //     Float.metaClass.call << { delegate * 2 }
        // but without the side effects on the state of Float's meta class after the test
        def call() {
            return this * 2
        }
    }

    @Category(Double)
    private class DoubleCategory {
        // using @Category here has a similar effect like
        //     Double.metaClass.call << { delegate * 2 }
        // but without the side effects on the state of Double's meta class after the test
        def call() {
            return this * 2
        }
    }

    @Test
    void testCategories() {
        def x = new WithMethods()
        use (BigDecimalCategory) {
            assert (x.42() == 'Arg: 0.84')
        }
        use (FloatCategory) {
            assert (x.42f() == 'Arg: 0.84')
        }
        use (DoubleCategory) {
            assert (x.42d() == 'Arg: 0.84')
        }
    }
}

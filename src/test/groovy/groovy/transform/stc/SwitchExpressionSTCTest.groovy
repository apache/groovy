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

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * Static type checking for first-class switch expressions (GROOVY-12255 / JEP 361).
 */
class SwitchExpressionSTCTest extends StaticTypeCheckingTestCase {

    @Test
    void testInferredTypeFromArms() {
        assertScript '''
            String s = switch (1) {
                case 1 -> 'a'
                default -> 'b'
            }
            assert s == 'a'
        '''
    }

    @Test
    void testIntResult() {
        assertScript '''
            int n = switch (2) {
                case 1 -> 10
                case 2 -> 20
                default -> 0
            }
            assert n == 20
        '''
    }

    @Test
    void testExhaustiveEnumNeedsNoDefault() {
        assertScript '''
            import java.time.Month
            import static java.time.Month.*

            String q(Month m) {
                switch (m) {
                    case JANUARY, FEBRUARY, MARCH -> 'Q1'
                    case APRIL, MAY, JUNE -> 'Q2'
                    case JULY, AUGUST, SEPTEMBER -> 'Q3'
                    case OCTOBER, NOVEMBER, DECEMBER -> 'Q4'
                }
            }
            assert q(JUNE) == 'Q2'
        '''
    }

    @Test
    void testNonExhaustiveIsError() {
        shouldFailWithMessages '''
            def r = switch (1) {
                case 1 -> 'a'
            }
        ''', 'the switch expression does not cover all possible input values'
    }

    @Test
    void testYieldTypesAreUnified() {
        assertScript '''
            CharSequence cs = switch (1) {
                case 1 -> 'hello'
                default -> new StringBuilder('x')
            }
            assert cs.toString() in ['hello', 'x']
        '''
    }

    @Test
    void testNestedExpressionInsideSwitchStatementDifferentEnums() {
        assertScript '''
            enum Color { RED, BLUE }
            enum Size { S, L }

            int meth(Color color, Size size) {
                switch (color) {
                    case RED:
                        return switch (size) {
                            case S -> 1
                            case L -> 2
                        }
                    case BLUE:
                        return switch (size) {
                            case S -> 3
                            case L -> 4
                        }
                }
            }
            assert meth(Color.RED, Size.S) == 1
            assert meth(Color.BLUE, Size.L) == 4
        '''
    }

    @Test
    void testSwitchStatementNeedNotBeExhaustive() {
        assertScript '''
            int n = 0
            switch (2) {
                case 1:
                    n = 1
            }
            assert n == 0
        '''
    }

    @Test
    void testTargetTypePushedIntoArms() {
        assertScript '''
            CharSequence cs = switch (1) {
                case 1 -> 'hello'
                default -> new StringBuilder('x')
            }
            assert cs instanceof CharSequence
            assert cs.toString() == 'hello'
        '''
    }

    @Test
    void testAssignmentToOuterLocal() {
        assertScript '''
            int acc = 0
            int r = switch (1) {
                case 1 -> {
                    acc = 4
                    yield acc + 1
                }
                default -> 0
            }
            assert r == 5
            assert acc == 4
        '''
    }

    @Test
    void testNativeSealedInterfaceIsExhaustive() {
        assertScript '''
            sealed interface Shape permits Circle, Square {}
            final class Circle implements Shape {}
            final class Square implements Shape {}

            String name(Shape s) {
                return switch (s) {
                    case Circle -> 'c'
                    case Square -> 's'
                }
            }
            assert name(new Circle()) == 'c'
            assert name(new Square()) == 's'
        '''
    }

    @Test
    void testNativeSealedAbstractClassIsExhaustive() {
        assertScript '''
            abstract sealed class Shape permits Circle, Square {}
            final class Circle extends Shape {}
            final class Square extends Shape {}

            @groovy.transform.CompileStatic
            String name(Shape s) {
                return switch (s) {
                    case Circle -> 'c'
                    case Square -> 's'
                }
            }
            assert name(new Circle()) == 'c'
        '''
    }

    @Test
    void testNativeSealedMissingPermittedSubclassIsError() {
        shouldFailWithMessages '''
            sealed interface Shape permits Circle, Square {}
            final class Circle implements Shape {}
            final class Square implements Shape {}

            String name(Shape s) {
                return switch (s) {
                    case Circle -> 'c'
                }
            }
        ''', 'the switch expression does not cover all possible input values'
    }

    @Test
    void testEmulatedSealedSwitchExpressionIsNotExhaustive() {
        shouldFailWithMessages '''
            import groovy.transform.SealedMode
            import groovy.transform.SealedOptions

            @SealedOptions(mode = SealedMode.EMULATE)
            sealed interface Shape permits Circle, Square {}
            final class Circle implements Shape {}
            final class Square implements Shape {}

            String name(Shape s) {
                return switch (s) {
                    case Circle -> 'c'
                    case Square -> 's'
                }
            }
        ''', 'the switch expression does not cover all possible input values'
    }

    @Test
    void testDuplicatePermittedSubclassLabelIsError() {
        shouldFailWithMessages '''
            sealed interface Shape permits Circle, Square {}
            final class Circle implements Shape {}
            final class Square implements Shape {}

            String name(Shape s) {
                return switch (s) {
                    case Circle -> 'c'
                    case Circle -> 'again'
                    case Square -> 's'
                }
            }
        ''', '[Static type checking] - Duplicate case label: Circle'
    }

    @Test
    void testObjectTypeCaseDoesNotMakeSealedSwitchExhaustive() {
        shouldFailWithMessages '''
            sealed interface Shape permits Circle, Square {}
            final class Circle implements Shape {}
            final class Square implements Shape {}

            String name(Shape s) {
                return switch (s) {
                    case Object -> 'any'
                }
            }
        ''', 'the switch expression does not cover all possible input values'
    }

    @Test
    void testStringLabelDoesNotCollideWithPermittedSubclassType() {
        assertScript '''
            sealed interface Shape permits Circle, Square {}
            final class Circle implements Shape {}
            final class Square implements Shape {}

            String name(Shape s) {
                return switch (s) {
                    case Circle -> 'c'
                    case 'Circle' -> 'str'
                    case Square -> 's'
                }
            }
            assert name(new Circle()) == 'c'
        '''
    }

    @Test
    void testSealedSwitchStatementNeedNotBeExhaustive() {
        assertScript '''
            sealed interface Shape permits Circle, Square {}
            final class Circle implements Shape {}
            final class Square implements Shape {}

            int n = 0
            Shape s = new Circle()
            switch (s) {
                case Circle:
                    n = 1
            }
            assert n == 1
        '''
    }

    @Test
    void testConcreteSealedClassRequiresCaseForTheTypeItself() {
        shouldFailWithMessages '''
            sealed class Shape permits Circle, Square {}
            final class Circle extends Shape {}
            final class Square extends Shape {}

            String name(Shape s) {
                return switch (s) {
                    case Circle -> 'c'
                    case Square -> 's'
                }
            }
        ''', 'the switch expression does not cover all possible input values'
    }

    @Test
    void testConcreteSealedClassCoveredByCaseOnTheType() {
        assertScript '''
            sealed class Shape permits Circle, Square {}
            final class Circle extends Shape {}
            final class Square extends Shape {}

            String name(Shape s) {
                return switch (s) {
                    case Shape -> 'any'
                }
            }
            assert name(new Shape()) == 'any'
            assert name(new Circle()) == 'any'
        '''
    }

    @Test
    void testLeafCasesDoNotCoverPermittedParent() {
        shouldFailWithMessages '''
            sealed interface Shape permits Circle, Rect {}
            final class Circle implements Shape {}
            sealed class Rect implements Shape permits Square, Triangle {}
            final class Square extends Rect {}
            final class Triangle extends Rect {}

            String name(Shape s) {
                return switch (s) {
                    case Circle -> 'c'
                    case Square -> 's'
                    case Triangle -> 't'
                }
            }
        ''', 'the switch expression does not cover all possible input values'
    }

    @Test
    void testPermittedParentCaseCoversItsSubtypes() {
        assertScript '''
            sealed interface Shape permits Circle, Rect {}
            final class Circle implements Shape {}
            sealed class Rect implements Shape permits Square, Triangle {}
            final class Square extends Rect {}
            final class Triangle extends Rect {}

            String name(Shape s) {
                return switch (s) {
                    case Circle -> 'c'
                    case Rect -> 'r'
                }
            }
            assert name(new Square()) == 'r'
        '''
    }

    @Test
    void testNativeSealedFromClassFileIsExhaustive() {
        File target = File.createTempDir()
        try {
            def loader = new GroovyClassLoader(this.class.classLoader, new CompilerConfiguration(targetDirectory: target))
            loader.parseClass('''
                package stc.sealed
                sealed interface Shape permits Circle, Square {}
                final class Circle implements Shape {}
                final class Square implements Shape {}
            ''')
            def isolated = new GroovyClassLoader(loader, config)
            def isolatedShell = new GroovyShell(isolated, config)
            isolatedShell.evaluate('''
                package stc.sealed
                String name(Shape s) {
                    return switch (s) {
                        case Circle -> 'c'
                        case Square -> 's'
                    }
                }
                assert name(new Circle()) == 'c'
            ''', 'SealedFromClassFile.groovy')
        } finally {
            target.deleteDir()
        }
    }

    @Test
    void testEmulatedSealedClassFileIsNotClosed() {
        File target = File.createTempDir()
        try {
            def loader = new GroovyClassLoader(this.class.classLoader, new CompilerConfiguration(targetDirectory: target))
            loader.parseClass('''
                package stc.emulated
                import groovy.transform.SealedMode
                import groovy.transform.SealedOptions
                @SealedOptions(mode = SealedMode.EMULATE)
                sealed interface Shape permits Circle, Square {}
                final class Circle implements Shape {}
                final class Square implements Shape {}
            ''')
            def isolated = new GroovyClassLoader(loader, config)
            def isolatedShell = new GroovyShell(isolated, config)
            def err = shouldFail(MultipleCompilationErrorsException) {
                isolatedShell.evaluate('''
                    package stc.emulated
                    String name(Shape s) {
                        return switch (s) {
                            case Circle -> 'c'
                            case Square -> 's'
                        }
                    }
                ''', 'EmulatedFromClassFile.groovy')
            }
            assert err.message.contains('the switch expression does not cover all possible input values')
        } finally {
            target.deleteDir()
        }
    }
}

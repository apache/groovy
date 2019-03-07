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
package org.codehaus.groovy.transform.tailrec

class TailRecursiveTransformationTest extends GroovyShellTestCase {

    void testSimpleRecursiveMethod() {
        def target = evaluate("""
            import groovy.transform.TailRecursive
            class TargetClass {
            	@TailRecursive
            	int countDown(int zahl) {
            		if (zahl == 0)
            			return zahl
            		countDown(zahl - 1)
            	}
            }
            new TargetClass()
        """)
        assert target.countDown(5) == 0
        assert target.countDown(100000) == 0 //wouldn't work with real recursion
    }

    void testSimpleStaticRecursiveMethod() {
        def target = evaluate("""
            import groovy.transform.TailRecursive
            class TargetClass {
            	@TailRecursive
            	static int staticCountDown(int zahl) {
            		if (zahl == 0)
            			return zahl
            		return staticCountDown(zahl - 1)
            	}
            }
            new TargetClass()
        """)
        assert target.staticCountDown(5) == 0
        assert target.staticCountDown(100000) == 0
    }

    void testRecursiveFunctionWithTwoParameters() {
        def target = evaluate('''
            import groovy.transform.TailRecursive
            class TargetClass {
				@TailRecursive
				long sumUp(long number, long sum) {
					if (number == 0)
						return sum;
					sumUp(number - 1, sum + number)
				}
        	}
            new TargetClass()
		''')

        assert target.sumUp(0, 0) == 0
        assert target.sumUp(1, 0) == 1
        assert target.sumUp(5, 0) == 15
        assert target.sumUp(1000000, 0) == 500000500000
    }

    void testRecursiveFunctionWithTwoRecursiveCalls() {
        def target = evaluate('''
			import groovy.transform.TailRecursive
			class TargetClass {
				@TailRecursive
				int countDown(int number) {
					if (number == 0)
						return number;
					if (number < 10)
						return countDown(number - 1)
					else
						return countDown(number - 1)
				}
			}
			new TargetClass()
		''')

        assert target.countDown(0) == 0
        assert target.countDown(9) == 0
        assert target.countDown(100) == 0
    }

    void testRecursiveFunctionWithReturnInForLoop() {
        def target = evaluate('''
			import groovy.transform.TailRecursive
			class TargetClass {
				@TailRecursive
				int countDownWithFor(int number) {
					if (number <= 1)
						return number;
					for (int i = number - 1; i > 0; i++)
						return countDownWithFor(i);
				}
			}
			new TargetClass()
		''')

        assert target.countDownWithFor(0) == 0
        assert target.countDownWithFor(9) == 1
        assert target.countDownWithFor(100) == 1
    }

    void testRecursiveFunctionWithTernaryOperator() {
        // for loops can have "continue" thus the while-iteration's continue might not work
        def target = evaluate('''
			import groovy.transform.TailRecursive
			class TargetClass {
				@TailRecursive
				int countDownWithTernary(int number) {
				    (number == 0) ? number : countDownWithTernary(number - 1)
				}
			}
			new TargetClass()
		''')

        assert target.countDownWithTernary(0) == 0
        assert target.countDownWithTernary(9) == 0
        assert target.countDownWithTernary(100) == 0
    }

    void testNestedRecursiveTernaryOperator() {
        def target = evaluate('''
			import groovy.transform.TailRecursive
			class TargetClass {
				@TailRecursive
				int countDownWithTernary(int number) {
				    if (number == 0)
                        (true) ? 0 : countDownWithTernary(number - 1)
                    else
                        (false) ? 0 : countDownWithTernary(number - 1)
				}
			}
			new TargetClass()
		''')

        assert target.countDownWithTernary(0) == 0
        assert target.countDownWithTernary(9) == 0
        assert target.countDownWithTernary(100) == 0
    }

    /*
        Is covered by Ternary Operator measures b/c ElvisOperatorExpression is subclass of TernaryOperatorExpression
     */

    void testNestedRecursiveElvisOperator() {
        def target = evaluate('''
			import groovy.transform.TailRecursive
			class TargetClass {
				@TailRecursive
				def countDownWithElvis(int number) {
				    if (number == 0)
                        (true) ?: countDownWithElvis(number - 1)
                    else
                        (false) ?: countDownWithElvis(number - 1)
				}
			}
			new TargetClass()
		''')

        assert target.countDownWithElvis(0) == true
        assert target.countDownWithElvis(9) == true
        assert target.countDownWithElvis(100) == true
    }


    void testRecursiveCallInTryCatch() {
        def target = evaluate('''
			import groovy.transform.TailRecursive
			class TargetClass {
				@TailRecursive
				int countDownInTryCatch(int number) {
				    try {
                        (number == 0) ? 0 : countDownInTryCatch(number - 1)
				    } catch (Exception e) {}
				    finally {}
				}
			}
			new TargetClass()
		''')

        assert target.countDownInTryCatch(0) == 0
        assert target.countDownInTryCatch(9) == 0
        assert target.countDownInTryCatch(100) == 0
    }

    void testRecursiveCallInSwitchStatement() {
        def target = evaluate('''
			import groovy.transform.TailRecursive
			class TargetClass {
				@TailRecursive
				int countDownInSwitch(int number) {
                    switch(number) {
                        case 0: -1; break;
                        default: countDownInSwitch(number -1)
                    }
				}
			}
			new TargetClass()
		''')

        assert target.countDownInSwitch(0) == -1
        assert target.countDownInSwitch(9) == -1
        assert target.countDownInSwitch(100) == -1
    }

    void testRecursiveCallWithVariableInBooleanExpression() {
        def target = evaluate('''
			import groovy.transform.TailRecursive
			class TargetClass {
				@TailRecursive
				int stringSize(String s, int size = 0) {
				    if (s)
				        return stringSize(s.substring(1), 1+size)
				    return size
				}
			}
			new TargetClass()
		''')

        assert target.stringSize('') == 0
        assert target.stringSize('a') == 1
        assert target.stringSize('abcdef') == 6
    }

    void testRecursiveCallWithVariableInNotExpression() {
        def target = evaluate('''
			import groovy.transform.TailRecursive
			class TargetClass {
				@TailRecursive
				int stringSize(String s, int size = 0) {
				    if (!s)
				        return size
				    return stringSize(s.substring(1), 1+size)
				}
			}
			new TargetClass()
		''')

        assert target.stringSize('') == 0
        assert target.stringSize('a') == 1
        assert target.stringSize('abcdef') == 6
    }

    void testRecursiveCallWithVariableInUnaryMinusExpression() {
        def target = evaluate('''
			import groovy.transform.TailRecursive
			class TargetClass {
				@TailRecursive
				def enumerateNegative(int downFrom, list = []) {
				    if (downFrom == 0)
				        return list
				    return enumerateNegative(downFrom - 1, list << -downFrom)
				}
			}
			new TargetClass()
		''')

        assert target.enumerateNegative(0) == []
        assert target.enumerateNegative(1) == [-1]
        assert target.enumerateNegative(9) == [-9, -8, -7, -6, -5, -4, -3, -2, -1]
    }

    void testVariableScopes() {
        def target = evaluate('''
			import groovy.transform.TailRecursive
			class TargetClass {
			    def argsFromClosure = []
				@TailRecursive
				int countDown(int number) {
				    int oops = number
				    def c = { -> number }
				    argsFromClosure << c()
				    if (number == 0)
				        return -1
				    return countDown(c() - 1)
				}
			}
			new TargetClass()
		''')

        assert target.countDown(2) == -1
        assert target.argsFromClosure == [2, 1, 0]
    }

    void testSimpleContinuousPassingStyle() {
        def target = evaluate('''
			import groovy.transform.TailRecursive
			class TargetClass {
				@TailRecursive
                long factorial(int number, Closure continuation = {it}) {
                    if (number <= 1)
                        return continuation(1)
                    return factorial(number - 1, { x ->
                        return continuation(x * number)
                    })
                }
            }
			new TargetClass()
		''')

        assert target.factorial(1) == 1
        assert target.factorial(2) == 2
    }

    void testRecursiveCallInEmbeddedClosures() {
        def target = evaluate('''
            import groovy.transform.TailRecursive
            class TargetClass {
                @TailRecursive
                int sum(int n, res = 0) {
                    if (n == 0)
                        return res
                    def c = {sum(n - 1, res + n)}
                    return c()
                }
            }
            new TargetClass()
        ''')
        assert target.sum(0) == 0
        assert target.sum(1) == 1
        assert target.sum(100) == 5050
    }

    /**
     * https://issues.apache.org/jira/browse/GROOVY-6573
     */
    void testParameterIsUsedInRecursiveArgPositionedEarlier() {
        def target = evaluate('''
            import groovy.transform.TailRecursive
            class TargetClass {
                @TailRecursive
                BigInteger iterate(final BigInteger i, final BigInteger a, final BigInteger b) {
                    i < 1 ? a : iterate(i - 1, b, a + b)
                }
            }
            new TargetClass()
        ''')
        assert target.iterate(0, 0, 1) == 0
        assert target.iterate(1, 0, 1) == 1
        assert target.iterate(10, 1, 1) == 89
    }

}

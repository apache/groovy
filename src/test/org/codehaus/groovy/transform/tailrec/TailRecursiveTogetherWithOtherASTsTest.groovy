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

class TailRecursiveTogetherWithOtherASTsTest extends GroovyShellTestCase {

    void testStaticallyCompiledRecursiveMethod() {
        def target = evaluate("""
            import groovy.transform.TailRecursive
            import groovy.transform.CompileStatic

            @CompileStatic
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

    void testTypeCheckedRecursiveMethod() {
        def target = evaluate('''
            import groovy.transform.TailRecursive
            import groovy.transform.TypeChecked

            @TypeChecked
            class TargetClass {
				@TailRecursive
				String fillString(long number, String filled) {
					if (number == 0)
						return filled;
					fillString(number - 1, filled + "+")
				}
        	}
            new TargetClass()
		''')

        assert target.fillString(0, "") == ""
        assert target.fillString(1, "") == "+"
        assert target.fillString(5, "") == "+++++"
        assert target.fillString(10000, "") == "+" * 10000
    }

    void testStaticallyCompiledSumDown() {
        def target = evaluate('''
            import groovy.transform.TailRecursive
            import groovy.transform.CompileStatic

            @CompileStatic
            class TargetClass {
				@TailRecursive
                long sumDown(long number, long sum = 0) {
                    (number == 0) ? sum : sumDown(number - 1, sum + number)
                }
        	}
            new TargetClass()
		''')

        assert target.sumDown(0) == 0
        assert target.sumDown(5) == 5 + 4 + 3 + 2 + 1
        assert target.sumDown(100) == 5050
        assert target.sumDown(1000000) == 500000500000
    }

    void testStaticallyCompiledRecursiveFunctionWithTwoParameters() {
        def target = evaluate('''
            import groovy.transform.TailRecursive
            import groovy.transform.CompileStatic

            @CompileStatic
            class TargetClass {
				@TailRecursive
				String fillString(long number, String filled) {
					if (number == 0)
						return filled;
					fillString(number - 1, filled + "+")
				}
        	}
            new TargetClass()
		''')

        assert target.fillString(0, "") == ""
        assert target.fillString(1, "") == "+"
        assert target.fillString(5, "") == "+++++"
        assert target.fillString(10000, "") == "+" * 10000
    }

    void testTailRecursiveFirstWorksWithMemoized() {
        def target = evaluate("""
            import groovy.transform.Memoized
            import groovy.transform.TailRecursive

            class TargetClass {
                int countActualInvocations = 0
            	@TailRecursive
                @Memoized
            	int countDown(int zahl) {
            		if (zahl == 0) {
            		    countActualInvocations++
            			return zahl
            	    }
            		return countDown(zahl - 1)
            	}
            }
            new TargetClass()
        """)

        assert target.countDown(10) == 0
        assert target.countActualInvocations == 1
        assert target.countDown(10) == 0
        assert target.countActualInvocations == 1
    }

}

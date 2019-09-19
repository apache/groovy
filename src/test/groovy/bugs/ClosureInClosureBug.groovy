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

import groovy.test.GroovyTestCase

/**
 * Bug illustrating the nested closures variable scope visibility issue.
 * l.each is ClosureInClosureBug$1 and it.each is ClosureInClosureBug$2
 * The variable text is not visible from ClosureInClosureBug$2.
 * Indeed, a closure can only see the variable defined outside this closure (one level up)
 * but cannot see what's in the second level.
 *
 * In order to make the test work, do not forget to uncomment the line "println(text)"
 *
 * @authour Guillaume Laforge
 */
class ClosureInClosureBug extends GroovyTestCase {

    void testInvisibleVariable() {
        def text = "test "

        def l = [1..11, 2..12, 3..13, 4..14]

        l.each {
            //println(text)
            it.each{
                println(text)
            }
        }
    }

    static void main(args) {
        def bug = new ClosureInClosureBug()
        bug.testInvisibleVariable()
    }
}
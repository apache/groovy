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
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.ClosuresSTCTest

/**
 * Unit tests for static compilation: closures.
 */
class ClosuresStaticCompileTest extends ClosuresSTCTest implements StaticCompilationTestSupport {

    // GROOVY-5584
    void testEachOnMapClosure() {
        assertScript '''
                def test() {
                    def result = ""
                    [a:1, b:3].each { key, value -> result += "$key$value" }
                    assert result == "a1b3"
                }
                test()'''
    }

    // GROOVY-5811
    void testClosureExceptionUnwrap() {
        assertScript '''
            @groovy.transform.InheritConstructors
            public class MyException extends Exception {}

            void foo() {
                throw new MyException()
            }

            void bar() {
                boolean caught = false
                try {
                    def cl = { foo() }
                    cl()
                } catch (MyException e) {
                    caught = true
                } finally {
                    assert caught
                }
            }

            bar()
        '''
    }

    // GROOVY-6522
    void testShouldCallClosure() {
        assertScript '''
class Sample {

    Closure formatFirstName
    Closure formatLastName

    void doStuff (String fname, String lname, Closure callback) {
        formatFirstName(fname) { String fnameError, String formattedFname ->
            formatLastName(lname) { String lnameError, String formattedLname ->
                String errors = "${fnameError ? "${fnameError}, " : ''}${lnameError ?: ''}"
                callback(errors, formattedFname, formattedLname)
            }
        }
    }

}

Closure ffn = { String fname, Closure callback ->
    String firstInitial = fname?.substring(0,1)

    if (!firstInitial)
        callback('invalid first name', null)
    else
        callback(null, firstInitial.toLowerCase())
}

Closure fln = { String lname, Closure callback ->
    String lastPrefix = lname?.size() > 2 ? lname.substring(0,3) : null

    if (!lastPrefix)
        callback('invalid last name', null)
    else
        callback(null, lastPrefix.toLowerCase())
}

Sample sample = new Sample(formatFirstName: ffn, formatLastName: fln)

sample.doStuff('John', 'Doe') { String errors, String formattedFname, String formattedLname ->
    if (errors)
        println errors
    else
        println "${formattedFname}.${formattedLname}"
}'''
    }
}


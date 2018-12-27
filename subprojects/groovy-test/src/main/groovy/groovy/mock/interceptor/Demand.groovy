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
package groovy.mock.interceptor

import junit.framework.AssertionFailedError

/**
 *  The object that registers method calls on it for the use with Mocks and Stubs.
 *  For each call a CallSpec object is added to the recorded list.
 */

class Demand {

    List recorded = []
    Map ignore = [:]

    Object invokeMethod(String methodName, Object args) {
        def range = 1..1
        if (args[0] instanceof IntRange) {
            range = args[0]
            if (range.reverse) throw new IllegalArgumentException('Reverse ranges not supported.')
        } else if (args[0] instanceof Integer) {
            range = args[0]..args[0]
        }
        if (args[-1] instanceof Closure) {
            recorded << new CallSpec(name:methodName, behavior:args[-1], range:range)
        }
    }

    def verify(List calls) {
        for (i in 0 ..< recorded.size()) {
            def call = recorded[i]
            def callCounter = calls[i] ? calls[i] : 0
            if (! call.range.contains( callCounter ) ) {
                def msg = "verify[$i]: expected ${call.range.toString()} call(s) to '${call.name}' but was "
                throw new AssertionFailedError(msg + "called $callCounter time(s).")
            }
        }
    }

}

class CallSpec {
    String  name
    Closure behavior
    Range   range
}
